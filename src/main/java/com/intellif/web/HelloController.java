package com.intellif.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private RemoteService remoteService;

    @GetMapping("/test/hello")
    public User hello() {
        return remoteService.user();
    }
}
