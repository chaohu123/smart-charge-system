import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const loginAction = async (phone, password) => {
    const res = await login(phone, password)
    if (res.code === 200) {
      setToken(res.data.token)
      setUserInfo(res.data.user)
      return true
    }
    return false
  }

  const getUserInfoAction = async () => {
    if (!token.value) return
    const res = await getUserInfo()
    if (res.code === 200) {
      setUserInfo(res.data)
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    loginAction,
    getUserInfoAction,
    logout
  }
})

