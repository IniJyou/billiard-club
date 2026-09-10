import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const mocks = vi.hoisted(() => ({
  getCurrentUser: vi.fn(), login: vi.fn(), logout: vi.fn(), register: vi.fn(), setUnauthorizedHandler: vi.fn()
}))
vi.mock('../api/auth', () => ({
  getCurrentUser: mocks.getCurrentUser, login: mocks.login, logout: mocks.logout, register: mocks.register
}))
vi.mock('../api/request', () => ({ setUnauthorizedHandler: mocks.setUnauthorizedHandler }))

import { useAuthStore } from './auth'

describe('auth store', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('restores current user and registers the 401 reset handler', async () => {
    mocks.getCurrentUser.mockResolvedValue({ data: { id: 1, role: 1, username: 'admin' } })
    const store = useAuthStore()
    await store.initialize()
    expect(store.isAdmin).toBe(true)
    const handler = mocks.setUnauthorizedHandler.mock.calls.at(-1)[0]
    handler()
    expect(store.user).toBeNull()
    expect(store.initialized).toBe(true)
  })

  it('keeps the local session cleared even if logout request fails', async () => {
    mocks.logout.mockRejectedValue(new Error('network'))
    const store = useAuthStore()
    store.user = { id: 2, role: 2 }
    await expect(store.logout()).rejects.toThrow('network')
    expect(store.user).toBeNull()
  })

  it('registers a user and selects the user portal as default path', async () => {
    mocks.register.mockResolvedValue({ data: { id: 4, role: 3, username: '13800000000' } })
    const store = useAuthStore()
    await store.register({ realName: '测试用户', phone: '13800000000', password: '123456' })
    expect(store.isUser).toBe(true)
    expect(store.isStaff).toBe(false)
    expect(store.defaultPath).toBe('/user/home')
  })
})
