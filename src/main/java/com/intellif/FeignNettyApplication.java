package com.intellif;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@Component
public class FeignNettyApplication {

	@Autowired
	public DiscoveryClient discoveryClient;

	public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		ConfigurableApplicationContext application = SpringApplication.run(FeignNettyApplication.class, args);
//		DispatcherServlet obj = application.getBean(DispatcherServlet.class);
//		Method doService = DispatcherServlet.class.getDeclaredMethod("doDispatch", HttpServletRequest.class, HttpServletResponse.class);
//		Method onRefresh = DispatcherServlet.class.getDeclaredMethod("onRefresh", ApplicationContext.class);
//		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//		MockHttpServletResponse response = new MockHttpServletResponse();
//		mockHttpServletRequest.setMethod("GET");
//		mockHttpServletRequest.setRequestURI("/test/hello");
//		doService.setAccessible(true);
//		onRefresh.setAccessible(true);
//		onRefresh.invoke(obj, application);
//		doService.invoke(obj, mockHttpServletRequest, response);
//		System.out.println(response);
	}

}
