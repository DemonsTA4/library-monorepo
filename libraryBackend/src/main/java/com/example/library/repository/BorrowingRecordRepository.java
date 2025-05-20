package com.example.library.repository; // 确保包名与你的项目结构一致

import com.example.library.entity.impl.Book; // 你的 Book 实体
import com.example.library.entity.impl.BorrowStatus;
import com.example.library.entity.impl.BorrowingRecord; // 你的 BorrowingRecord 实体
import com.example.library.entity.impl.User;     // 你的 User 实体
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository // 声明这是一个 Spring管理的 Repository Bean
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    // Spring Data JPA 会根据方法名自动生成查询实现

    // 1. 根据用户和状态查询借阅记录 (例如：查询某用户所有已借阅的书)
    List<BorrowingRecord> findByUserAndStatus(User user, BorrowStatus status);

    // 2. 根据用户和多种状态查询借阅记录
    List<BorrowingRecord> findByUserAndStatusIn(User user, List<BorrowStatus> statuses);

    // 3. 根据图书和状态查询借阅记录 (例如：查询某本书所有预约中的记录)
    List<BorrowingRecord> findByBookAndStatus(Book book, BorrowStatus status);

    // 4. 根据图书和多种状态查询
    List<BorrowingRecord> findByBookAndStatusIn(Book book, List<BorrowStatus> statuses);

    // 5. 查询特定用户对特定图书的特定状态的记录 (例如：检查用户是否已借阅某本书且未归还)
    Optional<BorrowingRecord> findByUserAndBookAndStatus(User user, Book book, BorrowStatus status);

    // 6. 检查特定用户对特定图书是否存在某些状态的记录
    boolean existsByUserAndBookAndStatusIn(User user, Book book, List<BorrowStatus> statuses);

    // 7. 查询用户的所有借阅记录 (按借阅日期降序排序)
    List<BorrowingRecord> findByUserOrderByBorrowDateDesc(User user);

    // 8. 查询所有超期的借阅记录 (状态为 BORROWED 且应还日期在当前日期之前)
    List<BorrowingRecord> findByStatusAndDueDateBefore(BorrowStatus status, LocalDate currentDate);

    // 9. 统计用户当前处于某些状态的借阅数量 (例如：统计用户当前已借阅和已逾期的书籍总数)
    long countByUserAndStatusIn(User user, List<BorrowStatus> statuses);

    // 10. 查询特定状态且应还日期在指定日期或之前的记录 (例如：查询 N 天内即将到期的书)
    List<BorrowingRecord> findByStatusAndDueDateLessThanEqual(BorrowStatus status, LocalDate date);

    // 11. 根据图书查询所有预约记录并按预约日期升序排序 (用于处理预约队列)
    List<BorrowingRecord> findByBookAndStatusOrderByReservationDateAsc(Book book, BorrowStatus status);


    // 如果你需要更复杂的查询，可以使用 @Query 注解配合 JPQL 或 Native SQL

    // 示例：使用 JPQL 查询热门图书 (按借阅次数排序)
    // 注意：这个查询返回的是 Object[], 你需要在 Service 层处理它
    // 第一个元素是 Book 对象，第二个元素是借阅次数 (Long)
    @Query("SELECT br.book, COUNT(br.id) as borrowCount " +
            "FROM BorrowingRecord br " +
            "WHERE br.status = :statusBorrowed OR br.status = :statusReturned " + // 可以根据需要定义哪些状态算作一次有效借阅
            "GROUP BY br.book " +
            "ORDER BY borrowCount DESC")
    List<Object[]> findTopBorrowedBooks(
            @Param("statusBorrowed") BorrowStatus statusBorrowed,
            @Param("statusReturned") BorrowStatus statusReturned
            // 可以添加 Pageable pageable 来限制返回数量
    );

    // 示例：查询用户是否有未支付的罚款 (假设 BorrowingRecord 中有 fineAmount 和 finePaid 字段)
    // @Query("SELECT CASE WHEN COUNT(br) > 0 THEN TRUE ELSE FALSE END " +
    //        "FROM BorrowingRecord br " +
    //        "WHERE br.user = :user AND br.fineAmount > 0 AND br.finePaid = false")
    // boolean hasUnpaidFines(@Param("user") User user);

}