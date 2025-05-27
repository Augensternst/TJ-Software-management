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
              :placeholder="'搜索模型'"
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
      <div v-if="isStart===false" class="data-result">
        <img src="@/assets/DataSimulation/simulationResult.svg" alt="计算结果" style="width:90%;align-self: center">
        <div class="simulation-result">
          <div class="simulation-label worksans-font-green">器件图片</div>
          <div class="simulation-value">
            <img :src="simulationResult?.imageUrl || '@/assets/MonitorCenter/TestImage.png'" alt="器件图片" style="width:15%;align-self: center;justify-self: center;">
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
import { drawHealthChart } from "@/utils/drawHealthChart";
import {getModels,getDevices, submitSimulationTask,getSimulationResult} from "@/api/dataSimulationApi";
import SelectDropdown from "@/components/SelectDropdown.vue";
export default {
  name: 'DataSimulation',
  components:{
    SelectDropdown,
  },
  data() {
    return {
      health_data: 80, // 健康度数据
      models: [{id:1,name:"模型1"}], // 模型选项
      devices: [{id:1,name:"设备1"}], // 设备选项
      selectedModel: null, // 当前选中的模型
      selectedDevice: null, // 当前选中的设备
      fileName: '',
      taskId: null, // 任务 ID
      simulationResult: null, // 模拟结果
      isLoading: false, // 是否正在加载
      isStart:true,// 是否是初始化

      // 通用数据结构
      selections: {
        model: {
          items: [{ id: 1, name: "模型1" }], // 模型选项
          selected: null, // 当前选中的模型
          message: "选择模型",
          searchQuery: '', // 搜索关键字
          currentPage: 1, // 当前页码
          pageSize: 5, // 每页大小
          totalPages: 1, // 总页数
        },
        device: {
          items: [{ id: 1, name: "设备1" }], // 设备选项
          selected: null, // 当前选中的设备
          message: "选择设备",
          searchQuery: '', // 搜索关键字
          currentPage: 1, // 当前页码
          pageSize: 5, // 每页大小
          totalPages: 1, // 总页数
        },
      },
    };
  },
   async mounted() {
    // 初始化加载模型和设备列表（第一页）
      await this.fetchData('model');
      await this.fetchData('device');

  },
  methods: {
    // 通用方法 获取数据
    async fetchData(type) {
      try {
        const selection = this.selections[type];
        const response = await (type === 'model' ? getModels : getDevices)(
            selection.currentPage,
            selection.pageSize,
            selection.searchQuery
        );
        selection.items = response[type === 'model' ? 'models' : 'devices'].map(item => ({
          id: item.id,
          name: item.name,
        }));
        selection.totalPages = Math.ceil(response.total / selection.pageSize);
      } catch (error) {
        console.error(`Error fetching ${type}:`, error);
      }
    },

    // 通用方法：搜索
    onSearch(type, query) {
      const selection = this.selections[type];
      selection.searchQuery = query;
      selection.currentPage = 1; // 搜索时重置为第一页
      this.fetchData(type);
    },
// 通用方法：翻页
    prevPage(type) {
      const selection = this.selections[type];
      if (selection.currentPage > 1) {
        selection.currentPage--;
        this.fetchData(type);
      }
    },

    nextPage(type) {
      const selection = this.selections[type];
      if (selection.currentPage < selection.totalPages) {
        selection.currentPage++;
        this.fetchData(type);
      }
    },

    // 通用方法：选择
    onSelect(type, command) {
      const selection = this.selections[type];
      selection.selected = command;
      selection.message = command.name;
      console.log(selection);
      console.log(`Selected ${type}:`, command);
    },


    //   // 获取模型列表
    // async fetchModels() {
    //   try {
    //     const response = await getModels(this.modelCurrentPage, this.modelPageSize, this.modelSearchQuery);
    //     this.models = response.models.map(model => ({ id: model.id, name: model.name }));
    //     this.modelTotalPages = Math.ceil(response.total / this.modelPageSize);
    //   } catch (error) {
    //     console.error('Error fetching models:', error);
    //   }
    // },
    //   // 获取设备列表
    // async fetchDevices() {
    //   try {
    //     const response = await getDevices(this.deviceCurrentPage, this.devicePageSize, this.deviceSearchQuery);
    //     this.devices = response.devices.map(device => ({ id: device.id, name: device.name }));
    //     this.deviceTotalPages = Math.ceil(response.total / this.devicePageSize);
    //   } catch (error) {
    //     console.error('Error fetching devices:', error);
    //   }
    // },
    //
    //   // 模型搜索
    // onModelSearch(query) {
    //   this.modelSearchQuery=query;
    //   this.modelCurrentPage = 1; // 搜索时重置为第一页
    //   this.fetchModels();
    // },
    //
    // // 设备搜索
    // onDeviceSearch(query) {
    //   this.deviceSearchQuery=query;
    //   this.deviceCurrentPage = 1; // 搜索时重置为第一页
    //   this.fetchDevices();
    // },
    //
    //   // 模型翻页
    // prevModelPage() {
    //   console.log(this.modelCurrentPage);
    //   if (this.modelCurrentPage > 1) {
    //     this.modelCurrentPage--;
    //     this.fetchModels();
    //   }
    // },
    // nextModelPage() {
    //   if (this.modelCurrentPage < this.modelTotalPages) {
    //     this.modelCurrentPage++;
    //     this.fetchModels();
    //   }
    // },
    //
    //   // 设备翻页
    // prevDevicePage() {
    //   if (this.deviceCurrentPage > 1) {
    //     this.deviceCurrentPage--;
    //     this.fetchDevices();
    //   }
    // },
    // nextDevicePage() {
    //   if (this.deviceCurrentPage < this.deviceTotalPages) {
    //     this.deviceCurrentPage++;
    //     this.fetchDevices();
    //   }
    // },
    //
    //
    // onModelChange(command) {
    //
    //   this.selectedModel = command;
    //   this.modelMessage = command.name;
    //   console.log('Selected Model:', this.selectedModel);
    //
    //
    // },
    // onDeviceChange(command) {
    //
    //   this.selectedDevice = command;
    //   this.equipmentMessage = command.name;
    //   console.log('Selected Device:', this.selectedDevice);
    //
    // },
    triggerFileUpload() {
      this.$refs.fileInput.click(); // 触发文件选择
    },
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        console.log('Selected File:', file);
        this.fileName = file.name;
      }
    },

    // async startSimulation() {
    //   if (!this.selectedModel || !this.selectedDevice || !this.fileName) {
    //     alert('请选择模型、设备并上传文件');
    //     return;
    //   }
    //
    //   this.isLoading = true;
    //   this.isStart=false;
    //
    //   try {
    //     // 提交模拟任务
    //     const taskResponse = await submitSimulationTask(
    //       this.selectedModel.id,
    //       this.selectedDevice.id,
    //       this.$refs.fileInput.files[0]
    //     );
    //     this.taskId = taskResponse.taskId;
    //
    //     // 获取模拟结果
    //     const resultResponse = await getSimulationResult(this.taskId);
    //     this.simulationResult = resultResponse;
    //
    //     // 更新健康度图表
    //     if (this.$refs.healthChart) {
    //       drawHealthChart(this.$refs.healthChart, resultResponse.healthIndex);
    //     }
    //   } catch (error) {
    //     console.error('Error during simulation:', error);
    //   } finally {
    //     this.isLoading = false;
    //   }
    // },

    async startSimulation() {
      const {model, device} = this.selections;
      if (!model.selected || !device.selected || !this.fileName) {
        alert('请选择模型、设备并上传文件');
        return;
      }

      this.isLoading = true;
      this.isStart = false;

      try {
        const taskResponse = await submitSimulationTask(
            model.selected.id,
            device.selected.id,
            this.$refs.fileInput.files[0]
        );
        this.taskId = taskResponse.taskId;

        const resultResponse = await getSimulationResult(this.taskId);
        this.simulationResult = resultResponse;

        if (this.$refs.healthChart) {
          drawHealthChart(this.$refs.healthChart, resultResponse.healthIndex);
        }
      } catch (error) {
        console.error('Error during simulation:', error);
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