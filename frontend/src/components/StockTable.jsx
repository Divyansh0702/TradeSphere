import { Link } from 'react-router-dom'
import { ArrowDownRight, ArrowUpRight, Star, X } from 'lucide-react'
import { formatInr } from '../auth/currency.js'
import { formatDate, formatPercent } from '../lib/format.js'
import { Button, EmptyState, ErrorNotice, LoadingState } from './ui.jsx'

function PriceChange({ value }) {
  if (value == null) return <span className="change-neutral">—</span>
  const positive = value >= 0
  const Icon = positive ? ArrowUpRight : ArrowDownRight
  return <span className={`price-change ${positive ? 'gain' : 'loss'}`}><Icon size={14} /> {formatPercent(value)}</span>
}

export default function StockTable({ stocks, isLoading, error, onRetry, onAction, actionSymbol, actionMode = 'add' }) {
  if (isLoading && stocks.length === 0) return <LoadingState label="Loading simulated market data" />
  if (error && stocks.length === 0) return <ErrorNotice message={error} onRetry={onRetry} />
  if (!stocks.length) return <EmptyState title="No matching stocks" description="Try another company name or ticker symbol." />

  return (
    <div className="table-wrap">
      <table className="data-table stock-table">
        <thead><tr><th>Instrument</th><th>Sector</th><th>Last price</th><th>Change</th><th>Updated</th>{onAction && <th aria-label="Actions" />}</tr></thead>
        <tbody>
          {stocks.map((stock) => {
            const removing = actionMode === 'remove'
            return (
              <tr key={stock.symbol}>
                <td><Link className="instrument-cell" to={`/stocks/${encodeURIComponent(stock.symbol)}`}><span className="instrument-symbol">{stock.symbol}</span><span className="instrument-name">{stock.companyName}</span></Link></td>
                <td><span className="sector-label">{stock.sector}</span></td>
                <td className="table-number">{formatInr(stock.currentPrice)}</td>
                <td><PriceChange value={stock.changePercent} /></td>
                <td className="table-muted">{formatDate(stock.updatedAt, { hour: '2-digit', minute: '2-digit' })}</td>
                {onAction && <td className="table-action"><Button variant="ghost" size="sm" onClick={() => onAction(stock.symbol)} disabled={actionSymbol === stock.symbol} aria-label={removing ? `Remove ${stock.symbol} from watchlist` : `Add ${stock.symbol} to watchlist`} title={removing ? 'Remove from watchlist' : 'Add to watchlist'}>{removing ? <X size={16} /> : <Star size={16} />}</Button></td>}
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}