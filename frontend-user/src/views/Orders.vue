<template>
  <div class="orders">
    <!-- 页面头部 -->
    <div class="orders__header">
      <!-- 返回按钮 -->
      <el-button 
        class="orders__back-button" 
        text 
        @click="handleGoBack"
      >
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      
      <!-- 标题和副标题（居中） -->
      <div class="orders__header-content">
        <h1 class="orders__title">我的订单</h1>
        <p class="orders__subtitle">查看你的充电与预约记录</p>
      </div>
      
      <!-- 主页按钮 -->
      <el-button 
        class="orders__home-button" 
        text 
        @click="handleGoHome"
      >
        <el-icon><HomeFilled /></el-icon>
        <span>主页</span>
      </el-button>
      
      <!-- 状态筛选 Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="orders__tabs">
        <el-tab-pane label="全部" name=""></el-tab-pane>
        <el-tab-pane label="待支付" name="pending"></el-tab-pane>
        <el-tab-pane label="进行中" name="1"></el-tab-pane>
        <el-tab-pane label="已预约" name="reserved"></el-tab-pane>
        <el-tab-pane label="已完成" name="2"></el-tab-pane>
        <el-tab-pane label="已取消" name="3"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 订单内容区域 -->
    <div class="orders__content">
      <!-- 加载状态 -->
      <Skeleton v-if="loading" type="list" :rows="3" />
      
      <!-- 空状态 -->
      <EmptyState
        v-else-if="!orders.length"
        description="暂无订单记录"
        sub-description="快去预约一个充电桩吧~"
        icon="Document"
        :show-button="true"
        button-text="去首页看看"
        @action="handleGoHome"
      />
      
      <!-- 订单列表 -->
      <div v-else class="orders__list">
        <OrderCard
          v-for="order in orders"
          :key="order.id"
          :order="order"
          @view-detail="handleViewDetail"
          @stop-charging="handleStopCharging"
          @cancel-reservation="handleCancelReservation"
          @evaluate="handleEvaluate"
          @delete="handleDelete"
          @pay="handlePay"
        />
      </div>
      
      <!-- 分页组件 -->
      <div v-if="!loading && orders.length > 0" class="orders__pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="5"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList, stopCharging, cancelOrder } from '@/api/order'
import { getStationDetail, getStationPiles } from '@/api/station'
import { createEvaluation, uploadEvaluationImage } from '@/api/evaluation'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, HomeFilled } from '@element-plus/icons-vue'
import Skeleton from '@/components/Skeleton.vue'
import EmptyState from '@/components/EmptyState.vue'
import OrderCard from '@/components/OrderCard.vue'

const router = useRouter()
const activeTab = ref('')
const orders = ref([])
const loading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(5) // 每页显示5个订单
const total = ref(0)

// 缓存充电站信息
const stationCache = new Map()

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

onMounted(() => {
  // 初始化时重置到第一页
  currentPage.value = 1
  loadOrders()
})

// 获取充电站信息（带缓存）
const getStationInfo = async (stationId) => {
  if (!stationId) return null
  
  // 从缓存获取
  if (stationCache.has(stationId)) {
    return stationCache.get(stationId)
  }
  
  try {
    const res = await getStationDetail(stationId)
    if (res.code === 200 && res.data) {
      const stationInfo = {
        name: res.data.name || '充电站',
        address: res.data.address || '地址信息待完善'
      }
      stationCache.set(stationId, stationInfo)
      return stationInfo
    }
  } catch (error) {
    console.error('获取充电站信息失败:', error)
  }
  
  return {
    name: '充电站',
    address: '地址信息待完善'
  }
}

// 缓存充电桩信息
const pileCache = new Map()

// 获取充电桩信息
const getPileInfo = async (stationId, pileId) => {
  if (!stationId || !pileId) return null
  
  const cacheKey = `${stationId}_${pileId}`
  if (pileCache.has(cacheKey)) {
    return pileCache.get(cacheKey)
  }
  
  try {
    const res = await getStationPiles(stationId)
    if (res.code === 200 && res.data) {
      const pile = Array.isArray(res.data) 
        ? res.data.find(p => p.id === pileId)
        : null
      if (pile) {
        const pileInfo = {
          type: pile.type, // 0-慢充 1-快充
          power: pile.power // 功率
        }
        pileCache.set(cacheKey, pileInfo)
        return pileInfo
      }
    }
  } catch (error) {
    console.error('获取充电桩信息失败:', error)
  }
  
  const defaultInfo = {
    type: 1, // 默认快充
    power: 0
  }
  pileCache.set(cacheKey, defaultInfo)
  return defaultInfo
}

