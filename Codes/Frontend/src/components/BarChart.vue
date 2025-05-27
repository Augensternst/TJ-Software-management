<script>
import * as echarts from "echarts";
import { onMounted, ref, watch, onBeforeUnmount } from "vue";

export default {
  props: {
    chartData: {
      type: Object,
      default: () => ({
        xAxis: ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'],
        series: [
          {
            name: '已处理',
            data: [0, 0, 0, 0, 0, 0, 0]
          },
          {
            name: '未处理',
            data: [0, 0, 0, 0, 0, 0, 0]
          }
        ]
      })
    }
  },
  setup(props) {
    const chart = ref(null);
    let myChart = null;

    const updateChart = () => {
      if (!myChart || !chart.value) return;

      // 确保series存在，如果不存在则使用默认值
      const series = props.chartData.series || [
        { name: '已处理', data: [0, 0, 0, 0, 0, 0, 0] },
        { name: '未处理', data: [0, 0, 0, 0, 0, 0, 0] }
      ];

      // 确保xAxis存在
      const xAxis = props.chartData.xAxis || ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'];

      const option = {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow",
          },
        },
        grid: {
          left: '3%',
          right: '3%',
          bottom: '7%',
          top: '4%',
          containLabel: true
        },
        legend: {
          data: series.map(item => item.name),
          icon: 'rect',
          left: "center",
          bottom: "bottom",
          textStyle: {
            color: "#fff",
          },
          itemHeight: 10,
        },
        xAxis: {
          type: "category",
          data: xAxis,
          axisLabel: {
            color: "#fff",
          },
        },
        yAxis: {
          type: "value",
          axisLabel: {
            color: "#fff",
          },
        },
        series: series.map((item, index) => ({
          name: item.name,
          type: "bar",
          label: {
            show: true,
            position: "inside",
            color: "#00FDE7",
          },
          itemStyle: {
            color: index === 0 ? "#8a85ff" : "#ff9999",
          },
          data: item.data || [0, 0, 0, 0, 0, 0, 0],
        })),
      };

      // 使用setOption的第二个参数notMerge:true来确保完全刷新图表
      myChart.setOption(option, true);
    };

    const initChart = () => {
      if (!chart.value) return;

      // 确保只初始化一次
      if (!myChart) {
        myChart = echarts.init(chart.value);

        // 添加窗口大小变化的监听
        window.addEventListener("resize", handleResize);
      }

      // 更新图表数据
      updateChart();
    };

    const handleResize = () => {
      myChart && myChart.resize();
    };

    onMounted(() => {
      // 初始化图表
      initChart();
    });

    onBeforeUnmount(() => {
      // 组件销毁前移除事件监听和释放图表实例
      window.removeEventListener("resize", handleResize);
      if (myChart) {
        myChart.dispose();
        myChart = null;
      }
    });

    // 使用深度监听确保检测到嵌套属性的变化
    watch(() => props.chartData, () => {
      // 确保图表已初始化
      if (myChart) {
        // 直接更新图表
        updateChart();
      } else {
        // 如果图表未初始化，则初始化
        initChart();
      }
    }, { deep: true, immediate: true });

    return {
      chart,
    };
  },
};
</script>

<template>
  <div ref="chart" class="chart-container"></div>
</template>

<style scoped>
.chart-container {
  width: 100%;
  height: 250px;
}
</style>