package com.intellif.feign.transfer;



/**
 * @author inori
 * @create 2019-07-08 16:32
 */
public class RequestMessage extends Message<TransferRequest> {

    public RequestMessage() {
    }

    public RequestMessage(String uuid, feign.Request data) {
        super(uuid, toTransferRequest(data));
    }

    public feign.Request getFeignRequest() {
        TransferRequest req = this.getData();
        return feign.Request.create(req.getMethod(), req.getUrl(), req.getHeaders(), req.getBody(), req.getCharset());
    }

    public static TransferRequest toTransferRequest(feign.Request request) {
        return new TransferRequest(request.method(), request.url(), request.headers(), request.body(), request.charset());
    }

}
