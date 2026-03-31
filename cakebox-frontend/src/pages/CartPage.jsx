import { Link, useNavigate } from 'react-router-dom'
import { Trash2, Plus, Minus, ShoppingBag, Tag } from 'lucide-react'
import { useCart } from '../context/CartContext'
import toast from 'react-hot-toast'

export default function CartPage() {
  const { cart, loading, removeFromCart, updateCartItem } = useCart()
  const navigate = useNavigate()

  const handleRemove = async (cartItemId) => {
    try { await removeFromCart(cartItemId); toast.success('Item removed') }
    catch { toast.error('Could not remove item') }
  }

  const handleQty = async (cartItemId, newQty) => {
    try { await updateCartItem(cartItemId, newQty) }
    catch { toast.error('Could not update quantity') }
  }

  if (loading) return (
    <div className="max-w-5xl mx-auto px-4 py-12">
      <div className="shimmer h-8 w-40 rounded mb-8" />
      {[1,2,3].map(i => <div key={i} className="shimmer h-28 rounded-2xl mb-4" />)}
    </div>
  )

  if (!cart || cart.items?.length === 0) return (
    <div className="max-w-5xl mx-auto px-4 py-24 text-center">
      <div className="text-7xl mb-6">🛒</div>
      <h2 className="font-display text-3xl font-bold text-amber-900 mb-3">Your cart is empty</h2>
      <p className="text-amber-600 mb-8">Add some delicious cakes to get started!</p>
      <Link to="/" className="bg-amber-800 text-white px-8 py-4 rounded-2xl font-semibold text-lg hover:bg-amber-700 transition-colors inline-block">
        Browse Cakes
      </Link>
    </div>
  )

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="font-display text-3xl font-bold text-amber-900 mb-8">
        My Cart <span className="text-orange-400 text-xl font-normal">({cart.itemCount} items)</span>
      </h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* ITEMS LIST */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items?.map(item => (
            <div key={item.cartItemId}
              className="bg-white rounded-2xl p-5 border border-orange-100 flex gap-5">
              <img
                src={item.productImage || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=200'}
                alt={item.productName}
                className="w-24 h-24 object-cover rounded-xl flex-shrink-0"
              />
              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-start gap-2">
                  <h3 className="font-semibold text-amber-900 leading-tight">{item.productName}</h3>
                  <button onClick={() => handleRemove(item.cartItemId)}
                    className="text-red-400 hover:text-red-600 p-1 shrink-0 transition-colors">
                    <Trash2 size={16} />
                  </button>
                </div>

                {/* Customization details */}
                <div className="flex flex-wrap gap-2 mt-2">
                  {item.flavor && <span className="text-xs bg-orange-50 text-orange-600 px-2 py-1 rounded-lg">{item.flavor}</span>}
                  {item.sizeKg && <span className="text-xs bg-orange-50 text-orange-600 px-2 py-1 rounded-lg">{item.sizeKg} kg</span>}
                  {item.isEggless && <span className="text-xs bg-green-50 text-green-600 px-2 py-1 rounded-lg">Eggless</span>}
                  {item.customMessage && <span className="text-xs bg-blue-50 text-blue-600 px-2 py-1 rounded-lg">"{item.customMessage}"</span>}
                </div>

                <div className="flex items-center justify-between mt-3">
                  {/* Quantity control */}
                  <div className="flex items-center gap-2">
                    <button onClick={() => handleQty(item.cartItemId, item.quantity - 1)}
                      disabled={item.quantity <= 1}
                      className="w-7 h-7 rounded-lg bg-orange-50 border border-orange-200 flex items-center justify-center disabled:opacity-40 hover:bg-orange-100 transition-colors">
                      <Minus size={12} />
                    </button>
                    <span className="w-6 text-center font-semibold text-amber-900 text-sm">{item.quantity}</span>
                    <button onClick={() => handleQty(item.cartItemId, item.quantity + 1)}
                      className="w-7 h-7 rounded-lg bg-orange-50 border border-orange-200 flex items-center justify-center hover:bg-orange-100 transition-colors">
                      <Plus size={12} />
                    </button>
                  </div>
                  <span className="font-bold text-amber-900">₹{item.itemTotal?.toFixed(2)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* ORDER SUMMARY */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-2xl border border-orange-100 p-6 sticky top-24">
            <h2 className="font-semibold text-amber-900 text-lg mb-5">Order Summary</h2>

            <div className="space-y-3 text-sm">
              <div className="flex justify-between text-amber-700">
                <span>Subtotal ({cart.itemCount} items)</span>
                <span className="font-medium">₹{cart.subtotal?.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-amber-700">
                <span>Delivery</span>
                <span className={`font-medium ${cart.freeDeliveryEligible ? 'text-green-600' : ''}`}>
                  {cart.freeDeliveryEligible ? 'FREE' : `₹${cart.deliveryCharge}`}
                </span>
              </div>
              {!cart.freeDeliveryEligible && (
                <p className="text-xs text-orange-400">
                  Add ₹{(500 - cart.subtotal).toFixed(0)} more for free delivery
                </p>
              )}
            </div>

            <div className="border-t border-orange-100 mt-4 pt-4 flex justify-between font-bold text-amber-900 text-lg">
              <span>Total</span>
              <span>₹{cart.total?.toFixed(2)}</span>
            </div>

            {/* Coupon */}
            <div className="mt-4 flex gap-2">
              <div className="flex-1 flex items-center gap-2 px-3 py-2.5 rounded-xl border border-orange-200 bg-orange-50">
                <Tag size={14} className="text-orange-400" />
                <input placeholder="Coupon code" className="bg-transparent text-sm outline-none w-full text-amber-800" />
              </div>
              <button className="px-4 py-2.5 bg-amber-800 text-white rounded-xl text-sm font-medium hover:bg-amber-700 transition-colors">
                Apply
              </button>
            </div>

            {/* Trust badges */}
            <div className="mt-4 flex gap-2 text-xs text-green-600">
              <span>✓ Fresh Guarantee</span>
              <span>✓ On-time or refund</span>
            </div>

            <button onClick={() => navigate('/checkout')}
              className="w-full mt-5 bg-amber-800 hover:bg-amber-700 text-white py-4 rounded-2xl font-bold text-lg transition-all hover:scale-[1.02] flex items-center justify-center gap-2">
              <ShoppingBag size={20} /> Proceed to Checkout
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}