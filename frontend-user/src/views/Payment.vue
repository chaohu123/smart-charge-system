<template>
  <div class="payment">
    <el-card>
      <template #header>
        <span>订单支付</span>
      </template>
      
      <div v-if="order" class="order-info">
        <h3>订单信息</h3>
        <el-alert
          v-if="order.status !== 0"
          title="当前订单状态不可支付（仅待支付状态可支付）"
          type="warning"
          show-icon
          closable={false}
          style="margin-bottom: 12px;"
        />
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ formatStatus(order.status) }}</el-descriptions-item>
          <el-descriptions-item label="充电站">{{ order.stationName || '充电站' }}</el-descriptions-item>
          <el-descriptions-item label="站点地址">{{ order.stationAddress || '--' }}</el-descriptions-item>
          <el-descriptions-item label="枪型">{{ getPileTypeText(order.pileType) }}</el-descriptions-item>
          <el-descriptions-item label="枪编号">{{ order.pileNumber || '--' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(order.actualStartTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(order.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="使用时长">{{ formatDuration(order.actualStartTime, order.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="充电量">{{ order.power || 0 }}kWh</el-descriptions-item>
          <el-descriptions-item label="电费">¥{{ order.electricityFee ?? order.price ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="服务费">¥{{ order.serviceFee || 0 }}</el-descriptions-item>
          <el-descriptions-item label="应付金额" :span="2">
            <span class="total-amount">¥{{ order.totalAmount || 0 }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider />

      <div class="payment-methods">
        <h3>选择支付方式</h3>
        <el-radio-group v-model="paymentMethod">
          <el-radio :value="0" border>
            <div class="payment-option">
              <el-icon><Wallet /></el-icon>
              <span>余额支付</span>
              <span class="balance">余额: ¥{{ userStore.userInfo?.balance || 0 }}</span>
            </div>
          </el-radio>
          <el-radio :value="1" border>
            <div class="payment-option">
              <el-icon><Message /></el-icon>
              <span>微信支付</span>
            </div>
          </el-radio>
          <el-radio :value="2" border>
            <div class="payment-option">
              <el-icon><CreditCard /></el-icon>
              <span>支付宝</span>
            </div>
          </el-radio>
        </el-radio-group>
      </div>

      <el-divider />

      <div class="discount-section">
        <h3>优惠与积分</h3>
        
        <!-- 优惠券选择 -->
        <div class="coupon-select">
          <el-select
            v-model="selectedCouponId"
            placeholder="选择优惠券"
            clearable
            style="width: 100%; margin-bottom: 15px;"
          >
            <el-option
              v-for="coupon in availableCoupons"
              :key="coupon.id"
              :label="`${coupon.name} - 满${coupon.minAmount}减${coupon.amount}`"
              :value="coupon.id"
            />
          </el-select>
        </div>
        
        <!-- 积分使用 -->
        <div class="points-select">
          <el-checkbox v-model="usePoints">使用积分抵扣</el-checkbox>
          <el-input-number
            v-if="usePoints"
            v-model="pointsAmount"
            :min="0"
            :max="maxPoints"
            style="width: 150px; margin-left: 10px;"
          />
          <p v-if="usePoints" class="points-info">可用积分：{{ pointsBalance }}，可抵扣：¥{{ (pointsAmount / 100).toFixed(2) }}</p>
        </div>
      </div>

      <el-divider />

      <div v-if="order" class="payment-summary">
        <div class="summary-item">
          <span>订单金额：</span>
          <span>¥{{ order?.totalAmount || 0 }}</span>
        </div>
        <div v-if="selectedCouponId" class="summary-item">
          <span>优惠券抵扣：</span>
          <span style="color: #67C23A;">
            -¥{{ availableCoupons.find(c => c.id === selectedCouponId)?.amount || 0 }}
          </span>
        </div>
        <div v-if="usePoints && pointsAmount > 0" class="summary-item">
          <span>积分抵扣：</span>
          <span style="color: #67C23A;">-¥{{ (pointsAmount / 100).toFixed(2) }}</span>
        </div>
        <div class="summary-item total">
          <span>实付金额：</span>
          <span style="font-size: 20px; font-weight: bold; color: #F56C6C;">
            ¥{{ calculateFinalAmount() }}
          </span>
        </div>
      </div>
      <div v-else class="payment-summary">
        <el-skeleton :rows="3" animated />
      </div>

      <div class="payment-actions">
        <el-button
          type="primary"
          size="large"
          @click="handlePay"
          :loading="loading"
          :disabled="!order || order.status !== 0"
          style="width: 100%"
        >
          确认支付
        </el-button>
        <el-button @click="$router.back()" style="width: 100%; margin-top: 10px;">
          取消
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getOrderDetail } from '@/api/order'
import { pay, getBalance } from '@/api/payment'
import { getPointsBalance } from '@/api/points'
import { getMyCoupons } from '@/api/coupon'
import { ElMessage } from 'element-plus'
import { Wallet, Message, CreditCard } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const order = ref(null)
const paymentMethod = ref(0)
const loading = ref(false)
const usePoints = ref(false)
const pointsAmount = ref(0)
const pointsBalance = ref(0)
const maxPoints = ref(0)
const availableCoupons = ref([])
const selectedCouponId = ref(null)

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    await loadOrderDetail(orderId)
    await loadPointsBalance()
    await loadAvailableCoupons()
    await loadBalance()
  } else {
    ElMessage.error('订单ID不能为空')
    router.back()
  }
})

const loadAvailableCoupons = async () => {
  try {
    const res = await getMyCoupons(0) // 获取未使用的优惠券
    if (res.code === 200) {
      availableCoupons.value = res.data || []
      // 过滤出满足使用条件的优惠券
      if (order.value) {
        availableCoupons.value = availableCoupons.value.filter(coupon => {
          return coupon.minAmount <= (order.value.totalAmount || 0)
        })
      }
    }
  } catch (error) {
    console.error('加载优惠券失败:', error)
  }
}

const loadPointsBalance = async () => {
  try {
    const res = await getPointsBalance()
    if (res.code === 200) {
      pointsBalance.value = res.data || 0
      maxPoints.value = pointsBalance.value
    }
  } catch (error) {
    console.error('加载积分失败:', error)
  }
}

const loadOrderDetail = async (orderId) => {
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200) {
      order.value = res.data
    // 如果订单状态不可支付，提示用户
    if (order.value.status !== 0) {
      ElMessage.warning('当前订单状态不可支付')
    }
    }
  } catch (error) {
    ElMessage.error('加载订单信息失败')
  }
}

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

const formatDateTime = (time) => {
  if (!time) return '--'
  return String(time).replace('T', ' ').slice(0, 19)
}

const formatDuration = (start, end) => {
  if (!start || !end) return '--'
  const startTime = new Date(start).getTime()
  const endTime = new Date(end).getTime()
  if (Number.isNaN(startTime) || Number.isNaN(endTime) || endTime <= startTime) return '--'
  const diff = endTime - startTime
  const hours = Math.floor(diff / 3600000)
  const minutes = Math.floor((diff % 3600000) / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)
  const parts = []
  if (hours) parts.push(`${hours}小时`)
  if (minutes) parts.push(`${minutes}分钟`)
  if (!hours && !minutes) parts.push(`${seconds}秒`)
  return parts.join('') || '--'
}

const getPileTypeText = (pileType) => {
  if (pileType === undefined || pileType === null) return '快充'
  return pileType === 1 ? '快充' : '慢充'
}

const formatStatus = (status) => {
  const map = {
    0: '待支付',
    1: '充电中',
    2: '已完成',
    3: '已取消',
    4: '已退款'
  }
  return map[status] || '未知'
}

const calculateFinalAmount = () => {
  let amount = parseFloat(order.value?.totalAmount || 0)
  
  // 优惠券抵扣
  if (selectedCouponId.value) {
    const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value)
    if (coupon) {
      amount -= parseFloat(coupon.amount || 0)
    }
  }
  
  // 积分抵扣
  if (usePoints.value && pointsAmount.value > 0) {
    amount -= pointsAmount.value / 100
  }
  
  return Math.max(0, amount).toFixed(2)
}

const handlePay = async () => {
  if (!order.value) {
    ElMessage.error('订单信息不存在')
    return
  }

  if (order.value.status !== 0) {
    ElMessage.error('当前订单状态不可支付')
    return
  }

  const finalAmount = parseFloat(calculateFinalAmount())
  
  if (paymentMethod.value === 0 && (userStore.userInfo?.balance || 0) < finalAmount) {
    ElMessage.warning('余额不足，请先充值')
    router.push('/wallet')
    return
  }

  loading.value = true
  try {
    // 如果使用积分，先调用积分使用接口
    if (usePoints.value && pointsAmount.value > 0) {
      // 这里应该调用积分使用接口
      // await usePoints(pointsAmount.value, order.value.id)
    }
    
    const res = await pay(order.value.id, paymentMethod.value)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      router.push('/orders')
    }
  } catch (error) {
    ElMessage.error('支付失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.payment {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.order-info h3 {
  margin-bottom: 15px;
}

.order-info p {
  margin: 10px 0;
  color: #666;
}

.total-amount {
  font-size: 20px;
  font-weight: bold;
  color: #F56C6C;
  margin-top: 20px;
}

.payment-methods h3 {
  margin-bottom: 15px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
}

.payment-option .balance {
  margin-left: auto;
  color: #409EFF;
}

.discount-section {
  margin: 20px 0;
}

.points-info {
  color: #666;
  font-size: 12px;
  margin-top: 5px;
}

.payment-summary {
  margin: 20px 0;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 5px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin: 10px 0;
}

.summary-item.total {
  border-top: 1px solid #ddd;
  padding-top: 10px;
  margin-top: 15px;
}

.payment-actions {
  margin-top: 20px;
}
</style>


