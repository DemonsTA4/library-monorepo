import apiClient from './api';
// 如果你使用了 Vue Router，并且想在服务中进行编程式导航
// import router from '@/router'; // 假设你的 router 实例在 @/router/index.js

const TOKEN_KEY = 'authToken';
const USER_INFO_KEY = 'userInfo'; // 可以存储用户名、角色等

export default {
  /**
   * 用户登录
   * @param {Object} credentials - 包含 username 和 password 的对象
   * @returns {Promise<Object>} - 成功时返回后端的用户数据或 token 数据
   */
  // src/services/authService.js (部分 login 方法)
  async login(credentials) {
    try {
      const response = await apiClient.post('/auth/login', credentials); // 确认此路径与后端 AuthController 匹配
  
      if (response.data && response.data.token) {
        localStorage.setItem('authToken', response.data.token);
        const userInfo = { // 从响应中提取或构造用户信息对象
            username: response.data.username,
            roles: response.data.roles || []
        };
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
  
        // 设置 Axios 默认头部，以便后续请求自动携带 token
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`;
  
        // 触发状态更新（可选，如果 Navbar 等组件不直接监听 localStorage）
        window.dispatchEvent(new CustomEvent('login-status-changed', { detail: { isAuthenticated: true, user: userInfo } }));
  
        return response.data;
      } else {
        throw new Error('登录响应中缺少 token');
      }
    } catch (error) {
      this.logoutLocally(); // 登录失败则清除本地状态
      throw error;
    }
  },

  /**
   * 用户登出
   */
  logoutLocally() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_INFO_KEY);
    delete apiClient.defaults.headers.common['Authorization'];
    // 如果使用了 Vuex 或 Pinia，也需要在这里清除相关的用户状态
  },

  async logout() {
    // 如果后端有专门的登出 API (例如使 token 失效)，则调用它
    // try {
    //   await apiClient.post('/auth/logout');
    // } catch (error) {
    //   console.warn("Error calling backend logout, proceeding with local logout:", error);
    // }
    this.logoutLocally();
    // 可以在这里触发全局事件或更新状态，让 Navbar 等组件响应
    // router.push('/login'); // 登出后跳转到登录页
  },

  /**
   * 用户注册
   * @param {Object} userData - 包含 username, password, role 等用户信息
   * @returns {Promise<Object>}
   */
  async register(userData) {
    // 假设注册 API 是 /api/v1/users (POST) 或 /api/v1/auth/register
    return apiClient.post('/users/register', userData); // 根据你的后端 API 调整端点
  },

  /**
   * 获取当前存储的认证 token
   * @returns {string|null}
   */
  getToken() {
    return localStorage.getItem(TOKEN_KEY);
  },

  /**
   * 获取当前存储的用户信息
   * @returns {Object|null}
   */
  getUserInfo() {
    const userInfo = localStorage.getItem(USER_INFO_KEY);
    return userInfo ? JSON.parse(userInfo) : null;
  },

  /**
   * 检查用户是否已登录 (基于 token 是否存在)
   * @returns {boolean}
   */
  isAuthenticated() {
    return !!this.getToken();
  },

  /**
   * 检查用户是否拥有特定角色
   * @param {string} role
   * @returns {boolean}
   */
  hasRole(role) {
    const userInfo = this.getUserInfo();
    return userInfo && userInfo.roles && userInfo.roles.includes(role);
  }
};