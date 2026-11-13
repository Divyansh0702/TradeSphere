import { useCallback, useEffect, useRef, useState } from 'react'
import { apiRequest } from '../auth/api.js'

export function useApiResource(path, refreshMs = 0) {
  const [data, setData] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [refreshIndex, setRefreshIndex] = useState(0)
  const previousPath = useRef(path)

  useEffect(() => {
    let active = true
    let running = false
    let intervalId
    if (previousPath.current !== path) {
      previousPath.current = path
      setData(null)
      setIsLoading(true)
      setError('')
    }

    async function load() {
      if (running) return
      running = true
      try {
        const result = await apiRequest(path)
        if (active) {
          setData(result)
          setError('')
        }
      } catch (requestError) {
        if (active) setError(requestError.message)
      } finally {
        running = false
        if (active) setIsLoading(false)
      }
    }

    load()
    if (refreshMs > 0) intervalId = window.setInterval(load, refreshMs)
    return () => {
      active = false
      window.clearInterval(intervalId)
    }
  }, [path, refreshMs, refreshIndex])

  const refresh = useCallback(() => setRefreshIndex((index) => index + 1), [])
  return { data, isLoading, error, refresh }
}