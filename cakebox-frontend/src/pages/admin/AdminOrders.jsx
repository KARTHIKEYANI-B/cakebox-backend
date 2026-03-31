import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import api from '../../api/axios'
import toast from 'react-hot-toast'

const STATUSES = ['ORDER_PLACED', 'CONFIRMED', 'BAKING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED']

const STATUS_COLORS = {
  ORDER_PLACED:      'bg-blue-100 text-blue-700',
  CONFIRMED:         'bg-purple-100 text-purple-700',
  BAKING:            'bg-amber-100 text-amber-700',
  OUT_FOR_DELIVERY:  'bg-orange-100 text-orange-700',
  DELIVERED:         'bg-green-100 text-green-700',
  CANCELLED:         'bg-red-100 text-red-700',
}

const STATUS_ICONS = {
  ORDER_PLACED: '📦', CONFIRMED: '✅', BAKING: '🍰',
  OUT_FOR_DELIVERY: '🚚', DELIVERED: '🎉', CANCELLED: '❌',
}

export default function AdminOrders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('ALL')
  const [updating, setUpdating] = useState(null)

  useEffect(() => {
    fetchOrders()
  }, [])

  const fetchOrders = async () => {
    try {
      const res = await api.get('/admin/orders')
      setOrders(res.data)
    } catch { toast.error('Could not load orders') }
    finally { setLoading(false) }
  }

  const updateStatus = async (orderId, status) => {
    setUpdating(orderId)
    try {
      await api.put(`/admin/orders/${orderId}/status`, { status })
      setOrders(prev => prev.map(o =>
        o.orderId === orderId ? { ...o, status } : o
      ))
      toast.success(`Order #${orderId} → ${status.replace(/_/g, ' ')}`)
    } catch { toast.error('Could not update status') }
    finally { setUpdating(null) }
  }

  const filtered = filter === 'ALL' ? orders : orders.filter(o => o.status === filter)

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex items-center gap-4 mb-8">
        <Link to="/admin" className="p-2 hover:bg-orange-50 rounded-xl transition-colors">
          <ChevronLeft size={22} className="text-amber-800" />
        </Link>
        <div>
          <h1 className="font-display text-3xl font-bold text-amber-900">All Orders</h1>
          <p className="text-orange-400 mt-0.5">{orders.length} total orders</p>
        </div>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2 overflow-x-auto hide-scrollbar mb-6 pb-1">
        {['ALL', ...STATUSES].map(s => (
          <button key={s} onClick={() => setFilter(s)}
            className={`shrink-0 px-4 py-2 rounded-xl text-sm font-medium transition-all ${
              filter === s
                ? 'bg-amber-800 text-white'
                : 'bg-white border border-orange-100 text-amber-700 hover:border-orange-300'
            }`}>
            {STATUS_ICONS[s] || '📋'} {s.replace(/_/g, ' ')}
            {s !== 'ALL' && (
              <span className="ml-1.5 text-xs opacity-70">
                ({orders.filter(o => o.status === s).length})
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Orders table */}
      {loading ? (
        <div className="space-y-4">
          {[1,2,3,4,5].map(i => <div key={i} className="shimmer h-24 rounded-2xl" />)}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20">
          <p className="text-5xl mb-4">📭</p>
          <p className="text-amber-700 font-semibold">No orders in this status</p>
        </div>
      ) : (
        <div className="space-y-4">
          {filtered.map(order => (
            <div key={order.orderId}
              className="bg-white rounded-2xl border border-orange-100 p-5 flex flex-col sm:flex-row sm:items-center gap-5">

              {/* Image + Info */}
              <div className="flex gap-4 items-center flex-1 min-w-0">
                <img
                  src={order.firstItemImage ||
                    'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=100'}
                  alt=""
                  className="w-16 h-16 rounded-xl object-cover flex-shrink-0"
                />
                <div className="min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <p className="font-bold text-amber-900">Order #{order.orderId}</p>
                    <span className={`text-xs px-2 py-0.5 rounded-lg font-medium ${STATUS_COLORS[order.status]}`}>
                      {STATUS_ICONS[order.status]} {order.status?.replace(/_/g, ' ')}
                    </span>
                    {order.paymentStatus === 'PAID' && (
                      <span className="text-xs bg-green-50 text-green-600 px-2 py-0.5 rounded-lg font-medium">💳 Paid</span>
                    )}
                  </div>
                  <p className="text-sm text-amber-700 mt-0.5 truncate">{order.firstItemName}</p>
                  <div className="flex gap-3 text-xs text-orange-400 mt-1">
                    <span>{order.itemCount} item{order.itemCount > 1 ? 's' : ''}</span>
                    {order.deliveryDate && <span>📅 {order.deliveryDate}</span>}
                  </div>
                </div>
              </div>

              {/* Amount */}
              <div className="text-right shrink-0">
                <p className="font-bold text-amber-900 text-lg">₹{order.totalAmount?.toFixed(0)}</p>
                <p className="text-xs text-orange-400 mt-0.5">
                  {new Date(order.createdAt).toLocaleDateString('en-IN', {
                    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
                  })}
                </p>
              </div>

              {/* Status updater */}
              <div className="shrink-0">
                <label className="block text-xs text-orange-400 font-medium mb-1.5">Update Status</label>
                <select
                  value={order.status}
                  onChange={e => updateStatus(order.orderId, e.target.value)}
                  disabled={updating === order.orderId || order.status === 'DELIVERED' || order.status === 'CANCELLED'}
                  className="w-44 px-3 py-2 rounded-xl border border-orange-200 text-sm text-amber-900 focus:outline-none focus:border-orange-400 bg-white disabled:opacity-50 disabled:cursor-not-allowed">
                  {STATUSES.map(s => (
                    <option key={s} value={s}>{STATUS_ICONS[s]} {s.replace(/_/g, ' ')}</option>
                  ))}
                </select>
                {updating === order.orderId && (
                  <p className="text-xs text-orange-400 mt-1 animate-pulse">Updating...</p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}