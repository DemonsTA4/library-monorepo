import { createApp } from 'vue';
import App from './App.vue';
import router from './router'; // 引入路由配置
// import store from './store'; // 如果使用 Vuex

const app = createApp(App);
app.use(router); // 使用路由
// app.use(store); // 使用 Vuex
app.mount('#app');