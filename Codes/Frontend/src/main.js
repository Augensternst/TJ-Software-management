import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'particles.js' // 正确引入 particles.js
import ElementPlus from 'element-plus'  //这里引入了ElementPlus，
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs' // 中文语言包
import '@/assets/fonts/fonts.css'

const app = createApp(App);


app.use(ElementPlus,{locale:zhCn})
app.use(router)
app.mount('#app')