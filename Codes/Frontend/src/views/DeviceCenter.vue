<template>
  <div class="device-center">
    <div class="main-container">
      <!-- 左侧容器 -->
      <div class="left-container">
        <!-- 数据卡片行 - 设备数和测点数 -->
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-label">设备数</div>
            <div class="stat-value">{{ deviceCount }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">测点数</div>
            <div class="stat-value">{{ measurePointCount }}</div>
          </div>
        </div>

        <!-- 设备运行状态卡片 -->
        <div class="status-card">
          <h3 class="card-title">设备运行状态</h3>
          <div class="status-chart">
            <div class="status-item" v-for="(item, index) in deviceStatus" :key="index">
              <div class="status-dot" :style="{backgroundColor: item.color}"></div>
              <div class="status-name">{{ item.name }}</div>
              <div class="status-count">{{ item.count }}</div>
            </div>
          </div>
        </div>

        <!-- 缺陷设备卡片 -->
        <div class="defect-card">
          <h3 class="card-title">缺陷设备</h3>
          <div class="defect-list">
            <div class="defect-item" v-for="(defect, index) in defectDevices" :key="index">
              <div class="defect-icon">
                <i class="fas fa-exclamation-triangle"></i>
              </div>
              <div class="defect-info">
                <div class="defect-name">{{ defect.name }}</div>
                <div class="defect-time">{{ defect.time }}</div>
              </div>
              <div class="defect-level" :class="'level-' + defect.level">
                {{ defect.levelText }}
              </div>
            </div>
          </div>
        </div>

        <!-- 预警状态分布卡片 -->
        <div class="warning-card">
          <h3 class="card-title">预警状态分布</h3>
          <div class="warning-chart">
            <div class="chart-placeholder">
              <div class="chart-segment" v-for="(segment, index) in warningDistribution" :key="index"
                  :style="{
                    backgroundColor: segment.color,
                    width: segment.percentage + '%'
                  }">
                <span class="segment-label">{{ segment.name }}: {{ segment.percentage }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧容器 -->
      <div class="right-container">
        <h2 class="section-title">设备详情</h2>
        
        <!-- 顶部功能栏 -->
        <div class="function-row">
          <div class="function-card">
            <div class="function-icon">
              <i class="fas fa-bell"></i>
            </div>
            <div class="function-info">
              <div class="function-label">待处理警报</div>
              <div class="function-value">{{ pendingAlarms }}</div>
            </div>
          </div>
          

          
          <div class="search-container">
            <input 
              type="text" 
              class="search-input" 
              placeholder="搜索设备..." 
              v-model="searchQuery"
              @input="searchDevices"
            >
            <button class="search-button">
              <i class="fas fa-search"></i>
            </button>
          </div>
        </div>
        
        <!-- 设备卡片网格 -->
        <div class="device-grid" ref="deviceContainer">
          <div class="device-card" v-for="(device, index) in displayedDevices" :key="index">
            <div class="center-device-image">
              <img :src="device.image || 'https://placehold.co/200x150?text=设备图片'" alt="设备图片">
            </div>
            <div class="device-info">
              <div class="center-device-name">{{ device.name }}</div>
              <div class="device-status" :class="'status-' + device.status.toLowerCase()">
                {{ device.statusText }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分页控制 -->
        <div class="pagination-controls" v-if="devices.length > 9">
          <button 
            class="page-button" 
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >
            <i class="fas fa-chevron-left"></i>
          </button>
          
          <div class="page-indicator">
            {{ currentPage }} / {{ totalPages }}
          </div>
          
          <button 
            class="page-button" 
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DeviceCenter',
  data() {
    return {
      // 设备和测点统计
      deviceCount: 186,
      measurePointCount: 1548,
      
      // 设备状态数据
      deviceStatus: [
        { name: '正常', count: 142, color: '#34C759' },
        { name: '预警', count: 24, color: '#FF9500' },
        { name: '故障', count: 8, color: '#FF3B30' },
      ],
      
      // 缺陷设备数据
      defectDevices: [
        { name: '空压机01', time: '2025-03-15', level: 3, levelText: '严重' },
        { name: '水泵系统A', time: '2025-03-16', level: 2, levelText: '中等' },
        { name: '电机控制器', time: '2025-03-17', level: 1, levelText: '轻微' },
        { name: '电机控制器', time: '2025-03-17', level: 1, levelText: '轻微' },
        { name: '电机控制器', time: '2025-03-17', level: 1, levelText: '轻微' },
        { name: '电机控制器', time: '2025-03-17', level: 1, levelText: '轻微' },
      ],
      
      // 预警状态分布
      warningDistribution: [
        { name: '一般提醒', percentage: 45, color: '#5856D6' },
        { name: '维修', percentage: 30, color: '#FF9500' },
        { name: '警告', percentage: 25, color: '#FF2D55' }
      ],
      
      // 功能区数据
      pendingAlarms: 14,
      pendingDefects: 7,
      searchQuery: '',
      
      // 设备数据
      devices: [
        { id: 1, name: '发动机叶片A-1', status: 'normal', statusText: '正常', image: require('@/assets/DeviceCenter/ex/A.png') },
        { id: 2, name: '发动机叶片A-2', status: 'warning', statusText: '预警', image: require('@/assets/DeviceCenter/ex/A.png') },
        { id: 3, name: '发动机叶片B-1', status: 'normal', statusText: '正常', image: require('@/assets/DeviceCenter/ex/B.png')},
        { id: 4, name: '发动机叶片B-2', status: 'error', statusText: '故障', image: require('@/assets/DeviceCenter/ex/B.png') },
        { id: 5, name: '发动机叶片C-1', status: 'normal', statusText: '正常', image: require('@/assets/DeviceCenter/ex/C.png') },
        { id: 6, name: '发动机叶片C-2', status: 'error', statusText: '故障', image: require('@/assets/DeviceCenter/ex/C.png') },
        { id: 7, name: '发动机叶片C-3', status: 'normal', statusText: '正常', image: require('@/assets/DeviceCenter/ex/C.png') },
        { id: 8, name: '发动机轴承C-1', status: 'normal', statusText: '正常', image: require('@/assets/DeviceCenter/ex/C-1.png') },
        { id: 9, name: '发动机轴承C-2', status: 'warning', statusText: '预警', image: require('@/assets/DeviceCenter/ex/C-2.png') },
        { id: 10, name: '发动机轴承C-3', status: 'normal', statusText: '正常', image: require('@/assets/DeviceCenter/ex/C-3.png') },
      ],
      
      // 分页控制
      currentPage: 1,
      itemsPerPage: 9
    };
  },
  computed: {
    // 过滤并分页显示设备
    displayedDevices() {
      let filtered = this.devices;
      
      // 搜索过滤
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(device => 
          device.name.toLowerCase().includes(query)
        );
      }
      
      // 计算当前页显示的设备
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      
      return filtered.slice(start, end);
    },
    
    // 计算总页数
    totalPages() {
      let filtered = this.devices;
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(device => 
          device.name.toLowerCase().includes(query)
        );
      }
      return Math.ceil(filtered.length / this.itemsPerPage);
    }
  },
  methods: {
    // 搜索设备
    searchDevices() {
      this.currentPage = 1; // 重置到第一页
    },
    
    // 切换页面
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    }
  }
}
</script>

