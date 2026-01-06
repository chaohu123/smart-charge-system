<template>
  <div class="station-detail">
    <el-container>
      <el-header>
        <div class="header-left">
          <el-button @click="$router.back()">返回</el-button>
          <h2>{{ station?.name || '充电站详情' }}</h2>
        </div>
        <div v-if="userStore.token" class="header-right-menu">
          <el-button
            text
            class="header-btn"
            @click="$router.push('/orders')"
          >
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-button>
          <el-button
            text
            class="header-btn"
            @click="$router.push('/reservation-history')"
          >
            <el-icon><Calendar /></el-icon>
            <span>我的预约</span>
          </el-button>
          <el-button
            text
            class="header-btn"
            @click="$router.push('/wallet')"
          >
            <el-icon><CreditCard /></el-icon>
            <span>钱包</span>
          </el-button>
          <el-dropdown trigger="hover">
            <div class="user-section">
              <el-avatar :src="userStore.userInfo?.avatar" :size="36">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.phone }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <div v-if="loading" class="loading-wrapper">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <el-card v-else-if="station">
          <div class="station-header">
            <h3 class="station-name">{{ station.name }}</h3>
            <div class="station-info">
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span class="info-label">地址：</span>
                <span class="info-value">{{ station.address || '地址信息待完善' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Phone /></el-icon>
                <span class="info-label">联系电话：</span>
                <span class="info-value">{{ station.servicePhone || '暂无联系电话' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span class="info-label">营业时间：</span>
                <span class="info-value">{{ station.businessHours || '24小时营业' }}</span>
              </div>
            </div>
          </div>
          
          <div class="piles-section">
            <h4>充电桩列表</h4>
            <el-table :data="piles" style="width: 100%">
              <el-table-column prop="pileNumber" label="桩号" />
              <el-table-column prop="power" label="功率(kW)" />
              <el-table-column prop="type" label="类型">
                <template #default="scope">
                  {{ scope.row.type === 1 ? '快充' : '慢充' }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status, scope.row.isReserved)">
                    {{ getStatusText(scope.row.status, scope.row.isReserved) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <div class="action-buttons">
                    <el-button
                      v-if="scope.row.status === 0"
                      type="primary"
                      size="small"
                      @click="handleReserve(scope.row)"
                    >
                      预约
                    </el-button>
                    <el-button
                      v-if="scope.row.status === 0"
                      type="success"
                      size="small"
                      @click="handleStartCharging(scope.row)"
                    >
                      立即充电
                    </el-button>
                    <span v-if="scope.row.status !== 0" class="no-action">—</span>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="station-actions">
            <el-button
              v-if="userStore.token"
              :type="isFavorite ? 'danger' : 'primary'"
              @click="handleFavorite"
            >
              {{ isFavorite ? '取消收藏' : '收藏' }}
            </el-button>
            <el-button
              v-if="userStore.token"
              type="success"
              @click="handleEvaluate"
            >
              评价
            </el-button>
            <el-button
              v-if="userStore.token"
              type="warning"
              @click="handleReportFault"
            >
              故障上报
            </el-button>
          </div>

          <div class="evaluations-section">
            <h4>用户评价 (平均评分: {{ avgScore.toFixed(1) }})</h4>
            <EmptyState v-if="evaluations.length === 0" description="暂无用户评价" />
            <div v-else v-for="evaluation in evaluations" :key="evaluation.id" class="evaluation-item">
              <div class="evaluation-header">
                <el-avatar :src="evaluation.userAvatar || evaluation.avatar || ''" size="small">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="user-nickname">{{ (evaluation.userNickname && evaluation.userNickname.trim()) || (evaluation.nickname && evaluation.nickname.trim()) || evaluation.userPhone || evaluation.phone || '匿名用户' }}</span>
                <el-rate v-model="evaluation.score" disabled class="evaluation-score" />
              </div>
              <p class="evaluation-content">{{ evaluation.content }}</p>
              <div v-if="evaluation.images" class="evaluation-images">
                <el-image
                  v-for="(img, index) in evaluation.images.split(',')"
                  :key="index"
                  :src="img"
                  :preview-src-list="evaluation.images.split(',')"
                  fit="cover"
                  class="evaluation-image"
                />
              </div>
              <div class="evaluation-footer">
                <span class="eval-time">{{ evaluation.createTime }}</span>
                <div class="evaluation-actions">
                  <!-- 点赞按钮：对所有评价显示（如果已登录） -->
                  <div v-if="userStore.token && userStore.userInfo" class="like-wrapper" @click="handleLikeEvaluation(evaluation)">
                    <svg
                      class="like-icon"
                      :class="{ active: evaluation.isLiked }"
                      viewBox="0 0 24 24"
                      width="22"
                      height="22"
                    >
                      <path
                        d="M2 21h4V9H2v12zM22 10c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32
                        c0-.41-.17-.79-.44-1.06L13 1 6.59 7.41C6.22 7.78 6 8.3 6 8.83V19
                        c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05
                        c.09-.23.14-.47.14-.73v-2z"
                      />
                    </svg>
                    <div class="like-info">
                      <span class="like-text">{{ evaluation.isLiked ? '已赞' : '点赞' }}</span>
                      <span class="like-count">{{ evaluation.likeCount || 0 }}</span>
                    </div>
                  </div>
                  <!-- 删除按钮：只对自己发表的评价显示 -->
                  <el-button
                    v-if="userStore.token && userStore.userInfo && evaluation.userId === userStore.userInfo.id"
                    type="danger"
                    size="small"
                    text
                    @click="handleDeleteEvaluation(evaluation)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-main>
    </el-container>

    <!-- 预约弹窗 -->
    <el-dialog
      v-model="reservationDialogVisible"
      title="预约充电"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="reservationFormRef"
        :model="reservationForm"
        :rules="reservationRules"
        label-width="100px"
      >
        <el-form-item label="充电桩">
          <el-input :value="selectedPile?.pileNumber || ''" disabled />
        </el-form-item>
        <el-form-item label="功率">
          <el-input :value="`${selectedPile?.power || 0}kW (${selectedPile?.type === 1 ? '快充' : '慢充'})`" disabled />
        </el-form-item>
        <el-form-item label="预约开始时间" prop="startTime">
          <el-date-picker
            v-model="reservationForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            :disabled-time="disabledTime"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预约时长" prop="duration">
          <el-select v-model="reservationForm.duration" style="width: 100%">
            <el-option label="30分钟" :value="30" />
            <el-option label="1小时" :value="60" />
            <el-option label="2小时" :value="120" />
            <el-option label="3小时" :value="180" />
          </el-select>
        </el-form-item>
        <el-form-item label="结束时间">
          <el-input :value="endTimeText" disabled />
        </el-form-item>
        <el-form-item v-if="reservedTimeSlots.length > 0" label="已预约时间段">
          <div style="max-height: 150px; overflow-y: auto;">
            <div
              v-for="(slot, index) in reservedTimeSlots"
              :key="index"
              style="padding: 4px 0; font-size: 12px; color: #909399;"
            >
              {{ formatDateTimeDisplay(slot.startTime) }} - {{ formatDateTimeDisplay(slot.endTime) }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="预计费用">
          <span class="fee-text">约 ¥{{ estimatedFee.toFixed(2) }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reservationDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reservationLoading" @click="handleSubmitReservation">
          确认预约
        </el-button>
      </template>
    </el-dialog>

    <!-- 付款弹窗 -->
    <el-dialog
      v-model="paymentDialogVisible"
      title="支付订单"
      width="450px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="payment-form">
        <div class="payment-item">
          <span class="payment-label">订单号：</span>
          <span class="payment-value">{{ currentOrder?.orderNo || '—' }}</span>
        </div>
        <div class="payment-item">
          <span class="payment-label">订单创建时间：</span>
          <span class="payment-value">{{ orderCreateTimeText }}</span>
        </div>
        <div class="payment-amount">
          <div class="amount-label">支付金额</div>
          <div class="amount-value">¥{{ orderAmount.toFixed(2) }}</div>
        </div>
        <div class="payment-qrcode">
          <div class="qrcode-placeholder">
            <el-icon class="qrcode-icon"><Picture /></el-icon>
            <div class="qrcode-text">支付二维码</div>
            <div class="qrcode-hint">请使用微信/支付宝扫码支付</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="handlePaymentConfirm">确认支付</el-button>
        <el-button @click="handlePaymentCancel">取消</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog
      v-model="evaluationDialogVisible"
      title="评价充电站"
      width="600px"
      :close-on-click-modal="false"
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

    <!-- 故障上报弹窗 -->
    <el-dialog
      v-model="faultDialogVisible"
      title="故障上报"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="faultFormRef"
        :model="faultForm"
        :rules="faultRules"
        label-width="120px"
      >
        <el-form-item label="充电站">
          <el-input :value="station?.name || ''" disabled />
        </el-form-item>
        <el-form-item label="充电桩">
          <el-select v-model="faultForm.pileId" placeholder="请选择充电桩（可选）" clearable style="width: 100%">
            <el-option
              v-for="pile in piles"
              :key="pile.id"
              :label="pile.pileNumber"
              :value="pile.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型" prop="faultType">
          <el-select v-model="faultForm.faultType" placeholder="请选择故障类型" style="width: 100%">
            <el-option label="无法启动" value="无法启动" />
            <el-option label="充电中断" value="充电中断" />
            <el-option label="显示异常" value="显示异常" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="description">
          <el-input
            v-model="faultForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述故障情况"
          />
        </el-form-item>
        <el-form-item label="故障图片">
          <el-upload
            :http-request="handleFaultUpload"
            list-type="picture-card"
            :on-remove="handleFaultRemove"
            :file-list="faultImageList"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="faultDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="faultLoading" @click="handleSubmitFault">
          提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 车辆选择对话框 -->
    <el-dialog
      v-model="vehicleDialogVisible"
      title="选择充电车辆"
      width="480px"
    >
      <el-radio-group v-model="selectedVehicleId" class="vehicle-list">
        <el-radio
          v-for="v in vehicles"
          :key="v.id"
          :label="v.id"
          border
          style="width: 100%; margin-bottom: 8px;"
        >
          <div class="vehicle-item">
            <div class="vehicle-main">
              <span class="plate">{{ v.plateNumber }}</span>
              <span class="model">{{ v.brand }} {{ v.model }}</span>
            </div>
            <div class="vehicle-extra">
              <span v-if="v.batteryCapacity">电池：{{ v.batteryCapacity }}kWh</span>
              <el-tag v-if="v.isDefault === 1" size="small" type="success" style="margin-left: 8px;">
                默认
              </el-tag>
            </div>
          </div>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="vehicleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmVehicle">开始充电</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStationDetail, getStationPiles, getStationEvaluations } from '@/api/station'
import { startCharging, cancelOrder } from '@/api/order'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import { createReservation, getPileReservations } from '@/api/reservation'
import { likeEvaluation, unlikeEvaluation, checkLikeStatus, getEvaluationLikeCount, createEvaluation, uploadEvaluationImage, deleteEvaluation } from '@/api/evaluation'
import { reportFault } from '@/api/fault'
import { useUserStore } from '@/stores/user'
import { getVehicleList } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, Loading, Location, Phone, Clock, Picture, Plus, User, Document, Calendar, CreditCard } from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const station = ref(null)
const piles = ref([])
const vehicles = ref([])
const vehicleDialogVisible = ref(false)
const selectedVehicleId = ref(null)
const evaluations = ref([])
const avgScore = ref(0)
const isFavorite = ref(false)
const loading = ref(true)

// 预约相关
const reservationDialogVisible = ref(false)
const reservationFormRef = ref(null)
const selectedPile = ref(null)
const reservationLoading = ref(false)
const reservationForm = ref({
  startTime: '',
  duration: 60
})
const reservedTimeSlots = ref([]) // 已预约的时间段列表

const reservationRules = {
  startTime: [
    { required: true, message: '请选择预约开始时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请选择预约时长', trigger: 'change' }
  ]
}

// 付款相关
const paymentDialogVisible = ref(false)
const currentOrder = ref(null)
const orderAmount = ref(0)

// 评价相关状态
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

// 故障上报相关状态
const faultDialogVisible = ref(false)
const faultFormRef = ref(null)
const faultLoading = ref(false)
const faultForm = ref({
  stationId: null,
  pileId: null,
  faultType: '',
  description: ''
})
const faultRules = {
  faultType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
}
const faultImageList = ref([])
const faultImageFiles = ref([])

onMounted(async () => {
  const stationId = route.params.id
  if (!stationId) {
    ElMessage.error('充电站ID无效')
    router.back()
    return
  }
  
  // 如果已登录但用户信息未加载，先加载用户信息
  if (userStore.token && !userStore.userInfo) {
    await userStore.getUserInfoAction()
  }
  
  await loadStationDetail(stationId)
  if (station.value) {
    await Promise.all([
      loadPiles(stationId),
      loadEvaluations(stationId),
      userStore.token ? checkFavoriteStatus(stationId) : Promise.resolve()
    ])
    // 启动预约过期检查
    startReservationCheck()
  }
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (reservationCheckTimer) {
    clearInterval(reservationCheckTimer)
    reservationCheckTimer = null
  }
})

const checkFavoriteStatus = async (stationId) => {
  try {
    const res = await checkFavorite(stationId)
    if (res.code === 200) {
      // 兼容不同的返回格式
      isFavorite.value = res.data?.isFavorite ?? res.data ?? false
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    isFavorite.value = false
  }
}

const loadStationDetail = async (id) => {
  try {
    const res = await getStationDetail(id)
    if (res.code === 200 && res.data) {
      station.value = res.data
    } else {
      ElMessage.error('充电站不存在')
      router.back()
    }
  } catch (error) {
    console.error('加载充电站信息失败:', error)
    ElMessage.error('加载充电站信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const loadPiles = async (id) => {
  try {
    const res = await getStationPiles(id)
    if (res.code === 200) {
      piles.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载充电桩列表失败')
  }
}

const loadEvaluations = async (id) => {
  try {
    const res = await getStationEvaluations(id, { current: 1, size: 10 })
    if (res && res.code === 200 && res.data) {
      // 安全地获取记录数组
      let records = []
      if (Array.isArray(res.data.records)) {
        records = res.data.records
      } else if (Array.isArray(res.data)) {
        records = res.data
      }
      
      // 调试：打印原始数据
      console.log('评价原始数据:', records)
      
      // 确保每个评价都是对象，并使用安全的展开运算符
      evaluations.value = records
        .filter(item => item && typeof item === 'object' && !Array.isArray(item))
        .map(evaluation => {
          try {
            // 调试：打印单个评价数据
            console.log('单个评价数据:', evaluation)
            
            // 设置默认值，确保不会默认显示为已赞
            // 直接使用后端返回的所有字段，包括userNickname
            const processedEvaluation = {
              ...evaluation,
              isLiked: false, // 明确设置为false，避免默认显示为已赞
              likeCount: 0,
              // 确保userId被正确传递
              userId: evaluation.userId,
              // 保留后端返回的所有用户信息字段，不要覆盖
              // 确保userNickname被正确设置，如果后端返回了就用后端的值
              // 注意：不要使用 || null，因为空字符串也会被转换为null
              userNickname: evaluation.userNickname,
              userAvatar: evaluation.userAvatar || '',
              userPhone: evaluation.userPhone || ''
            }
            
            // 调试：打印处理后的评价数据，特别关注userNickname
            console.log('处理后的评价数据:', processedEvaluation)
            console.log('userNickname值:', processedEvaluation.userNickname, '类型:', typeof processedEvaluation.userNickname)
            
            return processedEvaluation
          } catch (e) {
            console.warn('评价数据格式错误:', evaluation, e)
            return null
          }
        })
        .filter(item => item !== null)
      
      if (evaluations.value.length > 0) {
        const sum = evaluations.value.reduce((acc, e) => acc + (Number(e.score) || 0), 0)
        avgScore.value = sum / evaluations.value.length
        
        // 加载每个评价的点赞状态和数量
        // 对所有评价都加载点赞数量，但只对非自己发表的评价加载点赞状态
        if (userStore.token) {
          // 确保userInfo已加载
          if (!userStore.userInfo) {
            await userStore.getUserInfoAction()
          }
          
          const currentUserId = userStore.userInfo?.id
          
          // 并行加载所有评价的点赞数量
          const countPromises = evaluations.value
            .filter(evaluation => evaluation && evaluation.id)
            .map(evaluation => getEvaluationLikeCount(evaluation.id).catch(() => ({ code: 500, data: 0 })))
          
          const countResults = await Promise.all(countPromises)
          countResults.forEach((res, index) => {
            if (res && res.code === 200 && res.data !== undefined) {
              const count = res.data.count !== undefined ? res.data.count : (typeof res.data === 'number' ? res.data : 0)
              evaluations.value[index].likeCount = Number(count) || 0
            }
          })
          
          // 加载所有评价的点赞状态（包括自己发表的）
          if (currentUserId) {
            const statusPromises = evaluations.value
              .filter(evaluation => evaluation && evaluation.id)
              .map(evaluation => checkLikeStatus(evaluation.id).catch(() => ({ code: 500, data: { isLiked: false } })))
            
            const statusResults = await Promise.all(statusPromises)
            let statusIndex = 0
            evaluations.value.forEach((evaluation, index) => {
              if (evaluation && evaluation.id) {
                const res = statusResults[statusIndex++]
                if (res && res.code === 200 && res.data) {
                  const isLiked = res.data.isLiked === true || res.data.isLiked === 'true'
                  evaluations.value[index].isLiked = Boolean(isLiked)
                } else {
                  // 确保默认是false
                  evaluations.value[index].isLiked = false
                }
              }
            })
          }
        }
      } else {
        avgScore.value = 0
      }
    } else {
      evaluations.value = []
      avgScore.value = 0
    }
  } catch (error) {
    console.error('加载评价失败:', error)
    evaluations.value = []
    avgScore.value = 0
    // 只在非网络错误时显示错误消息
    if (error.message && !error.message.includes('Network')) {
      ElMessage.error('加载评价失败')
    }
  }
}

const loadEvaluationLikeInfo = async (evaluation) => {
  if (!evaluation || !evaluation.id) {
    return
  }
  
  try {
    const [statusRes, countRes] = await Promise.all([
      checkLikeStatus(evaluation.id).catch(() => ({ code: 500, data: null })),
      getEvaluationLikeCount(evaluation.id).catch(() => ({ code: 500, data: null }))
    ])
    
    if (statusRes && statusRes.code === 200 && statusRes.data) {
      const isLiked = statusRes.data.isLiked || statusRes.data || false
      // 使用索引更新，确保响应式
      const index = evaluations.value.findIndex(e => e.id === evaluation.id)
      if (index !== -1) {
        evaluations.value[index].isLiked = Boolean(isLiked)
      }
    }
    
    if (countRes && countRes.code === 200 && countRes.data !== undefined) {
      const count = countRes.data.count !== undefined ? countRes.data.count : (typeof countRes.data === 'number' ? countRes.data : 0)
      // 使用索引更新，确保响应式
      const index = evaluations.value.findIndex(e => e.id === evaluation.id)
      if (index !== -1) {
        evaluations.value[index].likeCount = Number(count) || 0
      }
    }
  } catch (error) {
    console.error('加载点赞信息失败:', error)
    // 不显示错误消息，避免干扰用户体验
  }
}

const handleLikeEvaluation = async (evaluation) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!evaluation || !evaluation.id) {
    ElMessage.error('评价信息不完整')
    return
  }
  
  try {
    const index = evaluations.value.findIndex(e => e.id === evaluation.id)
    if (index === -1) {
      ElMessage.error('评价不存在')
      return
    }
    
    const currentIsLiked = evaluations.value[index].isLiked
    
    if (currentIsLiked) {
      // 取消点赞
      try {
        const res = await unlikeEvaluation(evaluation.id)
        if (res.code === 200) {
          evaluations.value[index].isLiked = false
          evaluations.value[index].likeCount = Math.max(0, (evaluations.value[index].likeCount || 0) - 1)
          ElMessage.success('取消点赞成功')
        } else {
          // 如果后端返回"未找到点赞记录"，说明状态不同步，刷新状态
          if (res.message && res.message.includes('未找到点赞记录')) {
            // 直接设置为false并刷新点赞数
            evaluations.value[index].isLiked = false
            const countRes = await getEvaluationLikeCount(evaluation.id).catch(() => ({ code: 500, data: 0 }))
            if (countRes.code === 200 && countRes.data !== undefined) {
              const count = countRes.data.count !== undefined ? countRes.data.count : (typeof countRes.data === 'number' ? countRes.data : 0)
              evaluations.value[index].likeCount = Number(count) || 0
            }
          } else {
            ElMessage.error(res.message || '取消点赞失败')
          }
        }
      } catch (error) {
        // 如果错误是"未找到点赞记录"，刷新状态
        if (error.message && error.message.includes('未找到点赞记录')) {
          evaluations.value[index].isLiked = false
          const countRes = await getEvaluationLikeCount(evaluation.id).catch(() => ({ code: 500, data: 0 }))
          if (countRes.code === 200 && countRes.data !== undefined) {
            const count = countRes.data.count !== undefined ? countRes.data.count : (typeof countRes.data === 'number' ? countRes.data : 0)
            evaluations.value[index].likeCount = Number(count) || 0
          }
        } else {
          throw error
        }
      }
    } else {
      // 点赞
      const res = await likeEvaluation(evaluation.id)
      if (res.code === 200) {
        evaluations.value[index].isLiked = true
        evaluations.value[index].likeCount = (evaluations.value[index].likeCount || 0) + 1
        ElMessage.success('点赞成功')
      } else {
        // 如果后端返回"已经点过赞了"，说明状态不同步，刷新状态
        if (res.message && res.message.includes('已经点过赞了')) {
          evaluations.value[index].isLiked = true
          const countRes = await getEvaluationLikeCount(evaluation.id).catch(() => ({ code: 500, data: 0 }))
          if (countRes.code === 200 && countRes.data !== undefined) {
            const count = countRes.data.count !== undefined ? countRes.data.count : (typeof countRes.data === 'number' ? countRes.data : 0)
            evaluations.value[index].likeCount = Number(count) || 0
          }
        } else {
          ElMessage.error(res.message || '点赞失败')
        }
      }
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    // 如果是"未找到点赞记录"错误，不显示错误消息，只刷新状态
    if (error.message && error.message.includes('未找到点赞记录')) {
      const index = evaluations.value.findIndex(e => e.id === evaluation.id)
      if (index !== -1) {
        evaluations.value[index].isLiked = false
      }
    } else {
      ElMessage.error('操作失败')
    }
  }
}

// 删除评价
const handleDeleteEvaluation = async (evaluation) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!evaluation || !evaluation.id) {
    ElMessage.error('评价信息不完整')
    return
  }
  
  // 确认删除
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用删除API
    const res = await deleteEvaluation(evaluation.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 从列表中移除
      const index = evaluations.value.findIndex(e => e.id === evaluation.id)
      if (index !== -1) {
        evaluations.value.splice(index, 1)
        // 重新计算平均分
        if (evaluations.value.length > 0) {
          const sum = evaluations.value.reduce((acc, e) => acc + (Number(e.score) || 0), 0)
          avgScore.value = sum / evaluations.value.length
        } else {
          avgScore.value = 0
        }
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  }).catch(() => {})
}

const getStatusType = (status, isReserved) => {
  // 如果被预约，显示为 warning 类型
  if (isReserved) {
    return 'warning'
  }
  const types = { 0: 'success', 1: 'warning', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status, isReserved) => {
  // 如果被预约，显示为"被预约"
  if (isReserved) {
    return '被预约'
  }
  const texts = { 0: '空闲', 1: '占用', 2: '故障', 3: '离线' }
  return texts[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return null
  return new Date(dateTimeStr)
}

// 添加分钟数
const addMinutes = (dateTimeStr, minutes) => {
  if (!dateTimeStr) return null
  // 如果是字符串，先转换为 ISO 格式（如果包含空格）
  const isoStr = typeof dateTimeStr === 'string' ? dateTimeStr.replace(' ', 'T') : dateTimeStr
  const date = new Date(isoStr)
  if (isNaN(date.getTime())) {
    console.error('Invalid date:', dateTimeStr)
    return null
  }
  date.setMinutes(date.getMinutes() + minutes)
  return date
}

// 格式化日期时间字符串为 ISO 格式 (YYYY-MM-DDTHH:mm:ss)
const formatDateTimeStr = (date) => {
  if (!date) return ''
  const d = date instanceof Date ? date : new Date(date)
  if (isNaN(d.getTime())) {
    console.error('Invalid date:', date)
    return ''
  }
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  // 返回 ISO 格式 (YYYY-MM-DDTHH:mm:ss) 供后端 LocalDateTime.parse 使用
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

// 格式化日期时间显示
const formatDateTimeDisplay = (dateTimeStr) => {
  if (!dateTimeStr) return '—'
  const date = new Date(dateTimeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const handleReserve = async (pile) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  selectedPile.value = pile
  // 设置默认开始时间为当前时间后30分钟
  const now = new Date()
  now.setMinutes(now.getMinutes() + 30)
  reservationForm.value = {
    startTime: formatDateTimeStr(now),
    duration: 60
  }
  
  // 加载该充电桩的预约时间段
  await loadPileReservations(pile.id)
  
  reservationDialogVisible.value = true
}

const openVehicleDialog = async (pile) => {
  selectedPile.value = pile
  try {
    const res = await getVehicleList()
    if (res.code === 200 && Array.isArray(res.data)) {
      vehicles.value = res.data
    } else {
      vehicles.value = []
    }
  } catch (error) {
    console.error('加载车辆列表失败:', error)
    vehicles.value = []
  }

  if (!vehicles.value.length) {
    ElMessage.warning('请先在个人中心添加车辆')
    router.push('/profile')
    return
  }

  // 默认选中默认车辆或第一辆
  const defaultVehicle = vehicles.value.find(v => v.isDefault === 1) || vehicles.value[0]
  selectedVehicleId.value = defaultVehicle.id
  vehicleDialogVisible.value = true
}

const handleConfirmVehicle = async () => {
  if (!selectedPile.value || !selectedVehicleId.value) {
    ElMessage.warning('请选择车辆')
    return
  }
  vehicleDialogVisible.value = false

  try {
    const res = await startCharging(selectedPile.value.id)
    if (res.code === 200 && res.data) {
      currentOrder.value = { ...res.data, vehicleId: selectedVehicleId.value }
      ElMessage.success('已创建订单并开始计时')
      router.push({ path: `/order/${res.data.id}`, query: { vehicleId: selectedVehicleId.value } })
      if (station.value) {
        loadPiles(station.value.id)
      }
    } else {
      ElMessage.error(res.message || '启动充电失败')
    }
  } catch (error) {
    console.error('启动充电失败:', error)
    ElMessage.error('启动充电失败')
  }
}

// 禁用过去的日期
const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

// 解析营业时间字符串（如"06:00-24:00"或"24小时营业"）
const parseBusinessHours = (businessHours) => {
  if (!businessHours || businessHours === '24小时营业') {
    return { startHour: 0, startMinute: 0, endHour: 23, endMinute: 59 }
  }
  
  // 解析格式如 "06:00-24:00" 或 "06:00-23:59"
  const match = businessHours.match(/(\d{1,2}):(\d{2})-(\d{1,2}):(\d{2})/)
  if (match) {
    const startHour = parseInt(match[1], 10)
    const startMinute = parseInt(match[2], 10)
    let endHour = parseInt(match[3], 10)
    const endMinute = parseInt(match[4], 10)
    
    // 处理24:00的情况，表示到次日0:00，即当天的23:59
    if (endHour === 24) {
      endHour = 23
      return { startHour, startMinute, endHour, endMinute: 59 }
    }
    
    return { startHour, startMinute, endHour, endMinute }
  }
  
  // 默认24小时营业
  return { startHour: 0, startMinute: 0, endHour: 23, endMinute: 59 }
}

// 检查时间是否在营业时间内
const isWithinBusinessHours = (dateTime) => {
  if (!station.value || !station.value.businessHours) {
    return true // 如果没有营业时间信息，默认允许
  }
  
  const businessHours = parseBusinessHours(station.value.businessHours)
  const hour = dateTime.getHours()
  const minute = dateTime.getMinutes()
  
  // 转换为分钟数进行比较
  const timeInMinutes = hour * 60 + minute
  const startInMinutes = businessHours.startHour * 60 + businessHours.startMinute
  const endInMinutes = businessHours.endHour * 60 + businessHours.endMinute
  
  // 如果结束时间是24:00，表示到次日0:00，即当天的23:59:59
  // 检查原始营业时间字符串是否包含24:00
  if (station.value.businessHours.includes('24:00')) {
    // 24小时营业或到24:00，只要时间 >= 开始时间即可
    return timeInMinutes >= startInMinutes
  }
  
  return timeInMinutes >= startInMinutes && timeInMinutes <= endInMinutes
}

// 加载充电桩的预约时间段
const loadPileReservations = async (pileId) => {
  try {
    const res = await getPileReservations(pileId)
    if (res.code === 200 && res.data) {
      reservedTimeSlots.value = res.data.map(slot => ({
        startTime: new Date(slot.startTime),
        endTime: new Date(slot.endTime)
      }))
    } else {
      reservedTimeSlots.value = []
    }
  } catch (error) {
    console.error('加载预约时间段失败:', error)
    reservedTimeSlots.value = []
  }
}

// 禁用过去的时间
const disabledTime = (time) => {
  if (!reservationForm.value.startTime) return {}
  const selectedDate = new Date(reservationForm.value.startTime)
  const today = new Date()
  
  const disabledHours = []
  const disabledMinutes = {}
  
  // 解析营业时间
  const businessHours = station.value ? parseBusinessHours(station.value.businessHours) : null
  
  // 禁用营业时间之外的时间
  if (businessHours) {
    // 禁用营业开始时间之前的小时
    for (let h = 0; h < businessHours.startHour; h++) {
      if (!disabledHours.includes(h)) {
        disabledHours.push(h)
      }
    }
    
    // 禁用营业开始时间之前的分钟
    if (!disabledMinutes[businessHours.startHour]) {
      disabledMinutes[businessHours.startHour] = []
    }
    for (let m = 0; m < businessHours.startMinute; m++) {
      if (!disabledMinutes[businessHours.startHour].includes(m)) {
        disabledMinutes[businessHours.startHour].push(m)
      }
    }
    
    // 如果结束时间不是24:00，禁用营业结束时间之后的时间
    if (businessHours.endHour < 24) {
      // 禁用营业结束时间之后的小时
      for (let h = businessHours.endHour + 1; h < 24; h++) {
        if (!disabledHours.includes(h)) {
          disabledHours.push(h)
        }
      }
      
      // 禁用营业结束时间之后的分钟
      if (!disabledMinutes[businessHours.endHour]) {
        disabledMinutes[businessHours.endHour] = []
      }
      for (let m = businessHours.endMinute + 1; m < 60; m++) {
        if (!disabledMinutes[businessHours.endHour].includes(m)) {
          disabledMinutes[businessHours.endHour].push(m)
        }
      }
    }
  }
  
  // 禁用过去的时间
  if (selectedDate.toDateString() === today.toDateString()) {
    const currentHour = today.getHours()
    const currentMinute = today.getMinutes()
    
    for (let i = 0; i < currentHour; i++) {
      if (!disabledHours.includes(i)) {
        disabledHours.push(i)
      }
    }
    
    if (!disabledMinutes[currentHour]) {
      disabledMinutes[currentHour] = []
    }
    if (selectedDate.getHours() === currentHour) {
      for (let i = 0; i <= currentMinute; i++) {
        if (!disabledMinutes[currentHour].includes(i)) {
          disabledMinutes[currentHour].push(i)
        }
      }
    }
  }
  
  // 禁用已被预约的时间段
  const selectedDateStr = selectedDate.toDateString()
  reservedTimeSlots.value.forEach(slot => {
    const slotStart = new Date(slot.startTime)
    const slotEnd = new Date(slot.endTime)
    
    // 如果是同一天
    if (slotStart.toDateString() === selectedDateStr) {
      const slotStartHour = slotStart.getHours()
      const slotStartMinute = slotStart.getMinutes()
      const slotEndHour = slotEnd.getHours()
      const slotEndMinute = slotEnd.getMinutes()
      
      // 如果整个小时都在预约时间段内，禁用该小时
      if (slotStartHour === slotEndHour) {
        // 同一小时内的预约
        if (!disabledMinutes[slotStartHour]) {
          disabledMinutes[slotStartHour] = []
        }
        for (let m = slotStartMinute; m < slotEndMinute; m++) {
          if (!disabledMinutes[slotStartHour].includes(m)) {
            disabledMinutes[slotStartHour].push(m)
          }
        }
      } else {
        // 跨小时的预约
        // 禁用开始小时的后续分钟
        if (!disabledMinutes[slotStartHour]) {
          disabledMinutes[slotStartHour] = []
        }
        for (let m = slotStartMinute; m < 60; m++) {
          if (!disabledMinutes[slotStartHour].includes(m)) {
            disabledMinutes[slotStartHour].push(m)
          }
        }
        
        // 禁用中间的小时
        for (let h = slotStartHour + 1; h < slotEndHour; h++) {
          if (!disabledHours.includes(h)) {
            disabledHours.push(h)
          }
        }
        
        // 禁用结束小时的前续分钟
        if (!disabledMinutes[slotEndHour]) {
          disabledMinutes[slotEndHour] = []
        }
        for (let m = 0; m < slotEndMinute; m++) {
          if (!disabledMinutes[slotEndHour].includes(m)) {
            disabledMinutes[slotEndHour].push(m)
          }
        }
      }
    }
  })
  
  return {
    disabledHours: () => disabledHours,
    disabledMinutes: (selectedHour) => {
      return disabledMinutes[selectedHour] || []
    }
  }
}

// 计算结束时间
const endTimeText = computed(() => {
  if (!reservationForm.value.startTime || !reservationForm.value.duration) {
    return '—'
  }
  const endTime = addMinutes(reservationForm.value.startTime, reservationForm.value.duration)
  return formatDateTimeDisplay(endTime)
})

// 计算预计费用
const estimatedFee = computed(() => {
  if (!station.value || !selectedPile.value || !reservationForm.value.duration) {
    return 0
  }
  const price = Number(station.value.serviceFee || 0)
  const hours = reservationForm.value.duration / 60
  // 假设平均功率使用率70%
  const powerRate = selectedPile.value.power * 0.7
  const fee = price * powerRate * hours
  return Number.isNaN(fee) ? 0 : fee
})

// 提交预约
const handleSubmitReservation = async () => {
  if (!reservationFormRef.value) return
  
  await reservationFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!selectedPile.value || !station.value) {
      ElMessage.error('充电桩信息不完整')
      return
    }
    
    // 检查开始时间是否在未来
    // reservationForm.value.startTime 格式是 "YYYY-MM-DD HH:mm:ss"
    // 需要转换为 ISO 格式才能正确解析
    const startTimeStr = reservationForm.value.startTime.replace(' ', 'T')
    const startTime = new Date(startTimeStr)
    const now = new Date()
    if (startTime <= now) {
      ElMessage.warning('预约开始时间不能早于或等于当前时间')
      return
    }
    
    // 检查开始时间是否在营业时间内
    if (!isWithinBusinessHours(startTime)) {
      const businessHours = station.value?.businessHours || '24小时营业'
      ElMessage.warning(`预约开始时间不在营业时间内（营业时间：${businessHours}）`)
      return
    }
    
    // 计算结束时间并检查是否在营业时间内
    const endTime = addMinutes(startTime, reservationForm.value.duration)
    if (!isWithinBusinessHours(endTime)) {
      const businessHours = station.value?.businessHours || '24小时营业'
      ElMessage.warning(`预约结束时间不在营业时间内（营业时间：${businessHours}）`)
      return
    }
    
    // 检查整个预约时间段是否都在营业时间内
    // 如果预约跨天，需要特殊处理
    const businessHours = parseBusinessHours(station.value?.businessHours)
    const startHour = startTime.getHours()
    const startMinute = startTime.getMinutes()
    const endHour = endTime.getHours()
    const endMinute = endTime.getMinutes()
    
    // 如果营业时间不是24小时，需要检查预约时间段是否完全在营业时间内
    if (businessHours.endHour < 24) {
      const startInMinutes = startHour * 60 + startMinute
      const endInMinutes = endHour * 60 + endMinute
      const businessStartInMinutes = businessHours.startHour * 60 + businessHours.startMinute
      const businessEndInMinutes = businessHours.endHour * 60 + businessHours.endMinute
      
      // 如果预约跨天，需要检查
      if (startTime.toDateString() !== endTime.toDateString()) {
        // 跨天预约：开始时间必须在营业时间内，结束时间必须在次日的营业时间内
        if (startInMinutes < businessStartInMinutes || startInMinutes > businessEndInMinutes) {
          ElMessage.warning(`预约时间段超出营业时间（营业时间：${station.value?.businessHours || '24小时营业'}）`)
          return
        }
      } else {
        // 同一天：开始和结束时间都必须在营业时间内
        if (startInMinutes < businessStartInMinutes || endInMinutes > businessEndInMinutes) {
          ElMessage.warning(`预约时间段超出营业时间（营业时间：${station.value?.businessHours || '24小时营业'}）`)
          return
        }
      }
    }
    
    reservationLoading.value = true
    try {
      // 将 "YYYY-MM-DD HH:mm:ss" 格式转换为 Date 对象
      const startTimeDate = new Date(startTimeStr)
      const endTime = addMinutes(startTimeDate, reservationForm.value.duration)
      
      // 后端 LocalDateTime.parse 期望 ISO 格式 (YYYY-MM-DDTHH:mm:ss)
      const res = await createReservation({
        pileId: selectedPile.value.id,
        stationId: station.value.id,
        startTime: startTimeStr, // ISO 格式
        endTime: formatDateTimeStr(endTime), // ISO 格式
        duration: reservationForm.value.duration
      })
      
      if (res.code === 200) {
        ElMessage.success('预约成功')
        reservationDialogVisible.value = false
        // 重新加载充电桩列表，更新状态
        await loadPiles(station.value.id)
        // 清空预约时间段列表
        reservedTimeSlots.value = []
        // 启动定时检查过期预约
        startReservationCheck()
      } else {
        ElMessage.error(res.message || '预约失败')
      }
    } catch (error) {
      console.error('预约失败:', error)
      ElMessage.error('预约失败')
    } finally {
      reservationLoading.value = false
    }
  })
}

// 定时检查过期预约
let reservationCheckTimer = null
const startReservationCheck = () => {
  // 清除之前的定时器
  if (reservationCheckTimer) {
    clearInterval(reservationCheckTimer)
  }
  
  // 每30秒检查一次过期预约
  reservationCheckTimer = setInterval(async () => {
    if (!station.value || !piles.value.length) return
    
    // 刷新充电桩状态，后端应该自动处理过期预约
    await loadPiles(station.value.id)
  }, 30000) // 30秒检查一次
}

// 格式化订单创建时间
const orderCreateTimeText = computed(() => {
  if (!currentOrder.value || !currentOrder.value.createTime) {
    return '—'
  }
  return formatDateTimeDisplay(currentOrder.value.createTime)
})

const handleStartCharging = async (pile) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 先让用户选择车辆
  await openVehicleDialog(pile)
}

// 确认支付
const handlePaymentConfirm = () => {
  ElMessage.success('支付成功，开始充电')
  paymentDialogVisible.value = false
  // 刷新充电桩状态
  if (station.value) {
    loadPiles(station.value.id)
  }
  // 跳转到充电页面
  router.push('/charging')
}

// 取消支付
const handlePaymentCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消支付吗？取消后将无法开始充电，充电桩将被释放。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '继续支付',
      type: 'warning'
    })
    
    // 用户确认取消，调用后端API取消订单
    if (currentOrder.value && currentOrder.value.id) {
      try {
        const res = await cancelOrder(currentOrder.value.id)
        if (res.code === 200) {
          ElMessage.success('订单已取消，充电桩已释放')
        } else {
          ElMessage.error(res.message || '取消订单失败')
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        ElMessage.error('取消订单失败，请稍后重试')
      }
    }
    
    // 关闭弹窗
    paymentDialogVisible.value = false
    currentOrder.value = null
    orderAmount.value = 0
    
    // 刷新充电桩状态（充电桩应该恢复空闲）
    if (station.value) {
      await loadPiles(station.value.id)
    }
  } catch {
    // 用户选择继续支付，不做任何操作
  }
}

const handleFavorite = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!station.value || !station.value.id) {
    ElMessage.error('充电站信息不完整')
    return
  }
  
  try {
    if (isFavorite.value) {
      const res = await removeFavorite(station.value.id)
      if (res.code === 200) {
        isFavorite.value = false
        ElMessage.success('取消收藏成功')
      } else {
        ElMessage.error(res.message || '取消收藏失败')
      }
    } else {
      const res = await addFavorite(station.value.id)
      if (res.code === 200) {
        isFavorite.value = true
        ElMessage.success('收藏成功')
      } else {
        ElMessage.error(res.message || '收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleEvaluate = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!station.value) {
    ElMessage.error('充电站信息不存在')
    return
  }
  // 重置表单
  evaluationForm.value = {
    stationId: station.value.id,
    orderId: null,
    score: 5,
    environmentScore: 5,
    serviceScore: 5,
    equipmentScore: 5,
    content: ''
  }
  evaluationImageList.value = []
  evaluationUploadedImages.value = []
  evaluationDialogVisible.value = true
}

const handleReportFault = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!station.value) {
    ElMessage.error('充电站信息不存在')
    return
  }
  // 重置表单
  faultForm.value = {
    stationId: station.value.id,
    pileId: null,
    faultType: '',
    description: ''
  }
  faultImageList.value = []
  faultImageFiles.value = []
  faultDialogVisible.value = true
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
      ElMessage.error('图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

// 评价图片移除
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
    if (!valid) return
    
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
        // 重置表单
        evaluationForm.value = {
          stationId: station.value.id,
          orderId: null,
          score: 5,
          environmentScore: 5,
          serviceScore: 5,
          equipmentScore: 5,
          content: ''
        }
        evaluationImageList.value = []
        evaluationUploadedImages.value = []
        // 重新加载评价列表
        if (station.value) {
          await loadEvaluations(station.value.id)
        }
      } else {
        ElMessage.error(res.message || '评价失败')
      }
    } catch (error) {
      console.error('评价失败:', error)
      ElMessage.error('评价失败')
    } finally {
      evaluationLoading.value = false
    }
  })
}

