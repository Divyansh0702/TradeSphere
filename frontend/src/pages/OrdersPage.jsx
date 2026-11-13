import { Link } from 'react-router-dom'
import { ArrowDownRight, ArrowRight, ArrowUpRight, ReceiptText } from 'lucide-react'
import { formatInr } from '../auth/currency.js'
import { useApiResource } from '../hooks/useApiResource.js'
import { formatDate } from '../lib/format.js'
import { Badge, Card, EmptyState, ErrorNotice, LoadingState, PageHeading } from '../components/ui.jsx'

export default function OrdersPage() {
  const { data: orders, isLoading, error } = useApiResource('/api/orders', 15000)
  return (
    <div className="page-stack">
      <PageHeading eyebrow="LATEST 100 EXECUTED ORDERS" title="Order history" description="Every buy and sell submitted by your account, with backend-confirmed fills." />
      {error && <ErrorNotice message={error} />}
      <Card className="orders-card">
        <div className="card-heading-row"><div><span className="section-kicker">EXECUTIONS</span><h2>Transactions</h2></div><span className="history-count">{orders?.length ?? 0} orders</span></div>
        {isLoading && !orders ? <LoadingState label="Loading order history" /> : orders?.length ? (
          <div className="table-wrap"><table className="data-table orders-table"><thead><tr><th>Order</th><th>Side</th><th>Quantity</th><th>Fill price</th><th>Total</th><th>Realized P&L</th><th>Status</th><th>Time</th></tr></thead><tbody>
            {orders.map((order) => <tr key={order.id}><td><Link className="instrument-symbol" to={`/stocks/${encodeURIComponent(order.symbol)}`}>{order.symbol}</Link><span className="order-id">#{order.id}</span></td><td><span className={`side-label ${order.side === 'BUY' ? 'gain' : 'loss'}`}>{order.side === 'BUY' ? <ArrowDownRight size={14} /> : <ArrowUpRight size={14} />}{order.side}</span></td><td>{order.quantity}</td><td className="table-number">{formatInr(order.executionPrice)}</td><td className="table-number">{formatInr(order.totalAmount)}</td><td className={order.realizedPnl >= 0 ? 'gain table-number' : 'loss table-number'}>{formatInr(order.realizedPnl)}</td><td><Badge tone="gain">{order.status}</Badge></td><td className="table-muted">{formatDate(order.createdAt, { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })}</td></tr>)}
          </tbody></table></div>
        ) : <EmptyState icon={ReceiptText} title="No orders yet" description="Your executed simulated orders will appear here." action={<Link className="text-link" to="/market">Find a stock <ArrowRight size={14} /></Link>} />}
      </Card>
    </div>
  )
}