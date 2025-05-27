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
          <el-button link type="primary" @click="handleDelete(row)" class="delete-button">
            <el-icon>
              <Delete/>
            </el-icon>
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
import {Delete, Search} from '@element-plus/icons'
import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'https://af1f2aee-0858-4e5f-8a9e-6e279126c69d.mock.pstmn.io/api',
  timeout: 10000
})

export default {
  components: {
    Delete, Search
  },
  data() {
    return {
      searchQuery: '',
      startTime: '',
      endTime: '',
      selectedRows: [],
      //我先造一些数据再alertList里面

      alertList: [
      {
        id: 'ALT20250301-001',
        device: '发动机叶片A1',
        date: '2025-03-01',
        time: '08:23:15',
        severity: '一般提醒',
        details: '叶片表面磨损超过预警阈值，建议下次维护时检查'
      },
      {
        id: 'ALT20250301-002',
        device: '发动机轴承C2',
        date: '2025-03-01',
        time: '09:45:22',
        severity: '警告',
        details: '轴承温度异常升高，需要立即检查冷却系统'
      },
      {
        id: 'ALT20250302-001',
        device: '压气机叶片A3',
        date: '2025-03-02',
        time: '10:15:33',
        severity: '维修',
        details: '叶片出现微裂纹，需要安排更换'
      },
      {
        id: 'ALT20250302-002',
        device: '涡轮叶片A2',
        date: '2025-03-02',
        time: '13:05:47',
        severity: '一般提醒',
        details: '热障涂层部分剥落，建议下次检修时处理'
      },
      {
        id: 'ALT20250303-001',
        device: '主轴承C1',
        date: '2025-03-03',
        time: '07:30:10',
        severity: '警告',
        details: '振动频率异常，可能存在内圈损伤'
      },
      {
        id: 'ALT20250303-002',
        device: '燃油控制器F1',
        date: '2025-03-03',
        time: '15:42:55',
        severity: '维修',
        details: '喷油嘴堵塞，需要清洁或更换'
      },
      {
        id: 'ALT20250304-001',
        device: '高压压气机叶片A1',
        date: '2025-03-04',
        time: '02:17:33',
        severity: '警告',
        details: '叶片前缘出现异物损伤，需要评估'
      },
      {
        id: 'ALT20250304-002',
        device: '低压轴承C3',
        date: '2025-03-04',
        time: '11:23:41',
        severity: '一般提醒',
        details: '润滑油压力略低，需要检查供油系统'
      },
      {
        id: 'ALT20250305-001',
        device: '燃烧室衬套B2',
        date: '2025-03-05',
        time: '09:58:02',
        severity: '维修',
        details: '衬套高温变形，需要更换'
      },
      {
        id: 'ALT20250305-002',
        device: '高压涡轮叶片A2',
        date: '2025-03-05',
        time: '14:35:26',
        severity: '警告',
        details: '叶片冷却孔部分堵塞，温度超标'
      },
      {
        id: 'ALT20250306-001',
        device: '低压涡轮轴承C1',
        date: '2025-03-06',
        time: '08:12:37',
        severity: '一般提醒',
        details: '轴承游隙接近上限，下次大修时关注'
      },
      {
        id: 'ALT20250306-002',
        device: '压气机进口导叶A4',
        date: '2025-03-06',
        time: '10:49:55',
        severity: '警告',
        details: '导叶调节机构反馈信号异常，需要校准'
      },
      {
        id: 'ALT20250307-001',
        device: '排气温度传感器T1',
        date: '2025-03-07',
        time: '07:15:22',
        severity: '维修',
        details: '传感器读数不稳定，需要更换'
      },
      {
        id: 'ALT20250307-002',
        device: '中压压气机叶片A3',
        date: '2025-03-07',
        time: '13:45:10',
        severity: '一般提醒',
        details: '叶片清洁度不足，可能影响效率'
      },
      {
        id: 'ALT20250308-001',
        device: '推力轴承C4',
        date: '2025-03-08',
        time: '09:30:45',
        severity: '警告',
        details: '轴向位移超出正常范围，需要检查'
      },
      {
        id: 'ALT20250308-002',
        device: '风扇叶片A1',
        date: '2025-03-08',
        time: '16:20:33',
        severity: '维修',
        details: '叶片平衡性异常，需要动平衡测试'
      },
      {
        id: 'ALT20250309-001',
        device: '附件传动齿轮G2',
        date: '2025-03-09',
        time: '08:55:17',
        severity: '一般提醒',
        details: '齿轮磨损超过30%，记录并监控'
      },
      {
        id: 'ALT20250309-002',
        device: '发动机控制单元ECU',
        date: '2025-03-09',
        time: '11:33:40',
        severity: '警告',
        details: '软件版本需要更新，存在已知缺陷'
      }
      ],
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
       // 如果后端还未连接，使用本地分页
    setTimeout(() => {
      // 模拟后端的筛选逻辑
      let filteredList = [...this.alertList]
      
      // 应用搜索条件
      if (this.searchQuery) {
        filteredList = filteredList.filter(item => 
          item.device.toLowerCase().includes(this.searchQuery.toLowerCase())
        )
      }
      
      // 应用时间筛选
      if (this.startTime) {
        const startDate = new Date(this.startTime)
        filteredList = filteredList.filter(item => {
          const itemDate = new Date(`${item.date} ${item.time}`)
          return itemDate >= startDate
        })
      }
      
      if (this.endTime) {
        const endDate = new Date(this.endTime)
        filteredList = filteredList.filter(item => {
          const itemDate = new Date(`${item.date} ${item.time}`)
          return itemDate <= endDate
        })
      }
      
      // 更新总数
      this.total = filteredList.length
      
      // 分页处理
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      
      // 返回当前页数据
      this.alertList = filteredList.slice(start, end)
      this.loading = false
    }, 500) // 模拟网络延迟
  } catch (error) {
    console.error('获取警报列表失败:', error)
    this.$message.error('获取警报列表失败，请稍后重试')
    this.loading = false
      } finally {
        this.loading = false
      }
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

    // 处理页码大小变化
    handleSizeChange(size) {
      this.pageSize = size
      this.fetchAlertList()
    },

    // 处理页码变化
    handleCurrentChange(page) {
      this.currentPage = page
      this.fetchAlertList()
    },

    // 确认单个警报
    async handleConfirm(row) {
      try {
        await this.$confirm('确认处理该警报吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await this.confirmAlerts([row.id])
        this.$message.success('警报已确认')
        await this.fetchAlertList() // 刷新列表
      } catch (error) {
        if (error !== 'cancel') {
          console.error('确认警报失败:', error)
          this.$message.error('确认警报失败，请稍后重试')
        }
      }
    },

    // 批量确认警报
    async handleBatchConfirm() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请至少选择一项警报进行确认')
        return
      }

      try {
        await this.$confirm(`确定要批量确认选中的 ${this.selectedRows.length} 项警报吗？`, '批量确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const alertIds = this.selectedRows.map(row => row.id)
        await this.confirmAlerts(alertIds)
        this.$message.success(`已确认 ${this.selectedRows.length} 项警报`)
        await this.fetchAlertList() // 刷新列表
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量确认警报失败:', error)
          this.$message.error('批量确认警报失败，请稍后重试')
        }
      }
    },

    // API调用：确认警报（单个和批量共用）
    async confirmAlerts(alertIds) {
      return api.post('/alerts/confirm', {alertIds})
    },

    // 删除警报
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该警报吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await api.delete(`/alerts/${row.id}`)
        this.$message.success('警报已删除')
        await this.fetchAlertList() // 刷新列表
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除警报失败:', error)
          this.$message.error('删除警报失败，请稍后重试')
        } else {
          this.$message.info('已取消删除')
        }
      }
    },

    // 导出报表
    async exportReport() {
      try {
        this.loading = true
        const params = {
          device: this.searchQuery || undefined,
          startTime: this.startTime ? new Date(this.startTime).toISOString() : undefined,
          endTime: this.endTime ? new Date(this.endTime).toISOString() : undefined
        }

        // 使用blob方式处理文件下载
        const response = await api.get('/alerts/export', {
          params,
          responseType: 'blob'
        })

        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url

        // 从响应头获取文件名，如果没有则使用默认名称
        const filename = response.headers['content-disposition']
            ? response.headers['content-disposition'].split('filename=')[1].replace(/"/g, '')
            : '警报报表.xlsx'

        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        this.$message.success('报表导出成功')
      } catch (error) {
        console.error('导出报表失败:', error)
        this.$message.error('导出报表失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    // 选择行变化处理
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    // 严重性标签类型
    severityTagType(severity) {
      const types = {
        '一般提醒': 'success',
        '警告': 'danger',
        '维修': 'warning'
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