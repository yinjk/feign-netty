package com.intellif.feign;

import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author inori
 * @create 2019-07-08 17:53
 */
public class Test {

    public static void main(String[] args) throws NoSuchMethodException {
        Method doService = DispatcherServlet.class.getDeclaredMethod("doDispatch", HttpServletRequest.class, HttpServletResponse.class);
        System.out.println(doService);
    }


}