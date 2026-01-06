<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="48" class="logo-icon"><Lightning /></el-icon>
          <h1>智能充电桩</h1>
        </div>
        <p class="subtitle">让充电更简单，让出行更绿色</p>
      </div>

      <el-card class="login-card" shadow="hover">
        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="手机登录" name="phone">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0">
              <el-form-item prop="phone">
                <el-input
                  v-model="loginForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  prefix-icon="Phone"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  prefix-icon="Lock"
                  show-password
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  style="width: 100%"
                  :loading="loading"
                  @click="handleLogin"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>
            <div class="login-footer">
              <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
              <el-link type="info">忘记密码？</el-link>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Lightning } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('phone')
const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = ref({
  phone: '',
  password: ''
})

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

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm.value.phone, loginForm.value.password)
        if (res.code === 200) {
          userStore.setToken(res.data.token)
          userStore.setUserInfo(res.data.user)
          ElMessage.success('登录成功')
          const redirect = router.currentRoute.value.query.redirect || '/home'
          router.replace(redirect)
        } else {
          ElMessage.error(res.message || '登录失败')
        }
      } catch (error) {
        ElMessage.error('登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  color: white;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.logo-icon {
  color: var(--accent-color);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.login-header h1 {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  font-size: var(--font-size-sm);
  opacity: 0.9;
  margin: 0;
}

.login-card {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.login-tabs {
  padding: var(--spacing-lg);
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--spacing-lg);
}

.login-tabs :deep(.el-tabs__item) {
  font-size: var(--font-size-md);
  font-weight: 500;
}

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: var(--spacing-md);
  font-size: var(--font-size-sm);
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--divider-color) inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}
</style>
