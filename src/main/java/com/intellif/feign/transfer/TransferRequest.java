package com.intellif.feign.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

/**
 * 用于传输的请求信息，请求中附带了一个uuid，服务端接收到请求处理之后会把该id放在响应中传回来
 *
 * @author inori
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {

    private String uuid;
    private String method;
    private String url;
    private Map<String, Collection<String>> headers;
    private byte[] body;
    private Charset charset;

    public feign.Request toFeignRequest() {
        return feign.Request.create(this.getMethod(), this.getUrl(), this.getHeaders(), this.getBody(), this.getCharset());
    }

    /**
     * 根据feign.Request生成响应的基于传输的Request
     *
     * @param uuid    唯一id，每次请求都会有一个uuid
     * @param request feign的request
     * @return 基于传输的request
     */
    public static TransferRequest create(String uuid, feign.Request request) {
        return new TransferRequest(uuid, request.method(), request.url(), request.headers(), request.body(), request.charset());
    }
}