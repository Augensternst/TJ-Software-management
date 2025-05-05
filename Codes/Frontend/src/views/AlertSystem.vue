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