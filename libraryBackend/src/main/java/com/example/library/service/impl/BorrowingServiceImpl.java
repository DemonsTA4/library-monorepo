package com.example.library.service.impl;

import com.example.library.entity.impl.Book; // 你的 Book 实体
import com.example.library.entity.impl.User; // 你的 User 实体
import com.example.library.entity.impl.BorrowingRecord; // 你的 BorrowingRecord 实体
import com.example.library.entity.impl.BorrowStatus;   // 你的 BorrowStatus 枚举
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowingRecordRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.BorrowingService;
// import com.example.library.service.FineService; // 如果你创建了单独的罚款服务
// import com.example.library.service.NotificationService; // 如果你创建了通知服务 (例如邮件)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 用于从配置文件读取参数
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional // 默认情况下，所有公共方法都将在事务中运行
public class BorrowingServiceImpl implements BorrowingService {

    private static final Logger log = LoggerFactory.getLogger(BorrowingServiceImpl.class);

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    // private final FineService fineService; // 可选注入
    // private final NotificationService notificationService; // 可选注入

    // 从 application.properties 读取配置值
    @Value("${library.borrow.default-days:30}") // 默认借阅天数，可配置，默认为30天
    private int defaultBorrowDays;

    @Value("${library.borrow.max-books-per-user:5}") // 每用户最大借阅数量，可配置，默认为5本
    private int maxBorrowCountPerUser;

    @Value("${library.fine.daily-rate:0.50}") // 每日罚款费率，可配置，默认为0.50
    private BigDecimal dailyFineRate;

    @Value("${library.renew.max-count:1}") // 最大续借次数
    private int maxRenewals;

    @Value("${library.renew.days:15}") // 每次续借增加的天数
    private int renewalDays;

    @Value("${library.reservation.valid-days:3}") // 预约保留有效天数
    private int reservationValidDays;


    @Autowired
    public BorrowingServiceImpl(BorrowingRecordRepository borrowingRecordRepository,
                                UserRepository userRepository,
                                BookRepository bookRepository
            /*,FineService fineService, NotificationService notificationService (可选注入) */) {
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        // this.fineService = fineService;
        // this.notificationService = notificationService;
    }

