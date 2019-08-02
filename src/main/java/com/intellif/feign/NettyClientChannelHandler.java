package com.intellif.feign;

import com.alibaba.fastjson.JSON;
import com.intellif.feign.transfer.ResponseMessage;
import com.intellif.remoting.RemotingException;
import com.intellif.remoting.netty.AbstractNettyChannelHandler;
import com.intellif.remoting.netty.NetUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author yinjk
 */
public class NettyClientChannelHandler extends AbstractNettyChannelHandler {

    private Map<String, ResponseMessage> nettyResult = new ConcurrentHashMap<>(); // uuid : result [存放同步消息的返回]

    private Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>(); // uuid : latch

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyClientChannelHandler.class);

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        //doing nothing...
        System.out.println("sent message: "+ new Date().getTime());
    }

    @Override
    public void received(Channel channel, Object o) throws RemotingException {
        if ("h".equals(o)) { //接收到心跳 跳过不处理
            log.info("received heartbeat from server:[" + NetUtils.toAddressString((InetSocketAddress) channel.remoteAddress()) + "]");
            return;
        }
        String mJson = (String) o;
        ResponseMessage result = JSON.parseObject(mJson, ResponseMessage.class);
        System.out.printf("received response message %s: => %d \n", result.getUuid(), new Date().getTime());
        //将服务端返回的消息先暂时放在nettyResult缓存中，然后通知等待放去获取
        nettyResult.put(result.getUuid(), result);
        CountDownLatch latch = latchMap.remove(result.getUuid());
        if (latch != null) {
            latch.countDown(); //通知等待方消息已经成功获取
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        //TODO: hand this exception
        log.error("");
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
