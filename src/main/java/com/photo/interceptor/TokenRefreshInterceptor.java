package com.photo.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Session 刷新拦截器：滑动过期，token 随 Session 同步过期
 */
@Component
public class TokenRefreshInterceptor implements HandlerInterceptor {

    private static final String TOKEN_SESSION_KEY = "token";
    private static final int SESSION_MAX_INACTIVE_INTERVAL = 1200; // 20 分钟
    private static final long REFRESH_THRESHOLD_MS = SESSION_MAX_INACTIVE_INTERVAL * 1000L / 3; // 剩余 < 1/3 时刷新

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) return true;

        String sessionToken = (String) session.getAttribute(TOKEN_SESSION_KEY);
        if (sessionToken == null) return true;

        long now = System.currentTimeMillis();
        long remaining = session.getMaxInactiveInterval() * 1000L - (now - session.getLastAccessedTime());

        if (remaining > 0 && remaining < REFRESH_THRESHOLD_MS) {
            session.setMaxInactiveInterval(SESSION_MAX_INACTIVE_INTERVAL);
        }
        return true;
    }
}
