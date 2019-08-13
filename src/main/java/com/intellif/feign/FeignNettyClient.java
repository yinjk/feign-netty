package com.intellif.feign;

import com.intellif.common.Constants;
import com.intellif.discover.DiscoverClientProvider;
import com.intellif.feign.transfer.TransferRequest;
import com.intellif.feign.transfer.TransferResponse;
import com.intellif.listener.NettyInitRunListener;
import com.intellif.remoting.RemotingException;
import com.intellif.remoting.netty.NettyClient;
import feign.Client;
import feign.Request;
import feign.Response;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Feign Client{@link Client}的实现类，通过netty长连接的方式进行服务间通信
 * TODO 这部分代码写的很乱，需要重新规划一下
 *
 * @author inori
 * @create 2019-07-08 14:20
 */
public class FeignNettyClient implements Client {

    //默认的http执行器
    private Client httpClient = new Client.Default(null, null);

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(FeignNettyClient.class);

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Date start = new Date();
        String url = request.url();
        URI uri = URI.create(url);
        String remoteService = uri.getHost() + ":" + uri.getPort();
        NettyClient nettyClient = NettyInitRunListener.nettyClientMap.get(remoteService);
        if (nettyClient == null) { //没有拿到客户端，应该去重新获取
            try {
                //TODO 获取netty长连接应该考虑一些策略，以提升并发下的性能，如：第一次获取失败2s后再重试，第二次获取失败4秒后尝试，第三次获取失败8秒后重试（参照ribbon的策略）
                nettyClient = getNettyClient(uri);
                NettyInitRunListener.nettyClientMap.putIfAbsent(remoteService, nettyClient);
            } catch (Throwable throwable) {// 链接失败，考虑切换回http的方式
                log.warn("Connect netty server failed, using original http client");
                return httpClient.execute(request, options);
            }
        }
        TransferResponse result = null;
        try {
            result = (TransferResponse) nettyClient.sendSync(TransferRequest.create(UUID.randomUUID().toString(), request), options.readTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (RemotingException e) {
            log.error(e.getMessage());
            throw new IOException(e.getMessage());
        }
        Response response = result.toFeignResponse();
//        System.out.println("return time: " + new Date().getTime());
        System.out.println("=================+> get result: " + (new Date().getTime() - start.getTime()));
        return response;
    }

    /**
     * 根据请求的uri，主动尝试获取netty长连接，通过discovery客户端去轮询当前注册中心中的所有服务，找到匹配的那个,
     * （因为会遍历一遍当前所有服务，当服务数量太多时性能会很差，可以考虑更高性能的方式，比如使用ribbon的ServerList或其他思路）
     *
     * @param uri 请求的uri
     * @return 建立好的netty长连接
     * @throws Throwable 获取长连接过程发生的任何错误都会抛出
     */
    public synchronized NettyClient getNettyClient(URI uri) throws Throwable {
        String target = uri.getHost() + ":" + uri.getPort();
        DiscoveryClient discoverClient = DiscoverClientProvider.getDiscoverClient();
        List<String> services = discoverClient.getServices();
        for (String service : services) {
            List<ServiceInstance> instances = discoverClient.getInstances(service);
            for (ServiceInstance instance : instances) {
                if (target.equals(instance.getHost() + ":" + instance.getPort())) {
                    Map<String, String> metadata = instance.getMetadata();
                    String nettyPort = metadata.get(Constants.DISCOVERY_METADATA_FEIGN_NETTY_PORT_KEY);
                    int nettyIntPort;
                    if (StringUtils.isNotBlank(nettyPort) && StringUtils.isNumeric(nettyPort)) {
                        nettyIntPort = Integer.parseInt(nettyPort);
                    } else { // 如果metadata中没有netty-port的信息
                        nettyIntPort = NettyInitRunListener.autoNettyPort(instance.getPort());
                    }
                    log.info("Connect to netty server " + instance.getHost() + ":" + nettyIntPort);
                    return new com.intellif.remoting.netty.NettyClient(instance.getHost(), nettyIntPort, new NettyClientChannelHandler());
                }
            }
        }
        throw new Exception("The server is not available to accept netty connect");
    }
}
