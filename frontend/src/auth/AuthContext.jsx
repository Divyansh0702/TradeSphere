import { useEffect, useState } from 'react'
import { AuthContext } from './context.js'
import { apiRequest } from './api.js'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let active = true
    const handleExpiredSession = () => {
      if (active) setUser(null)
    }

    window.addEventListener('auth:expired', handleExpiredSession)
    apiRequest('/api/users/me')
      .then((profile) => { if (active) setUser(profile) })
      .catch(() => { if (active) setUser(null) })
      .finally(() => { if (active) setIsLoading(false) })
    return () => {
      active = false
      window.removeEventListener('auth:expired', handleExpiredSession)
    }
  }, [])

  async function authenticate(path, values) {
    const response = await apiRequest(path, { method: 'POST', body: JSON.stringify(values) })
    setUser(response.user)
    return response.user
  }

  async function login(values) {
    return authenticate('/api/auth/login', values)
  }

  async function register(values) {
    return authenticate('/api/auth/register', values)
  }

  async function logout() {
    try {
      await apiRequest('/api/auth/logout', { method: 'POST' })
    } finally {
      setUser(null)
    }
  }

  return <AuthContext.Provider value={{ user, isLoading, login, register, logout }}>{children}</AuthContext.Provider>
}
