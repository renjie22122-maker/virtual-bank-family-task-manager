import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { api, money } from './api.js'

describe('api client', () => {
  const storage = new Map()

  beforeEach(() => {
    storage.clear()
    vi.stubGlobal('localStorage', {
      getItem: key => storage.get(key) ?? null,
      setItem: (key, value) => storage.set(key, value),
      removeItem: key => storage.delete(key),
    })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('adds the bearer token and handles an empty response', async () => {
    localStorage.setItem('familyflow-token', 'demo-token')
    const fetchMock = vi.fn().mockResolvedValue({ status: 204 })
    vi.stubGlobal('fetch', fetchMock)

    await expect(api('/api/auth/logout', { method: 'POST' })).resolves.toBeNull()
    expect(fetchMock).toHaveBeenCalledWith('/api/auth/logout', expect.objectContaining({
      headers: expect.objectContaining({ Authorization: 'Bearer demo-token' }),
    }))
  })

  it('clears an invalid token when the API rejects it', async () => {
    localStorage.setItem('familyflow-token', 'expired-token')
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      status: 403,
      ok: false,
      json: vi.fn().mockResolvedValue({ message: 'Session expired' }),
    }))

    await expect(api('/api/dashboard')).rejects.toThrow('Session expired')
    expect(localStorage.getItem('familyflow-token')).toBeNull()
  })
})

describe('money', () => {
  it('formats values as US dollars', () => {
    expect(money(12.5)).toBe('$12.50')
  })
})
