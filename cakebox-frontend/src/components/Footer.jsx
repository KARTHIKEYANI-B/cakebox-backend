import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="bg-amber-900 text-amber-100 mt-16">
      <div className="max-w-7xl mx-auto px-4 py-12 grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <div className="flex items-center gap-2 mb-4">
            <span className="text-2xl">🎂</span>
            <span className="font-display text-xl font-bold text-white">CAKEBOX</span>
          </div>
          <p className="text-sm text-amber-300 leading-relaxed">
            Delicious moments, delivered fast. Fresh cakes baked daily in Karur.
          </p>
        </div>

        <div>
          <h4 className="font-semibold text-white mb-4">Quick Links</h4>
          <ul className="space-y-2 text-sm text-amber-300">
            <li><Link to="/" className="hover:text-white transition-colors">Home</Link></li>
            <li><Link to="/customize" className="hover:text-white transition-colors">Customize Cake</Link></li>
            <li><Link to="/cart" className="hover:text-white transition-colors">My Cart</Link></li>
          </ul>
        </div>

        <div>
          <h4 className="font-semibold text-white mb-4">Occasions</h4>
          <ul className="space-y-2 text-sm text-amber-300">
            {['Birthday', 'Anniversary', 'Wedding', 'Festival'].map(o => (
              <li key={o}><Link to={`/?occasion=${o.toUpperCase()}`} className="hover:text-white transition-colors">{o}</Link></li>
            ))}
          </ul>
        </div>

        <div>
          <h4 className="font-semibold text-white mb-4">Contact</h4>
          <ul className="space-y-2 text-sm text-amber-300">
            <li>📍 Karur, Tamil Nadu</li>
            <li>📞 +91 98765 43210</li>
            <li>✉️ hello@cakebox.com</li>
            <li className="pt-2">
              <span className="inline-block bg-green-600 text-white text-xs px-3 py-1 rounded-full">
                ✓ Fresh Guarantee
              </span>
            </li>
          </ul>
        </div>
      </div>
      <div className="border-t border-amber-800 py-4 text-center text-xs text-amber-400">
        © 2024 CakeBox · Made with ❤️ in Karur · "Delicious moments, delivered fast 🍰"
      </div>
    </footer>
  )
}