<style scoped>
.device-center {
  color: #ffffff;
  width: 100%;
  padding: 1.5rem;
  box-sizing: border-box;
}

.main-container {
  display: flex;
  gap: 2%;
  padding-bottom: 2rem; 
}

/* 左侧容器样式 */
.left-container {
  width: 35%;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 右侧容器样式 */
.right-container {
  width: 63%;
  display: flex;
  flex-direction: column;
  background-image: url('@/assets/DeviceCenter/device-card.svg');
  background-size: 100% 100%; 
  background-repeat: no-repeat; 
  background-position: center; 
}

/* 统计卡片行 */
.stats-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.stat-card {
  flex: 1;
  background-image: url('@/assets/DeviceCenter/count-card.svg');
  border-radius: 10px;
  backdrop-filter: blur(10px);
  text-align: center;
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
  width:263px;
  height:139px;
}

.stat-value {
  font-size: 2.2rem;
  font-weight: 700;
  color: #A7B3FF;
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 40px;
}

.stat-label {
  margin-top: 20px;
  font-size: 20px;
  color: #AEB9E1;
}



/* 状态卡片 */
.status-card, .defect-card, .warning-card {
  background: #0B1739;
  border-radius: 10px;
  padding: 1.2rem;
  backdrop-filter: blur(10px);
}

.card-title {
  font-size: 1.2rem;
  margin-bottom: 1rem;
  color: #AEB9E1;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.status-chart {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.status-name {
  flex: 1;
  font-size: 0.95rem;
  color:#90FFDC;
}

.status-count {
  font-size: 1.1rem;
  font-weight: 600;
  color:#90FFDC;
}

/* 缺陷列表 */
.defect-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  overflow-y: auto; /* 启用垂直滚动 */
  height: calc(3 * (1.2rem + 0.6rem * 2 + 0.3rem + 1.6rem)); /* 计算3个项目的高度 */
  padding-right: 0.5rem; /* 为滚动条留出空间 */
}

