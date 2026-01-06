import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', requiresAuth: false }
  },
  {
    path: '/map',
    name: 'Map',
    component: () => import('@/views/Map.vue'),
    meta: { title: '地图找桩', requiresAuth: false }
  },
  {
    path: '/station/:id',
    name: 'StationDetail',
    component: () => import('@/views/StationDetail.vue'),
    meta: { title: '充电站详情', requiresAuth: false }
  },
  {
    path: '/reservation',
    name: 'Reservation',
    component: () => import('@/views/Reservation.vue'),
    meta: { title: '预约充电', requiresAuth: true }
  },
  {
    path: '/reservation-history',
    name: 'ReservationHistory',
    component: () => import('@/views/ReservationHistory.vue'),
    meta: { title: '我的预约', requiresAuth: true }
  },
  {
    path: '/charging',
    name: 'Charging',
    component: () => import('@/views/Charging.vue'),
    meta: { title: '充电中', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/Orders.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { title: '订单详情', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/Wallet.vue'),
    meta: { title: '我的钱包', requiresAuth: true }
  },
  {
    path: '/password/change',
    name: 'ChangePassword',
    component: () => import('@/views/ChangePassword.vue'),
    meta: { title: '修改密码', requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/Favorites.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/evaluation',
    name: 'EvaluationForm',
    component: () => import('@/views/EvaluationForm.vue'),
    meta: { title: '评价充电站', requiresAuth: true }
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('@/views/Payment.vue'),
    meta: { title: '订单支付', requiresAuth: true }
  },
  {
    path: '/scan',
    name: 'ScanCharge',
    component: () => import('@/views/ScanCharge.vue'),
    meta: { title: '扫码充电', requiresAuth: true }
  },
  {
    path: '/search',
    name: 'StationSearch',
    component: () => import('@/views/StationSearch.vue'),
    meta: { title: '搜索充电站', requiresAuth: false }
  },
  {
    path: '/invoice',
    name: 'InvoiceList',
    component: () => import('@/views/InvoiceList.vue'),
    meta: { title: '我的发票', requiresAuth: true }
  },
  {
    path: '/invoice/apply',
    name: 'InvoiceApply',
    component: () => import('@/views/InvoiceApply.vue'),
    meta: { title: '申请发票', requiresAuth: true }
  },
  {
    path: '/fault/report',
    name: 'FaultReport',
    component: () => import('@/views/FaultReport.vue'),
    meta: { title: '故障上报', requiresAuth: true }
  },
  {
    path: '/coupons',
    name: 'Coupons',
    component: () => import('@/views/Coupons.vue'),
    meta: { title: '优惠券', requiresAuth: true }
  },
  {
    path: '/points',
    name: 'Points',
    component: () => import('@/views/Points.vue'),
    meta: { title: '我的积分', requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/Notifications.vue'),
    meta: { title: '订单消息与系统消息', requiresAuth: true }
  },
  {
    path: '/route-search',
    name: 'RouteSearch',
    component: () => import('@/views/RouteSearch.vue'),
    meta: { title: '沿途搜索', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else {
    next()
  }
})

export default router

