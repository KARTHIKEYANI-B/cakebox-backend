// =============================================================
// FILE: src/context/CartContext.jsx
// Global cart state — cart count available in Navbar
// =============================================================
import { createContext, useContext, useState, useEffect } from 'react'
import api from '../api/axios'
import { useAuth } from './AuthContext'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const { isLoggedIn } = useAuth()
  const [cart, setCart] = useState(null)
  const [cartCount, setCartCount] = useState(0)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (isLoggedIn) fetchCart()
    else { setCart(null); setCartCount(0) }
  }, [isLoggedIn])

  const fetchCart = async () => {
    try {
      setLoading(true)
      const res = await api.get('/cart')
      setCart(res.data)
      setCartCount(res.data.itemCount || 0)
    } catch (e) {
      console.log('Cart fetch error:', e.message)
    } finally {
      setLoading(false)
    }
  }

  const addToCart = async (productId, options = {}) => {
    const res = await api.post('/cart/add', { productId, ...options })
    setCart(res.data)
    setCartCount(res.data.itemCount || 0)
    return res.data
  }

  const removeFromCart = async (cartItemId) => {
    const res = await api.delete(`/cart/remove/${cartItemId}`)
    setCart(res.data)
    setCartCount(res.data.itemCount || 0)
  }

  const updateCartItem = async (cartItemId, quantity) => {
    const res = await api.put(`/cart/update/${cartItemId}`, { quantity })
    setCart(res.data)
    setCartCount(res.data.itemCount || 0)
  }

  const clearCart = () => {
    setCart(null)
    setCartCount(0)
  }

  return (
    <CartContext.Provider value={{
      cart, cartCount, loading,
      fetchCart, addToCart, removeFromCart, updateCartItem, clearCart
    }}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)