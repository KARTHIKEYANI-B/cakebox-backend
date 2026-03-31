import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { MapPin, Clock, CreditCard, Gift, ChevronRight } from 'lucide-react'
import api from '../api/axios'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

const SLOTS = ['10:00 AM - 12:00 PM', '12:00 PM - 02:00 PM', '04:00 PM - 06:00 PM', '06:00 PM - 08:00 PM']
const STEPS = ['Address', 'Delivery', 'Payment']

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { cart, clearCart } = useCart()
  const { user } = useAuth()
  const [step, setStep] = useState(0)
  const [loading, setLoading] = useState(false)
  const [addresses, setAddresses] = useState([])
  const [selectedAddress, setSelectedAddress] = useState(null)
  const [showAddressForm, setShowAddressForm] = useState(false)
  const [placedOrderId, setPlacedOrderId] = useState(null)

  // Address form
  const [form, setForm] = useState({ fullName: user?.name || '', phoneNumber: '', addressLine1: '', city: 'Karur', state: 'Tamil Nadu', pincode: '', isDefault: true })

  // Delivery options
  const [deliveryDate, setDeliveryDate] = useState('')
  const [deliverySlot, setDeliverySlot] = useState(SLOTS[0])
  const [isExpress, setIsExpress] = useState(false)
  const [isSurprise, setIsSurprise] = useState(false)
  const [surpriseMsg, setSurpriseMsg] = useState('')
  const [doNotCall, setDoNotCall] = useState(false)

  useEffect(() => {
    api.get('/orders/addresses').then(r => {
      setAddresses(r.data)
      if (r.data.length > 0) {
        setSelectedAddress(r.data.find(a => a.isDefault)?.addressId || r.data[0].addressId)
        setShowAddressForm(false)
      } else {
        setShowAddressForm(true)
      }
    }).catch(() => setShowAddressForm(true))

    // Set tomorrow as default delivery date
    const tomorrow = new Date(); tomorrow.setDate(tomorrow.getDate() + 1)
    setDeliveryDate(tomorrow.toISOString().split('T')[0])
  }, [])

  const saveAddress = async () => {
    if (!form.fullName || !form.phoneNumber || !form.addressLine1 || !form.pincode) {
      toast.error('Please fill all required fields'); return
    }
    try {
      const res = await api.post('/orders/addresses', form)
      setAddresses(prev => [...prev, res.data])
      setSelectedAddress(res.data.addressId)
      setShowAddressForm(false)
      toast.success('Address saved!')
    } catch { toast.error('Could not save address') }
  }

  const placeOrder = async () => {
    if (!selectedAddress) { toast.error('Please select a delivery address'); return }
    setLoading(true)
    try {
      const res = await api.post('/orders/place', {
        addressId: selectedAddress,
        deliveryDate, deliverySlot,
        isExpressDelivery: isExpress,
        isSurpriseMode: isSurprise,
        surpriseMessage: surpriseMsg,
        doNotCallRecipient: doNotCall
      })
      setPlacedOrderId(res.data.orderId)
      setStep(2) // Move to payment step
    } catch (e) {
      toast.error(e.response?.data?.error || 'Could not place order')
    } finally { setLoading(false) }
  }

  const handlePayment = async () => {
    if (!placedOrderId) return
    setLoading(true)
    try {
      // Step 1: Create Razorpay order
      const orderRes = await api.post('/payment/create-order', { orderId: placedOrderId })
      const { razorpayOrderId, amount, keyId, customerName, customerEmail, customerPhone } = orderRes.data

      // Step 2: Open Razorpay popup
      const options = {
        key: keyId,
        amount: amount,
        currency: 'INR',
        name: 'CakeBox 🎂',
        description: 'Fresh Cake Order',
        order_id: razorpayOrderId,
        prefill: { name: customerName, email: customerEmail, contact: customerPhone },
        theme: { color: '#78350f' },
        handler: async function(response) {
          // Step 3: Verify payment
          try {
            await api.post('/payment/verify', {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature
            })
            clearCart()
            toast.success('Payment successful! 🎂')
            navigate(`/orders/${placedOrderId}`)
          } catch {
            toast.error('Payment verification failed. Please contact support.')
          }
        },
        modal: { ondismiss: () => toast('Payment cancelled') }
      }

      // Load Razorpay script if not loaded
      if (!window.Razorpay) {
        await new Promise((resolve, reject) => {
          const script = document.createElement('script')
          script.src = 'https://checkout.razorpay.com/v1/checkout.js'
          script.onload = resolve
          script.onerror = reject
          document.body.appendChild(script)
        })
      }

      const rzp = new window.Razorpay(options)
      rzp.open()
    } catch (e) {
      toast.error('Could not initialize payment')
    } finally { setLoading(false) }
  }

  if (!cart || cart.itemCount === 0) {
    navigate('/cart'); return null
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="font-display text-3xl font-bold text-amber-900 mb-8">Checkout</h1>

      {/* STEP INDICATOR */}
      <div className="flex items-center gap-2 mb-10">
        {STEPS.map((s, i) => (
          <div key={s} className="flex items-center gap-2 flex-1">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all ${
              i < step ? 'bg-green-500 text-white' :
              i === step ? 'bg-amber-800 text-white' :
              'bg-orange-100 text-amber-600'
            }`}>{i < step ? '✓' : i + 1}</div>
            <span className={`text-sm font-medium hidden sm:block ${i === step ? 'text-amber-900' : 'text-orange-400'}`}>{s}</span>
            {i < STEPS.length - 1 && <div className={`flex-1 h-0.5 ${i < step ? 'bg-green-400' : 'bg-orange-100'}`} />}
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">

          {/* ── STEP 0: ADDRESS ── */}
          {step === 0 && (
            <div className="bg-white rounded-2xl border border-orange-100 p-6">
              <div className="flex items-center justify-between mb-5">
                <h2 className="font-semibold text-amber-900 text-lg flex items-center gap-2"><MapPin size={18} /> Delivery Address</h2>
                {addresses.length > 0 && (
                  <button onClick={() => setShowAddressForm(!showAddressForm)}
                    className="text-sm text-orange-500 hover:text-orange-600 font-medium">
                    {showAddressForm ? 'Use saved' : '+ Add new'}
                  </button>
                )}
              </div>

              {/* Saved addresses */}
              {!showAddressForm && addresses.map(a => (
                <label key={a.addressId}
                  className={`flex gap-3 p-4 rounded-xl border-2 mb-3 cursor-pointer transition-all ${
                    selectedAddress === a.addressId ? 'border-amber-800 bg-amber-50' : 'border-orange-100'
                  }`}>
                  <input type="radio" name="addr" checked={selectedAddress === a.addressId}
                    onChange={() => setSelectedAddress(a.addressId)} className="mt-1" />
                  <div>
                    <p className="font-semibold text-amber-900">{a.fullName}</p>
                    <p className="text-sm text-amber-700">{a.addressLine1}, {a.city}, {a.state} - {a.pincode}</p>
                    <p className="text-sm text-amber-600">{a.phoneNumber}</p>
                    {a.isDefault && <span className="text-xs text-green-600 font-medium">Default</span>}
                  </div>
                </label>
              ))}

              {/* New address form */}
              {showAddressForm && (
                <div className="space-y-4">
                  {[
                    { key: 'fullName', label: 'Full Name *', placeholder: 'Ravi Kumar' },
                    { key: 'phoneNumber', label: 'Phone Number *', placeholder: '9876543210' },
                    { key: 'addressLine1', label: 'Address Line 1 *', placeholder: 'Street, Area' },
                    { key: 'city', label: 'City *', placeholder: 'Karur' },
                    { key: 'state', label: 'State *', placeholder: 'Tamil Nadu' },
                    { key: 'pincode', label: 'Pincode *', placeholder: '639001' },
                  ].map(f => (
                    <div key={f.key}>
                      <label className="block text-sm font-medium text-amber-800 mb-1">{f.label}</label>
                      <input value={form[f.key]} onChange={e => setForm({...form, [f.key]: e.target.value})}
                        placeholder={f.placeholder}
                        className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
                    </div>
                  ))}
                  <button onClick={saveAddress}
                    className="w-full bg-amber-800 text-white py-3 rounded-xl font-semibold hover:bg-amber-700 transition-colors">
                    Save Address
                  </button>
                </div>
              )}

              <button onClick={() => setStep(1)} disabled={!selectedAddress}
                className="w-full mt-5 bg-amber-800 text-white py-4 rounded-2xl font-bold text-lg hover:bg-amber-700 transition-all disabled:opacity-50 flex items-center justify-center gap-2">
                Continue to Delivery <ChevronRight size={20} />
              </button>
            </div>
          )}

          {/* ── STEP 1: DELIVERY ── */}
          {step === 1 && (
            <div className="bg-white rounded-2xl border border-orange-100 p-6">
              <h2 className="font-semibold text-amber-900 text-lg flex items-center gap-2 mb-5">
                <Clock size={18} /> Delivery Options
              </h2>

              <div className="space-y-5">
                {/* Date */}
                <div>
                  <label className="block text-sm font-medium text-amber-800 mb-2">Delivery Date</label>
                  <input type="date" value={deliveryDate}
                    min={new Date().toISOString().split('T')[0]}
                    onChange={e => setDeliveryDate(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-orange-200 focus:outline-none focus:border-orange-400 text-sm" />
                </div>

                {/* Time slot */}
                <div>
                  <label className="block text-sm font-medium text-amber-800 mb-2">Time Slot</label>
                  <div className="grid grid-cols-2 gap-3">
                    {SLOTS.map(s => (
                      <button key={s} onClick={() => setDeliverySlot(s)}
                        className={`py-3 px-4 rounded-xl border-2 text-sm font-medium transition-all ${
                          deliverySlot === s ? 'border-amber-800 bg-amber-50 text-amber-900' : 'border-orange-100 text-amber-700'
                        }`}>{s}</button>
                    ))}
                  </div>
                </div>

                {/* Express */}
                <div className={`flex items-center justify-between p-4 rounded-xl border-2 cursor-pointer transition-all ${isExpress ? 'border-amber-800 bg-amber-50' : 'border-orange-100'}`}
                  onClick={() => setIsExpress(!isExpress)}>
                  <div className="flex items-center gap-3">
                    <span className="text-2xl">⚡</span>
                    <div>
                      <p className="font-semibold text-amber-900 text-sm">Express Delivery (2 hrs)</p>
                      <p className="text-xs text-orange-400">Extra ₹99 · Get it super fast!</p>
                    </div>
                  </div>
                  <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center ${isExpress ? 'bg-amber-800 border-amber-800' : 'border-orange-300'}`}>
                    {isExpress && <div className="w-2 h-2 bg-white rounded-full" />}
                  </div>
                </div>

                {/* Surprise Mode */}
                <div className="p-4 rounded-xl border border-orange-100 bg-rose-50">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <Gift size={18} className="text-rose-500" />
                      <span className="font-semibold text-amber-900 text-sm">Surprise Mode 🎁</span>
                    </div>
                    <button onClick={() => setIsSurprise(!isSurprise)}
                      className={`w-10 h-6 rounded-full relative transition-all ${isSurprise ? 'bg-rose-500' : 'bg-gray-200'}`}>
                      <span className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-all ${isSurprise ? 'left-5' : 'left-1'}`} />
                    </button>
                  </div>
                  {isSurprise && (
                    <div className="space-y-3">
                      <input placeholder="Secret message for recipient..."
                        value={surpriseMsg} onChange={e => setSurpriseMsg(e.target.value)}
                        className="w-full px-3 py-2.5 rounded-lg border border-rose-200 bg-white text-sm focus:outline-none" />
                      <label className="flex items-center gap-2 text-sm text-amber-700 cursor-pointer">
                        <input type="checkbox" checked={doNotCall} onChange={e => setDoNotCall(e.target.checked)} />
                        Don't call recipient before delivery
                      </label>
                    </div>
                  )}
                </div>
              </div>

              <div className="flex gap-3 mt-6">
                <button onClick={() => setStep(0)}
                  className="flex-1 border-2 border-amber-800 text-amber-800 py-3.5 rounded-2xl font-semibold hover:bg-amber-50 transition-colors">
                  Back
                </button>
                <button onClick={placeOrder} disabled={loading}
                  className="flex-1 bg-amber-800 text-white py-3.5 rounded-2xl font-bold hover:bg-amber-700 transition-all disabled:opacity-50 flex items-center justify-center gap-2">
                  {loading ? <><div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" /> Placing...</>
                  : <>Confirm Order <ChevronRight size={18} /></>}
                </button>
              </div>
            </div>
          )}

          {/* ── STEP 2: PAYMENT ── */}
          {step === 2 && (
            <div className="bg-white rounded-2xl border border-orange-100 p-6 text-center">
              <div className="text-5xl mb-4">🎉</div>
              <h2 className="font-display text-2xl font-bold text-amber-900 mb-2">Order Confirmed!</h2>
              <p className="text-amber-600 mb-2">Order #{placedOrderId} placed successfully</p>
              <p className="text-orange-500 font-medium mb-8">Complete payment to start baking 🍰</p>

              <div className="bg-orange-50 rounded-2xl p-5 mb-6 text-left space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-amber-700">Items</span>
                  <span className="font-medium text-amber-900">₹{cart.subtotal?.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-amber-700">Delivery{isExpress ? ' (Express)' : ''}</span>
                  <span className="font-medium text-amber-900">₹{cart.deliveryCharge + (isExpress ? 99 : 0)}</span>
                </div>
                <div className="flex justify-between font-bold text-amber-900 text-lg border-t border-orange-200 pt-2">
                  <span>Total</span>
                  <span>₹{cart.total?.toFixed(2)}</span>
                </div>
              </div>

              {/* Payment methods */}
              <div className="flex gap-3 justify-center mb-6 text-sm text-amber-700">
                <span className="bg-blue-50 px-3 py-1.5 rounded-lg">📱 UPI</span>
                <span className="bg-green-50 px-3 py-1.5 rounded-lg">💳 Card</span>
                <span className="bg-orange-50 px-3 py-1.5 rounded-lg">🏦 Net Banking</span>
                <span className="bg-gray-50 px-3 py-1.5 rounded-lg">💵 COD</span>
              </div>

              <button onClick={handlePayment} disabled={loading}
                className="w-full bg-amber-800 hover:bg-amber-700 text-white py-4 rounded-2xl font-bold text-xl transition-all hover:scale-[1.02] disabled:opacity-50 flex items-center justify-center gap-3">
                <CreditCard size={22} />
                {loading ? 'Loading...' : `Pay ₹${cart.total?.toFixed(2)}`}
              </button>

              <div className="flex items-center justify-center gap-4 mt-4 text-xs text-green-600">
                <span>✓ 100% Secure Payment</span>
                <span>✓ Powered by Razorpay</span>
              </div>
            </div>
          )}
        </div>

        {/* ORDER SUMMARY SIDEBAR */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-2xl border border-orange-100 p-5 sticky top-24">
            <h3 className="font-semibold text-amber-900 mb-4">Order Summary</h3>
            <div className="space-y-3 max-h-52 overflow-y-auto">
              {cart.items?.map(item => (
                <div key={item.cartItemId} className="flex gap-3 items-center">
                  <img src={item.productImage || 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=100'}
                    className="w-12 h-12 rounded-lg object-cover flex-shrink-0" alt="" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-amber-900 truncate">{item.productName}</p>
                    <p className="text-xs text-orange-400">Qty {item.quantity} × {item.sizeKg}kg</p>
                  </div>
                  <span className="text-sm font-semibold text-amber-900 shrink-0">₹{item.itemTotal?.toFixed(0)}</span>
                </div>
              ))}
            </div>
            <div className="border-t border-orange-100 mt-4 pt-4 flex justify-between font-bold text-amber-900">
              <span>Total</span>
              <span>₹{cart.total?.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}