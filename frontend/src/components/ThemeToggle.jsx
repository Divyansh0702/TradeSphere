import { Moon, Sun } from 'lucide-react'
import { useTheme } from '../theme/useTheme.js'

export default function ThemeToggle() {
  const { theme, toggleTheme } = useTheme()
  const nextTheme = theme === 'dark' ? 'light' : 'dark'
  const Icon = theme === 'dark' ? Sun : Moon

  return (
    <button className="theme-toggle" type="button" onClick={toggleTheme} aria-label={`Switch to ${nextTheme} mode`} title={`Switch to ${nextTheme} mode`}>
      <Icon size={16} aria-hidden="true" />
    </button>
  )
}