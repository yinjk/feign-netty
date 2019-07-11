package com.intellif.consumer.web;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {

    @Autowired
    private RemoteService remoteService;

    @GetMapping("/test/hello")
    public User hello() {
        return remoteService.user();
    }

    @GetMapping("/remote/hello")
    public String remoteHello() {
        Map<String, String> param = Maps.newHashMap();
        param.put("age", "18");
        param.put("sex", "女");
        param.put("name", "张珊");
        return remoteService.hello(param);
    }

    @RequestMapping(value = "/remote/form/data", method = RequestMethod.POST)
    public String remoteFormData(String name) {
        return remoteService.formData(name);
    }
}
