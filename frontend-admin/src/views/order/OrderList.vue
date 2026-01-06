<template>
  <div class="order-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.orderNo"
              placeholder="请输入订单号搜索"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchForm.userPhone"
              placeholder="请输入用户手机号搜索"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select v-model="searchForm.status" placeholder="订单状态" clearable style="width: 100%">
              <el-option label="全部" value="" />
              <el-option label="待支付" :value="0" />
              <el-option label="进行中" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
            />
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="24">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table :data="orders" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="userPhone" label="用户手机" />
        <el-table-column prop="stationName" label="充电站" />
        <el-table-column prop="power" label="充电量(kWh)" />
        <el-table-column prop="totalAmount" label="金额" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button
              v-if="scope.row.status === 2"
              size="small"
              type="warning"
              @click="handleRefund(scope.row)"
            >
              退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadOrders"
        @current-change="loadOrders"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="订单详情" width="700px">
      <el-descriptions :column="2" border v-if="orderDetail">
        <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(orderDetail.status)">
            {{ getStatusText(orderDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户手机">{{ orderDetail.userPhone || '--' }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ orderDetail.userNickname || '--' }}</el-descriptions-item>
        <el-descriptions-item label="充电站">{{ orderDetail.stationName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="充电站地址" :span="2">{{ orderDetail.stationAddress || '--' }}</el-descriptions-item>
        <el-descriptions-item label="充电桩ID">{{ orderDetail.pileId || '--' }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ orderDetail.type === 0 ? '即时充电' : '预约充电' }}</el-descriptions-item>
        <el-descriptions-item label="充电量(kWh)">{{ orderDetail.power || '--' }}</el-descriptions-item>
        <el-descriptions-item label="金额">
          <span style="color: #f56c6c; font-weight: 600;">¥{{ orderDetail.totalAmount || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ orderDetail.createTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ orderDetail.payTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ orderDetail.startTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ orderDetail.endTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ orderDetail.updateTime || '--' }}</el-descriptions-item>
      </el-descriptions>
      <div v-else style="text-align: center; padding: 20px;">
        <el-icon class="is-loading" style="font-size: 24px;"><Loading /></el-icon>
        <p style="margin-top: 10px;">加载中...</p>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList, refundOrder, getOrderDetail } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = ref({
  orderNo: '',
  userPhone: '',
  status: '',
  dateRange: []
})

onMounted(() => {
  loadOrders()
})

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.value.orderNo) {
      params.orderNo = searchForm.value.orderNo
    }
    if (searchForm.value.userPhone) {
      params.userPhone = searchForm.value.userPhone
    }
    if (searchForm.value.status !== '') {
      params.status = searchForm.value.status
    }
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
      params.startDate = searchForm.value.dateRange[0]
      params.endDate = searchForm.value.dateRange[1]
    }
    const res = await getOrderList(params)
    if (res.code === 200) {
      orders.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadOrders()
}

const handleReset = () => {
  searchForm.value = {
    orderNo: '',
    userPhone: '',
    status: '',
    dateRange: []
  }
  currentPage.value = 1
  loadOrders()
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm('确定要退款吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await refundOrder(row.id)
    if (res.code === 200) {
      ElMessage.success('退款成功')
      loadOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退款失败')
    }
  }
}

const showDetailDialog = ref(false)
const orderDetail = ref(null)

const handleViewDetail = async (row) => {
  showDetailDialog.value = true
  orderDetail.value = null
  try {
    const res = await getOrderDetail(row.id)
    if (res.code === 200) {
      orderDetail.value = res.data
    } else {
      ElMessage.error('加载订单详情失败')
    }
  } catch (error) {
    ElMessage.error('加载订单详情失败')
  }
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待支付', 1: '进行中', 2: '已完成', 3: '已取消' }
  return texts[status] || '未知'
}
</script>

<style scoped>
.order-list {
  padding: var(--spacing-lg);
  background-color: var(--content-bg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.el-card) {
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  background-color: var(--card-bg);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-color);
  padding: var(--spacing-md) var(--spacing-lg);
}

:deep(.el-table) {
  border-radius: var(--border-radius);
  overflow: hidden;
}

:deep(.el-button) {
  border-radius: var(--border-radius);
}

:deep(.el-tag) {
  border-radius: var(--border-radius);
}
</style>

