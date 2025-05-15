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
        <p><strong>库存数量:</strong> {{ book.quantity }}</p>
        </div>
      <div class="actions" v-if="isAdmin">
        <router-link :to="`/books/edit/${book.id}`" class="button is-info">编辑</router-link>
        <button @click="confirmDeleteBook" class="button is-danger">删除</button>
      </div>
    </div>
    <div v-if="!book && !isLoading && !error">
      <p>未找到指定的图书。</p>
      <router-link to="/books">返回图书列表</router-link>
    </div>
  </div>
</template>

<script>
import bookService from '@/services/bookService';
// import authService from '@/services/authService';

export default {
  name: 'BookDetailView',
  props: {
    id: { // 从路由参数接收 id
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      book: null,
      isLoading: false,
      error: null,
      isAdmin: false,
    };
  },
  created() {
    this.fetchBookDetails();
    this.checkUserRole();
  },
  methods: {
    async fetchBookDetails() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await bookService.getBookById(this.id);
        this.book = response.data;
      } catch (err) {
        this.error = '加载图书详情失败: ' + (err.response?.data?.message || err.message);
        console.error(err);
        if (err.response && err.response.status === 404) {
            this.book = null; // 确保如果404，book对象是null
        }
      } finally {
        this.isLoading = false;
      }
    },
    checkUserRole() {
      const roles = JSON.parse(localStorage.getItem('userRoles')) || [];
      this.isAdmin = roles.includes('ROLE_ADMIN');
    },
    async confirmDeleteBook() {
      if (this.book && window.confirm(`确定要删除《${this.book.title}》吗？`)) {
        try {
          await bookService.deleteBook(this.book.id);
          alert('图书删除成功！');
          this.$router.push('/books');
        } catch (err) {
          alert('删除图书失败: ' + (err.response?.data?.message || err.message));
          console.error(err);
        }
      }
    },
  },
};
</script>

<style scoped>
.book-detail-view {
  padding: 20px;
  max-width: 800px;
  margin: auto;
}
.loading, .error-message {
  text-align: center;
  font-style: italic;
  margin-top: 20px;
}
.error-message {
  color: red;
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
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); /* 响应式列 */
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
.cover-image-container {
  margin-top: 20px;
  text-align: center;
}
.cover-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.actions {
  margin-top: 20px;
  text-align: right;
}
.actions .button {
  margin-left: 10px;
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
.button.is-info {
  background-color: #209cee;
  color: white;
}
.button.is-danger {
  background-color: #ff3860;
  color: white;
}
</style>