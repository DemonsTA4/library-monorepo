import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import BooksView from '../views/BooksView.vue';
import AddBookView from '../views/AddBookView.vue';
import EditBookView from '../views/EditBookView.vue';
import BookDetailView from '../views/BookDetailView.vue'; // 你之前导入为 DetailBookView，保持一致性或改为 BookDetailView
import LoginView from '../views/LoginView.vue';
import UsersView from '../views/UserView.vue'; // 假设你已经创建或将要创建 UsersView.vue 用于显示用户列表
// 如果有单独的添加和编辑用户页面，也需要导入
// import AddUserView from '../views/AddUserView.vue';
// import EditUserView from '../views/EditUserView.vue';
// import NotFoundView from '../views/NotFoundView.vue'; // 如果你想添加404页面

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
  },
  {
    path: '/login', // 添加登录页面的路由
    name: 'login',
    component: LoginView,
  },
  {
    path: '/books',
    name: 'books',
    component: BooksView,
    meta: { requiresAuth: true } // 示例：访问图书列表需要登录
  },
  {
    path: '/books/add',
    name: 'add-book',
    component: AddBookView,
    meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } // 示例：只有管理员能添加图书
  },
  {
    path: '/books/edit/:id', // 动态路由参数
    name: 'edit-book',
    component: EditBookView,
    props: true, // 将路由参数作为 props 传递给组件
    meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } // 示例：只有管理员能编辑图书
  },
  {
    path: '/books/:id', // 图书详情页的路由
    name: 'book-detail',
    component: BookDetailView, // 确保这里的组件名与 import 时一致
    props: true,
    meta: { requiresAuth: true } // 示例：查看图书详情需要登录
  },
  {
    path: '/users', // 用户管理页面的路由
    name: 'users',
    component: UsersView, // 确保你有一个 UsersView.vue
    meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] } // 示例：只有管理员能访问用户管理
  },
  // 如果有单独的添加和编辑用户页面，可以这样添加：
  // {
  //   path: '/users/add',
  //   name: 'add-user',
  //   component: AddUserView, // 确保你有一个 AddUserView.vue
  //   meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] }
  // },
  // {
  //   path: '/users/edit/:id',
  //   name: 'edit-user',
  //   component: EditUserView, // 确保你有一个 EditUserView.vue
  //   props: true,
  //   meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] }
  // },

  // 404 页面 (放在所有路由的最后)
  // {
  //   path: '/:pathMatch(.*)*', // 匹配所有未匹配到的路径
  //   name: 'NotFound',
  //   component: NotFoundView // 确保你有一个 NotFoundView.vue
  // }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL), // 使用 HTML5 History 模式
  routes, // 上面定义的路由数组
});

router.beforeEach((to, from, next) => {
  console.log(`Navigating to: ${to.path}`);
  next(); // 暂时允许所有导航
});

export default router;