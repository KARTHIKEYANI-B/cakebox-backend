import { Link } from 'react-router-dom'
import { Star, ShoppingCart, Zap } from 'lucide-react'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'
import { useNavigate } from 'react-router-dom'

export default function ProductCard({ product }) {
  const { addToCart } = useCart()
  const { isLoggedIn } = useAuth()
  const navigate = useNavigate()

  const discountPct = product.price && product.discountPrice
    ? Math.round((1 - product.discountPrice / product.price) * 100)
    : 0

  const handleAddToCart = async (e) => {
    e.preventDefault()
    if (!isLoggedIn) { navigate('/login'); return }
    try {
      await addToCart(product.id, {
        quantity: 1,
        flavor: product.availableFlavors?.[0] || 'Chocolate',
        sizeKg: '1',
        isEggless: false
      })
      toast.success(`${product.name} added to cart! 🎂`)
    } catch (err) {
      toast.error('Could not add to cart')
    }
  }

  return (
    <Link to={`/product/${product.id}`}
      className="card-lift bg-white rounded-2xl overflow-hidden border border-orange-100 group block">
      {/* IMAGE */}
      <div className="relative overflow-hidden h-52 bg-orange-50">
        <img
          src={product.mainImageUrl || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400'}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
        />
        {discountPct > 0 && (
          <span className="absolute top-3 left-3 bg-rose-500 text-white text-xs font-bold px-2 py-1 rounded-lg">
            {discountPct}% OFF
          </span>
        )}
        {product.isTrending && (
          <span className="absolute top-3 right-3 bg-amber-500 text-white text-xs font-bold px-2 py-1 rounded-lg flex items-center gap-1">
            <Zap size={10} /> Trending
          </span>
        )}
      </div>

      {/* DETAILS */}
      <div className="p-4">
        <p className="text-xs text-orange-500 font-medium mb-1">{product.categoryName}</p>
        <h3 className="font-semibold text-amber-900 line-clamp-2 leading-tight mb-2">{product.name}</h3>

        {/* RATING */}
        <div className="flex items-center gap-1 mb-3">
          <Star size={13} className="fill-amber-400 text-amber-400" />
          <span className="text-sm font-medium text-amber-800">{product.averageRating?.toFixed(1) || '4.5'}</span>
          <span className="text-xs text-gray-400">({product.totalRatings || 0})</span>
        </div>

        {/* PRICE */}
        <div className="flex items-center justify-between">
          <div className="flex items-baseline gap-2">
            <span className="text-lg font-bold text-amber-900">
              ₹{product.discountPrice || product.price}
            </span>
            {product.discountPrice && (
              <span className="text-sm text-gray-400 line-through">₹{product.price}</span>
            )}
          </div>
          <button
            onClick={handleAddToCart}
            className="bg-amber-800 hover:bg-amber-700 text-white p-2.5 rounded-xl transition-colors">
            <ShoppingCart size={16} />
          </button>
        </div>
      </div>
    </Link>
  )
}