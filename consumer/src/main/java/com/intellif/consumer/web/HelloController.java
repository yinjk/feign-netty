package com.intellif.consumer.web;

import com.google.common.collect.Maps;
import com.intellif.utils.ApplicationContextHolder;
import com.netflix.loadbalancer.ServerList;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import feign.Response;
import io.prometheus.client.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class HelloController {

    private static final Counter requestTotal = Counter.build()
            .name("my_sample_counter")
            .labelNames("status")
            .help("A simple Counter to illustrate custom Counters in Spring Boot and Prometheus").register();

    @Autowired
    private RemoteService remoteService;


    @GetMapping("/batch/test")
    public Map<String, Object> batchTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2000);
        List<Long> execTimes = Collections.synchronizedList(new ArrayList<>());
        int testCount = 1000 * 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(testCount);
        AtomicInteger errCount = new AtomicInteger();
        for (int i = 0; i < testCount; i++) {
            executor.execute(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    //doing nothing...
                }
                try {
                    Date startTime = new Date();
                    postJson();
                    execTimes.add(new Date().getTime() - startTime.getTime());
                } catch (Throwable t) {
                    System.out.println(t);
                    errCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        long sumTime = 0;
        for (Long execTime : execTimes) {
            sumTime += execTime;
            System.out.println("execute time: " + execTime);
        }
        System.out.println("avgTime : " + sumTime / execTimes.size());
        System.out.println("errCount: " + errCount.get());
        System.out.println("totalCount: " + execTimes.size());
        Map<String, Object> map = new HashMap<>();
        map.put("time", execTimes);
        map.put("err", errCount);
        map.put("avgTime", sumTime / execTimes.size());
        return map;
    }


    @GetMapping("/test/simple")
    public String simple() throws InterruptedException {
        Thread.sleep(new Random().nextInt(1000 * 10));
        return "hello world";
    }

    @GetMapping("/test/hello")
    public User hello() {
        User user = remoteService.user();
        ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();
        ServerList serverList = applicationContext.getBean(ServerList.class);
        List<DiscoveryEnabledServer> updatedListOfServers = serverList.getUpdatedListOfServers();
        for (DiscoveryEnabledServer updatedListOfServer : updatedListOfServers) {
            System.out.printf("id[%s] host:port[%s] \n", updatedListOfServer.getId(), updatedListOfServer.getHostPort());
        }
        System.out.println("--------- initial ---------");
        List<DiscoveryEnabledServer> initialListOfServers = serverList.getInitialListOfServers();
        for (DiscoveryEnabledServer initialListOfServer : initialListOfServers) {
            System.out.printf("id[%s] host:port[%s] \n", initialListOfServer.getId(), initialListOfServer.getHostPort());
        }


        return user;
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
        Date start = new Date();
        String name = "xiao熊";
        User user = new User("王华", "男", 18);
        String s = remoteService.postJson(name, user);
        System.out.printf("the controller get result: ==> %d\n", new Date().getTime() - start.getTime());
        return s;
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
