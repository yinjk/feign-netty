package com.intellif.consumer.web;

import com.google.common.collect.Maps;
import feign.Response;
import io.prometheus.client.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

@RestController
public class HelloController {

    private static final Counter requestTotal = Counter.build()
            .name("my_sample_counter")
            .labelNames("status")
            .help("A simple Counter to illustrate custom Counters in Spring Boot and Prometheus").register();

    @Autowired
    private RemoteService remoteService;

    @GetMapping("/test/simple")
    public String simple() throws InterruptedException {
        Thread.sleep(new Random().nextInt(1000 * 10));
        return "hello world";
    }

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

    @GetMapping(value = "/remote/form/data")
    public String remoteFormData(String name) {
        return remoteService.formData("张三");
    }

    @GetMapping(value = "/remote/post/json")
    public String postJson() {
        String name = "xiao熊";
        User user = new User("王华", "男", 18);
        return remoteService.postJson(name, user);
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downFile() {
        ResponseEntity<byte[]> result = null;
        InputStream inputStream = null;
        try {
            // feign文件下载
            Response response = remoteService.downloadFile();
            Response.Body body = response.body();
            inputStream = body.asInputStream();
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            HttpHeaders heads = new HttpHeaders();
            heads.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=123.txt");
            heads.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            result = new ResponseEntity<byte[]>(b, heads, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


}
