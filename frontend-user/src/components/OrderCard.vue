<template>
  <div class="order-card">
    <!-- 卡片头部：状态标签 + 订单编号与时间 -->
    <div class="order-card__header">
      <el-tag :class="getStatusTagClass(order.status)" class="order-card__status-tag">
        {{ getStatusText(order.status) }}
      </el-tag>
      <div class="order-card__meta">
        <span class="order-card__order-no">订单号: {{ order.orderNo }}</span>
        <span class="order-card__separator">|</span>
        <span class="order-card__time">{{ formatTime(order.createTime) }}</span>
      </div>
    </div>

    <!-- 卡片主体：充电站信息 + 充电详情 -->
    <div class="order-card__body">
      <!-- 充电站信息 -->
      <div class="order-card__station-info">
        <h3 class="order-card__station-name">{{ order.stationName || '充电站名称' }}</h3>
        <div class="order-card__station-address">
          <el-icon><Location /></el-icon>
          <span>{{ order.stationAddress || '地址信息待完善' }}</span>
        </div>
      </div>

      <!-- 充电详情 -->
      <div class="order-card__charging-details">
        <div class="order-card__detail-item">
          <el-icon><Lightning /></el-icon>
          <span>{{ getPileTypeText(order.pileType) }}</span>
        </div>
        <span class="order-card__detail-separator">|</span>
        <div class="order-card__detail-item">
          <span>{{ formatPower(order.pilePower, order.power) }}kW</span>
        </div>
        <span class="order-card__detail-separator">|</span>
        <div class="order-card__detail-item">
          <span>{{ formatDuration(order.duration, order.actualStartTime, order.endTime) }}</span>
        </div>
        <span class="order-card__detail-separator">|</span>
        <div class="order-card__detail-item">
          <span>{{ order.power || order.energy || 0 }}kWh</span>
        </div>
      </div>
    </div>

    <!-- 卡片底部：金额 + 操作按钮 -->
    <div class="order-card__footer">
      <div class="order-card__amount">
        <span class="order-card__amount-label">实付金额：</span>
        <span class="order-card__amount-value">¥{{ formatAmount(getActualAmount()) }}</span>
      </div>
      <div class="order-card__actions">
        <!-- 充电中：查看详情、结束充电 -->
        <template v-if="order.status === 1">
          <el-button text @click="handleViewDetail">查看详情</el-button>
          <el-button type="primary" @click="handleStopCharging">结束充电</el-button>
        </template>
        
        <!-- 已预约（type=1 且 status=0）：取消预约、查看详情 -->
        <template v-else-if="order.type === 1 && order.status === 0">
          <el-button text @click="handleViewDetail">查看详情</el-button>
          <el-button type="primary" @click="handleCancelReservation">取消预约</el-button>
        </template>
        
        <!-- 已完成：查看详情、评价 -->
        <template v-else-if="order.status === 2">
          <el-button text @click="handleViewDetail">查看详情</el-button>
          <el-button type="primary" @click="handleEvaluate">评价</el-button>
        </template>
        
        <!-- 已取消：查看详情、删除订单 -->
        <template v-else-if="order.status === 3">
          <el-button text @click="handleViewDetail">查看详情</el-button>
          <el-button type="danger" @click="handleDelete">删除订单</el-button>
        </template>
        
        <!-- 待支付：查看详情、去支付 -->
        <template v-else-if="order.status === 0">
          <el-button text @click="handleViewDetail">查看详情</el-button>
          <el-button type="primary" @click="handlePay">去支付</el-button>
        </template>
        
        <!-- 默认：查看详情 -->
        <template v-else>
          <el-button text @click="handleViewDetail">查看详情</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Location, Lightning } from '@element-plus/icons-vue'

const props = defineProps({
  order: {
    type: Object,
    required: true,
    default: () => ({
      id: null,
      orderNo: '',
      status: 0,
      type: 0, // 0-即时充电 1-预约充电
      createTime: '',
      stationName: '',
      stationAddress: '',
      pileType: 1, // 1-快充 2-慢充
      power: 0,
      duration: '',
      energy: 0,
      totalAmount: 0
    })
  }
})

const emit = defineEmits([
  'view-detail',
  'stop-charging',
  'cancel-reservation',
  'evaluate',
  'delete',
  'pay'
])

// 获取状态标签样式类
const getStatusTagClass = (status) => {
  const classMap = {
    0: 'order-card__status-tag--pending',      // 待支付
    1: 'order-card__status-tag--charging',     // 进行中
    2: 'order-card__status-tag--completed',    // 已完成
    3: 'order-card__status-tag--cancelled',    // 已取消
    4: 'order-card__status-tag--refunded'      // 已退款
  }
  return classMap[status] || 'order-card__status-tag--default'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '待支付',
    1: '充电中',
    2: '已完成',
    3: '已取消',
    4: '已退款'
  }
  return textMap[status] || '未知'
}

// 获取枪型文本
const getPileTypeText = (pileType) => {
  // pileType: 0-慢充 1-快充
  if (pileType === undefined || pileType === null) {
    return '快充' // 默认
  }
  return pileType === 1 ? '快充' : '慢充'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  // 如果是字符串，直接返回；如果是日期对象，格式化
  if (typeof time === 'string') {
    return time.replace('T', ' ').substring(0, 16)
  }
  return time
}

