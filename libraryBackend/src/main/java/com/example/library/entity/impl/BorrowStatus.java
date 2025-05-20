package com.example.library.entity.impl; // 或者你项目定义的实体/模型包名

import lombok.Getter; // 导入 Lombok @Getter

/**
 * 图书借阅记录的状态枚举
 */
@Getter // 在枚举类级别添加 @Getter，会为所有 final 字段生成 getter
public enum BorrowStatus {
    // 正在借阅中
    BORROWED("Borrowed", "已借出"),

    // 已归还
    RETURNED("Returned", "已归还"),

    // 已逾期未归还
    OVERDUE("Overdue", "已逾期"),

    // 用户已预约，等待图书可用
    RESERVED("Reserved", "已预约"),

    // 预约被取消 (用户主动取消或预约到期自动取消)
    RESERVATION_CANCELED("Reservation Canceled", "预约已取消"),

    // 预约到期 (用户在规定时间内未借阅预约的图书)
    RESERVATION_EXPIRED("Reservation Expired", "预约已过期"),

    // (可选) 如果图书丢失或损坏
    LOST("Lost", "已丢失"),
    DAMAGED("Damaged", "已损坏");

    private final String code; // 确保字段是 final，以便 @Getter 能为它们生成方法
    private final String displayName;

    BorrowStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    // 不再需要手动编写 getCode() 和 getDisplayName() 方法了
    // Lombok 会在编译时自动生成它们：
    // public String getCode() { return this.code; }
    // public String getDisplayName() { return this.displayName; }

    // 仍然可以添加其他自定义的辅助方法
    // public boolean isStillBorrowed() {
    //     return this == BORROWED || this == OVERDUE;
    // }
}