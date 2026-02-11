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
 * 登录校验拦截器（无 JWT，只校验随机 token + Session）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN_SESSION_KEY = "token";
    private static final String USER_SESSION_KEY = "user";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, 401, "未登录，请先登录");
            return false;
        }
        String token = authHeader.substring(7);

        HttpSession session = request.getSession(false);
        if (session == null) {
            sendErrorResponse(response, 401, "登录已过期，请重新登录");
            return false;
        }

        // 校验 token 与 session 映射是否一致
        String mappedSessionId = SessionManager.getSessionIdByToken(token);
        if (mappedSessionId == null || !mappedSessionId.equals(session.getId())) {
            sendErrorResponse(response, 401, "Token无效或已过期，请重新登录");
            return false;
        }

        // 校验 session 中存的 token 一致
        String sessionToken = (String) session.getAttribute(TOKEN_SESSION_KEY);
        if (sessionToken == null || !sessionToken.equals(token)) {
            sendErrorResponse(response, 401, "Token已失效，请重新登录");
            return false;
        }

        // 校验 session 仍是当前有效会话（防止被挤下线）
        if (!SessionManager.isSessionValid(session)) {
            sendErrorResponse(response, 401, "账号已在其他设备登录，请重新登录");
            return false;
        }

        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) {
            sendErrorResponse(response, 401, "用户信息不存在，请重新登录");
            return false;
        }

        UserContext.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

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
