package com.photo.util;

import com.photo.entity.User;

/**
 * 用户上下文工具类
 * 使用ThreadLocal存储当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<User> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setUser(User user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取当前用户
     */
    public static User getUser() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户ID
     */
    public static String getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        User user = getUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清除当前用户信息
     * 防止内存泄漏，请求结束后必须调用
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}