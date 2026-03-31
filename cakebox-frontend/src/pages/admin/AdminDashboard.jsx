import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Package, ShoppingBag, Users, TrendingUp } from 'lucide-react'
import api from '../../api/axios'

export default function AdminDashboard() {
  const [stats, setStats] = useState({ products: 0, orders: 0 })
  const [recentOrders, setRecentOrders] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.get('/admin/products'),
      api.get('/admin/orders')
    ]).then(([prodRes, orderRes]) => {
      setStats({ products: prodRes.data.length, orders: orderRes.data.length })
      setRecentOrders(orderRes.data.slice(0, 5))
      setLoading(false)
    }).catch(() => setLoading(false))
  }, [])

  const STATUS_COLORS = {
    ORDER_PLACED: 'bg-blue-100 text-blue-700',
    CONFIRMED: 'bg-purple-100 text-purple-700',
    BAKING: 'bg-amber-100 text-amber-700',
    OUT_FOR_DELIVERY: 'bg-orange-100 text-orange-700',
    DELIVERED: 'bg-green-100 text-green-700',
    CANCELLED: 'bg-red-100 text-red-700',
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="font-display text-3xl font-bold text-amber-900">Admin Dashboard</h1>
          <p className="text-orange-400 mt-1">Manage your CakeBox store</p>
        </div>
        <div className="flex gap-3">
          <Link to="/admin/products" className="bg-amber-800 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-amber-700 transition-colors">
            Manage Products
          </Link>
          <Link to="/admin/orders" className="border-2 border-amber-800 text-amber-800 px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-amber-50 transition-colors">
            View Orders
          </Link>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        {[
          { label: 'Total Products', value: stats.products, icon: Package, color: 'bg-blue-50 text-blue-600' },
          { label: 'Total Orders', value: stats.orders, icon: ShoppingBag, color: 'bg-green-50 text-green-600' },
          { label: 'Delivered Today', value: recentOrders.filter(o => o.status === 'DELIVERED').length, icon: TrendingUp, color: 'bg-amber-50 text-amber-600' },
          { label: 'Pending', value: recentOrders.filter(o => o.status === 'ORDER_PLACED').length, icon: Users, color: 'bg-rose-50 text-rose-600' },
        ].map(s => (
          <div key={s.label} className="bg-white rounded-2xl border border-orange-100 p-5">
            <div className={`w-10 h-10 rounded-xl flex items-center justify-center mb-3 ${s.color}`}>
              <s.icon size={20} />
            </div>
            <p className="text-2xl font-bold text-amber-900">{s.value}</p>
            <p className="text-xs text-orange-400 mt-1">{s.label}</p>
          </div>
        ))}
      </div>

      {/* Recent Orders */}
      <div className="bg-white rounded-2xl border border-orange-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-orange-100 flex items-center justify-between">
          <h2 className="font-semibold text-amber-900">Recent Orders</h2>
          <Link to="/admin/orders" className="text-sm text-orange-500 font-medium">View all →</Link>
        </div>
        {loading ? (
          <div className="p-6 space-y-3">
            {[1,2,3].map(i => <div key={i} className="shimmer h-12 rounded-xl" />)}
          </div>
        ) : (
          <div className="divide-y divide-orange-50">
            {recentOrders.map(order => (
              <div key={order.orderId} className="px-6 py-4 flex items-center gap-4">
                <img src={order.firstItemImage || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=100'}
                  className="w-12 h-12 rounded-xl object-cover" alt="" />
                <div className="flex-1">
                  <p className="font-medium text-amber-900 text-sm">Order #{order.orderId}</p>
                  <p className="text-xs text-orange-400">{order.firstItemName} · {order.itemCount} items</p>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-amber-900 text-sm">₹{order.totalAmount?.toFixed(0)}</p>
                  <span className={`text-xs px-2 py-1 rounded-lg font-medium ${STATUS_COLORS[order.status] || 'bg-gray-100 text-gray-600'}`}>
                    {order.status?.replace('_', ' ')}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}