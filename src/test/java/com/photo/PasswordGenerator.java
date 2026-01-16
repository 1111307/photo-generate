package com.photo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成器
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("密码长度: " + encodedPassword.length());
        
        // 验证密码
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("密码验证: " + matches);
    }
}