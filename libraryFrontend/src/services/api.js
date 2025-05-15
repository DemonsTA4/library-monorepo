import axios from 'axios';

// 使用 import.meta.env 来访问 Vite 环境变量
const API_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

console.log('API_URL in api.js (VERSION XYZ):', API_URL); // 也加上标记

const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 添加请求拦截器，例如用于自动附加认证 token
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('authToken'); // 假设 token 存在 localStorage
  if (token) {
    config.headers.Authorization = `Bearer ${token}`; // 或者其他认证方案
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// 可选：添加响应拦截器，例如用于全局错误处理或刷新 token
apiClient.interceptors.response.use(response => {
  return response;
}, error => {
  if (error.response && error.response.status === 401) {
    // 处理未授权错误，例如跳转到登录页
    console.error('Unauthorized, redirecting to login.');
    // router.push('/login'); // 需要引入 Vue Router 实例
  }
  return Promise.reject(error);
});

export default apiClient;