// 故障上报图片上传
const handleFaultUpload = async (options) => {
  try {
    faultImageFiles.value.push(options.file)
    // 故障上报的图片会在提交时一起上传，这里只保存文件用于预览
    faultImageList.value.push({
      uid: options.file.uid,
      name: options.file.name,
      url: URL.createObjectURL(options.file)
    })
    ElMessage.success('图片已添加')
  } catch (error) {
    console.error('图片添加失败:', error)
    ElMessage.error('图片添加失败')
  }
}

// 故障上报图片移除
const handleFaultRemove = (file) => {
  const index = faultImageFiles.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    faultImageFiles.value.splice(index, 1)
  }
}

// 提交故障上报
const handleSubmitFault = async () => {
  if (!faultFormRef.value) return
  
  await faultFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    faultLoading.value = true
    try {
      const faultData = {
        stationId: faultForm.value.stationId,
        pileId: faultForm.value.pileId || null,
        faultType: faultForm.value.faultType,
        description: faultForm.value.description,
        images: faultImageFiles.value
      }
      const res = await reportFault(faultData)
      if (res.code === 200) {
        ElMessage.success('故障上报成功')
        faultDialogVisible.value = false
      } else {
        ElMessage.error(res.message || '故障上报失败')
      }
    } catch (error) {
      console.error('故障上报失败:', error)
      ElMessage.error('故障上报失败')
    } finally {
      faultLoading.value = false
    }
  })
}
</script>

