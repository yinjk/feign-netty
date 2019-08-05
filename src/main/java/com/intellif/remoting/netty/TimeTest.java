package com.intellif.remoting.netty;

import com.alibaba.fastjson.JSON;
import com.intellif.feign.transfer.TransferRequest;
import com.intellif.feign.transfer.TransferResponse;
import com.intellif.remoting.RemotingException;
import io.netty.channel.Channel;
import org.apache.coyote.http11.Http11NioProtocol;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author inori
 * @create 2019-07-18 10:41
 */
public class TimeTest {
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            100,
            100,
            1,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(1000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public static void main(String[] args) throws Throwable {
        NettyChannelHandler serverHandler = new AbstractNettyChannelHandler() {

            @Override
            public void sent(Channel channel, Object message) throws RemotingException {
                //doing nothing...
            }

            @Override
            public void received(Channel channel, Object message) throws RemotingException {
                Http11NioProtocol nioProtocol = new Http11NioProtocol();
                executor.execute(()->{
                    TransferRequest request = JSON.parseObject((String) message, TransferRequest.class);
                    System.out.printf("server received request: %s => %d \n", request.getUuid(), new Date().getTime());
                    String uuid = request.getUuid();
                    try {
                        Thread.sleep(200); //模拟程序耗时
                    } catch (InterruptedException e) {
                        //doing nothing...
                    }
                    TransferResponse response = new TransferResponse();
                    response.setUuid(uuid);
                    channel.writeAndFlush(JSON.toJSONString(response));
                });

            }
        };
        NettyChannelHandler clientHandler = new AbstractNettyChannelHandler() {

            private Map<String, TransferResponse> nettyResult = new ConcurrentHashMap<>(); // uuid : result [存放同步消息的返回]

            private Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>(); // uuid : latch

            @Override
            public void sent(Channel channel, Object message) throws RemotingException {
                //doing nothing...
            }

            @Override
            public void received(Channel channel, Object message) throws RemotingException {
                if ("h".equals(message)) {
                    //doing nothing...
                    return;
                }
                TransferResponse response = JSON.parseObject((String) message, TransferResponse.class);
                nettyResult.put(response.getUuid(), response);
                CountDownLatch latch = latchMap.remove(response.getUuid());
                latch.countDown();
            }

            @Override
            public void setLatch(String uuid, CountDownLatch latch) {
                latchMap.put(uuid, latch);
            }

            @Override
            public Object getResult(String uuid) throws RemotingException {
                return nettyResult.get(uuid);
            }
        };

        ExecutorService executorService = Executors.newCachedThreadPool();

        NettyServer nettyServer = new NettyServer(9091, serverHandler);
        List<NettyClient> clients = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            clients.add(new NettyClient("localhost", 9091, clientHandler));
        }
        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                TransferRequest request = new TransferRequest();
                request.setUuid(UUID.randomUUID().toString());
                TransferResponse result = null;
                try {
                    result = (TransferResponse) clients.get(0).sendSync(request, 100, TimeUnit.SECONDS);
                } catch (RemotingException e) {
                    //doing nothing
                }
            });
        }
        executorService.shutdown();
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }
}