<template>
  <div class="alert-container">
    <!-- 搜索和操作区域 -->
    <div class="search-area">
      <div class="search-input-custom">
        <el-input
            v-model="searchQuery"
            placeholder="搜索设备"
            class="search-input"
            @input="handleSearch"
        >
          <template #prefix>
            <el-icon>
              <search/>
            </el-icon>
          </template>
        </el-input>
      </div>

      <el-button type="primary" @click="handleBatchConfirm" class="action-button">批量确认</el-button>

      <el-date-picker
          v-model="startTime"
          type="datetime"
          placeholder="开始时间"
          class="date-picker"
          @change="handleDateChange"
      ></el-date-picker>

      <el-date-picker
          v-model="endTime"
          type="datetime"
          placeholder="结束时间"
          class="date-picker"
          @change="handleDateChange"
      ></el-date-picker>

      <el-button type="primary" @click="exportReport" class="export-button">导出报表</el-button>
    </div>

    <!-- 表格标题 -->
    <h2 class="table-title">警报列表</h2>

    <!-- 表格 -->
    <el-table
        ref="alertTable"
        v-loading="loading"
        :data="alertList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        :header-cell-style="headerCellStyle"
        :row-style="rowStyle"
    >
      <el-table-column type="selection" width="55"/>

      <el-table-column prop="id" label="报警id" width="120">
        <template #default="{ row }">
          <span style="color: #E040FB;">{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="device" label="设备" width="180"/>

      <el-table-column label="报警时间" width="180">
        <template #default="{ row }">
          <div>{{ row.date }}</div>
          <div>{{ row.time }}</div>
        </template>
      </el-table-column>

      <el-table-column prop="severity" label="严重性" width="120">
        <template #default="{ row }">
          <el-tag :type="severityTagType(row.severity)" size="default">
            {{ row.severity }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="details" label="报警详情"/>

      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleConfirm(row)" class="confirm-button">
            确认
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页控件 -->
    <div class="pagination-container">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :locale="{
          total: '总条数',
          prev: '上一页',
          next: '下一页',
          jumper: '跳转',
          pagesize: '条/页'
        }"
      />
    </div>
  </div>
</template>

<script>
import {Search} from '@element-plus/icons'
import {getUnconfirmedAlerts,exportAlerts,confirmAlerts} from '@/api/Alert'



export default {
  components: {
     Search
  },
  data() {
    return {
      searchQuery: '',
      startTime: '',
      endTime: '',
      selectedRows: [],

      alertList: [],
      loading: false,
      // 分页相关
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.fetchAlertList()
  },
  methods: {
    // 获取警报列表数据
    async fetchAlertList() {
      this.loading = true
      try {
        const response = await getUnconfirmedAlerts();

        if (response.data.success) {
          // 转换数据格式
          this.alertList = response.data.alerts.map(alert => ({
            id: `${alert.alertId}`,
            device: alert.deviceName,
            date: alert.alertTime.split(' ')[0],
            time: alert.alertTime.split(' ')[1],
            severity: this.mapStatusToSeverity(alert.status),
            details: alert.alertDescription,
            rawStatus: alert.status,
            confirmed: alert.confirmed
          }))

          this.total = response.data.total
        }
      } catch (error) {
        console.error('获取警报列表失败:', error)
        this.$message.error('获取警报列表失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

  // 导出报表
async exportReport() {
  try {
    this.loading = true;
    const response = await exportAlerts();

    // 创建下载链接
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    // 从响应头获取文件名
    const contentDisposition = response.headers['content-disposition'];
    let fileName = 'alerts.xlsx';

    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename=(.+)/);
      if (fileNameMatch.length > 1) {
        fileName = fileNameMatch[1];
      }
    }

    // 创建临时链接并触发下载
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = fileName;
    link.click();

    // 清理资源
    window.URL.revokeObjectURL(link.href);
    link.remove();

    this.$message.success('报表导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    let errorMessage = '导出失败，请稍后重试';

    // 处理特定错误
    if (error.response) {
      if (error.response.status === 401) {
        errorMessage = '登录已过期，请重新登录';
      } else if (error.response.status === 404) {
        errorMessage = '导出接口不存在';
      }
    }

    this.$message.error(errorMessage);
  } finally {
    this.loading = false;
  }
},

    // 状态映射
    mapStatusToSeverity(status) {
      const map = {
        1: '一般',
        2: '中等',
        3: '严重'
      }
      return map[status] || '未知状态'
    },

    /*
    async confirmAlerts(alertIds) {
      try {
        const response = await api.post('/alerts/confirm', {
          alertIds: alertIds.map(id => parseInt(id.replace('ALT', '')))
        })

        if (response.data.success) {
          return true
        }
      } catch (error) {
        console.error('确认警报失败:', error)
        throw new Error('确认操作失败')
      }
    },*/


    // 修改分页处理
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.fetchAlertList()
    },

    handleCurrentChange(page) {
      this.currentPage = page
      this.fetchAlertList()
    },

    // 处理搜索输入
    handleSearch() {
      this.currentPage = 1 // 重置到第一页
      this.fetchAlertList()
    },

    // 处理日期变化
    handleDateChange() {
      this.currentPage = 1 // 重置到第一页
      this.fetchAlertList()
    },

    /*handleSizeChange(size) {
      this.pageSize = size
      this.fetchAlertList()
    },*/

    // 处理页码变化
    /*handleCurrentChange(page) {
      this.currentPage = page
      this.fetchAlertList()
    },*/

 // 修改后的确认方法（单个）
async handleConfirm(row) {
  try {
    console.log('当前确认的警报ID:', row.id);

    await this.$confirm('确认处理该警报吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // 直接使用原始ID，确保传递数组格式
    const alertIds = [row.id];


    await confirmAlerts(alertIds);

    this.$message.success('警报已确认');
    await this.fetchAlertList();
  } catch (error) {
    if (error !== 'cancel') {
      this.$message.error(error.message || '确认失败');
    }
  }
},

// 修改后的批量确认方法
async handleBatchConfirm() {
  if (this.selectedRows.length === 0) {
    this.$message.warning('请至少选择一项警报进行确认');
    return;
  }

  try {
    await this.$confirm(`确定批量确认选中的 ${this.selectedRows.length} 项警报？`, '批量确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // 直接获取ID数组
    const alertIds = this.selectedRows.map(row => row.id);


    await confirmAlerts(alertIds);

    this.$message.success(`成功确认 ${alertIds.length} 项`);
    this.$refs.alertTable.clearSelection();
    await this.fetchAlertList();
  } catch (error) {
    if (error !== 'cancel') {
      this.$message.error(error.message || '批量确认失败');
    }
  }
},

    /*
    async confirmAlerts(alertIds) {
      return api.post('/alerts/confirm', {alertIds})
    },*/



    // 选择行变化处理
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    // 严重性标签类型
    severityTagType(severity) {
      const types = {
        '一般': 'success',
        '中等': 'warning',
        '严重': 'danger'
      }
      return types[severity] || 'info'
    },

    // 表头样式
    headerCellStyle() {
      return {
        backgroundColor: '#121E36',
        color: '#fff',
        fontWeight: 'bold'
      }
    },

    // 行样式
    rowStyle(row) {
      return row.rowIndex % 2 === 0 ?
          {background: '#0B1739'} :
          {background: '#0A1330'}
    }
  }
}
</script>

<style scoped>
.alert-container {
  padding: 20px;
  background-color: #121E36;
  color: white;
  min-height: calc(100vh - 200px);
}

.search-area {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
  gap: 10px;
}

.date-picker {
  width: 200px;
}

.export-button {
  background-color: #2E9BB1;
  border-color: #2E9BB1;
}

.action-button {
  background-color: #2E46B1;
  border-color: #2E46B1;
}

.confirm-button {
  background-color: #E040FB;
  border-color: #E040FB;
}

.table-title {
  color: #1DE9B6;
  margin-bottom: 20px;
  text-align: center;
  font-size: 24px;
}

.search-input {
  width: 200px;
}

/* 输入框主体背景色 */
.search-input-custom :deep(.el-input__inner) {
  background-color: #412F74 !important;
  color: #68F0EB !important;
}

/* 图标本身的样式 */
.search-input-custom :deep(.el-input__prefix .el-icon) {
  color: white; /* 调整图标颜色以适应深色背景 */
}

/* 确保整个输入框没有其他背景色破坏效果 */
.search-input-custom :deep(.el-input__wrapper) {
  background-color: #412F74 !important;
  box-shadow: 0 0 0 1px #343B4F inset; /* 自定义边框颜色 */
}

/* 处理占位符文本颜色 */
.search-input-custom :deep(.el-input__inner::placeholder) {
  color: #68F0EB !important;
}

.search-area :deep(.el-input__wrapper) {
  background-color: #5D5478;
  box-shadow: 0 0 0 1px #343B4F inset; /* 自定义边框颜色 */
}

.search-area :deep(.el-input__inner) {
  color: #68F0EB;
  --el-input-text-color: #68F0EB !important;
}

.search-area :deep(.el-input__inner::placeholder) {
  color: #9CD2D0CC;
}

:deep(.el-table) {
  background-color: #121E36;
  color: white;
  height: calc(100vh - 200px - 124px - 50px); /* 减去分页高度 */
}

:deep(.el-table) {
  --el-table-row-hover-bg-color: rgba(11, 73, 196, 0.5);
  --el-table-border: none;

}

:deep(.el-table--border, .el-table--group) {
  border-color: #243656;
}

:deep(.el-table td, .el-table th.is-leaf) {
  border-color: #243656;
}

:deep(.el-table--border th, .el-table--border td) {
  border-right-color: #243656;
}

:deep(.el-tag--success) {
  background-color: rgba(29, 233, 182, 0.23);
  border-color: #1DE9B6;
  color: #1DE9B6;
}

:deep(.el-tag--danger) {
  background-color: rgba(244, 67, 54, 0.2);
  border-color: #F44336;
}

:deep(.el-tag--warning) {
  background-color: rgba(255, 152, 0, 0.24);
  border-color: #FF9800;
}

/* 分页容器样式 */
.pagination-container {
  margin-top: 20px;
  text-align: center;
}

/* 分页组件样式 */
:deep(.el-pagination) {
  color: white;
  --el-pagination-bg-color: transparent;
  --el-pagination-button-color: white;
  --el-pagination-hover-color: #1DE9B6;
}

:deep(.el-pagination .el-pagination__total) {
  color: white;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  background-color: rgba(29, 233, 182, 0.1);
}

:deep(.el-pagination .el-pager li) {
  background-color: rgba(29, 233, 182, 0.1);
  color: white;
}

:deep(.el-pagination .el-pager li.is-active) {
  background-color: #1DE9B6;
  color: #121E36;
}

:deep(.el-pagination) {
  justify-content: center;

}

:deep(.el-loading-mask) {
  background-color: rgba(18, 30, 54, 0.9) !important;
}

/* 修改 el-pagination 的背景色 */
:deep(.el-pagination) {

  border-radius: 5px; /* 圆角 */
  padding: 8px 12px; /* 内边距 */
}

/* 修改分页按钮背景 */
:deep(.el-pagination .el-pager li) {
  background-color: #1c4183; /* 按钮背景色 */
  color: white; /* 文字颜色 */
}

/* 修改当前选中页的背景色 */
:deep(.el-pagination .el-pager li.is-active) {
  background-color: #1DE9B6; /* 选中页背景色 */
  color: white;
}
</style>