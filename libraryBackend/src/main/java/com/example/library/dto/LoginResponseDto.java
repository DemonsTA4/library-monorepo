package com.example.library.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor // 方便构造
public class LoginResponseDto {
    private String token;
    private String username;
    private List<String> roles; // 用户角色列表
    // 可以根据需要添加其他用户信息，如用户ID等
    // private Long userId;
}