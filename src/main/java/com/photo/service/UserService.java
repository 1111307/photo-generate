package com.photo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.photo.entity.User;

import javax.servlet.http.HttpSession;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    boolean register(String username, String password, String email, String phone);

    /**
     * 用户登录
     */
    String login(String username, String password, HttpSession session);

    /**
     * 用户登出
     */
    boolean logout(String token, HttpSession session);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
}