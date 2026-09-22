const baseUrl = import.meta.env.VITE_API_URL || ''

export async function api(path, options = {}) {
  const token = localStorage.getItem('familyflow-token')
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  if (response.status === 204) return null
  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    if (response.status === 403 && token) localStorage.removeItem('familyflow-token')
    throw new Error(data.message || 'The request could not be completed.')
  }
  return data
}

export const money = value => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value || 0)
export const dateTime = value => value ? new Intl.DateTimeFormat('en', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : 'Not set'
