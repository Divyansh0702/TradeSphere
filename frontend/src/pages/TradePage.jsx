import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ArrowDownRight, ArrowUpRight, CheckCircle2, Info } from 'lucide-react'
import { apiRequest } from '../auth/api.js'
import { formatInr } from '../auth/currency.js'
import { useApiResource } from '../hooks/useApiResource.js'
import { useStockSnapshot } from '../hooks/useStockSnapshot.js'
import { formatDate } from '../lib/format.js'
import { Badge, Button, Card, ErrorNotice, Input, LoadingState, PageHeading } from '../components/ui.jsx'

export default function TradePage() {
  const [searchParams] = useSearchParams()
  const [symbol, setSymbol] = useState(searchParams.get('symbol')?.toUpperCase() || '')
  const [side, setSide] = useState('BUY')
  const [quantity, setQuantity] = useState('1')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [order, setOrder] = useState(null)
  const { stocks, isLoading: stocksLoading, error: stocksError } = useStockSnapshot('', 7000)
  const { data: wallet, isLoading: walletLoading, error: walletError, refresh: refreshWallet } = useApiResource('/api/wallet', 7000)
  const { data: holdings, isLoading: holdingsLoading, refresh: refreshHoldings } = useApiResource('/api/portfolio/holdings', 7000)
  const selectedStock = stocks.find((stock) => stock.symbol === symbol)
  const quantityNumber = Number(quantity)
  const estimatedTotal = selectedStock && Number.isInteger(quantityNumber) && quantityNumber > 0
    ? Number(selectedStock.currentPrice) * quantityNumber
    : null
  const heldQuantity = holdings?.find((holding) => holding.symbol === symbol)?.quantity || 0
  const insufficientCash = side === 'BUY' && estimatedTotal != null && Number(wallet?.balance || 0) < estimatedTotal
  const insufficientShares = side === 'SELL' && Number.isInteger(quantityNumber) && quantityNumber > heldQuantity

  async function submit(event) {
    event.preventDefault()
    setError('')
    setOrder(null)
    if (!symbol || !Number.isInteger(quantityNumber) || quantityNumber < 1 || quantityNumber > 1000000) {
      setError('Choose a stock and enter a whole-share quantity between 1 and 1,000,000.')
      return
    }
    if (insufficientCash) {
      setError('Your virtual wallet balance is below this estimated order value.')
      return
    }
    if (insufficientShares) {
      setError(`You currently own ${heldQuantity} ${symbol} shares.`)
      return
    }

    setIsSubmitting(true)
    try {
      const idempotencyKey = crypto.randomUUID()
      const result = await apiRequest(`/api/orders/${side.toLowerCase()}`, {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotencyKey },
        body: JSON.stringify({ symbol, quantity: quantityNumber }),
      })
      setOrder(result)
      refreshWallet()
      refreshHoldings()
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="page-stack">
      <PageHeading eyebrow="SIMULATED EXECUTION · INR" title="Place an order" description="Orders execute immediately at the backend’s current simulated price. No client-supplied price is sent." />
      {(stocksError || walletError) && <ErrorNotice message={stocksError || walletError} />}
      <div className="trade-layout">
        <Card className="trade-ticket">
          <div className="card-heading-row"><div><span className="section-kicker">MARKET ORDER</span><h2>Trade ticket</h2></div><Badge>Simulated</Badge></div>
          <form onSubmit={submit} className="trade-form">
            <div className="trade-side-control" role="group" aria-label="Order side">
              <button type="button" className={side === 'BUY' ? 'side-option side-option-buy selected' : 'side-option side-option-buy'} onClick={() => { setSide('BUY'); setError(''); setOrder(null) }}><ArrowDownRight size={16} /> Buy</button>
              <button type="button" className={side === 'SELL' ? 'side-option side-option-sell selected' : 'side-option side-option-sell'} onClick={() => { setSide('SELL'); setError(''); setOrder(null) }}><ArrowUpRight size={16} /> Sell</button>
            </div>

            <div className="form-field"><label htmlFor="trade-symbol">Stock</label><select id="trade-symbol" className="ui-input" value={symbol} onChange={(event) => { setSymbol(event.target.value); setError(''); setOrder(null) }} required>
              <option value="">{stocksLoading ? 'Loading market…' : 'Select a stock'}</option>
              {stocks.map((stock) => <option key={stock.symbol} value={stock.symbol}>{stock.symbol} · {stock.companyName}</option>)}
            </select></div>

            {selectedStock && <div className="selected-quote"><span>Current simulated price</span><strong>{formatInr(selectedStock.currentPrice)}</strong><small>{selectedStock.companyName} · {selectedStock.exchange}</small></div>}

            <div className="form-field"><label htmlFor="trade-quantity">Quantity</label><Input id="trade-quantity" type="number" min="1" max="1000000" step="1" inputMode="numeric" value={quantity} onChange={(event) => { setQuantity(event.target.value); setError(''); setOrder(null) }} required /><small>{side === 'SELL' ? `${heldQuantity} shares available` : 'Whole shares only'}</small></div>

            <div className="trade-estimate"><span>Estimated order value</span><strong>{estimatedTotal == null ? '—' : formatInr(estimatedTotal)}</strong><small>Final fill price is determined by the backend at execution.</small></div>
            {walletLoading ? <LoadingState label="Checking wallet balance" /> : <div className="trade-balance"><span>Available virtual balance</span><strong>{formatInr(wallet?.balance || 0)}</strong></div>}
            {insufficientCash && <p className="inline-error">Not enough virtual funds for this estimated order.</p>}
            {insufficientShares && <p className="inline-error">Your holdings do not contain enough shares to sell.</p>}
            {error && <p className="form-error" role="alert">{error}</p>}
            <Button className="trade-submit" type="submit" disabled={isSubmitting || walletLoading || stocksLoading || !selectedStock || insufficientCash || insufficientShares}>
              {isSubmitting ? 'Executing…' : `Confirm ${side.toLowerCase()}`}
            </Button>
            <p className="trade-disclaimer"><Info size={14} /> Virtual funds only. This order does not reach an exchange.</p>
          </form>
        </Card>

        <div className="trade-side-column">
          <Card className="trade-help-card"><span className="section-kicker">EXECUTION RULES</span><h2>Backend-priced fills</h2><p>Your request includes only a stock symbol and whole-share quantity. The server validates current funds or holdings and executes atomically.</p><div className="trade-rule"><span>Available balance</span><strong>{walletLoading ? 'Loading…' : formatInr(wallet?.balance || 0)}</strong></div><div className="trade-rule"><span>Selected position</span><strong>{holdingsLoading ? 'Loading…' : `${heldQuantity} shares`}</strong></div></Card>
          {order && <Card className="order-confirmation" role="status"><div className="confirmation-title"><CheckCircle2 size={19} /><div><span>Order executed</span><strong>{order.side} {order.symbol}</strong></div></div><dl><div><dt>Quantity</dt><dd>{order.quantity}</dd></div><div><dt>Fill price</dt><dd>{formatInr(order.executionPrice)}</dd></div><div><dt>Total value</dt><dd>{formatInr(order.totalAmount)}</dd></div><div><dt>Executed</dt><dd>{formatDate(order.createdAt, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}</dd></div></dl></Card>}
        </div>
      </div>
    </div>
  )
}