package com.example.library.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal; // 如果需要包含罚款金额
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借阅记录的数据传输对象 (DTO)。
 * 用于在API响应中返回借阅/预约记录的详细信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRecordDto {

    private Long id; // 借阅/预约记录的ID

    private Long userId; // 借阅/预约用户的ID
    private String username; // 借阅/预约用户的用户名

    private Long bookId; // 借阅/预约图书的ID
    private String bookTitle; // 借阅/预约图书的标题

    private LocalDateTime borrowDate; // 借阅日期
    private LocalDate dueDate;      // 应还日期
    private LocalDateTime returnDate;   // 实际归还日期 (如果已归还)

    private String status; // 借阅/预约状态 (例如："BORROWED", "RETURNED", "RESERVED", "OVERDUE")

    private LocalDateTime reservationDate; // 预约日期 (如果这是一条预约记录)
    private LocalDate reservationExpiryDate; // 预约有效截止日期

    // 可选：根据需要添加罚款相关信息
    // private BigDecimal fineAmount; // 罚款金额
    // private boolean finePaid;      // 罚款是否已支付
}
