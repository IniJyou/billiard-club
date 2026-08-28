import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/home', name: 'Home', component: () => import('../views/Home.vue') }
  // 后续按模块新增路由：会员管理 /member、球桌管理 /table、开台结账 /billing、流水 /record 等
]

export default createRouter({
  history: createWebHistory(),
  routes
})
