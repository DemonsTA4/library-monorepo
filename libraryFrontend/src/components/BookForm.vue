<template>
  <form @submit.prevent="submitForm" class="book-form">
    <div>
      <label for="title">书名:</label>
      <input type="text" id="title" v-model="book.title" required />
    </div>
    <div>
      <label for="author">作者:</label>
      <input type="text" id="author" v-model="book.author" required />
    </div>
    <div>
      <label for="isbn">ISBN:</label>
      <input type="text" id="isbn" v-model="book.isbn" required />
    </div>
    <div>
      <label for="publisher">出版社:</label>
      <input type="text" id="publisher" v-model="book.publisher" />
    </div>
    <div>
      <label for="price">价格:</label>
      <input type="number" id="price" v-model.number="book.price" step="0.01" required />
    </div>
    <div>
      <label for="quantity">数量:</label>
      <input type="number" id="quantity" v-model.number="book.quantity" required />
    </div>
    <button type="submit" :disabled="isSubmitting">
      {{ isSubmitting ? '提交中...' : (initialBookData ? '更新图书' : '添加图书') }}
    </button>
  </form>
</template>

<script>
export default {
  name: 'BookForm',
  props: {
    initialBookData: { // 用于编辑时填充表单
      type: Object,
      default: () => ({
        title: '',
        author: '',
        isbn: '',
        publisher: '',
        price: null,
        quantity: null,
        coverImageUrl: '' // 如果添加了此字段
      }),
    },
    isSubmitting: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      // 使用 props 的深拷贝，避免直接修改 props
      book: JSON.parse(JSON.stringify(this.initialBookData)),
    };
  },
  watch: {
    // 如果 initialBookData 异步加载，需要监听变化来更新表单
    initialBookData(newData) {
      this.book = JSON.parse(JSON.stringify(newData));
    }
  },
  methods: {
    submitForm() {
      // 可以添加一些客户端校验
      this.$emit('submit-form', { ...this.book });
    },
  },
};
</script>

<style scoped>
.book-form div {
  margin-bottom: 15px;
}
.book-form label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.book-form input[type="text"],
.book-form input[type="number"],
.book-form input[type="url"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}
.book-form button {
  padding: 10px 20px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.book-form button:disabled {
  background-color: #aaa;
}
.book-form button:hover:not(:disabled) {
  background-color: #218838;
}
</style>