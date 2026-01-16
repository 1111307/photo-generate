package com.photo.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photo.entity.User;
import com.photo.util.SessionManager;
import com.photo.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证拦截器
 * 第二次拦截：拦截非登录和注册接口的请求
 * 如果请求不携带token直接拦截，携带token的去session找对应用户信息
 * 用户信息存在就放入threadlocal，请求结束要删除threadlocal中的用户信息
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN_SESSION_KEY = "token";
    private static final String USER_SESSION_KEY = "user";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
        // 如果请求不携带token，返回JSON错误
        if (token == null || !token.startsWith("Bearer ")) {
            sendErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }
        
        token = token.substring(7);
        HttpSession session = request.getSession(false);
        
        // 如果session不存在，返回JSON错误
        if (session == null) {
            sendErrorResponse(response, 401, "登录已过期，请重新登录");
            return false;
        }
        
        // 验证请求中的token是否与session中的token匹配
        String sessionToken = (String) session.getAttribute(TOKEN_SESSION_KEY);
        if (sessionToken == null || !sessionToken.equals(token)) {
            sendErrorResponse(response, 401, "Token已失效，请重新登录");
            return false;
        }
        
        // 从session中获取用户信息
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        
        // 如果用户信息不存在，返回401错误
        if (user == null) {
            sendErrorResponse(response, 401, "用户信息不存在，请重新登录");
            return false;
        }
        
        // 使用SessionManager验证Session是否有效（检查是否被新登录挤掉）
        if (!SessionManager.isSessionValid(session)) {
            sendErrorResponse(response, 401, "账号已在其他设备登录，请重新登录");
            return false;
        }
        
        // 用户信息存在且Session有效，放入ThreadLocal
        UserContext.setUser(user);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束，删除ThreadLocal中的用户信息，防止数据泄露
        UserContext.clear();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", status);
        result.put("message", message);
        result.put("data", null);
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}