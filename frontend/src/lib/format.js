export function formatDate(value, options = {}) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat('en-IN', options).format(date)
}

export function formatPercent(value, digits = 2) {
  if (value == null || !Number.isFinite(value)) return '—'
  const sign = value > 0 ? '+' : ''
  return `${sign}${value.toFixed(digits)}%`
}

export function priceChange(history = []) {
  if (history.length < 2) return null
  const newest = Number(history[0].price)
  const oldest = Number(history.at(-1).price)
  if (!Number.isFinite(newest) || !Number.isFinite(oldest) || oldest === 0) return null
  const change = newest - oldest
  return { change, changePercent: (change / oldest) * 100 }
}

export function toChartPoints(history = []) {
  const ordered = [...history].sort((a, b) => new Date(a.recordedAt) - new Date(b.recordedAt))
  const uniquePoints = new Map()
  for (const point of ordered) {
    const time = Math.floor(new Date(point.recordedAt).getTime() / 1000)
    const value = Number(point.price)
    if (Number.isFinite(time) && Number.isFinite(value)) uniquePoints.set(time, { time, value })
  }
  return [...uniquePoints.values()]
}