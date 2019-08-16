package com.intellif.config;

import com.intellif.mockhttp.DispatcherServletProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletRegistration;

/**
 * 代理DispatcherServlet，将其processRequest方法公开。
 *
 * @author inori
 * @create 2019-08-15 14:15
 */

@Configuration
@ConditionalOnProperty(value = "feign.netty.enabled", matchIfMissing = true)
@ConditionalOnClass(ServletRegistration.class)
@EnableConfigurationProperties(WebMvcProperties.class)
public class DispatcherServletAutoConfig {


    private final WebMvcProperties webMvcProperties;

    public DispatcherServletAutoConfig(WebMvcProperties webMvcProperties) {
        this.webMvcProperties = webMvcProperties;
    }

    @Primary
    @Bean(name = org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServletProxy();
        dispatcherServlet.setDispatchOptionsRequest(
                this.webMvcProperties.isDispatchOptionsRequest());
        dispatcherServlet.setDispatchTraceRequest(
                this.webMvcProperties.isDispatchTraceRequest());
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(
                this.webMvcProperties.isThrowExceptionIfNoHandlerFound());
        return dispatcherServlet;
    }

    /**
     * 设置loadOnStartup为1，确保在程序启动时就调用{@link DispatcherServlet#init(ServletConfig)}方法，初始化DispatcherServlet容器
     *
     * @return BeanFactoryPostProcessor
     */
    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return new BeanFactoryPostProcessor() {

            @Override
            public void postProcessBeanFactory(
                    ConfigurableListableBeanFactory beanFactory) throws BeansException {
                BeanDefinition bean = beanFactory.getBeanDefinition(
                        DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);

                bean.getPropertyValues().add("loadOnStartup", 1);
            }
        };
    }
}