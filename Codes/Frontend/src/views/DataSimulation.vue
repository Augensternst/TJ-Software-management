<template>
  <div class="data-simulation">
    <div class="content">
      <div class="data-result">
        <img src="@/assets/DataSimulation/uploadData.svg" alt="上传数据" style="width:90%;align-self: center">
        <div class="select">
          <img src="@/assets/DataSimulation/star.svg" alt="star" style="width:3%;align-self: center;">
          <!-- 选择模型部分 -->
          <div class="input">
            <span class="worksans-font" style="text-align: center; align-self: center; padding-left: 5%;">{{ selections.model.message }}</span>
            <!-- 按钮 -->
            <SelectDropdown
              :placeholder="'搜索标准数据'"
              :items="selections.model.items"
              :current-page="selections.model.currentPage"
              :total-pages="selections.model.totalPages"
              @select="(command) => onSelect('model', command)"
              @search="(query) => onSearch('model', query)"
              @prev-page="() => prevPage('model')"
              @next-page="() => nextPage('model')"
            />
          </div>
        </div>
        <div class="select">
          <img src="@/assets/DataSimulation/puzzle.svg" alt="puzzle" style="width:3%;align-self: center;justify-self: center">
          <!-- 选择设备部分 -->
          <div class="input">
            <span class="worksans-font" style="text-align: center; align-self: center; padding-left: 5%;" >{{ selections.device.message }}</span>
            <!-- 按钮 -->
            <SelectDropdown
              :placeholder="'搜索设备'"
              :items="selections.device.items"
              :current-page="selections.device.currentPage"
              :total-pages="selections.device.totalPages"
              @select="(command) => onSelect('device', command)"
              @search="(query) => onSearch('device', query)"
              @prev-page="() => prevPage('device')"
              @next-page="() => nextPage('device')"
            />
          </div>
        </div>
        <div class="upload-file" @click="triggerFileUpload">
          <img src="@/assets/DataSimulation/gear.svg" alt="puzzle" style="width:3%;align-self: center;justify-self: center">
          <div style="background: #081028;flex:0.8;border-radius: 12px;display: flex;justify-content: center">
            <!-- 动态显示文件名或 SVG 图片 -->
            <div v-if="fileName" class="file-name worksans-font" style="color: #55F0BD; align-self: center;">
              {{ fileName }}
            </div>
            <img v-else src="@/assets/DataSimulation/uploadFile.svg" alt="upload-file" style="width:40%;">
          </div>
          <input type="file" ref="fileInput" style="display: none;" @change="handleFileUpload" />
        </div>
      </div>
      <div v-if="!isStart" class="data-result">
        <img src="@/assets/DataSimulation/simulationResult.svg" alt="计算结果" style="width:90%;align-self: center">
        <div class="simulation-result">
          <div class="simulation-label worksans-font-green">器件图片</div>
          <div class="simulation-value">
            <img :src="simulationResult?.imageUrl || require('@/assets/MonitorCenter/TestImage.png')" alt="器件图片" style="width:15%;align-self: center;justify-self: center;">
          </div>
        </div>
        <div class="simulation-result">
          <div class="simulation-label worksans-font-green">损伤部位</div>
          <div class="simulation-value worksans-font-green">{{ simulationResult?.damageLocation || '无' }}</div>
        </div>
        <div class="simulation-result">
          <div class="simulation-label worksans-font-green">寿命预测</div>
          <div class="simulation-value">
            <span style="font-family: 'Zen Dots', sans-serif;font-size:3vw;color:#55F0BD;">{{ simulationResult?.lifespan || '0' }}</span>
            <span style="color:#55F0BD;align-self: flex-end;margin-left:2%;font-size:1vw">天</span>
          </div>
        </div>
        <div class="simulation-result" style="flex:0.3;">
          <div class="simulation-label worksans-font-green">健康度</div>
          <div class="simulation-value">
            <canvas ref="healthChart" class="simulation-health"></canvas>
          </div>
        </div>
      </div>
    </div>
    <div class="start-simulation" @click="startSimulation">
      <img src="@/assets/DataSimulation/startSimulation.svg" alt="startSimulation" style="width:15%;align-self: center;justify-self: center;">
    </div>
  </div>
</template>

<script>
import {drawHealthChart} from "@/utils/drawHealthChart";
import { getModels, getDevices, getSimulationResult } from "@/api/dataSimulationApi";
import SelectDropdown from "@/components/SelectDropdown.vue";

