<template>
  <div class="book-list">
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>书名</th>
          <th>作者</th>
          <th>ISBN</th>
          <th>价格</th>
          <th>数量</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.id">
          <td>{{ book.id }}</td>
          <td>{{ book.title }}</td>
          <td>{{ book.author }}</td>
          <td>{{ book.isbn }}</td>
          <td>{{ book.price }}</td>
          <td>{{ book.quantity }}</td>
          <td>
            <button @click="$emit('edit-book', book.id)">编辑</button>
            <button @click="$emit('delete-book', book.id)" class="delete-btn">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'BookList',
  props: {
    books: {
      type: Array,
      required: true,
    },
  },
};
</script>

<style scoped>
.book-list table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}
.book-list th, .book-list td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
.book-list th {
  background-color: #f2f2f2;
}
.book-list button {
  margin-right: 5px;
  padding: 5px 10px;
  cursor: pointer;
}
.delete-btn {
  background-color: #dc3545;
  color: white;
  border: none;
}
.delete-btn:hover {
  background-color: #c82333;
}
</style>