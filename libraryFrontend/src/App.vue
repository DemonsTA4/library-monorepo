<template>
  <div id="app">
    <NavbarComponent /> <!- - 使用 Navbar 组件 -->
    <div class="container"> <!- - 可选的容器 -->
      <router-view />
    </div>
  </div>
</template>

<script>
import NavbarComponent from './components/Navbar.vue'; // 导入 Navbar 组件

export default {
  name: 'App',
  components: {
    NavbarComponent, // 注册组件
  },
  // App.vue 通常不需要直接管理登录状态，这个职责可以交给 NavbarComponent 内部
  // 或者通过 props/events 或 Vuex/Pinia 与 NavbarComponent 通信
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* text-align: center; */ /* 导航栏通常是横跨顶部，内容区域可以自己控制对齐 */
  color: #2c3e50;
}

/* 你可以保留一些全局样式，或者将大部分样式移到 Navbar.vue 的 <style scoped> 中 */
.container {
  padding: 20px;
}
</style>