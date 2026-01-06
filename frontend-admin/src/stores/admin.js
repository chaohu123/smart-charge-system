import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getAdminInfo } from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref(null)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('admin_token', newToken)
  }

  const setAdminInfo = (info) => {
    adminInfo.value = info
  }

  const loginAction = async (username, password) => {
    const res = await login(username, password)
    if (res.code === 200) {
      setToken(res.data.token)
      setAdminInfo(res.data.admin)
      return true
    }
    return false
  }

  const getAdminInfoAction = async () => {
    if (!token.value) return
    const res = await getAdminInfo()
    if (res.code === 200) {
      setAdminInfo(res.data)
    }
  }

  const logout = () => {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
  }

  return {
    token,
    adminInfo,
    setToken,
    setAdminInfo,
    loginAction,
    getAdminInfoAction,
    logout
  }
})

