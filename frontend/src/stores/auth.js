import { defineStore } from 'pinia'
import { getCurrentUser, login as loginRequest, logout as logoutRequest } from '../api/auth'
import { setUnauthorizedHandler } from '../api/request'

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
      setUnauthorizedHandler(() => {
        this.user = null
        this.initialized = true
      })
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
    },
    clearSession() {
      this.user = null
      this.initialized = true
    }
  }
})
