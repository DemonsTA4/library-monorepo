<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <router-link to="/" class="navbar-item brand-text">图书馆管理</router-link>
    </div>
    <div class="navbar-menu">
      <div class="navbar-start">
        <router-link to="/" class="navbar-item">首页</router-link>
        <router-link to="/books" class="navbar-item">图书管理</router-link>
        <router-link v-if="isAdmin" to="/users" class="navbar-item">用户管理</router-link>
      </div>
      <div class="navbar-end">
        <div v-if="isLoggedIn" class="navbar-item">
          <span>你好, {{ username }}</span>
          <a class="navbar-item button is-light" @click="logout">登出</a>
        </div>
        <div v-else class="navbar-item">
          <router-link to="/login" class="button is-primary">登录</router-link>
          </div>
      </div>
    </div>
  </nav>
</template>

<script>
// 假设你有一个 authService 来处理认证状态和登出逻辑
// import authService from '@/services/authService';

export default {
  name: 'NavbarComponent', // 更改组件名称以避免与 HTML 保留字冲突
  data() {
    return {
      isLoggedIn: false,
      username: '',
      isAdmin: false, // 根据实际角色判断
    };
  },
  created() {
    this.checkLoginStatus();
    // 监听登录状态变化事件 (例如，如果使用 Vuex 或事件总线)
    // EventBus.$on('login-status-changed', this.checkLoginStatus);
  },
  // beforeUnmount() {
  //   EventBus.$off('login-status-changed', this.checkLoginStatus);
  // },
  watch: {
    // 监听路由变化，以便在导航后刷新登录状态（例如从登录页导航走）
    '$route'() {
      this.checkLoginStatus();
    }
  },
  methods: {
    checkLoginStatus() {
      // 实际项目中，应从认证服务或 Vuex store 获取登录状态和用户信息
      const token = localStorage.getItem('authToken'); // 简单的 token 检查示例
      this.isLoggedIn = !!token;
      if (this.isLoggedIn) {
        // 假设用户信息（包括角色）在登录后也存储了
        this.username = localStorage.getItem('username') || '用户';
        const roles = JSON.parse(localStorage.getItem('userRoles')) || [];
        this.isAdmin = roles.includes('ROLE_ADMIN'); // 假设管理员角色是 'ROLE_ADMIN'
      } else {
        this.username = '';
        this.isAdmin = false;
      }
    },
    async logout() {
      // try {
      //   await authService.logout(); // 调用后端登出接口（如果需要）
      // } catch (error) {
      //   console.error("Logout error:", error);
      // } finally {
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        localStorage.removeItem('userRoles');
        this.isLoggedIn = false;
        this.username = '';
        this.isAdmin = false;
        if (this.$route.meta && this.$route.meta.requiresAuth) {
          this.$router.push('/login'); // 如果当前在受保护页面，则跳转到登录
        } else {
           this.$router.push('/'); // 否则跳转到首页或登录页
        }
      // }
    },
  },
};
</script>

<style scoped>
.navbar {
  background-color: #f5f5f5;
  padding: 0.5rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dbdbdb;
}
.navbar-brand .navbar-item.brand-text {
  font-weight: bold;
  font-size: 1.5rem;
  color: #333;
}
.navbar-menu {
  display: flex;
  align-items: center;
  flex-grow: 1; /* 允许菜单项填充剩余空间 */
}
.navbar-start {
  display: flex;
  margin-right: auto; /* 将开始部分推到左边 */
}
.navbar-end {
  display: flex;
  margin-left: auto; /* 将结束部分推到右边 */
}
.navbar-item {
  padding: 0.5rem 0.75rem;
  color: #4a4a4a;
  text-decoration: none;
}
.navbar-item:hover,
.navbar-item.router-link-exact-active {
  background-color: #e8e8e8;
  color: #3273dc;
}
.navbar-item span {
  margin-right: 10px;
}
/* 简易按钮样式，你可以使用 UI 库或自定义更复杂的样式 */
.button {
  padding: 0.5em 1em;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
}
.button.is-light {
  background-color: #f5f5f5;
  border-color: #dbdbdb;
  color: #363636;
}
.button.is-light:hover {
  background-color: #e8e8e8;
  border-color: #b5b5b5;
}
.button.is-primary {
  background-color: #00d1b2;
  border-color: transparent;
  color: #fff;
}
.button.is-primary:hover {
  background-color: #00c4a7;
}
</style>