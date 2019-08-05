package com.intellif.feign.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于传输的响应信息，响应中附带了一个uuid，这个id是请求{@link TransferRequest#uuid}中传过来的，
 * 由于netty是事件驱动模型，通过该id我们在客户端就能知道那一个响应是当前请求的响应
 *
 * @author inori
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {

    /**
     * 为了避免频繁创建对象，这里把空header做成静态常量
     */
    private static final Map<String, Collection<String>> emptyHeaders = new HashMap<>(0);

    private String uuid;
    private int status;
    private String reason;
    private Map<String, Collection<String>> headers;
    private byte[] body;
    private TransferRequest request;

    //将我们的传输Response转换成feign的response
    public feign.Response toFeignResponse() {
        feign.Request targetReq = null;
        if (this.request != null) {
            targetReq = this.request.toFeignRequest();
        }
        return feign.Response.builder()
                .request(targetReq)
                .headers(this.headers)
                .status(this.status)
                .reason(this.reason)
                .body(this.body)
                .build();
    }

    /**
     * 处理请求抛出了任何异常，创建transfer response，直接返回500，并且把异常信息传回去。
     *
     * @param request 远程传输request
     * @param errMsg  错误信息
     * @return TransferResponse
     */
    public static TransferResponse createErrorResponse(TransferRequest request, String errMsg) {
        errMsg = errMsg == null ? "" : errMsg;
        return new TransferResponse(request.getUuid(), HttpStatus.INTERNAL_SERVER_ERROR.value(), errMsg, emptyHeaders, errMsg.getBytes(), request);
    }


    /**
     * 服务器繁忙，暂时无法处理更多的请求（线程池拒绝的请求），直接返回503，并告诉客户端当前服务器繁忙
     *
     * @param request 远程传输request
     * @return TransferResponse
     */
    public static TransferResponse create503ErrorResponse(TransferRequest request) {
        String reason = "The server is busy. Please try again later";
        return new TransferResponse(request.getUuid(), HttpStatus.SERVICE_UNAVAILABLE.value(), reason, emptyHeaders, reason.getBytes(), request);
    }
}