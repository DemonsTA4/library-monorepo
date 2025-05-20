package com.example.library.service; // 确保包名正确

import com.example.library.entity.impl.BorrowingRecord; // 你的 BorrowingRecord 实体
import java.util.List;

public interface BorrowingService {

    /**
     * 用户借阅图书
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 创建的借阅记录
     * @throws IllegalArgumentException 如果用户或图书不存在
     * @throws IllegalStateException 如果图书无库存、用户达到借阅上限、或已借阅此书未还等
     */
    BorrowingRecord borrowBook(Long userId, Long bookId);

    /**
     * 用户归还图书
     * @param borrowingRecordId 借阅记录ID
     * @return 更新后的借阅记录
     * @throws IllegalArgumentException 如果借阅记录不存在
     * @throws IllegalStateException 如果图书已归还
     */
    BorrowingRecord returnBook(Long borrowingRecordId);

    /**
     * 用户续借图书
     * @param borrowingRecordId 借阅记录ID
     * @return 更新后的借阅记录
     * @throws IllegalArgumentException 如果借阅记录无效或已归还
     * @throws IllegalStateException 如果不满足续借条件 (例如已被预约、超期过久、达到续借上限)
     */
    BorrowingRecord renewBook(Long borrowingRecordId);

    /**
     * 用户预约图书
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 创建的预约记录
     * @throws IllegalArgumentException 如果用户或图书不存在
     * @throws IllegalStateException 如果图书当前不可预约或用户已预约/借阅此书
     */
    BorrowingRecord reserveBook(Long userId, Long bookId);

    /**
     * 用户取消自己的预约
     * @param userId 用户ID (用于权限校验)
     * @param reservationId 预约记录ID
     * @throws IllegalArgumentException 如果预约记录无效
     * @throws SecurityException 如果用户无权取消此预约
     */
    void cancelReservation(Long userId, Long reservationId);

    /**
     * 获取用户的借阅历史
     * @param userId 用户ID
     * @return 该用户的借阅记录列表
     * @throws IllegalArgumentException 如果用户不存在
     */
    List<BorrowingRecord> getUserBorrowingHistory(Long userId);

    /**
     * 查找所有已逾期且未归还的图书记录
     * @return 逾期借阅记录列表
     */
    List<BorrowingRecord> findOverdueBooks();

    // --- 你可以根据需要添加更多方法 ---
    // 例如:
    // List<BorrowingRecord> getCurrentUserBorrowings(Long userId);
    // BorrowingRecord getBorrowingRecordById(Long recordId);
    // void processBookReturnAndNotifyNextReservation(Long borrowingRecordId);
    // List<Fine> getUserUnpaidFines(Long userId);
    // void payFine(Long fineId);
}