package com.intellif.feign.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    private String method;
    private String url;
    private Map<String, Collection<String>> headers;
    private byte[] body;
    private Charset charset;

    public feign.Request toFeignRequest() {
        return feign.Request.create(this.getMethod(), this.getUrl(), this.getHeaders(), this.getBody(), this.getCharset());
    }
}