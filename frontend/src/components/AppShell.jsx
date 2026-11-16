import { useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
  ArrowLeftRight,
  ChartNoAxesCombined,
  CircleHelp,
  Command,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  Star,
  WalletCards,
  X,
} from 'lucide-react'
import { useAuth } from '../auth/useAuth.js'
import ThemeToggle from './ThemeToggle.jsx'
import { Button } from './ui.jsx'

const primaryNavigation = [
  { to: '/dashboard', label: 'Overview', icon: LayoutDashboard },
  { to: '/market', label: 'Stock market', icon: ChartNoAxesCombined },
  { to: '/watchlist', label: 'Watchlist', icon: Star },
  { to: '/portfolio', label: 'Portfolio', icon: WalletCards },
  { to: '/orders', label: 'Order history', icon: ArrowLeftRight },
]

const titleByPath = {
  '/dashboard': 'Overview',
  '/market': 'Stock market',
  '/watchlist': 'Watchlist',
  '/portfolio': 'Portfolio',
  '/orders': 'Order history',
  '/trade': 'Trade',
}

function Brand() {
  return (
    <NavLink className="app-brand" to="/dashboard" aria-label="PortfolioPro overview">
      <span className="app-brand-mark"><Command size={17} strokeWidth={2.2} /></span>
      <span>Portfolio<span>Pro</span></span>
    </NavLink>
  )
}

export default function AppShell() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const currentTitle = location.pathname.startsWith('/stocks/')
    ? 'Stock details'
    : titleByPath[location.pathname] || 'TradeSphere'

  async function handleLogout() {
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  function closeMenu() {
    setMobileMenuOpen(false)
  }

  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <div className="sidebar-top"><Brand /><div className="market-status"><span /> Simulated market</div></div>
        <nav className="sidebar-nav" aria-label="Main navigation">
          <span className="nav-caption">Workspace</span>
          {primaryNavigation.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>
              <Icon size={17} strokeWidth={1.8} /><span>{label}</span>
            </NavLink>
          ))}
          <span className="nav-caption nav-caption-spaced">Trading</span>
          <NavLink to="/trade" className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>
            <ArrowLeftRight size={17} strokeWidth={1.8} /><span>Place an order</span>
          </NavLink>
        </nav>
        <div className="sidebar-bottom">
          <div className="sidebar-note"><CircleHelp size={15} /><span>Paper trading only</span></div>
          <div className="sidebar-user">
            <div className="avatar">{user?.name?.slice(0, 1)?.toUpperCase() || 'U'}</div>
            <div className="sidebar-user-copy"><strong>{user?.name || 'Investor'}</strong><span>{user?.email}</span></div>
            <button type="button" className="icon-button" onClick={handleLogout} aria-label="Sign out" title="Sign out"><LogOut size={16} /></button>
          </div>
        </div>
      </aside>

      <div className="app-main">
        <header className="app-topbar">
          <button type="button" className="mobile-menu-button" onClick={() => setMobileMenuOpen(!mobileMenuOpen)} aria-label={mobileMenuOpen ? 'Close navigation' : 'Open navigation'}>
            {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
          <div className="topbar-title"><span>TRADESPEHERE</span><strong>{currentTitle}</strong></div>
          <div className="topbar-actions">
            <NavLink to="/market" className="topbar-search"><Search size={16} /><span>Find a stock</span><kbd>/</kbd></NavLink>
            <ThemeToggle />
            <Button className="topbar-trade" onClick={() => navigate('/trade')}><ArrowLeftRight size={15} /> Trade</Button>
          </div>
        </header>
        {mobileMenuOpen && (
          <div className="mobile-menu-backdrop" onClick={closeMenu}>
            <nav className="mobile-menu" aria-label="Mobile navigation" onClick={(event) => event.stopPropagation()}>
              <Brand />
              {primaryNavigation.map(({ to, label, icon: Icon }) => (
                <NavLink key={to} to={to} onClick={closeMenu} className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>
                  <Icon size={18} /><span>{label}</span>
                </NavLink>
              ))}
              <NavLink to="/trade" onClick={closeMenu} className={({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`}>
                <ArrowLeftRight size={18} /><span>Place an order</span>
              </NavLink>
              <Button variant="outline" onClick={handleLogout}><LogOut size={16} /> Sign out</Button>
            </nav>
          </div>
        )}
        <main className="page-content"><Outlet /></main>
      </div>
    </div>
  )
}
