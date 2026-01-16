package com.photo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 密码编码器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置HTTP安全
     * 禁用默认的表单登录和HTTP Basic认证
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  // 禁用CSRF
            .authorizeRequests()
            .antMatchers("/**").permitAll()  // 允许所有请求
            .anyRequest().authenticated()
            .and()
            .httpBasic().disable()  // 禁用HTTP Basic认证
            .formLogin().disable();  // 禁用表单登录
    }
}