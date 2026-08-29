import { defineStore } from 'pinia'
import { getCurrentUser, login as loginRequest, logout as logoutRequest } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    initialized: false
  }),
  getters: {
    isAdmin: (state) => state.user?.role === 1
  },
  actions: {
    async initialize() {
      if (this.initialized) return this.user
      try {
        const response = await getCurrentUser()
        this.user = response.data
      } catch {
        this.user = null
      } finally {
        this.initialized = true
      }
      return this.user
    },
    async login(payload) {
      const response = await loginRequest(payload)
      this.user = response.data
      this.initialized = true
      return this.user
    },
    async logout() {
      try {
        await logoutRequest()
      } finally {
        this.user = null
        this.initialized = true
      }
    }
  }
})
