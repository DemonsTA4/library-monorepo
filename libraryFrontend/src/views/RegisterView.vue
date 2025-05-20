<template>
  <div class="register-container">
    <h2>用户注册</h2>
    <form @submit.prevent="handleRegister">
      <div class="form-group">
        <label for="username">用户名:</label>
        <input type="text" id="username" v-model="username" required />
      </div>
      <div class="form-group">
        <label for="password">密码:</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>
      <button type="submit" :disabled="isLoading">
        {{ isLoading ? '注册中...' : '注册' }}
      </button>
    </form>
    <p>
      已有账户? <router-link to="/login">点此登录</router-link>
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// 假设你的API服务函数放在 src/services/authService.js
import authService from '@/services/authService'; // 确保路径正确

const username = ref('');
const password = ref('');
// const confirmPassword = ref(''); // 如果需要确认密码
const isLoading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const router = useRouter();

const handleRegister = async () => {
  // if (password.value !== confirmPassword.value) { // 如果需要确认密码
  //   errorMessage.value = '两次输入的密码不一致！';
  //   return;
  // }

  isLoading.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  try {
    const userData = {
      username: username.value,
      password: password.value,
	  //role: role.value || null,
      // 根据你的 UserDto 添加其他需要的字段
    };
    const response = await authService.register(userData);
    // 后端成功注册通常返回 201 Created
    successMessage.value = '注册成功！将跳转到登录页面...';
    // 可以在这里根据后端返回的数据做一些操作，比如保存用户名等
    console.log('注册成功:', response); // response 通常是创建成功的用户对象

    setTimeout(() => {
      router.push('/login'); // 注册成功后跳转到登录页
    }, 2000);

  } catch (error) {
    if (error.response && error.response.data) {
        // 尝试从后端响应中获取更具体的错误信息
        // 后端 UserController 中是 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        // error.response.data 可能是一个对象，其中包含 message 字段，或者直接是字符串
        if (typeof error.response.data === 'string') {
             errorMessage.value = error.response.data;
        } else if (error.response.data.message) {
             errorMessage.value = error.response.data.message;
        } else if (error.response.status === 400) {
            errorMessage.value = '注册失败：无效的输入或用户名已存在。';
        } else {
            errorMessage.value = '注册失败，请稍后再试。';
        }
    } else {
        errorMessage.value = '注册失败，网络错误或服务器无响应。';
    }
    console.error('注册失败:', error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
}

.form-group input {
  width: calc(100% - 20px);
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

button:disabled {
  background-color: #aaa;
}

button:hover:not(:disabled) {
  background-color: #0056b3;
}

.error-message {
  color: red;
  margin-bottom: 15px;
  text-align: center;
}

.success-message {
  color: green;
  margin-bottom: 15px;
  text-align: center;
}

p {
  text-align: center;
  margin-top: 20px;
}
</style>