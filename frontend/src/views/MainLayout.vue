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
            @click="switchTab(item.id)"
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
          <component :is="currentComponent"></component>
        </div>
      </main>
    </div>
  </template>
  
  <script>
  // 导入各个页面组件
  import DeviceCenter from '@/views/DeviceCenter.vue';
  import MonitorCenter from '@/views/MonitorCenter.vue';
  import DataSimulation from '@/views/DataSimulation.vue';
  import AlertSystem from '@/views/AlertSystem.vue';
  import ReportSystem from '@/views/ReportSystem.vue';
  
  export default {
    name: 'MainLayout',
    components: {
      DeviceCenter,
      MonitorCenter,
      DataSimulation,
      AlertSystem,
      ReportSystem
    },
    data() {
      return {
        // 用户信息
        userName: 'Tongji University',
        userAvatar: require('@/assets/avatar.png'), // 用户头像
        
        // 激活的选项卡
        activeTab: 'devices',
        
        // 导航项
        navItems: [
          {
            id: 'devices',
            name: '设备中心',
            icon: require('@/assets/icons/devices.svg'),
            component: 'DeviceCenter'
          },
          {
            id: 'monitor',
            name: '监测中心',
            icon: require('@/assets/icons/monitor.svg'),
            component: 'MonitorCenter'
          },
          {
            id: 'simulation',
            name: '数据模拟',
            icon: require('@/assets/icons/simulation.svg'),
            component: 'DataSimulation'
          },
          {
            id: 'alert',
            name: '警报系统',
            icon: require('@/assets/icons/alert.svg'),
            component: 'AlertSystem'
          },
          {
            id: 'report',
            name: '报表系统',
            icon: require('@/assets/icons/report.svg'),
            component: 'ReportSystem'
          }
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
      // 从路由参数获取初始选项卡
      if (this.$route.params.tab && this.navItems.some(item => item.id === this.$route.params.tab)) {
        this.activeTab = this.$route.params.tab;
      }
      
      // 初始化宇宙粒子效果
      this.initCosmicParticles();
    },
    methods: {
      // 切换选项卡
      switchTab(tabId) {
        this.activeTab = tabId;
        // 可选：更新路由
        this.$router.push({ name: 'main', params: { tab: tabId } });
      },
      
      // 初始化宇宙粒子效果
      initCosmicParticles() {
        setTimeout(() => {
          if (window.particlesJS) {
            window.particlesJS('particles-js', {
              "particles": {
                "number": {
                  "value": 160,
                  "density": {
                    "enable": true,
                    "value_area": 800
                  }
                },
                "color": {
                  "value": "#ffffff"
                },
                "shape": {
                  "type": "circle",
                  "stroke": {
                    "width": 0,
                    "color": "#000000"
                  },
                  "polygon": {
                    "nb_sides": 5
                  }
                },
                "opacity": {
                  "value": 0.5,
                  "random": true,
                  "anim": {
                    "enable": true,
                    "speed": 1,
                    "opacity_min": 0,
                    "sync": false
                  }
                },
                "size": {
                  "value": 3,
                  "random": true,
                  "anim": {
                    "enable": true,
                    "speed": 2,
                    "size_min": 0.3,
                    "sync": false
                  }
                },
                "line_linked": {
                  "enable": false,
                  "distance": 150,
                  "color": "#ffffff",
                  "opacity": 0.4,
                  "width": 1
                },
                "move": {
                  "enable": true,
                  "speed": 1,
                  "direction": "none",
                  "random": true,
                  "straight": false,
                  "out_mode": "out",
                  "bounce": false,
                  "attract": {
                    "enable": false,
                    "rotateX": 600,
                    "rotateY": 600
                  }
                }
              },
              "interactivity": {
                "detect_on": "canvas",
                "events": {
                  "onhover": {
                    "enable": true,
                    "mode": "bubble"
                  },
                  "onclick": {
                    "enable": true,
                    "mode": "repulse"
                  },
                  "resize": true
                },
                "modes": {
                  "grab": {
                    "distance": 400,
                    "line_linked": {
                      "opacity": 1
                    }
                  },
                  "bubble": {
                    "distance": 250,
                    "size": 0,
                    "duration": 2,
                    "opacity": 0,
                    "speed": 3
                  },
                  "repulse": {
                    "distance": 400,
                    "duration": 0.4
                  },
                  "push": {
                    "particles_nb": 4
                  },
                  "remove": {
                    "particles_nb": 2
                  }
                }
              },
              "retina_detect": true
            });
            console.log("宇宙粒子效果已成功初始化");
          } else {
            console.error('particles.js 未加载，请确保正确引入该库');
          }
        }, 100);
      }
    }
  };
  </script>
  
  
  <style>
  /* 添加全局重置样式 */
  html, body {
    margin: 0;
    padding: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
  }
  
  #app {
    margin: 0;
    padding: 0;
    width: 100%;
    height: 100%;
  }
  </style>
  
  <style scoped>
  .main-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #f5f7fa;
  }
  
  /* 顶部导航栏样式 */
  .top-navigation {
    display: flex;
    width: 100%;
    height: 100px;
    background: linear-gradient(90deg, #2d4277 0%, #4e2677 50%, #340245 100%);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    align-items: center;
    padding: 0;
    z-index: 100;
  }
  
  /* 新增导航选项卡容器 */
  .nav-tabs-container {
    display: flex;
    flex: 1;
    height: 100%;
  }
  
  .nav-item {
    display: flex;
    align-items: center;
    height: 100%;
    padding: 0 16px;
    color: #ffffff;
    font-family: 'Noto Sans', sans-serif;
    font-weight: 500;
    font-size: 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
  }
  
  /* 修改选项卡样式使其平均分布 */
  .nav-tabs-container .nav-item {
    flex: 1;
    justify-content: center;
    text-align: center;
  }
  
  .nav-item:not(.user-profile):hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
  
  .nav-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 3px;
    background-color: #F689EF;
  }
  
  /* 用户信息区域样式 */
  .user-profile {
    flex: 0 0 240px; /* 固定宽度 */
    margin-right: 0;
    cursor: default;
    padding: 0 20px;
    overflow: hidden;
    white-space: nowrap;
  }
  
  .avatar-container {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 12px;
    background-color: #ffffff;
    flex-shrink: 0; /* 防止头像被压缩 */
  }
  
  .user-avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .icon-container {
    width: 50px; /* 减小一点宽度 */
    height: 50px; /* 减小一点高度 */
    margin-right: 8px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .nav-icon {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
  
  .nav-text {
    white-space: nowrap;
    color: #AEB9E1;
  }
  
  /* 添加文本溢出处理 */
  .user-profile .nav-text {
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 140px; /* 根据需要调整 */
  }
  
  /* 内容区域样式 */
  .content-area {
    flex: 1;
    position: relative;
    overflow: hidden;
    background: linear-gradient(0deg, rgba(184, 142, 255, 0.33), rgba(184, 142, 255, 0.33)), #0F0F0F;
  }
  
  /* 宇宙粒子效果 */
  .cosmic-particles {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
  }
  
  /* 内容层 */
  .content-layer {
    position: relative;
    z-index: 2;
    padding: 24px;
    height: 100%;
    overflow-y: auto;
    box-sizing: border-box;
    color: #ffffff;
  }
  
  /* 星云效果 - 添加一些大型模糊光点 */
  .content-area::before,
  .content-area::after {
    content: '';
    position: absolute;
    border-radius: 50%;
    filter: blur(100px);
    opacity: 0.2;
    z-index: 0;
  }
  
  .content-area::before {
    width: 300px;
    height: 300px;
    background: rgba(138, 43, 226, 0.8);
    top: 10%;
    left: 20%;
    animation: float 15s infinite alternate;
  }
  
  .content-area::after {
    width: 200px;
    height: 200px;
    background: rgba(65, 105, 225, 0.8);
    bottom: 15%;
    right: 15%;
    animation: float 12s infinite alternate-reverse;
  }
  
  @keyframes float {
    0% {
      transform: translate(0, 0);
    }
    50% {
      transform: translate(-30px, 20px);
    }
    100% {
      transform: translate(30px, -20px);
    }
  }
  
  /* 响应式设计 */
  @media (max-width: 1024px) {
    .user-profile {
      flex: 0 0 200px; /* 在中等屏幕上减小固定宽度 */
    }
    
    .user-profile .nav-text {
      max-width: 100px;
    }
  }
  
  @media (max-width: 768px) {
    .top-navigation {
      overflow-x: auto;
      justify-content: flex-start;
      padding: 0;
    }
    
    .nav-tabs-container {
      width: calc(100% - 160px); /* 用户头像区域留160px */
    }
    
    .user-profile {
      flex: 0 0 160px;
      padding: 0 10px;
    }
    
    .user-profile .nav-text {
      max-width: 80px;
    }
    
    .nav-tabs-container .nav-item {
      padding: 0 8px;
      font-size: 14px;
      min-width: 70px;
      flex: none; /* 在小屏幕上取消flex:1 */
    }
    
    .nav-text {
      display: none;
    }
    
    .user-profile .nav-text {
      display: inline;
    }
    
    .icon-container {
      margin-right: 0;
      width: 40px;
      height: 40px;
    }
    
    .avatar-container {
      margin-right: 8px;
      width: 40px;
      height: 40px;
    }
  
    .content-area::before {
      width: 150px;
      height: 150px;
    }
  
    .content-area::after {
      width: 100px;
      height: 100px;
    }
  }
  
  @media (max-width: 480px) {
    .top-navigation {
      height: 60px;
    }
    
    .user-profile {
      flex: 0 0 120px;
    }
    
    .user-profile .nav-text {
      font-size: 12px;
      max-width: 60px;
    }
    
    .avatar-container {
      width: 30px;
      height: 30px;
    }
    
    .icon-container {
      width: 30px;
      height: 30px;
    }
  }
  </style>