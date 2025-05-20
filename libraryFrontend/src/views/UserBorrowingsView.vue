<template>
  <div class="my-borrowings-view">
    <h1>我的借阅与预约</h1>

    <div v-if="isLoading" class="loading">加载中...</div>
    <div v-if="error" class="error-message">{{ error }}</div>

    <div v-if="!isLoading && !error">
      <section v-if="currentBorrowings.length > 0">
        <h2>当前借阅</h2>
        <ul>
          <li v-for="record in currentBorrowings" :key="record.id" class="borrow-item">
            <strong>《{{ record.bookTitle }}》</strong> - 应还日期: {{ formatDate(record.dueDate) }}
            (状态: {{ record.status }})
            <button @click="handleReturn(record.id)" :disabled="isProcessing === record.id" class="button is-small">
              {{ isProcessing === record.id && actionType === 'return' ? '处理中...' : '归还' }}
            </button>
            <button @click="handleRenew(record.id)" :disabled="isProcessing === record.id" class="button is-small is-info">
              {{ isProcessing === record.id && actionType === 'renew' ? '处理中...' : '续借' }}
            </button>
          </li>
        </ul>
      </section>
      <p v-else>您当前没有借阅中的图书。</p>

      <section v-if="currentReservations.length > 0" style="margin-top: 20px;">
        <h2>我的预约</h2>
        <ul>
          <li v-for="record in currentReservations" :key="record.id" class="borrow-item">
            <strong>《{{ record.bookTitle }}》</strong> - 预约于: {{ formatDateTime(record.reservationDate) }}
            (有效期至: {{ formatDate(record.reservationExpiryDate) }})
            <button @click="handleCancelReservation(record.id)" :disabled="isProcessing === record.id" class="button is-small is-warning">
              {{ isProcessing === record.id && actionType === 'cancel' ? '处理中...' : '取消预约' }}
            </button>
          </li>
        </ul>
      </section>
      <p v-else>您当前没有有效的预约。</p>

      <section v-if="borrowingHistory.length > 0" style="margin-top: 20px;">
        <h2>借阅历史</h2>
        <ul>
          <li v-for="record in borrowingHistory" :key="record.id" class="borrow-item history-item">
            <strong>《{{ record.bookTitle }}》</strong> - 借于: {{ formatDateTime(record.borrowDate) }}
            <span v-if="record.returnDate">, 归还于: {{ formatDateTime(record.returnDate) }}</span>
            (状态: {{ record.status }})
          </li>
        </ul>
      </section>
       <p v-else>您还没有借阅历史。</p>
    </div>
     <p v-if="actionMessage" :class="actionMessageType === 'success' ? 'success-message' : 'error-message'">
        {{ actionMessage }}
    </p>
  </div>
</template>

<script>
import borrowingService from '@/services/borrowingService';
import authService from '@/services/authService';

export default {
  name: 'MyBorrowingsView',
  data() {
    return {
      allRecords: [],
      isLoading: false,
      error: null,
      isProcessing: null, // 用于跟踪当前正在处理的记录ID
      actionType: '', // 'return', 'renew', 'cancel'
      actionMessage: '',
      actionMessageType: '', // 'success' or 'error'
    };
  },
  computed: {
    currentBorrowings() {
      return this.allRecords.filter(r => r.status === 'BORROWED' || r.status === 'OVERDUE');
    },
    currentReservations() {
      return this.allRecords.filter(r => r.status === 'RESERVED');
    },
    borrowingHistory() {
      return this.allRecords.filter(r => r.status === 'RETURNED' || r.status === 'RESERVATION_CANCELED' || r.status === 'RESERVATION_EXPIRED');
    }
  },
  async created() {
    if (authService.isAuthenticated()) {
      await this.fetchMyHistory();
    } else {
      this.error = "请先登录查看借阅信息。";
      // 也可以跳转到登录页 this.$router.push('/login');
    }
  },
  methods: {
    async fetchMyHistory() {
      this.isLoading = true;
      this.error = null;
      this.actionMessage = '';
      try {
        const response = await borrowingService.getMyBorrowingHistory();
        this.allRecords = response.data;
      } catch (err) {
        this.error = '加载借阅历史失败: ' + (err.response?.data?.message || err.message);
      } finally {
        this.isLoading = false;
      }
    },
    async handleReturn(recordId) {
      this.isProcessing = recordId;
      this.actionType = 'return';
      this.actionMessage = '';
      try {
        await borrowingService.returnBook(recordId);
        this.actionMessageType = 'success';
        this.actionMessage = '归还成功！';
        await this.fetchMyHistory(); // 刷新列表
      } catch (err) {
        this.actionMessageType = 'error';
        this.actionMessage = '归还失败: ' + (err.response?.data?.message || err.message);
      } finally {
        this.isProcessing = null;
        this.actionType = '';
      }
    },
    async handleRenew(recordId) {
      this.isProcessing = recordId;
      this.actionType = 'renew';
      this.actionMessage = '';
      try {
        await borrowingService.renewBook(recordId);
        this.actionMessageType = 'success';
        this.actionMessage = '续借成功！';
        await this.fetchMyHistory();
      } catch (err) {
         this.actionMessageType = 'error';
        this.actionMessage = '续借失败: ' + (err.response?.data?.message || err.message);
      } finally {
        this.isProcessing = null;
        this.actionType = '';
      }
    },
    async handleCancelReservation(reservationId) {
      this.isProcessing = reservationId;
      this.actionType = 'cancel';
      this.actionMessage = '';
      try {
        await borrowingService.cancelReservation(reservationId);
        this.actionMessageType = 'success';
        this.actionMessage = '预约已取消！';
        await this.fetchMyHistory();
      } catch (err) {
        this.actionMessageType = 'error';
        this.actionMessage = '取消预约失败: ' + (err.response?.data?.message || err.message);
      } finally {
        this.isProcessing = null;
        this.actionType = '';
      }
    },
    formatDate(dateString) {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleDateString();
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      return new Date(dateTimeString).toLocaleString();
    }
  }
};
</script>

<style scoped>
.my-borrowings-view { padding: 20px; }
.borrow-item {
  border: 1px solid #eee;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
  background-color: #f9f9f9;
}
.history-item { background-color: #e9e9e9; }
.borrow-item strong { font-size: 1.1em; }
.borrow-item button { margin-left: 10px; }
.loading, .error-message, .success-message { margin-top: 15px; font-style: italic; }
.error-message { color: red; }
.success-message { color: green; }
h2 { font-size: 1.5em; margin-top: 20px; margin-bottom: 10px; border-bottom: 1px solid #ccc; padding-bottom: 5px;}
ul { list-style-type: none; padding: 0; }
/* 简单的按钮样式，你可以使用 UI 库或自定义 */
.button.is-small { font-size: 0.8rem; padding: 4px 8px; }
.button.is-info { background-color: #209cee; color: white; border:none;}
.button.is-warning { background-color: #ffdd57; color: rgba(0,0,0,0.7); border:none;}
</style>