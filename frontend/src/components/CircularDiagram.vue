<template>
  <div ref="chartContainer" class="chart-container"></div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

const api = axios.create({
  baseURL: process.env.VUE_APP_API_URL || 'https://af1f2aee-0858-4e5f-8a9e-6e279126c69d.mock.pstmn.io/api',
  timeout: 10000
})
export default {
  name: 'AeroEngineChart',
  props: {
    device: {
      type: Object,
      default: () => ({
        id: '#1111',
        name: '航空发动机 A1'
      })
    }
  },
  data() {
    return {
      chart: null,
      chartData: [],
      isLoading: false,
      errorMessage: ''
    }
  },
  mounted() {
    this.fetchComponentData();
    window.addEventListener('resize', this.handleResize);
  },
  watch: {
    // 监听设备ID变化，重新获取数据
    'device.id': {
      handler(newVal) {
        if (newVal) {
          this.fetchComponentData();
        }
      }
    }
  },
  beforeUnmount() {
    if (this.chart) {
      this.chart.dispose();
    }
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    handleResize() {
      this.chart && this.chart.resize()
    },

    // 向后端发起请求获取组件数据
    async fetchComponentData() {
      this.isLoading = true;
      this.errorMessage = '';

      try {
        // 使用axios发起GET请求，传递设备ID作为参数
        const response = await api.get('/device/components', {
          params: {
            deviceId: this.device.id
          }
        });

        // 假设后端返回的数据格式为 { total: number, components: Array<{name: string, value: number}> }
        if (response.data && response.data.components) {
          this.chartData = response.data.components;
          const totalValue = response.data.total || this.chartData.reduce((sum, item) => sum + item.value, 0);
          // 获取数据后初始化或更新图表
          if (this.chart) {
            this.updateChart(totalValue);
          } else {
            this.initChart(totalValue);
          }
        }
      } catch (error) {
        console.error('获取组件数据失败:', error);
        this.errorMessage = '获取数据失败，请稍后重试';
      } finally {
        this.isLoading = false;
      }
    },

    // 初始化图表
    initChart(totalValue) {
      this.chart = echarts.init(this.$refs.chartContainer);
      this.updateChart(totalValue);
    },
    getRandomHexColor() {
      // 生成格式为 #RRGGBB 的随机颜色
      return '#' + Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0');
    },
    // 更新图表数据
    updateChart(totalValue) {
      if (!this.chart) return;

      // 为每个组件数据添加样式信息
      const colorMap = {
        '叶片': '#8B5CF6',
        '燃料器': '#F87171',
        '风机': '#67E8F9',
        '旁通管道': '#FCD34D',
        'LPC': '#60A5FA',
        'HPT': '#4ADE80',
        'HPC': '#A78BFA'
      };

      const formattedData = this.chartData.map((item) => {
        const color = colorMap[item.name] || this.getRandomHexColor();

        return {
          value: item.value,
          name: item.name,
          itemStyle: {color},
          label: {
            show: true,
            position: 'outside',
            color: '#fff',
            formatter: item.value.toString(),
          },
          labelLine: {
            show: true,
            lineStyle: {
              color
            }
          }
        };
      });

      const option = {
        title: {
          text: totalValue.toString(),
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#fff',
            fontSize: 24,
            fontWeight: 'bold'
          }
        },
        grid: {
          left: '3%',
          right: '20%',
          bottom: '7%',
          top: '4%',
          containLabel: true
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          right: "right",
          bottom: 'center',
          textStyle: {color: '#eee'},
          itemHeight: 10,
          itemWidth: 10,
          icon: 'circle',
        },
        series: [
          {
            type: 'pie',
            radius: ['30%', '80%'],
            avoidLabelOverlap: true,
            label: {
              show: true,
              position: 'outside',
              formatter: '{value}',
              color: '#fff',
              fontSize: 14,
              lineHeight: 20,
              alignTo: 'edge',
              edgeDistance: '15%',
              distanceToLabelLine: 5
            },
            labelLine: {
              show: true,
              length: 4,
              length2: 100,
              smooth: false,
              lineStyle: {
                width: 1,
                type: 'solid',
                color: '#fff'
              }
            },
            data: formattedData,
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              }
            }
          }
        ]
      };

      this.chart.setOption(option);
    }
  }
}
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 200px;
}
</style>