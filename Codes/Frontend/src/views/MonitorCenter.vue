<template>
  <div class="monitor-center">
    <div class="top-section">
      <!-- 上半部分内容 -->
      <div class="sub-section">
        <div class="svg-container svg-left">
          <div class="device-name">
            {{deviceName}}
          </div>
          <img :src="deviceImage" alt="设备图片" class="device-image">
          </div>

      </div>

      <div class="sub-section mid">
        <div class="svg-container svg-mid">
          <!-- 统计图容器 -->
          <div class="canvas-container" style="margin-top:10%">
            <canvas ref="healthyCanvas" class="line-canvas"></canvas>
          </div>
        </div>
      </div>

      <div class="sub-section right">
        <div class="svg-container svg-right">
          <div class="energy-cost">{{energyCost}} </div>
          <div class="canvas-container" style="margin-top:2%">
            <canvas ref="energyCanvas" class="line-canvas"></canvas>
          </div>
        </div>
      </div>
    </div>

    <div class="bottom-section">
      <!-- 下半部分内容 -->
      <div class="bottom-container">
        <!-- 分页展示卡片 -->
        <div v-for="(row, rowIndex) in visibleRows" :key="rowIndex" class="row-container">
          <div v-for="(item, colIndex) in row" :key="colIndex" class="card">
            <div class="card-body">
              <div style="flex:0.2;flex-direction: row;display:flex;">
                <img src="@/assets/MonitorCenter/Gear.svg" alt="Gear" class="gear" style="width: 1vw;" >
                <span class="card-title" style="text-align: center;flex:auto;font-family: 'Work Sans', sans-serif;color:#23FFC4;font-size:1vw">{{ item.name }}</span>
              </div>
              <div style="flex:0.6;justify-content: center;align-content: center;margin-left:1vw;">
                <span class="card-value zendots-font" style="font-size:2vw;">{{ item.value }}</span>
                <span class="card-unit zendots-font" style="font-size:1vw;">{{ item.unit }}</span>
              </div>
            </div>
            <div class="card-healthy" v-if="item.health!==-1">
              <div style="text-align: center;flex:0.2;font-size:0.8vw;font-family: 'Work Sans', sans-serif;color:#23FFC4" >健康度</div>
              <div class="health-chart-container">
                <canvas :ref="el => setHealthChartRef(el, item.health)" class="health-chart"></canvas>
              </div>
            </div>
          </div>
        </div>

        <!-- 翻页控件 -->
        <div class="pagination-controls" v-if="totalPages > 1">
          <button @click="prevPage" :disabled="currentPage === 1" style="background-color: transparent; color:#55F0BD">◄</button>
          <span style="color:#55F0BD;margin:1%">{{ currentPage }} / {{ totalPages }}</span>
          <button @click="nextPage" :disabled="currentPage === totalPages " style="background-color: transparent; color:#55F0BD">►</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { drawChart } from '@/utils/drawChart'; // 引入绘制图表的脚本
import { drawHealthChart } from "@/utils/drawHealthChart";
import { getDeviceHealthData, getDeviceEnergyData, getDeviceMetricCards, getDeviceById } from '@/api/monitorCenterApi'; // 修改为正确的导入
import { useRoute } from 'vue-router'

