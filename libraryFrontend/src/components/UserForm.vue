<template>
  <form @submit.prevent="submitForm" class="user-form">
    <div>
      <label for="username">用户名:</label>
      <input type="text" id="username" v-model="user.username" required :disabled="isEditMode" />
      <small v-if="isEditMode" class="form-text text-muted">用户名在编辑模式下不可修改。</small>
    </div>
    <div>
      <label for="password">密码:</label>
      <input type="password" id="password" v-model="user.password" :placeholder="isEditMode ? '留空则不修改密码' : ''" :required="!isEditMode" />
    </div>
    <div>
      <label for="role">角色:</label>
      <select id="role" v-model="user.role" required>
        <option value="ROLE_USER">用户 (ROLE_USER)</option>
        <option value="ROLE_ADMIN">管理员 (ROLE_ADMIN)</option>
        </select>
    </div>
    <div>
      <label for="enabled">账户是否启用:</label>
      <input type="checkbox" id="enabled" v-model="user.enabled" />
    </div>

    <button type="submit" :disabled="isSubmitting" class="button is-primary">
      {{ isSubmitting ? '提交中...' : (isEditMode ? '更新用户' : '创建用户') }}
    </button>
    <button type="button" @click="$emit('cancel')" v-if="isEditMode" class="button is-light">取消</button>
  </form>
</template>

<script>
export default {
  name: 'UserForm',
  props: {
    initialUserData: {
      type: Object,
      default: () => ({
        username: '',
        password: '', // 编辑时，如果为空则不修改密码
        role: 'ROLE_USER', // 默认角色
        enabled: true,
      }),
    },
    isSubmitting: {
      type: Boolean,
      default: false,
    },
    isEditMode: { // 用于区分是创建还是编辑模式
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      // 深拷贝以避免直接修改 prop
      user: JSON.parse(JSON.stringify(this.initialUserData)),
    };
  },
  watch: {
    initialUserData: {
      handler(newData) {
        this.user = JSON.parse(JSON.stringify(newData));
        // 编辑模式下，如果 initialUserData 传入时 password 为空，确保表单中的 password 也为空
        // 这样用户不必为了不修改密码而清空输入框
        if (this.isEditMode && !newData.password) {
            this.user.password = '';
        }
      },
      deep: true, // 需要深度监听对象内部属性的变化
      immediate: true // 立即执行一次，以确保初始值正确设置
    }
  },
  methods: {
    submitForm() {
      const userDataToSubmit = { ...this.user };
      // 在编辑模式下，如果密码字段为空，则不应提交密码字段，以便后端不更新密码
      if (this.isEditMode && (userDataToSubmit.password === null || userDataToSubmit.password.trim() === '')) {
        delete userDataToSubmit.password;
      }
      this.$emit('submit-form', userDataToSubmit);
    },
  },
};
</script>

<style scoped>
.user-form div {
  margin-bottom: 15px;
}
.user-form label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.user-form input[type="text"],
.user-form input[type="password"],
.user-form select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}
.user-form input[type="checkbox"] {
  margin-right: 5px;
  vertical-align: middle;
}
.user-form .form-text {
  font-size: 0.85em;
  color: #6c757d;
}
.user-form button {
  margin-right: 10px;
}
.button.is-primary {
  background-color: #28a745;
  color: white;
  border: none;
}
.button.is-primary:hover:not(:disabled) {
  background-color: #218838;
}
.button:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}
.button.is-light {
  background-color: #f5f5f5;
  border-color: #dbdbdb;
  color: #363636;
}
.button.is-light:hover {
  background-color: #e8e8e8;
}
</style>