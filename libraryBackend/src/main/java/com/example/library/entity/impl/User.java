package com.example.library.entity.impl; // 你的包名

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*; // 对于 Spring Boot 3.x，使用 jakarta.persistence
// 对于 Spring Boot 2.x，使用 javax.persistence

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // 确保数据库中表名是 "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50) // 建议为 username 添加长度限制
    private String username;

    @Column(nullable = false, length = 255) // 存储哈希后的密码，长度可能需要调整，BCrypt 哈希通常是60个字符，但留些余地
    private String password;

    @Column(length = 50) // 角色名通常不会太长
    private String role; // e.g., "ROLE_USER", "ROLE_ADMIN" for Spring Security

    // 建议为 Spring Security UserDetails 添加一个 'enabled' 字段
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE") // 默认为 true
    private boolean enabled = true; // 用户账户是否启用

    // 你还可以根据需要添加以下字段，以更精细地控制账户状态：
    // @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    // private boolean accountNonExpired = true;

    // @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    // private boolean accountNonLocked = true;

    // @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    // private boolean credentialsNonExpired = true;


    // 如果你使用 Lombok 并且不想手动写构造函数来初始化 enabled (虽然 @AllArgsConstructor 会包含它)
    // 你可以在需要的地方确保 enabled 被正确设置。
    // 例如，在创建新用户时。
}