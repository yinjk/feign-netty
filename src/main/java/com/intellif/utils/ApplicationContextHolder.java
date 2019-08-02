package com.intellif.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * applicationContext 持有者
 *
 * @author inori
 * @create 2019-07-24 17:46
 */
public final class ApplicationContextHolder implements ApplicationListener<ContextRefreshedEvent> {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContextHolder.applicationContext = event.getApplicationContext();
    }
}