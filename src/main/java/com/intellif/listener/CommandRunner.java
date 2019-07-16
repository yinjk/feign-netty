package com.intellif.listener;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.DispatcherServlet;

import java.lang.reflect.Method;

/**
 * @author inori
 * @create 2019-07-08 18:30
 */
public class CommandRunner implements CommandLineRunner, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        DispatcherServlet dispatcherServlet = applicationContext.getBean(DispatcherServlet.class);
        Method onRefresh = DispatcherServlet.class.getDeclaredMethod("onRefresh", ApplicationContext.class);
        onRefresh.setAccessible(true);
        onRefresh.invoke(dispatcherServlet, applicationContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}