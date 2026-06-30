package com.qihang.campusmarket.config;

import com.qihang.campusmarket.util.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getSession().getAttribute(SessionKeys.LOGIN_USER) != null) {
            return true;
        }
        response.sendRedirect("/login?redirect=" + request.getRequestURI());
        return false;
    }
}
