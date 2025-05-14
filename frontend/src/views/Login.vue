<template>
    <div class="home-container">
      <!-- particles.js 容器 -->
      <div id="particles-js"></div>
      
      <!-- 背景图 -->
      <div class="background-image"></div>
  
      <!-- 网站介绍文字区域 -->
      <div class="intro-text" :class="{ 'fade-in': showIntro, 'fade-out': showForm }">
        <h1>Maintain Mind</h1>
        <p>--------    洞察未来，为您的设备保驾护航    --------</p>
        <div class="hint">
          <span>点击任意位置登录</span>
        </div>
      </div>
  
      <!-- 登录/注册框设计 -->
      <div class="login-container" v-if="showForm" :class="{ 'slide-in': showForm }">
        <div class="login-content">
          <!-- 登录表单 -->
          <div v-if="!isRegisterMode" class="upper-section">
            <div class="login-text">
              <div class="login-title">Login</div>
            </div>
            
            <div class="credentials">
              <div class="input-container">
                <input type="text" v-model="username" placeholder="Username" />
              </div>
              
              <div class="password-section">
                <div class="input-container">
                  <input :type="showPassword ? 'text' : 'password'" v-model="password" placeholder="Password" />
                  <div class="eye-icon" @click="togglePasswordVisibility">
                    <i :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
                  </div>
                </div>
              </div>
              
              <div class="login-actions">
                <button class="login-button" @click="handleLogin">Login</button>
              </div>
            </div>
          </div>
          
          <!-- 注册表单 -->
          <div v-else class="upper-section">
            <div class="login-text">
              <div class="login-title">Register</div>
            </div>
            
            <div class="credentials">
              <div class="input-container">
                <input type="text" v-model="registerUsername" placeholder="Username" />
              </div>
              
              <div class="input-container" style="margin-top: 25px;">
                <input type="tel" v-model="phoneNumber" placeholder="Phone Number" />
              </div>
              
              <div class="password-section">
                <div class="input-container">
                  <input :type="showPassword ? 'text' : 'password'" v-model="registerPassword" placeholder="Password" />
                  <div class="eye-icon" @click="togglePasswordVisibility">
                    <i :class="showPassword ? 'bi bi-eye' : 'bi bi-eye-slash'"></i>
                  </div>
                </div>
              </div>
              
              <div class="login-actions">
                <button class="login-button" @click="handleRegister">Register</button>
              </div>
            </div>
          </div>
          
          <div class="other-logins">
            <div class="or-divider">
              <div class="divider-line"></div>
              <div class="or-text">Or</div>
              <div class="divider-line"></div>
            </div>
          </div>
          
          <div class="signup-section">
            <div class="signup-text" v-if="!isRegisterMode">
              没有账户？ <span class="signup-link" @click="toggleMode">注册</span>
            </div>
            <div class="signup-text" v-else>
              已有账户？ <span class="signup-link" @click="toggleMode">登录</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'HomePage',
    data() {
      return {
        // 登录表单数据
        username: '',
        password: '',
        
        // 注册表单数据
        registerUsername: '',
        registerPassword: '',
        phoneNumber: '',
        
        // 控制显示状态
        showIntro: false,
        showForm: false,
        showPassword: false,
        isRegisterMode: false
      };
    },
    mounted() {
      this.initParticles();
      
      // 延迟显示介绍文字，创建渐入效果
      setTimeout(() => {
        this.showIntro = true;
      }, 500);
      
      // 添加点击事件监听器
      document.addEventListener('click', this.handlePageClick);
    },
    beforeUnmount() {
      // 修正了 beforeUmount 拼写错误为 beforeUnmount
      document.removeEventListener('click', this.handlePageClick);
    },
    methods: {
      initParticles() {
        // particles.js 初始化代码（保持不变）
        setTimeout(() => {
          if (window.particlesJS) {
            window.particlesJS('particles-js', {
              particles: {
                number: {
                  value: 80,
                  density: {
                    enable: true,
                    value_area: 800
                  }
                },
                color: {
                  value: "#ffffff"
                },
                shape: {
                  type: "circle",
                },
                opacity: {
                  value: 0.5,
                },
                size: {
                  value: 3,
                },
                line_linked: {
                  enable: true,
                  distance: 150,
                  color: "#ffffff",
                  opacity: 0.4,
                  width: 1
                },
                move: {
                  enable: true,
                  speed: 3,
                }
              },
              interactivity: {
                detect_on: "canvas",
                events: {
                  onhover: {
                    enable: true,
                    mode: "grab"
                  },
                  onclick: {
                    enable: true,
                    mode: "push"
                  },
                },
              }
            });
            console.log("Particles.js 已成功初始化");
          } else {
            console.error('particles.js 未加载，请确保正确引入该库');
          }
        }, 100);
      },
      
      handlePageClick() {
        if (this.showForm) return;
        this.showIntro = false;
        this.showForm = true;
        document.removeEventListener('click', this.handlePageClick);
      },
      
      // 登录函数
      handleLogin() {
        console.log('用户名:', this.username);
        console.log('密码:', this.password);

        // 这里可以调用登录接口

        this.$router.push('/main');  //我直接跳转，如何呢？ 后面再改
      },
      
      // 注册函数
      handleRegister() {
        console.log('注册用户名:', this.registerUsername);
        console.log('注册密码:', this.registerPassword);
        console.log('手机号:', this.phoneNumber);
        // 这里可以调用注册接口
      },
      
      // 切换登录/注册模式
      toggleMode() {
        this.isRegisterMode = !this.isRegisterMode;
        // 切换模式时重置表单
        if (this.isRegisterMode) {
          this.registerUsername = this.username;
          this.registerPassword = this.password;
        } else {
          this.username = this.registerUsername;
          this.password = this.registerPassword;
        }
      },
      
      // 显示/隐藏密码
      togglePasswordVisibility() {
        this.showPassword = !this.showPassword;
      }
    },
  };
  </script>
  
  <style scoped>
  @import url('https://fonts.googleapis.com/css2?family=Noto+Sans:wght@400;500;600&display=swap');
  @import url('https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css');
  
  .home-container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 0;
    padding: 0;
  }
  
  #particles-js {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
  }
  
  .background-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url('@/assets/home.png');
    background-size: contain;
    background-position: center;
    z-index: -1;
    opacity: 1;
  }
  
  .intro-text {
    position: relative;
    z-index: 1;
    color: white;
    text-align: center;
    max-width: 800px;
    padding: 30px;
    border-radius: 10px;
    background-color: rgba(0, 0, 0, 0.5);
    transform: translateY(30px);
    opacity: 0;
    transition: all 1.5s ease;
  }
  
  .intro-text h1 {
    font-size: 3rem;
    margin-bottom: 1.5rem;
    color: #fff;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  }
  
  .intro-text p {
    font-size: 1.2rem;
    margin-bottom: 1rem;
    line-height: 1.6;
    color: #f5f5f5;
  }
  
  .hint {
    margin-top: 2rem;
    font-size: 1rem;
    color: #cccccc;
    animation: pulse 2s infinite;
  }
  
  @keyframes pulse {
    0% {
      opacity: 0.5;
    }
    50% {
      opacity: 1;
    }
    100% {
      opacity: 0.5;
    }
  }
  
  .fade-in {
    opacity: 1;
    transform: translateY(0);
  }
  
  .fade-out {
    opacity: 0;
    transform: translateY(-30px);
    transition: all 0.5s ease;
  }
  
  /* 登录/注册框样式 */
  .login-container {
    position: absolute;
    width: 480px;
    height: 780px;
    right: 60px;
    left: 38%;
    background: linear-gradient(321.23deg, rgba(191, 191, 191, 0.062) 5.98%, rgba(0, 0, 0, 0) 66.28%), rgba(0, 0, 0, 0.14);
    box-shadow: -8px 4px 5px rgba(0, 0, 0, 0.24);
    backdrop-filter: blur(26.5px);
    border-radius: 20px;
    z-index: 1;
    opacity: 0;
    transform: translateX(100px);
    transition: all 0.8s ease;
  }
  
  .slide-in {
    opacity: 1;
    transform: translateX(0);
  }
  
  .login-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0px;
    gap: 101px;
    position: absolute;
    width: 400px;
    height: 652px;
    left: 40px;
    top: 97px;
  }
  
  .upper-section {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    padding: 0px;
    gap: 14px;
    width: 400px;
    height: 368px;
    transition: all 0.3s ease;
  }
  
  .login-text {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    padding: 0px;
    width: 135px;
    height: 71px;
  }
  
  .login-title {
    width: 150px; /* 增加宽度适应 "Register" 文字 */
    height: 49px;
    font-family: 'Noto Sans', sans-serif;
    font-style: normal;
    font-weight: 600;
    font-size: 36px;
    line-height: 49px;
    color: #FFFFFF;
  }
  
  .credentials {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    padding: 0px;
    gap: 25px;
    width: 400px;
    height: 283px;
  }
  
  .input-container {
    box-sizing: border-box;
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 14px 16px;
    gap: 10px;
    width: 400px;
    height: 55px;
    border: 1px solid #FFFFFF;
    border-radius: 12px;
    position: relative;
    margin-bottom: 0;
    transition: all 0.3s ease;
  }
  
  .input-container input {
    background: transparent;
    border: none;
    width: 100%;
    height: 27px;
    font-family: 'Noto Sans', sans-serif;
    font-style: normal;
    font-weight: 400;
    font-size: 20px;
    line-height: 27px;
    color: #FFFFFF;
    outline: none;
  }
  
  .input-container input::placeholder {
    color: #FFFFFF;
  }
  
  .eye-icon {
    position: absolute;
    right: 16px;
    width: 18px;
    height: 18px;
    color: #FFFFFF;
    cursor: pointer;
  }
  
  .password-section {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    padding: 0px;
    gap: 12px;
    width: 400px;
    height: 89px;
    margin-top: 25px;
  }
  
  .login-actions {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 0px;
    gap: 12px;
    width: 400px;
    height: 89px;
    margin-top: 25px;
  }
  
  .login-button {
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    padding: 14px 10px;
    gap: 10px;
    width: 400px;
    height: 55px;
    background: linear-gradient(90.57deg, #628EFF 9.91%, #8740CD 53.29%, #580475 91.56%);
    border-radius: 12px;
    border: none;
    font-family: 'Noto Sans', sans-serif;
    font-style: normal;
    font-weight: 600;
    font-size: 20px;
    line-height: 27px;
    color: #FFFFFF;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .login-button:hover {
    opacity: 0.9;
    transform: translateY(-2px);
  }
  
  .login-button:active {
    transform: translateY(0);
  }
  
  .other-logins {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0px;
    gap: 12px;
    width: 400px;
    height: 76px;
  }
  
  .or-divider {
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 0px;
    gap: 20px;
    width: 400px;
    height: 22px;
  }
  
  .divider-line {
    width: 170px;
    height: 0px;
    border: 2px solid #4D4D4D;
  }
  
  .or-text {
    width: 20px;
    height: 22px;
    font-family: 'Noto Sans', sans-serif;
    font-style: normal;
    font-weight: 500;
    font-size: 16px;
    line-height: 22px;
    color: #4D4D4D;
  }
  
  .signup-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0px;
    gap: 8px;
    width: 400px;
    height: 60px;
  }
  
  .signup-text {
    width: 239px;
    height: 22px;
    font-family: 'Noto Sans', sans-serif;
    font-style: normal;
    font-weight: 500;
    font-size: 16px;
    line-height: 22px;
    color: #FFFFFF;
  }
  
  .signup-link {
    color: #628EFF;
    cursor: pointer;
    transition: color 0.3s ease;
  }
  
  .signup-link:hover {
    color: #8740CD;
    text-decoration: underline;
  }
  
  .customer-care {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    padding: 4px 6px;
    gap: 31px;
    width: 400px;
    height: 30px;
    background: linear-gradient(180deg, rgba(98, 98, 98, 0) 0%, rgba(98, 98, 98, 0.07) 100%);
    border-radius: 6px;
  }
  
  .care-item {
    font-family: 'Noto Sans', sans-serif;
    font-style: normal;
    font-weight: 400;
    font-size: 16px;
    line-height: 22px;
    color: #FFFFFF;
  }
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .intro-text h1 {
      font-size: 2rem;
    }
    
    .intro-text p {
      font-size: 1rem;
    }
    
    .login-container {
      right: 50%;
      left: auto;
      transform: translateX(50%) translateY(-50%);
      width: 90%;
      max-width: 480px;
      height: auto;
      min-height: 700px;
    }
    
    .slide-in {
      transform: translateX(50%) translateY(-50%);
    }
    
    .login-content {
      position: relative;
      left: 0;
      top: 0;
      width: 90%;
      height: auto;
      padding: 40px 20px;
      margin: 0 auto;
    }
    
    .upper-section,
    .credentials,
    .input-container,
    .login-actions,
    .login-button,
    .other-logins,
    .or-divider,
    .signup-section,
    .customer-care {
      width: 100%;
    }
  }
  </style>