package com.photo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.photo.entity.User;
import com.photo.mapper.UserMapper;
import com.photo.service.UserService;
import com.photo.util.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

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

        // 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成随机 token（不含业务信息）
        String token = UUID.randomUUID().toString().replace("-", "");

        // 存入 session
        session.setAttribute(TOKEN_SESSION_KEY, token);
        session.setAttribute(USER_SESSION_KEY, user);
        session.setMaxInactiveInterval(1200); // 20 分钟

        // 维护映射 & 单端登录
        SessionManager.bindSession(user.getId(), token, session);

        return token;
    }

    @Override
    public boolean logout(String token, HttpSession session) {
        // 优先按 token 定位原会话并清理（即使当前 session 已失效）
        String mappedSessionId = SessionManager.getSessionIdByToken(token);
        if (mappedSessionId != null) {
            HttpSession mappedSession = SessionManager.getSessionBySessionId(mappedSessionId);
            if (mappedSession != null) {
                SessionManager.removeMappings(mappedSession);
                try { mappedSession.invalidate(); } catch (Exception ignored) {}
            } else {
                // 找不到 HttpSession 实例，按 sessionId 清理映射
                SessionManager.removeMappings(mappedSessionId);
            }
        }

        // 再清理当前请求携带的 session（若存在且未失效）
        if (session != null) {
            session.removeAttribute(TOKEN_SESSION_KEY);
            session.removeAttribute(USER_SESSION_KEY);
            SessionManager.removeMappings(session);
            try { session.invalidate(); } catch (Exception ignored) {}
        }
        return true;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }
}
