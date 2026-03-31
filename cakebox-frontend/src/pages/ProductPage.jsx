import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Star, ShoppingCart, Zap, Shield, Clock, ChevronLeft, Heart } from 'lucide-react'
import api from '../api/axios'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

export default function ProductPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { addToCart } = useCart()
  const { isLoggedIn } = useAuth()

  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [selectedFlavor, setSelectedFlavor] = useState('')
  const [selectedSize, setSelectedSize] = useState('1')
  const [isEggless, setIsEggless] = useState(false)
  const [message, setMessage] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [adding, setAdding] = useState(false)
  const [activeImg, setActiveImg] = useState(0)

  useEffect(() => {
    api.get(`/products/${id}`)
      .then(res => {
        setProduct(res.data)
        setSelectedFlavor(res.data.availableFlavors?.[0] || 'Chocolate')
        setLoading(false)
      })
      .catch(() => { toast.error('Product not found'); navigate('/'); })
  }, [id])

  const handleAddToCart = async (buyNow = false) => {
    if (!isLoggedIn) { navigate('/login'); return }
    setAdding(true)
    try {
      await addToCart(product.id, {
        quantity, flavor: selectedFlavor,
        sizeKg: selectedSize, isEggless, customMessage: message
      })
      toast.success('Added to cart! 🎂')
      if (buyNow) navigate('/cart')
    } catch (e) {
      toast.error(e.response?.data?.error || 'Could not add to cart')
    } finally { setAdding(false) }
  }

  if (loading) return (
    <div className="max-w-7xl mx-auto px-4 py-12 grid md:grid-cols-2 gap-12">
      <div className="shimmer h-96 rounded-3xl" />
      <div className="space-y-4">
        {[1,2,3,4,5].map(i => <div key={i} className={`shimmer h-8 rounded-xl w-${i%2===0?'full':'3/4'}`} />)}
      </div>
    </div>
  )

  const displayPrice = product.discountPrice || product.price
  const discountPct = product.price && product.discountPrice
    ? Math.round((1 - product.discountPrice / product.price) * 100) : 0
  const totalPrice = (parseFloat(displayPrice) * parseFloat(selectedSize) * quantity).toFixed(2)
  const images = [product.mainImageUrl, ...(product.imageUrls || [])].filter(Boolean)

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* BACK */}
      <button onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-amber-700 hover:text-amber-900 mb-6 transition-colors">
        <ChevronLeft size={20} /> Back
      </button>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
        {/* ── LEFT: IMAGES ──── */}
        <div>
          <div className="relative rounded-3xl overflow-hidden h-96 bg-orange-50 mb-4">
            <img
              src={images[activeImg] || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800'}
              alt={product.name}
              className="w-full h-full object-cover"
            />
            {discountPct > 0 && (
              <span className="absolute top-4 left-4 bg-rose-500 text-white font-bold px-3 py-1.5 rounded-xl">
                {discountPct}% OFF
              </span>
            )}
          </div>
          {images.length > 1 && (
            <div className="flex gap-3 overflow-x-auto hide-scrollbar">
              {images.map((img, i) => (
                <button key={i} onClick={() => setActiveImg(i)}
                  className={`shrink-0 w-20 h-20 rounded-xl overflow-hidden border-2 transition-all ${
                    activeImg === i ? 'border-orange-500 scale-105' : 'border-transparent'
                  }`}>
                  <img src={img} className="w-full h-full object-cover" alt="" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* ── RIGHT: DETAILS ── */}
        <div>
          <p className="text-sm text-orange-500 font-medium mb-1">{product.categoryName}</p>
          <h1 className="font-display text-3xl font-bold text-amber-900 mb-3">{product.name}</h1>

          {/* Rating */}
          <div className="flex items-center gap-2 mb-4">
            <div className="flex gap-0.5">
              {[1,2,3,4,5].map(s => (
                <Star key={s} size={16}
                  className={s <= Math.round(product.averageRating || 0)
                    ? 'fill-amber-400 text-amber-400' : 'text-gray-300'} />
              ))}
            </div>
            <span className="font-semibold text-amber-800">{product.averageRating?.toFixed(1) || '0.0'}</span>
            <span className="text-sm text-gray-400">({product.totalRatings} reviews)</span>
          </div>

          {/* Price */}
          <div className="flex items-baseline gap-3 mb-6">
            <span className="text-4xl font-bold text-amber-900">₹{displayPrice}</span>
            {product.discountPrice && (
              <span className="text-xl text-gray-400 line-through">₹{product.price}</span>
            )}
            <span className="text-sm text-orange-500 font-medium">per kg</span>
          </div>

          {/* Description */}
          {product.description && (
            <p className="text-amber-700 leading-relaxed mb-6 text-sm">{product.description}</p>
          )}

          {/* ── CUSTOMIZATION PANEL ── */}
          <div className="bg-orange-50 rounded-2xl p-5 space-y-5 mb-6">
            <h3 className="font-semibold text-amber-900 text-lg">Customize Your Cake</h3>

            {/* Flavor */}
            {product.availableFlavors?.length > 0 && (
              <div>
                <p className="text-sm font-medium text-amber-800 mb-2">Flavor</p>
                <div className="flex flex-wrap gap-2">
                  {product.availableFlavors.map(f => (
                    <button key={f} onClick={() => setSelectedFlavor(f)}
                      className={`px-4 py-2 rounded-xl text-sm font-medium transition-all ${
                        selectedFlavor === f
                          ? 'bg-amber-800 text-white scale-105'
                          : 'bg-white border border-orange-200 text-amber-800 hover:border-orange-400'
                      }`}>{f}</button>
                  ))}
                </div>
              </div>
            )}

            {/* Size */}
            {product.availableSizes?.length > 0 && (
              <div>
                <p className="text-sm font-medium text-amber-800 mb-2">Size</p>
                <div className="flex flex-wrap gap-2">
                  {product.availableSizes.map(s => (
                    <button key={s} onClick={() => setSelectedSize(s)}
                      className={`px-4 py-2 rounded-xl text-sm font-medium transition-all ${
                        selectedSize === s
                          ? 'bg-amber-800 text-white scale-105'
                          : 'bg-white border border-orange-200 text-amber-800 hover:border-orange-400'
                      }`}>{s} kg</button>
                  ))}
                </div>
              </div>
            )}

            {/* Egg Toggle */}
            {product.egglessAvailable && (
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-amber-800">Eggless</p>
                  <p className="text-xs text-orange-400">Available for this cake</p>
                </div>
                <button onClick={() => setIsEggless(!isEggless)}
                  className={`w-12 h-6 rounded-full transition-all relative ${isEggless ? 'bg-amber-800' : 'bg-gray-200'}`}>
                  <span className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-all ${isEggless ? 'left-7' : 'left-1'}`} />
                </button>
              </div>
            )}

            {/* Message */}
            <div>
              <p className="text-sm font-medium text-amber-800 mb-2">Message on Cake</p>
              <input
                type="text"
                placeholder="e.g. Happy Birthday Priya! 🎂"
                value={message}
                onChange={e => setMessage(e.target.value)}
                maxLength={50}
                className="w-full px-4 py-3 rounded-xl border border-orange-200 bg-white text-sm focus:outline-none focus:border-orange-400"
              />
              <p className="text-xs text-orange-400 mt-1 text-right">{message.length}/50</p>
            </div>

            {/* Quantity */}
            <div className="flex items-center justify-between">
              <p className="text-sm font-medium text-amber-800">Quantity</p>
              <div className="flex items-center gap-3">
                <button onClick={() => setQuantity(q => Math.max(1, q-1))}
                  className="w-9 h-9 rounded-xl bg-white border border-orange-200 font-bold text-amber-800 hover:bg-orange-100 transition-colors">−</button>
                <span className="w-8 text-center font-bold text-amber-900">{quantity}</span>
                <button onClick={() => setQuantity(q => q+1)}
                  className="w-9 h-9 rounded-xl bg-white border border-orange-200 font-bold text-amber-800 hover:bg-orange-100 transition-colors">+</button>
              </div>
            </div>
          </div>

          {/* Total price */}
          <div className="flex items-center justify-between mb-5 p-4 bg-amber-50 rounded-xl">
            <span className="text-sm font-medium text-amber-700">Total ({quantity} × {selectedSize}kg)</span>
            <span className="text-2xl font-bold text-amber-900">₹{totalPrice}</span>
          </div>

          {/* Delivery info */}
          <div className="flex gap-3 mb-6 text-sm text-amber-700">
            <span className="flex items-center gap-1"><Clock size={14} /> Express: 2 hrs</span>
            <span className="flex items-center gap-1"><Shield size={14} /> Fresh Guarantee</span>
            <span className="flex items-center gap-1"><Zap size={14} /> Free delivery ₹500+</span>
          </div>

          {/* CTA Buttons */}
          <div className="flex gap-3">
            <button onClick={() => handleAddToCart(false)} disabled={adding}
              className="flex-1 flex items-center justify-center gap-2 bg-white border-2 border-amber-800 text-amber-800 py-4 rounded-2xl font-semibold text-lg hover:bg-amber-50 transition-all disabled:opacity-50">
              <ShoppingCart size={20} />
              {adding ? 'Adding...' : 'Add to Cart'}
            </button>
            <button onClick={() => handleAddToCart(true)} disabled={adding}
              className="flex-1 bg-amber-800 hover:bg-amber-700 text-white py-4 rounded-2xl font-semibold text-lg transition-all hover:scale-105 disabled:opacity-50">
              Buy Now
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}