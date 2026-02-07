package com.photo.util;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session管理工具类
 * 实现"单端独占，新登挤旧登"功能
 */
public class SessionManager {
    
    // 用户ID -> Session的映射
    private static final Map<String, HttpSession> USER_SESSION_MAP = new ConcurrentHashMap<>();
    
    // Session -> 用户ID的映射
    private static final Map<String, String> SESSION_USER_MAP = new ConcurrentHashMap<>();
    
    /**
     * 添加用户Session映射
     * 如果该用户已有Session，先让旧Session失效
     */
    public static void addUserSession(String userId, HttpSession session) {
        // 检查该用户是否已有Session
        HttpSession oldSession = USER_SESSION_MAP.get(userId);
        if (oldSession != null && oldSession.getId().equals(session.getId())) {
            // 如果是同一个Session，不做处理
            return;
        }
        
        // 如果有旧Session，让旧Session失效
        if (oldSession != null) {
            try {
                oldSession.invalidate();
                // 从映射中移除旧Session
                SESSION_USER_MAP.remove(oldSession.getId());
            } catch (Exception e) {
                // Session可能已经失效，忽略异常
            }
        }
        
        // 添加新Session映射
        USER_SESSION_MAP.put(userId, session);
        SESSION_USER_MAP.put(session.getId(), userId);
    }
    
    /**
     * 移除用户Session映射
     */
    public static void removeUserSession(HttpSession session) {
        String userId = SESSION_USER_MAP.remove(session.getId());
        if (userId != null) {
            USER_SESSION_MAP.remove(userId);
        }
    }
    
    /**
     * 根据用户ID获取Session
     */
    public static HttpSession getSessionByUserId(String userId) {
        return USER_SESSION_MAP.get(userId);
    }
    
    /**
     * 根据Session ID获取用户ID
     */
    public static String getUserIdBySessionId(String sessionId) {
        return SESSION_USER_MAP.get(sessionId);
    }
    
    /**
     * 检查Session是否有效
     */
    public static boolean isSessionValid(HttpSession session) {
        if (session == null) {
            return false;
        }
        
        String userId = SESSION_USER_MAP.get(session.getId());
        if (userId == null) {
            return false;
        }
        
        HttpSession currentSession = USER_SESSION_MAP.get(userId);
        return currentSession != null && currentSession.getId().equals(session.getId());
    }
}