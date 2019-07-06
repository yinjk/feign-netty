package com.intellif.remoting.discover;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

/**
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