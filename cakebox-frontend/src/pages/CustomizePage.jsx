import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Upload, ChevronRight, ChevronLeft, Check, ShoppingCart } from 'lucide-react'
import api from '../api/axios'
import { useCart } from '../context/CartContext'
import toast from 'react-hot-toast'

const STEPS = ['Flavor', 'Shape & Size', 'Message', 'Photo & Confirm']

const FLAVORS = [
  { name: 'Chocolate', emoji: '🍫' },
  { name: 'Vanilla', emoji: '🍦' },
  { name: 'Red Velvet', emoji: '❤️' },
  { name: 'Butterscotch', emoji: '🍮' },
  { name: 'Strawberry', emoji: '🍓' },
  { name: 'Pineapple', emoji: '🍍' },
  { name: 'Black Forest', emoji: '🌲' },
  { name: 'White Forest', emoji: '⬜' },
]

const SHAPES = [
  { label: 'Round', icon: '⭕' },
  { label: 'Square', icon: '🟫' },
  { label: 'Heart', icon: '❤️' },
  { label: '3-Tier', icon: '🎂' },
]

const SIZES = ['0.5', '1', '2', '3']

export default function CustomizePage() {
  const navigate = useNavigate()
  const { addToCart } = useCart()

  const [step, setStep] = useState(0)
  const [config, setConfig] = useState({
    flavor: '',
    shape: '',
    sizeKg: '1',
    isEggless: false,
    message: '',
    specialInstructions: '',
    imageFile: null,
    imagePreview: null,
  })
  const [products, setProducts] = useState([])
  const [uploading, setUploading] = useState(false)

  // Load products so we can find the best matching one
  useEffect(() => {
    api.get('/products').then(r => setProducts(r.data)).catch(() => {})
  }, [])

  const canNext = () => {
    if (step === 0) return !!config.flavor
    if (step === 1) return !!config.shape
    return true
  }

  const handleImageChange = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    if (file.size > 5 * 1024 * 1024) { toast.error('Image must be under 5MB'); return }
    const reader = new FileReader()
    reader.onload = (ev) => setConfig(c => ({ ...c, imageFile: file, imagePreview: ev.target.result }))
    reader.readAsDataURL(file)
  }

  // Find the best matching product based on chosen flavor
  const findMatchingProduct = () => {
    if (!products.length) return null

    // Try to find a product whose name or flavors match chosen flavor
    const flavorLower = config.flavor.toLowerCase()
    let match = products.find(p =>
      p.name?.toLowerCase().includes(flavorLower) ||
      p.availableFlavors?.some(f => f.toLowerCase().includes(flavorLower))
    )

    // Fallback: find any cake product
    if (!match) match = products.find(p => p.categoryName?.toLowerCase().includes('cake'))

    // Last fallback: first product
    if (!match) match = products[0]

    return match
  }

  const handleAddToCart = async () => {
    const product = findMatchingProduct()
    if (!product) {
      toast.error('No products available. Please try again.')
      return
    }

    setUploading(true)
    try {
      // Upload reference image if provided
      let imageUrl = null
      if (config.imageFile) {
        const formData = new FormData()
        formData.append('image', config.imageFile)
        try {
          const uploadRes = await api.post('/customize/upload-image', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })
          imageUrl = uploadRes.data.imageUrl
        } catch {
          // If upload fails, continue without image
          console.log('Image upload skipped')
        }
      }

      // Add matching product to cart with custom config
      await addToCart(product.id, {
        quantity: 1,
        flavor: config.flavor,            // ← user's chosen flavor
        sizeKg: config.sizeKg,            // ← user's chosen size
        isEggless: config.isEggless,      // ← user's egg preference
        customMessage: config.message,    // ← message on cake
      })

      toast.success(`Custom ${config.flavor} cake added to cart! 🎂`, { duration: 3000 })
      navigate('/cart')
    } catch (e) {
      toast.error(e.response?.data?.error || 'Could not add to cart. Please try again.')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">

      {/* Header */}
      <div className="text-center mb-10">
        <div className="text-5xl mb-3">🎨</div>
        <h1 className="font-display text-3xl font-bold text-amber-900">Design Your Cake</h1>
        <p className="text-orange-400 mt-2">Build the perfect cake step by step</p>
      </div>

      {/* Step indicator */}
      <div className="flex items-center gap-2 mb-10">
        {STEPS.map((s, i) => (
          <div key={s} className="flex items-center gap-2 flex-1">
            <button
              onClick={() => i < step && setStep(i)}
              className={`w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold transition-all ${
                i < step ? 'bg-green-500 text-white cursor-pointer' :
                i === step ? 'bg-amber-800 text-white' :
                'bg-orange-100 text-amber-600 cursor-not-allowed'
              }`}>
              {i < step ? <Check size={16} /> : i + 1}
            </button>
            <span className={`text-xs font-medium hidden sm:block ${
              i === step ? 'text-amber-900' : i < step ? 'text-green-600' : 'text-orange-300'
            }`}>{s}</span>
            {i < STEPS.length - 1 && (
              <div className={`flex-1 h-0.5 ${i < step ? 'bg-green-400' : 'bg-orange-100'}`} />
            )}
          </div>
        ))}
      </div>

      {/* Step content */}
      <div className="bg-white rounded-3xl border border-orange-100 p-8 min-h-80 shadow-sm">

        {/* STEP 0: FLAVOR */}
        {step === 0 && (
          <div>
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-2">Choose Your Flavor</h2>
            <p className="text-orange-400 text-sm mb-6">This determines how your cake tastes 🍰</p>
            <div className="grid grid-cols-2 gap-3">
              {FLAVORS.map(f => (
                <button
                  key={f.name}
                  onClick={() => setConfig(c => ({ ...c, flavor: f.name }))}
                  className={`p-4 rounded-2xl border-2 font-medium transition-all text-left flex items-center gap-3 ${
                    config.flavor === f.name
                      ? 'border-amber-800 bg-amber-50 text-amber-900 scale-[1.02] shadow-sm'
                      : 'border-orange-100 text-amber-700 hover:border-orange-300 hover:bg-orange-50'
                  }`}>
                  <span className="text-2xl">{f.emoji}</span>
                  <span className="font-semibold">{f.name}</span>
                  {config.flavor === f.name && (
                    <Check size={16} className="ml-auto text-green-500" />
                  )}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* STEP 1: SHAPE & SIZE */}
        {step === 1 && (
          <div>
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-2">Shape & Size</h2>
            <p className="text-orange-400 text-sm mb-6">Pick the shape and how big you want it</p>

            {/* Shape */}
            <p className="font-semibold text-amber-800 mb-3">Shape</p>
            <div className="grid grid-cols-2 gap-3 mb-6">
              {SHAPES.map(s => (
                <button
                  key={s.label}
                  onClick={() => setConfig(c => ({ ...c, shape: s.label }))}
                  className={`p-5 rounded-2xl border-2 flex flex-col items-center gap-2 transition-all ${
                    config.shape === s.label
                      ? 'border-amber-800 bg-amber-50 scale-[1.02] shadow-sm'
                      : 'border-orange-100 hover:border-orange-300'
                  }`}>
                  <span className="text-3xl">{s.icon}</span>
                  <span className="font-semibold text-amber-900 text-sm">{s.label}</span>
                </button>
              ))}
            </div>

            {/* Size */}
            <p className="font-semibold text-amber-800 mb-3">Size (kg)</p>
            <div className="flex gap-3 flex-wrap mb-5">
              {SIZES.map(s => (
                <button
                  key={s}
                  onClick={() => setConfig(c => ({ ...c, sizeKg: s }))}
                  className={`px-6 py-3 rounded-xl border-2 font-semibold transition-all ${
                    config.sizeKg === s
                      ? 'border-amber-800 bg-amber-50 text-amber-900'
                      : 'border-orange-100 text-amber-700 hover:border-orange-300'
                  }`}>
                  {s} kg
                  <span className="block text-xs font-normal text-orange-400 mt-0.5">
                    {s === '0.5' ? '~4 slices' : s === '1' ? '~8 slices' : s === '2' ? '~16 slices' : '~24 slices'}
                  </span>
                </button>
              ))}
            </div>

            {/* Eggless toggle */}
            <div
              onClick={() => setConfig(c => ({ ...c, isEggless: !c.isEggless }))}
              className={`flex items-center justify-between p-4 rounded-xl border-2 cursor-pointer transition-all ${
                config.isEggless ? 'border-green-500 bg-green-50' : 'border-orange-100'
              }`}>
              <div className="flex items-center gap-3">
                <span className="text-2xl">🌱</span>
                <div>
                  <p className="font-semibold text-amber-900 text-sm">Make it Eggless</p>
                  <p className="text-xs text-orange-400">Perfect for vegetarians</p>
                </div>
              </div>
              <div className={`w-12 h-6 rounded-full relative transition-all ${config.isEggless ? 'bg-green-500' : 'bg-gray-200'}`}>
                <span className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-all shadow-sm ${config.isEggless ? 'left-7' : 'left-1'}`} />
              </div>
            </div>
          </div>
        )}

        {/* STEP 2: MESSAGE */}
        {step === 2 && (
          <div>
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-2">Personal Message</h2>
            <p className="text-orange-400 text-sm mb-6">What would you like written on the cake?</p>
            <div className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-amber-800 mb-2">
                  Message on Cake <span className="text-orange-400 font-normal">(optional)</span>
                </label>
                <input
                  type="text"
                  maxLength={50}
                  value={config.message}
                  onChange={e => setConfig(c => ({ ...c, message: e.target.value }))}
                  placeholder="e.g. Happy Birthday Priya! 🎂"
                  className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm"
                />
                <p className="text-xs text-orange-400 mt-1 text-right">{config.message.length}/50 characters</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-amber-800 mb-2">
                  Special Instructions for Baker <span className="text-orange-400 font-normal">(optional)</span>
                </label>
                <textarea
                  rows={4}
                  value={config.specialInstructions}
                  onChange={e => setConfig(c => ({ ...c, specialInstructions: e.target.value }))}
                  placeholder="e.g. Extra chocolate drizzle on top, sugar-free frosting, etc."
                  className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm resize-none"
                />
              </div>
            </div>
          </div>
        )}

        {/* STEP 3: PHOTO + CONFIRM */}
        {step === 3 && (
          <div>
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-2">Reference Photo</h2>
            <p className="text-orange-400 text-sm mb-6">Show our baker what you have in mind (optional)</p>

            {/* Upload */}
            <label className="flex flex-col items-center justify-center gap-3 border-2 border-dashed border-orange-200 rounded-2xl p-8 cursor-pointer hover:border-orange-400 hover:bg-orange-50 transition-all mb-6">
              {config.imagePreview ? (
                <div className="text-center">
                  <img src={config.imagePreview} className="w-40 h-40 object-cover rounded-2xl mx-auto mb-3" alt="preview" />
                  <p className="text-sm text-orange-500">Click to change photo</p>
                </div>
              ) : (
                <>
                  <Upload size={32} className="text-orange-400" />
                  <span className="text-amber-700 font-medium">Click to upload photo</span>
                  <span className="text-xs text-orange-400">JPG, PNG up to 5MB</span>
                </>
              )}
              <input type="file" accept="image/*" onChange={handleImageChange} className="hidden" />
            </label>

            {/* Order Summary */}
            <div className="bg-gradient-to-r from-orange-50 to-amber-50 rounded-2xl p-5 border border-orange-100">
              <h3 className="font-bold text-amber-900 mb-4 flex items-center gap-2">
                <span>🎂</span> Your Custom Cake Summary
              </h3>
              <div className="grid grid-cols-2 gap-y-3 text-sm">
                <span className="text-amber-600">Flavor:</span>
                <span className="font-semibold text-amber-900">{config.flavor}</span>

                <span className="text-amber-600">Shape:</span>
                <span className="font-semibold text-amber-900">{config.shape}</span>

                <span className="text-amber-600">Size:</span>
                <span className="font-semibold text-amber-900">{config.sizeKg} kg</span>

                <span className="text-amber-600">Eggless:</span>
                <span className="font-semibold text-amber-900">{config.isEggless ? '✅ Yes' : '❌ No'}</span>

                {config.message && (
                  <>
                    <span className="text-amber-600">Message:</span>
                    <span className="font-semibold text-amber-900">"{config.message}"</span>
                  </>
                )}

                {config.imagePreview && (
                  <>
                    <span className="text-amber-600">Photo:</span>
                    <span className="font-semibold text-green-600">✅ Uploaded</span>
                  </>
                )}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Navigation buttons */}
      <div className="flex gap-4 mt-6">
        {step > 0 && (
          <button
            onClick={() => setStep(s => s - 1)}
            className="flex-1 flex items-center justify-center gap-2 border-2 border-amber-800 text-amber-800 py-3.5 rounded-2xl font-semibold hover:bg-amber-50 transition-colors">
            <ChevronLeft size={18} /> Back
          </button>
        )}

        {step < STEPS.length - 1 ? (
          <button
            onClick={() => setStep(s => s + 1)}
            disabled={!canNext()}
            className="flex-1 flex items-center justify-center gap-2 bg-amber-800 text-white py-3.5 rounded-2xl font-semibold hover:bg-amber-700 transition-all disabled:opacity-40 disabled:cursor-not-allowed">
            Next <ChevronRight size={18} />
          </button>
        ) : (
          <button
            onClick={handleAddToCart}
            disabled={uploading}
            className="flex-1 flex items-center justify-center gap-2 bg-amber-800 text-white py-3.5 rounded-2xl font-semibold hover:bg-amber-700 transition-all disabled:opacity-50">
            {uploading
              ? <><div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" /> Adding to Cart...</>
              : <><ShoppingCart size={18} /> Add Custom Cake to Cart</>
            }
          </button>
        )}
      </div>
    </div>
  )
}