package com.photo.controller;

import com.photo.common.Result;
import com.photo.entity.User;
import com.photo.service.UserService;
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
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", username);
            
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
            return Result.success("登出成功");
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
}