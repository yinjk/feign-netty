package com.intellif;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@Component
public class FeignNettyApplication {

	@Autowired
	public DiscoveryClient discoveryClient;

	public static void main(String[] args) {
		ConfigurableApplicationContext application = SpringApplication.run(FeignNettyApplication.class, args);
		DiscoveryClient discovery = application.getBean(DiscoveryClient.class);
		System.out.println(discovery.getServices());
	}

}
