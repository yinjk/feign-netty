package com.intellif.feign.transfer;


/**
 * @author inori
 * @create 2019-07-08 16:32
 */
public class ResponseMessage extends Message<TransferResponse> {

    public ResponseMessage() {
    }

    public ResponseMessage(String uuid, TransferResponse response) {
        super(uuid, response);
    }
}