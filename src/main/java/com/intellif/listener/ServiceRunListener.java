package com.intellif.listener;

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
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.web.servlet.DispatcherServlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class ServiceRunListener implements SpringApplicationRunListener {

    private final SpringApplication application;

    private final String[] args;

    public static final Map<String, NettyClient> nettyClientMap = new ConcurrentHashMap<>(); // ip:port, NettyClient

    public static volatile NettyServer nettyServer;

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(ServiceRunListener.class);


    public ServiceRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    private AtomicBoolean inited = new AtomicBoolean(false);

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

    private NettyServer startNettyServer(ApplicationContext context) throws Throwable {
        //TODO: 1.处理这个异常， 2. 考虑将端口可配
        initServer(context);
        Environment environment = context.getEnvironment();
        String nettyPort = environment.getProperty("feign.netty.port");
        if (StringUtils.isBlank(nettyPort)) {//没有配置netty port
            // 根据统一算法自动生成nettyPort
            int nettyPortInt = autoNettyPort(Integer.parseInt(environment.getProperty("server.port")));
            nettyPort = String.valueOf(nettyPortInt);
        }
        return new NettyServer(Integer.parseInt(nettyPort), new NettyServerChannelHandler(context.getBean(DispatcherServlet.class)));
    }

    private void doConnect(List<ServiceInstance> serviceInstances) throws Throwable {
        for (ServiceInstance serviceInstance : serviceInstances) {
            String remoteService = serviceInstance.getHost() + ":" + serviceInstance.getPort();
            String nettyPort = serviceInstance.getMetadata().get("netty-port");
            if (StringUtils.isBlank(nettyPort)) { //如果没有将netty-port注册到注册中心，直接取server.port,将其加10000，如果大于30000，则加123
                nettyPort = String.valueOf(autoNettyPort(serviceInstance.getPort()));
            }
            //TODO 客户端连不上怎么办
            nettyClientMap.putIfAbsent(remoteService, new NettyClient(serviceInstance.getHost(), Integer.parseInt(nettyPort), new NettyClientChannelHandler()));
        }
    }

    private List<ServiceInstance> searchInDiscovery(ApplicationContext context, String serviceName) {
        DiscoveryClient discoveryClient = context.getBean(DiscoveryClient.class);
        return discoveryClient.getInstances(serviceName);
    }

    /**
     * 按照一定算法，自动生成nettyPort
     *
     * @param port
     * @return
     */
    private int autoNettyPort(int port) {
        return port + 10000 > 30000 ? port + 123 : port + 10000;
    }

    private void initServer(ApplicationContext context) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method onRefresh = null;
        DispatcherServlet dispatcherServlet = context.getBean(DispatcherServlet.class);
        onRefresh = DispatcherServlet.class.getDeclaredMethod("onRefresh", ApplicationContext.class);
        onRefresh.setAccessible(true);
        onRefresh.invoke(dispatcherServlet, context);

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
        if (inited.get()) { //如果已经被初始化过了，直接跳过（确保只初始化一次）
            return;
        }
        String nettyPort = environment.getProperty("feign.netty.port");
        if (StringUtils.isBlank(nettyPort)) { //没有配置，直接跳过
            return;
        }
        Map<String, Object> map = new HashMap<>();
        // 尝试获取当前的注册中心
        try {
            Class.forName("org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean");
            map.put("eureka.instance.metadata-map.netty-port", nettyPort);
            MapPropertySource mapPropertySource = new MapPropertySource("netty-port", map);
            environment.getPropertySources().addFirst(mapPropertySource);
        } catch (ClassNotFoundException e) {
            //没有该类，说明注册中心不是eureka，将feign.netty.port去掉
            map.put("feign.netty.port", "");
            environment.getPropertySources().addFirst(new MapPropertySource("netty-port", map));
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
     * @param context
     * @param exception
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