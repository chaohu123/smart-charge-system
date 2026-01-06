<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="48"><Setting /></el-icon>
          <h1>充电桩管理系统</h1>
        </div>
        <p class="subtitle">智能充电桩运营管理平台</p>
      </div>

      <el-card class="login-card" shadow="hover">
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
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
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'

const router = useRouter()
const adminStore = useAdminStore()

const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
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
        const success = await adminStore.loginAction(loginForm.username, loginForm.password)
        if (success) {
          ElMessage.success('登录成功')
          // 根据角色决定默认重定向路径
          const role = adminStore.adminInfo?.role
          let defaultPath = '/dashboard'
          if (role === 1 || role === '1') {
            // 充电站管理员：重定向到充电站管理
            defaultPath = '/station'
          } else if (role === 2 || role === '2') {
            // 运维人员：重定向到设备监控
            defaultPath = '/monitor'
          }
          const redirect = router.currentRoute.value.query.redirect || defaultPath
          router.replace(redirect)
        } else {
          ElMessage.error('用户名或密码错误')
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
  background: linear-gradient(135deg, var(--admin-primary) 0%, var(--admin-primary-dark) 100%);
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

.logo .el-icon {
  color: var(--accent-color);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.login-header h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.login-card {
  background: var(--card-bg);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.login-card :deep(.el-card__body) {
  padding: var(--spacing-xl);
}

:deep(.el-card) {
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  background-color: var(--card-bg);
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--border-color) inset;
  border-radius: var(--border-radius);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--admin-primary) inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--admin-primary) inset;
}

:deep(.el-button--primary) {
  background-color: var(--admin-primary);
  border-color: var(--admin-primary);
  border-radius: var(--border-radius);
}

:deep(.el-button--primary:hover) {
  background-color: var(--admin-primary-dark);
  border-color: var(--admin-primary-dark);
}
</style>
