package com.intellif.remoting.transport.netty;

import com.intellif.feign.Message;
import com.intellif.remoting.RemotingException;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class AbstractNettyChannelHandler implements NettyChannelHandler {

    private ThreadLocal<Message> nettyResult = new ThreadLocal<>();

    private Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>(); // uuid : latch

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {

    }
}
