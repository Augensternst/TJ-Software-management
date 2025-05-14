import {ArcElement, Chart, DoughnutController} from 'chart.js';

// 注册 Chart.js 组件
Chart.register(DoughnutController, ArcElement);

/**
 * 绘制健康度圆环图
 * @param {HTMLCanvasElement} canvas - Canvas 元素
 * @param {number} healthData - 健康度数据（0-100）
 */
export const drawHealthChart = (canvas, healthData) => {
  if (!canvas) return;

  // 销毁已经有的
  if (canvas.chart) {
    canvas.chart.destroy(); // 销毁之前的 Chart 实例
  }

  const ctx = canvas.getContext('2d');

  // 根据健康度设置颜色
  const getHealthColor = (health) => {
    if (health >= 80) return '#55F0BD'; // 绿色
    if (health >= 50) return '#E4EE53'; // 黄色
    return '#FF0E0E'; // 红色
  };

  const healthColor = getHealthColor(healthData);

  canvas.chart=new Chart(ctx, {
    type: 'doughnut',
    data: {
      datasets: [
        {
          data: [healthData, 100 - healthData], // 健康度和剩余部分
          backgroundColor: [healthColor, '#212A42'], // 颜色
          borderWidth: 0, // 去掉边框
        },
      ],
    },
    options: {
      responsive: true, // 启用响应式
      aspectRatio: 1.5, // 宽高比
      cutout: '60%', // 圆环的宽度
      rotation: 0, // 从顶部开始
      circumference: 360, // 完整的圆环
      animation: {
        animateRotate: true, // 启用旋转动画
        animateScale: true, // 启用缩放动画
      },
      plugins: {
        legend: {
          display: false, // 不显示图例
        },
        tooltip: {
          enabled: false, // 不显示提示
        },
      },
    },
    plugins: [{
      id: 'healthText', // 自定义插件 ID
      beforeDraw: (chart) => {
        const {ctx, chartArea: {width, height}} = chart;
        // console.log(width);
        ctx.save(); // 保存当前绘图状态

        // 动态计算字体大小（基于 canvas 宽度）
        const fontSize = Math.max(10, width * 0.1); // 字体大小至少为 10px，最大为宽度的 10%
        ctx.font = `bold ${fontSize}px Arial`; // 设置字体大小
        ctx.fillStyle = healthColor; // 文本颜色与圆环颜色一致
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';

        // 在圆环中心绘制健康度数字
        const text = `${healthData}`;
        const x = width / 2;
        const y = height / 2;
        ctx.fillText(text, x, y);

        ctx.restore(); // 恢复绘图状态
      },
    }],
  });


};