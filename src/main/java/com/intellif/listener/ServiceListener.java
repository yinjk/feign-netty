package com.intellif.listener;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

/**
 * 监听
 *
 * @author inori
 * @create 2019-07-06 19:08
 */
public class ServiceListener implements ApplicationListener<ApplicationContextEvent> {

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        ApplicationContext context = event.getApplicationContext();
        String applicationName = context.getEnvironment().getProperty("spring.application.name");
        if ("bootstrap".equals(applicationName)) {
            return;
        }
        connectRemote(context);
    }

    private void connectRemote(ApplicationContext context) {
        DiscoveryClient discoveryClient = context.getBean(DiscoveryClient.class);
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            for (ServiceInstance instance : instances) {
                System.out.println("---------------------");
                String serviceHost = instance.getHost() + ":" + instance.getPort();
                System.out.println(serviceHost);
                System.out.println(instance);
                System.out.println(instance.getUri());
                System.out.println("---------------------");
            }
        }
    }

}