import { apiRequest } from '../auth/api.js'

export async function getOrCreateWatchlist() {
  try {
    return await apiRequest('/api/watchlists/me')
  } catch (error) {
    if (error.status !== 404) throw error
  }

  try {
    return await apiRequest('/api/watchlists', { method: 'POST' })
  } catch (error) {
    if (error.status !== 409) throw error
    return apiRequest('/api/watchlists/me')
  }
}

export async function addStockToWatchlist(symbol) {
  try {
    return await apiRequest(`/api/watchlists/me/stocks/${encodeURIComponent(symbol)}`, { method: 'POST' })
  } catch (error) {
    if (error.status !== 404) throw error
    await getOrCreateWatchlist()
    return apiRequest(`/api/watchlists/me/stocks/${encodeURIComponent(symbol)}`, { method: 'POST' })
  }
}

export function removeStockFromWatchlist(symbol) {
  return apiRequest(`/api/watchlists/me/stocks/${encodeURIComponent(symbol)}`, { method: 'DELETE' })
}