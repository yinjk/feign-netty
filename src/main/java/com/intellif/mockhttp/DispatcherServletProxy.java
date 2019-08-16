package com.intellif.mockhttp;

import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 代理 DispatcherServlet对象，代理其{@link DispatcherServlet#processRequest(HttpServletRequest, HttpServletResponse)}方法，
 * 因为该方法是protected，无法直接调用，将其改造成public，以模拟springMVC的http请求，从而做到对业务无感知
 *
 * @author inori
 * @create 2019-07-08 14:20
 */
public class DispatcherServletProxy extends DispatcherServlet {

    public void doProcessRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        super.processRequest(request, response);
    }
}