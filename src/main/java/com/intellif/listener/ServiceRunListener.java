package com.intellif.listener;

import com.intellif.remoting.transport.netty.NettyClient;
import com.intellif.remoting.transport.netty.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监听
 *
 * @author inori
 * @create 2019-07-06 19:08
 */
public class ServiceRunListener implements SpringApplicationRunListener {

    private final SpringApplication application;

    private final String[] args;

    public static Map<String, NettyClient> nettyClientMap = new ConcurrentHashMap<>(); // ip:port, NettyClient

    public static volatile NettyServer nettyServer;

    public ServiceRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    private AtomicBoolean inited = new AtomicBoolean(false);

    private void connectRemote(final ApplicationContext context) throws Throwable {
        Map<String, Object> beansWithAnnotation = context.getBeansWithAnnotation(FeignClient.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            String k = entry.getKey();
            Object obj = entry.getValue();
            FeignClient feignClient = obj.getClass().getAnnotation(FeignClient.class);
            String providerName = feignClient.name(); //获取服务提供方的服务名
            List<ServiceInstance> serviceInstances = searchInDiscovery(context, providerName); //获取对应服务的所有实例
            nettyServer = startNettyServer(); //启动netty服务端
            doConnect(serviceInstances); //启动netty客户端
        }
    }

    private NettyServer startNettyServer() throws Throwable {
        //TODO: 1.处理这个异常， 2. 考虑将端口可配
        return new NettyServer(20800, null);
    }

    private void doConnect(List<ServiceInstance> serviceInstances) throws Throwable {
        for (ServiceInstance serviceInstance : serviceInstances) {
            String remoteService = serviceInstance.getHost() + ":" + serviceInstance.getPort();
            //TODO 客户端连不上怎么办
            nettyClientMap.putIfAbsent(remoteService, new NettyClient(serviceInstance.getHost(), 20800, null));
        }
    }

    private List<ServiceInstance> searchInDiscovery(ApplicationContext context, String serviceName) {
        DiscoveryClient discoveryClient = context.getBean(DiscoveryClient.class);
        return discoveryClient.getInstances(serviceName);
    }


    @Override
    public void starting() {
        //doing nothing...
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        //doing nothing...
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        //doing nothing...
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        //doing nothing...
    }

    /**
     * 在springboot启动完成时去连接当前服务的所有provider
     *
     * @param context
     * @param exception
     */
    @Override
    public void finished(ConfigurableApplicationContext context, Throwable exception) {
        String applicationName = context.getEnvironment().getProperty("spring.application.name");
        if ("bootstrap".equals(applicationName)) {
            return;
        }
        if (inited.get()) { //如果已经被初始化过了，直接跳过（确保只初始化一次）
            return;
        }
        inited.set(true);
        try {
            connectRemote(context);
        } catch (Throwable throwable) {
            //TODO: 处理这个异常
            throwable.printStackTrace();
        }
    }
}