<style scoped>
.station-detail {
  min-height: 100vh;
  background: var(--bg-color);
  padding-bottom: 80px;
}

.station-detail .el-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  box-shadow: var(--shadow-md);
  position: sticky;
  top: 0;
  z-index: 100;
}

.station-detail .header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.station-detail .header-right-menu {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.station-detail .header-btn {
  color: white;
  border-color: rgba(255, 255, 255, 0.3);
}

.station-detail .header-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.station-detail .user-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: background var(--transition-base);
}

.station-detail .user-section:hover {
  background: rgba(255, 255, 255, 0.1);
}

.station-detail .username {
  color: white;
  font-size: var(--font-size-sm);
}

.station-detail .el-header .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(10px);
}

.station-detail .el-header .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.station-detail .el-header h2 {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: 600;
  flex: 1;
}

.station-detail .el-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.station-header {
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-lg);
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.station-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-lg) 0;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.station-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
  font-size: var(--font-size-md);
  line-height: 1.6;
}

.info-item .el-icon {
  color: var(--primary-color);
  font-size: 18px;
  flex-shrink: 0;
}

.info-label {
  font-weight: 500;
  color: var(--text-primary);
  min-width: 80px;
}

.info-value {
  color: var(--text-secondary);
  flex: 1;
}

.piles-section, .evaluations-section {
  margin-top: var(--spacing-xl);
}

