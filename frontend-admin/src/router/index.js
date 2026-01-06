import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'AdminLogin',
    component: () => import('@/views/Login.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据大屏' }
      },
      {
        path: 'station',
        name: 'StationList',
        component: () => import('@/views/station/StationList.vue'),
        meta: { title: '充电站管理' }
      },
      {
        path: 'station/add',
        name: 'StationAdd',
        component: () => import('@/views/station/StationForm.vue'),
        meta: { title: '新增充电站' }
      },
      {
        path: 'station/edit/:id',
        name: 'StationEdit',
        component: () => import('@/views/station/StationForm.vue'),
        meta: { title: '编辑充电站' }
      },
      {
        path: 'pile',
        name: 'PileList',
        component: () => import('@/views/pile/PileList.vue'),
        meta: { title: '充电桩管理' }
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'finance',
        name: 'Finance',
        component: () => import('@/views/finance/Finance.vue'),
        meta: { title: '财务管理' }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('@/views/monitor/Monitor.vue'),
        meta: { title: '设备监控' }
      },
      {
        path: 'maintenance',
        name: 'Maintenance',
        component: () => import('@/views/maintenance/Maintenance.vue'),
        meta: { title: '运维工单' }
      },
      {
        path: 'alert',
        name: 'FaultAlert',
        component: () => import('@/views/alert/FaultAlert.vue'),
        meta: { title: '故障报警' }
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/analysis/Analysis.vue'),
        meta: { title: '数据分析' }
      },
      {
        path: 'withdraw',
        name: 'WithdrawList',
        component: () => import('@/views/withdraw/WithdrawList.vue'),
        meta: { title: '提现管理' }
      },
      {
        path: 'operation/activity',
        name: 'ActivityList',
        component: () => import('@/views/operation/ActivityList.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'operation/coupon',
        name: 'CouponList',
        component: () => import('@/views/operation/CouponList.vue'),
        meta: { title: '优惠券管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const adminStore = useAdminStore()
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !adminStore.token) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  // 如果有token但没有adminInfo，先获取管理员信息
  if (to.meta.requiresAuth && adminStore.token && !adminStore.adminInfo) {
    await adminStore.getAdminInfoAction()
  }
  
  // 处理根路径重定向，根据角色决定重定向到哪里
  if (to.path === '/' && adminStore.adminInfo) {
    const role = adminStore.adminInfo.role
    if (role === 1 || role === '1') {
      // 充电站管理员：重定向到充电站管理
      next('/station')
      return
    } else if (role === 2 || role === '2') {
      // 运维人员：重定向到设备监控
      next('/monitor')
      return
    } else {
      // 系统管理员：重定向到数据大屏
      next('/dashboard')
      return
    }
  }
  
  // 检查角色权限
  if (to.meta.requiresAuth && adminStore.adminInfo) {
    const role = adminStore.adminInfo.role
    const path = to.path
    
    // 系统管理员（role=0）：所有权限
    if (role === 0 || role === '0') {
      next()
      return
    }
    
    // 充电站管理员（role=1）：只能访问充电站管理、充电桩管理、设备监控
    if (role === 1 || role === '1') {
      if (path.startsWith('/station') || 
          path.startsWith('/pile') || 
          path.startsWith('/monitor')) {
        next()
        return
      }
      // 无权限，跳转到充电站管理
      ElMessage.warning('您没有权限访问此页面')
      next('/station')
      return
    }
    
    // 运维人员（role=2）：只能访问故障报警、运维工单、设备监控
    if (role === 2 || role === '2') {
      if (path.startsWith('/alert') || 
          path.startsWith('/maintenance') || 
          path.startsWith('/monitor')) {
        next()
        return
      }
      // 无权限，跳转到设备监控
      ElMessage.warning('您没有权限访问此页面')
      next('/monitor')
      return
    }
  }
  
  next()
})

export default router

