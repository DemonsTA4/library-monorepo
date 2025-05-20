<template>
  <div class="book-detail-view">
    <div v-if="isLoading" class="loading">正在加载图书详情...</div>
    <div v-if="error" class="error-message">{{ error }}</div>
    <div v-if="book && !isLoading && !error" class="book-details-container">
      <router-link to="/books" class="back-link">&lt; 返回图书列表</router-link>
      <h1>{{ book.title }}</h1>
      <div class="details-grid">
        <p><strong>作者:</strong> {{ book.author }}</p>
        <p><strong>ISBN:</strong> {{ book.isbn }}</p>
        <p><strong>出版社:</strong> {{ book.publisher || 'N/A' }}</p>
        <p><strong>价格:</strong> ¥{{ book.price ? book.price.toFixed(2) : 'N/A' }}</p>
        <p><strong>库存数量:</strong> <span :class="{'low-stock': book.quantity <= 0}">{{ book.quantity }}</span></p>
      </div>

      <div class="user-actions section-spacing">
        <button
          v-if="isAuthenticated && book.quantity > 0 && canBorrowOrReserve"
          @click="handleBorrowBook"
          :disabled="isProcessingAction"
          class="button is-success">
          {{ isProcessingAction && currentAction === 'borrow' ? '借阅中...' : '借阅此书' }}
        </button>
        <button
          v-if="isAuthenticated && book.quantity <= 0 && canBorrowOrReserve"
          @click="handleReserveBook"
          :disabled="isProcessingAction"
          class="button is-warning">
          {{ isProcessingAction && currentAction === 'reserve' ? '预约中...' : '预约此书' }}
        </button>
        <p v-if="!isAuthenticated" class="info-message">请 <router-link to="/login">登录</router-link> 后进行借阅或预约。</p>
        <p v-if="actionError" class="error-message">{{ actionError }}</p>
        <p v-if="actionSuccess" class="success-message">{{ actionSuccess }}</p>
      </div>

      <div class="actions admin-actions section-spacing" v-if="isAdmin">
        <router-link :to="`/books/edit/${book.id}`" class="button is-info">编辑</router-link>
        <button @click="confirmDeleteBook" class="button is-danger" :disabled="isProcessingAction">删除</button>
      </div>
    </div>
    <div v-if="!book && !isLoading && !error" class="not-found-message">
      <p>未找到指定的图书。</p>
      <router-link to="/books">返回图书列表</router-link>
    </div>
  </div>
</template>

<script>
import bookService from '@/services/bookService';
import borrowingService from '@/services/borrowingService'; // 引入借阅服务
import authService from '@/services/authService';       // 引入认证服务