.piles-section h4, .evaluations-section h4 {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--primary-color);
  display: inline-block;
}

.evaluation-item {
  margin-top: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  border-left: 4px solid transparent;
}

.evaluation-item:hover {
  box-shadow: var(--shadow-md);
  border-left-color: var(--primary-color);
  transform: translateX(4px);
}

.evaluation-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.user-nickname {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-md);
}

.evaluation-score {
  margin-left: auto;
}

.evaluation-content {
  color: var(--text-secondary);
  margin-bottom: var(--spacing-md);
  line-height: 1.8;
  font-size: var(--font-size-sm);
}

.evaluation-images {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
  flex-wrap: wrap;
}

.evaluation-image {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-md);
  object-fit: cover;
  border: 2px solid var(--divider-color);
  cursor: pointer;
  transition: all var(--transition-base);
}

.evaluation-image:hover {
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.evaluation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--divider-color);
}

.eval-time {
  color: var(--text-disabled);
  font-size: var(--font-size-xs);
}

.evaluation-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.like-wrapper {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  user-select: none;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.like-wrapper:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

/* 图标基础样式 */
.like-icon {
  fill: #bfbfbf;
  transition: all 0.25s ease;
  margin-bottom: 4px;
}

/* 已点赞 */
.like-icon.active {
  fill: #22c55e; /* 新能源绿 */
  transform: scale(1.1);
}

.like-info {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
}

.like-text {
  font-size: 12px;
  color: #666;
  line-height: 1;
}

.like-count {
  font-size: 12px;
  color: #999;
  line-height: 1;
  font-weight: 500;
}

/* hover 提示 */
.like-wrapper:hover .like-icon {
  fill: #16a34a;
}

.like-wrapper:hover .like-icon.active {
  fill: #22c55e;
}

.own-evaluation-hint {
  color: var(--text-disabled);
  font-size: var(--font-size-sm);
}

.station-actions {
  margin: var(--spacing-xl) 0;
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.station-actions .el-button {
  flex: 1;
  min-width: 120px;
}

:deep(.el-card) {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: none;
}

:deep(.el-card__body) {
  padding: var(--spacing-lg);
}

:deep(.el-table) {
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--divider-color);
}

:deep(.el-table th) {
  background: var(--bg-hover);
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-table td) {
  border-bottom: 1px solid var(--divider-color);
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: nowrap;
}

.action-buttons .el-button {
  flex-shrink: 0;
}

.no-action {
  color: var(--text-disabled);
  font-size: var(--font-size-sm);
}

:deep(.el-tag) {
  border-radius: var(--radius-md);
  font-weight: 500;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #757575;
}

.loading-wrapper .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .station-detail .el-main {
    padding: var(--spacing-md);
  }
  
  .station-actions {
    flex-direction: column;
  }
  
  .station-actions .el-button {
    width: 100%;
  }
  
  .evaluation-images {
    gap: var(--spacing-xs);
  }
  
  .evaluation-image {
    width: 80px;
    height: 80px;
  }
  
  .payment-amount {
    padding: var(--spacing-md);
  }
  
  .amount-value {
    font-size: 28px;
  }
  
  .qrcode-placeholder {
    width: 200px;
    height: 200px;
  }
  
  .qrcode-icon {
    font-size: 48px;
  }
}

