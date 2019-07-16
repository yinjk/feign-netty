package com.intellif.feign;

import com.alibaba.fastjson.JSON;
import com.intellif.feign.transfer.RequestMessage;
import com.intellif.feign.transfer.ResponseMessage;
import com.intellif.feign.transfer.TransferResponse;
import com.intellif.mockhttp.MockHttpClient;
import com.intellif.remoting.RemotingException;
import com.intellif.remoting.netty.AbstractNettyChannelHandler;
import com.intellif.remoting.netty.NetUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author inori
 */
public class NettyServerChannelHandler extends AbstractNettyChannelHandler {

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyServerChannelHandler.class);

    private MockHttpClient mockHttpClient;


    public NettyServerChannelHandler(DispatcherServlet dispatcherServlet) {
        this.mockHttpClient = new MockHttpClient(dispatcherServlet);
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
        RequestMessage req = JSON.parseObject((String) message, RequestMessage.class);
        TransferResponse response;
        try {
            response = mockHttpClient.execute(req.getData().toFeignRequest());
        } catch (Exception e) {
            //TODO: 处理这个异常
            log.error(e.getMessage());
            throw new RemotingException(channel, e.getMessage());
        }
        // 把消息返回给客户端
        ResponseMessage responseMessage = new ResponseMessage(req.getUuid(), response);
        String responseJson = JSON.toJSONString(responseMessage);
        channel.writeAndFlush(responseJson); //回复消息
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        //TODO: hand this exception
        if (exception instanceof IOException) {
            String remoteService = NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress());
            log.info("the remote client close the current connection:[" + remoteService + "]");
        }
    }
}