export default {
  name: 'MoniterCenter',
  setup() {
    const healthyCanvas = ref(null); // 引用 canvas 元素
    const energyCanvas = ref(null);
    const healthCharts = ref({});

    //获取设备信息
    const route = useRoute()
    const deviceId = route.params.deviceId
    console.log('当前设备ID:', deviceId)
    const deviceName = ref("航空发动机"); // 改为ref，方便后续异步更新
    const deviceImage = ref(""); // 改为ref

    //默认能耗成本
    const energyCost = ref(48170);

    // 图表数据
    const healthyData = ref({
      labels: [1,2,3,4,5,6,7], // X 轴标签
      datasets: [
        {
          label: '健康度',
          data: [95, 93, 91, 92, 88, 87, 85], // Y 轴数据
          backgroundColor: 'rgb(0, 243, 243)'
        },
      ],
    });

    const energyData = ref({
      labels: [1,2,3,4,5,6,7], // X 轴标签
      datasets: [
        {
          label: '能耗实时曲线',
          data: [6800, 7200, 8500, 7900, 8300, 6500, 5200], // Y 轴数据
          backgroundColor: 'rgb(85, 240, 189)'
        },
      ],
    });

    // 模拟传入的字典数据
    const items = ref([
      { name: '设备1', value: 98, unit: '%', health: -1 },
      { name: '设备2', value: 85, unit: '%', health: 80 },
      { name: '设备3', value: 90, unit: '%', health: 88 },
      { name: '设备4', value: 92, unit: '%', health: 91 },
      { name: '设备5', value: 87, unit: '%', health: 84 },
      { name: '设备6', value: 89, unit: '%', health: 86 },
      { name: '设备7', value: 94, unit: '%', health: 90 },
    ]);

    const itemsPerPage = 12; // 每页最多显示12个卡片（3行 x 4列）
    const currentPage = ref(1);

    // 计算总页数
    const totalPages = ref(1);

    // 将 visibleItems 分成三行
    const visibleRows = computed(() => {
      const rows = [];
      for (let i = 0; i < items.value.length; i += 4) {
        rows.push(items.value.slice(i, i + 4));
      }
      return rows;
    });

    // 翻页逻辑
    const prevPage = async () => {
      if (currentPage.value > 1) {
        currentPage.value--;
        clearHealthCharts();
        await fetchCardData();
      }
    };

    const nextPage = async () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
        clearHealthCharts();
        await fetchCardData();
      }
    };

    // 获取卡片数据的方法
    const fetchCardData = async () => {
      try {
        const response = await getDeviceMetricCards(deviceId, currentPage.value, itemsPerPage);
        if (response.data.success) {
          console.log("设备卡片信息",response);
          items.value = response.data.items;
          totalPages.value = response.data.totalPages;
        }
      } catch (error) {
        console.error('获取设备指标卡片数据失败:', error);
      }
    };

    const setHealthChartRef = (el, health) => {
      if (el) {
        if(health>=0){
          drawHealthChart(el, health);
        }
      }
    };

    const clearHealthCharts = () => {
      const canvases = document.querySelectorAll('.health-chart');
      canvases.forEach(canvas => {
        if (canvas && canvas.chart) {
          canvas.chart.destroy(); // 销毁 Chart 实例
        }
      });
    };

    // 在组件挂载后获取数据并绘制图表
    onMounted(async () => {
      try {
        // 获取设备基本信息
        const deviceInfo = await getDeviceById(deviceId);

        if (deviceInfo.data.success) {
          deviceName.value = deviceInfo.data.deviceName;
          deviceImage.value = deviceInfo.data.picture;
        }
        else {
          console.log('设备信息获取失败:', deviceInfo);
        }

        // 获取健康数据
        const healthResponse = await getDeviceHealthData(deviceId);
        if (healthResponse.data.success) {
          // 使用最近7天数据，只取值，标签用1-7
          healthyData.value.datasets[0].data = healthResponse.data.values;

          // 如果需要绘制图表
          if (healthyCanvas.value) {
            drawChart(healthyCanvas.value, healthyData.value);
          }
        }

        // 获取能耗数据
        const energyResponse = await getDeviceEnergyData(deviceId);
        if (energyResponse.data.success) {
          energyData.value.datasets[0].data = energyResponse.data.values;
          energyCost.value = energyResponse.data.energyCost;

          // 如果需要绘制图表
          if (energyCanvas.value) {
            drawChart(energyCanvas.value, energyData.value);
          }
        }

        // 获取卡片信息
        await fetchCardData();

      } catch (error) {
        console.error('获取设备数据失败:', error);
      }
    });

    return {
      deviceName,
      deviceImage,
      healthCharts,
      healthyCanvas,
      energyCanvas,
      items,
      visibleRows,
      currentPage,
      totalPages,
      prevPage,
      nextPage,
      setHealthChartRef,
      energyCost,
    };
  },
};
</script>

<style scoped>
@import '@/assets/MonitorCenter/MonitorStyles.css';
</style>