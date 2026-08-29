import axios from 'axios'

// 统一 axios 实例：baseURL=/api 会走 vite 代理到后端 8080
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

// 响应拦截：后端统一返回 { code, message, data }
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络请求失败'
    const normalizedError = new Error(message)
    normalizedError.status = status
    if (status === 401 && window.location.pathname !== '/login') {
      window.location.replace('/login')
    }
    return Promise.reject(normalizedError)
  }
)

export default request
