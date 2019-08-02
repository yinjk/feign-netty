package com.intellif.config;

import com.intellif.feign.NettyClient;
import com.netflix.loadbalancer.ILoadBalancer;
import feign.Client;
import feign.Feign;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置，参照{@link FeignAutoConfiguration}和{@link org.springframework.cloud.netflix.feign.ribbon.OkHttpFeignLoadBalancedConfiguration}代码，
 * 自动配置feign-netty客户端，并且基于负载均很和非负载均衡的两种方式配置，特别需要注意的是，负载均衡默认使用的是原生的负载均很策略，从而做到了对代码完全无感知
 *
 * @author yinjk
 */
@Configuration
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
public class NettyFeignAutoConfiguration {

    @Configuration
    @ConditionalOnMissingClass("com.netflix.loadbalancer.ILoadBalancer")
    @ConditionalOnProperty(value = "feign.netty.enabled", matchIfMissing = true)
    protected static class NettyFeignConfiguration {

        @Bean
        @ConditionalOnMissingBean(Client.class)
        public Client feignClient() {
            //这里使用我们创建的netty客户端
            return new NettyClient();
        }
    }

    @Configuration
    @ConditionalOnClass({ILoadBalancer.class, NettyClient.class})
    @ConditionalOnProperty(value = "feign.netty.enabled", matchIfMissing = true)
    protected class NettyFeignLoadBalancedConfiguration {

        @Bean
        @ConditionalOnMissingBean(Client.class)
        public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
                                  SpringClientFactory clientFactory) {
            //使用负载均衡的方式
            return new LoadBalancerFeignClient(new NettyClient(), cachingFactory, clientFactory);
        }

    }
}
