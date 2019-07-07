package com.intellif.feign;

import com.alibaba.fastjson.JSON;
import com.intellif.remoting.RemotingException;
import com.intellif.remoting.transport.netty.NetUtils;
import com.intellif.remoting.transport.netty.NettyChannelHandler;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * author: yinjk
 */
public class NettyServerChannelHandler implements NettyChannelHandler {

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyServerChannelHandler.class);

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
        Message req = JSON.parseObject((String) message, Message.class);
        channel.writeAndFlush(JSON.toJSONString(new Message(req.getUuid(), "i'm ok!"))); //回复消息
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress());
    }
}
