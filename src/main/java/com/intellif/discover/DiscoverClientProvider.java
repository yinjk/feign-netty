package com.intellif.discover;

import com.intellif.common.Constants;
import com.intellif.utils.ApplicationContextHolder;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * @author inori
 * @create 2019-07-24 17:45
 */
public interface DiscoverClientProvider {

    static DiscoveryClient getDiscoverClient() {
        return ApplicationContextHolder.getApplicationContext().getBean(Constants.DISCOVERY_CLIENT, DiscoveryClient.class);
    }
}