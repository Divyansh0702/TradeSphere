import { useEffect, useRef } from 'react'
import { AreaSeries, ColorType, createChart } from 'lightweight-charts'
import { formatInr } from '../auth/currency.js'
import { toChartPoints } from '../lib/format.js'
import { useTheme } from '../theme/useTheme.js'

export default function StockChart({ history, height = 300 }) {
  const containerRef = useRef(null)
  const { theme } = useTheme()

  useEffect(() => {
    const container = containerRef.current
    const points = toChartPoints(history)
    if (!container || points.length === 0) return undefined
    const light = theme === 'light'
    const textColor = light ? '#64748b' : '#94a3b8'
    const gridColor = light ? 'rgba(0, 0, 0, 0.05)' : 'rgba(255, 255, 255, 0.04)'
    const lineColor = light ? '#059669' : '#10b981'

    const chart = createChart(container, {
      width: container.clientWidth,
      height,
      layout: { background: { type: ColorType.Solid, color: 'transparent' }, textColor, fontFamily: 'JetBrains Mono, Plus Jakarta Sans, sans-serif', fontSize: 11 },
      grid: { vertLines: { color: gridColor }, horzLines: { color: gridColor } },
      rightPriceScale: { borderColor: light ? 'rgba(0, 0, 0, 0.08)' : 'rgba(255, 255, 255, 0.08)' },
      timeScale: { borderColor: light ? 'rgba(0, 0, 0, 0.08)' : 'rgba(255, 255, 255, 0.08)', timeVisible: true, secondsVisible: false },
      crosshair: { vertLine: { color: light ? 'rgba(5, 150, 105, 0.35)' : 'rgba(16, 185, 129, 0.45)' }, horzLine: { color: light ? 'rgba(5, 150, 105, 0.3)' : 'rgba(16, 185, 129, 0.35)' } },
      handleScroll: true,
      handleScale: true,
    })
    const series = chart.addSeries(AreaSeries, {
      lineColor,
      topColor: light ? 'rgba(5, 150, 105, 0.22)' : 'rgba(16, 185, 129, 0.28)',
      bottomColor: light ? 'rgba(5, 150, 105, 0.0)' : 'rgba(16, 185, 129, 0.0)',
      lineWidth: 2.5,
      priceFormat: { type: 'custom', minMove: 0.01, formatter: (price) => formatInr(price) },
      priceLineVisible: false,
      lastValueVisible: true,
    })
    series.setData(points)
    chart.timeScale().fitContent()
    const observer = new ResizeObserver(([entry]) => {
      if (entry?.contentRect.width) chart.applyOptions({ width: entry.contentRect.width })
    })
    observer.observe(container)

    return () => {
      observer.disconnect()
      chart.remove()
    }
  }, [height, history, theme])

  return <div ref={containerRef} className="stock-chart" style={{ height }} role="img" aria-label="Historical simulated stock prices in Indian rupees" />
}