/* 定制滚动条样式 */
.defect-list::-webkit-scrollbar {
  width: 4px;
}

.defect-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.defect-list::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

.defect-item {
  display: flex;
  align-items: center;
  padding: 0.6rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  flex-shrink: 0; /* 防止项目被压缩 */
}

.defect-icon {
  color: #FF9500;
  margin-right: 0.8rem;
  font-size: 1.2rem;
}

.defect-info {
  flex: 1;
}

.defect-name {
  font-size: 0.95rem;
  margin-bottom: 0.3rem;
  color:#90FFDC;
}

.defect-time {
  font-size: 0.8rem;
  color: #AEB9E1;
}

.defect-level {
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  white-space: nowrap; /* 防止级别标签换行 */
}

.level-1 {
  background-color: rgba(255, 204, 0, 0.2);
  color: #FFCC00;
}

.level-2 {
  background-color: rgba(255, 149, 0, 0.2);
  color: #FF9500;
}

.level-3 {
  background-color: rgba(255, 59, 48, 0.2);
  color: #FF3B30;
}

/* 预警图表 */
.warning-chart {
  padding: 0.5rem 0;
}

.chart-placeholder {
  display: flex;
  height: 30px;
  width: 100%;
  border-radius: 6px;
  overflow: hidden;
}

.chart-segment {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: width 0.3s ease;
}

.segment-label {
  font-size: 0.8rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color:#90FFDC;
  padding: 0 8px;
}

/* 右侧标题 */
.section-title {
  font-size: 30px;
  margin-bottom: 1.2rem;
  font-weight: 600;
  text-align: center;
  color: #68F0EB;
}

/* 功能行 */
.function-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.function-card {
  display: flex;
  align-items: center;
  border-radius: 10px;
  padding: 0.8rem 1.2rem;
  flex: 1;
}

.function-icon {
  font-size: 1.5rem;
  margin-right: 0.8rem;
  color: #628EFF;
}

.function-info {
  flex: 1;
  display: flex;
  align-items: center;
}

.function-label {
  font-size: 20px;
  color: #AEB9E1;
  margin-bottom: 0.3rem;
  color:#68F0EB;
  margin-right: 2.0rem;
}

.function-value {
  font-size: 20px;
  font-weight: 500;
  color:rgb(76, 253, 182);
}

/* 搜索框 */
.search-container {
  position: relative;
  flex: 1.5;
}

.search-input {
  width: 80%;
  padding: 0.8rem 1rem;
  padding-right: 3rem;
  border: none;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  font-size: 1rem;
}

.search-input::placeholder {
  color: #AEB9E1;
}

.search-button {
  position: absolute;
  right: 0.8rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #AEB9E1;
  font-size: 1.2rem;
  cursor: pointer;
}

/* 设备卡片网格 */
.device-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, minmax(230px, 1fr)); /* 固定为3行，每行最小高度230px */
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  height: calc(3 * 230px + 2 * 1.5rem); 
}

/* 设备卡片 */
.device-card {
  margin-left: 0.5rem;
  margin-right: 0.5rem;
  background: #412F74;
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  height: 100%; /* 让卡片占满格子空间 */
}

.device-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.center-device-image {
  width: 100%;
  height: 150px; /* 固定图片高度 */
  overflow: hidden;
  flex-shrink: 0; /* 防止图片区域被压缩 */
}

.center-device-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-fit: contain;
}

.device-info {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  flex-grow: 1; /* 让信息区域占用剩余空间 */
  justify-content: space-between; /* 在垂直方向均匀分布内容 */
  text-align: center;
  align-items: center;
}

.center-device-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.8rem; /* 增加与状态的间距 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color:#68F0EB;
}

.device-status {
  display: inline-block;
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}
.status-normal {
  background-color: rgba(52, 199, 89, 0.2);
  color: #34C759;
}

.status-warning {
  background-color: rgba(255, 149, 0, 0.2);
  color: #FF9500;
}

.status-error {
  background-color: rgba(255, 59, 48, 0.2);
  color: #FF3B30;
}

.status-offline {
  background-color: rgba(142, 142, 147, 0.2);
  color: #8E8E93;
}


/* 分页控制 - 确保定位正确 */
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
  margin-bottom: 1rem; /* 确保底部有足够空间 */
}

.page-button {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}

.page-button:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
}

.page-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-indicator {
  font-size: 0.9rem;
  color: #AEB9E1;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .device-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .main-container {
    flex-direction: column;
    gap: 2rem;
  }
  
  .left-container,
  .right-container {
    width: 100%;
  }
  
  .device-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .function-row {
    flex-direction: column;
  }
  
  .device-grid {
    grid-template-columns: 1fr;
  }
}
</style>