export default {
  name: 'BookDetailView',
  props: {
    id: {
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      book: null,
      isLoading: false, // 页面整体加载状态
      error: null,      // 获取图书详情的错误
      isAdmin: false,
      isAuthenticated: false,
      isProcessingAction: false, // 用于借阅/预约按钮的加载状态
      currentAction: '', // 'borrow' or 'reserve'
      actionError: null,     // 借阅/预约操作的错误信息
      actionSuccess: null,   // 借阅/预约操作的成功信息
      canBorrowOrReserve: true, // 用于控制是否已借阅或预约（避免重复操作）
    };
  },
  async created() {
    this.isAuthenticated = authService.isAuthenticated();
    this.checkUserRole(); // 获取管理员状态
    await this.fetchBookDetails();
    if (this.isAuthenticated && this.book) {
      // 可选：如果需要，可以检查用户是否已借阅或预约了此书，以禁用按钮
      // await this.checkUserBookStatus();
    }
  },
  methods: {
    async fetchBookDetails() {
      this.isLoading = true;
      this.error = null;
      this.actionError = null; // 清除之前的操作错误
      this.actionSuccess = null;
      try {
        const response = await bookService.getBookById(this.id);
        this.book = response.data;
      } catch (err) {
        this.error = '加载图书详情失败: ' + (err.response?.data?.message || err.message);
        console.error("Fetch book detail error:", err.response || err);
        if (err.response && err.response.status === 404) {
          this.book = null;
        }
      } finally {
        this.isLoading = false;
      }
    },
    checkUserRole() {
      // 从 authService 获取角色信息更可靠
      this.isAdmin = authService.hasRole('ROLE_ADMIN');
    },
    // 可选：检查用户是否已借阅或预约此书
    // async checkUserBookStatus() {
    //   try {
    //     // 你需要后端API来支持这个查询，例如 /api/v1/borrowings/user/book-status?bookId=...
    //     // 或者在 getMyBorrowingHistory 返回的数据中查找
    //     const myBorrowings = await borrowingService.getMyBorrowingHistory();
    //     const existingRecord = myBorrowings.data.find(
    //       record => record.bookId === this.book.id && (record.status === 'BORROWED' || record.status === 'OVERDUE' || record.status === 'RESERVED')
    //     );
    //     if (existingRecord) {
    //       this.canBorrowOrReserve = false;
    //       if (existingRecord.status === 'RESERVED') {
    //         this.actionError = "您已预约此书。";
    //       } else {
    //         this.actionError = "您已借阅此书且未归还。";
    //       }
    //     }
    //   } catch (error) {
    //     console.error("Error checking user book status:", error);
    //   }
    // },
    async handleBorrowBook() {
      if (!this.book || this.isProcessingAction) return;
      this.isProcessingAction = true;
      this.currentAction = 'borrow';
      this.actionError = null;
      this.actionSuccess = null;
      try {
        // 后端应从token中获取userId，前端只需传递bookId
        const response = await borrowingService.borrowBook({ bookId: this.book.id });
        this.actionSuccess = `《${this.book.title}》借阅成功！应还日期: ${new Date(response.data.dueDate).toLocaleDateString()}`;
        // 更新图书库存信息
        await this.fetchBookDetails();
        this.canBorrowOrReserve = false; // 标记为已操作，避免重复
      } catch (err) {
        this.actionError = '借阅失败: ' + (err.response?.data?.message || err.response?.data || err.message);
        console.error("Borrow error:", err.response || err);
      } finally {
        this.isProcessingAction = false;
        this.currentAction = '';
      }
    },
    async handleReserveBook() {
      if (!this.book || this.isProcessingAction) return;
      this.isProcessingAction = true;
      this.currentAction = 'reserve';
      this.actionError = null;
      this.actionSuccess = null;
      try {
        await borrowingService.reserveBook({ bookId: this.book.id });
        this.actionSuccess = `《${this.book.title}》预约成功！`;
        // 预约成功后，你可能想给用户一些提示或更新界面状态
        this.canBorrowOrReserve = false; // 标记为已操作，避免重复
      } catch (err) {
        this.actionError = '预约失败: ' + (err.response?.data?.message || err.response?.data || err.message);
        console.error("Reserve error:", err.response || err);
      } finally {
        this.isProcessingAction = false;
        this.currentAction = '';
      }
    },
    async confirmDeleteBook() {
      if (this.book && window.confirm(`确定要删除《${this.book.title}》吗？`)) {
        this.isProcessingAction = true; // 防止在删除时进行其他操作
        try {
          await bookService.deleteBook(this.book.id);
          alert('图书删除成功！');
          this.$router.push('/books');
        } catch (err) {
          alert('删除图书失败: ' + (err.response?.data?.message || err.message));
          console.error("Delete book error:", err);
        } finally {
          this.isProcessingAction = false;
        }
      }
    },
  },
};
</script>

<style scoped>
/* 你已有的样式 */
.book-detail-view {
  padding: 20px;
  max-width: 800px;
  margin: auto;
}
.loading, .error-message, .success-message, .info-message {
  text-align: center;
  font-style: italic;
  margin-top: 15px;
  padding: 10px;
  border-radius: 4px;
}
.error-message {
  color: #e74c3c;
  background-color: #fdd;
  border: 1px solid #e74c3c;
}
.success-message {
  color: #27ae60;
  background-color: #e6ffed;
  border: 1px solid #27ae60;
}
.info-message {
    color: #3273dc;
    background-color: #eef5fb;
    border: 1px solid #3273dc;
}
.book-details-container {
  background: #fff;
  padding: 25px;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.book-details-container h1 {
  font-size: 2em;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 25px;
}
.details-grid p {
  background-color: #f9f9f9;
  padding: 10px;
  border-radius: 4px;
  font-size: 1em;
}
.details-grid p strong {
  color: #555;
}
.low-stock {
    color: #e74c3c;
    font-weight: bold;
}
.user-actions, .admin-actions {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.user-actions {
    text-align: left; /* 让借阅预约按钮靠左 */
}
.admin-actions {
  text-align: right;
}
.actions .button, .user-actions .button {
  margin-left: 10px;
}
.user-actions .button:first-child {
    margin-left: 0;
}
.back-link {
  display: inline-block;
  margin-bottom: 20px;
  color: #3273dc;
  text-decoration: none;
}
.back-link:hover {
  text-decoration: underline;
}
.button.is-info { background-color: #209cee; color: white; border:none; }
.button.is-danger { background-color: #ff3860; color: white; border:none; }
.button.is-success { background-color: #23d160; color: white; border:none; }
.button.is-warning { background-color: #ffdd57; color: rgba(0,0,0,0.7); border:none; }
.button:disabled { opacity: 0.7; cursor: not-allowed; }
.not-found-message {
    text-align: center;
    padding: 20px;
}
.section-spacing { /* 用于分隔不同区域 */
    margin-top: 30px;
}
</style>