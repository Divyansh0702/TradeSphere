import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowDownRight, ArrowRight, ArrowUpRight, RefreshCw, WalletCards } from 'lucide-react'
import { apiRequest } from '../auth/api.js'
import { formatInr } from '../auth/currency.js'
import { useApiResource } from '../hooks/useApiResource.js'
import { useStockSnapshot } from '../hooks/useStockSnapshot.js'
import { formatDate, formatPercent } from '../lib/format.js'
import { Button, Card, EmptyState, ErrorNotice, Input, LoadingState, PageHeading } from '../components/ui.jsx'

function MetricCard({ label, value, note, icon: Icon, tone }) {
  return (
    <Card className="metric-card">
      <div className="metric-top"><span>{label}</span><span className={`metric-icon ${tone}`}><Icon size={17} /></span></div>
      <strong className="metric-value">{value}</strong>
      <span className="metric-note">{note}</span>
    </Card>
  )
}

function SignedAmount({ value }) {
  const amount = Number(value || 0)
  return <span className={amount >= 0 ? 'gain' : 'loss'}>{amount > 0 ? '+' : ''}{formatInr(amount)}</span>
}

function WalletTopUp({ balance, onComplete }) {
  const [isOpen, setIsOpen] = useState(false)
  const [amount, setAmount] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function submit(event) {
    event.preventDefault()
    const value = Number(amount)
    if (!Number.isFinite(value) || value < 1 || value > 10000000) {
      setError('Enter an amount between ₹1 and ₹1,00,00,000.')
      return
    }
    setError('')
    setMessage('')
    setIsSubmitting(true)
    try {
      const wallet = await apiRequest('/api/wallet/top-ups', {
        method: 'POST',
        body: JSON.stringify({ amount: value }),
      })
      setMessage(`Wallet updated to ${formatInr(wallet.balance)}.`)
      setAmount('')
      onComplete()
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="wallet-topup">
      {!isOpen ? (
        <Button variant="outline" size="sm" onClick={() => setIsOpen(true)}>Add virtual funds</Button>
      ) : (
        <form className="topup-form" onSubmit={submit}>
          <label htmlFor="topup-amount">Add virtual INR</label>
          <div className="topup-row">
            <Input id="topup-amount" type="number" min="1" max="10000000" step="0.01" value={amount} onChange={(event) => setAmount(event.target.value)} placeholder="Amount" required />
            <Button type="submit" size="sm" disabled={isSubmitting}>{isSubmitting ? 'Adding…' : 'Add funds'}</Button>
            <Button type="button" variant="ghost" size="sm" onClick={() => { setIsOpen(false); setError('') }}>Cancel</Button>
          </div>
          {error && <p className="inline-error" role="alert">{error}</p>}
          {message && <p className="inline-success" role="status">{message}</p>}
        </form>
      )}
      <span className="wallet-mini-balance">Available {formatInr(balance)}</span>
    </div>
  )
}

function RecentOrders({ orders, isLoading }) {
  if (isLoading && !orders) return <LoadingState label="Loading transactions" />
  if (!orders?.length) return <EmptyState title="No trades yet" description="Executed buy and sell orders will appear here." />
  return (
    <div className="recent-orders">
      {orders.slice(0, 5).map((order) => (
        <div className="recent-order-row" key={order.id}>
          <span className={`order-side-icon ${order.side === 'BUY' ? 'side-buy' : 'side-sell'}`}>{order.side === 'BUY' ? <ArrowDownRight size={16} /> : <ArrowUpRight size={16} />}</span>
          <div className="recent-order-copy"><strong>{order.side} {order.symbol}</strong><span>{order.quantity} shares · {formatDate(order.createdAt, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}</span></div>
          <span className="recent-order-value">{formatInr(order.totalAmount)}</span>
        </div>
      ))}
    </div>
  )
}

export default function DashboardPage() {
  const navigate = useNavigate()
  const { data: summary, isLoading: summaryLoading, error: summaryError, refresh: refreshSummary } = useApiResource('/api/portfolio/summary', 10000)
  const { data: orders, isLoading: ordersLoading, error: ordersError } = useApiResource('/api/orders', 15000)
  const { stocks, isLoading: stocksLoading, error: stocksError, refresh: refreshStocks } = useStockSnapshot('', 15000)
  const [refreshing, setRefreshing] = useState(false)
  const movers = [...stocks].filter((stock) => stock.changePercent != null).sort((a, b) => b.changePercent - a.changePercent)
  const validMoves = stocks.filter((stock) => stock.changePercent != null)
  const basketChange = validMoves.length
    ? validMoves.reduce((sum, stock) => sum + stock.changePercent, 0) / validMoves.length
    : null
  const featuredStocks = [...new Map([
    ...movers.slice(0, 3),
    ...[...movers].reverse().slice(0, 3),
  ].map((stock) => [stock.symbol, stock])).values()]

  function refreshAll() {
    setRefreshing(true)
    refreshSummary()
    refreshStocks()
    window.setTimeout(() => setRefreshing(false), 500)
  }

  return (
    <div className="page-stack">
      <PageHeading
        eyebrow="PORTFOLIO ACTIVITY"
        title="Your overview"
        description="A live view of your simulated investments and the NSE basket."
        actions={<Button variant="outline" onClick={refreshAll} disabled={refreshing}><RefreshCw size={15} className={refreshing ? 'spin-icon' : ''} /> Refresh</Button>}
      />

      {(summaryError || stocksError) && <ErrorNotice message={summaryError || stocksError} onRetry={refreshAll} />}
      {summaryLoading && !summary ? <LoadingState label="Loading your portfolio" /> : summary && (
        <div className="metric-grid">
          <MetricCard label="Portfolio value" value={formatInr(summary.currentValue)} note="Current value of open holdings" icon={WalletCards} tone="icon-lime" />
          <MetricCard label="Available wallet" value={formatInr(summary.walletBalance)} note="Virtual funds available to trade" icon={WalletCards} tone="icon-cyan" />
          <MetricCard label="Invested" value={formatInr(summary.investedValue)} note="Current holding cost basis" icon={ArrowDownRight} tone="icon-gold" />
          <MetricCard label="Realized P&L" value={<SignedAmount value={summary.realizedPnl} />} note="Closed positions" icon={summary.realizedPnl >= 0 ? ArrowUpRight : ArrowDownRight} tone={summary.realizedPnl >= 0 ? 'icon-lime' : 'icon-coral'} />
          <MetricCard label="Unrealized P&L" value={<SignedAmount value={summary.unrealizedPnl} />} note="Open positions" icon={summary.unrealizedPnl >= 0 ? ArrowUpRight : ArrowDownRight} tone={summary.unrealizedPnl >= 0 ? 'icon-lime' : 'icon-coral'} />
        </div>
      )}

      {summary && <Card className="wallet-card"><div><span className="section-kicker">PAPER WALLET · INR</span><strong>{formatInr(summary.walletBalance)}</strong><small>These funds are simulated and have no cash value.</small></div><WalletTopUp balance={summary.walletBalance} onComplete={refreshSummary} /></Card>}

      <div className="dashboard-grid">
        <Card className="market-overview-card">
          <div className="card-heading-row"><div><span className="section-kicker">NSE SIMULATED BASKET</span><h2>Market overview</h2></div><Button variant="ghost" size="sm" onClick={() => navigate('/market')}>All stocks <ArrowRight size={14} /></Button></div>
          {stocksLoading && !stocks.length ? <LoadingState label="Loading market" /> : stocksError ? <ErrorNotice message={stocksError} onRetry={refreshStocks} /> : (
            <>
              <div className="market-pulse"><strong className={basketChange == null ? 'change-neutral' : basketChange >= 0 ? 'gain' : 'loss'}>{formatPercent(basketChange)}</strong><span>mean move · {stocks.length} listed instruments</span></div>
              <div className="mover-list">
                {movers.slice(0, 4).map((stock) => (
                  <Link className="mover-row" to={`/stocks/${encodeURIComponent(stock.symbol)}`} key={stock.symbol}>
                    <span className="mover-symbol">{stock.symbol}</span><span className="mover-company">{stock.companyName}</span><strong>{formatInr(stock.currentPrice)}</strong><span className={stock.changePercent >= 0 ? 'gain' : 'loss'}>{formatPercent(stock.changePercent)}</span>
                  </Link>
                ))}
                {!movers.length && <div className="market-no-history">Market change appears as persisted price history accumulates.</div>}
              </div>
            </>
          )}
        </Card>

        <Card className="transactions-card">
          <div className="card-heading-row"><div><span className="section-kicker">ACCOUNT ACTIVITY</span><h2>Recent transactions</h2></div><Button variant="ghost" size="sm" onClick={() => navigate('/orders')}>History <ArrowRight size={14} /></Button></div>
          {ordersError ? <ErrorNotice message={ordersError} /> : <RecentOrders orders={orders} isLoading={ordersLoading} />}
        </Card>
      </div>

      <Card className="movers-card">
        <div className="card-heading-row"><div><span className="section-kicker">PERSISTED PRICE HISTORY</span><h2>Top movers</h2></div><Link className="text-link" to="/market">Explore market <ArrowRight size={14} /></Link></div>
        {stocksLoading && !stocks.length ? <LoadingState label="Calculating price changes" /> : (
          <div className="mover-grid">
            {featuredStocks.map((stock) => <Link className="mover-card" to={`/stocks/${encodeURIComponent(stock.symbol)}`} key={stock.symbol}><span>{stock.symbol}</span><strong>{formatInr(stock.currentPrice)}</strong><small className={stock.changePercent >= 0 ? 'gain' : 'loss'}>{formatPercent(stock.changePercent)}</small></Link>)}
            {!featuredStocks.length && <EmptyState title="Collecting market history" description="Top movers will appear after the simulated market records another price point." />}
          </div>
        )}
      </Card>
    </div>
  )
}