<template>
  <div ref="chart" class="chart-container"></div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts';

export default {
  props: {
    chartData: {
      type: Object,
      default: () => ({
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
      })
    }
  },
  setup(props) {
    const chart = ref(null);
    let myChart = null;

    const updateChart = () => {
      if (!myChart ||!chart.value) return;

      const option = {
        color: ['#7B68EE', '#FF9F7F'],
        grid: {
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(50, 50, 50, 0.7)',
          borderColor: '#333',
          textStyle: {
            color: '#fff'
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: props.chartData.xAxis,
          axisLine: { show: false },
          axisLabel: { color: '#eee' }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: {
            lineStyle: {
              color: 'rgba(255, 255, 255, 0.1)'
            }
          },
          axisLabel: { color: '#eee' }
        },
        series: props.chartData.series.map(series => ({
          ...series,
          type: 'line',
          symbolSize: 8,
          lineStyle: { width: 1 },
          emphasis: { focus: 'series' }
        })),
        legend: {
          data: props.chartData.series.map(s => s.name),
          left: "center",
          bottom: "bottom",
          textStyle: { color: '#eee' },
          itemWidth: 15,
          itemHeight: 10
        }
      };

      // 使用 setOption 的第二个参数 notMerge: true 确保完整刷新
      myChart.setOption(option, true);
    };

    const initChart = () => {
      if (!chart.value) return;

      if (!myChart) {
        myChart = echarts.init(chart.value);
        window.addEventListener("resize", handleResize);
      }

      updateChart();
    };

    const handleResize = () => {
      myChart && myChart.resize();
    };

    onMounted(() => {
      initChart();
    });

    onBeforeUnmount(() => {
      window.removeEventListener("resize", handleResize);
      if (myChart) {
        myChart.dispose();
        myChart = null;
      }
    });

    watch(() => props.chartData, () => {
      if (myChart) {
        updateChart();
      } else {
        initChart();
      }
    }, { deep: true, immediate: true });

    return { chart };
  }
};
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 340px;
}
</style>
