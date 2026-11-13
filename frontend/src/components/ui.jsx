import { LoaderCircle } from 'lucide-react'

const buttonStyles = {
  primary: 'button-primary',
  secondary: 'button-secondary',
  outline: 'button-outline',
  ghost: 'button-ghost',
  danger: 'button-danger',
}

export function Button({ variant = 'primary', size = 'md', className = '', children, ...props }) {
  return (
    <button
      className={`ui-button ${buttonStyles[variant] || buttonStyles.primary} ${size === 'sm' ? 'button-sm' : ''} ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}

export function Card({ className = '', children, ...props }) {
  return <section className={`ui-card ${className}`} {...props}>{children}</section>
}

export function Input({ className = '', ...props }) {
  return <input className={`ui-input ${className}`} {...props} />
}

export function Badge({ tone = 'neutral', children, className = '' }) {
  return <span className={`ui-badge badge-${tone} ${className}`}>{children}</span>
}

export function LoadingState({ label = 'Loading data' }) {
  return (
    <div className="state-panel" role="status">
      <LoaderCircle className="spin-icon" size={20} aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}

export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="state-panel state-empty">
      {Icon && <Icon size={24} strokeWidth={1.5} aria-hidden="true" />}
      <strong>{title}</strong>
      <p>{description}</p>
      {action}
    </div>
  )
}

export function ErrorNotice({ message, onRetry }) {
  return (
    <div className="error-notice" role="alert">
      <span>{message}</span>
      {onRetry && <Button variant="outline" size="sm" onClick={onRetry}>Retry</Button>}
    </div>
  )
}

export function PageHeading({ eyebrow, title, description, actions }) {
  return (
    <div className="page-heading">
      <div>
        {eyebrow && <span className="eyebrow">{eyebrow}</span>}
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>
      {actions && <div className="heading-actions">{actions}</div>}
    </div>
  )
}