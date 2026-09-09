import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('../api/auth', () => ({
  getCurrentUser: vi.fn(), login: vi.fn(), logout: vi.fn()
}))
vi.mock('../api/request', () => ({ setUnauthorizedHandler: vi.fn() }))

import router from './index'
import { pinia } from '../stores'
import { useAuthStore } from '../stores/auth'

describe('router permissions', () => {
  beforeEach(async () => {
    const auth = useAuthStore(pinia)
    auth.clearSession()
    await router.replace('/login')
  })

  it('redirects cashier away from reports and allows administrator', async () => {
    const auth = useAuthStore(pinia)
    auth.user = { id: 2, role: 2 }
    await router.push('/reports')
    expect(router.currentRoute.value.path).toBe('/forbidden')

    auth.user = { id: 1, role: 1 }
    await router.push('/reports')
    expect(router.currentRoute.value.path).toBe('/reports')
  })
})
