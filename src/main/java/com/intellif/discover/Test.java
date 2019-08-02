package com.intellif.discover;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

/**
 * 动态感知服务的上线与下线
 *
 * @author inori
 * @create 2019-07-05 15:36
 */
@Component
public class Test {

    @Autowired
    private DiscoveryClient discoveryClient;

    public void test() {


    }
}