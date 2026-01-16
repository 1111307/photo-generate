package com.photo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码测试类
 */
public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中的密码
        String dbPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH";
        
        // 尝试验证的密码
        String inputPassword = "admin123";
        
        System.out.println("数据库密码: " + dbPassword);
        System.out.println("输入密码: " + inputPassword);
        System.out.println("密码长度: " + dbPassword.length());
        
        // 验证密码
        boolean matches = encoder.matches(inputPassword, dbPassword);
        System.out.println("密码验证结果: " + matches);
        
        // 生成一个新的admin123的BCrypt密码
        String newEncodedPassword = encoder.encode("admin123");
        System.out.println("新生成的密码: " + newEncodedPassword);
        System.out.println("新密码长度: " + newEncodedPassword.length());
    }
}