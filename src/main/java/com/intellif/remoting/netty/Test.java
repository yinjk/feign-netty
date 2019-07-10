package com.intellif.remoting.netty;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * @author inori
 * @create 2019-07-05 17:04
 */
public class Test {
    public static void main(String[] args) throws Throwable {
        NettyChannelHandler serverHandler = new AbstractNettyChannelHandler() {
            @Override
            public void connected(Channel channel) {
                channel.writeAndFlush("hello");
            }

            @Override
            public void disconnected(Channel channel) {
                channel.writeAndFlush("see you");
            }

            @Override
            public void sent(Channel channel, Object message) {
                System.out.println("server sent: " + message);
            }

            @Override
            public void received(Channel channel, Object message) {
                System.out.println("server received:" + message);
                channel.writeAndFlush("i'm received your message: " + message);
            }

            @Override
            public void caught(Channel channel, Throwable exception) {
                System.out.println(NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress()));
            }
        };
        NettyChannelHandler clientHandler = new AbstractNettyChannelHandler() {
            @Override
            public void connected(Channel channel) {
                channel.writeAndFlush("client connected");
            }

            @Override
            public void disconnected(Channel channel) {
                channel.writeAndFlush("886");
            }

            @Override
            public void sent(Channel channel, Object message) {
                System.out.println("client[" + channel.localAddress() + "] sent to[" + channel.remoteAddress() + "]: " + message);
            }

            @Override
            public void received(Channel channel, Object message) {
                System.out.println("client[" + channel.localAddress() + "] received[" + channel.remoteAddress() + "]: " + message);
            }

            @Override
            public void caught(Channel channel, Throwable exception) {
                System.out.println(NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress()));
            }
        };

//        NettyServer nettyServer = new NettyServer(9090, serverHandler);
        NettyClient nettyClient1 = new NettyClient("localhost", 9090, clientHandler);
        NettyClient nettyClient2 = new NettyClient("localhost", 9090, clientHandler);
        nettyClient1.send("The rain is down", false);
        nettyClient2.send("The moon is beautiful", false);

        CountDownLatch latch = new CountDownLatch(1);
        latch.await();


    }
}