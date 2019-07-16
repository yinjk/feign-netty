package com.intellif.feign;

import com.intellif.feign.transfer.RequestMessage;
import com.intellif.feign.transfer.ResponseMessage;
import com.intellif.listener.ServiceRunListener;
import com.intellif.remoting.RemotingException;
import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NettyClient implements Client {

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);

    private int timeout = 10;

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        String url = request.url();
        URI uri = URI.create(url);
        String remoteService = uri.getHost() + ":" + uri.getPort();
        com.intellif.remoting.netty.NettyClient nettyClient = ServiceRunListener.nettyClientMap.get(remoteService);
        if (nettyClient == null) { //没有拿到客户端，应该去重新获取
            try {
                nettyClient = getNettyClient(uri);
                ServiceRunListener.nettyClientMap.putIfAbsent(remoteService, nettyClient);
            } catch (Throwable throwable) {// 链接失败，考虑切换回http的方式
                //TODO: 切换回http方式
                log.warn("connect netty server failed, using original http client");
                throw new IOException(throwable.getMessage());
            }
        }
        ResponseMessage result = null;
        try {
            result = (ResponseMessage) nettyClient.sendSync(new RequestMessage(UUID.randomUUID().toString(), request), timeout, TimeUnit.SECONDS);
        } catch (RemotingException e) {
            log.error(e.getMessage());
            throw new IOException(e.getMessage());
        }
        return result.getData().toFeignResponse();
    }

    public com.intellif.remoting.netty.NettyClient getNettyClient(URI uri) throws Throwable {
        return new com.intellif.remoting.netty.NettyClient(uri.getHost(), uri.getPort(), new NettyClientChannelHandler());
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
