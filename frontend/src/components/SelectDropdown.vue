<template>
  <el-dropdown
    trigger="click"
    @command="handleCommand"
    style="display:flex;margin-right:2%"
    :hide-on-click="true"
    @visible-change="onDropdownVisibleChange"
  >
    <el-button class="custom-button" :class="{ clicked: isClicked }">
      <i class="el-icon-arrow-down"></i>
    </el-button>
    <!-- 下拉菜单 -->
    <template v-slot:dropdown>
      <el-dropdown-menu class="el-dropdown-menu">
        <!-- 搜索框 -->
        <div class="search-item">
          <el-input
            v-model="searchQuery"
            :placeholder="placeholder"
            style="flex:1;"
            @keyup.enter="onSearch"
          />
          <button @click="onSearch" style="height:80%;background-color: transparent;">🔍</button>
        </div>
        <!-- 列表 -->
        <el-dropdown-item v-for="item in items" :key="item.id" :command="item" class="content-item">
          {{ item.name }}
        </el-dropdown-item>
        <!-- 翻页控件 -->
        <div class="pagination-item">
          <div class="pagination-controls" style="display:flex;flex-direction: row;justify-content: center">
            <button @click="prevPage" v-if="currentPage !== 1" class="pagination-button">◄</button>
            <span style="color:#68F0EB;font-size:80%">{{ currentPage }} / {{ totalPages }}</span>
            <button @click="nextPage" v-if="currentPage !== totalPages" class="pagination-button">►</button>
          </div>
        </div>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
export default {
  props: {
    placeholder: String, // 搜索框的占位符
    items: Array, // 下拉列表数据
    currentPage: Number, // 当前页码
    totalPages: Number, // 总页数

  },
  data() {
    return {
      searchQuery: '', // 搜索关键字
      isClicked: false, // 是否点击
    };
  },
  methods: {
    handleCommand(command) {
      this.$emit('select', command); // 触发选择事件
    },
    onSearch() {
      this.$emit('search', this.searchQuery); // 触发搜索事件
    },
    prevPage() {
      this.$emit('prev-page'); // 触发上一页事件
    },
    nextPage() {
      this.$emit('next-page'); // 触发下一页事件
    },
    onDropdownVisibleChange(visible) {
      this.isClicked = visible;
      if(!visible){
         this.searchQuery='';
      }
    },
  },
};
</script>

<style scoped>
.custom-button{

    appearance: none;
    background-color: transparent;
    width:1vw;
    height:1vw;

    border:none;
    align-self: center;

    margin-right:2%;

    /*指针样式*/
    cursor: pointer;
      display: flex;
      justify-content: center;
      align-items: center;
}

.custom-button i {
    display:inline-block;
    background-image: url("@/assets/DataSimulation/arrow-right.svg");
    background-repeat: no-repeat;
    background-position: center center;
    background-size: contain;
    width: 1vw;
    height: 1vw;
    transition: transform 0.3s ease; /* 添加旋转动画 */
}

.custom-button:hover i {
  transform: rotate(90deg);
  transition: transform 0.3s ease;
}

/* 定义点击后的样式 */
.custom-button.clicked i {
  transform: rotate(90deg);
}


.el-dropdown-menu {
    min-width:220px;
    border:1px solid #68F0EB;
    background-color: #081028;
    border-radius:3px;
    list-style: none;
    padding: 5%; /* 去掉默认的内边距 */
    margin: 0; /* 去掉默认的外边距 */

}
:deep(.content-item){
    padding:0.3vw 0.3vw;
    color:#68F0EB;
    cursor:pointer;
    transition:background-color 0.3s ease;
    border: none;
}

:deep(.content-item:hover ) {
  background-color:  #1a2a4a; /* 悬停时的背景色 */
}


.pagination-button{
    background-color: #081028;
    color:#68F0EB;

}

.search-item{
    display:flex;
    justify-content: space-between;
    align-items: center;
    width:100%;

}


</style>