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
                  :src="currentDevice.imgSrc || require(`@/assets/ReportSystem/example.png`)"
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
              <li v-for="device in devices" :key="device.id" @click="selectDevice(device)">
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

</script>

<style scoped>
.
</style>