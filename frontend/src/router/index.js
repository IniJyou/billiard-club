import { createRouter, createWebHistory } from 'vue-router'
import { pinia } from '../stores'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layouts/AppLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'members', name: 'Members', component: () => import('../views/Member.vue') },
      { path: 'tables', name: 'Tables', component: () => import('../views/Table.vue') },
      { path: 'records', name: 'Records', component: () => import('../views/Record.vue') },
      { path: 'reports', name: 'Reports', component: () => import('../views/Report.vue'), meta: { roles: [1] } },
      { path: 'forbidden', name: 'Forbidden', component: () => import('../views/Forbidden.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore(pinia)
  await auth.initialize()
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  if (requiresAuth && !auth.user) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (auth.user && to.meta.roles && !to.meta.roles.includes(auth.user.role)) {
    return '/forbidden'
  }
  if (to.path === '/login' && auth.user) {
    return '/home'
  }
  return true
})

export default router
