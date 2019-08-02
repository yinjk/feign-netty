package com.intellif.remoting.netty;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author inori
 * @create 2019-07-05 17:04
 */
public class Test {
    public static void main1(String[] args) throws Throwable {
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
                long sendTime = Long.parseLong(message.toString());
                long time = new Date().getTime() - sendTime;
                System.out.printf("receive time %d ms \n", time);
                channel.writeAndFlush(new Date().getTime() + "");
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
//                System.out.println("client[" + channel.localAddress() + "] sent to[" + channel.remoteAddress() + "]: " + message);
            }

            @Override
            public void received(Channel channel, Object message) {
//                System.out.println("client[" + channel.localAddress() + "] received[" + channel.remoteAddress() + "]: " + message);
                System.out.printf("client [%s] received [%s] message %s \n", channel.localAddress(), channel.remoteAddress(), message);
            }

            @Override
            public void caught(Channel channel, Throwable exception) {
                System.out.println(NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress()));
            }
        };

        NettyServer nettyServer = new NettyServer(9090, serverHandler);
//        List<NettyClient> clients = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            clients.add(new NettyClient("localhost", 9090, clientHandler));
//        }
        NettyClient nettyClient = new NettyClient("localhost", 9090, clientHandler);

        Thread.sleep(1000);
        System.out.println("----------------------");
        //发送当前时间
        for (int i = 0; i < 1000; i++) {
            nettyClient.send(new Date().getTime() + "", false);
        }

        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }
}