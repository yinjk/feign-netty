package com.intellif.mockhttp;

import com.intellif.feign.RequestMessage;
import com.intellif.feign.TransferResponse;
import feign.Request;
import feign.Response;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

import java.net.URI;
import java.util.Collection;
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


    public TransferResponse execute(Request request) throws Exception {
        MockHttpServletRequest mockReq = toMockRequest(request);
        MockHttpServletResponse mockRes = new MockHttpServletResponse();
        dispatcherServlet.doService(mockReq, mockRes);
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
        mockRequest.setMethod(request.method());
        return mockRequest;
    }
}