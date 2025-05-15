package com.example.library.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BookRelatedConfig {
    @Bean
    public Map<String, String> bookCategoryMap() {
        Map<String, String> bookCategoryMap = new HashMap<>();
        bookCategoryMap.put("novel", "小说");
        bookCategoryMap.put("sci-fi", "科幻");
        bookCategoryMap.put("tech", "技术类");
        bookCategoryMap.put("edu", "教育");
        return bookCategoryMap;
    }
}