// 格式化金额
const formatAmount = (amount) => {
  // 处理 null、undefined、空字符串等情况
  if (amount === null || amount === undefined || amount === '') {
    return '0.00'
  }
  
  // 转换为数字
  const num = Number(amount)
  
  // 检查是否为有效数字
  if (isNaN(num)) {
    return '0.00'
  }
  
  // 确保金额不为负数（显示时）
  const positiveAmount = Math.max(0, num)
  
  // 格式化为两位小数
  return positiveAmount.toFixed(2)
}

// 计算实付金额（总金额 - 优惠金额）
const getActualAmount = () => {
  const totalAmount = Number(props.order.totalAmount) || 0
  const discountAmount = Number(props.order.discountAmount) || 0
  return Math.max(0, totalAmount - discountAmount)
}

// 格式化功率
const formatPower = (pilePower, power) => {
  const p = pilePower || power
  if (!p && p !== 0) return '--'
  return Number(p).toFixed(1)
}

// 格式化充电时长
const formatDuration = (duration, startTime, endTime) => {
  if (duration) return duration
  
  // 如果有开始和结束时间，计算时长
  if (startTime && endTime) {
    try {
      const start = new Date(startTime)
      const end = new Date(endTime)
      if (isNaN(start.getTime()) || isNaN(end.getTime())) {
        return '--'
      }
      const diff = Math.floor((end - start) / 1000 / 60) // 分钟数
      
      if (diff < 0) return '--'
      if (diff < 60) {
        return `${diff}分钟`
      } else {
        const hours = Math.floor(diff / 60)
        const minutes = diff % 60
        return minutes > 0 ? `${hours}小时${minutes}分钟` : `${hours}小时`
      }
    } catch (e) {
      return '--'
    }
  }
  
  // 如果只有开始时间，计算到现在的时间
  if (startTime && !endTime) {
    try {
      const start = new Date(startTime)
      if (isNaN(start.getTime())) {
        return '--'
      }
      const now = new Date()
      const diff = Math.floor((now - start) / 1000 / 60) // 分钟数
      
      if (diff < 0) return '--'
      if (diff < 60) {
        return `${diff}分钟`
      } else {
        const hours = Math.floor(diff / 60)
        const minutes = diff % 60
        return minutes > 0 ? `${hours}小时${minutes}分钟` : `${hours}小时`
      }
    } catch (e) {
      return '--'
    }
  }
  
  return '--'
}

// 事件处理
const handleViewDetail = () => {
  emit('view-detail', props.order.id)
}

const handleStopCharging = () => {
  emit('stop-charging', props.order.id)
}

const handleCancelReservation = () => {
  emit('cancel-reservation', props.order.id)
}

const handleEvaluate = () => {
  emit('evaluate', props.order.id)
}

const handleDelete = () => {
  emit('delete', props.order.id)
}

const handlePay = () => {
  emit('pay', props.order.id)
}
</script>

<style scoped>
.order-card {
  width: 100%;
  padding: 20px 24px;
  margin-bottom: 16px;
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeIn 0.3s ease-out;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片头部 */
.order-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E0E0E0;
}

.order-card__status-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.5;
  border: none;
}

/* 状态标签颜色 */
.order-card__status-tag--pending {
  background: rgba(255, 193, 7, 0.1);
  color: #FFC107;
}

.order-card__status-tag--charging {
  background: rgba(255, 152, 0, 0.1);
  color: #FF9800;
}

.order-card__status-tag--completed {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.order-card__status-tag--cancelled {
  background: rgba(158, 158, 158, 0.1);
  color: #9E9E9E;
}

.order-card__status-tag--refunded {
  background: rgba(244, 67, 54, 0.1);
  color: #F44336;
}

.order-card__status-tag--default {
  background: rgba(33, 150, 243, 0.1);
  color: #2196F3;
}

.order-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #757575;
}

.order-card__order-no {
  color: #757575;
}

.order-card__separator {
  color: #BDBDBD;
  margin: 0 4px;
}

.order-card__time {
  color: #757575;
}

/* 卡片主体 */
.order-card__body {
  margin-bottom: 16px;
}

/* 充电站信息 */
.order-card__station-info {
  margin-bottom: 16px;
}

.order-card__station-name {
  font-size: 18px;
  font-weight: 600;
  color: #212121;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.order-card__station-address {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #9E9E9E;
  line-height: 1.5;
}

.order-card__station-address .el-icon {
  font-size: 14px;
  color: #9E9E9E;
}

/* 充电详情 */
.order-card__charging-details {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid #E0E0E0;
  border-bottom: 1px solid #E0E0E0;
  font-size: 14px;
  color: #424242;
  flex-wrap: wrap;
}

.order-card__detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.order-card__detail-item .el-icon {
  font-size: 16px;
  color: #2196F3;
}

.order-card__detail-separator {
  color: #BDBDBD;
  margin: 0 4px;
}

/* 卡片底部 */
.order-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E0E0E0;
}

.order-card__amount {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.order-card__amount-label {
  font-size: 13px;
  color: #757575;
}

.order-card__amount-value {
  font-size: 20px;
  font-weight: 600;
  color: #FF9800;
}

.order-card__actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.order-card__actions .el-button {
  height: 36px;
  padding: 0 16px;
  font-size: 14px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.order-card__actions .el-button:active {
  transform: scale(0.98);
}

/* 响应式设计 */
@media (max-width: 767px) {
  .order-card {
    padding: 16px;
    margin-bottom: 12px;
  }

  .order-card__header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .order-card__charging-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .order-card__detail-separator {
    display: none;
  }

  .order-card__footer {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .order-card__actions {
    flex-direction: column;
    width: 100%;
  }

  .order-card__actions .el-button {
    width: 100%;
  }
}
</style>

