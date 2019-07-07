package com.intellif.feign;

import com.alibaba.fastjson.JSON;
import com.intellif.remoting.RemotingException;
import com.intellif.remoting.transport.netty.AbstractNettyChannelHandler;
import com.intellif.remoting.transport.netty.NetUtils;
import com.intellif.remoting.transport.netty.NettyChannelHandler;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * author: yinjk
 */
public class NettyClientChannelHandler extends AbstractNettyChannelHandler {

    private Map<String, Message> nettyResult = new ConcurrentHashMap<>(); // uuid : result [存放同步消息的返回]

    private Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>(); // uuid : latch

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyClientChannelHandler.class);

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        //doing nothing...
    }

    @Override
    public void received(Channel channel, Object o) throws RemotingException {
        if ("h".equals(o)) { //接收到心跳 跳过不处理
            log.info("received heartbeat from server:[" + NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress()) + "]");
            return;
        }
        String mJson = (String) o;
        Message result = JSON.parseObject(mJson, Message.class);
        nettyResult.put(result.getUuid(), result);
        CountDownLatch latch = latchMap.remove(result.getUuid());
        if (latch != null) {
            latch.countDown(); //通知等待方消息已经成功获取
        }

    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        //TODO: hand this exception
        NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress());
    }

    @Override
    public Object getResult(String uuid) throws RemotingException {
        return nettyResult.remove(uuid);
    }

    @Override
    public void setLatch(String uuid, CountDownLatch latch) {
        this.latchMap.put(uuid, latch);
    }
}
