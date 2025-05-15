<template>
  <div class="edit-book-view">
    <h1>编辑图书信息</h1>
    <div v-if="isLoading" class="loading">正在加载图书数据...</div>
    <div v-if="fetchError" class="error-message">{{ fetchError }}</div>

    <BookForm
      v-if="bookToEdit && !isLoading"
      :initial-book-data="bookToEdit"
      :is-submitting="isSubmitting"
      @submit-form="handleUpdateBook"
      is-edit-mode />
    <p v-if="submitError" class="error-message submit-error">{{ submitError }}</p>
    <router-link v-if="!isLoading" to="/books" class="back-button button is-light">返回图书列表</router-link>
  </div>
</template>

<script>
import bookService from '@/services/bookService';
import BookForm from '@/components/BookForm.vue';

export default {
  name: 'EditBookView',
  components: {
    BookForm,
  },
  props: {
    id: { // 从路由参数接收 id
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      bookToEdit: null,
      isLoading: false,
      isSubmitting: false,
      fetchError: null,
      submitError: null,
    };
  },
  async created() {
    await this.fetchBook();
  },
  methods: {
    async fetchBook() {
      this.isLoading = true;
      this.fetchError = null;
      try {
        const response = await bookService.getBookById(this.id);
        this.bookToEdit = response.data;
      } catch (err) {
        this.fetchError = '加载图书信息失败: ' + (err.response?.data?.message || err.message);
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    async handleUpdateBook(bookData) {
      this.isSubmitting = true;
      this.submitError = null;
      try {
        // 从 bookData 中移除 id，因为 id 通常在 URL 中传递，而不是在请求体中用于更新
        const { id, ...dataToUpdate } = bookData;
        console.log(id); // 确保 id 不在 dataToUpdate 中 (虽然我们的 BookForm 应该不会包含它)
        await bookService.updateBook(this.id, dataToUpdate);
        alert('图书信息更新成功！');
        this.$router.push('/books'); // 或 this.$router.push(`/books/${this.id}`) 跳转到详情页
      } catch (err) {
        this.submitError = '更新图书信息失败: ' + (err.response?.data?.message || err.message);
        console.error(err);
      } finally {
        this.isSubmitting = false;
      }
    },
  },
};
</script>

<style scoped>
.edit-book-view {
  padding: 20px;
  max-width: 600px;
  margin: auto;
}
.edit-book-view h1 {
  text-align: center;
  margin-bottom: 20px;
}
.loading, .error-message {
  text-align: center;
  font-style: italic;
  margin-bottom: 15px;
}
.error-message {
  color: red;
}
.submit-error {
    margin-top: 10px;
}
.back-button {
    margin-top: 20px;
    display: inline-block;
}
/* 使用一些 Bulma 或自定义的按钮样式 */
.button.is-light {
  background-color: #f5f5f5;
  border-color: #dbdbdb;
  color: #363636;
}
.button.is-light:hover {
  background-color: #e8e8e8;
}
</style>