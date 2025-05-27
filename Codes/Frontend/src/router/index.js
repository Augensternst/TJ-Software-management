import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from '@/views/Login.vue'; // 导入登录组件
import MainPage from '@/views/MainLayout.vue'; //导入主页面组件


const routes = [
  {
    path: '/',
    name: 'LoginPage',
    component: LoginPage, 
  },

  {
    path: '/main/:tab?',
    name: 'main',
    component: MainPage,
  }

];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;