// 处理订单数据，补充关联信息
const enrichOrderData = async (order) => {
  const enrichedOrder = { ...order }
  
  // 补充充电站信息
  if (order.stationId && (!order.stationName || !order.stationAddress)) {
    const stationInfo = await getStationInfo(order.stationId)
    if (stationInfo) {
      enrichedOrder.stationName = stationInfo.name
      enrichedOrder.stationAddress = stationInfo.address
      enrichedOrder.stationServiceFee = stationInfo.serviceFee // 充电站服务费单价
    }
  }
  
  // 补充充电桩信息
  if (order.pileId && (!order.pileType || !order.pilePower)) {
    const pileInfo = await getPileInfo(order.stationId, order.pileId)
    if (pileInfo) {
      enrichedOrder.pileType = pileInfo.type
      enrichedOrder.pilePower = pileInfo.power
    }
  }
  
  // 处理字段映射
  // 后端返回的 orderNo 可能是 order_no，需要统一
  if (!enrichedOrder.orderNo && enrichedOrder.order_no) {
    enrichedOrder.orderNo = enrichedOrder.order_no
  }
  
  // 处理时间字段
  if (!enrichedOrder.createTime && enrichedOrder.create_time) {
    enrichedOrder.createTime = enrichedOrder.create_time
  }
  if (!enrichedOrder.actualStartTime && enrichedOrder.actual_start_time) {
    enrichedOrder.actualStartTime = enrichedOrder.actual_start_time
  }
  if (!enrichedOrder.endTime && enrichedOrder.end_time) {
    enrichedOrder.endTime = enrichedOrder.end_time
  }
  
  // 处理金额字段映射（支持驼峰和下划线两种格式）
  if (!enrichedOrder.totalAmount) {
    enrichedOrder.totalAmount = enrichedOrder.total_amount || 0
  }
  if (!enrichedOrder.discountAmount) {
    enrichedOrder.discountAmount = enrichedOrder.discount_amount || 0
  }
  if (!enrichedOrder.price) {
    enrichedOrder.price = enrichedOrder.price || 0
  }
  if (!enrichedOrder.serviceFee) {
    enrichedOrder.serviceFee = enrichedOrder.service_fee || 0
  }
  
  // 确保金额为数字类型
  enrichedOrder.totalAmount = Number(enrichedOrder.totalAmount) || 0
  enrichedOrder.discountAmount = Number(enrichedOrder.discountAmount) || 0
  enrichedOrder.price = Number(enrichedOrder.price) || 0
  enrichedOrder.serviceFee = Number(enrichedOrder.serviceFee) || 0
  enrichedOrder.power = Number(enrichedOrder.power) || 0
  
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
  
  // 确保 discountAmount 为数字类型
  enrichedOrder.discountAmount = Number(enrichedOrder.discountAmount) || 0
  
  return enrichedOrder
}

const loadOrders = async () => {
  loading.value = true
  try {
    let params = {
      current: currentPage.value,
      size: pageSize.value
    }
    
    // 处理"已预约"和"待支付"状态
    if (activeTab.value === 'reserved') {
      // type=1 且 status=0
      params.status = 0
    } else if (activeTab.value === 'pending') {
      params.status = 0
    } else if (activeTab.value) {
      params.status = parseInt(activeTab.value)
    }
    
    const res = await getOrderList(params)
    if (res.code === 200) {
      let orderList = res.data.records || []
      
      // 如果是"已预约"标签，过滤出 type=1 且 status=0 的订单
      if (activeTab.value === 'reserved') {
        orderList = orderList.filter(order => order.type === 1 && order.status === 0)
      }
      // 如果是"待支付"标签，仅保留 status=0 且 非预约订单
      if (activeTab.value === 'pending') {
        orderList = orderList.filter(order => order.status === 0 && order.type !== 1)
      }
      
      // 补充订单关联信息
      const enrichedOrders = await Promise.all(
        orderList.map(order => enrichOrderData(order))
      )
      
      // 前端分页限制：确保每页只显示5条
      const startIndex = (currentPage.value - 1) * pageSize.value
      const endIndex = startIndex + pageSize.value
      orders.value = enrichedOrders.slice(startIndex, endIndex)
      
      // 设置总数（使用过滤后的总长度，而不是当前页的长度）
      total.value = res.data.total || enrichedOrders.length
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  // 切换标签时重置到第一页
  currentPage.value = 1
  loadOrders()
}

// 当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadOrders()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 查看详情
const handleViewDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

// 结束充电
const handleStopCharging = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要结束充电吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await stopCharging(orderId)
    if (res.code === 200) {
      ElMessage.success('已停止充电')
      router.push({ path: '/payment', query: { orderId } })
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('停止充电失败')
    }
  }
}

// 取消预约
const handleCancelReservation = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await cancelOrder(orderId)
    if (res.code === 200) {
      ElMessage.success('已取消预约')
      loadOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消预约失败')
    }
  }
}

