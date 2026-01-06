<template>
  <header class="app-header">
    <div class="header-container">
      <div class="header-left">
        <div class="logo-section">
          <div class="logo-icon">⚡</div>
          <div class="logo-text">
            <h1 class="system-name">智能充电桩</h1>
            <p class="system-subtitle">公共区域共享充电桩查询与预约管理系统</p>
          </div>
        </div>
      </div>
      
      <div class="header-center">
        <nav class="nav-menu-container">
          <el-menu mode="horizontal" :default-active="activeNav" class="nav-menu" router>
            <el-menu-item index="/home" @click="goHome">首页</el-menu-item>
            <el-menu-item index="/map" @click="goMap">地图</el-menu-item>
            <el-menu-item index="/orders" @click="goOrders">订单</el-menu-item>
            <el-menu-item index="/notifications" @click="goMessages">
              消息
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="nav-badge" />
            </el-menu-item>
          </el-menu>
        </nav>
        <el-select
          v-model="selectedCity"
          placeholder="选择城市"
          size="large"
          class="city-selector"
          @change="handleCityChange"
        >
          <el-option
            v-for="city in cities"
            :key="city.value"
            :label="city.label"
            :value="city.value"
          />
        </el-select>
      </div>
      
      <div class="header-right">
        <el-button
          text
          class="header-btn"
          @click="$router.push('/orders')"
        >
          <el-icon><Document /></el-icon>
          <span>我的订单</span>
        </el-button>
        <el-button
          text
          class="header-btn"
          @click="$router.push('/wallet')"
        >
          <el-icon><CreditCard /></el-icon>
          <span>钱包</span>
        </el-button>
        <el-button
          v-if="userStore.token"
          text
          class="header-btn"
          @click="$router.push('/favorites')"
        >
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </el-button>
        <div v-if="userStore.token" class="nav-right">
          <el-dropdown trigger="hover">
            <div class="user-section">
              <el-avatar :src="userStore.userInfo?.avatar" :size="36">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.phone }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div v-else class="auth-buttons">
          <el-button type="primary" plain @click="showLoginDialog = true">登录</el-button>
          <el-button type="primary" @click="$router.push('/register')">注册</el-button>
        </div>
      </div>
    </div>
    
    <!-- 登录弹窗 -->
    <el-dialog
      v-model="showLoginDialog"
      title="登录"
      width="400px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0">
        <el-form-item prop="phone">
          <el-input
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loginLoading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="login-dialog-footer">
          <span>还没有账号？</span>
          <el-button type="primary" link @click="goRegister">立即注册</el-button>
        </div>
      </template>
    </el-dialog>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { getUnreadCount } from '@/api/notification'
import { ElMessage } from 'element-plus'
import { Document, CreditCard, User, ArrowDown, Star } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedCity = ref('beijing')
const showLoginDialog = ref(false)
const loginLoading = ref(false)
const loginFormRef = ref(null)
const unreadCount = ref(0)
const loginForm = ref({
  phone: '',
  password: ''
})

const activeNav = computed(() => route.path)

const loginRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const cities = [
  { label: '北京', value: 'beijing', longitude: 116.397428, latitude: 39.90923 },
  { label: '上海', value: 'shanghai', longitude: 121.473701, latitude: 31.230416 },
  { label: '广州', value: 'guangzhou', longitude: 113.264385, latitude: 23.129112 },
  { label: '深圳', value: 'shenzhen', longitude: 114.057868, latitude: 22.543099 },
  { label: '杭州', value: 'hangzhou', longitude: 120.153576, latitude: 30.287459 },
  { label: '成都', value: 'chengdu', longitude: 104.066541, latitude: 30.572269 },
  { label: '大连', value: 'dalian', longitude: 121.614682, latitude: 38.914003 }
]

