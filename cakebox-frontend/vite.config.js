import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// NOTE: Using Tailwind v3 via PostCSS (not the v4 Vite plugin)
// PostCSS config is in postcss.config.js (auto-created by npx tailwindcss init -p)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})