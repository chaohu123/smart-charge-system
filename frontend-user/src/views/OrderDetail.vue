<template>
  <div class="order-detail">
    <!-- 页面头部 -->
    <div class="order-detail__header">
      <el-button 
        class="order-detail__back-button" 
        text 
        @click="handleGoBack"
      >
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
      
      <h1 class="order-detail__title">订单详情</h1>
      
      <div class="order-detail__order-no">
        订单号：{{ order.orderNo || '--' }}
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="order-detail__loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 订单内容 -->
    <div v-else-if="order.id" class="order-detail__content">
      <!-- 1. 订单状态概览卡 -->
      <div class="order-detail__status-card">
        <div class="status-card__icon" :class="getStatusIconClass(order.status)">
          <el-icon :size="48">
            <component :is="getStatusIcon(order.status)" />
          </el-icon>
        </div>
        <div class="status-card__content">
          <h2 class="status-card__title">{{ getStatusText(order.status) }}</h2>
          <p class="status-card__desc">{{ getStatusDesc(order.status) }}</p>
          <p class="status-card__time">下单时间：{{ formatTime(order.createTime) }}</p>
        </div>
      </div>

      <!-- 2. 实时充电监控区（仅充电中显示） -->
      <div v-if="order.status === 1" class="order-detail__charging-monitor">
        <div class="charging-monitor__data">
          <div class="data-item">
            <div class="data-item__label">已充电量</div>
            <div class="data-item__value">{{ formatPower(order.power || 0) }}<span class="data-item__unit">kWh</span></div>
          </div>
          <div class="data-item">
            <div class="data-item__label">当前功率</div>
            <div class="data-item__value">{{ formatPower(currentPower) }}<span class="data-item__unit">kW</span></div>
          </div>
          <div class="data-item">
            <div class="data-item__label">已用时长</div>
            <div class="data-item__value">{{ formatDuration(order.actualStartTime, order.endTime) }}</div>
          </div>
          <div class="data-item">
            <div class="data-item__label">预计剩余</div>
            <div class="data-item__value">{{ estimatedRemainingTime }}</div>
          </div>
        </div>
        <div class="charging-monitor__progress">
          <el-progress
            type="circle"
            :percentage="chargingProgress"
            :width="120"
            :stroke-width="8"
            color="#2196F3"
          >
            <template #default="{ percentage }">
              <span class="progress-text">{{ percentage }}%</span>
            </template>
          </el-progress>
          <p class="progress-desc">充电进度</p>
        </div>
      </div>

      <!-- 3. 预约信息区（已预约状态显示） -->
      <div v-if="order.type === 1 && order.status === 0" class="order-detail__reservation-info">
        <div class="info-card">
          <h3 class="info-card__title">预约信息</h3>
          <div class="info-card__content">
            <div class="info-item">
              <span class="info-item__label">预约时间段</span>
              <span class="info-item__value">{{ formatTimeRange(order.startTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-item__label">枪型</span>
              <span class="info-item__value">{{ getPileTypeText(order.pileType) }}</span>
            </div>
            <div class="info-item">
              <span class="info-item__label">最大功率</span>
              <span class="info-item__value">{{ formatPower(order.pilePower || 0) }}kW</span>
            </div>
            <div class="info-item info-item--warning">
              <el-icon><Warning /></el-icon>
              <span>请在预约时间前15分钟到达，超时未到达将自动取消预约</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. 充电站与设备信息 -->
      <div class="order-detail__station-info">
        <div class="info-card">
          <h3 class="info-card__title">充电站信息</h3>
          <div class="info-card__content">
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <div class="info-item__content">
                <div class="info-item__name">{{ order.stationName || '充电站名称' }}</div>
                <div class="info-item__address">{{ order.stationAddress || '地址信息待完善' }}</div>
              </div>
            </div>
            <div class="info-item">
              <el-icon><Lightning /></el-icon>
              <div class="info-item__content">
                <div class="info-item__label">枪编号</div>
                <div class="info-item__value">{{ order.pileNumber || '--' }}</div>
              </div>
            </div>
            <div class="info-item">
              <el-icon><Setting /></el-icon>
              <div class="info-item__content">
                <div class="info-item__label">枪型</div>
                <div class="info-item__value">{{ getPileTypeText(order.pileType) }}</div>
              </div>
            </div>
            <div class="info-item">
              <el-icon><DataLine /></el-icon>
              <div class="info-item__content">
                <div class="info-item__label">最大功率</div>
                <div class="info-item__value">{{ formatPower(order.pilePower || 0) }}kW</div>
              </div>
            </div>
            <div class="info-card__actions">
              <el-button @click="handleViewMap" type="primary" plain>
                <el-icon><MapLocation /></el-icon>
                查看地图
              </el-button>
              <el-button @click="handleContactStation" plain>
                <el-icon><Phone /></el-icon>
                联系站点
              </el-button>
            </div>
          </div>
        </div>

        <div class="info-card">
          <h3 class="info-card__title">车辆信息</h3>
          <div class="info-card__content">
            <div class="info-item">
              <el-icon><Van /></el-icon>
              <div class="info-item__content">
                <div class="info-item__label">车牌号</div>
                <div class="info-item__value">{{ vehicleInfo?.plateNumber || '—' }}</div>
              </div>
            </div>
            <div class="info-item">
              <el-icon><UserFilled /></el-icon>
              <div class="info-item__content">
                <div class="info-item__label">车型</div>
                <div class="info-item__value">{{ vehicleInfo ? `${vehicleInfo.brand || ''} ${vehicleInfo.model || ''}` : '—' }}</div>
              </div>
            </div>
            <div class="info-item" v-if="vehicleInfo?.batteryCapacity">
              <el-icon><DataLine /></el-icon>
              <div class="info-item__content">
                <div class="info-item__label">电池容量</div>
                <div class="info-item__value">{{ vehicleInfo.batteryCapacity }}kWh</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 5. 费用明细区 -->
      <div class="order-detail__fee-info">
        <div class="info-card">
          <h3 class="info-card__title">费用明细</h3>
          <div class="info-card__content">
            <div class="fee-list">
              <div class="fee-item">
                <span class="fee-item__label">电费</span>
                <span class="fee-item__value">¥{{ formatAmount(calculateElectricityFee()) }}</span>
              </div>
              <div class="fee-item">
                <span class="fee-item__label">服务费</span>
                <span class="fee-item__value">¥{{ formatAmount(calculateServiceFee()) }}</span>
              </div>
              <div v-if="order.discountAmount && order.discountAmount > 0" class="fee-item fee-item--discount">
                <span class="fee-item__label">优惠</span>
                <span class="fee-item__value">-¥{{ formatAmount(order.discountAmount) }}</span>
              </div>
              <div v-if="order.status === 2 || order.status === 1" class="fee-item">
                <span class="fee-item__label">充电服务时长</span>
                <span class="fee-item__value">{{ formatServiceDuration() }}</span>
              </div>
            </div>
            <div class="fee-total">
              <span class="fee-total__label">实付金额</span>
              <span class="fee-total__value">¥{{ formatAmount(getActualAmount()) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 6. 操作按钮区 -->
      <div class="order-detail__actions">
        <!-- 充电中：结束充电 -->
        <template v-if="order.status === 1">
          <el-button type="primary" size="large" @click="handleStopCharging">
            <el-icon><SwitchButton /></el-icon>
            结束充电
          </el-button>
        </template>
        
        <!-- 已完成：评价、申请发票 -->
        <template v-else-if="order.status === 2">
          <el-button type="primary" size="large" @click="handleEvaluate">
            <el-icon><Star /></el-icon>
            评价
          </el-button>
          <el-button size="large" @click="handleApplyInvoice">
            <el-icon><Document /></el-icon>
            申请发票
          </el-button>
        </template>
        
        <!-- 已预约：取消预约 -->
        <template v-else-if="order.type === 1 && order.status === 0">
          <el-button type="warning" size="large" @click="handleCancelReservation">
            <el-icon><Close /></el-icon>
            取消预约
          </el-button>
        </template>
        
        <!-- 已取消：删除订单 -->
        <template v-else-if="order.status === 3">
          <el-button type="danger" size="large" @click="handleDelete">
            <el-icon><Delete /></el-icon>
            删除订单
          </el-button>
        </template>
        
        <!-- 待支付：去支付 -->
        <template v-else-if="order.status === 0">
          <el-button type="primary" size="large" @click="handlePay">
            <el-icon><CreditCard /></el-icon>
            去支付
          </el-button>
        </template>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="order-detail__error">
      <el-empty description="订单不存在">
        <el-button type="primary" @click="handleGoBack">返回订单列表</el-button>
      </el-empty>
    </div>

    <!-- 地图对话框 -->
    <el-dialog
      v-model="mapDialogVisible"
      title="查看地图"
      width="90%"
      :before-close="handleMapDialogClose"
      class="map-dialog"
    >
      <div class="map-dialog__content">
        <div :id="`order-map-container-${order.id || 'default'}`" class="map-dialog__map" ref="mapContainerRef"></div>
        <div class="map-dialog__info">
          <div class="map-info-card">
            <h4>{{ order.stationName || '充电站' }}</h4>
            <p class="map-info-address">
              <el-icon><Location /></el-icon>
              {{ order.stationAddress || '地址信息待完善' }}
            </p>
            <div class="map-info-actions">
              <el-button type="primary" @click="handleStartNavigation">
                <el-icon><Guide /></el-icon>
                开始导航
              </el-button>
            </div>
          </div>
          <div v-if="showNavigationRoute" class="navigation-route">
            <h5>导航路线</h5>
            <div class="route-info">
              <div class="route-item">
                <span class="route-label">距离：</span>
                <span class="route-value">{{ navigationDistance || '计算中...' }}</span>
              </div>
              <div class="route-item">
                <span class="route-label">预计时间：</span>
                <span class="route-value">{{ navigationDuration || '计算中...' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 站点电话对话框 -->
    <el-dialog
      v-model="phoneDialogVisible"
      title="联系站点"
      width="400px"
      align-center
    >
      <div class="phone-dialog__content">
        <div class="phone-dialog__station">
          <h4>{{ order.stationName || '充电站' }}</h4>
          <p class="phone-dialog__address">{{ order.stationAddress || '地址信息待完善' }}</p>
        </div>
        <div class="phone-dialog__phone">
          <el-icon class="phone-icon"><Phone /></el-icon>
          <span class="phone-number">{{ order.servicePhone || '暂无联系电话' }}</span>
        </div>
        <div class="phone-dialog__actions">
          <el-button 
            v-if="order.servicePhone" 
            type="primary" 
            size="large"
            @click="handleCallPhone"
          >
            <el-icon><Phone /></el-icon>
            拨打电话
          </el-button>
          <el-button 
            v-if="order.servicePhone" 
            size="large"
            @click="handleCopyPhone"
          >
            <el-icon><DocumentCopy /></el-icon>
            复制号码
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="evaluationDialogVisible"
      title="评价充电站"
      width="600px"
      :close-on-click-modal="false"
      @close="handleEvaluationDialogClose"
    >
      <el-form
        ref="evaluationFormRef"
        :model="evaluationForm"
        :rules="evaluationRules"
        label-width="120px"
      >
        <el-form-item label="总体评分" prop="score">
          <el-rate v-model="evaluationForm.score" :max="5" />
        </el-form-item>
        <el-form-item label="环境评分" prop="environmentScore">
          <el-rate v-model="evaluationForm.environmentScore" :max="5" />
        </el-form-item>
        <el-form-item label="服务评分" prop="serviceScore">
          <el-rate v-model="evaluationForm.serviceScore" :max="5" />
        </el-form-item>
        <el-form-item label="设备评分" prop="equipmentScore">
          <el-rate v-model="evaluationForm.equipmentScore" :max="5" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="evaluationForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容"
          />
        </el-form-item>
        <el-form-item label="评价图片">
          <el-upload
            :http-request="handleEvaluationUpload"
            list-type="picture-card"
            :on-remove="handleEvaluationRemove"
            :file-list="evaluationImageList"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="evaluationLoading" @click="handleSubmitEvaluation">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, stopCharging, cancelOrder, getOrderStatus } from '@/api/order'
import { getStationDetail, getStationPiles } from '@/api/station'
import { getVehicleList } from '@/api/user'
import { createEvaluation, uploadEvaluationImage } from '@/api/evaluation'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Loading,
  Location,
  Lightning,
  Setting,
  DataLine,
  MapLocation,
  Phone,
  SwitchButton,
  Star,
  Document,
  Close,
  Delete,
  CreditCard,
  Warning,
  Clock,
  CircleCheck,
  CircleClose,
  Refresh,
  Guide,
  DocumentCopy,
  Plus
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const order = ref({})
const vehicleInfo = ref(null)
const loading = ref(false)
const currentPower = ref(0) // 当前功率（实时更新）
const statusTimer = ref(null) // 状态轮询定时器

// 地图对话框相关
const mapDialogVisible = ref(false)
const mapContainerRef = ref(null)
const showNavigationRoute = ref(false)
const navigationDistance = ref('')
const navigationDuration = ref('')
let mapInstance = null
let navigationMarker = null
let navigationRoute = null

// 站点电话对话框
const phoneDialogVisible = ref(false)

// 评价对话框相关
const evaluationDialogVisible = ref(false)
const evaluationFormRef = ref(null)
const evaluationLoading = ref(false)
const evaluationForm = ref({
  stationId: null,
  orderId: null,
  score: 5,
  environmentScore: 5,
  serviceScore: 5,
  equipmentScore: 5,
  content: ''
})
const evaluationRules = {
  score: [{ required: true, message: '请选择总体评分', trigger: 'change' }],
  content: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
}
const evaluationImageList = ref([])
const evaluationUploadedImages = ref([])

// 计算充电进度
const chargingProgress = computed(() => {
  if (order.value.status !== 1) return 0
  // 这里可以根据实际需求计算进度，暂时使用电量估算
  const power = Number(order.value.power) || 0
  const maxPower = Number(order.value.pilePower) || 60
  return Math.min(100, Math.floor((power / maxPower) * 100))
})

// 预计剩余时间
const estimatedRemainingTime = computed(() => {
  if (order.value.status !== 1) return '--'
  // 简化计算，实际应该根据充电功率和剩余电量计算
  const power = Number(order.value.power) || 0
  const currentPowerValue = currentPower.value || 1
  const remaining = Math.max(0, (60 - power) / currentPowerValue)
  if (remaining < 1) return '不足1小时'
  return `${Math.ceil(remaining)}小时`
})

onMounted(() => {
  loadOrderDetail()
})

onUnmounted(() => {
  if (statusTimer.value) {
    clearInterval(statusTimer.value)
  }
})

// 加载订单详情
const loadOrderDetail = async () => {
  const orderId = route.params.id
  if (!orderId) {
    ElMessage.error('订单ID不存在')
    return
  }

  loading.value = true
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200 && res.data) {
      const orderData = res.data
      
      // 处理字段映射
      const enrichedOrder = {
        ...orderData,
        orderNo: orderData.orderNo || orderData.order_no,
        createTime: orderData.createTime || orderData.create_time,
        actualStartTime: orderData.actualStartTime || orderData.actual_start_time,
        endTime: orderData.endTime || orderData.end_time,
        startTime: orderData.startTime || orderData.start_time,
        totalAmount: Number(orderData.totalAmount || orderData.total_amount || 0),
        discountAmount: Number(orderData.discountAmount || orderData.discount_amount || 0),
        serviceFee: Number(orderData.serviceFee || orderData.service_fee || 0),
        price: Number(orderData.price || 0),
        power: Number(orderData.power || 0),
        stationId: orderData.stationId || orderData.station_id,
        pileId: orderData.pileId || orderData.pile_id
      }
      
      // 补充充电站信息（需要在计算金额前获取服务费单价）
      if (enrichedOrder.stationId && (!enrichedOrder.stationName || !enrichedOrder.stationAddress)) {
        await enrichStationInfo(enrichedOrder)
      }
      
      // 重新计算金额（确保金额正确）
      // 电费 = 电价 × 电量
      const electricityFee = (enrichedOrder.price || 0) * (enrichedOrder.power || 0)
      
      // 服务费：优先使用后端返回的服务费，如果为0则按电量计算
      let serviceFee = enrichedOrder.serviceFee || 0
      if (serviceFee <= 0 && enrichedOrder.power > 0 && enrichedOrder.stationServiceFee) {
        // 如果服务费为0，使用充电站服务费单价 × 电量
        serviceFee = (enrichedOrder.stationServiceFee || 0) * (enrichedOrder.power || 0)
        enrichedOrder.serviceFee = serviceFee
      }
      
      // 总金额 = 电费 + 服务费
      const calculatedTotalAmount = electricityFee + serviceFee
      
      // 如果后端返回的 totalAmount 与计算值差异很大，使用计算值
      const backendTotalAmount = enrichedOrder.totalAmount || 0
      if (backendTotalAmount > 0 && Math.abs(backendTotalAmount - calculatedTotalAmount) > 0.01) {
        // 如果差异超过0.01，可能是后端计算有误，使用前端计算值
        enrichedOrder.totalAmount = calculatedTotalAmount
      } else if (backendTotalAmount <= 0 && calculatedTotalAmount > 0) {
        // 如果后端没有返回总金额，使用计算值
        enrichedOrder.totalAmount = calculatedTotalAmount
      }
      
      // 补充充电桩信息
      if (enrichedOrder.pileId && (!enrichedOrder.pileType || !enrichedOrder.pilePower)) {
        await enrichPileInfo(enrichedOrder)
      }
      
      order.value = enrichedOrder

      // 如果有车辆ID参数，加载车辆信息
      const vehicleId = route.query.vehicleId
      if (vehicleId) {
        try {
          const vehicleRes = await getVehicleList()
          if (vehicleRes.code === 200 && Array.isArray(vehicleRes.data)) {
            vehicleInfo.value = vehicleRes.data.find(v => String(v.id) === String(vehicleId)) || null
          }
        } catch (e) {
          console.error('加载车辆信息失败', e)
        }
      }
      
      // 如果订单是充电中状态，开始轮询状态
      if (enrichedOrder.status === 1) {
        startStatusPolling(orderId)
      }
    } else {
      ElMessage.error('订单不存在')
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 补充充电站信息
const enrichStationInfo = async (orderData) => {
  try {
    const res = await getStationDetail(orderData.stationId)
    if (res.code === 200 && res.data) {
      orderData.stationName = res.data.name
      orderData.stationAddress = res.data.address
      orderData.stationLongitude = res.data.longitude
      orderData.stationLatitude = res.data.latitude
      orderData.servicePhone = res.data.servicePhone
      orderData.stationServiceFee = res.data.serviceFee // 充电站服务费单价
    }
  } catch (error) {
    console.error('获取充电站信息失败:', error)
  }
}

// 补充充电桩信息
const enrichPileInfo = async (orderData) => {
  try {
    const res = await getStationPiles(orderData.stationId)
    if (res.code === 200 && res.data) {
      const pile = Array.isArray(res.data) 
        ? res.data.find(p => p.id === orderData.pileId)
        : null
      if (pile) {
        orderData.pileType = pile.type
        orderData.pilePower = pile.power
        orderData.pileNumber = pile.pileNumber || pile.pile_number
      }
    }
  } catch (error) {
    console.error('获取充电桩信息失败:', error)
  }
}

// 开始状态轮询（充电中时）
const startStatusPolling = (orderId) => {
  if (statusTimer.value) {
    clearInterval(statusTimer.value)
  }
  
  statusTimer.value = setInterval(async () => {
    try {
      const res = await getOrderStatus(orderId)
      if (res.code === 200 && res.data) {
        // 更新订单状态和电量
        if (res.data.power) {
          order.value.power = Number(res.data.power)
        }
        if (res.data.status !== undefined) {
          order.value.status = res.data.status
          // 如果状态不再是充电中，停止轮询
          if (res.data.status !== 1) {
            clearInterval(statusTimer.value)
            statusTimer.value = null
          }
        }
      }
    } catch (error) {
      console.error('获取订单状态失败:', error)
    }
  }, 5000) // 每5秒轮询一次
}

// 获取状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    0: 'Clock',      // 待支付
    1: 'Lightning', // 充电中
    2: 'CircleCheck', // 已完成
    3: 'CircleClose', // 已取消
    4: 'Refresh'     // 已退款
  }
  return iconMap[status] || 'Document'
}

// 获取状态图标样式类
const getStatusIconClass = (status) => {
  const classMap = {
    0: 'status-icon--pending',
    1: 'status-icon--charging',
    2: 'status-icon--completed',
    3: 'status-icon--cancelled',
    4: 'status-icon--refunded'
  }
  return classMap[status] || 'status-icon--default'
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

// 获取状态描述
const getStatusDesc = (status) => {
  const descMap = {
    0: '请尽快完成支付，超时订单将自动取消',
    1: '正在充电中，请保持连接',
    2: '充电已完成，感谢使用',
    3: '订单已取消',
    4: '订单已退款'
  }
  return descMap[status] || ''
}

// 获取枪型文本
const getPileTypeText = (pileType) => {
  if (pileType === undefined || pileType === null) return '快充'
  return pileType === 1 ? '快充' : '慢充'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  if (typeof time === 'string') {
    return time.replace('T', ' ').substring(0, 16)
  }
  return time
}

// 格式化时间范围
const formatTimeRange = (startTime) => {
  if (!startTime) return '--'
  const start = new Date(startTime)
  const end = new Date(start.getTime() + 2 * 60 * 60 * 1000) // 默认2小时
  return `${formatTime(startTime)} - ${formatTime(end.toISOString())}`
}

// 格式化金额
const formatAmount = (amount) => {
  if (amount === null || amount === undefined || amount === '') return '0.00'
  const num = Number(amount)
  if (isNaN(num)) return '0.00'
  return Math.max(0, num).toFixed(2)
}

// 格式化功率
const formatPower = (power) => {
  if (power === null || power === undefined || power === '') return '0.0'
  const num = Number(power)
  if (isNaN(num)) return '0.0'
  return num.toFixed(1)
}

// 格式化时长
const formatDuration = (startTime, endTime) => {
  if (!startTime) return '--'
  
  const start = new Date(startTime)
  const end = endTime ? new Date(endTime) : new Date()
  
  if (isNaN(start.getTime())) return '--'
  
  const diff = Math.floor((end - start) / 1000 / 60) // 分钟数
  
  if (diff < 0) return '--'
  if (diff < 60) {
    return `${diff}分钟`
  } else {
    const hours = Math.floor(diff / 60)
    const minutes = diff % 60
    return minutes > 0 ? `${hours}小时${minutes}分钟` : `${hours}小时`
  }
}

// 计算电费（电价 × 电量）
const calculateElectricityFee = () => {
  const price = Number(order.value.price) || 0
  const power = Number(order.value.power) || 0
  if (price <= 0 || power <= 0) return 0
  return price * power
}

// 计算服务费（可能是总服务费，也可能是按电量计算）
const calculateServiceFee = () => {
  // 如果后端返回了服务费，直接使用
  if (order.value.serviceFee && Number(order.value.serviceFee) > 0) {
    return Number(order.value.serviceFee)
  }
  
  // 如果没有服务费，尝试从充电站信息获取服务费单价
  // 服务费可能是按电量计算的：服务费单价 × 电量
  const serviceFeeRate = Number(order.value.stationServiceFee) || 0
  const power = Number(order.value.power) || 0
  if (serviceFeeRate > 0 && power > 0) {
    return serviceFeeRate * power
  }
  
  return 0
}

// 计算总金额（电费 + 服务费）
const calculateTotalAmount = () => {
  const electricityFee = calculateElectricityFee()
  const serviceFee = calculateServiceFee()
  return electricityFee + serviceFee
}

// 计算实付金额（总金额 - 优惠金额）
const calculateActualAmount = () => {
  const totalAmount = calculateTotalAmount()
  const discountAmount = Number(order.value.discountAmount) || 0
  return Math.max(0, totalAmount - discountAmount)
}

// 获取实付金额（优先使用后端返回的，如果不存在或为0则计算）
const getActualAmount = () => {
  // 如果后端返回了 totalAmount 且大于0，优先使用
  const backendTotalAmount = Number(order.value.totalAmount) || 0
  if (backendTotalAmount > 0) {
    const discountAmount = Number(order.value.discountAmount) || 0
    return Math.max(0, backendTotalAmount - discountAmount)
  }
  
  // 否则使用计算的值
  return calculateActualAmount()
}

// 返回上一页
const handleGoBack = () => {
  router.push('/orders')
}

// 查看地图
const handleViewMap = () => {
  if (!order.value.stationId || !order.value.stationLongitude || !order.value.stationLatitude) {
    ElMessage.warning('充电站位置信息不完整')
    return
  }
  mapDialogVisible.value = true
  // 等待对话框打开后再初始化地图
  setTimeout(() => {
    initMapDialog()
  }, 300)
}

// 初始化地图对话框
const initMapDialog = () => {
  if (!mapContainerRef.value) return
  
  // 检查是否已加载高德地图SDK
  if (typeof window.AMap === 'undefined') {
    ElMessage.warning('地图SDK未加载，请检查配置')
    return
  }
  
  try {
    // 清除旧地图
    if (mapInstance) {
      mapInstance.destroy()
      mapInstance = null
    }
    
    const center = [Number(order.value.stationLongitude), Number(order.value.stationLatitude)]
    const mapId = `order-map-container-${order.value.id || 'default'}`
    
    mapInstance = new window.AMap.Map(mapId, {
      zoom: 15,
      center: center,
      mapStyle: 'amap://styles/normal'
    })
    
    // 添加充电站标记
    if (navigationMarker) {
      mapInstance.remove(navigationMarker)
    }
    
    navigationMarker = new window.AMap.Marker({
      position: center,
      title: order.value.stationName,
      icon: new window.AMap.Icon({
        size: new window.AMap.Size(40, 40),
        image: 'https://webapi.amap.com/theme/v1.3/markers/n/mid_r.png',
        imageSize: new window.AMap.Size(40, 40)
      })
    })
    
    mapInstance.add(navigationMarker)
    
    // 获取当前位置并规划路线
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { longitude, latitude } = position.coords
          planRoute([longitude, latitude], center)
        },
        () => {
          ElMessage.warning('获取当前位置失败，无法规划路线')
        }
      )
    }
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败')
  }
}

// 规划导航路线
const planRoute = (start, end) => {
  if (!mapInstance || typeof window.AMap === 'undefined') return
  
  try {
    // 使用高德地图路径规划
    const driving = new window.AMap.Driving({
      map: mapInstance,
      panel: null,
      hideMarkers: false
    })
    
    driving.search(
      new window.AMap.LngLat(start[0], start[1]),
      new window.AMap.LngLat(end[0], end[1]),
      (status, result) => {
        if (status === 'complete' && result.routes && result.routes.length > 0) {
          const route = result.routes[0]
          navigationDistance.value = formatDistance(route.distance)
          navigationDuration.value = formatDurationFromSeconds(route.time)
          showNavigationRoute.value = true
          
          // 保存路线对象，用于后续导航
          navigationRoute = route
        } else {
          ElMessage.warning('路线规划失败')
        }
      }
    )
  } catch (error) {
    console.error('路线规划失败:', error)
  }
}

// 格式化距离
const formatDistance = (distance) => {
  if (!distance) return '未知'
  if (distance < 1000) {
    return `${distance}米`
  }
  return `${(distance / 1000).toFixed(1)}公里`
}

// 格式化时长（从秒数）
const formatDurationFromSeconds = (seconds) => {
  if (!seconds) return '未知'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
}

// 开始导航
const handleStartNavigation = () => {
  if (!order.value.stationLongitude || !order.value.stationLatitude) {
    ElMessage.warning('充电站位置信息不完整')
    return
  }
  
  // 使用高德地图导航
  const lng = Number(order.value.stationLongitude)
  const lat = Number(order.value.stationLatitude)
  const name = order.value.stationName || '充电站'
  
  // 打开高德地图导航
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { longitude, latitude } = position.coords
        // 使用高德地图导航URL
        const url = `https://uri.amap.com/navigation?to=${lng},${lat},${name}&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=1`
        window.open(url, '_blank')
        ElMessage.success('已打开导航')
      },
      () => {
        // 如果无法获取当前位置，直接导航到目标点
        const url = `https://uri.amap.com/navigation?to=${lng},${lat},${name}&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=1`
        window.open(url, '_blank')
        ElMessage.success('已打开导航')
      }
    )
  } else {
    const url = `https://uri.amap.com/navigation?to=${lng},${lat},${name}&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=1`
    window.open(url, '_blank')
    ElMessage.success('已打开导航')
  }
}

