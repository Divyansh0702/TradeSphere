import { Link } from 'react-router-dom'
import { ArrowRight, BriefcaseBusiness } from 'lucide-react'
import { formatInr } from '../auth/currency.js'
import { useApiResource } from '../hooks/useApiResource.js'
import { Card, EmptyState, ErrorNotice, LoadingState, PageHeading } from '../components/ui.jsx'

function Metric({ label, value, tone = '' }) {
  return <Card className="portfolio-metric"><span>{label}</span><strong className={tone}>{value}</strong></Card>
}

export default function PortfolioPage() {
  const { data: summary, isLoading: summaryLoading, error: summaryError } = useApiResource('/api/portfolio/summary', 10000)
  const { data: holdings, isLoading: holdingsLoading, error: holdingsError } = useApiResource('/api/portfolio/holdings', 10000)

  return (
    <div className="page-stack">
      <PageHeading eyebrow="POSITIONS AT CURRENT SIMULATED PRICES" title="Portfolio" description="Your persisted holdings and mark-to-market performance." actions={<Link className="ui-button button-primary" to="/trade"><ArrowRight size={15} /> Trade stocks</Link>} />
      {(summaryError || holdingsError) && <ErrorNotice message={summaryError || holdingsError} />}
      {summaryLoading && !summary ? <LoadingState label="Loading portfolio summary" /> : summary && (
        <div className="portfolio-metric-grid">
          <Metric label="Current value" value={formatInr(summary.currentValue)} />
          <Metric label="Invested" value={formatInr(summary.investedValue)} />
          <Metric label="Unrealized P&L" value={formatInr(summary.unrealizedPnl)} tone={summary.unrealizedPnl >= 0 ? 'gain' : 'loss'} />
          <Metric label="Realized P&L" value={formatInr(summary.realizedPnl)} tone={summary.realizedPnl >= 0 ? 'gain' : 'loss'} />
        </div>
      )}
      <Card className="holdings-card">
        <div className="card-heading-row"><div><span className="section-kicker">OPEN POSITIONS</span><h2>Holdings</h2></div><span className="history-count">{holdings?.length ?? 0} instruments</span></div>
        {holdingsLoading && !holdings ? <LoadingState label="Loading holdings" /> : holdingsError ? <ErrorNotice message={holdingsError} /> : holdings?.length ? (
          <div className="table-wrap"><table className="data-table holdings-table"><thead><tr><th>Instrument</th><th>Quantity</th><th>Average price</th><th>Current price</th><th>Invested value</th><th>Current value</th><th>Unrealized P&L</th></tr></thead><tbody>
            {holdings.map((holding) => <tr key={holding.symbol}><td><Link className="instrument-cell" to={`/stocks/${encodeURIComponent(holding.symbol)}`}><span className="instrument-symbol">{holding.symbol}</span><span className="instrument-name">{holding.companyName}</span></Link></td><td>{holding.quantity}</td><td className="table-number">{formatInr(holding.averagePurchasePrice)}</td><td className="table-number">{formatInr(holding.currentPrice)}</td><td className="table-number">{formatInr(holding.investedValue)}</td><td className="table-number">{formatInr(holding.currentValue)}</td><td className={holding.unrealizedPnl >= 0 ? 'gain table-number' : 'loss table-number'}>{formatInr(holding.unrealizedPnl)}</td></tr>)}
          </tbody></table></div>
        ) : <EmptyState icon={BriefcaseBusiness} title="No open positions" description="When you execute a buy order, your holdings will appear here." action={<Link className="text-link" to="/market">Explore the market <ArrowRight size={14} /></Link>} />}
      </Card>
      {summary && <p className="page-footnote">Total account value {formatInr(summary.totalAccountValue)} includes your virtual wallet balance of {formatInr(summary.walletBalance)}.</p>}
    </div>
  )
}