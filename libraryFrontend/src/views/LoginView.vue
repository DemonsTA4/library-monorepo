<template>
  <div class="login-view">
    <div class="login-container">
      <h1 class="title">用户登录</h1>

      <form @submit.prevent="handleLogin">
        <div class="field">
          <label class="label" for="username">用户名:</label>
          <div class="control">
            <input class="input" type="text" id="username" v-model="credentials.username" required autofocus />
          </div>
        </div>

        <div class="field">
          <label class="label" for="password">密码:</label>
          <div class="control">
            <input class="input" type="password" id="password" v-model="credentials.password" required />
          </div>
        </div>

        <div v-if="error" class="notification is-danger">
          {{ error }}
        </div>

        <div class="field">
          <div class="control">
            <button type="submit" class="button is-link is-fullwidth" :disabled="isLoading">
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
          </div>
        </div>
      </form>
	  <p class="has-text-centered" style="margin-top: 1.5rem;">
	          还没有账户? <router-link to="/register">立即注册</router-link>
	        </p>
    </div>
  </div>
</template>

<script>
import authService from '@/services/authService';

export default {
  name: 'LoginView',
  data() {
    return {
      credentials: {
        username: '',
        password: '',
      },
      isLoading: false,
      error: null,
    };
  },
  methods: {
    async handleLogin() {
      this.isLoading = true;
      this.error = null;
      try {
        const responseData = await authService.login(this.credentials);
        // 登录成功后，authService 应该已经处理了 token 和用户信息的存储
        // console.log('Login successful:', responseData);

        // 触发一个全局事件或更新 store 来通知 Navbar 等组件登录状态已改变
        // EventBus.$emit('login-status-changed'); // 如果使用事件总线

        // 获取重定向地址（如果存在）或跳转到默认页面
        const redirectPath = this.$route.query.redirect || '/';
        this.$router.push(redirectPath);

      } catch (err) {
        this.error = '登录失败: ' + (err.response?.data?.message || '用户名或密码错误。');
        console.error('Login error details:', err.response || err);
      } finally {
        this.isLoading = false;
      }
    },
  },
};
</script>

<style scoped>
/* 你可以使用 Bulma CSS 框架的样式，或者自定义 */
.login-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh; /* 使其在视口中居中 */
  background-color: #f5f5f5; /* 可选背景色 */
}

.login-container {
  background: white;
  padding: 2rem;
  border-radius: 5px;
  box-shadow: 0 0.5em 1em -0.125em rgba(10, 10, 10, 0.1), 0 0px 0 1px rgba(10, 10, 10, 0.02);
  width: 100%;
  max-width: 400px;
}

.title {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #363636;
}

.field:not(:last-child) {
  margin-bottom: 1rem;
}

.label {
  font-weight: bold;
  color: #363636;
}

.input {
  width: 100%;
  padding: 0.75em;
  border: 1px solid #dbdbdb;
  border-radius: 4px;
  box-sizing: border-box;
}
.input:focus {
  border-color: #3273dc;
  box-shadow: 0 0 0 0.125em rgba(50, 115, 220, 0.25);
}

.button.is-link {
  background-color: #3273dc;
  border-color: transparent;
  color: #fff;
}
.button.is-link:hover {
  background-color: #276cda;
}
.button.is-fullwidth {
  display: block;
  width: 100%;
}
.button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.notification.is-danger {
  background-color: #ff3860;
  color: #fff;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}
.has-text-centered {
  text-align: center;
  margin-top: 1rem;
}
</style>