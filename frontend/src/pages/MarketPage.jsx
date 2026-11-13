import { useState } from 'react'
import { Search, Star } from 'lucide-react'
import { useStockSnapshot } from '../hooks/useStockSnapshot.js'
import { addStockToWatchlist } from '../services/watchlist.js'
import { Button, Card, ErrorNotice, Input, PageHeading } from '../components/ui.jsx'
import StockTable from '../components/StockTable.jsx'

export default function MarketPage() {
  const [query, setQuery] = useState('')
  const [workingSymbol, setWorkingSymbol] = useState('')
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const { stocks, isLoading, error: loadError, refresh } = useStockSnapshot(query, 15000)

  async function addStock(symbol) {
    setWorkingSymbol(symbol)
    setNotice('')
    setError('')
    try {
      await addStockToWatchlist(symbol)
      setNotice(`${symbol} added to your watchlist.`)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setWorkingSymbol('')
    }
  }

  return (
    <div className="page-stack">
      <PageHeading eyebrow={stocks.length ? `${stocks.length} SIMULATED NSE INSTRUMENTS` : 'SIMULATED NSE INSTRUMENTS'} title="Stock market" description="Search by ticker, company, or sector. Quotes and movements come from the simulated market engine." />
      <Card className="market-page-card">
        <div className="market-toolbar">
          <label className="search-field"><Search size={17} /><Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search stocks or sectors" aria-label="Search stocks by ticker, company, or sector" /></label>
          <span className="market-refresh-label">Prices refresh every 15 seconds</span>
        </div>
        {(notice || error) && <div className={error ? 'inline-banner banner-error' : 'inline-banner banner-success'} role={error ? 'alert' : 'status'}>{error || notice}<Button variant="ghost" size="sm" onClick={() => { setError(''); setNotice('') }}>Dismiss</Button></div>}
        {loadError && stocks.length > 0 && <ErrorNotice message={loadError} onRetry={refresh} />}
        <StockTable stocks={stocks} isLoading={isLoading} error={loadError} onRetry={refresh} onAction={addStock} actionSymbol={workingSymbol} />
        <div className="market-footnote"><Star size={14} /> Save an instrument to your watchlist from any row.</div>
      </Card>
    </div>
  )
}