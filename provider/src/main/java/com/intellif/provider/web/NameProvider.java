package com.intellif.provider.web;

import com.intellif.provider.bean.User;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    @PostMapping(value = "/form/data")
    public String formData(String name) {
        System.out.println(name);
        return "nihao";
    }

    @PostMapping(value = "/post/json")
    public String postJson(@RequestParam("name") String name, @RequestBody User user) throws InterruptedException {
        System.out.println(name);
        System.out.println(user.toString());
        Thread.sleep(300);
        return user.toString();
    }

    @RequestMapping(value = "/downloadFile", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void downloadFile(HttpServletResponse response) {
        String filePath = "D://1.txt";
        File file = new File(filePath);
        InputStream in = null;
        if (file.exists()) {
            try {
                OutputStream out = response.getOutputStream();
                in = new FileInputStream(file);
                byte buffer[] = new byte[1024];
                int length = 0;
                while ((length = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}