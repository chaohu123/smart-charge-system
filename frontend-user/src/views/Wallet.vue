<template>
  <div class="wallet">
    <!-- 页面头部 -->
    <div class="wallet__header">
      <!-- 返回按钮 -->
      <el-button 
        class="wallet__back-button" 
        text 
        @click="handleGoBack"
      >
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      
      <!-- 标题（居中） -->
      <div class="wallet__header-content">
        <h1 class="wallet__title">我的钱包</h1>
      </div>
      
      <!-- 右侧操作区 -->
      <div class="wallet__header-right">
        <el-button 
          class="wallet__home-button" 
          text 
          @click="handleGoHome"
        >
          <el-icon><HomeFilled /></el-icon>
          <span>主页</span>
        </el-button>
        <el-icon class="wallet__security-icon"><Lock /></el-icon>
      </div>
    </div>

    <!-- 页面内容 -->
    <div class="wallet__content">
      <!-- 账户余额卡（视觉核心） -->
      <div class="wallet__balance-card">
        <div class="balance-card__content">
          <div class="balance-card__main">
            <p class="balance-card__label">账户余额</p>
            <p class="balance-card__amount" :class="{ 'animate': balanceChanged }">
              ¥{{ formatBalance(userStore.userInfo?.balance || 0) }}
            </p>
            <div class="balance-card__status">
              <el-tag type="success" size="small" effect="dark">
                <el-icon><CircleCheck /></el-icon>
                <span>正常</span>
              </el-tag>
              <span class="balance-card__hint">可用于充电消费</span>
            </div>
          </div>
          <div class="balance-card__security">
            <el-icon class="balance-card__lock-icon"><Lock /></el-icon>
          </div>
        </div>
      </div>

      <!-- 快捷操作区 -->
      <div class="wallet__actions">
        <div 
          class="action-item action-item--primary" 
          @click="handleRecharge"
        >
          <div class="action-item__icon action-item__icon--recharge">
            <el-icon><Plus /></el-icon>
          </div>
          <span class="action-item__text">充值</span>
        </div>
        <div 
          class="action-item" 
          @click="handleWithdraw"
        >
          <div class="action-item__icon action-item__icon--withdraw">
            <el-icon><Minus /></el-icon>
          </div>
          <span class="action-item__text">提现</span>
        </div>
        <div 
          class="action-item" 
          @click="handlePaymentMethods"
        >
          <div class="action-item__icon action-item__icon--payment">
            <el-icon><CreditCard /></el-icon>
          </div>
          <span class="action-item__text">支付方式</span>
        </div>
        <div 
          class="action-item" 
          @click="handleCoupons"
        >
          <div class="action-item__icon action-item__icon--coupon">
            <el-icon><Ticket /></el-icon>
          </div>
          <span class="action-item__text">优惠券</span>
        </div>
      </div>

      <!-- 钱包记录区 -->
      <div class="wallet__records">
        <el-card class="records-card" shadow="never">
          <template #header>
            <div class="records-card__header">
              <h3 class="records-card__title">交易记录</h3>
            </div>
          </template>
          
          <!-- Tabs 切换 -->
          <el-tabs 
            v-model="recordType" 
            @tab-change="handleRecordTypeChange"
            class="records-tabs"
          >
            <el-tab-pane label="消费记录" name="consume" />
            <el-tab-pane label="充值记录" name="recharge" />
            <el-tab-pane label="退款记录" name="refund" />
            <el-tab-pane label="提现记录" name="withdraw" />
          </el-tabs>

          <!-- 加载状态 -->
          <div v-if="loading" class="records-loading">
            <el-skeleton :rows="3" animated />
          </div>

          <!-- 空状态 -->
          <EmptyState
            v-else-if="!filteredRecords.length"
            :description="emptyStateConfig.description"
            :sub-description="emptyStateConfig.subDescription"
            :icon="emptyStateConfig.icon"
            :show-button="emptyStateConfig.showButton"
            :button-text="emptyStateConfig.buttonText"
            @action="emptyStateConfig.action"
          />

          <!-- 记录列表 -->
          <div v-else class="records-list">
            <div
              v-for="(record, index) in filteredRecords"
              :key="record?.id || `record-${index}`"
              class="record-item"
              @click="handleViewRecordDetail(record)"
            >
              <div class="record-item__left">
                <div class="record-item__icon" :class="getRecordIconClass(record)">
                  <el-icon>
                    <component :is="getRecordIcon(record)" />
                  </el-icon>
                </div>
                <div class="record-item__info">
                  <p class="record-item__type">{{ getRecordTypeText(record) }}</p>
                  <p class="record-item__desc">{{ getRecordDescription(record) }}</p>
                  <p class="record-item__time">{{ formatTime(record.createTime) }}</p>
                </div>
              </div>
              <div class="record-item__right">
                <p 
                  class="record-item__amount"
                  :class="getAmountClass(record)"
                >
                  {{ getAmountPrefix(record) }}¥{{ formatAmount(record.amount) }}
                </p>
                <el-tag 
                  :type="getStatusTagType(record.status)"
                  size="small"
                  effect="plain"
                >
                  {{ getStatusText(record.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 风险与规则提示区（可折叠） -->
      <div class="wallet__rules">
        <el-collapse v-model="activeRules" class="rules-collapse">
          <el-collapse-item name="rules" title="钱包使用规则与提示">
            <div class="rules-content">
              <div class="rule-item">
                <el-icon class="rule-item__icon"><InfoFilled /></el-icon>
                <div class="rule-item__content">
                  <p class="rule-item__title">钱包使用规则</p>
                  <p class="rule-item__text">账户余额可用于支付充电费用，充值后立即到账，提现将在1-3个工作日内到账。</p>
                </div>
              </div>
              <div class="rule-item">
                <el-icon class="rule-item__icon"><Clock /></el-icon>
                <div class="rule-item__content">
                  <p class="rule-item__title">退款到账时间</p>
                  <p class="rule-item__text">订单退款将在3-7个工作日内原路退回，请耐心等待。</p>
                </div>
              </div>
              <div class="rule-item">
                <el-icon class="rule-item__icon"><Lock /></el-icon>
                <div class="rule-item__content">
                  <p class="rule-item__title">安全提示</p>
                  <p class="rule-item__text">为保障账户安全，建议完成实名认证并绑定手机号。如有异常，请及时联系客服。</p>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <!-- 充值对话框 -->
    <el-dialog 
      v-model="showRechargeDialog" 
      title="账户充值" 
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="充值金额">
          <el-input-number 
            v-model="rechargeForm.amount" 
            :min="10" 
            :max="10000" 
            :precision="2"
            :step="10"
            style="width: 100%" 
            placeholder="请输入充值金额"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="rechargeForm.paymentMethod" style="width: 100%">
            <el-radio :label="1" border class="payment-radio">
              <el-icon><Wallet /></el-icon>
              <span>微信支付</span>
            </el-radio>
            <el-radio :label="2" border class="payment-radio">
              <el-icon><CreditCard /></el-icon>
              <span>支付宝</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <div class="quick-amounts">
            <span 
              v-for="amount in quickAmounts"
              :key="amount"
              class="quick-amount"
              :class="{ active: rechargeForm.amount === amount }"
              @click="rechargeForm.amount = amount"
            >
              ¥{{ amount }}
            </span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleRechargeSubmit"
          :loading="rechargeLoading"
        >
          确认充值
        </el-button>
      </template>
    </el-dialog>

    <!-- 提现对话框 -->
    <el-dialog 
      v-model="showWithdrawDialog" 
      title="账户提现" 
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form :model="withdrawForm" label-width="100px">
        <el-form-item label="可提现金额">
          <p class="available-amount">¥{{ formatBalance(userStore.userInfo?.balance || 0) }}</p>
        </el-form-item>
        <el-form-item label="提现金额">
          <el-input-number 
            v-model="withdrawForm.amount" 
            :min="10" 
            :max="userStore.userInfo?.balance || 0"
            :precision="2"
            :step="10"
            style="width: 100%" 
            placeholder="请输入提现金额"
          />
        </el-form-item>
        <el-alert
          title="提现将在1-3个工作日内到账"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="showWithdrawDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleWithdrawSubmit"
          :loading="withdrawLoading"
        >
          确认提现
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPaymentRecords, recharge, getBalance } from '@/api/payment'
import { applyWithdraw } from '@/api/withdraw'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import {
  ArrowLeft,
  Lock,
  Plus,
  Minus,
  CreditCard,
  Ticket,
  CircleCheck,
  InfoFilled,
  Clock,
  Wallet,
  Money,
  ShoppingCart,
  Refresh,
  HomeFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const balanceChanged = ref(false)
const showRechargeDialog = ref(false)
const showWithdrawDialog = ref(false)
const rechargeLoading = ref(false)
const withdrawLoading = ref(false)
const records = ref([])
const recordType = ref('consume')
const activeRules = ref([])

// 充值表单
const rechargeForm = ref({
  amount: 100,
  paymentMethod: 1
})

// 提现表单
const withdrawForm = ref({
  amount: 0
})

// 快捷金额
const quickAmounts = [50, 100, 200, 500, 1000]

// 监听余额变化
let previousBalance = userStore.userInfo?.balance || 0
watch(() => userStore.userInfo?.balance, (newBalance) => {
  if (newBalance !== previousBalance) {
    balanceChanged.value = true
    setTimeout(() => {
      balanceChanged.value = false
    }, 600)
    previousBalance = newBalance
  }
})

// 过滤后的记录
const filteredRecords = computed(() => {
  // 确保 records.value 是数组，如果不是则返回空数组
  const recordsList = Array.isArray(records.value) ? records.value : []
  
  if (recordType.value === 'consume') {
    return recordsList.filter(item => item && item.type === 2) // 消费
  }
  if (recordType.value === 'recharge') {
    return recordsList.filter(item => item && item.type === 1) // 充值
  }
  if (recordType.value === 'refund') {
    return recordsList.filter(item => item && item.type === 3) // 退款
  }
  if (recordType.value === 'withdraw') {
    return recordsList.filter(item => item && item.type === 4) // 提现
  }
  return recordsList
})

// 空状态配置
const emptyStateConfig = computed(() => {
  const configs = {
    consume: {
      description: '暂无消费记录',
      subDescription: '快去预约充电桩，开始你的充电之旅吧~',
      icon: 'ShoppingCart',
      showButton: true,
      buttonText: '去充电',
      action: () => router.push('/home')
    },
    recharge: {
      description: '暂无充值记录',
      subDescription: '账户余额不足时，记得及时充值哦~',
      icon: 'Wallet',
      showButton: true,
      buttonText: '立即充值',
      action: () => handleRecharge()
    },
    refund: {
      description: '暂无退款记录',
      subDescription: '订单退款将显示在这里，请耐心等待~',
      icon: 'Refresh',
      showButton: false,
      buttonText: '',
      action: () => {}
    },
    withdraw: {
      description: '暂无提现记录',
      subDescription: '提现申请将显示在这里，审核通过后到账~',
      icon: 'Minus',
      showButton: false,
      buttonText: '',
      action: () => {}
    }
  }
  return configs[recordType.value] || configs.consume
})

// 方法
const handleGoBack = () => {
  router.push('/profile')
}

const handleGoHome = () => {
  router.push('/home')
}

const handleRecharge = () => {
  showRechargeDialog.value = true
}

const handleWithdraw = () => {
  if (!userStore.userInfo?.balance || userStore.userInfo.balance < 10) {
    ElMessage.warning('账户余额不足，无法提现')
    return
  }
  showWithdrawDialog.value = true
}

const handlePaymentMethods = () => {
  ElMessage.info('支付方式管理功能开发中')
}

const handleCoupons = () => {
  router.push('/coupons')
}

const handleRecordTypeChange = () => {
  // 切换标签时使用计算属性过滤
}

const handleRechargeSubmit = async () => {
  if (!rechargeForm.value.amount || rechargeForm.value.amount < 10) {
    ElMessage.warning('充值金额不能少于10元')
    return
  }
  
  rechargeLoading.value = true
  try {
    const res = await recharge(rechargeForm.value.amount, rechargeForm.value.paymentMethod)
    if (res.code === 200) {
      ElMessage.success('充值成功')
      showRechargeDialog.value = false
      // 重置表单
      rechargeForm.value.amount = 100
      rechargeForm.value.paymentMethod = 1
      // 刷新余额
      await loadBalance()
      // 切换到充值记录标签页
      recordType.value = 'recharge'
      // 重新加载记录
      await loadRecords()
    } else {
      ElMessage.error(res.message || '充值失败')
    }
  } catch (error) {
    ElMessage.error('充值失败，请稍后重试')
    console.error('充值失败:', error)
  } finally {
    rechargeLoading.value = false
  }
}

const handleWithdrawSubmit = async () => {
  if (!withdrawForm.value.amount || withdrawForm.value.amount < 10) {
    ElMessage.warning('提现金额不能少于10元')
    return
  }
  if (withdrawForm.value.amount > (userStore.userInfo?.balance || 0)) {
    ElMessage.warning('提现金额不能超过账户余额')
    return
  }
  
  withdrawLoading.value = true
  try {
    const res = await applyWithdraw(withdrawForm.value.amount)
    if (res.code === 200) {
      ElMessage.success('提现申请已提交，等待审核')
      showWithdrawDialog.value = false
      // 重置表单
      withdrawForm.value.amount = 0
      // 刷新余额（余额可能被冻结或减少）
      await loadBalance()
      // 切换到提现记录标签页
      recordType.value = 'withdraw'
      // 重新加载记录
      await loadRecords()
    } else {
      ElMessage.error(res.message || '提现申请失败')
    }
  } catch (error) {
    ElMessage.error('提现申请失败，请稍后重试')
    console.error('提现失败:', error)
  } finally {
    withdrawLoading.value = false
  }
}

const handleViewRecordDetail = (record) => {
  // 跳转到订单详情
  if (record.orderId) {
    router.push(`/order/${record.orderId}`)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getPaymentRecords({ current: 1, size: 100 })
    console.log('交易记录API响应:', res)
    console.log('data内容:', res?.data)
    console.log('records内容:', res?.data?.records)
    
    if (res && res.code === 200) {
      // 处理 PageResult 结构：res.data.records 或 res.data.list
      let dataList = null
      
      if (res.data) {
        // 优先检查 records 字段（PageResult 标准结构）
        if (res.data.records !== undefined) {
          // records 可能是 null 或数组
          dataList = res.data.records
        } else if (res.data.list !== undefined) {
          // 兼容 list 字段
          dataList = res.data.list
        } else if (Array.isArray(res.data)) {
          // 如果 data 本身就是数组
          dataList = res.data
        }
      }
      
      // 确保始终是数组：null 或 undefined 都转换为空数组
      if (Array.isArray(dataList)) {
        records.value = dataList
        console.log('加载交易记录成功，共', dataList.length, '条')
      } else {
        // records 为 null 或 undefined 时，设置为空数组
        records.value = []
        console.log('交易记录为空或格式不正确，已设置为空数组。dataList:', dataList)
      }
    } else {
      records.value = []
      console.log('API返回错误或数据为空')
    }
  } catch (error) {
    console.error('加载交易记录失败:', error)
    ElMessage.error('加载交易记录失败')
    records.value = [] // 确保错误时也是空数组
  } finally {
    loading.value = false
  }
}

// 格式化方法
const formatBalance = (balance) => {
  if (balance === null || balance === undefined) return '0.00'
  const num = typeof balance === 'string' ? parseFloat(balance) : Number(balance)
  if (isNaN(num)) return '0.00'
  return num.toFixed(2)
}

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  const num = typeof amount === 'string' ? parseFloat(amount) : Number(amount)
  if (isNaN(num)) return '0.00'
  return num.toFixed(2)
}

const formatTime = (time) => {
  if (!time) return '—'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 记录相关方法
const getRecordTypeText = (record) => {
  if (record.type === 1) return '充值'
  if (record.type === 2) return '消费'
  if (record.type === 3) return '退款'
  if (record.type === 4) return '提现'
  return '交易'
}

const getRecordDescription = (record) => {
  if (record.type === 1) {
    return record.paymentMethod === 1 ? '微信支付充值' : record.paymentMethod === 2 ? '支付宝充值' : '账户充值'
  }
  if (record.type === 2) {
    return record.stationName || record.description || '充电消费'
  }
  if (record.type === 3) {
    return record.description || '订单退款'
  }
  if (record.type === 4) {
    return '提现申请' + (record.status === 0 ? '（待审核）' : record.status === 1 ? '（已通过）' : '（已拒绝）')
  }
  return record.description || '交易'
}

const getRecordIcon = (record) => {
  if (record.type === 1) return Plus
  if (record.type === 2) return ShoppingCart
  if (record.type === 3) return Refresh
  if (record.type === 4) return Minus
  return Money
}

const getRecordIconClass = (record) => {
  if (record.type === 1) return 'record-icon--recharge'
  if (record.type === 2) return 'record-icon--consume'
  if (record.type === 3) return 'record-icon--refund'
  if (record.type === 4) return 'record-icon--withdraw'
  return ''
}

const getAmountPrefix = (record) => {
  if (record.type === 1) return '+' // 充值
  if (record.type === 3) return '+' // 退款
  if (record.type === 4) return '-' // 提现
  return '-' // 消费
}

const getAmountClass = (record) => {
  if (record.type === 1 || record.type === 3) return 'amount--income' // 充值和退款是收入
  return 'amount--expense' // 消费和提现是支出
}

const getStatusText = (status) => {
  if (status === 1 || status === 'success') return '成功'
  if (status === 0 || status === 'processing') return '待审核'
  if (status === 2) return '已拒绝'
  if (status === 3 || status === 'failed') return '失败'
  return '成功'
}

const getStatusTagType = (status) => {
  if (status === 1 || status === 'success') return 'success' // 成功/已通过
  if (status === 0 || status === 'processing') return 'warning' // 待审核/处理中
  if (status === 2) return 'danger' // 已拒绝
  if (status === 3 || status === 'failed') return 'danger' // 失败
  return 'success'
}

onMounted(async () => {
  // 如果已登录，获取最新的用户信息（确保余额是最新的）
  if (userStore.token) {
    await loadBalance()
  }
  // 加载交易记录
  await loadRecords()
})


const loadBalance = async () => {
  try {
    const res = await getBalance()
    if (res.code === 200) {
      if (!userStore.userInfo) {
        userStore.userInfo = {}
      }
      userStore.userInfo.balance = res.data || 0
    }
  } catch (error) {
    console.error('获取余额失败', error)
  }
}
</script>

<style scoped lang="scss">
.wallet {
  min-height: 100vh;
  background: var(--bg-color);
  padding-bottom: 80px;
}

/* 页面头部 */
.wallet__header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-md);
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.wallet__back-button {
  color: var(--text-primary);
  font-size: 20px;
}

.wallet__header-content {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.wallet__title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.wallet__header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.wallet__home-button {
  color: var(--text-primary);
  font-size: var(--font-size-md);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.wallet__home-button:hover {
  color: #1677FF;
}

.wallet__security-icon {
  font-size: 20px;
  color: #1677FF;
}

/* 页面内容 */
.wallet__content {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg) var(--spacing-md);
}

/* 账户余额卡 */
.wallet__balance-card {
  margin-bottom: var(--spacing-lg);
  border-radius: 16px;
  background: linear-gradient(135deg, #1677FF 0%, #00B578 100%);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.2);
  padding: var(--spacing-xl);
  color: #ffffff;
  position: relative;
  overflow: hidden;
}

.wallet__balance-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.balance-card__content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.balance-card__main {
  flex: 1;
}

.balance-card__label {
  font-size: var(--font-size-md);
  opacity: 0.9;
  margin: 0 0 var(--spacing-md) 0;
}

.balance-card__amount {
  font-size: 64px;
  font-weight: 700;
  margin: 0 0 var(--spacing-md) 0;
  line-height: 1;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.balance-card__amount.animate {
  animation: balancePulse 0.6s ease;
}

@keyframes balancePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.balance-card__status {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.balance-card__hint {
  font-size: var(--font-size-sm);
  opacity: 0.85;
}

.balance-card__security {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  backdrop-filter: blur(10px);
}

.balance-card__lock-icon {
  font-size: 24px;
  color: #ffffff;
}

/* 快捷操作区 */
.wallet__actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  gap: var(--spacing-sm);
}

.action-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.action-item--primary {
  background: linear-gradient(135deg, #1677FF 0%, #4096FF 100%);
  color: #ffffff;
}

.action-item--primary .action-item__text {
  color: #ffffff;
}

.action-item__icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: rgba(22, 119, 255, 0.1);
  color: #1677FF;
}

.action-item--primary .action-item__icon {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.action-item__icon--recharge {
  background: rgba(22, 119, 255, 0.1);
  color: #1677FF;
}

.action-item__icon--withdraw {
  background: rgba(250, 173, 20, 0.1);
  color: #FAAD14;
}

.action-item__icon--payment {
  background: rgba(0, 181, 120, 0.1);
  color: #00B578;
}

.action-item__icon--coupon {
  background: rgba(255, 77, 79, 0.1);
  color: #FF4D4F;
}

.action-item__text {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-primary);
}

/* 钱包记录区 */
.wallet__records {
  margin-bottom: var(--spacing-lg);
}

.records-card {
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: none;
}

.records-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.records-card__title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.records-tabs {
  margin-bottom: var(--spacing-md);
}

:deep(.el-tabs__item) {
  font-weight: 500;
}

:deep(.el-tabs__active-bar) {
  background: #1677FF;
}

:deep(.el-tabs__item.is-active) {
  color: #1677FF;
}

.records-loading {
  padding: var(--spacing-lg) 0;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--divider-color);
  cursor: pointer;
  transition: all 0.3s ease;
}

.record-item:hover {
  background: var(--bg-hover);
  border-color: #1677FF;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.1);
}

.record-item__left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex: 1;
}

.record-item__icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.record-icon--recharge {
  background: rgba(82, 196, 26, 0.1);
  color: #52C41A;
}

.record-icon--consume {
  background: rgba(22, 119, 255, 0.1);
  color: #1677FF;
}

.record-icon--refund {
  background: rgba(250, 173, 20, 0.1);
  color: #FAAD14;
}

.record-icon--withdraw {
  background: rgba(255, 77, 79, 0.1);
  color: #FF4D4F;
}

.record-item__info {
  flex: 1;
}

.record-item__type {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.record-item__desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 4px 0;
}

.record-item__time {
  font-size: var(--font-size-xs);
  color: var(--text-disabled);
  margin: 0;
}

.record-item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--spacing-xs);
}

.record-item__amount {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin: 0;
}

.amount--income {
  color: #52C41A;
}

.amount--expense {
  color: var(--text-primary);
}

/* 风险与规则提示区 */
.wallet__rules {
  margin-bottom: var(--spacing-lg);
}

.rules-collapse {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: none;
}

:deep(.el-collapse-item__header) {
  padding: var(--spacing-md) var(--spacing-lg);
  font-weight: 500;
  color: var(--text-primary);
}

:deep(.el-collapse-item__content) {
  padding: 0 var(--spacing-lg) var(--spacing-lg);
}

.rules-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.rule-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--bg-hover);
  border-radius: 8px;
}

.rule-item__icon {
  font-size: 20px;
  color: #1677FF;
  margin-top: 2px;
}

.rule-item__content {
  flex: 1;
}

.rule-item__title {
  font-size: var(--font-size-md);
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.rule-item__text {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
}

/* 对话框样式 */
.quick-amounts {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.quick-amount {
  padding: 8px 16px;
  border: 1px solid var(--divider-color);
  border-radius: 6px;
  background: var(--bg-card);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: var(--font-size-sm);
}

.quick-amount:hover {
  border-color: #1677FF;
  color: #1677FF;
}

.quick-amount.active {
  border-color: #1677FF;
  background: rgba(22, 119, 255, 0.1);
  color: #1677FF;
}

.available-amount {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: #1677FF;
  margin: 0;
}

.payment-radio {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .wallet__balance-card {
    padding: var(--spacing-lg);
  }

  .balance-card__amount {
    font-size: 48px;
  }

  .wallet__actions {
    grid-template-columns: repeat(2, 1fr);
  }

  .action-item {
    padding: var(--spacing-md);
  }

  .action-item__icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}
</style>