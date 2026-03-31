import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Eye, EyeOff } from 'lucide-react'
import api from '../api/axios'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.email || !form.password) {
      toast.error('Please enter email and password')
      return
    }
    setLoading(true)
    try {
      const res = await api.post('/auth/login', form)
      // Save user + token
      login(
        {
          name: res.data.name,
          email: res.data.email,
          role: res.data.role,
          id: res.data.userId
        },
        res.data.token
      )
      toast.success(`Welcome back, ${res.data.name}! 🎂`)
      // Always go to home page after login
      navigate('/', { replace: true })
    } catch (err) {
      const msg = err.response?.data?.error || 'Invalid email or password. Please try again.'
      toast.error(msg, { duration: 4000 })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-amber-50 to-orange-50 px-4 py-12">
      <div className="bg-white rounded-3xl shadow-xl w-full max-w-md p-8 border border-orange-100">
        <div className="text-center mb-8">
          <div className="text-5xl mb-4">🎂</div>
          <h1 className="font-display text-3xl font-bold text-amber-900">Welcome Back!</h1>
          <p className="text-orange-400 mt-2">Sign in to your CakeBox account</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-amber-800 mb-2">Email</label>
            <input
              type="email"
              required
              value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })}
              placeholder="you@example.com"
              className="w-full px-4 py-3.5 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 focus:ring-2 focus:ring-orange-100 transition-all text-sm"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-amber-800 mb-2">Password</label>
            <div className="relative">
              <input
                type={showPw ? 'text' : 'password'}
                required
                value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })}
                placeholder="Enter your password"
                className="w-full px-4 py-3.5 pr-12 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 focus:ring-2 focus:ring-orange-100 transition-all text-sm"
              />
              <button
                type="button"
                onClick={() => setShowPw(!showPw)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-orange-400 hover:text-orange-600">
                {showPw ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-amber-800 hover:bg-amber-700 text-white py-4 rounded-2xl font-bold text-lg transition-all hover:scale-[1.02] disabled:opacity-60 flex items-center justify-center gap-2">
            {loading
              ? <><div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" /> Signing in...</>
              : 'Sign In 🎂'}
          </button>
        </form>

        <p className="text-center text-sm text-amber-700 mt-6">
          Don't have an account?{' '}
          <Link to="/register" className="text-orange-500 font-semibold hover:text-orange-600">
            Create one free
          </Link>
        </p>
      </div>
    </div>
  )
}