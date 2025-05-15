<template>
  <div class="books-view">
    <h1>图书馆藏</h1>
    <div class="search-bar">
      <input type="text" v-model="searchQuery.title" placeholder="按书名搜索..." @keyup.enter="fetchBooks"/>
      <input type="text" v-model="searchQuery.author" placeholder="按作者搜索..." @keyup.enter="fetchBooks"/>
      <button @click="fetchBooks">搜索</button>
      <router-link to="/books/add" class="add-book-btn">添加新书</router-link>
    </div>

    <div v-if="isLoading" class="loading">加载中...</div>
    <div v-if="error" class="error-message">{{ error }}</div>

    <BookList v-if="!isLoading && !error && books.length" :books="books" @edit-book="editBook" @delete-book="confirmDeleteBook" />
    <p v-if="!isLoading && !error && !books.length && hasSearched">没有找到匹配的图书。</p>
  </div>
</template>

<script>
import bookService from '@/services/bookService';
import BookList from '@/components/BookList.vue'; // 假设你有一个 BookList 组件

export default {
  name: 'BooksView',
  components: {
    BookList,
  },
  data() {
    return {
      books: [],
      isLoading: false,
      error: null,
      hasSearched: false, // 用于判断是否执行过搜索
      searchQuery: {
        title: '',
        author: '',
      },
    };
  },
  created() {
    this.fetchBooks(); // 页面创建时加载所有图书
  },
  methods: {
    async fetchBooks() {
      this.isLoading = true;
      this.error = null;
      this.hasSearched = true;
      try {
        // 构建有效的查询参数对象，只包含非空的值
        const queryParams = {};
        if (this.searchQuery.title) {
          queryParams.title = this.searchQuery.title;
        }
        if (this.searchQuery.author) {
          queryParams.author = this.searchQuery.author;
        }
        const response = await bookService.getAllBooks(queryParams);
        this.books = response.data;
      } catch (err) {
        this.error = '获取图书列表失败: ' + (err.response?.data?.message || err.message);
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    editBook(bookId) {
      this.$router.push(`/books/edit/${bookId}`);
    },
    async confirmDeleteBook(bookId) {
      if (window.confirm('确定要删除这本书吗？')) {
        try {
          await bookService.deleteBook(bookId);
          // 重新加载列表或从当前列表中移除
          this.books = this.books.filter(book => book.id !== bookId);
          alert('图书删除成功！');
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
.books-view {
  padding: 20px;
}
.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}
.search-bar input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.search-bar button {
  padding: 8px 15px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.search-bar button:hover {
  background-color: #36a374;
}
.add-book-btn {
  padding: 8px 15px;
  background-color: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  margin-left: auto; /* 将添加按钮推到最右边 */
}
.loading, .error-message {
  margin-top: 20px;
  font-style: italic;
}
.error-message {
  color: red;
}
</style>