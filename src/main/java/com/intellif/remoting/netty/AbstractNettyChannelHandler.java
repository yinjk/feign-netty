package com.intellif.remoting.netty;

import com.intellif.feign.transfer.Message;
import com.intellif.remoting.RemotingException;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractNettyChannelHandler implements NettyChannelHandler {

    private ThreadLocal<Message> nettyResult = new ThreadLocal<>();

    private Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>(); // uuid : latch

    @Override
    public void connected(Channel channel) throws RemotingException {
        //doing nothing...
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        //doing nothing...
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        //doing nothing...
    }

    @Override
    public Object getResult(String uuid) throws RemotingException {
        return null;
    }

    @Override
    public void setLatch(String uuid, CountDownLatch latch) {
        //doing nothing
    }
}
