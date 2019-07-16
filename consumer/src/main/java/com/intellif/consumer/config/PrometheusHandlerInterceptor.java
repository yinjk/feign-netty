package com.intellif.consumer.config;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author inori
 * @create 2019-07-15 14:56
 */
public class PrometheusHandlerInterceptor implements HandlerInterceptor {

    private static final Counter requestTotal = Counter.build()
            .name("bench_request_in_count")
            .help("request total count").register();
    private static final Counter passTotal = Counter.build()
            .name("bench_request_pass_count")
            .help("request pass total count").register();
    private static final Gauge rtTime = Gauge.build()
            .name("bench_request_rt_milliseconds")
            .help("request return time milli seconds").register();

    private static final ThreadLocal<Date> startTime = new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        requestTotal.inc();
        startTime.set(new Date());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //doing nothing
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        passTotal.inc();
        Date start = startTime.get();
        long rt = new Date().getTime() - start.getTime();
        rtTime.set(rt);
        startTime.remove();
    }
}