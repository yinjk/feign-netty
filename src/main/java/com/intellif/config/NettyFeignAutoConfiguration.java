package com.intellif.config;

import com.intellif.feign.NettyClient;
import com.netflix.loadbalancer.ILoadBalancer;
import feign.Client;
import feign.Feign;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.feign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yinjk
 */
@Configuration
@ConditionalOnClass(Feign.class)
@AutoConfigureAfter(FeignAutoConfiguration.class)
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
            return new LoadBalancerFeignClient(new NettyClient(), cachingFactory, clientFactory);
        }

    }
}
