<template>
  <div class="report-system">

    <!-- 主要数据区域 -->
    <div class="data-section1">
      <div class="handling-situation">
        <div class="today-status">
          <div class="process-unit">
            <span class="text-processing">今日未处理</span>
            <div class="highlight-number number-not-process">{{ todayStats.notProcessed }}</div>
          </div>
          <div class="process-unit">
            <span class="text-processing">今日已处理</span>
            <div class="highlight-number number-have-process">{{ todayStats.processed }}</div>
          </div>
        </div>
        <!-- 设备故障统计卡片 -->
        <div class="stats-card">
          <span class="stats-title">监控设备故障</span>
          <table class="stats-table">
            <tr>
              <th>预警数量</th>
              <th>已处理</th>
              <th>未处理</th>
            </tr>
            <tr>
              <td class="number green">{{ totalStats.total }}</td>
              <td class="number green">{{ totalStats.processed }}</td>
              <td class="number orange">{{ totalStats.notProcessed }}</td>
            </tr>
          </table>
        </div>
      </div>
      <!-- 右侧统计卡片 -->
      <div class="week-stats-card">
        <div class="stats-header">
          <span class="stats-title2">本周监控信息</span>
          <div class="stats-numbers">
            <div class="this-week-info">{{ weekStats.total }} 预警数量</div>
            <div class="this-week-info medium">{{ weekStats.processed }} 已处理</div>
            <div class="this-week-info">{{ weekStats.notProcessed }} 未处理</div>
          </div>
          <BarChart :chartData="chartData"/>
        </div>
      </div>
    </div>

    <div class="data-section2">
      <!-- 设备控制区域 -->
      <div class="device-control">
        <span class="stats-title3">监控设备</span>

        <div class="device-options">
          <div class="row">
            <div class="device-actions">
              <span style="color: #68F0EB" class="btn">★</span>
              <div class="change btn" @click="showDeviceSelector = true">切换监控设备
              </div>
              <div class="output btn" @click="exportData">导出</div>
            </div>
            <span class="label">目前监控：</span>
          </div>
          <div class="row">
            <div class="image-container">
              <el-image
                  :src="currentDevice.imgSrc || require(`../assets/ReportSystem/example.png`)"
                  :preview-src-list="[currentDevice.imgSrc]"
                  fit="contain"
                  class="aspect-ratio-image"
                  :style="{ 'aspect-ratio': 16/5 }"
                  :alt="currentDevice.name">
              </el-image>
            </div>
            <span class="current-device">{{ currentDevice.name }}</span>
          </div>
        </div>

        <!-- 设备选择弹窗 -->
        <div v-if="showDeviceSelector" class="device-selector-modal">
          <div class="device-selector-content">
            <h3>选择监控设备</h3>
            <ul>
              <li v-for="device in devices" :key="device" @click="selectDevice(device)">
                {{ device.name }}
              </li>
            </ul>
            <button @click="showDeviceSelector = false">关闭</button>
          </div>
        </div>

        <div class="chart-section">
          <aero-engine-chart :device="currentDevice"></aero-engine-chart>
        </div>
      </div>

      <!-- 趋势图表 -->
      <div class="trend-section">
        <span class="stats-title2">本周监控趋势</span>
        <div class="trend-chart">
          <LineChart :chartData="chartData"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import BarChart from "@/components/BarChart.vue";
import LineChart from "@/components/LineChart.vue";
import AeroEngineChart from "@/components/CircularDiagram.vue";

