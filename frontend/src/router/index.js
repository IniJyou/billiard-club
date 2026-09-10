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
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue'), meta: { roles: [1, 2] } },
      { path: 'members', name: 'Members', component: () => import('../views/Member.vue'), meta: { roles: [1, 2] } },
      { path: 'tables', name: 'Tables', component: () => import('../views/Table.vue'), meta: { roles: [1, 2] } },
      { path: 'records', name: 'Records', component: () => import('../views/Record.vue'), meta: { roles: [1, 2] } },
      { path: 'reservations', name: 'StaffReservations', component: () => import('../views/StaffReservation.vue'), meta: { roles: [1, 2] } },
      { path: 'reports', name: 'Reports', component: () => import('../views/Report.vue'), meta: { roles: [1] } },
      { path: 'user/home', name: 'UserHome', component: () => import('../views/UserHome.vue'), meta: { roles: [3] } },
      { path: 'user/membership', name: 'UserMembership', component: () => import('../views/UserMembership.vue'), meta: { roles: [3] } },
      { path: 'user/reservations', name: 'UserReservations', component: () => import('../views/UserReservation.vue'), meta: { roles: [3] } },
      { path: 'user/records', name: 'UserRecords', component: () => import('../views/UserRecords.vue'), meta: { roles: [3] } },
      { path: 'user/profile', name: 'UserProfile', component: () => import('../views/UserProfile.vue'), meta: { roles: [3] } },
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
  if (to.path === '/home' && auth.isUser) {
    return '/user/home'
  }
  if (auth.user && to.meta.roles && !to.meta.roles.includes(auth.user.role)) {
    return '/forbidden'
  }
  if (to.path === '/login' && auth.user) {
    return auth.defaultPath
  }
  return true
})

export default router
