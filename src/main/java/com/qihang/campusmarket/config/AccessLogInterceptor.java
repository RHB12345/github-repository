package com.qihang.campusmarket.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessLogInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AccessLogInterceptor.class);
    private static final String START_TIME = "accessLogStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startedAt = request.getAttribute(START_TIME);
        long elapsed = startedAt instanceof Long start ? System.currentTimeMillis() - start : -1;
        String client = clientIp(request);
        String query = request.getQueryString();
        String uri = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
        if (ex == null) {
            log.info("access method={} uri=\"{}\" status={} costMs={} ip={}",
                    request.getMethod(), uri, response.getStatus(), elapsed, client);
        } else {
            log.warn("access method={} uri=\"{}\" status={} costMs={} ip={} error={}",
                    request.getMethod(), uri, response.getStatus(), elapsed, client, ex.getClass().getSimpleName());
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