const api = axios.create({
  baseURL: process.env.VUE_APP_API_URL || 'https://af1f2aee-0858-4e5f-8a9e-6e279126c69d.mock.pstmn.io/api',
  timeout: 10000
})
export default {
  name: 'ReportSystem',
  components: {AeroEngineChart, LineChart, BarChart},
  data() {
    return {
      // 初始空数据
      todayStats: {
        processed: 0,
        notProcessed: 0
      },
      totalStats: {
        total: 0,
        processed: 0,
        notProcessed: 0
      },
      weekStats: {
        total: 0,
        processed: 0,
        notProcessed: 0
      },
      devices: [],
      currentDevice: {
        id: "1234",
        name: "加载中..."
      },
      showDeviceSelector: false,
      deviceData: {},
      weekBarChartData: {},
      chartData: {
        xAxis: ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'],
        series: [
          {
            name: '已处理',
            data: [32, 22, 30, 40, 21, 20, 3]
          },
          {
            name: '未处理',
            data: [12, 5, 7, 3, 2, 1, 0]
          }
        ]
      },

    }
  },
  created() {
    // 在组件创建时加载所有数据
    this.fetchTodayStats();
    this.fetchTotalStats();
    this.fetchWeekStats();
    this.fetchDevices();
    this.fetchWeekTrend();
  },
  methods: {
    // 获取今日处理情况
    async fetchTodayStats() {
      try {
        const response = await api.get(`/stats/today`);
        this.todayStats = response.data;
      } catch (error) {
        console.error('获取今日数据失败:', error);
        // 使用默认数据
        this.todayStats = {
          processed: 3,
          notProcessed: 3
        };
      }
    },

    // 获取总体统计数据
    async fetchTotalStats() {
      try {
        const response = await api.get(`/stats/total`);
        this.totalStats = response.data;
      } catch (error) {
        console.error('获取总体统计数据失败:', error);
        // 使用默认数据
        this.totalStats = {
          total: 730,
          processed: 623,
          notProcessed: 97
        };
      }
    },

    // 获取本周统计数据
    async fetchWeekStats() {
      try {
        const response = await api.get(`/stats/week`);
        this.weekStats = response.data;

        // 更新周统计柱状图数据
        this.weekBarChartData = {
          labels: ['预警数量', '已处理', '未处理'],
          datasets: [
            {
              data: [
                this.weekStats.total,
                this.weekStats.processed,
                this.weekStats.notProcessed
              ]
            }
          ]
        };
      } catch (error) {
        console.error('获取周统计数据失败:', error);
        // 使用默认数据
        this.weekStats = {
          total: 623,
          processed: 623,
          notProcessed: 97
        };
      }
    },

    // 获取设备列表
    async fetchDevices() {
      try {
        const response = await api.get(`/devices`);
        this.devices = response.data.devices;
        // 默认选择第一个设备
        if (this.devices.length > 0) {
          this.currentDevice = this.devices[0];
          console.log(this.currentDevice);
        }
      } catch (error) {
        console.error('获取设备列表失败:', error);
        // 使用默认数据
        this.devices = [
          {
            "id": "#1534",
            "name": "航空发动机A1",
            "imgSrc": "../assets/ReportSystem/example.png"
          },
          {
            "id": "#1535",
            "name": "航空发动机A2",
            "imgSrc": "../assets/ReportSystem/example.png"
          }, {
            "id": "#1536",
            "name": "航空发动机A3",
            "imgSrc": "../assets/ReportSystem/example.png"
          }, {
            "id": "#1537",
            "name": "航空发动机A4",
            "imgSrc": "../assets/ReportSystem/example.png"
          }
        ];
        this.currentDevice = {
          "id": "#1534",
          "name": "航空发动机A1"
        };
      }
    },

    // 获取周趋势数据
    async fetchWeekTrend() {
      try {
        const response = await api.get(`/stats/week-trend`);
        const data = response.data;

        this.chartData = {
          xAxis: data.days || ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'],
          series: [
            {
              name: '已处理',
              data: data.processed || [0, 0, 0, 0, 0, 0, 0]
            },
            {
              name: '未处理',
              data: data.notProcessed || [0, 0, 0, 0, 0, 0, 0]
            }
          ]
        };

      } catch (error) {
        console.error('获取周趋势数据失败:', error);
        // 使用默认数据
        this.chartData = {
          xAxis: ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'],
          series: [
            {
              name: '已处理',
              data: [32, 22, 30, 40, 21, 20, 3]
            },
            {
              name: '未处理',
              data: [12, 5, 7, 3, 2, 1, 0]
            }
          ]
        };
      }
    },

    // 选择设备
    selectDevice(device) {
      this.currentDevice = device;
      this.showDeviceSelector = false;
    },

    // 导出数据功能
    async exportData() {
      const id = this.currentDevice.id;
      const name = this.currentDevice.name;
      // 使用axios发送请求下载文件
      api.get('/device',{
        params: {deviceId: id},
        responseType: 'blob'  // 指定响应类型为blob
      })
          .then(response => {
            // 创建一个Blob对象
            const blob = new Blob([response.data], {type: response.headers['content-type']});

            // 创建一个下载链接
            const downloadLink = document.createElement('a');

            // 创建一个URL对象
            const url = window.URL.createObjectURL(blob);

            // 设置下载链接属性
            downloadLink.href = url;
            downloadLink.download = `device-${id}-${name}-export.xlsx`;

            // 添加链接到页面，触发点击，然后移除
            document.body.appendChild(downloadLink);
            downloadLink.click();
            document.body.removeChild(downloadLink);

            // 释放URL对象
            window.URL.revokeObjectURL(url);
          })
          .catch(error => {
            console.error('导出数据失败:', error);
            this.$message.error('导出数据失败，请稍后重试');
          });
    }
  }
}
</script>

