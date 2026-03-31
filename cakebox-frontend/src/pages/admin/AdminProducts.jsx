import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Pencil, Trash2, ChevronLeft, X, Upload } from 'lucide-react'
import api from '../../api/axios'
import toast from 'react-hot-toast'

const EMPTY_FORM = {
  name: '', description: '', price: '', discountPrice: '',
  occasionTag: 'ALL', isTrending: false, egglessAvailable: true,
  stockQuantity: 100, categoryId: '',
  flavors: 'Chocolate,Vanilla,Red Velvet',
  sizes: '0.5,1,2',
  image: null, imagePreview: null,
}

export default function AdminProducts() {
  const [products, setProducts]   = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading]     = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editProduct, setEditProduct] = useState(null)
  const [form, setForm]           = useState(EMPTY_FORM)
  const [saving, setSaving]       = useState(false)

  useEffect(() => {
    fetchAll()
  }, [])

  const fetchAll = async () => {
    try {
      const [pRes, cRes] = await Promise.all([
        api.get('/admin/products'),
        api.get('/categories'),
      ])
      setProducts(pRes.data)
      setCategories(cRes.data)
    } catch { toast.error('Could not load data') }
    finally { setLoading(false) }
  }

  const openAdd = () => {
    setEditProduct(null)
    setForm({ ...EMPTY_FORM, categoryId: categories[0]?.id || '' })
    setShowModal(true)
  }

  const openEdit = (p) => {
    setEditProduct(p)
    setForm({
      name: p.name || '',
      description: p.description || '',
      price: p.price || '',
      discountPrice: p.discountPrice || '',
      occasionTag: p.occasionTag || 'ALL',
      isTrending: p.isTrending || false,
      egglessAvailable: p.egglessAvailable ?? true,
      stockQuantity: p.stockQuantity || 100,
      categoryId: p.categoryId || '',
      flavors: p.availableFlavors?.join(',') || '',
      sizes: p.availableSizes?.join(',') || '',
      image: null, imagePreview: p.mainImageUrl || null,
    })
    setShowModal(true)
  }

  const handleImageChange = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = ev => setForm(f => ({ ...f, image: file, imagePreview: ev.target.result }))
    reader.readAsDataURL(file)
  }

  const handleSave = async () => {
    if (!form.name || !form.price || !form.categoryId) {
      toast.error('Name, price and category are required')
      return
    }
    setSaving(true)
    try {
      const data = new FormData()
      data.append('name', form.name)
      data.append('description', form.description)
      data.append('price', form.price)
      if (form.discountPrice) data.append('discountPrice', form.discountPrice)
      data.append('occasionTag', form.occasionTag)
      data.append('isTrending', form.isTrending)
      data.append('egglessAvailable', form.egglessAvailable)
      data.append('stockQuantity', form.stockQuantity)
      data.append('categoryId', form.categoryId)
      form.flavors.split(',').forEach(f => data.append('flavors', f.trim()))
      form.sizes.split(',').forEach(s => data.append('sizes', s.trim()))
      if (form.image) data.append('mainImage', form.image)

      if (editProduct) {
        const res = await api.put(`/admin/products/${editProduct.id}`, data, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        setProducts(prev => prev.map(p => p.id === editProduct.id ? res.data : p))
        toast.success('Product updated! ✅')
      } else {
        const res = await api.post('/admin/products', data, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        setProducts(prev => [res.data, ...prev])
        toast.success('Product added! 🎂')
      }
      setShowModal(false)
    } catch (e) {
      toast.error(e.response?.data?.error || 'Could not save product')
    } finally { setSaving(false) }
  }

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete "${name}"? This hides it from customers.`)) return
    try {
      await api.delete(`/admin/products/${id}`)
      setProducts(prev => prev.filter(p => p.id !== id))
      toast.success('Product removed')
    } catch { toast.error('Could not delete') }
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center gap-4">
          <Link to="/admin" className="p-2 hover:bg-orange-50 rounded-xl transition-colors">
            <ChevronLeft size={22} className="text-amber-800" />
          </Link>
          <div>
            <h1 className="font-display text-3xl font-bold text-amber-900">Products</h1>
            <p className="text-orange-400 mt-0.5">{products.length} products</p>
          </div>
        </div>
        <button onClick={openAdd}
          className="flex items-center gap-2 bg-amber-800 text-white px-5 py-2.5 rounded-xl font-semibold hover:bg-amber-700 transition-colors">
          <Plus size={18} /> Add Product
        </button>
      </div>

      {/* Products grid */}
      {loading ? (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {[1,2,3,4,5,6,7,8].map(i => <div key={i} className="shimmer h-64 rounded-2xl" />)}
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {products.map(p => (
            <div key={p.id} className="bg-white rounded-2xl border border-orange-100 overflow-hidden group">
              <div className="relative h-44 bg-orange-50">
                <img
                  src={p.mainImageUrl || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400'}
                  alt={p.name}
                  className="w-full h-full object-cover"
                />
                {/* Action overlay */}
                <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-3">
                  <button onClick={() => openEdit(p)}
                    className="bg-white text-amber-800 p-2.5 rounded-xl hover:bg-amber-50 transition-colors">
                    <Pencil size={16} />
                  </button>
                  <button onClick={() => handleDelete(p.id, p.name)}
                    className="bg-white text-red-500 p-2.5 rounded-xl hover:bg-red-50 transition-colors">
                    <Trash2 size={16} />
                  </button>
                </div>
                {p.isTrending && (
                  <span className="absolute top-2 left-2 bg-amber-500 text-white text-xs px-2 py-0.5 rounded-lg font-medium">🔥 Trending</span>
                )}
              </div>
              <div className="p-3">
                <p className="font-semibold text-amber-900 text-sm line-clamp-2 leading-tight">{p.name}</p>
                <div className="flex items-center justify-between mt-2">
                  <div>
                    <span className="font-bold text-amber-900 text-sm">₹{p.discountPrice || p.price}</span>
                    {p.discountPrice && (
                      <span className="text-xs text-gray-400 line-through ml-1">₹{p.price}</span>
                    )}
                  </div>
                  <span className="text-xs text-orange-400">{p.categoryName}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ADD / EDIT MODAL */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-start justify-center bg-black/50 overflow-y-auto py-8 px-4">
          <div className="bg-white rounded-3xl w-full max-w-xl my-auto shadow-2xl">
            {/* Modal header */}
            <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-orange-100">
              <h2 className="font-display text-xl font-bold text-amber-900">
                {editProduct ? 'Edit Product' : 'Add New Product'}
              </h2>
              <button onClick={() => setShowModal(false)}
                className="p-2 hover:bg-orange-50 rounded-xl transition-colors">
                <X size={20} className="text-amber-700" />
              </button>
            </div>

            {/* Modal body */}
            <div className="px-6 py-5 space-y-4 max-h-[70vh] overflow-y-auto">

              {/* Image upload */}
              <label className="flex flex-col items-center justify-center gap-2 border-2 border-dashed border-orange-200 rounded-2xl p-5 cursor-pointer hover:border-orange-400 hover:bg-orange-50 transition-all">
                {form.imagePreview ? (
                  <img src={form.imagePreview} className="w-32 h-32 object-cover rounded-xl" alt="preview" />
                ) : (
                  <>
                    <Upload size={24} className="text-orange-400" />
                    <span className="text-sm text-amber-700 font-medium">Upload cake photo</span>
                  </>
                )}
                <input type="file" accept="image/*" onChange={handleImageChange} className="hidden" />
              </label>

              {/* Name */}
              <div>
                <label className="block text-sm font-medium text-amber-800 mb-1.5">Product Name *</label>
                <input value={form.name} onChange={e => setForm(f => ({...f, name: e.target.value}))}
                  placeholder="e.g. Red Velvet Bliss"
                  className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
              </div>

              {/* Description */}
              <div>
                <label className="block text-sm font-medium text-amber-800 mb-1.5">Description</label>
                <textarea rows={3} value={form.description}
                  onChange={e => setForm(f => ({...f, description: e.target.value}))}
                  placeholder="Describe your cake..."
                  className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm resize-none" />
              </div>

              {/* Price row */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-amber-800 mb-1.5">Price (₹) *</label>
                  <input type="number" value={form.price}
                    onChange={e => setForm(f => ({...f, price: e.target.value}))}
                    placeholder="1199"
                    className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-amber-800 mb-1.5">Discount Price (₹)</label>
                  <input type="number" value={form.discountPrice}
                    onChange={e => setForm(f => ({...f, discountPrice: e.target.value}))}
                    placeholder="999"
                    className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
                </div>
              </div>

              {/* Category + Occasion */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-amber-800 mb-1.5">Category *</label>
                  <select value={form.categoryId}
                    onChange={e => setForm(f => ({...f, categoryId: e.target.value}))}
                    className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none text-sm bg-white">
                    <option value="">Select...</option>
                    {categories.map(c => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-amber-800 mb-1.5">Occasion</label>
                  <select value={form.occasionTag}
                    onChange={e => setForm(f => ({...f, occasionTag: e.target.value}))}
                    className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none text-sm bg-white">
                    {['ALL','BIRTHDAY','ANNIVERSARY','WEDDING','FESTIVAL'].map(o => (
                      <option key={o} value={o}>{o}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Flavors */}
              <div>
                <label className="block text-sm font-medium text-amber-800 mb-1.5">Flavors (comma separated)</label>
                <input value={form.flavors}
                  onChange={e => setForm(f => ({...f, flavors: e.target.value}))}
                  placeholder="Chocolate,Vanilla,Red Velvet"
                  className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
              </div>

              {/* Sizes + Stock */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-amber-800 mb-1.5">Sizes (kg, comma sep.)</label>
                  <input value={form.sizes}
                    onChange={e => setForm(f => ({...f, sizes: e.target.value}))}
                    placeholder="0.5,1,2"
                    className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-amber-800 mb-1.5">Stock Qty</label>
                  <input type="number" value={form.stockQuantity}
                    onChange={e => setForm(f => ({...f, stockQuantity: e.target.value}))}
                    className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
                </div>
              </div>

              {/* Toggles */}
              <div className="flex gap-4">
                {[
                  { key: 'isTrending', label: '🔥 Mark as Trending' },
                  { key: 'egglessAvailable', label: '🌱 Eggless Available' },
                ].map(t => (
                  <label key={t.key} className="flex items-center gap-2 cursor-pointer">
                    <button type="button" onClick={() => setForm(f => ({...f, [t.key]: !f[t.key]}))}
                      className={`w-10 h-6 rounded-full relative transition-all ${form[t.key] ? 'bg-amber-800' : 'bg-gray-200'}`}>
                      <span className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-all ${form[t.key] ? 'left-5' : 'left-1'}`} />
                    </button>
                    <span className="text-sm text-amber-800">{t.label}</span>
                  </label>
                ))}
              </div>
            </div>

            {/* Modal footer */}
            <div className="px-6 py-4 border-t border-orange-100 flex gap-3">
              <button onClick={() => setShowModal(false)}
                className="flex-1 border-2 border-amber-800 text-amber-800 py-3 rounded-2xl font-semibold hover:bg-amber-50 transition-colors">
                Cancel
              </button>
              <button onClick={handleSave} disabled={saving}
                className="flex-1 bg-amber-800 text-white py-3 rounded-2xl font-semibold hover:bg-amber-700 transition-all disabled:opacity-50 flex items-center justify-center gap-2">
                {saving
                  ? <><div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> Saving...</>
                  : editProduct ? '✅ Update' : '🎂 Add Product'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}