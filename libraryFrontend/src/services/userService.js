import apiClient from './api';

export default {
  /**
   * 获取所有用户列表
   * @returns {Promise<Array>}
   */
  getAllUsers() {
    return apiClient.get('/users');
  },

  /**
   * 根据 ID 获取单个用户信息
   * @param {number|string} id - 用户 ID
   * @returns {Promise<Object>}
   */
  getUserById(id) {
    return apiClient.get(`/users/${id}`);
  },

  /**
   * 创建新用户 (通常由管理员操作，或通过专门的注册接口)
   * @param {Object} userData - 用户数据
   * @returns {Promise<Object>}
   */
  createUser(userData) {
    // 注意：如果注册逻辑在 authService.register 中，这里可能是管理员创建用户的接口
    return apiClient.post('/users', userData);
  },

  /**
   * 更新用户信息
   * @param {number|string} id - 用户 ID
   * @param {Object} userData - 要更新的用户数据
   * @returns {Promise<Object>}
   */
  updateUser(id, userData) {
    return apiClient.put(`/users/${id}`, userData);
  },

  /**
   * 删除用户
   * @param {number|string} id - 用户 ID
   * @returns {Promise}
   */
  deleteUser(id) {
    return apiClient.delete(`/users/${id}`);
  }
};