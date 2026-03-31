import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ShoppingCart, Search, Menu, X, User, LogOut, Settings, MapPin } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function Navbar() {
  const { user, isLoggedIn, isAdmin, logout } = useAuth()
  const { cartCount } = useCart()
  const [searchQuery, setSearchQuery] = useState('')
  const [menuOpen, setMenuOpen] = useState(false)
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const navigate = useNavigate()

  const handleSearch = (e) => {
    e.preventDefault()
    if (searchQuery.trim()) navigate(`/?search=${searchQuery.trim()}`)
  }

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <nav className="sticky top-0 z-50 bg-white shadow-sm border-b border-orange-100">
      {/* TOP BAR */}
      <div className="bg-amber-800 text-amber-100 text-xs text-center py-1.5 flex items-center justify-center gap-2">
        <MapPin size={12} />
        <span>Delivering to Karur</span>
        <span className="mx-2 opacity-40">|</span>
        <span>Order within 30 mins for same-day delivery 🎂</span>
      </div>

      {/* MAIN NAV */}
      <div className="max-w-7xl mx-auto px-4 py-3 flex items-center gap-4">
        {/* LOGO */}
        <Link to="/" className="flex items-center gap-2 shrink-0">
          <span className="text-2xl">🎂</span>
          <span className="font-display text-xl font-bold text-amber-900">CAKEBOX</span>
        </Link>

        {/* SEARCH */}
        <form onSubmit={handleSearch} className="flex-1 max-w-xl mx-auto hidden sm:flex">
          <div className="relative w-full">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-orange-400" />
            <input
              type="text"
              placeholder="Search cakes, brownies, sweets..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-orange-200 bg-orange-50 text-sm focus:outline-none focus:border-orange-400 focus:bg-white transition-colors"
            />
          </div>
        </form>

        {/* RIGHT SIDE */}
        <div className="flex items-center gap-3 ml-auto">
          {/* CUSTOMIZE button */}
          <Link to="/customize"
            className="hidden md:block text-sm font-medium text-amber-800 hover:text-orange-500 transition-colors">
            🎨 Customize
          </Link>

          {/* CART */}
          <Link to="/cart" className="relative p-2 hover:bg-orange-50 rounded-xl transition-colors">
            <ShoppingCart size={22} className="text-amber-800" />
            {cartCount > 0 && (
              <span className="absolute -top-0.5 -right-0.5 bg-orange-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">
                {cartCount > 9 ? '9+' : cartCount}
              </span>
            )}
          </Link>

          {/* USER */}
          {isLoggedIn ? (
            <div className="relative">
              <button
                onClick={() => setDropdownOpen(!dropdownOpen)}
                className="flex items-center gap-2 bg-orange-50 hover:bg-orange-100 px-3 py-2 rounded-xl transition-colors">
                <div className="w-7 h-7 rounded-full bg-amber-800 text-white flex items-center justify-center text-xs font-bold">
                  {user?.name?.charAt(0).toUpperCase()}
                </div>
                <span className="hidden md:block text-sm font-medium text-amber-900 max-w-20 truncate">
                  {user?.name?.split(' ')[0]}
                </span>
              </button>

              {dropdownOpen && (
                <div className="absolute right-0 top-full mt-2 w-44 bg-white rounded-2xl shadow-xl border border-orange-100 overflow-hidden z-50">
                  <div className="px-4 py-3 border-b border-orange-50">
                    <p className="text-xs text-orange-400 font-medium">Signed in as</p>
                    <p className="text-sm font-semibold text-amber-900 truncate">{user?.email}</p>
                  </div>
                  {isAdmin && (
                    <Link to="/admin"
                      onClick={() => setDropdownOpen(false)}
                      className="flex items-center gap-2 px-4 py-3 hover:bg-orange-50 text-sm text-amber-800 transition-colors">
                      <Settings size={14} /> Admin Panel
                    </Link>
                  )}
                  <button
                    onClick={() => { handleLogout(); setDropdownOpen(false) }}
                    className="w-full flex items-center gap-2 px-4 py-3 hover:bg-red-50 text-sm text-red-600 transition-colors">
                    <LogOut size={14} /> Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <Link to="/login"
              className="flex items-center gap-1.5 bg-amber-800 text-white px-4 py-2 rounded-xl text-sm font-medium hover:bg-amber-700 transition-colors">
              <User size={15} /> Login
            </Link>
          )}

          {/* MOBILE MENU TOGGLE */}
          <button className="sm:hidden p-2" onClick={() => setMenuOpen(!menuOpen)}>
            {menuOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </div>
      </div>

      {/* MOBILE SEARCH */}
      {menuOpen && (
        <div className="sm:hidden px-4 pb-4">
          <form onSubmit={handleSearch}>
            <div className="relative">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-orange-400" />
              <input
                type="text"
                placeholder="Search cakes..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-orange-200 bg-orange-50 text-sm focus:outline-none"
              />
            </div>
          </form>
          <div className="mt-3 flex gap-3">
            <Link to="/customize" onClick={() => setMenuOpen(false)}
              className="text-sm font-medium text-amber-800">🎨 Customize Cake</Link>
          </div>
        </div>
      )}
    </nav>
  )
}