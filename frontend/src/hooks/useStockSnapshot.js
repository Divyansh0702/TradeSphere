import { useEffect, useState } from 'react'
import { apiRequest } from '../auth/api.js'
import { priceChange } from '../lib/format.js'

export function useStockSnapshot(query = '', refreshMs = 12000) {
  const [stocks, setStocks] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [refreshIndex, setRefreshIndex] = useState(0)

  useEffect(() => {
    let active = true
    let running = false
    let timeoutId

    async function refresh() {
      if (running) return
      running = true
      try {
        const search = query.trim() ? `?query=${encodeURIComponent(query.trim())}` : ''
        const result = await apiRequest(`/api/stocks${search}`)
        const snapshots = await Promise.all(result.map(async (stock) => {
          try {
            const history = await apiRequest(`/api/stocks/${encodeURIComponent(stock.symbol)}/history`)
            return { ...stock, ...priceChange(history), history }
          } catch {
            return { ...stock, change: null, changePercent: null, history: [] }
          }
        }))
        if (active) {
          setStocks(snapshots)
          setError('')
        }
      } catch (requestError) {
        if (active) setError(requestError.message)
      } finally {
        running = false
        if (active) setIsLoading(false)
      }
    }

    const initialDelay = query ? 250 : 0
    timeoutId = window.setTimeout(() => {
      refresh()
      if (refreshMs > 0) timeoutId = window.setInterval(refresh, refreshMs)
    }, initialDelay)

    return () => {
      active = false
      window.clearTimeout(timeoutId)
      window.clearInterval(timeoutId)
    }
  }, [query, refreshMs, refreshIndex])

  return { stocks, isLoading, error, refresh: () => setRefreshIndex((index) => index + 1) }
}