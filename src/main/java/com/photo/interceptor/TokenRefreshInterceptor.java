package com.photo.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Session刷新拦截器
 * 当Session剩余时间 < 1/3 TTL时，刷新Session有效期
 * Token存储在Session中，Session刷新时Token也会被刷新
 */
@Component
public class TokenRefreshInterceptor implements HandlerInterceptor {

    private static final String TOKEN_SESSION_KEY = "token";
    private static final int SESSION_MAX_INACTIVE_INTERVAL = 1200; // 20分钟
    private static final long SESSION_REFRESH_THRESHOLD = SESSION_MAX_INACTIVE_INTERVAL * 1000 / 3; // 1/3 TTL

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // 检查token是否在session中
            String sessionToken = (String) session.getAttribute(TOKEN_SESSION_KEY);
            
            if (sessionToken != null) {
                // 获取Session创建时间
                long creationTime = session.getCreationTime();
                long currentTime = System.currentTimeMillis();
                long sessionAge = currentTime - creationTime;
                long sessionRemainingTime = SESSION_MAX_INACTIVE_INTERVAL * 1000 - sessionAge;
                
                // 如果Session剩余时间 < 1/3 TTL，刷新Session有效期
                if (sessionRemainingTime > 0 && sessionRemainingTime < SESSION_REFRESH_THRESHOLD) {
                    session.setMaxInactiveInterval(SESSION_MAX_INACTIVE_INTERVAL);
                }
            }
        }
        
        // 放行请求
        return true;
    }
}