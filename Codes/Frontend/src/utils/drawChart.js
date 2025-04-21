// @/utils/drawChart.js
import Chart from 'chart.js/auto';

export const drawChart = (canvasRef, data) => {
  const ctx = canvasRef.getContext('2d');
  const baseColor=data.datasets[0].backgroundColor;

  const colorValues = baseColor.match(/\d+/g); // 提取颜色值
  const r = colorValues[0];
  const g = colorValues[1];
  const b = colorValues[2];
  const a = colorValues[3] || 1; // 如果透明度未定义，默认为 1

  // 创建渐变颜色
  const gradient = ctx.createLinearGradient(0,0,0,canvasRef.height);
  gradient.addColorStop(0, `rgba(${r}, ${g}, ${b}, ${a * 0.8})`);
  gradient.addColorStop(1, `rgba(${r}, ${g}, ${b}, ${a * 0.1})`);


  const modifiedData = {
    ...data,
    datasets: data.datasets.map((dataset) => {
      return {
        ...dataset,
        fill: true, // 填充曲线下方的面积
        backgroundColor:gradient,
        borderColor: '#8979FF', // 折线的颜色为紫色
        pointBorderColor: '#8979FF', // 数据点的边缘颜色为紫色
        pointBackgroundColor: 'white', // 数据点的内部颜色为白色
        pointBorderWidth: 1, // 数据点的边缘宽度

      };
    }),
  };

  const chartInstance = new Chart(ctx, {
    type: 'line', // 图表类型为折线图
    data: modifiedData, // 使用修改后的数据
    options: {

      responsive: true,
      aspectRatio:1.5,
      plugins: {
        legend: {
          display: true, // 显示图例
          position: 'bottom', // 图例放置在底部
          labels: {
            color: 'rgba(0, 178, 255,0.95)', // 图例文字颜色为蓝色
          },
        },
      },
      animation: {
        duration: 500, // 动画时长
        easing: 'easeInOutQuad', // 动画效果
      },
      elements: {
        line: {
          tension: 0.4, // 设置线条的曲率，0 为直线，1 为最大曲率
          borderWidth: 1, // 线条宽度
        },
        point: {
          radius: 3, // 数据点的大小
          hoverRadius: 7, // 鼠标悬停时数据点的大小
          borderWidth: 2, // 数据点的边缘宽度
        },
      },
      scales: {
        x: {
          grid: {
            display: true, // 显示 y 轴的网格线
            lineWidth:1,
            borderDash: [5, 5], // 设置网格线为虚线
            color: 'rgba(0, 0, 26, 0.15)', // 深蓝色，透明度 0.15
          },
          ticks:{
            color: 'rgba(0, 178, 255,0.95)'
          },
        },
        y: {
          grid: {
            borderDash:[5,5],
            color: 'rgba(0, 0, 26, 0.15)', // 深蓝色，透明度 0.15
          },
          ticks:{
            color: 'rgba(0, 178, 255,0.95)'
          },
        },
      },
    },
  });

  // 动态加载效果
  let animationProgress = 0;
  const animationDuration = 500; // 动画时长
  const animationStep = 10;


  const animate = () => {
    if (animationProgress < animationDuration) {
      animationProgress += animationStep;
      chartInstance.update(); // 更新图表
      requestAnimationFrame(animate);
    }
  };
  animate();


  return chartInstance;
};