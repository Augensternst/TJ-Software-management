<template>
  <div ref="chartContainer" class="chart-container"></div>
</template>

<script>
import * as echarts from 'echarts'
import { getDeviceAttributes } from '@/api/reportSystemApi'

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
    this.fetchDeviceAttributes();
    window.addEventListener('resize', this.handleResize);
  },
  watch: {
    'device.id': {
      handler(newVal) {
        if (newVal) {
          this.fetchDeviceAttributes();
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

    async fetchDeviceAttributes() {
      if (!this.device || !this.device.id) {
        console.error('设备ID无效');
        return;
      }

      this.isLoading = true;
      this.errorMessage = '';

      try {
        const response = await getDeviceAttributes(this.device.id);
        console.log('设备属性数据:', response);

        if (response.data && response.data.success && response.data.attributes) {
          this.processAttributeData(response.data.attributes);
        } else {
          throw new Error('获取设备属性失败: 数据格式不正确');
        }
      } catch (error) {
        console.error('获取设备属性失败:', error);
        this.errorMessage = '获取属性数据失败，请稍后重试';
        this.useMockData();
      } finally {
        this.isLoading = false;
      }
    },

    processAttributeData(attributes) {
      if (!Array.isArray(attributes)) {
        console.error('属性数据不是数组格式');
        this.useMockData();
        return;
      }

      this.chartData = attributes.map(attr => ({
        name: attr.name || attr.attributeName,
        value: parseFloat(attr.value) || 0
      }));

      this.chartData = this.chartData.filter(item => item.value > 0);

      if (this.chart) {
        this.updateChart();
      } else {
        this.initChart();
      }
    },

    useMockData() {
      this.chartData = [
        { name: '燃油流量', value: 1996.5 },
        { name: '风扇转速', value: 19.2 },
        { name: '风扇裕度', value: 56.2 },
        { name: '风扇出口温度', value: 2.8 },
        { name: 'HPT出口温度', value: 1643.8 },
        { name: '高压压气机转速', value: 8217.9 },
        { name: '高压压气机裕度', value: 27.8 }
      ];

      if (this.chart) {
        this.updateChart();
      } else {
        this.initChart();
      }
    },

    initChart() {
      this.chart = echarts.init(this.$refs.chartContainer);
      this.updateChart();
    },

    getRandomHexColor() {
      return '#' + Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0');
    },

    // 格式化大数字为缩写形式
    formatNumber(num) {
      if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
      }
      return num.toFixed(1);
    },

    updateChart() {
      if (!this.chart) return;

      const colorMap = {
        '燃油流量': '#B980F0',
        '风扇转速': '#5758F0',
        '风扇裕度': '#5B68F0',
        '风扇出口温度': '#5E9DF0',
        'HPT出口温度': '#F0E454',
        '高压压气机转速': '#B8F042',
        '高压压气机裕度': '#42F07D'
      };

      const formattedData = this.chartData.map((item) => {
        const color = colorMap[item.name] || this.getRandomHexColor();

        return {
          value: item.value,
          name: item.name,
          itemStyle: {color},
          label: {
            show: true,
            position: 'inside',
            formatter: (params) => {
              return this.formatNumber(params.value);
            },
            color: '#fff',
            fontSize: 12
          }
        };
      });

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a}<br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'middle',
          textStyle: {color: '#eee'},
          itemWidth: 10,
          itemHeight: 10,
          icon: 'circle',
          formatter: (name) => {
            return name.length > 6 ? name.substring(0, 6) + '...' : name;
          },
          tooltip: {
            show: true,
            formatter: (params) => {
              return params.name;
            }
          }
        },
        series: [
          {
            name: '设备属性',
            type: 'pie',
            radius: ['20%', '75%'], // 扩大饼图，利用中心空间
            center: ['38%', '50%'],
            avoidLabelOverlap: true,
            itemStyle: {
              borderRadius: 4,
              borderWidth: 2,
              borderColor: '#0B1739'
            },
            labelLine: {
              show: false
            },
            data: formattedData,
            emphasis: {
              label: {
                show: true,
                fontSize: 14,
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
  height: 100%;
  min-height: 300px;
}
</style>