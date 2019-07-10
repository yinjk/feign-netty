package com.intellif.mockhttp;

import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 代理 DispatcherServlet对象，反射获得其{@link DispatcherServlet#doService(HttpServletRequest, HttpServletResponse)}方法，
 * 对该方法进行代理，以模拟springMVC的http请求，从而做到对业务无感知
 *
 * @author inori
 * @create 2019-07-08 14:20
 */
public class DispatcherServletWrap {

    private DispatcherServlet dispatcherServlet;

    private Method doServiceMethod;

    public DispatcherServletWrap(DispatcherServlet dispatcherServlet) {
        this.dispatcherServlet = dispatcherServlet;
    }

    public void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (doServiceMethod == null) {
            this.doServiceMethod = this.dispatcherServlet.getClass().getDeclaredMethod("doService", HttpServletRequest.class, HttpServletResponse.class);
        }
        doServiceMethod.setAccessible(true);
        doServiceMethod.invoke(dispatcherServlet, request, response);
    }
}