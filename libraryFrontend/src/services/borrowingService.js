import apiClient from './api'; // 你配置好的 Axios 实例

export default {
  /**
   * 用户借阅图书
   * @param {Object} borrowData - 包含 bookId (userId 应由后端从token获取)
   * @returns {Promise<Object>} 后端返回的 BorrowingRecordDto
   */
  borrowBook(borrowData) {
    // 后端 BorrowingController 的 @PostMapping
    // public ResponseEntity<BorrowingRecordDto> borrowBook(@Valid @RequestBody BorrowRequestDto borrowRequest)
    // borrowRequest DTO 需要 bookId。userId 将由后端从认证信息获取。
    // 所以前端只需要传递 bookId。
    // 如果你的 BorrowRequestDto 仍然设计为需要 userId，那么前端需要传递。
    // 但最佳实践是后端从认证信息中获取 userId。
    // 假设你的 BorrowRequestDto 只需要 bookId，或者也接受 userId (但后端会优先用认证用户)
    return apiClient.post('/borrowings', borrowData); // borrowData 应该是 { bookId: xxx }
  },

  /**
   * 用户归还图书
   * @param {number|string} recordId - 借阅记录ID
   * @returns {Promise<Object>} 后端返回的 BorrowingRecordDto
   */
  returnBook(recordId) {
    return apiClient.put(`/borrowings/${recordId}/return`);
  },

  /**
   * 用户续借图书
   * @param {number|string} recordId - 借阅记录ID
   * @returns {Promise<Object>} 后端返回的 BorrowingRecordDto
   */
  renewBook(recordId) {
    return apiClient.put(`/borrowings/${recordId}/renew`);
  },

  /**
   * 用户预约图书
   * @param {Object} reservationData - 包含 bookId
   * @returns {Promise<Object>} 后端返回的 BorrowingRecordDto (状态为 RESERVED)
   */
  reserveBook(reservationData) {
    // 同样，userId 最好由后端从认证信息中获取
    return apiClient.post('/borrowings/reservations', reservationData); // reservationData 应该是 { bookId: xxx }
  },

  /**
   * 用户取消预约
   * @param {number|string} reservationId - 预约记录ID
   * @returns {Promise}
   */
  cancelReservation(reservationId) {
    // 后端 cancelReservation 需要 userId 进行权限验证，但这里只传 reservationId
    // 后端会从认证信息中获取当前用户ID进行比较
    return apiClient.delete(`/borrowings/reservations/${reservationId}`);
  },

  /**
   * 获取当前认证用户的借阅历史
   * @returns {Promise<Array<Object>>} BorrowingRecordDto 列表
   */
  getMyBorrowingHistory() {
    return apiClient.get('/borrowings/history/me');
  },

  /**
   * (管理员/图书管理员) 获取所有逾期图书记录
   * @returns {Promise<Array<Object>>} BorrowingRecordDto 列表
   */
  getOverdueBooks() {
    // 确保此API端点和权限在后端已配置
    return apiClient.get('/borrowings/overdue');
  },

  /**
   * (管理员/图书管理员) 获取指定用户的借阅历史
   * @param {number|string} userId
   * @returns {Promise<Array<Object>>} BorrowingRecordDto 列表
   */
  getUserBorrowingHistoryForAdmin(userId) {
    return apiClient.get(`/borrowings/history/user/${userId}`);
  }
};