const handleCityChange = (cityValue) => {
  // 找到选中的城市信息
  const city = cities.find(c => c.value === cityValue)
  if (city) {
    // 触发城市切换事件，传递城市信息和坐标
    emit('city-change', {
      value: city.value,
      label: city.label,
      longitude: city.longitude,
      latitude: city.latitude
    })
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loginLoading.value = true
    try {
      const res = await login(loginForm.value.phone, loginForm.value.password)
      if (res.code === 200) {
        userStore.setToken(res.data.token)
        userStore.setUserInfo(res.data.user)
        ElMessage.success('登录成功')
        showLoginDialog.value = false
        await loadUnreadCount()
        emit('login-success')
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (e) {
      ElMessage.error('登录失败')
    } finally {
      loginLoading.value = false
    }
  })
}

const goRegister = () => {
  showLoginDialog.value = false
  router.push('/register')
}

const goProfile = () => {
  router.push('/profile')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
}

// 导航跳转
const goHome = () => router.push('/home')
const goMap = () => router.push('/map')
const goOrders = () => router.push('/orders')
const goMessages = () => router.push('/notifications')

// 加载未读消息数量
const loadUnreadCount = async () => {
  if (!userStore.token) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

onMounted(() => {
  loadUnreadCount()
})

const emit = defineEmits(['city-change', 'login-success'])
</script>

<style scoped>
.app-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid #f0f0f0;
}

.header-container {
  max-width: 1920px;
  margin: 0 auto;
  padding: 0 32px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  flex: 0 0 auto;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1a6dff, #00b894);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(26, 109, 255, 0.3);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.system-name {
  font-size: 20px;
  font-weight: 600;
  color: #212121;
  margin: 0;
  line-height: 1.2;
}

.system-subtitle {
  font-size: 12px;
  color: #757575;
  margin: 2px 0 0;
  line-height: 1.2;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  padding: 0 32px;
}

.nav-menu-container {
  display: flex;
  align-items: center;
}

.nav-menu {
  background: transparent;
  border: none;
}

.nav-menu :deep(.el-menu-item) {
  color: #212121;
  font-size: 15px;
  font-weight: 500;
  padding: 0 20px;
  height: 72px;
  line-height: 72px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.nav-menu :deep(.el-menu-item:hover) {
  color: #1a6dff;
  background: transparent;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #1a6dff;
  border-bottom-color: #1a6dff;
  background: transparent;
}

.nav-badge {
  margin-left: 4px;
}

.nav-badge :deep(.el-badge__content.is-fixed) {
  top: 8px;
  right: 8px;
}

.city-selector {
  width: 200px;
}

.header-right {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #212121;
  font-size: 14px;
}

.header-btn:hover {
  color: #1a6dff;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.user-section:hover {
  background: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #212121;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  font-size: 12px;
  color: #757575;
  transition: transform 0.3s;
}

.user-section:hover .arrow-icon {
  transform: rotate(180deg);
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  z-index: 1001;
}

.auth-buttons {
  display: flex;
  gap: 8px;
}

.login-dialog-footer {
  text-align: center;
  padding-top: 16px;
  color: #757575;
  font-size: 14px;
}

@media (max-width: 1200px) {
  .system-subtitle {
    display: none;
  }
  
  .header-container {
    padding: 0 16px;
  }
  
  .nav-menu :deep(.el-menu-item) {
    padding: 0 12px;
    font-size: 14px;
  }
  
  .city-selector {
    width: 160px;
  }
}

@media (max-width: 768px) {
  .header-container {
    height: auto;
    padding: 12px 16px;
    flex-wrap: wrap;
  }
  
  .header-center {
    order: 3;
    width: 100%;
    padding: 12px 0 0;
    justify-content: flex-start;
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .nav-menu-container {
    width: 100%;
  }
  
  .nav-menu {
    display: flex;
    justify-content: space-around;
  }
  
  .nav-menu :deep(.el-menu-item) {
    padding: 0 8px;
    font-size: 13px;
    height: 48px;
    line-height: 48px;
    flex: 1;
    text-align: center;
  }
  
  .city-selector {
    width: 100%;
  }
  
  .system-subtitle {
    display: none;
  }
  
  .header-right {
    order: 2;
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
  }
}
</style>

