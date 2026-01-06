<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <div class="logo">
          <el-icon :size="48" class="logo-icon"><Lightning /></el-icon>
          <h1>智能充电桩</h1>
        </div>
        <p class="subtitle">注册账号，开启绿色出行</p>
      </div>

      <el-card class="register-card" shadow="hover">
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="0">
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              size="large"
              prefix-icon="Phone"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleRegister"
            />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="请输入昵称（可选）"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        <div class="register-footer">
          <el-link type="primary" @click="$router.push('/login')">已有账号？立即登录</el-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Lightning } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const registerFormRef = ref(null)

const registerForm = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await register(registerForm)
        if (res.code === 200) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        }
      } catch (error) {
        ElMessage.error('注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
}

.register-container {
  width: 100%;
  max-width: 420px;
}

.register-header {
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

.register-header h1 {
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

.register-card {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.register-card :deep(.el-card__body) {
  padding: var(--spacing-xl);
}

.register-footer {
  text-align: center;
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
