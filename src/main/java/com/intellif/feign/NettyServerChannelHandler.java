package com.intellif.feign;

import com.alibaba.fastjson.JSON;
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
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author inori
 */
public class NettyServerChannelHandler extends AbstractNettyChannelHandler {

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyServerChannelHandler.class);

    private ThreadPoolExecutor executor;

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
//                System.out.printf("server start received request: => %d \n", new Date().getTime());
                TransferRequest req = JSON.parseObject((String) message, TransferRequest.class);
//                System.out.printf("server received request: %s => %d \n", req.getUuid(), new Date().getTime());
                TransferResponse response;
                try {
                    response = mockHttpClient.execute(req);
//                    System.out.printf("server completed request: %s => %d \n", req.getUuid(), new Date().getTime());
                } catch (Exception e) {
                    //服务端执行请求过程中出现了任何异常，都将该异常直接返回给客户端
//                    log.error(e.getMessage());
                    TransferResponse errorResponse = TransferResponse.createErrorResponse(req, e.getMessage());
                    channel.writeAndFlush(JSON.toJSONString(errorResponse)); //将错误消息写回客户端
                    return;
                }
                // 将请求的执行结果返回给客户端
                String responseJson = JSON.toJSONString(response);
//                System.out.printf("server json format request: %s => %d \n", req.getUuid(), new Date().getTime());
                channel.writeAndFlush(responseJson); //回复消息
//                System.out.printf("server send response: %s => %d \n", response.getUuid(), new Date().getTime());
//                System.out.printf("hand request: %s => %d ms \n", req.getUuid(), new Date().getTime() - starTime);
//                log.error(" =====================测试（测试完成之后请删除）================ ");
//                log.error("|                  execute by netty server!                |");
//                log.error(" =====================测试（测试完成之后请删除）================ ");
            });
        } catch (RejectedExecutionException e) { //阻塞队列满了，直接拒绝所有连接，返回服务器当前忙
            System.out.println("服务器忙，主动拒绝响应。。。");
            TransferRequest req = JSON.parseObject((String) message, TransferRequest.class);
            TransferResponse errorResponse = TransferResponse.create503ErrorResponse(req);
            //直接返回，状态码为503，表示当前服务器忙碌，暂时不可用
            channel.writeAndFlush(JSON.toJSONString(errorResponse));
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        if (exception instanceof IOException) {
            String remoteService = NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress());
            log.info("The remote client closed the current connection:[" + remoteService + "]");
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

}
