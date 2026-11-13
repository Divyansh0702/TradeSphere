import { useEffect, useState } from 'react'
import { ThemeContext } from './context.js'

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => {
    try {
      return window.localStorage.getItem('portfolio-pro-theme') || 'dark'
    } catch {
      return 'dark'
    }
  })

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    try {
      window.localStorage.setItem('portfolio-pro-theme', theme)
    } catch {
      return undefined
    }
    return undefined
  }, [theme])

  function toggleTheme() {
    setTheme((current) => current === 'dark' ? 'light' : 'dark')
  }

  return <ThemeContext.Provider value={{ theme, toggleTheme }}>{children}</ThemeContext.Provider>
}