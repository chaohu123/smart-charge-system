<template>
  <div class="charging">
    <el-container>
      <el-header>
        <h2>充电中</h2>
      </el-header>
      <el-main>
        <el-card v-if="currentOrder">
          <div class="charging-info">
            <div class="power-display">
              <h1>{{ displayPower }}kWh</h1>
              <p>已充电量</p>
            </div>

            <div class="stats">
              <div class="stat-item">
                <p class="label">充电功率</p>
                <p class="value">{{ displayPowerRate }}kW</p>
              </div>
              <div class="stat-item">
                <p class="label">已用时长</p>
                <p class="value">{{ elapsedDuration }}</p>
              </div>
              <div class="stat-item">
                <p class="label">预计剩余时间</p>
                <p class="value">{{ estimatedTime }}</p>
              </div>
              <div class="stat-item">
                <p class="label">当前费用</p>
                <p class="value">¥{{ currentOrder.currentAmount || 0 }}</p>
              </div>
            </div>

            <div class="progress-wrapper">
              <el-progress
                type="circle"
                :percentage="progress"
                :width="140"
                :stroke-width="10"
                color="#409EFF"
              >
                <template #default="{ percentage }">
                  <div class="progress-inner">
                    <div class="progress-value">{{ percentage }}%</div>
                    <div class="progress-label">充电进度</div>
                  </div>
                </template>
              </el-progress>
            </div>

            <div class="actions">
              <el-button type="danger" size="large" @click="handleStopCharging">
                停止充电
              </el-button>
            </div>
          </div>
        </el-card>
        <el-empty v-else description="当前没有进行中的充电订单" />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCurrentOrder, stopCharging, getOrderStatus } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import websocket from '@/utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const currentOrder = ref(null)
const estimatedTime = ref('--')
const elapsedDuration = ref('--')
const progress = ref(0)
const displayPower = ref(0)
const displayPowerRate = ref(0)
let timer = null
let subscription = null
let tickTimer = null

onMounted(async () => {
  await loadCurrentOrder()
  
  // 尝试使用WebSocket实时更新
  if (currentOrder.value && userStore.token) {
    try {
      websocket.connect(userStore.token, () => {
        subscription = websocket.subscribe(`/topic/order/${currentOrder.value.id}`, (data) => {
          currentOrder.value = { ...currentOrder.value, ...data }
          updateRuntimeDisplays()
        })
      }, (error) => {
        console.error('WebSocket连接失败，使用轮询方式:', error)
        // WebSocket连接失败，使用轮询
        timer = setInterval(async () => {
          await updateOrderStatus()
        }, 5000)
      })
    } catch (error) {
      // 如果WebSocket不可用，使用轮询
      timer = setInterval(async () => {
        await updateOrderStatus()
      }, 5000)
    }
  } else {
    // 没有订单或未登录，使用轮询
    timer = setInterval(async () => {
      await updateOrderStatus()
    }, 5000)
  }

  // 本地tick，用于更新时间显示与进度展示
  tickTimer = setInterval(() => {
    updateRuntimeDisplays()
  }, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  if (tickTimer) {
    clearInterval(tickTimer)
  }
  if (subscription) {
    subscription.unsubscribe()
  }
  websocket.disconnect()
})

const loadCurrentOrder = async () => {
  try {
    const res = await getCurrentOrder()
    if (res.code === 200 && res.data) {
      currentOrder.value = res.data
      updateRuntimeDisplays()
    }
  } catch (error) {
    console.error('加载当前订单失败:', error)
  }
}

const updateOrderStatus = async () => {
  if (!currentOrder.value) return
  try {
    const res = await getOrderStatus(currentOrder.value.id)
    if (res.code === 200) {
      currentOrder.value = { ...currentOrder.value, ...res.data }
      updateRuntimeDisplays()
    }
  } catch (error) {
    console.error('更新订单状态失败:', error)
  }
}

const updateRuntimeDisplays = () => {
  if (!currentOrder.value) {
    elapsedDuration.value = '--'
    estimatedTime.value = '--'
    progress.value = 0
    displayPower.value = 0
    displayPowerRate.value = 0
    return
  }
  // 已充电量优先使用实时字段 currentPower，其次使用订单累计 power
  const energy = Number(currentOrder.value.currentPower ?? currentOrder.value.power ?? 0)
  displayPower.value = energy.toFixed ? Number(energy.toFixed(2)) : energy

  // 当前功率优先使用 currentPowerRate，其次使用桩额定功率 pilePower
  const rate = Number(currentOrder.value.currentPowerRate ?? currentOrder.value.pilePower ?? 0)
  displayPowerRate.value = rate.toFixed ? Number(rate.toFixed(2)) : rate

  elapsedDuration.value = formatDuration(currentOrder.value.actualStartTime, currentOrder.value.endTime)
  estimatedTime.value = calculateEstimatedTime()
  progress.value = calculateProgress()
}

const calculateEstimatedTime = () => {
  if (!currentOrder.value) return '--'
  const rate = Number(currentOrder.value.currentPowerRate ?? currentOrder.value.pilePower) || 0
  const target = Number(currentOrder.value.targetPower || 60)
  const power = Number(currentOrder.value.currentPower ?? currentOrder.value.power) || 0
  if (rate <= 0) return '--'
  const remaining = Math.max(0, target - power)
  const minutes = Math.ceil((remaining / rate) * 60)
  if (minutes < 60) return `${minutes}分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m ? `${h}小时${m}分钟` : `${h}小时`
}

const calculateProgress = () => {
  const power = Number(currentOrder.value?.currentPower ?? currentOrder.value?.power) || 0
  const target = Number(currentOrder.value?.targetPower || 60)
  if (target <= 0) return 0
  return Math.min(100, Math.floor((power / target) * 100))
}

const formatDuration = (start, end) => {
  if (!start) return '--'
  const startTime = new Date(start).getTime()
  const endTime = end ? new Date(end).getTime() : Date.now()
  if (Number.isNaN(startTime) || Number.isNaN(endTime) || endTime <= startTime) return '0分钟'
  const diff = endTime - startTime
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) return `${mins}分钟`
  return mins ? `${hours}小时${mins}分钟` : `${hours}小时`
}

const handleStopCharging = async () => {
  try {
    await ElMessageBox.confirm('确定要停止充电吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await stopCharging(currentOrder.value.id)
    if (res.code === 200) {
      ElMessage.success('已停止充电')
      router.push({ path: '/payment', query: { orderId: currentOrder.value.id } })
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('停止充电失败')
    }
  }
}
</script>

<style scoped>
.charging {
  min-height: 100vh;
  background: var(--bg-color);
}

.charging .el-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  display: flex;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
}

.charging .el-header h2 {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: 600;
}

.charging .el-main {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-xl);
}

.charging-info {
  text-align: center;
  padding: var(--spacing-lg);
}

.power-display {
  margin-bottom: var(--spacing-xl);
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, rgba(0, 200, 83, 0.1) 0%, rgba(33, 150, 243, 0.1) 100%);
  border-radius: var(--radius-xl);
}

.power-display h1 {
  font-size: 64px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--spacing-sm);
}

.power-display p {
  color: var(--text-secondary);
  font-size: var(--font-size-md);
  margin: 0;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
}

.stat-item {
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
}

.stat-item:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-item .label {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-sm);
}

.stat-item .value {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--primary-color);
}

.actions {
  margin-top: var(--spacing-xl);
}

:deep(.el-card) {
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
}

@media (max-width: 768px) {
  .power-display h1 {
    font-size: 48px;
  }
  
  .stats {
    grid-template-columns: 1fr;
  }
}
</style>

