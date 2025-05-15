package com.example.library.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        JdbcConfig.class,
        MybatisConfig.class,
        BookRelatedConfig.class,
        PasswordEncoderConfig.class,
        SecurityConfig.class,
})
public class SpringConfig {
}
