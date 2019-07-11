package com.intellif.consumer.web;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author inori
 * @create 2019-07-02 13:54
 */
@FeignClient(name = "spring-cloud-provider")
public interface RemoteService {

    @RequestMapping(value = "/user")
    User user();

    @PostMapping(value = "/hello")
    String hello(@RequestBody Map<String, String> param);

    @PostMapping("/form/data")
    String formData(@RequestParam("name") String name);


}