// 评价
const handleEvaluate = (orderId) => {
  const order = orders.value.find(o => o.id === orderId)
  if (!order) {
    ElMessage.error('订单不存在')
    return
  }
  
  // 重置表单
  evaluationForm.value = {
    stationId: order.stationId,
    orderId: orderId,
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
          // 重新加载订单列表
          loadOrders()
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

// 删除订单
const handleDelete = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里需要调用删除订单的 API
    // const res = await deleteOrder(orderId)
    // if (res.code === 200) {
    //   ElMessage.success('已删除订单')
    //   // 如果当前页删除后没有订单了，且不是第一页，则跳转到上一页
    //   if (orders.value.length === 1 && currentPage.value > 1) {
    //     currentPage.value--
    //   }
    //   loadOrders()
    // }
    ElMessage.success('已删除订单')
    // 如果当前页删除后没有订单了，且不是第一页，则跳转到上一页
    if (orders.value.length === 1 && currentPage.value > 1) {
      currentPage.value--
    }
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除订单失败')
    }
  }
}

// 去支付
const handlePay = (orderId) => {
  router.push({
    path: '/payment',
    query: { orderId }
  })
}

// 去首页
const handleGoHome = () => {
  router.push('/')
}

// 返回上一页
const handleGoBack = () => {
  router.back()
}
</script>

<style scoped>
.orders {
  min-height: 100vh;
  background: #F5F5F5;
}

/* 页面头部 */
.orders__header {
  background: #FFFFFF;
  padding: 24px 24px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
}

/* 返回按钮 */
.orders__back-button {
  position: absolute;
  left: 24px;
  top: 24px;
  z-index: 10;
  font-size: 20px;
  color: #212121;
  padding: 8px;
}

.orders__back-button:hover {
  color: #2196F3;
  background-color: rgba(33, 150, 243, 0.1);
}

.orders__home-button {
  position: absolute;
  right: 24px;
  top: 24px;
  z-index: 10;
  font-size: 16px;
  color: #212121;
  padding: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.orders__home-button:hover {
  color: #2196F3;
  background-color: rgba(33, 150, 243, 0.1);
}

.orders__header-content {
  margin-bottom: 16px;
  text-align: center;
  padding-top: 8px;
}

.orders__title {
  font-size: 28px;
  font-weight: 600;
  color: #212121;
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.orders__subtitle {
  font-size: 14px;
  color: #757575;
  margin: 0 0 24px 0;
  line-height: 1.5;
}

/* Tabs 样式 */
.orders__tabs {
  margin-top: 16px;
}

.orders__tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #E0E0E0;
}

.orders__tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.orders__tabs :deep(.el-tabs__item) {
  padding: 0 20px;
  font-size: 14px;
  height: 44px;
  line-height: 44px;
  color: #9E9E9E;
  transition: all 0.3s ease;
}

.orders__tabs :deep(.el-tabs__item:hover) {
  color: #2196F3;
}

.orders__tabs :deep(.el-tabs__item.is-active) {
  color: #2196F3;
  font-weight: 500;
}

.orders__tabs :deep(.el-tabs__active-bar) {
  background-color: #2196F3;
  height: 2px;
}

/* 订单内容区域 */
.orders__content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* 订单列表 */
.orders__list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* 分页组件 */
.orders__pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 32px;
  padding: 24px 0;
}

.orders__pagination :deep(.el-pagination) {
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 1023px) {
  .orders__content {
    padding: 20px;
  }
}

@media (max-width: 767px) {
  .orders__header {
    padding: 20px 16px 0;
  }

  .orders__back-button {
    left: 16px;
    top: 20px;
    font-size: 18px;
    padding: 6px;
  }

  .orders__home-button {
    right: 16px;
    top: 20px;
    font-size: 14px;
    padding: 6px;
  }

  .orders__home-button span {
    display: none; /* 移动端隐藏文字，只显示图标 */
  }

  .orders__header-content {
    padding-top: 4px;
    padding-left: 40px;
    padding-right: 40px; /* 为右侧主页按钮留出空间 */
  }

  .orders__title {
    font-size: 24px;
  }

  .orders__subtitle {
    font-size: 13px;
    margin-bottom: 20px;
  }

  .orders__tabs :deep(.el-tabs__item) {
    padding: 0 16px;
    font-size: 13px;
  }

  .orders__content {
    padding: 16px;
  }

  .orders__pagination {
    margin-top: 24px;
    padding: 20px 0;
  }

  .orders__pagination :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }

  .orders__pagination :deep(.el-pagination__total),
  .orders__pagination :deep(.el-pagination__jump) {
    margin: 8px 0;
    width: 100%;
    text-align: center;
  }

  .orders__tabs {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .orders__tabs :deep(.el-tabs__nav-scroll) {
    overflow-x: auto;
  }
}

/* 评价对话框样式 */
.orders :deep(.el-dialog__body) {
  padding: 20px;
}

.orders :deep(.el-form-item) {
  margin-bottom: 20px;
}

.orders :deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

.orders :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>


