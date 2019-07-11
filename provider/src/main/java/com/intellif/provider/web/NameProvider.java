package com.intellif.provider.web;

import com.intellif.provider.bean.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author inori
 * @create 2019-07-02 13:44
 */
@RestController
public class NameProvider {

    @GetMapping("/user")
    public User getUser() {
        return new User("sweet", "女", 18);
    }

    @PostMapping("/hello")
    public String hello(@RequestBody Map<String, String> param) {
        System.out.println(param);
        return "hello !";
    }

    @RequestMapping(value = "/form/data",method = RequestMethod.POST)
    public String formData(String name) {
        System.out.println(name);
        return "nihao";
    }

}