import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { Activity, Command } from 'lucide-react'
import { useAuth } from '../auth/useAuth.js'
import ThemeToggle from '../components/ThemeToggle.jsx'

function Brand() {
  return <Link className="auth-brand" to="/login"><span className="app-brand-mark"><Command size={17} /></span><span>TradeSphere</span></Link>
}

export default function AuthPage({ mode }) {
  const isRegister = mode === 'register'
  const { user, isLoading, login, register } = useAuth()
  const navigate = useNavigate()
  const [values, setValues] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (!isLoading && user) return <Navigate to="/dashboard" replace />

  function updateField(event) {
    setValues((current) => ({ ...current, [event.target.name]: event.target.value }))
    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      const action = isRegister ? register : login
      await action(isRegister ? values : { email: values.email, password: values.password })
      navigate('/dashboard', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <div className="auth-topbar"><Brand /><ThemeToggle /></div>
        <div className="auth-content">
          <span className="eyebrow">{isRegister ? 'Create your investor profile' : 'Secure investor access'}</span>
          <h1 className="auth-title">{isRegister ? 'A steadier way to explore the market.' : 'Your portfolio, in focus.'}</h1>
          <p className="auth-copy">{isRegister ? 'Build a simulated portfolio, keep a watchlist, and follow your progress in one place.' : 'Sign in to review your simulated holdings and the latest market activity.'}</p>
          <form className="auth-form" onSubmit={handleSubmit}>
            {isRegister && <div className="field"><label htmlFor="name">Full name</label><input id="name" name="name" autoComplete="name" placeholder="Jordan Lee" value={values.name} onChange={updateField} required minLength={2} maxLength={80} /></div>}
            <div className="field"><label htmlFor="email">Email address</label><input id="email" name="email" type="email" autoComplete="email" placeholder="you@example.com" value={values.email} onChange={updateField} required maxLength={254} /></div>
            <div className="field"><label htmlFor="password">Password</label><input id="password" name="password" type="password" autoComplete={isRegister ? 'new-password' : 'current-password'} placeholder={isRegister ? 'At least 8 characters' : 'Enter your password'} value={values.password} onChange={updateField} required minLength={isRegister ? 8 : undefined} maxLength={72} /></div>
            {error && <p className="form-error" role="alert">{error}</p>}
            <button className="submit-button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Please wait...' : isRegister ? 'Create account' : 'Sign in'}
              {!isSubmitting && <span aria-hidden="true">{isRegister ? '→' : '↗'}</span>}
            </button>
          </form>
          <p className="auth-switch">{isRegister ? 'Already have an account?' : 'New to TradeSphere?'}<Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Sign in' : 'Create an account'}</Link></p>
        </div>
        <p className="auth-footnote">Virtual INR balances only · No real-money trading</p>
      </section>
      <aside className="auth-aside">
        <div className="auth-aside-mark"><Activity size={21} /></div>
        <span className="section-kicker">TRADESPEHERE SIMULATED MARKETS</span>
        <h2>Trade ideas.<br /><span>Not real money.</span></h2>
        <p>Explore a curated NSE stock universe, test decisions with virtual INR, and review every execution in your own portfolio.</p>
        <div className="auth-aside-lines" aria-hidden="true"><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /><i /></div>
        <div className="auth-aside-meta"><span>NSE-listed instruments</span><span>INR · Simulated</span></div>
      </aside>
    </main>
  )
}