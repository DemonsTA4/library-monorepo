<template>
  <div class="users-view">
    <h1>用户管理</h1>
    <div class="actions">
      <router-link to="/users/add" class="button is-success">添加新用户</router-link>
    </div>

    <div v-if="isLoading" class="loading">加载中...</div>
    <div v-if="error" class="error-message">{{ error }}</div>

    <UserList v-if="!isLoading && !error" :users="users" @edit-user="editUser" @delete-user="confirmDeleteUser" />
  </div>
</template>

<script>
import userService from '@/services/userService'; // 假设你创建了这个服务
import UserList from '@/components/UserList.vue';

export default {
  name: 'UsersView',
  components: {
    UserList,
  },
  data() {
    return {
      users: [],
      isLoading: false,
      error: null,
    };
  },
  created() {
    this.fetchUsers();
  },
  methods: {
    async fetchUsers() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await userService.getAllUsers(); // 假设 userService 中有此方法
        this.users = response.data;
      } catch (err) {
        this.error = '获取用户列表失败: ' + (err.response?.data?.message || err.message);
        console.error(err);
      } finally {
        this.isLoading = false;
      }
    },
    editUser(userId) {
      this.$router.push(`/users/edit/${userId}`); // 假设有此路由
    },
    async confirmDeleteUser(userId) {
      if (window.confirm('确定要删除这个用户吗？')) {
        try {
          await userService.deleteUser(userId); // 假设 userService 中有此方法
          this.users = this.users.filter(user => user.id !== userId);
          alert('用户删除成功！');
        } catch (err) {
          alert('删除用户失败: ' + (err.response?.data?.message || err.message));
          console.error(err);
        }
      }
    },
  },
};
</script>

<style scoped>
.users-view {
  padding: 20px;
}
.actions {
  margin-bottom: 20px;
}
.loading, .error-message {
  margin-top: 20px;
  font-style: italic;
}
.error-message {
  color: red;
}
.button.is-success {
  background-color: #23d160;
  color: white;
  border:none;
  text-decoration: none;
  padding: 0.5em 1em;
  border-radius: 4px;
}
.button.is-success:hover {
   background-color: #20bc56;
}
</style>