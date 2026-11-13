import { useState } from 'react'
import { ArrowDownRight, ArrowLeft, ArrowUpRight, Star, TrendingUp } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { formatInr } from '../auth/currency.js'
import { useApiResource } from '../hooks/useApiResource.js'
import { formatDate, formatPercent, priceChange } from '../lib/format.js'
import { addStockToWatchlist } from '../services/watchlist.js'
import StockChart from '../components/StockChart.jsx'
import { Badge, Button, Card, EmptyState, ErrorNotice, LoadingState, PageHeading } from '../components/ui.jsx'

export default function StockDetailPage() {
  const { symbol = '' } = useParams()
  const navigate = useNavigate()
  const stockPath = `/api/stocks/${encodeURIComponent(symbol)}`
  const { data: stock, isLoading: stockLoading, error: stockError, refresh: refreshStock } = useApiResource(stockPath, 7000)
  const { data: history, isLoading: historyLoading, error: historyError, refresh: refreshHistory } = useApiResource(`${stockPath}/history`, 7000)
  const [notice, setNotice] = useState('')
  const [actionError, setActionError] = useState('')
  const [adding, setAdding] = useState(false)
  const change = priceChange(history || [])
  const positive = (change?.change ?? 0) >= 0

  async function addToWatchlist() {
    setAdding(true)
    setNotice('')
    setActionError('')
    try {
      await addStockToWatchlist(symbol)
      setNotice(`${symbol.toUpperCase()} added to your watchlist.`)
    } catch (error) {
      setActionError(error.message)
    } finally {
      setAdding(false)
    }
  }

  function refresh() {
    refreshStock()
    refreshHistory()
  }

  if (stockLoading && !stock) return <LoadingState label={`Loading ${symbol.toUpperCase()}`} />
  if (stockError && !stock) return <ErrorNotice message={stockError} onRetry={refresh} />

  return (
    <div className="page-stack">
      <button type="button" className="back-link" onClick={() => navigate(-1)}><ArrowLeft size={15} /> Back</button>
      {stock && (
        <>
          <PageHeading
            eyebrow={`${stock.exchange} · ${stock.sector}`}
            title={stock.companyName}
            description={stock.symbol}
            actions={<><Button variant="outline" onClick={addToWatchlist} disabled={adding}><Star size={15} /> {adding ? 'Saving…' : 'Watchlist'}</Button><Button onClick={() => navigate(`/trade?symbol=${encodeURIComponent(stock.symbol)}`)}><TrendingUp size={15} /> Trade</Button></>}
          />
          {(notice || actionError) && <div className={actionError ? 'inline-banner banner-error' : 'inline-banner banner-success'} role={actionError ? 'alert' : 'status'}>{actionError || notice}</div>}
          <div className="detail-summary-grid">
            <Card className="quote-card"><span className="section-kicker">LATEST SIMULATED PRICE</span><strong>{formatInr(stock.currentPrice)}</strong><div className="quote-change">{change ? <Badge tone={positive ? 'gain' : 'loss'}>{positive ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}{formatPercent(change.changePercent)}</Badge> : <Badge>Collecting price history</Badge>}<span>based on available history</span></div><small>Last updated {formatDate(stock.updatedAt, { hour: '2-digit', minute: '2-digit', second: '2-digit' })}</small></Card>
            <Card className="stock-facts"><span className="section-kicker">INSTRUMENT</span><dl><div><dt>Symbol</dt><dd>{stock.symbol}</dd></div><div><dt>Exchange</dt><dd>{stock.exchange}</dd></div><div><dt>Sector</dt><dd>{stock.sector}</dd></div><div><dt>Currency</dt><dd>INR</dd></div></dl></Card>
          </div>
          <Card className="chart-card"><div className="card-heading-row"><div><span className="section-kicker">SAVED TICKS</span><h2>Price history</h2></div><span className="history-count">{history?.length ?? 0} points</span></div>{historyLoading && !history ? <LoadingState label="Loading persisted price history" /> : historyError ? <ErrorNotice message={historyError} onRetry={refreshHistory} /> : history?.length ? <StockChart history={history} height={330} /> : <EmptyState title="No history available" description="The simulated market has not recorded a price point for this instrument yet." />}</Card>
        </>
      )}
    </div>
  )
}