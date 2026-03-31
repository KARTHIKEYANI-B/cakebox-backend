import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Shield, Truck, Star, Clock } from 'lucide-react'
import api from '../api/axios'
import ProductCard from '../components/ProductCard'

const OCCASIONS = [
  { label: 'All', icon: '✨', tag: 'ALL' },
  { label: 'Birthday', icon: '🎂', tag: 'BIRTHDAY' },
  { label: 'Anniversary', icon: '❤️', tag: 'ANNIVERSARY' },
  { label: 'Wedding', icon: '💍', tag: 'WEDDING' },
  { label: 'Festival', icon: '🎇', tag: 'FESTIVAL' },
]

function Skeleton() {
  return (
    <div className="bg-white rounded-2xl overflow-hidden border border-orange-100">
      <div className="shimmer h-52 w-full" />
      <div className="p-4 space-y-3">
        <div className="shimmer h-3 w-20 rounded" />
        <div className="shimmer h-4 w-full rounded" />
        <div className="shimmer h-4 w-3/4 rounded" />
        <div className="shimmer h-6 w-24 rounded" />
      </div>
    </div>
  )
}

export default function HomePage() {
  const [searchParams] = useSearchParams()
  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState([])
  const [trending, setTrending] = useState([])
  const [activeOccasion, setActiveOccasion] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [timeLeft, setTimeLeft] = useState({ h: 1, m: 47, s: 30 })

  // Delivery countdown timer
  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(t => {
        let { h, m, s } = t
        s--
        if (s < 0) { s = 59; m-- }
        if (m < 0) { m = 59; h-- }
        if (h < 0) return { h: 1, m: 59, s: 59 }
        return { h, m, s }
      })
    }, 1000)
    return () => clearInterval(timer)
  }, [])

  // Initial load
  useEffect(() => {
    const search = searchParams.get('search')
    const occasion = searchParams.get('occasion')
    if (occasion) setActiveOccasion(occasion)
    loadData(search, occasion)
  }, [searchParams])

  const loadData = async (search, occasion) => {
    setLoading(true)
    try {
      const [catRes, trendRes] = await Promise.all([
        api.get('/categories'),
        api.get('/products/trending'),
      ])
      setCategories(catRes.data)
      setTrending(trendRes.data)

      let prodRes
      if (search) {
        prodRes = await api.get(`/products/search?keyword=${search}`)
      } else if (occasion && occasion !== 'ALL') {
        prodRes = await api.get(`/products/occasion/${occasion}`)
      } else {
        prodRes = await api.get('/products')
      }
      setProducts(prodRes.data)
    } catch (e) {
      console.error('HomePage load error:', e)
      setProducts([])
    } finally {
      setLoading(false)
    }
  }

  // Occasion click handler — fixed
  const handleOccasion = async (tag) => {
    setActiveOccasion(tag)
    setLoading(true)
    setProducts([]) // Clear first so UI updates
    try {
      let res
      if (tag === 'ALL') {
        res = await api.get('/products')
      } else {
        res = await api.get(`/products/occasion/${tag}`)
      }
      setProducts(res.data || [])
    } catch (e) {
      console.error('Occasion filter error:', e)
      setProducts([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-4 pb-16">

      {/* ── HERO SECTION ───────────────────────────────────── */}
      <section className="relative overflow-hidden rounded-3xl my-6 bg-gradient-to-br from-amber-900 via-amber-800 to-rose-900 text-white">
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-0 right-0 w-96 h-96 bg-orange-300 rounded-full translate-x-1/2 -translate-y-1/2 opacity-10 blur-3xl" />
          <div className="absolute bottom-0 left-0 w-64 h-64 bg-rose-300 rounded-full -translate-x-1/2 translate-y-1/2 opacity-10 blur-3xl" />
        </div>

        <div className="relative flex flex-col md:flex-row items-center gap-8 p-8 md:p-12">
          <div className="flex-1">
            {/* Delivery timer badge */}
            <div className="inline-flex items-center gap-2 bg-white/20 backdrop-blur px-4 py-2 rounded-full text-sm mb-6">
              <Clock size={14} className="text-orange-300" />
              <span>Fresh cakes ready in </span>
              <span className="font-bold text-orange-300">
                {String(timeLeft.h).padStart(2, '0')}:{String(timeLeft.m).padStart(2, '0')}:{String(timeLeft.s).padStart(2, '0')}
              </span>
            </div>

            <h1 className="font-display text-4xl md:text-5xl lg:text-6xl font-bold leading-tight mb-4">
              Fresh Cakes<br />in 2 Hours 🚀
            </h1>
            <p className="text-amber-200 text-lg mb-8 max-w-md">
              Handcrafted with love, delivered fresh to your door. Order now for same-day delivery in Karur.
            </p>

            <div className="flex flex-wrap gap-4">
              <a href="#products"
                className="bg-orange-500 hover:bg-orange-400 text-white px-8 py-3.5 rounded-2xl font-semibold text-lg transition-all hover:scale-105 shadow-lg">
                Order Now
              </a>
              <Link to="/customize"
                className="bg-white/20 hover:bg-white/30 backdrop-blur text-white px-8 py-3.5 rounded-2xl font-semibold text-lg transition-all flex items-center gap-2">
                🎨 Customize Cake
              </Link>
            </div>

            {/* Trust badges */}
            <div className="flex flex-wrap gap-4 mt-8 text-sm text-amber-300">
              <span className="flex items-center gap-1"><Shield size={14} />Fresh Guarantee</span>
              <span className="flex items-center gap-1"><Truck size={14} />Free delivery ₹500+</span>
              <span className="flex items-center gap-1"><Star size={14} />4.8★ rated</span>
            </div>
          </div>

          <div className="hidden md:block flex-shrink-0">
            <img
              src="https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500"
              alt="Fresh Cake"
              className="w-72 h-72 object-cover rounded-3xl shadow-2xl rotate-3 hover:rotate-0 transition-transform duration-500"
            />
          </div>
        </div>
      </section>

      {/* ── OCCASION BAR ─────────────────────────────────────── */}
      <section className="my-8" id="occasions">
        <h2 className="font-display text-2xl font-bold text-amber-900 mb-4">Shop by Occasion</h2>
        <div className="flex gap-3 overflow-x-auto hide-scrollbar pb-2">
          {OCCASIONS.map(o => (
            <button
              key={o.tag}
              onClick={() => handleOccasion(o.tag)}
              className={`flex flex-col items-center gap-2 px-6 py-4 rounded-2xl border-2 shrink-0 transition-all ${
                activeOccasion === o.tag
                  ? 'border-orange-500 bg-orange-50 shadow-md scale-105'
                  : 'border-orange-100 bg-white hover:border-orange-300'
              }`}
            >
              <span className="text-2xl">{o.icon}</span>
              <span className="text-sm font-semibold text-amber-900">{o.label}</span>
            </button>
          ))}
        </div>
      </section>

      {/* ── CATEGORY GRID ──────────────────────────────────────── */}
      {categories.length > 0 && (
        <section className="my-8">
          <h2 className="font-display text-2xl font-bold text-amber-900 mb-4">Categories</h2>
          <div className="grid grid-cols-3 sm:grid-cols-5 gap-3">
            {categories.map(cat => (
              <button
                key={cat.id}
                onClick={() => {
                  setLoading(true)
                  api.get(`/products/category/${cat.id}`)
                    .then(r => setProducts(r.data || []))
                    .catch(() => setProducts([]))
                    .finally(() => setLoading(false))
                  setActiveOccasion('')
                }}
                className="card-lift bg-white rounded-2xl p-4 flex flex-col items-center gap-2 border border-orange-100 text-center"
              >
                <div className="w-16 h-16 rounded-2xl overflow-hidden bg-orange-50 flex items-center justify-center">
                  {cat.imageUrl
                    ? <img src={cat.imageUrl} alt={cat.name} className="w-full h-full object-cover" />
                    : <span className="text-3xl">🎂</span>
                  }
                </div>
                <span className="text-sm font-semibold text-amber-900">{cat.name}</span>
                <span className="text-xs text-orange-400">{cat.productCount} items</span>
              </button>
            ))}
          </div>
        </section>
      )}

      {/* ── TRENDING ─────────────────────────────────────────── */}
      {trending.length > 0 && (
        <section className="my-10">
          <div className="mb-5">
            <h2 className="font-display text-2xl font-bold text-amber-900">🔥 Popular Near You</h2>
            <p className="text-sm text-orange-400 mt-1">Trending in Karur</p>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {trending.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}

      {/* ── ALL PRODUCTS ─────────────────────────────────────── */}
      <section className="my-10" id="products">
        <div className="flex items-center justify-between mb-5">
          <h2 className="font-display text-2xl font-bold text-amber-900">
            {searchParams.get('search')
              ? `Results for "${searchParams.get('search')}"`
              : activeOccasion && activeOccasion !== 'ALL'
              ? `${activeOccasion.charAt(0) + activeOccasion.slice(1).toLowerCase()} Cakes`
              : 'All Cakes'}
          </h2>
          <span className="text-sm text-orange-400">{products.length} items</span>
        </div>

        {loading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {Array(8).fill(0).map((_, i) => <Skeleton key={i} />)}
          </div>
        ) : products.length === 0 ? (
          <div className="text-center py-20 bg-white rounded-3xl border border-orange-100">
            <span className="text-6xl">🎂</span>
            <p className="text-amber-800 font-semibold text-xl mt-4">No cakes found</p>
            <p className="text-orange-400 text-sm mt-2">Try a different filter or search</p>
            <button
              onClick={() => handleOccasion('ALL')}
              className="mt-6 bg-amber-800 text-white px-6 py-3 rounded-xl font-semibold hover:bg-amber-700 transition-colors">
              Show All Cakes
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {products.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        )}
      </section>

      {/* ── CUSTOMIZE CTA ──────────────────────────────────────── */}
      <section className="my-10 bg-gradient-to-r from-rose-50 to-orange-50 rounded-3xl p-8 border-2 border-dashed border-orange-200">
        <div className="flex flex-col md:flex-row items-center gap-6">
          <div className="text-6xl">🎨</div>
          <div className="flex-1 text-center md:text-left">
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-2">Design Your Dream Cake</h2>
            <p className="text-amber-700 mb-3">Choose flavor → shape → write a message → upload your design</p>
            <div className="flex gap-3 text-sm text-orange-500 justify-center md:justify-start flex-wrap">
              <span>🍫 Flavor</span><span>→</span>
              <span>⬛ Shape</span><span>→</span>
              <span>💬 Message</span><span>→</span>
              <span>📸 Photo</span>
            </div>
          </div>
          <Link
            to="/customize"
            className="bg-amber-800 hover:bg-amber-700 text-white px-8 py-4 rounded-2xl font-semibold text-lg transition-all hover:scale-105 whitespace-nowrap">
            Start Customizing
          </Link>
        </div>
      </section>

      {/* ── REVIEWS ──────────────────────────────────────────── */}
      <section className="my-10">
        <h2 className="font-display text-2xl font-bold text-amber-900 mb-6 text-center">What Our Customers Say 💬</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[
            { name: 'Priya S.', text: 'Delivered in 1 hour! The red velvet was absolutely divine. Best cake in Karur!', stars: 5 },
            { name: 'Raj K.', text: "Ordered a custom cake for my wife's birthday. The message and design were perfect!", stars: 5 },
            { name: 'Meena T.', text: 'Eggless chocolate truffle was so moist and delicious. Will definitely order again!', stars: 5 },
          ].map((r, i) => (
            <div key={i} className="bg-white rounded-2xl p-6 border border-orange-100 shadow-sm">
              <div className="flex gap-1 mb-3">
                {Array(r.stars).fill(0).map((_, j) => (
                  <Star key={j} size={16} className="fill-amber-400 text-amber-400" />
                ))}
              </div>
              <p className="text-amber-800 text-sm leading-relaxed mb-4">"{r.text}"</p>
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 bg-amber-800 rounded-full flex items-center justify-center text-white font-bold text-sm">
                  {r.name.charAt(0)}
                </div>
                <span className="font-semibold text-sm text-amber-900">{r.name}</span>
              </div>
            </div>
          ))}
        </div>
      </section>

    </div>
  )
}