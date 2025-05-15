<template>
  <div class="add-book-view">
    <h1>添加新书</h1>
    <BookForm @submit-form="handleCreateBook" :is-submitting="isSubmitting" />
    <p v-if="error" class="error-message">{{ error }}</p>
  </div>
</template>

<script>
import bookService from '@/services/bookService';
import BookForm from '@/components/BookForm.vue'; // 假设你有一个 BookForm 组件

export default {
  name: 'AddBookView',
  components: {
    BookForm,
  },
  data() {
    return {
      isSubmitting: false,
      error: null,
    };
  },
  methods: {
    async handleCreateBook(bookData) {
      this.isSubmitting = true;
      this.error = null;
      try {
        await bookService.createBook(bookData);
        alert('图书添加成功！');
        this.$router.push('/books'); // 跳转回图书列表页
      } catch (err) {
        this.error = '添加图书失败: ' + (err.response?.data?.message || err.message);
        console.error(err);
      } finally {
        this.isSubmitting = false;
      }
    },
  },
};
</script>

<style scoped>
.add-book-view {
  padding: 20px;
  max-width: 600px;
  margin: auto;
}
.error-message {
  color: red;
  margin-top: 10px;
}
</style>