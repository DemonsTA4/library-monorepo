package com.example.library.entity.impl;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "borrow_records") // 注意，之前日志中提到过这个表的外键约束
public class BorrowingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 关联用户，LAZY 表示惰性加载
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 关联图书
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDateTime borrowDate; // 借阅日期

    @Column(nullable = false)
    private LocalDate dueDate;      // 应还日期

    private LocalDateTime returnDate;   // 实际归还日期 (归还后填写)

    @Enumerated(EnumType.STRING)
    private BorrowStatus status; // 借阅状态 (例如：BORROWED, RETURNED, OVERDUE, RESERVED)

    private LocalDateTime reservationDate; // 预约日期 (如果支持预约)
    private LocalDate reservationExpiryDate; // 预约有效截止日期
}