// 关闭地图对话框
const handleMapDialogClose = () => {
  mapDialogVisible.value = false
  showNavigationRoute.value = false
  navigationDistance.value = ''
  navigationDuration.value = ''
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
  if (navigationMarker) {
    navigationMarker = null
  }
  if (navigationRoute) {
    navigationRoute = null
  }
}

// 联系站点
const handleContactStation = () => {
  phoneDialogVisible.value = true
}

// 拨打电话
const handleCallPhone = () => {
  if (order.value.servicePhone) {
    window.location.href = `tel:${order.value.servicePhone}`
  }
}

// 复制电话号码
const handleCopyPhone = async () => {
  if (order.value.servicePhone) {
    try {
      await navigator.clipboard.writeText(order.value.servicePhone)
      ElMessage.success('电话号码已复制到剪贴板')
    } catch (error) {
      // 降级方案
      const input = document.createElement('input')
      input.value = order.value.servicePhone
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
      ElMessage.success('电话号码已复制到剪贴板')
    }
  }
}

// 格式化充电服务时长
const formatServiceDuration = () => {
  if (order.value.status === 1) {
    // 充电中：从开始时间到现在
    return formatDuration(order.value.actualStartTime, null)
  } else if (order.value.status === 2) {
    // 已完成：从开始时间到结束时间
    return formatDuration(order.value.actualStartTime, order.value.endTime)
  }
  return '--'
}

