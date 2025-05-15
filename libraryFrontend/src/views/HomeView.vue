<template>
  <div class="home-view">
    <header class="hero">
      <div class="hero-body">
        <p class="title">
          欢迎来到图书馆管理系统
        </p>
        <p class="subtitle">
          在这里管理您的图书和用户。
        </p>
      </div>
    </header>
    <section class="section">
      <div class="columns">
        <div class="column">
          <div class="box">
            <h2 class="title is-4">图书管理</h2>
            <p>浏览、添加、编辑和删除图书信息。</p>
            <router-link to="/books" class="button is-primary is-fullwidth">前往图书管理</router-link>
          </div>
        </div>
        <div class="column" v-if="isAdmin">
          <div class="box">
            <h2 class="title is-4">用户管理</h2>
            <p>管理系统用户账户和权限。</p>
            <router-link to="/users" class="button is-info is-fullwidth">前往用户管理</router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
// import authService from '@/services/authService'; // 如果需要根据角色显示不同内容

export default {
  name: 'HomeView',
  data() {
    return {
      isAdmin: false,
    };
  },
  created() {
    this.checkUserRole();
  },
  methods: {
    checkUserRole() {
      // 实际项目中应从 authService 或 Vuex store 获取角色信息
      const roles = JSON.parse(localStorage.getItem('userRoles')) || [];
      this.isAdmin = roles.includes('ROLE_ADMIN');
    }
  }
};
</script>

<style scoped>
/* 你可以使用 Bulma CSS 框架的样式，或者自定义 */
.home-view {
  padding: 0; /* 通常首页视图占据整个空间 */
}

.hero {
  background-color: #00d1b2; /* 示例颜色，可以自定义 */
  color: white;
  text-align: center;
  padding: 3rem 1.5rem;
}

.hero .title {
  color: white;
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.hero .subtitle {
  color: white;
  font-size: 1.25rem;
}

.section {
  padding: 3rem 1.5rem;
}

.box {
  background-color: #ffffff;
  border-radius: 6px;
  box-shadow: 0 0.5em 1em -0.125em rgba(10, 10, 10, 0.1), 0 0px 0 1px rgba(10, 10, 10, 0.02);
  color: #4a4a4a;
  display: block;
  padding: 1.25rem;
  text-align: center;
}

.box .title.is-4 {
  margin-bottom: 1rem;
}

.button.is-fullwidth {
  display: block;
  width: 100%;
  margin-top: 1.5rem;
}

.button.is-primary {
  background-color: #00d1b2;
  border-color: transparent;
  color: #fff;
}
.button.is-primary:hover {
  background-color: #00c4a7;
}
.button.is-info {
  background-color: #209cee;
  border-color: transparent;
  color: #fff;
}
.button.is-info:hover {
  background-color: #1496ed;
}

/* 简单的列布局 */
.columns {
  display: flex;
  gap: 1.5rem; /* 列之间的间距 */
  justify-content: center;
}
.column {
  flex-basis: 0;
  flex-grow: 1;
  flex-shrink: 1;
  padding: 0.75rem;
  max-width: 400px; /* 控制卡片最大宽度 */
}
</style>