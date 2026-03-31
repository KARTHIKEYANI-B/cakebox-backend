import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Upload, ChevronRight, ChevronLeft, Check } from 'lucide-react'
import api from '../api/axios'
import { useCart } from '../context/CartContext'
import toast from 'react-hot-toast'

const STEPS = ['Flavor', 'Shape', 'Message', 'Photo & Confirm']
const FLAVORS = ['Chocolate', 'Vanilla', 'Red Velvet', 'Butterscotch', 'Strawberry', 'Pineapple', 'Black Forest', 'White Forest']
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
    flavor: '', shape: '', message: '', sizeKg: '1', isEggless: false,
    specialInstructions: '', imageFile: null, imagePreview: null
  })
  const [uploading, setUploading] = useState(false)

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

  const handleAddToCart = async () => {
    setUploading(true)
    try {
      let imageUrl = null
      if (config.imageFile) {
        const formData = new FormData()
        formData.append('image', config.imageFile)
        const uploadRes = await api.post('/customize/upload-image', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        imageUrl = uploadRes.data.imageUrl
      }

      // Find first product to add (in real app, would create custom product)
      const products = await api.get('/products')
      const firstProduct = products.data[0]

      await addToCart(firstProduct.id, {
        quantity: 1,
        flavor: config.flavor,
        sizeKg: config.sizeKg,
        isEggless: config.isEggless,
        customMessage: config.message
      })

      toast.success('Custom cake added to cart! 🎂')
      navigate('/cart')
    } catch (e) {
      toast.error('Could not add custom cake')
    } finally { setUploading(false) }
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="text-center mb-10">
        <div className="text-5xl mb-3">🎨</div>
        <h1 className="font-display text-3xl font-bold text-amber-900">Design Your Cake</h1>
        <p className="text-orange-400 mt-2">Create the perfect cake for your special moment</p>
      </div>

      {/* Step indicator */}
      <div className="flex items-center gap-2 mb-10">
        {STEPS.map((s, i) => (
          <div key={s} className="flex items-center gap-2 flex-1">
            <div className={`w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold transition-all ${
              i < step ? 'bg-green-500 text-white' :
              i === step ? 'bg-amber-800 text-white' :
              'bg-orange-100 text-amber-600'
            }`}>
              {i < step ? <Check size={16} /> : i + 1}
            </div>
            <span className={`text-xs font-medium hidden sm:block ${i === step ? 'text-amber-900' : 'text-orange-300'}`}>{s}</span>
            {i < STEPS.length - 1 && <div className={`flex-1 h-0.5 ${i < step ? 'bg-green-400' : 'bg-orange-100'}`} />}
          </div>
        ))}
      </div>

      <div className="bg-white rounded-3xl border border-orange-100 p-8 min-h-72">

        {/* STEP 0: FLAVOR */}
        {step === 0 && (
          <div>
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-6">Choose Your Flavor 🍫</h2>
            <div className="grid grid-cols-2 gap-3">
              {FLAVORS.map(f => (
                <button key={f} onClick={() => setConfig(c => ({...c, flavor: f}))}
                  className={`p-4 rounded-2xl border-2 font-medium transition-all text-left ${
                    config.flavor === f ? 'border-amber-800 bg-amber-50 text-amber-900 scale-[1.02]' : 'border-orange-100 text-amber-700 hover:border-orange-300'
                  }`}>
                  {config.flavor === f && <span className="text-green-500 mr-2">✓</span>}
                  {f}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* STEP 1: SHAPE */}
        {step === 1 && (
          <div>
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-6">Choose Shape ⬛</h2>
            <div className="grid grid-cols-2 gap-4">
              {SHAPES.map(s => (
                <button key={s.label} onClick={() => setConfig(c => ({...c, shape: s.label}))}
                  className={`p-6 rounded-2xl border-2 flex flex-col items-center gap-3 transition-all ${
                    config.shape === s.label ? 'border-amber-800 bg-amber-50 scale-[1.02]' : 'border-orange-100 hover:border-orange-300'
                  }`}>
                  <span className="text-4xl">{s.icon}</span>
                  <span className="font-semibold text-amber-900">{s.label}</span>
                </button>
              ))}
            </div>

            {/* Size selector */}
            <div className="mt-6">
              <p className="font-medium text-amber-800 mb-3">Size</p>
              <div className="flex gap-3">
                {SIZES.map(s => (
                  <button key={s} onClick={() => setConfig(c => ({...c, sizeKg: s}))}
                    className={`px-5 py-2.5 rounded-xl border-2 font-medium transition-all ${
                      config.sizeKg === s ? 'border-amber-800 bg-amber-50 text-amber-900' : 'border-orange-100 text-amber-700'
                    }`}>{s} kg</button>
                ))}
              </div>
            </div>

            {/* Eggless */}
            <div className="mt-4 flex items-center justify-between p-4 bg-green-50 rounded-xl">
              <span className="font-medium text-amber-800 text-sm">Make it Eggless 🌱</span>
              <button onClick={() => setConfig(c => ({...c, isEggless: !c.isEggless}))}
                className={`w-12 h-6 rounded-full relative transition-all ${config.isEggless ? 'bg-green-500' : 'bg-gray-200'}`}>
                <span className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-all ${config.isEggless ? 'left-7' : 'left-1'}`} />
              </button>
            </div>
          </div>
        )}

        {/* STEP 2: MESSAGE */}
        {step === 2 && (
          <div>
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-6">Write Your Message 💬</h2>
            <div className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-amber-800 mb-2">Message on Cake (optional)</label>
                <input type="text" maxLength={50} value={config.message}
                  onChange={e => setConfig(c => ({...c, message: e.target.value}))}
                  placeholder="e.g. Happy Birthday Priya! 🎂"
                  className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
                <p className="text-xs text-orange-400 mt-1 text-right">{config.message.length}/50</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-amber-800 mb-2">Special Instructions (optional)</label>
                <textarea rows={4} value={config.specialInstructions}
                  onChange={e => setConfig(c => ({...c, specialInstructions: e.target.value}))}
                  placeholder="Any specific requirements for the baker..."
                  className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm resize-none" />
              </div>
            </div>
          </div>
        )}

        {/* STEP 3: PHOTO + CONFIRM */}
        {step === 3 && (
          <div>
            <h2 className="font-display text-2xl font-bold text-amber-900 mb-6">Upload Reference Photo 📸</h2>

            <div className="mb-6">
              <label className="block text-sm font-medium text-amber-800 mb-3">Reference Image (optional)</label>
              <label className="flex flex-col items-center justify-center gap-3 border-2 border-dashed border-orange-200 rounded-2xl p-8 cursor-pointer hover:border-orange-400 hover:bg-orange-50 transition-all">
                {config.imagePreview ? (
                  <img src={config.imagePreview} className="w-40 h-40 object-cover rounded-2xl" alt="preview" />
                ) : (
                  <>
                    <Upload size={32} className="text-orange-400" />
                    <span className="text-amber-700 font-medium">Click to upload photo</span>
                    <span className="text-xs text-orange-400">JPG, PNG up to 5MB</span>
                  </>
                )}
                <input type="file" accept="image/*" onChange={handleImageChange} className="hidden" />
              </label>
            </div>

            {/* Summary */}
            <div className="bg-orange-50 rounded-2xl p-5 space-y-2 text-sm">
              <h3 className="font-semibold text-amber-900 mb-3">Your Custom Cake Summary</h3>
              <div className="grid grid-cols-2 gap-2 text-amber-700">
                <span>Flavor:</span><span className="font-medium text-amber-900">{config.flavor}</span>
                <span>Shape:</span><span className="font-medium text-amber-900">{config.shape}</span>
                <span>Size:</span><span className="font-medium text-amber-900">{config.sizeKg} kg</span>
                <span>Eggless:</span><span className="font-medium text-amber-900">{config.isEggless ? 'Yes 🌱' : 'No'}</span>
                {config.message && <><span>Message:</span><span className="font-medium text-amber-900">"{config.message}"</span></>}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Navigation */}
      <div className="flex gap-4 mt-6">
        {step > 0 && (
          <button onClick={() => setStep(s => s - 1)}
            className="flex-1 flex items-center justify-center gap-2 border-2 border-amber-800 text-amber-800 py-3.5 rounded-2xl font-semibold hover:bg-amber-50 transition-colors">
            <ChevronLeft size={18} /> Back
          </button>
        )}
        {step < STEPS.length - 1 ? (
          <button onClick={() => setStep(s => s + 1)} disabled={!canNext()}
            className="flex-1 flex items-center justify-center gap-2 bg-amber-800 text-white py-3.5 rounded-2xl font-semibold hover:bg-amber-700 transition-all disabled:opacity-50">
            Next <ChevronRight size={18} />
          </button>
        ) : (
          <button onClick={handleAddToCart} disabled={uploading}
            className="flex-1 flex items-center justify-center gap-2 bg-amber-800 text-white py-3.5 rounded-2xl font-semibold hover:bg-amber-700 transition-all disabled:opacity-50">
            {uploading ? <><div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" /> Adding...</> : '🎂 Add to Cart'}
          </button>
        )}
      </div>
    </div>
  )
}