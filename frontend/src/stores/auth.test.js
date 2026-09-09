import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const mocks = vi.hoisted(() => ({
  getCurrentUser: vi.fn(), login: vi.fn(), logout: vi.fn(), setUnauthorizedHandler: vi.fn()
}))
vi.mock('../api/auth', () => ({
  getCurrentUser: mocks.getCurrentUser, login: mocks.login, logout: mocks.logout
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
})
