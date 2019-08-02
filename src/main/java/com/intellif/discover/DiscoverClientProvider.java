package com.intellif.discover;

import com.intellif.utils.ApplicationContextHolder;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 *
 * @author inori
 * @create 2019-07-24 17:45
 */
public abstract class DiscoverClientProvider {

    public static DiscoveryClient getDiscoverClient() {
        return ApplicationContextHolder.getApplicationContext().getBean(DiscoveryClient.class);
    }

    private DiscoverClientProvider() {
    }
}