    @Override
    public BorrowingRecord borrowBook(Long userId, Long bookId) {
        log.info("Attempting to borrow book ID: {} for user ID: {}", bookId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Borrow attempt failed: User not found with ID {}", userId);
                    return new IllegalArgumentException("用户不存在 (ID: " + userId + ")");
                });
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.warn("Borrow attempt failed: Book not found with ID {}", bookId);
                    return new IllegalArgumentException("图书不存在 (ID: " + bookId + ")");
                });

        // 1. 检查图书库存
        if (book.getQuantity() <= 0) {
            log.warn("Borrow attempt failed for book '{}' (ID: {}): No stock available", book.getTitle(), bookId);
            throw new IllegalStateException("《" + book.getTitle() + "》已无库存，无法借阅。");
        }

        // 2. 检查用户借阅限制
        long currentBorrows = borrowingRecordRepository.countByUserAndStatusIn(user,
                List.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE));
        if (currentBorrows >= maxBorrowCountPerUser) {
            log.warn("Borrow attempt failed for user '{}' (ID: {}): Max borrow count ({}) reached", user.getUsername(), userId, maxBorrowCountPerUser);
            throw new IllegalStateException("用户已达到最大借阅数量 (" + maxBorrowCountPerUser + "本)。");
        }

        // 3. (可选) 检查用户是否有未处理的严重罚款
        // if (fineService != null && fineService.hasUnpaidSignificantFines(userId)) {
        //     log.warn("Borrow attempt failed for user '{}' (ID: {}): Has unpaid significant fines", user.getUsername(), userId);
        //     throw new IllegalStateException("用户有未处理的严重罚款，请先处理。");
        // }

        // 4. 检查用户是否已借阅此书且未归还
        if (borrowingRecordRepository.existsByUserAndBookAndStatusIn(user, book, List.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE))) {
            log.warn("Borrow attempt failed for user '{}' (ID: {}): Book '{}' (ID: {}) already borrowed and not returned", user.getUsername(), userId, book.getTitle(), bookId);
            throw new IllegalStateException("用户已借阅《" + book.getTitle() + "》且尚未归还。");
        }

        // (可选) 检查这本书是否被当前用户预约，并且预约有效
        Optional<BorrowingRecord> existingReservation = borrowingRecordRepository.findByUserAndBookAndStatus(user, book, BorrowStatus.RESERVED);
        if (existingReservation.isPresent()) {
            // 如果是被当前用户预约的，可以处理为借阅，并更新预约状态或删除预约记录
            BorrowingRecord reservationRecord = existingReservation.get();
            // 检查预约是否仍然有效 (例如，未过期)
            if (reservationRecord.getReservationExpiryDate() != null && LocalDate.now().isAfter(reservationRecord.getReservationExpiryDate())) {
                log.warn("Reservation for book '{}' by user '{}' has expired.", book.getTitle(), user.getUsername());
                reservationRecord.setStatus(BorrowStatus.RESERVATION_EXPIRED);
                borrowingRecordRepository.save(reservationRecord);
                // 这里可以选择继续允许借阅（如果还有库存），或者提示预约已过期
                // throw new IllegalStateException("您的预约已过期，请重新预约或借阅。");
            } else {
                // 有效预约，更新为已借阅
                log.info("User '{}' is borrowing a reserved book '{}'", user.getUsername(), book.getTitle());
                reservationRecord.setStatus(BorrowStatus.BORROWED); // 或者删除预约记录，新建借阅记录
                reservationRecord.setBorrowDate(LocalDateTime.now());
                reservationRecord.setDueDate(LocalDate.now().plusDays(defaultBorrowDays));
                reservationRecord.setReservationDate(null); // 清除预约相关日期
                reservationRecord.setReservationExpiryDate(null);

                book.setQuantity(book.getQuantity() - 1);
                bookRepository.save(book);
                return borrowingRecordRepository.save(reservationRecord);
            }
        }
        // (可选) 检查这本书是否被其他用户预约 (如果设置了严格的预约优先规则)
        // List<BorrowingRecord> reservationsByOthers = borrowingRecordRepository.findByBookAndStatusAndUserNot(book, BorrowStatus.RESERVED, user);
        // if (!reservationsByOthers.isEmpty()) {
        //     log.warn("Book '{}' (ID: {}) is reserved by other users.", book.getTitle(), bookId);
        //     throw new IllegalStateException("《" + book.getTitle() + "》已被其他用户预约。");
        // }


        BorrowingRecord newRecord = new BorrowingRecord();
        newRecord.setUser(user);
        newRecord.setBook(book);
        newRecord.setBorrowDate(LocalDateTime.now());
        newRecord.setDueDate(LocalDate.now().plusDays(defaultBorrowDays));
        newRecord.setStatus(BorrowStatus.BORROWED);

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        BorrowingRecord savedRecord = borrowingRecordRepository.save(newRecord);
        log.info("Book '{}' (ID: {}) successfully borrowed by user '{}' (ID: {}). Record ID: {}",
                book.getTitle(), bookId, user.getUsername(), userId, savedRecord.getId());
        return savedRecord;
    }

    @Override
    public BorrowingRecord returnBook(Long borrowingRecordId) {
        log.info("Attempting to return book for borrowing record ID: {}", borrowingRecordId);
        BorrowingRecord record = borrowingRecordRepository.findById(borrowingRecordId)
                .orElseThrow(() -> {
                    log.warn("Return attempt failed: Borrowing record not found with ID {}", borrowingRecordId);
                    return new IllegalArgumentException("借阅记录不存在 (ID: " + borrowingRecordId + ")");
                });

        if (record.getStatus() == BorrowStatus.RETURNED) {
            log.warn("Return attempt failed for record ID {}: Book already returned", borrowingRecordId);
            throw new IllegalStateException("该书已归还。");
        }

        record.setReturnDate(LocalDateTime.now());
        record.setStatus(BorrowStatus.RETURNED);

        Book book = record.getBook();
        book.setQuantity(book.getQuantity() + 1); // 归还后库存增加
        bookRepository.save(book);

        // 计算并应用罚款
        calculateAndApplyFine(record);

        BorrowingRecord savedRecord = borrowingRecordRepository.save(record);
        log.info("Book '{}' (ID: {}) successfully returned by user '{}' (ID: {}). Record ID: {}",
                book.getTitle(), book.getId(), record.getUser().getUsername(), record.getUser().getId(), savedRecord.getId());

        // (可选) 检查是否有用户预约了此书，并发送通知
        // if (notificationService != null) {
        //     checkAndNotifyNextReservation(book);
        // }
        return savedRecord;
    }

    private void calculateAndApplyFine(BorrowingRecord record) {
        if (record.getReturnDate() != null && record.getReturnDate().toLocalDate().isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), record.getReturnDate().toLocalDate());
            if (overdueDays > 0) {
                BigDecimal fineAmount = dailyFineRate.multiply(new BigDecimal(overdueDays));
                log.info("Book '{}' (Record ID: {}) is overdue by {} days. Fine calculated: {}",
                        record.getBook().getTitle(), record.getId(), overdueDays, fineAmount);
                // 实际项目中，这里会将 fineAmount 保存到 BorrowingRecord 或专门的 Fine 表
                // 并标记为未支付，然后通过其他接口处理支付。
                // 例如: record.setFineAmount(fineAmount); record.setFinePaid(false);
            }
        }
    }

    @Override
    public BorrowingRecord renewBook(Long borrowingRecordId) {
        log.info("Attempting to renew book for borrowing record ID: {}", borrowingRecordId);
        BorrowingRecord record = borrowingRecordRepository.findById(borrowingRecordId)
                .filter(r -> r.getStatus() == BorrowStatus.BORROWED || r.getStatus() == BorrowStatus.OVERDUE)
                .orElseThrow(() -> {
                    log.warn("Renew attempt failed: Invalid or already returned borrowing record with ID {}", borrowingRecordId);
                    return new IllegalArgumentException("无效的借阅记录或图书已归还，无法续借 (ID: " + borrowingRecordId + ")");
                });

        // 1. 检查是否允许续借 (例如：没有被其他用户预约)
        // if (borrowingRecordRepository.existsByBookAndStatusAndUserNot(record.getBook(), BorrowStatus.RESERVED, record.getUser())) {
        //     log.warn("Renew attempt failed for record ID {}: Book '{}' is reserved by another user.", borrowingRecordId, record.getBook().getTitle());
        //     throw new IllegalStateException("《" + record.getBook().getTitle() + "》已被其他用户预约，无法续借。");
        // }

        // 2. 检查续借次数限制 (假设 BorrowingRecord 有 renewalCount 字段)
        // int currentRenewals = record.getRenewalCount() == null ? 0 : record.getRenewalCount();
        // if (currentRenewals >= maxRenewals) {
        //     log.warn("Renew attempt failed for record ID {}: Max renewals ({}) reached.", borrowingRecordId, maxRenewals);
        //     throw new IllegalStateException("已达到最大续借次数。");
        // }

        // 3. 检查是否在允许续借的时间窗口内 (例如：到期前 N 天内，或超期 M 天内)
        LocalDate today = LocalDate.now();
        if (today.isAfter(record.getDueDate().plusDays(7))) { // 示例：超期7天以上不允许续借
            log.warn("Renew attempt failed for record ID {}: Book is overdue for too long.", borrowingRecordId);
            throw new IllegalStateException("图书逾期过久，无法续借。");
        }

        record.setDueDate(record.getDueDate().plusDays(renewalDays));
        // record.setRenewalCount(currentRenewals + 1);
        record.setStatus(BorrowStatus.BORROWED); // 如果之前是 OVERDUE，续借成功后状态更新为 BORROWED

        BorrowingRecord savedRecord = borrowingRecordRepository.save(record);
        log.info("Book '{}' (Record ID: {}) successfully renewed by user '{}'. New due date: {}",
                record.getBook().getTitle(), record.getId(), record.getUser().getUsername(), savedRecord.getDueDate());
        return savedRecord;
    }

    @Override
    public BorrowingRecord reserveBook(Long userId, Long bookId) {
        log.info("Attempting to reserve book ID: {} for user ID: {}", bookId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Reservation attempt failed: User not found with ID {}", userId);
                    return new IllegalArgumentException("用户不存在 (ID: " + userId + ")");
                });
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.warn("Reservation attempt failed: Book not found with ID {}", bookId);
                    return new IllegalArgumentException("图书不存在 (ID: " + bookId + ")");
                });

        // 1. 检查图书当前是否可被预约 (例如，库存为0)
        // if (book.getQuantity() > 0) {
        //    log.warn("Reservation attempt failed for book '{}': Stock available, please borrow directly.", book.getTitle());
        //    throw new IllegalStateException("《" + book.getTitle() + "》当前有库存，请直接借阅。");
        // }

        // 2. 检查用户是否已预约或已借阅此书
        if (borrowingRecordRepository.existsByUserAndBookAndStatusIn(user, book,
                List.of(BorrowStatus.RESERVED, BorrowStatus.BORROWED, BorrowStatus.OVERDUE))) {
            log.warn("Reservation attempt failed for user '{}': Book '{}' already reserved or borrowed.", user.getUsername(), book.getTitle());
            throw new IllegalStateException("用户已预约或已借阅此书。");
        }

        BorrowingRecord reservation = new BorrowingRecord();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setReservationExpiryDate(LocalDate.now().plusDays(reservationValidDays));
        reservation.setStatus(BorrowStatus.RESERVED);

        BorrowingRecord savedReservation = borrowingRecordRepository.save(reservation);
        log.info("Book '{}' (ID: {}) successfully reserved by user '{}' (ID: {}). Reservation ID: {}",
                book.getTitle(), bookId, user.getUsername(), userId, savedReservation.getId());
        return savedReservation;
    }

    @Override
    public void cancelReservation(Long userId, Long reservationId) { // 添加 userId 参数以验证操作权限
        log.info("Attempting to cancel reservation ID: {} by user ID: {}", reservationId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在 (ID: " + userId + ")"));

        BorrowingRecord reservation = borrowingRecordRepository.findById(reservationId)
                .filter(r -> r.getStatus() == BorrowStatus.RESERVED)
                .orElseThrow(() -> {
                    log.warn("Cancel reservation attempt failed: Invalid or non-reserved record with ID {}", reservationId);
                    return new IllegalArgumentException("无效的预约记录 (ID: " + reservationId + ")");
                });

        // 确保是用户自己的预约才能取消 (或者管理员可以取消任何预约)
        if (!reservation.getUser().equals(user) /* && !authService.isAdmin(currentUser) */) {
            log.warn("User '{}' attempted to cancel reservation ID {} which does not belong to them.", user.getUsername(), reservationId);
            throw new SecurityException("无权取消此预约。");
        }

        // borrowingRecordRepository.delete(reservation); // 直接删除
        // 或者标记为已取消
        reservation.setStatus(BorrowStatus.RESERVATION_CANCELED);
        borrowingRecordRepository.save(reservation);
        log.info("Reservation ID: {} for book '{}' successfully canceled by user '{}'.",
                reservationId, reservation.getBook().getTitle(), user.getUsername());
    }


    @Override
    public List<BorrowingRecord> getUserBorrowingHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在 (ID: " + userId + ")"));
        return borrowingRecordRepository.findByUserOrderByBorrowDateDesc(user);
    }

    @Override
    public List<BorrowingRecord> findOverdueBooks() {
        // 实际应用中可能需要分页
        return borrowingRecordRepository.findByStatusAndDueDateBefore(BorrowStatus.BORROWED, LocalDate.now());
    }

    // --- 辅助方法或未来扩展 ---

    // private boolean isBookReservedByOthers(Book book, User currentUser) {
    //     return borrowingRecordRepository.existsByBookAndStatusAndUserNot(book, BorrowStatus.RESERVED, currentUser);
    // }

    // private void checkAndNotifyNextReservation(Book returnedBook) {
    //     Optional<BorrowingRecord> nextReservation = borrowingRecordRepository
    //         .findByBookAndStatusOrderByReservationDateAsc(returnedBook, BorrowStatus.RESERVED)
    //         .stream().findFirst(); // 获取最早的预约
    //
    //     if (nextReservation.isPresent()) {
    //         BorrowingRecord reservation = nextReservation.get();
    //         // 更新预约的有效截止日期
    //         reservation.setReservationExpiryDate(LocalDate.now().plusDays(RESERVATION_VALID_DAYS));
    //         borrowingRecordRepository.save(reservation);
    //         // 发送通知给预约用户
    //         // notificationService.sendReservationAvailableNotification(reservation.getUser(), returnedBook);
    //         log.info("Notified user '{}' about reservation for book '{}' being available.",
    //                  reservation.getUser().getUsername(), returnedBook.getTitle());
    //     }
    // }
}