const apiBaseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export class ApiError extends Error {
  constructor(message, status, details = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

export async function apiRequest(path, options = {}) {
  const headers = new Headers(options.headers)
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    credentials: 'include',
    headers,
  })
  const body = response.status === 204 ? null : await response.json().catch(() => null)

  if (!response.ok) {
    const validationMessage = body?.validationErrors
      ? Object.values(body.validationErrors).join(' ')
      : null
    const message = validationMessage || body?.message || `Request failed (${response.status})`
    if (response.status === 401 && !path.startsWith('/api/auth/')) {
      window.dispatchEvent(new CustomEvent('auth:expired'))
    }
    throw new ApiError(message, response.status, body)
  }
  return body
}