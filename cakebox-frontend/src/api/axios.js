// =============================================================
// FILE: src/api/axios.js
//
// This is the CENTRAL axios configuration.
// All API calls in React go through this — not plain fetch().
//
// What it does:
//   1. Sets base URL to /api (proxied to localhost:8080 in dev)
//   2. Automatically adds JWT token to every request header
//   3. Handles 401 errors (token expired → redirect to login)
// =============================================================

import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000, // 30 second timeout
})

// REQUEST INTERCEPTOR
// Runs before every API call — attaches JWT token automatically
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('cakebox_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// RESPONSE INTERCEPTOR
// Runs after every API response
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid → clear storage → redirect to login
      localStorage.removeItem('cakebox_token')
      localStorage.removeItem('cakebox_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api