<style scoped>
.report-system {
  --bgcolor: #0B1739;
  --my-gap: 12px;
  --my-mb: 12px;
  height: 100%;
}


.data-section1 {
  display: grid;
  grid-template-columns: 2fr 3fr;
  gap: var(--my-gap);
  margin-bottom: var(--my-mb);
  height: 45%;
}

.data-section2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--my-gap);
  margin-bottom: var(--my-mb);
  height: calc(55% - var(--my-mb));
}

.handling-situation {
  display: inline-block;
  justify-content: space-between;

}

.today-status {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--my-gap);
  height: 40%;
  margin-bottom: var(--my-mb);
}

.stats-card {
  height: calc(60% - var(--my-mb));
  background: var(--bgcolor);
  background-image: url("@/assets/ReportSystem/box2.svg");
  background-size: 100% auto;
  background-repeat: no-repeat;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.week-stats-card {
  display: inline-block;
  background: var(--bgcolor);
  background-image: url("@/assets/ReportSystem/box3.svg");
  background-size: 100% 100%;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

}

.device-control {
  background: var(--bgcolor);
  border-radius: 8px;
  background-image: url("@/assets/ReportSystem/box4.svg");
  background-size: 100% 100%;
  position: relative;
}

.device-header {
  justify-content: space-between;
  align-items: center;
}

.device-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--my-gap);
}

.trend-section {
  background: var(--bgcolor);
  background-image: url("@/assets/ReportSystem/box5.svg");
  background-size: 100% 100%;
  border-radius: 12px;
}

.week-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: var(--my-gap);
  margin: 15px 0;
}

.process-unit {
  background: var(--bgcolor);
  background-image: url("@/assets/ReportSystem/box1.png");
  background-size: 100% 100%;
  border-radius: 10px;
}


/*文字效果*/
.text-processing {

  font-family: Work Sans, serif;
  font-weight: 500;
  font-size: calc(1.5vw + 0.3vh);
  height: 5rem;
  line-height: 4rem;
  letter-spacing: 0%;
  color: #AEB9E1;
}

/*数字效果*/

.highlight-number {
  height: 1rem;
  line-height: 1rem;
  font-family: Work Sans, cursive;
  font-weight: 900;
  font-size: calc(1.3vw + 2vh);

  letter-spacing: 0px;
}

.number-not-process {
  color: #FDB52A;

}

.number-have-process {
  color: #14CA74;
}

.stats-title {
  font-family: Work Sans, cursive;
  font-weight: 100;
  font-size: calc(0.8vw + 1vh);
  line-height: 3rem;
  height: 3rem;
  letter-spacing: 0%;
  color: #90FFDC;

}

.stats-title2 {
  line-height: 2rem;
  height: 2rem;
  font-family: Work Sans, serif;
  font-weight: 500;
  font-size: calc(0.8vw + 1vh);
  letter-spacing: 0%;
  color: #68F0EB;
}

