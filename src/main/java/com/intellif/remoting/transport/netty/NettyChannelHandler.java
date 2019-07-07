package com.intellif.remoting.transport.netty;

import com.intellif.remoting.RemotingException;
import io.netty.channel.Channel;

import java.util.concurrent.CountDownLatch;

public interface NettyChannelHandler {


    /**
     * on channel connected.
     *
     * @param channel channel.
     */
    default void connected(Channel channel) throws RemotingException {
        //doing nothing...
    }

    /**
     * on channel disconnected.
     *
     * @param channel channel.
     */
    default void disconnected(Channel channel) throws RemotingException {
        //doing nothing...
    }

    /**
     * on message sent.
     *
     * @param channel channel.
     * @param message message.
     */
    void sent(Channel channel, Object message) throws RemotingException;

    /**
     * on message received.
     *
     * @param channel channel.
     * @param message message.
     */
    void received(Channel channel, Object message) throws RemotingException;

    /**
     * on exception caught.
     *
     * @param channel   channel.
     * @param exception exception.
     */
    void caught(Channel channel, Throwable exception) throws RemotingException;


    /**
     * if your client want to get a sync result, please implement it!
     *
     * @return the netty result from remote
     * @throws RemotingException
     */
    default Object getResult(String uuid) throws RemotingException {
        return null;
    }

    /**
     * if you want to set latch and sync result, please implement it!
     *
     * @param uuid 请求的唯一id
     * @param latch 用于等待请求的超时，
     */
    default void setLatch(String uuid, final CountDownLatch latch) {

    }

}