package com.intellif.web;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author inori
 * @create 2019-07-02 13:54
 */
@FeignClient(name = "spring-cloud-provider")
public interface RemoteService {

    @RequestMapping(value = "/user")
    User user();

}