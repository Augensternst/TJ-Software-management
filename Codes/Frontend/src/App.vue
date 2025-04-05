<template>
  <div id="app">
    <router-view></router-view> <!-- 路由视图 -->
  </div>
</template>

<script>
export default {
  name: 'App',
};
</script>

<style>

/*消灭页边距 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100%; 
  width: 100%;
  position: relative;
  overflow-y: auto;
  text-align: center;
}
</style>