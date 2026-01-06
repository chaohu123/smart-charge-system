<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>充电桩管理</h2>
      </div>
      <el-menu
        v-if="adminStore.adminInfo"
        :default-active="$route.path"
        router
        :background-color="'var(--sidebar-bg)'"
        :text-color="'var(--sidebar-text)'"
        :active-text-color="'var(--sidebar-text-active)'"
        class="sidebar-menu"
      >
        <template v-if="canAccess('dashboard')">
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>数据大屏</span>
          </el-menu-item>
        </template>
        
        <!-- 设备管理 -->
        <el-sub-menu index="device" v-if="canAccessDevice()">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>设备管理</span>
          </template>
          <el-menu-item index="/station" v-if="canAccess('station')">
            <el-icon><Location /></el-icon>
            <span>充电站管理</span>
          </el-menu-item>
          <el-menu-item index="/pile" v-if="canAccess('pile')">
            <el-icon><Setting /></el-icon>
            <span>充电桩管理</span>
          </el-menu-item>
          <el-menu-item index="/monitor" v-if="canAccess('monitor')">
            <el-icon><Monitor /></el-icon>
            <span>设备监控</span>
          </el-menu-item>
        </el-sub-menu>
        
        <template v-if="canAccess('user')">
          <el-menu-item index="/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </template>
        
        <!-- 财务管理 -->
        <el-sub-menu index="finance" v-if="canAccessFinance()">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/finance" v-if="canAccess('finance')">
            <el-icon><Money /></el-icon>
            <span>财务概览</span>
          </el-menu-item>
          <el-menu-item index="/order" v-if="canAccess('order')">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/withdraw" v-if="canAccess('withdraw')">
            <el-icon><Money /></el-icon>
            <span>提现管理</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 运营管理（仅系统管理员） -->
        <el-sub-menu index="operation" v-if="canAccessOperation()">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>运营管理</span>
          </template>
          <el-menu-item index="/operation/activity">
            <el-icon><Document /></el-icon>
            <span>活动管理</span>
          </el-menu-item>
          <el-menu-item index="/operation/coupon">
            <el-icon><Ticket /></el-icon>
            <span>优惠券管理</span>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 运维管理 -->
        <el-sub-menu index="maintenance" v-if="canAccessMaintenance()">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>运维管理</span>
          </template>
          <el-menu-item index="/maintenance" v-if="canAccess('maintenance')">
            <el-icon><Tools /></el-icon>
            <span>运维工单</span>
          </el-menu-item>
          <el-menu-item index="/alert" v-if="canAccess('alert')">
            <el-icon><Warning /></el-icon>
            <span>故障报警</span>
          </el-menu-item>
        </el-sub-menu>
        <template v-if="canAccess('analysis')">
          <el-menu-item index="/analysis">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据分析</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" />
              <span>{{ adminStore.adminInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <div v-if="loading" style="display: flex; justify-content: center; align-items: center; height: 100%;">
          <el-icon class="is-loading" style="font-size: 24px;"><Loading /></el-icon>
        </div>
        <router-view v-else />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import {
  DataBoard,
  Setting,
  Location,
  Monitor,
  User,
  Money,
  Document,
  Tools,
  Warning,
  DataAnalysis,
  Loading,
  ArrowDown,
  Ticket
} from '@element-plus/icons-vue'

const router = useRouter()
const adminStore = useAdminStore()
const loading = ref(false)

// 组件挂载时，如果有token但没有adminInfo，获取管理员信息
onMounted(async () => {
  if (adminStore.token && !adminStore.adminInfo) {
    loading.value = true
    try {
      await adminStore.getAdminInfoAction()
    } catch (error) {
      console.error('获取管理员信息失败:', error)
    } finally {
      loading.value = false
    }
  }
})

const handleCommand = (command) => {
  if (command === 'logout') {
    adminStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  }
}

// 根据角色判断是否有权限访问
const canAccess = (module) => {
  const role = adminStore.adminInfo?.role
  if (role === 0 || role === '0') {
    // 系统管理员：所有权限
    return true
  } else if (role === 1 || role === '1') {
    // 充电站管理员：只能访问充电站管理、充电桩管理、设备监控
    return ['station', 'pile', 'monitor'].includes(module)
  } else if (role === 2 || role === '2') {
    // 运维人员：只能访问故障报警、运维工单、设备监控
    return ['alert', 'maintenance', 'monitor'].includes(module)
  }
  return false
}

// 判断是否有设备管理权限（至少有一个子菜单项有权限）
const canAccessDevice = () => {
  return canAccess('station') || canAccess('pile') || canAccess('monitor')
}

// 判断是否有财务管理权限
const canAccessFinance = () => {
  const role = adminStore.adminInfo?.role
  if (role === 0 || role === '0') {
    return true // 系统管理员可以访问财务管理
  }
  return false
}

// 判断是否有运维管理权限（至少有一个子菜单项有权限）
const canAccessMaintenance = () => {
  return canAccess('maintenance') || canAccess('alert')
}

// 运营管理仅系统管理员
const canAccessOperation = () => {
  const role = adminStore.adminInfo?.role
  return role === 0 || role === '0'
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background-color: var(--sidebar-bg);
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 100;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, var(--sidebar-hover), var(--sidebar-bg));
  font-size: 18px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
}

.logo::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

.logo h2 {
  margin: 0;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.sidebar-menu {
  border-right: none;
  background-color: transparent;
  padding: var(--spacing-sm) 0;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
  transition: all 0.3s ease;
  margin: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius);
  position: relative;
  overflow: hidden;
}

.sidebar-menu :deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--sidebar-active);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: var(--sidebar-active) !important;
  color: var(--sidebar-text-active) !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.sidebar-menu :deep(.el-menu-item.is-active::before) {
  transform: scaleY(1);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: var(--sidebar-hover) !important;
  transform: translateX(4px);
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: var(--spacing-sm);
}

.sidebar-menu :deep(.el-sub-menu) {
  margin: var(--spacing-xs) var(--spacing-sm);
}

.sidebar-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
  transition: all 0.3s ease;
  border-radius: var(--border-radius);
  position: relative;
  overflow: hidden;
}

.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: var(--sidebar-hover) !important;
}

.sidebar-menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  background-color: var(--sidebar-hover) !important;
}

.sidebar-menu :deep(.el-sub-menu .el-icon) {
  font-size: 18px;
  margin-right: var(--spacing-sm);
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 50px !important;
  height: 45px;
  line-height: 45px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--header-bg);
  border-bottom: 1px solid var(--header-border);
  padding: 0 var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 99;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  color: var(--header-text);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  transition: all 0.3s ease;
  position: relative;
}

.user-info:hover {
  background-color: var(--content-bg);
  box-shadow: var(--shadow-sm);
}

.user-info .el-avatar {
  border: 2px solid var(--admin-primary);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-info span {
  font-weight: 500;
  font-size: var(--font-size-sm);
}

.main-content {
  background-color: var(--content-bg);
  padding: var(--spacing-lg);
  overflow-y: auto;
  height: calc(100vh - 60px);
}

.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: var(--content-bg);
}

.main-content::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

@media (max-width: 768px) {
  .sidebar {
    width: 64px !important;
  }
  
  .logo h2 {
    display: none;
  }
  
  .sidebar-menu :deep(.el-menu-item span) {
    display: none;
  }
  
  .header {
    padding: 0 var(--spacing-md);
  }
  
  .user-info span {
    display: none;
  }
}
</style>

