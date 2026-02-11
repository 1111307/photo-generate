package com.photo.util;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session 管理工具类
 * - 单端登录：新登录挤掉旧端并使旧 Session 失效
 * - 维护 userId<->sessionId、token<->sessionId 双向映射
 * - 提供 Session 有效性校验
 */
public class SessionManager {

    // userId -> Session
    private static final Map<String, HttpSession> USER_SESSION_MAP = new ConcurrentHashMap<>();
    // sessionId -> userId
    private static final Map<String, String> SESSION_USER_MAP = new ConcurrentHashMap<>();

    // token -> sessionId
    private static final Map<String, String> TOKEN_SESSION_MAP = new ConcurrentHashMap<>();
    // sessionId -> token
    private static final Map<String, String> SESSION_TOKEN_MAP = new ConcurrentHashMap<>();

    /**
     * 绑定用户、token 与 session（单端登录）
     */
    public static void bindSession(String userId, String token, HttpSession session) {
        // 挤掉旧会话
        HttpSession oldSession = USER_SESSION_MAP.get(userId);
        if (oldSession != null && !oldSession.getId().equals(session.getId())) {
            removeMappings(oldSession);
            try {
                oldSession.invalidate();
            } catch (Exception ignored) {
            }
        }

        USER_SESSION_MAP.put(userId, session);
        SESSION_USER_MAP.put(session.getId(), userId);
        TOKEN_SESSION_MAP.put(token, session.getId());
        SESSION_TOKEN_MAP.put(session.getId(), token);
    }

    /**
     * 移除 session 相关的所有映射
     */
    public static void removeMappings(HttpSession session) {
        if (session == null) return;
        String sessionId = session.getId();

        String userId = SESSION_USER_MAP.remove(sessionId);
        if (userId != null) {
            HttpSession bound = USER_SESSION_MAP.get(userId);
            if (bound == null || bound.getId().equals(sessionId)) {
                USER_SESSION_MAP.remove(userId);
            }
        }

        String token = SESSION_TOKEN_MAP.remove(sessionId);
        if (token != null) {
            TOKEN_SESSION_MAP.remove(token);
        }
    }

    /**
     * 根据 token 获取 sessionId
     */
    public static String getSessionIdByToken(String token) {
        return TOKEN_SESSION_MAP.get(token);
    }

    /**
     * 根据 sessionId 获取 Session
     */
    public static HttpSession getSessionBySessionId(String sessionId) {
        String userId = SESSION_USER_MAP.get(sessionId);
        if (userId == null) {
            return null;
        }
        return USER_SESSION_MAP.get(userId);
    }

    /**
     * 根据 sessionId 获取 token
     */
    public static String getTokenBySessionId(String sessionId) {
        return SESSION_TOKEN_MAP.get(sessionId);
    }

    /**
     * 仅通过 sessionId 清理映射（用于找不到 HttpSession 实例的场景）
     */
    public static void removeMappings(String sessionId) {
        if (sessionId == null) {
            return;
        }
        String userId = SESSION_USER_MAP.remove(sessionId);
        if (userId != null) {
            HttpSession bound = USER_SESSION_MAP.get(userId);
            if (bound == null || bound.getId().equals(sessionId)) {
                USER_SESSION_MAP.remove(userId);
            }
        }
        String token = SESSION_TOKEN_MAP.remove(sessionId);
        if (token != null) {
            TOKEN_SESSION_MAP.remove(token);
        }
    }

    /**
     * 根据用户ID获取Session
     */
    public static HttpSession getSessionByUserId(String userId) {
        return USER_SESSION_MAP.get(userId);
    }

    /**
     * 校验 Session 是否仍是用户当前会话（未被挤下线且仍在映射表内）
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
