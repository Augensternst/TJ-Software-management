// src/api/user.js
import request from '@/utils/request';

// 登录
export function loginUser(username, password) {
  return request.post('/user/account/token/', null, {
    params: { username, password }
  });
}

// 注册
export function registerUser(data) {
  return request.post('/user/account/register/', null, {
    params: data
  });
}

// 登录信息
export function getUserInfo(token) {
  return request.get('/user/account/info/',null,{params:token});
}

