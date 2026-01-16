package com.photo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成BCrypt密码
 */
public class GenerateBCryptPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密后: " + encodedPassword);
        System.out.println("密码长度: " + encodedPassword.length());
        System.out.println("========================================");
        System.out.println();
        System.out.println("请复制上面的BCrypt加密密码，然后执行以下SQL命令：");
        System.out.println("USE photo_generate;");
        System.out.println("UPDATE user SET password='" + encodedPassword + "' WHERE username='admin';");
    }
}