// 结束充电
const handleStopCharging = async () => {
  try {
    await ElMessageBox.confirm('确定要结束充电吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await stopCharging(order.value.id)
    if (res.code === 200) {
      ElMessage.success('已停止充电')
      // 停止轮询
      if (statusTimer.value) {
        clearInterval(statusTimer.value)
        statusTimer.value = null
      }
      router.push({ path: '/payment', query: { orderId: order.value.id } })
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('停止充电失败')
    }
  }
}

// 取消预约
const handleCancelReservation = async () => {
  try {
    await ElMessageBox.confirm('确定要取消预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await cancelOrder(order.value.id)
    if (res.code === 200) {
      ElMessage.success('已取消预约')
      loadOrderDetail()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消预约失败')
    }
  }
}

// 评价
const handleEvaluate = () => {
  // 重置表单
  evaluationForm.value = {
    stationId: order.value.stationId,
    orderId: order.value.id,
    score: 5,
    environmentScore: 5,
    serviceScore: 5,
    equipmentScore: 5,
    content: ''
  }
  evaluationImageList.value = []
  evaluationUploadedImages.value = []
  
  // 打开对话框
  evaluationDialogVisible.value = true
}

// 评价图片上传
const handleEvaluationUpload = async (options) => {
  try {
    const res = await uploadEvaluationImage(options.file)
    if (res.code === 200) {
      evaluationUploadedImages.value.push(res.data)
      evaluationImageList.value.push({
        uid: options.file.uid,
        name: options.file.name,
        url: res.data,
        response: res
      })
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error(res.message || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

// 移除评价图片
const handleEvaluationRemove = (file) => {
  const url = file.response?.data || file.url
  const index = evaluationUploadedImages.value.findIndex(img => img === url)
  if (index > -1) {
    evaluationUploadedImages.value.splice(index, 1)
  }
}

// 提交评价
const handleSubmitEvaluation = async () => {
  if (!evaluationFormRef.value) return
  
  await evaluationFormRef.value.validate(async (valid) => {
    if (valid) {
      evaluationLoading.value = true
      try {
        const evaluationData = {
          ...evaluationForm.value,
          images: evaluationUploadedImages.value.join(',')
        }
        const res = await createEvaluation(evaluationData)
        if (res.code === 200) {
          ElMessage.success('评价成功')
          evaluationDialogVisible.value = false
          // 重新加载订单详情
          loadOrderDetail()
        } else {
          ElMessage.error(res.message || '评价失败')
        }
      } catch (error) {
        console.error('评价失败:', error)
        ElMessage.error('评价失败')
      } finally {
        evaluationLoading.value = false
      }
    }
  })
}

// 关闭评价对话框
const handleEvaluationDialogClose = () => {
  // 重置表单
  if (evaluationFormRef.value) {
    evaluationFormRef.value.resetFields()
  }
  evaluationForm.value = {
    stationId: null,
    orderId: null,
    score: 5,
    environmentScore: 5,
    serviceScore: 5,
    equipmentScore: 5,
    content: ''
  }
  evaluationImageList.value = []
  evaluationUploadedImages.value = []
}

// 申请发票
const handleApplyInvoice = () => {
  router.push({
    path: '/invoice/apply',
    query: { orderId: order.value.id }
  })
}

// 删除订单
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    ElMessage.success('已删除订单')
    handleGoBack()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除订单失败')
    }
  }
}

// 去支付
const handlePay = () => {
  router.push({
    path: '/payment',
    query: { orderId: order.value.id }
  })
}
</script>

<style scoped>
.order-detail {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 24px;
}

/* 页面头部 */
.order-detail__header {
  background: #FFFFFF;
  padding: 20px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.order-detail__back-button {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #212121;
  font-size: 14px;
}

.order-detail__back-button:hover {
  color: #2196F3;
}

.order-detail__title {
  flex: 1;
  font-size: 20px;
  font-weight: 600;
  color: #212121;
  margin: 0;
}

.order-detail__order-no {
  font-size: 14px;
  color: #757575;
}

/* 加载状态 */
.order-detail__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #757575;
}

.order-detail__loading .el-icon {
  font-size: 32px;
  margin-bottom: 16px;
}

/* 订单内容 */
.order-detail__content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 订单状态概览卡 */
.order-detail__status-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 24px;
}

.status-card__icon {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-icon--pending {
  background: rgba(255, 193, 7, 0.1);
  color: #FFC107;
}

.status-icon--charging {
  background: rgba(255, 152, 0, 0.1);
  color: #FF9800;
  animation: pulse 2s infinite;
}

.status-icon--completed {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.status-icon--cancelled {
  background: rgba(158, 158, 158, 0.1);
  color: #9E9E9E;
}

.status-icon--refunded {
  background: rgba(244, 67, 54, 0.1);
  color: #F44336;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.status-card__content {
  flex: 1;
}

.status-card__title {
  font-size: 28px;
  font-weight: 600;
  color: #212121;
  margin: 0 0 8px 0;
}

.status-card__desc {
  font-size: 14px;
  color: #757575;
  margin: 0 0 12px 0;
}

.status-card__time {
  font-size: 13px;
  color: #9E9E9E;
  margin: 0;
}

/* 实时充电监控区 */
.order-detail__charging-monitor {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 48px;
  align-items: center;
}

.charging-monitor__data {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.data-item {
  text-align: center;
}

.data-item__label {
  font-size: 14px;
  color: #757575;
  margin-bottom: 8px;
}

.data-item__value {
  font-size: 24px;
  font-weight: 600;
  color: #212121;
}

.data-item__unit {
  font-size: 16px;
  font-weight: 400;
  color: #757575;
  margin-left: 4px;
}

.charging-monitor__progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.progress-text {
  font-size: 20px;
  font-weight: 600;
  color: #2196F3;
}

.progress-desc {
  font-size: 14px;
  color: #757575;
  margin: 0;
}

/* 信息卡片通用样式 */
.info-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-card__title {
  font-size: 18px;
  font-weight: 600;
  color: #212121;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #E0E0E0;
}

.info-card__content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
}

.info-item__label {
  color: #757575;
  min-width: 80px;
}

.info-item__value {
  color: #212121;
  font-weight: 500;
}

.info-item__name {
  font-size: 16px;
  font-weight: 600;
  color: #212121;
  margin-bottom: 4px;
}

.info-item__address {
  font-size: 13px;
  color: #757575;
}

.info-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .el-icon {
  color: #2196F3;
  font-size: 18px;
  margin-top: 2px;
}

.info-item--warning {
  padding: 12px;
  background: rgba(255, 193, 7, 0.1);
  border-radius: 8px;
  color: #FF9800;
  align-items: center;
}

.info-item--warning .el-icon {
  color: #FF9800;
}

.info-card__actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E0E0E0;
}

/* 费用明细 */
.fee-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.fee-item__label {
  color: #757575;
}

.fee-item__value {
  color: #212121;
  font-weight: 500;
}

.fee-item--discount .fee-item__value {
  color: #4CAF50;
}

.fee-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 2px solid #E0E0E0;
}

.fee-total__label {
  font-size: 16px;
  font-weight: 600;
  color: #212121;
}

.fee-total__value {
  font-size: 24px;
  font-weight: 600;
  color: #FF9800;
}

/* 操作按钮区 */
.order-detail__actions {
  display: flex;
  gap: 12px;
  padding: 24px;
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.order-detail__actions .el-button {
  flex: 1;
}

/* 错误状态 */
.order-detail__error {
  padding: 80px 20px;
}

/* 地图对话框样式 */
.map-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.map-dialog__content {
  display: flex;
  flex-direction: column;
  height: 70vh;
  min-height: 500px;
}

.map-dialog__map {
  flex: 1;
  width: 100%;
  min-height: 400px;
}

.map-dialog__info {
  padding: 16px;
  background: #FFFFFF;
  border-top: 1px solid #E0E0E0;
}

.map-info-card h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.map-info-address {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #757575;
  margin: 0 0 16px 0;
}

.map-info-actions {
  margin-bottom: 16px;
}

.map-info-actions .el-button {
  width: 100%;
}

.navigation-route {
  padding-top: 16px;
  border-top: 1px solid #E0E0E0;
}

.navigation-route h5 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #212121;
}

.route-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.route-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.route-label {
  color: #757575;
}

.route-value {
  color: #212121;
  font-weight: 500;
}

/* 电话对话框样式 */
.phone-dialog__content {
  text-align: center;
  padding: 20px 0;
}

.phone-dialog__station {
  margin-bottom: 24px;
}

.phone-dialog__station h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.phone-dialog__address {
  font-size: 14px;
  color: #757575;
  margin: 0;
}

.phone-dialog__phone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 24px;
  background: #F5F5F5;
  border-radius: 12px;
}

.phone-icon {
  font-size: 48px;
  color: #2196F3;
}

.phone-number {
  font-size: 24px;
  font-weight: 600;
  color: #212121;
}

.phone-dialog__actions {
  display: flex;
  gap: 12px;
}

.phone-dialog__actions .el-button {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 767px) {
  .order-detail__header {
    padding: 16px;
    flex-wrap: wrap;
  }

  .order-detail__title {
    font-size: 18px;
    order: 2;
    width: 100%;
    text-align: center;
  }

  .order-detail__order-no {
    order: 3;
    width: 100%;
    text-align: center;
    font-size: 12px;
  }

  .order-detail__content {
    padding: 16px;
  }

  .order-detail__status-card {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }

  .status-card__icon {
    width: 80px;
    height: 80px;
  }

  .status-card__title {
    font-size: 24px;
  }

  .order-detail__charging-monitor {
    flex-direction: column;
    padding: 24px;
  }

  .charging-monitor__data {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .data-item__value {
    font-size: 20px;
  }

  .info-card {
    padding: 20px;
  }

  .info-card__actions {
    flex-direction: column;
  }

  .info-card__actions .el-button {
    width: 100%;
  }

  .order-detail__actions {
    flex-direction: column;
    padding: 20px;
  }

  .order-detail__actions .el-button {
    width: 100%;
  }

  .map-dialog__content {
    height: 60vh;
    min-height: 400px;
  }

  .phone-dialog__actions {
    flex-direction: column;
  }

  .phone-dialog__actions .el-button {
    width: 100%;
  }
}

/* 评价对话框样式 */
.order-detail :deep(.el-dialog__body) {
  padding: 20px;
}

.order-detail :deep(.el-form-item) {
  margin-bottom: 20px;
}

.order-detail :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

.order-detail :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>

