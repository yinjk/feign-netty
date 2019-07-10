package com.intellif.feign;

import feign.Request;
import feign.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private int status;
    private String reason;
    private Map<String, Collection<String>> headers;
    private byte[] body;
    private TransferRequest request;

    //将我们的传输Response转换成feign的response
    public Response toFeignResponse() {
        Request targetReq = null;
        if (this.request != null) {
            targetReq = this.request.toFeignRequest();
        }
        return Response.builder()
                .request(targetReq)
                .headers(this.headers)
                .status(this.status)
                .reason(this.reason)
                .body(this.body)
                .build();
    }
}