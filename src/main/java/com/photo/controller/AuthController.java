package com.photo.controller;

import com.photo.common.Result;
import com.photo.entity.User;
import com.photo.service.UserService;
import com.photo.util.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, String> params, HttpSession session) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            String email = params.get("email");
            String phone = params.get("phone");

            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }

            // 校验用户名：3-20 位，字母数字下划线
            if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
                return Result.error("用户名必须是3-20位，且仅包含字母、数字、下划线");
            }

            // 校验密码：6-32 位，包含字母和数字
            if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z0-9!@#$%^&*]{6,32}$")) {
                return Result.error("密码需6-32位，且同时包含字母和数字");
            }

            if (email != null && !email.trim().isEmpty()) {
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    return Result.error("邮箱格式不正确");
                }
            }

            if (phone != null && !phone.trim().isEmpty()) {
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    return Result.error("请输入有效的11位中国手机号");
                }
            }

            boolean success = userService.register(username, password, email, phone);
            if (success) {
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params, HttpSession session) {
        try {
            String username = params.get("username");
            String password = params.get("password");

            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }

            String token = userService.login(username, password, session);
            User user = (User) session.getAttribute("user");

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", username);
            data.put("role", user.getRole());

            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authorization, HttpSession session) {
        try {
            String token = authorization.substring(7);
            userService.logout(token, session);
            return Result.success("退出成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return Result.error("用户未登录");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> params, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return Result.error("用户未登录");
            }

            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            String confirmPassword = params.get("confirmPassword");

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Result.error("旧密码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                return Result.error("确认密码不能为空");
            }

            if (newPassword.length() < 6) {
                return Result.error("新密码长度不能少于6位");
            }
            if (newPassword.length() > 32) {
                return Result.error("新密码长度不能超过32位");
            }

            if (!newPassword.equals(confirmPassword)) {
                return Result.error("两次输入的密码不一致");
            }

            if (oldPassword.equals(newPassword)) {
                return Result.error("新密码不能与旧密码相同");
            }

            boolean success = userService.changePassword(user.getId(), oldPassword, newPassword);
            if (success) {
                // 修改密码后清理会话与映射
                session.removeAttribute("token");
                session.removeAttribute("user");
                SessionManager.removeMappings(session);
                session.invalidate();
                return Result.success("密码修改成功");
            } else {
                return Result.error("旧密码错误");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
