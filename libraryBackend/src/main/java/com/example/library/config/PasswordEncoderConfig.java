package com.example.library.config; // 确保包名与你的项目结构一致

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // 表明这是一个Spring配置类
public class PasswordEncoderConfig {

    /**
     * 定义一个 PasswordEncoder Bean，Spring Security 将使用它来编码和验证密码。
     * 我们使用 BCryptPasswordEncoder，它是一种安全的密码哈希算法。
     * @return PasswordEncoder 的实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}