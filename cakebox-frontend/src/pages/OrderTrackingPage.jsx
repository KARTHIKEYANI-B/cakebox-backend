import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/axios'

export default function OrderTrackingPage() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get(`/orders/${id}`)
      .then(r => { setOrder(r.data); setLoading(false) })
      .catch(() => setLoading(false))

    // Poll every 30 seconds for status updates
    const interval = setInterval(() => {
      api.get(`/orders/${id}`).then(r => setOrder(r.data)).catch(() => {})
    }, 30000)
    return () => clearInterval(interval)
  }, [id])

  if (loading) return (
    <div className="max-w-2xl mx-auto px-4 py-12">
      <div className="shimmer h-8 w-48 rounded mb-8" />
      {[1,2,3].map(i => <div key={i} className="shimmer h-20 rounded-2xl mb-4" />)}
    </div>
  )

  if (!order) return (
    <div className="text-center py-24">
      <p className="text-amber-800 text-lg">Order not found</p>
      <Link to="/" className="text-orange-500 mt-4 block">Back to Home</Link>
    </div>
  )

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="text-center mb-8">
        <div className="text-5xl mb-3">🎂</div>
        <h1 className="font-display text-3xl font-bold text-amber-900">Order #{order.orderId}</h1>
        <p className="text-orange-400 mt-1">
          {order.paymentStatus === 'PAID' ? '✅ Payment confirmed' : '⏳ Payment pending'}
        </p>
      </div>

      {/* TRACKING STEPS */}
      <div className="bg-white rounded-3xl border border-orange-100 p-8 mb-6">
        <h2 className="font-semibold text-amber-900 mb-8 text-center">Live Tracking</h2>
        <div className="relative">
          {/* Progress line */}
          <div className="absolute left-6 top-0 bottom-0 w-0.5 bg-orange-100" />
          <div className="absolute left-6 top-0 w-0.5 bg-amber-800 transition-all"
            style={{ height: `${(order.trackingSteps?.filter(s => s.completed).length - 1) / (order.trackingSteps?.length - 1) * 100}%` }} />

          <div className="space-y-6">
            {order.trackingSteps?.map((step, i) => (
              <div key={step.step} className={`flex items-center gap-5 transition-all ${step.completed ? 'opacity-100' : 'opacity-40'}`}>
                <div className={`w-12 h-12 rounded-full flex items-center justify-center text-2xl z-10 transition-all ${
                  step.active ? 'bg-amber-800 shadow-lg scale-110' :
                  step.completed ? 'bg-amber-100' : 'bg-gray-100'
                }`}>
                  {step.icon}
                </div>
                <div className="flex-1">
                  <p className={`font-semibold ${step.active ? 'text-amber-900 text-lg' : 'text-amber-700'}`}>
                    {step.label}
                    {step.active && <span className="ml-2 text-sm text-orange-400 font-normal animate-pulse">● In progress</span>}
                  </p>
                </div>
                {step.completed && !step.active && (
                  <span className="text-green-500 font-bold">✓</span>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Delivery info */}
      {order.deliveryDate && (
        <div className="bg-orange-50 rounded-2xl p-5 mb-6">
          <h3 className="font-semibold text-amber-900 mb-3">Delivery Details</h3>
          <div className="space-y-2 text-sm text-amber-700">
            <p>📅 Date: <span className="font-medium">{order.deliveryDate}</span></p>
            <p>⏰ Slot: <span className="font-medium">{order.deliverySlot}</span></p>
            {order.isExpressDelivery && <p>⚡ <span className="font-medium text-orange-600">Express Delivery</span></p>}
            {order.isSurpriseMode && <p>🎁 <span className="font-medium text-rose-600">Surprise Mode Active</span></p>}
          </div>
        </div>
      )}

      {/* Items */}
      <div className="bg-white rounded-2xl border border-orange-100 p-5 mb-6">
        <h3 className="font-semibold text-amber-900 mb-4">Items Ordered</h3>
        {order.items?.map(item => (
          <div key={item.orderItemId} className="flex gap-4 items-center mb-4 last:mb-0">
            <img src={item.productImage || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=100'}
              className="w-14 h-14 rounded-xl object-cover" alt={item.productName} />
            <div className="flex-1">
              <p className="font-medium text-amber-900 text-sm">{item.productName}</p>
              <p className="text-xs text-orange-400">{item.flavor} · {item.sizeKg}kg · Qty {item.quantity}</p>
              {item.customMessage && <p className="text-xs text-blue-500 mt-0.5">"{item.customMessage}"</p>}
            </div>
            <span className="font-semibold text-amber-900 text-sm">₹{item.totalPrice?.toFixed(0)}</span>
          </div>
        ))}
      </div>

      {/* Total */}
      <div className="bg-amber-50 rounded-2xl p-5 mb-8">
        <div className="flex justify-between font-bold text-amber-900 text-lg">
          <span>Total Paid</span>
          <span>₹{order.totalAmount?.toFixed(2)}</span>
        </div>
      </div>

      <div className="flex gap-4">
        <Link to="/" className="flex-1 text-center border-2 border-amber-800 text-amber-800 py-3.5 rounded-2xl font-semibold hover:bg-amber-50 transition-colors">
          Continue Shopping
        </Link>
      </div>
    </div>
  )
}