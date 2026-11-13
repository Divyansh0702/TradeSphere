import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext.jsx'
import { useAuth } from './auth/useAuth.js'
import AppShell from './components/AppShell.jsx'
import { LoadingState } from './components/ui.jsx'
import AuthPage from './pages/AuthPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import MarketPage from './pages/MarketPage.jsx'
import OrdersPage from './pages/OrdersPage.jsx'
import PortfolioPage from './pages/PortfolioPage.jsx'
import StockDetailPage from './pages/StockDetailPage.jsx'
import TradePage from './pages/TradePage.jsx'
import WatchlistPage from './pages/WatchlistPage.jsx'
import { ThemeProvider } from './theme/ThemeProvider.jsx'

function ProtectedRoute() {
  const { isLoading, user } = useAuth()
  if (isLoading) return <LoadingState label="Checking your secure session" />
  return user ? <Outlet /> : <Navigate to="/login" replace />
}

function PublicRoute({ children }) {
  const { isLoading, user } = useAuth()

  if (isLoading) return <LoadingState label="Checking your secure session" />
  return user ? <Navigate to="/dashboard" replace /> : children
}

function RootRedirect() {
  const { isLoading, user } = useAuth()
  if (isLoading) return <LoadingState label="Checking your secure session" />
  return <Navigate to={user ? '/dashboard' : '/login'} replace />
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/login" element={<PublicRoute><AuthPage mode="login" /></PublicRoute>} />
      <Route path="/register" element={<PublicRoute><AuthPage mode="register" /></PublicRoute>} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/market" element={<MarketPage />} />
          <Route path="/stocks/:symbol" element={<StockDetailPage />} />
          <Route path="/watchlist" element={<WatchlistPage />} />
          <Route path="/trade" element={<TradePage />} />
          <Route path="/portfolio" element={<PortfolioPage />} />
          <Route path="/orders" element={<OrdersPage />} />
        </Route>
      </Route>
      <Route path="*" element={<RootRedirect />} />
    </Routes>
  )
}

function App() {
  return <ThemeProvider><AuthProvider><AppRoutes /></AuthProvider></ThemeProvider>
}

export default App
