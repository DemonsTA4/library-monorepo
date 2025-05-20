package com.example.library.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * 图书借阅请求的数据传输对象 (DTO)。
 * 用于封装前端发送的借阅请求信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequestDto {

    /**
     * 请求借阅的用户ID。
     * 注意：在实际安全的实现中，这个用户ID应该从当前认证的 Principal（例如JWT Token中的用户信息）获取，
     * 而不是直接由前端传递，以防止用户替他人操作。
     * 如果前端传递了，后端应该验证其与当前登录用户是否一致。
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 请求借阅的图书ID。
     */
    @NotNull(message = "图书ID不能为空")
    private Long bookId;
}
