<template>
    <div class="main-layout">
      <!-- 顶部导航栏 -->
      <header class="top-navigation">
        <!-- 用户信息 -->
        <div class="nav-item user-profile">
          <div class="avatar-container">
            <img :src="userAvatar" alt="User Avatar" class="user-avatar" />
          </div>
          <span class="nav-text">{{ userName }}</span>
        </div>
        
        <!-- 导航选项卡容器 -->
        <div class="nav-tabs-container">
          <!-- 导航选项卡 -->
          <div 
            v-for="(item, index) in navItems" 
            :key="index"
            class="nav-item"
            :class="{ 'active': activeTab === item.id }"
            @click="item.id !== 'monitor' && switchTab(item.id)"
          >
            <div class="icon-container">
              <img :src="item.icon" :alt="item.name" class="nav-icon" />
            </div>
            <span class="nav-text">{{ item.name }}</span>
          </div>
        </div>
      </header>
  
      <!-- 内容区域 -->
      <main class="content-area">
        <!-- 宇宙粒子效果 -->
        <div id="particles-js" class="cosmic-particles"></div>
        
        <!-- 内容组件层 -->
        <div class="content-layer">
          <!-- 根据激活的选项卡显示不同内容 -->
          <router-view></router-view>
        </div>
      </main>
    </div>
  </template>
  
  <script>
  import {getUserInfo} from '@/api/user'  //导入用户接口
  
  export default {
    name: 'MainLayout',
    data() {
      return {
        // 用户信息
        userName: localStorage.getItem('username') || 'Tongji University',
        userAvatar: require('@/assets/avatar.png'), // 用户头像
        
        // 激活的选项卡
        activeTab: 'devices',
        // 导航项
        navItems: [
          
        ]
      };
    },
    computed: {
      // 根据当前激活的选项卡返回对应的组件
      currentComponent() {
        const activeItem = this.navItems.find(item => item.id === this.activeTab);
        return activeItem ? activeItem.component : 'DeviceCenter';
      }
    },


    mounted() {
      this.fetchUserInfo();
      this.initCosmicParticles();
    },
    


    methods: {
      // 切换选项卡
      switchTab(navId) {
      const routeMap = {
        devices: 'DeviceCenter',
        monitor: 'MonitorCenter',
        simulation: 'DataSimulation',
        alert: 'AlertSystem',
        report: 'ReportSystem'
      };
      
      if (routeMap[navId]) {
        this.$router.push({ name: routeMap[navId] });
      }
    },

      //初始化用户信息
      async fetchUserInfo() {
  try {
    console.log(localStorage.getItem('token'));
    const response = await getUserInfo();
    console.log('用户信息响应:', response); // 调试输出

    // 确保数据结构正确
    if (response.data && response.data.username) {
      this.userName = response.data.username;
      this.userPhone = response.data.phone;
      
      // 同步存储到本地
      localStorage.setItem('username', this.userName);
      localStorage.setItem('userInfo', JSON.stringify(response.data));
    } else {
      console.error('无效的用户信息格式:', response.data);
      this.userName = '未知用户';
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    this.userName = localStorage.getItem('username') || '默认用户';
  }
},