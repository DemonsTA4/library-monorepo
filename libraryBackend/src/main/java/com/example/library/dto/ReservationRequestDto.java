package com.example.library.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * 图书预约请求的数据传输对象 (DTO)。
 * 用于封装前端发送的预约请求信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDto {

    /**
     * 请求预约的用户ID。
     * 注意：与 BorrowRequestDto 类似，此ID应优先从认证主体获取。
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 请求预约的图书ID。
     */
    @NotNull(message = "图书ID不能为空")
    private Long bookId;
}