/* 付款表单样式 */
.payment-form {
  padding: var(--spacing-md) 0;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--divider-color);
}

.payment-item:last-of-type {
  border-bottom: none;
}

.payment-label {
  color: var(--text-secondary);
  font-size: var(--font-size-md);
}

.payment-value {
  color: var(--text-primary);
  font-size: var(--font-size-md);
  font-weight: 500;
}

.payment-amount {
  margin: var(--spacing-xl) 0;
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: var(--radius-lg);
  text-align: center;
  box-shadow: var(--shadow-md);
}

.amount-label {
  color: rgba(255, 255, 255, 0.9);
  font-size: var(--font-size-md);
  margin-bottom: var(--spacing-sm);
}

.amount-value {
  color: #fff;
  font-size: 36px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.payment-qrcode {
  margin: var(--spacing-xl) 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.qrcode-placeholder {
  width: 250px;
  height: 250px;
  border: 2px dashed var(--divider-color);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: var(--bg-page);
  transition: all var(--transition-base);
}

.qrcode-placeholder:hover {
  border-color: var(--primary-color);
  background: rgba(var(--primary-color-rgb), 0.05);
}

.qrcode-icon {
  font-size: 64px;
  color: var(--text-disabled);
  margin-bottom: var(--spacing-md);
}

.qrcode-text {
  color: var(--text-secondary);
  font-size: var(--font-size-md);
  font-weight: 500;
  margin-bottom: var(--spacing-sm);
}

.qrcode-hint {
  color: var(--text-disabled);
  font-size: var(--font-size-sm);
  text-align: center;
  padding: 0 var(--spacing-md);
}
</style>

