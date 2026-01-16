package com.photo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.entity.User;
import com.photo.mapper.UserMapper;
import com.photo.service.UserService;
import com.photo.util.JwtUtil;
import com.photo.util.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String TOKEN_SESSION_KEY = "token";
    private static final String USER_SESSION_KEY = "user";

    @Override
    public boolean register(String username, String password, String email, String phone) {
        // 检查用户名是否已存在
        User existUser = getByUsername(username);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 使用BCrypt加密密码
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(0); // 默认为普通用户

        return save(user);
    }

    @Override
    public String login(String username, String password, HttpSession session) {
        // 查询用户
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码（使用BCrypt）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 将token和用户信息存入session
        session.setAttribute(TOKEN_SESSION_KEY, token);
        session.setAttribute(USER_SESSION_KEY, user);
        // Session有效期设置为60分钟，确保不会先于Token（30分钟）过期
        session.setMaxInactiveInterval(3600); // 60分钟
        
        // 添加用户Session映射（实现新登挤旧登）
        SessionManager.addUserSession(user.getId(), session);

        return token;
    }

    @Override
    public boolean logout(String token, HttpSession session) {
        // 删除session中的token和用户信息
        session.removeAttribute(TOKEN_SESSION_KEY);
        session.removeAttribute(USER_SESSION_KEY);
        
        // 移除用户Session映射
        SessionManager.removeUserSession(session);
        
        return true;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
}