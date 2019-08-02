package com.intellif.mockhttp;

import com.intellif.feign.transfer.RequestMessage;
import com.intellif.feign.transfer.TransferResponse;
import feign.Request;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 模拟对springMVC的http调用
 *
 * @author inori
 * @create 2019-07-08 14:14
 */
public class MockHttpClient {

    private DispatcherServletWrap dispatcherServlet;

    public MockHttpClient(DispatcherServlet dispatcherServlet) {
        this.dispatcherServlet = new DispatcherServletWrap(dispatcherServlet);
    }

    /**
     * 调用dispatcherServlet执行request请求
     *
     * @param request 请求
     * @return 基于传输的响应
     * @throws Exception 任何异常
     */
    public TransferResponse execute(Request request) throws Exception {
        MockHttpServletRequest mockReq = toMockRequest(request);
        MockHttpServletResponse mockRes = new MockHttpServletResponse();
        System.out.printf("server begin real handle request: => %d \n", new Date().getTime());
        dispatcherServlet.doService(mockReq, mockRes);
        System.out.printf("server end real handle request: => %d \n", new Date().getTime());
        Map<String, Collection<String>> headers = new HashMap<>();
        for (String name : mockRes.getHeaderNames()) {
            headers.put(name, mockRes.getHeaders(name));
        }
        //构建netty传输的Response
        return new TransferResponse(mockRes.getStatus(), mockRes.getErrorMessage(), headers, mockRes.getContentAsByteArray(), RequestMessage.toTransferRequest(request));
    }

    private MockHttpServletRequest toMockRequest(Request request) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(request.body());
        //填充所有的header
        for (Map.Entry<String, Collection<String>> entry : request.headers().entrySet()) {
            for (String value : entry.getValue()) {
                mockRequest.addHeader(entry.getKey(), value);
            }
        }
        URI uri = URI.create(request.url());
        mockRequest.setRequestURI(uri.getPath());
        mockRequest.setServletPath(uri.getPath());
        mockRequest.setQueryString(uri.getQuery());
        if (!StringUtils.isBlank(uri.getQuery())) {
            String[] parameters = uri.getQuery().split("&");
            for (String parameter : parameters) {
                if (StringUtils.isBlank(parameter)) {
                    continue;
                }
                String[] param = parameter.split("=");
                String key = param[0];
                String value = "";
                if (param.length >= 2) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < param.length; i++) {
                        builder.append(param[i].trim()).append(",");
                    }
                    value = builder.substring(0, builder.lastIndexOf(","));
                }
                mockRequest.setParameter(key, value);
            }
        }
        mockRequest.setPathInfo(uri.getQuery());
        mockRequest.setMethod(request.method());
        return mockRequest;
    }
}