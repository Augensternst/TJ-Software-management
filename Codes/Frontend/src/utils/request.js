// src/utils/request.js
import axios from 'axios';

const request = axios.create({
  baseURL: 'http://localhost:8081', // 统一接口前缀,即后端端口
  timeout: 5000
});


// 添加请求拦截器
request.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token');
    
    // 如果 token 存在，添加 Authorization 头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

export default request;
