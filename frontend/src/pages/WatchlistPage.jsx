import { useEffect, useState } from 'react'
import { ArrowRight, Star } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiRequest } from '../auth/api.js'
import { getOrCreateWatchlist, removeStockFromWatchlist } from '../services/watchlist.js'
import StockTable from '../components/StockTable.jsx'
import { Button, Card, EmptyState, ErrorNotice, PageHeading } from '../components/ui.jsx'

export default function WatchlistPage() {
  const [watchlist, setWatchlist] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [workingSymbol, setWorkingSymbol] = useState('')
  const [refreshIndex, setRefreshIndex] = useState(0)

  useEffect(() => {
    let active = true
    async function load(createIfMissing) {
      try {
        const result = createIfMissing ? await getOrCreateWatchlist() : await apiRequest('/api/watchlists/me')
        if (active) { setWatchlist(result); setError('') }
      } catch (requestError) {
        if (active) setError(requestError.message)
      } finally {
        if (active) setIsLoading(false)
      }
    }
    load(true)
    const intervalId = window.setInterval(() => load(false), 9000)
    return () => { active = false; window.clearInterval(intervalId) }
  }, [refreshIndex])

  async function remove(symbol) {
    setWorkingSymbol(symbol)
    try {
      await removeStockFromWatchlist(symbol)
      setRefreshIndex((index) => index + 1)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setWorkingSymbol('')
    }
  }

  const stocks = watchlist?.stocks || []
  return (
    <div className="page-stack">
      <PageHeading eyebrow="Your saved instruments" title="Watchlist" description="A live view of saved stocks, refreshed from the simulated market." actions={<Button variant="outline" onClick={() => setRefreshIndex((index) => index + 1)}><ArrowRight size={15} /> Refresh</Button>} />
      {error && <ErrorNotice message={error} onRetry={() => setRefreshIndex((index) => index + 1)} />}
      <Card className="market-page-card">
        {!isLoading && !error && stocks.length === 0 ? (
          <EmptyState icon={Star} title="Your watchlist is clear" description="Save stocks from the market to keep their latest simulated prices close." action={<Link className="text-link" to="/market">Browse stocks <ArrowRight size={14} /></Link>} />
        ) : (
          <StockTable stocks={stocks} isLoading={isLoading} error={error} onRetry={() => setRefreshIndex((index) => index + 1)} onAction={remove} actionSymbol={workingSymbol} actionMode="remove" />
        )}
      </Card>
      <p className="page-footnote">Prices are refreshed from the backend’s simulated market engine. This watchlist belongs to your account.</p>
    </div>
  )
}