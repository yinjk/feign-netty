package com.intellif.listener;

import com.intellif.common.Constants;
import com.intellif.feign.NettyClientChannelHandler;
import com.intellif.feign.NettyServerChannelHandler;
import com.intellif.remoting.netty.NettyClient;
import com.intellif.remoting.netty.NettyServer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监听服务启动，在服务启动时启动netty服务器，并且去连接远程客户端
 *
 * @author inori
 * @create 2019-07-06 19:08
 */
public class NettyInitRunListener implements SpringApplicationRunListener {

    private final SpringApplication application;

    private final String[] args;

    private ConfigurableEnvironment environment;

    public static final Map<String, NettyClient> nettyClientMap = new ConcurrentHashMap<>(); // ip:port, NettyClient

    public static volatile NettyServer nettyServer;

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyInitRunListener.class);


    public NettyInitRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    /**
     * 确保该类只被初始化一次
     */
    private static AtomicBoolean initialized = new AtomicBoolean(false);

    private void connectRemote(final ApplicationContext context) throws Throwable {
        nettyServer = startNettyServer(context); //启动netty服务端
        Map<String, Object> beansWithAnnotation = context.getBeansWithAnnotation(FeignClient.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Object obj = entry.getValue();
            String providerName = getProviderName(obj); //获取服务提供方的服务名
            if (StringUtils.isEmpty(providerName)) { //没有获取到远程主机名？跳过
                continue;
            }
            List<ServiceInstance> serviceInstances = searchInDiscovery(context, providerName); //获取对应服务的所有实例
            doConnect(serviceInstances); //启动netty客户端
        }
    }

    /**
     * 通过获取被代理的feign客户端的接口上的FeignClient注解，从而获取到注解中的远程服务名
     *
     * @param target 被代理的feign客户端
     * @return 远程服务名
     */
    private String getProviderName(Object target) {
        Class<?> targetClass = target.getClass();
        for (Class<?> targetInf : targetClass.getInterfaces()) {
            FeignClient feignClient = targetInf.getAnnotation(FeignClient.class);
            if (feignClient != null) {
                return feignClient.name(); //获取服务提供方的服务名
            }
        }
        return "";
    }

    /**
     * 启动netty服务器
     *
     * @param context spring上下文
     * @return netty服务器
     * @throws Throwable
     */
    private NettyServer startNettyServer(ApplicationContext context) throws Throwable {
        Environment environment = context.getEnvironment();
        DiscoveryClient discoveryClient = context.getBean(Constants.DISCOVERY_CLIENT, DiscoveryClient.class);
        Registration registration = context.getBean(Registration.class);
        ServiceInstance localServiceInstance = localServiceInstance(discoveryClient, registration); //获取当前服务信息
        String nettyPort = localServiceInstance.getMetadata().get(Constants.DISCOVERY_METADATA_FEIGN_NETTY_PORT_KEY);
        if (nettyPort == null || StringUtils.isBlank(nettyPort)) {//没有配置netty port
            // 根据统一算法自动生成nettyPort
            int nettyPortInt = autoNettyPort(Integer.parseInt(environment.getProperty("server.port")));
            nettyPort = String.valueOf(nettyPortInt);
        }
        log.error("-------------- netty server port ----------->" + nettyPort);
        return new NettyServer(Integer.parseInt(nettyPort), new NettyServerChannelHandler(context.getBean(DispatcherServlet.class)));
    }

    /**
     * 启动netty客户端，与远程服务建立连接
     *
     * @param serviceInstances 可能会调用的所有服务端实例
     * @throws Throwable 客户端连不上抛出异常
     */
    private void doConnect(List<ServiceInstance> serviceInstances) throws Throwable {
        for (ServiceInstance serviceInstance : serviceInstances) {
            String remoteService = serviceInstance.getHost() + ":" + serviceInstance.getPort();
            String nettyPort = serviceInstance.getMetadata().get(Constants.DISCOVERY_METADATA_FEIGN_NETTY_PORT_KEY);
            if (StringUtils.isBlank(nettyPort)) { //如果没有将netty-port注册到注册中心，直接取server.port,将其加10000，如果大于30000，则加123
                nettyPort = String.valueOf(autoNettyPort(serviceInstance.getPort()));
            }
            //TODO 客户端连不上怎么办
            nettyClientMap.putIfAbsent(remoteService, new NettyClient(serviceInstance.getHost(), Integer.parseInt(nettyPort), new NettyClientChannelHandler()));
        }
    }

    /**
     * 从注册中心中寻找指定serverName的所有实例
     *
     * @param context     spring上下文
     * @param serviceName 远程服务名
     * @return 所有的远程服务实例
     */
    private List<ServiceInstance> searchInDiscovery(ApplicationContext context, String serviceName) {
        DiscoveryClient discoveryClient = context.getBean(DiscoveryClient.class);
        return discoveryClient.getInstances(serviceName);
    }

    /**
     * 获取本地服务实例
     *
     * @param discoveryClient 注册中心客户端
     * @param registration    Registration
     * @return 本地服务实例
     */
    public ServiceInstance localServiceInstance(DiscoveryClient discoveryClient, Registration registration) {
        List<ServiceInstance> list = discoveryClient.getInstances(registration.getServiceId());
        if (!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 按照一定算法，自动生成nettyPort
     *
     * @param port http服务器的端口
     * @return 根据http服务器端口自动生成的netty服务器端口
     */
    public static int autoNettyPort(int port) {
        return port + 10000 > 30000 ? port + 123 : port + 10000;
    }


    @Override
    public void starting() {
        //doing nothing...
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        String applicationName = environment.getProperty("spring.application.name");
        if ("bootstrap".equals(applicationName)) {
            return;
        }
        if (initialized.get()) { //如果已经被初始化过了，直接跳过（确保只初始化一次）
            return;
        }
        this.environment = environment;
        if (Boolean.FALSE.toString().equals(environment.getProperty("feign.netty.enabled"))) { //关闭feign-netty
            return;
        }
        String nettyPort = environment.getProperty(Constants.FEIGN_NETTY_PORT_KEY);
        if (StringUtils.isBlank(nettyPort)) { //没有配置，直接跳过
            return;
        }
        Map<String, Object> map = new HashMap<>();
        // 尝试获取当前的注册中心
        try {
            Class.forName("org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean");
            map.put("eureka.instance.metadata-map." + Constants.DISCOVERY_METADATA_FEIGN_NETTY_PORT_KEY, nettyPort);
            MapPropertySource mapPropertySource = new MapPropertySource(Constants.DISCOVERY_METADATA_FEIGN_NETTY_PORT_KEY, map);
            environment.getPropertySources().addFirst(mapPropertySource);
        } catch (ClassNotFoundException e) {
            //没有该类，说明注册中心不是eureka，将feign.netty.port去掉
            map.put(Constants.FEIGN_NETTY_PORT_KEY, "");
            environment.getPropertySources().addFirst(new MapPropertySource(Constants.DISCOVERY_METADATA_FEIGN_NETTY_PORT_KEY, map));
        }

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
     * @param context spring上下文
     * @param exception springboot启动中的异常
     */
    @Override
    public void finished(ConfigurableApplicationContext context, Throwable exception) {
        String applicationName = context.getEnvironment().getProperty("spring.application.name");
        if ("bootstrap".equals(applicationName)) {
            return;
        }
        if (exception != null) { //SpringBoot启动失败了，这里什么都不做
            return;
        }
        if (!initialized.compareAndSet(false, true)) {//如果已经被初始化过了，直接跳过（确保只初始化一次）
            return;
        }
        if (Boolean.FALSE.toString().equals(environment.getProperty("feign.netty.enabled"))) { //关闭feign-netty
            return;
        }
        try {
            connectRemote(context);
        } catch (Throwable throwable) {
            log.error("Start netty server failed, an error occurred while starting the server :" + throwable.getMessage());
        }
    }
}