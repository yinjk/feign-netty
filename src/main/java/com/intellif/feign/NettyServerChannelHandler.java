package com.intellif.feign;

import com.alibaba.fastjson.JSON;
import com.intellif.feign.transfer.RequestMessage;
import com.intellif.feign.transfer.ResponseMessage;
import com.intellif.feign.transfer.TransferRequest;
import com.intellif.feign.transfer.TransferResponse;
import com.intellif.mockhttp.MockHttpClient;
import com.intellif.remoting.RemotingException;
import com.intellif.remoting.netty.AbstractNettyChannelHandler;
import com.intellif.remoting.netty.NetUtils;
import io.netty.channel.Channel;
import org.apache.tomcat.util.threads.TaskQueue;
import org.apache.tomcat.util.threads.TaskThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author inori
 */
public class NettyServerChannelHandler extends AbstractNettyChannelHandler {

    /**
     * 为了避免频繁创建对象，这里把空header做成静态常量
     */
    private static Map<String, Collection<String>> emptyHeaders = new HashMap<>(0);

    private ThreadPoolExecutor executor;

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyServerChannelHandler.class);

    private MockHttpClient mockHttpClient;


    public NettyServerChannelHandler(DispatcherServlet dispatcherServlet) {
        this.mockHttpClient = new MockHttpClient(dispatcherServlet);
        createExecutor();
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        log.debug("the client [" + NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress()) + "] is connected");
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        log.debug("the client [" + NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress()) + "] is disconnected");
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        //doing nothing...
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        try {
            executor.execute(() -> { //异步处理http请求
                long starTime = new Date().getTime();
                System.out.printf("server start received request: => %d \n", new Date().getTime());
                RequestMessage req = JSON.parseObject((String) message, RequestMessage.class);
                System.out.printf("server received request: %s => %d \n", req.getUuid(), new Date().getTime());
                TransferResponse response;
                try {
                    response = mockHttpClient.execute(req.getData().toFeignRequest());
                    System.out.printf("server completed request: %s => %d \n", req.getUuid(), new Date().getTime());
                } catch (Exception e) {
                    log.error(e.getMessage());
                    TransferResponse errorResponse = createErrorResponse(req.getData(), e.getMessage());
                    ResponseMessage responseMessage = new ResponseMessage(req.getUuid(), errorResponse);
                    channel.writeAndFlush(JSON.toJSONString(responseMessage)); //将错误消息写回客户端
                    return;
                }
                // 把消息返回给客户端
                ResponseMessage responseMessage = new ResponseMessage(req.getUuid(), response);
                String responseJson = JSON.toJSONString(responseMessage);
                System.out.printf("server json format request: %s => %d \n", req.getUuid(), new Date().getTime());
                channel.writeAndFlush(responseJson); //回复消息
                System.out.printf("server send response: %s => %d \n", responseMessage.getUuid(), new Date().getTime());
                System.out.printf("hand request: %s => %d ms \n", req.getUuid(), new Date().getTime() - starTime);
            });
        } catch (RejectedExecutionException e) { //阻塞队列满了，直接拒绝所有连接
            RequestMessage req = JSON.parseObject((String) message, RequestMessage.class);
            TransferResponse errorResponse = create503ErrorResponse(req.getData());
            //直接返回，状态码为503，表示当前服务器忙碌，暂时不可用
            channel.writeAndFlush(JSON.toJSONString(new ResponseMessage(req.getUuid(), errorResponse)));
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        //TODO: hand this exception
        if (exception instanceof IOException) {
            String remoteService = NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress());
            log.info("the remote client close the current connection:[" + remoteService + "]");
        }
    }

    /**
     * 创建线程执行executor
     */
    private void createExecutor() {
        TaskQueue taskqueue = new TaskQueue(10000);
        TaskThreadFactory tf = new TaskThreadFactory("netty-server" + "-exec-", true, 5);
        executor = new org.apache.tomcat.util.threads.ThreadPoolExecutor(10, 200, 60, TimeUnit.SECONDS, taskqueue, tf);
        taskqueue.setParent((org.apache.tomcat.util.threads.ThreadPoolExecutor) executor);
    }

    /**
     * 处理请求抛出了任何异常，创建transfer response，直接返回500，并且把异常信息传回去。
     *
     * @param request 远程传输request
     * @param errMsg  错误信息
     * @return TransferResponse
     */
    private TransferResponse createErrorResponse(TransferRequest request, String errMsg) {
        errMsg = errMsg == null ? "" : errMsg;
        return new TransferResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), errMsg, emptyHeaders, errMsg.getBytes(), request);
    }


    /**
     * 服务器繁忙，暂时无法处理更多的请求（线程池拒绝的请求），直接返回503，并告诉客户端当前服务器繁忙
     *
     * @param request 远程传输request
     * @return TransferResponse
     */
    private TransferResponse create503ErrorResponse(TransferRequest request) {
        String reason = "The server is busy. Please try again later";
        return new TransferResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), reason, emptyHeaders, reason.getBytes(), request);
    }


}
