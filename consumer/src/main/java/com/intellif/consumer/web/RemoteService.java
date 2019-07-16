package com.intellif.consumer.web;

import feign.Response;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(value = "/post/json")
    String postJson(@RequestParam("name") String name, @RequestBody User user);

    @RequestMapping(value = "/downloadFile", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    Response downloadFile();

}