.stats-title3 {
  line-height: 2.6rem;
  height: 2.6rem;
  font-family: Work Sans, serif;
  font-weight: 500;
  font-size: calc(0.8vw + 1vh);

  letter-spacing: 0%;
  color: #68F0EB;

}

.stats-numbers {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 10px;
  border-top: 1px solid white;
  border-bottom: 1px solid white;
}

.stats-numbers .this-week-info {
  color: #90FFDC;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  font-size: calc(0.8vw + 1vh);
  font-weight: bold;
}

.stats-numbers .medium {
  border-left: 1px solid white;
  border-right: 1px solid white;
  width: 33%; /* 让中间的占 1/3 宽度 */
  flex: none;
}


.stats-table {
  width: 90%;
  height: 68%;
  margin: auto;
  border-collapse: collapse;
  color: #90FFDC;
  text-align: center;
  font-size: calc(0.8vw + 1vh);
  font-family: Work Sans, cursive;

}

.stats-table th, .stats-table td {
  border: 1px solid white;
  width: 33.33%;
}

.stats-table td {
  padding: 1px;
}

.stats-table th {
  padding: 5px;

}

/* 让最左列和最右列不显示边框 */
.stats-table th:first-child,
.stats-table td:first-child {
  border-left: none;
}

.stats-table th:last-child,
.stats-table td:last-child {
  border-right: none;
}

/* 调整第二行（数据行）高度 */
.stats-table tr:nth-child(1) td {
  min-height: 20%; /* 你可以调整这个值 */
}

.stats-table tr:nth-child(2) td {
  height: 70%; /* 你可以调整这个值 */
}

.stats-table .number {
  font-size: calc(1vw + 1.3vh);
  font-weight: bold;
}

.stats-table .green {
  color: #00FF99;
}

.stats-table .orange {
  color: #FF9933;
}

.device-options {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-left: 10%;
}

.row {
  display: flex;
  gap: 10px; /* 控制行内元素间距 */
  align-items: center;
}


.device-actions {
  display: flex;
  width: 50%;
}

.device-actions .btn {
  padding: 8px;
  cursor: pointer;
}

.device-actions .change {
  background-color: #081028;
  background-image: url("@/assets/ReportSystem/btn1.svg");
  background-size: 100% auto;
  background-repeat: no-repeat;
  color: #68F0EB;
  width: 70%;
  text-align: left;
  border-radius: 8px;
  font-size: 1em;
  word-break:keep-all;
  overflow:hidden;
}

.device-actions .output {
  background-color: #68F0EB;
  color: #7D30CA;
  border-radius: 6px;
  width: 10%;
  font-size: 1em;
  word-break:keep-all;
  overflow:hidden;
}

.device-options .current-device {
  display: flex;
  color: #68F0EB;
  background-color: #D9D9D91A;
  padding: 20px 15px;
  font-size: calc(1vw + 1.5vh);
  border-radius: 8px;
  margin: 0 auto;
}

.device-options .label {
  color: #68F0EB;
  font-family: Work Sans, serif;
  font-size: calc(1vw + 1.5vh);
  margin: 0 auto;
}

/* 设备选择器弹窗样式 */
.device-selector-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.device-selector-content {
  background-color: #0B1739;
  border: 2px solid #68F0EB;
  border-radius: 10px;
  padding: 20px;
  width: 400px;
  max-height: 80vh;
  overflow-y: auto;
}

.device-selector-content h3 {
  color: #68F0EB;
  margin-bottom: 15px;
  text-align: center;
}

.device-selector-content ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.device-selector-content li {
  color: #90FFDC;
  padding: 10px 15px;
  margin-bottom: 8px;
  background-color: rgba(104, 240, 235, 0.1);
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.device-selector-content li:hover {
  background-color: rgba(104, 240, 235, 0.3);
}

.device-selector-content button {
  display: block;
  margin: 15px auto 0;
  padding: 8px 20px;
  background-color: #68F0EB;
  color: #0B1739;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
}

.device-selector-content button:hover {
  background-color: #90FFDC;
}

.image-container {
  width: 50%;
  max-width: 350px;

}

.aspect-ratio-image {
  width: 100%;
  height: auto;
  cursor: pointer;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>