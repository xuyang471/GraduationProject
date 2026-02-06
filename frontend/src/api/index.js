// src/api/index.js
import axios from 'axios'

// 创建 axios 实例
const api = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
  timeout: 10000,
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 添加 token 到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API 请求错误:', error)
    return Promise.reject(error)
  }
)

// API 方法定义
export default {
  // 获取首页统计数据
  getHomeStatistics() {
    return api.get('/statistics/home')
  },

  // 获取最近失物信息
  getRecentLostItems() {
    return api.get('/items/recent')
  },

  // 获取通知信息
  getNotifications() {
    return api.get('/notifications')
  }
}
