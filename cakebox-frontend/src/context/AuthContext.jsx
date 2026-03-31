// =============================================================
// FILE: src/context/AuthContext.jsx
// Global login state — user info available everywhere in app
// =============================================================
import { createContext, useContext, useState, useEffect } from 'react'
import api from '../api/axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // On app load — check if user was previously logged in
  useEffect(() => {
    const stored = localStorage.getItem('cakebox_user')
    const token = localStorage.getItem('cakebox_token')
    if (stored && token) {
      setUser(JSON.parse(stored))
    }
    setLoading(false)
  }, [])

  const login = (userData, token) => {
    localStorage.setItem('cakebox_token', token)
    localStorage.setItem('cakebox_user', JSON.stringify(userData))
    setUser(userData)
  }

  const logout = () => {
    localStorage.removeItem('cakebox_token')
    localStorage.removeItem('cakebox_user')
    setUser(null)
  }

  const isAdmin = user?.role === 'ROLE_ADMIN'
  const isLoggedIn = !!user

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin, isLoggedIn, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)