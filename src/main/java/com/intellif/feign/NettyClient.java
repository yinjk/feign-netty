package com.intellif.feign;

import com.intellif.listener.ServiceRunListener;
import com.intellif.remoting.RemotingException;
import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NettyClient implements Client {

    /**
     * logger
     */
    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);

    private int timeout = 10;

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        String url = request.url();
        URI uri = URI.create(url);
        String remoteService = uri.getHost() + ":" + uri.getPort();
        com.intellif.remoting.netty.NettyClient nettyClient = ServiceRunListener.nettyClientMap.get(remoteService);
        ResponseMessage result = null;
        try {
            //TODO: create transfer data(can using original request)
            result = (ResponseMessage) nettyClient.sendSync(new RequestMessage(UUID.randomUUID().toString(), request), timeout, TimeUnit.SECONDS);
        } catch (RemotingException e) {
            //TODO: hand this error
            log.error(e.getMessage());
        }
        if (null != result) {
            //TODO: hand this result
            Response response = result.getData().toFeignResponse();
            return response;
        }
        Map<String, Collection<String>> headers = new LinkedHashMap<>();
        List<String> contentType = Arrays.asList("application/json", "charset=UTF-8");
        headers.put("Content-Type", contentType);
        String body = "{ \"name\": \"sweet\", \"sex\": \"男\", \"age\": 18 }";
        return Response.builder()
                .status(200)
                .reason("the reason is no reason")
                .headers(headers)
                .body(body, Charset.forName("UTF-8"))
                .build();
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
