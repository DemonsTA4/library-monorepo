package com.example.library.User;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // --- 为你的用户生成哈希密码 ---

        // 用户1: admin, 明文密码: admin123
        String usernameAdmin = "admin";
        String rawPasswordAdmin = "admin123";
        String encodedPasswordAdmin = passwordEncoder.encode(rawPasswordAdmin);
        System.out.println("Username: " + usernameAdmin);
        System.out.println("Raw Password: " + rawPasswordAdmin);
        System.out.println("BCrypt Hash: " + encodedPasswordAdmin);
        System.out.println("------------------------------------");

        // 用户2: testuser, 明文密码: user123
        String usernameTestuser = "testuser";
        String rawPasswordTestuser = "user123";
        String encodedPasswordTestuser = passwordEncoder.encode(rawPasswordTestuser);
        System.out.println("Username: " + usernameTestuser);
        System.out.println("Raw Password: " + rawPasswordTestuser);
        System.out.println("BCrypt Hash: " + encodedPasswordTestuser);
        System.out.println("------------------------------------");

        // 如果还有其他用户，继续为他们生成
    }
}