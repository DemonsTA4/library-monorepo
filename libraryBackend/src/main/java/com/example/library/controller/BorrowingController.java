package com.example.library.controller;

import com.example.library.dto.BorrowRequestDto;
import com.example.library.dto.BorrowingRecordDto;
import com.example.library.dto.ReservationRequestDto;
import com.example.library.entity.impl.User; // 假设 User 实体类路径
import com.example.library.entity.impl.BorrowingRecord; // 你的 BorrowingRecord 实体
import com.example.library.repository.UserRepository; // 用于从 Principal 获取 User 对象
import com.example.library.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/borrowings") // 借阅相关操作的基础路径
@Tag(name = "Borrowing Management", description = "用于图书借阅、归还、续借和预约的API")
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final UserRepository userRepository; // 用于从认证信息中获取用户对象

    @Autowired
    public BorrowingController(BorrowingService borrowingService, UserRepository userRepository) {
        this.borrowingService = borrowingService;
        this.userRepository = userRepository;
    }

    // --- 辅助方法 ---

    /**
     * 将 BorrowingRecord 实体转换为 BorrowingRecordDto。
     * @param record 借阅记录实体
     * @return 借阅记录DTO
     */
    private BorrowingRecordDto convertToDto(BorrowingRecord record) {
        if (record == null) {
            return null;
        }
        return new BorrowingRecordDto(
                record.getId(),
                record.getUser().getId(),
                record.getUser().getUsername(),
                record.getBook().getId(),
                record.getBook().getTitle(),
                record.getBorrowDate(),
                record.getDueDate(),
                record.getReturnDate(),
                record.getStatus() != null ? record.getStatus().name() : null, // 或者使用 getDisplayName()
                record.getReservationDate(),
                record.getReservationExpiryDate()
                // 如果 BorrowingRecordDto 中添加了罚款信息，在这里映射
                // record.getFineAmount(),
                // record.isFinePaid()
        );
    }

    /**
     * 获取当前认证用户的ID。
     * @return 当前用户ID
     * @throws ResponseStatusException 如果用户未认证或在数据库中找不到
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未认证");
        }
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("认证用户 '" + username + "' 在数据库中未找到"));
        return currentUser.getId();
    }

    // --- API 端点 ---

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    @Operation(summary = "借阅一本图书", description = "允许认证用户借阅一本可用的图书。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "图书借阅成功", content = @Content(schema = @Schema(implementation = BorrowingRecordDto.class))),
            @ApiResponse(responseCode = "400", description = "无效请求（例如，用户或图书不存在，无库存，达到借阅上限）"),
            @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<BorrowingRecordDto> borrowBook(@Valid @RequestBody BorrowRequestDto borrowRequest) {
        // 安全起见，userId 应该从当前的认证主体获取，而不是从请求体中获取。
        // 如果 borrowRequest 中的 userId 与当前登录用户不符，应拒绝或仅使用当前登录用户ID。
        Long currentUserId = getCurrentUserId();
        // 如果你的 BorrowRequestDto 仍然包含 userId，你可以选择验证它或忽略它，强制使用 currentUserId
        // if (!currentUserId.equals(borrowRequest.getUserId())) {
        //     throw new ResponseStatusException(HttpStatus.FORBIDDEN, "用户只能为自己借阅图书。");
        // }

        try {
            BorrowingRecord record = borrowingService.borrowBook(currentUserId, borrowRequest.getBookId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(record));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{recordId}/return")
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    @Operation(summary = "归还一本已借阅的图书", description = "将一本已借阅的图书标记为已归还，并可能计算罚款。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "图书归还成功", content = @Content(schema = @Schema(implementation = BorrowingRecordDto.class))),
            @ApiResponse(responseCode = "400", description = "无效请求（例如，借阅记录不存在或图书已归还）"),
            @ApiResponse(responseCode = "401", description = "用户未认证"),
            @ApiResponse(responseCode = "403", description = "用户无权操作此借阅记录")
    })
    public ResponseEntity<BorrowingRecordDto> returnBook(
            @Parameter(description = "要归还的借阅记录ID") @PathVariable Long recordId) {
        // 可以在 Service 层或此处添加逻辑，确保只有借阅者本人或图书管理员/管理员才能归还
        try {
            BorrowingRecord record = borrowingService.returnBook(recordId);
            return ResponseEntity.ok(convertToDto(record));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @PutMapping("/{recordId}/renew")
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    @Operation(summary = "续借一本已借阅的图书", description = "如果符合条件，延长图书的应还日期。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "图书续借成功", content = @Content(schema = @Schema(implementation = BorrowingRecordDto.class))),
            @ApiResponse(responseCode = "400", description = "无效请求（例如，不符合续借条件）"),
            @ApiResponse(responseCode = "401", description = "用户未认证"),
            @ApiResponse(responseCode = "403", description = "用户无权操作此借阅记录")
    })
    public ResponseEntity<BorrowingRecordDto> renewBook(
            @Parameter(description = "要续借的借阅记录ID") @PathVariable Long recordId) {
        // 类似的权限检查逻辑
        try {
            BorrowingRecord record = borrowingService.renewBook(recordId);
            return ResponseEntity.ok(convertToDto(record));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping("/history/me")
    @PreAuthorize("isAuthenticated()") // 任何已认证用户都可以查看自己的历史
    @Operation(summary = "获取当前用户的借阅历史", description = "检索当前认证用户的所有借阅记录。")
    public ResponseEntity<List<BorrowingRecordDto>> getCurrentUserBorrowingHistory() {
        Long userId = getCurrentUserId();
        List<BorrowingRecord> records = borrowingService.getUserBorrowingHistory(userId);
        List<BorrowingRecordDto> dtos = records.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/history/user/{userId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "获取指定用户的借阅历史 (图书管理员/管理员)", description = "检索指定用户的所有借阅记录。")
    public ResponseEntity<List<BorrowingRecordDto>> getUserBorrowingHistoryForAdmin(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<BorrowingRecord> records = borrowingService.getUserBorrowingHistory(userId);
        List<BorrowingRecordDto> dtos = records.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "获取所有逾期图书 (图书管理员/管理员)", description = "检索所有当前状态为逾期的借阅记录。")
    public ResponseEntity<List<BorrowingRecordDto>> getOverdueBooks() {
        List<BorrowingRecord> records = borrowingService.findOverdueBooks();
        List<BorrowingRecordDto> dtos = records.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // --- 预约相关端点 ---
    // 你也可以将这些放到一个专门的 ReservationController 中

    @PostMapping("/reservations")
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN', 'ADMIN')")
    @Operation(summary = "预约一本图书", description = "允许用户预约当前不可借的图书。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "图书预约成功", content = @Content(schema = @Schema(implementation = BorrowingRecordDto.class))),
            @ApiResponse(responseCode = "400", description = "无效请求（例如，图书不存在或不符合预约条件）"),
            @ApiResponse(responseCode = "401", description = "用户未认证")
    })
    public ResponseEntity<BorrowingRecordDto> reserveBook(@Valid @RequestBody ReservationRequestDto reservationRequest) {
        Long currentUserId = getCurrentUserId();
        // if (!currentUserId.equals(reservationRequest.getUserId())) {
        //     throw new ResponseStatusException(HttpStatus.FORBIDDEN, "用户只能为自己预约图书。");
        // }
        try {
            BorrowingRecord record = borrowingService.reserveBook(currentUserId, reservationRequest.getBookId());
            // 注意：这里返回的是 BorrowingRecordDto，它包含了预约信息，status 应该是 RESERVED
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(record));
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/reservations/{reservationId}")
    @PreAuthorize("isAuthenticated()") // 允许用户取消自己的预约，管理员也可以取消
    @Operation(summary = "取消图书预约", description = "允许用户取消他们自己的图书预约，或管理员取消任何预约。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "预约取消成功"),
            @ApiResponse(responseCode = "400", description = "无效的预约记录ID"),
            @ApiResponse(responseCode = "401", description = "用户未认证"),
            @ApiResponse(responseCode = "403", description = "用户无权取消此预约")
    })
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "要取消的预约记录ID") @PathVariable Long reservationId) {
        Long currentUserId = getCurrentUserId();
        try {
            // BorrowingService 的 cancelReservation 方法内部应该处理权限逻辑
            // (例如，检查是否是用户自己的预约，或者当前用户是否是管理员)
            borrowingService.cancelReservation(currentUserId, reservationId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) { // 通常表示记录未找到或状态不正确
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) { // Service 层抛出的无权限异常
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
