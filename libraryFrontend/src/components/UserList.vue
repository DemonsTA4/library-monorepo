<template>
  <div class="user-list">
    <div v-if="users.length === 0 && !isLoading" class="no-users">
      暂无用户数据。
    </div>
    <table v-else>
      <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
          <th>角色</th>
          <th>是否启用</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username }}</td>
          <td>{{ user.role }}</td>
          <td>{{ user.enabled ? '是' : '否' }}</td>
          <td>
            <button @click="$emit('edit-user', user.id)" class="button is-small is-info">编辑</button>
            <button @click="$emit('delete-user', user.id)" class="button is-small is-danger">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'UserList',
  props: {
    users: {
      type: Array,
      required: true,
    },
    isLoading: { // 可选，用于显示加载状态
      type: Boolean,
      default: false,
    }
  },
};
</script>

<style scoped>
.user-list table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}
.user-list th, .user-list td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
.user-list th {
  background-color: #f2f2f2;
}
.user-list button {
  margin-right: 5px;
}
.no-users {
  padding: 20px;
  text-align: center;
  color: #777;
}
/* 假设你有一些全局的按钮样式或从 UI 库导入 */
.button.is-small {
  font-size: 0.75rem;
  padding: 0.25em 0.5em;
}
.button.is-info {
  background-color: #209cee;
  color: white;
  border: none;
}
.button.is-info:hover {
  background-color: #1496ed;
}
.button.is-danger {
  background-color: #ff3860;
  color: white;
  border: none;
}
.button.is-danger:hover {
  background-color: #ff1f4b;
}
</style>