export default {
  name: 'DataSimulation',
  components: {
    SelectDropdown,
  },
  data() {
    return {
      fileName: '',
      simulationResult: null,
      isLoading: false,
      isStart: true,

      // 通用数据结构
      selections: {
        model: {
          items: [],
          selected: null,
          message: "选择标准数据",
          searchQuery: '',
          currentPage: 1,
          pageSize: 3,
          totalPages: 1,
        },
        device: {
          items: [],
          selected: null,
          message: "选择设备",
          searchQuery: '',
          currentPage: 1,
          pageSize: 3,
          totalPages: 1,
        },
      },
    };
  },
  async mounted() {
    // 初始化加载模型和设备列表
    await this.fetchModelData();
    await this.fetchDeviceData();
  },
  methods: {
    // 获取模型数据
    async fetchModelData() {
      try {
        const selection = this.selections.model;
        // 空字符串时不传递searchQuery参数
        const response = await getModels(
            selection.currentPage,
            selection.pageSize,
            selection.searchQuery || undefined
        );

        console.log('模型数据响应:', response);

        // 安全处理响应数据
        if (response && response.data && response.data.success) {
          if (Array.isArray(response.data.models)) {
            selection.items = response.data.models.map(item => ({
              id: item.id,
              name: item.name,
            }));

            // 计算总页数
            if (response.data.total) {
              selection.totalPages = Math.ceil(response.data.total / selection.pageSize);
            } else {
              selection.totalPages = Math.ceil(response.data.models.length / selection.pageSize);
            }
          } else {
            console.warn('API返回的模型列表不是数组:', response.data);
            this.setDefaultModelData();
          }
        } else {
          console.warn('API响应格式不符合预期:', response);
          this.setDefaultModelData();
        }
      } catch (error) {
        console.error('获取模型数据失败:', error);
        this.setDefaultModelData();
      }
    },

    // 设置默认模型数据
    setDefaultModelData() {
      this.selections.model.items = [
        {id: 1, name: "模型A"},
        {id: 2, name: "模型B"}
      ];
      this.selections.model.totalPages = 1;
    },

    // 获取设备数据
    async fetchDeviceData() {
      try {
        const selection = this.selections.device;
        // 空字符串时不传递searchQuery参数
        const response = await getDevices(
            selection.currentPage,
            selection.pageSize,
            selection.searchQuery || undefined
        );

        console.log('设备列表响应:', response);

        // 安全处理响应数据
        if (response && response.data && response.data.success) {
          console.log(response.data)
          if (Array.isArray(response.data.devices)) {
            selection.items = response.data.devices.map(item => ({
              id: item.deviceId || item.id,
              name: item.name,
            }));

            // 计算总页数
            if (response.data.pagination.totalPages) {
              selection.totalPages = response.data.pagination.totalPages;

            } else {
              selection.totalPages = Math.ceil(response.data.devices.length / selection.pageSize);
            }
          } else {
            console.warn('API返回的设备列表不是数组:', response.data);
            this.setDefaultDeviceData();
          }
        } else {
          console.warn('API响应格式不符合预期:', response);
          this.setDefaultDeviceData();
        }
      } catch (error) {
        console.error('获取设备列表失败:', error);
        this.setDefaultDeviceData();
      }
    },

    // 设置默认设备数据
    setDefaultDeviceData() {
      this.selections.device.items = [
        {id: 1, name: "发动机A"},
        {id: 2, name: "发动机B"}
      ];
      this.selections.device.totalPages = 1;
    },

    // 支持模型和设备搜索
    onSearch(type, query) {
      const selection = this.selections[type];
      selection.searchQuery = query;
      selection.currentPage = 1; // 搜索时重置为第一页

      if (type === 'model') {
        this.fetchModelData();
      } else if (type === 'device') {
        this.fetchDeviceData();
      }
    },

    // 翻页 - 支持模型和设备
    prevPage(type) {
      const selection = this.selections[type];
      if (selection.currentPage > 1) {
        selection.currentPage--;
        if (type === 'model') {
          this.fetchModelData();
        } else if (type === 'device') {
          this.fetchDeviceData();
        }
      }
    },

    nextPage(type) {
      const selection = this.selections[type];
      if (selection.currentPage < selection.totalPages) {
        selection.currentPage++;
        if (type === 'model') {
          this.fetchModelData();
        } else if (type === 'device') {
          this.fetchDeviceData();
        }
      }
    },

    // 选择
    onSelect(type, command) {
      const selection = this.selections[type];
      selection.selected = command;
      selection.message = command.name;
      console.log(`已选择 ${type}:`, command);
    },

    // 触发文件上传
    triggerFileUpload() {
      this.$refs.fileInput.click();
    },

    // 处理文件上传
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        console.log('已选择文件:', file);
        this.fileName = file.name;
      }
    },

    // 开始模拟
    async startSimulation() {
      const {model, device} = this.selections;

      // 验证所有必填项
      if (!model.selected || !device.selected || !this.$refs.fileInput.files[0]) {
        alert('请选择模型、设备并上传文件');
        return;
      }

      this.isLoading = true;
      this.isStart = false;

      try {
        console.log('开始模拟计算...');
        console.log('模型ID:', model.selected.id);
        console.log('设备ID:', device.selected.id);
        console.log('文件:', this.$refs.fileInput.files[0].name);

        // 直接调用模拟接口
        const response = await getSimulationResult(
            model.selected.id,
            device.selected.id,
            this.$refs.fileInput.files[0]
        );

        console.log('模拟结果:', response);

        if (response && response.data && response.data.success) {
          this.simulationResult = response.data;

          // 渲染健康度图表
          this.$nextTick(() => {
            if (this.$refs.healthChart) {
              drawHealthChart(this.$refs.healthChart, response.data.healthIndex);
            }
          });
        } else {
          alert('模拟失败: ' + ((response && response.data && response.data.message) || '未知错误'));
        }
      } catch (error) {
        console.error('模拟过程中出错:', error);
        alert('模拟过程中出错，请稍后重试');
      } finally {
        this.isLoading = false;
      }
    },
  },
};
</script>

<style scoped>
@import '@/assets/DataSimulation/DataSimulationStyle.css';
</style>