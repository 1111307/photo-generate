package com.photo.interceptor;

import com.photo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Token刷新拦截器
 * 第一次拦截：检查token是否即将过期，如果即将过期则生成新token并通过响应头返回
 * 实现token无感知刷新，前端需要监听响应头中的新token并更新
 */
@Component
public class TokenRefreshInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TOKEN_SESSION_KEY = "token";
    private static final String USER_SESSION_KEY = "user";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                // 检查token是否在session中
                String sessionToken = (String) session.getAttribute(TOKEN_SESSION_KEY);
                
                if (sessionToken != null && sessionToken.equals(token)) {
                    // Token在session中，刷新session有效期
                    session.setMaxInactiveInterval(1200); // 20分钟
                    
                    // 检查token是否即将过期（剩余时间小于有效期的1/3）
                    if (jwtUtil.canTokenBeRefreshed(token)) {
                        // 生成新token
                        String newToken = jwtUtil.refreshToken(token);
                        
                        if (newToken != null) {
                            // 更新session中的token
                            session.setAttribute(TOKEN_SESSION_KEY, newToken);
                            
                            // 将新token通过响应头返回给前端
                            response.setHeader("Authorization", "Bearer " + newToken);
                            response.setHeader("Access-Control-Expose-Headers", "Authorization");
                        }
                    }
                }
            }
        }
        
        // 无论token是否在session中，都放行
        return true;
    }
}