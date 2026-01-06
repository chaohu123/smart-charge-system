<template>
  <div class="reservation-history">
    <div class="page-header">
      <h1>我的预约</h1>
      <p class="subtitle">查看预约历史记录</p>
    </div>

    <div class="page-content">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>预约记录</span>
            <el-button type="primary" @click="$router.push('/reservation')">新建预约</el-button>
          </div>
        </template>

        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待使用" name="pending" />
          <el-tab-pane label="已使用" name="used" />
          <el-tab-pane label="已取消" name="cancelled" />
          <el-tab-pane label="已过期" name="expired" />
        </el-tabs>

        <div v-if="loading" class="loading-wrapper">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <el-empty v-else-if="filteredReservations.length === 0" description="暂无预约记录" />

        <el-table v-else :data="filteredReservations" style="width: 100%">
          <el-table-column prop="stationName" label="充电站" width="200" />
          <el-table-column prop="pileNumber" label="充电桩" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="duration" label="预约时长" width="120">
            <template #default="scope">
              {{ formatDuration(scope.row.duration) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="预约时间" width="180" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                v-if="scope.row.status === 0"
                type="danger"
                size="small"
                @click="handleCancel(scope.row.id)"
              >
                取消
              </el-button>
              <span v-else class="no-action">—</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getReservationList, cancelReservation } from '@/api/reservation'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const loading = ref(false)
const reservations = ref([])
const activeTab = ref('all')

const filteredReservations = computed(() => {
  if (activeTab.value === 'all') {
    return reservations.value
  }
  
  const statusMap = {
    pending: 0,
    used: 1,
    cancelled: 2,
    expired: 3
  }
  
  return reservations.value.filter(item => item.status === statusMap[activeTab.value])
})

const loadReservations = async () => {
  loading.value = true
  try {
    const res = await getReservationList()
    if (res.code === 200) {
      reservations.value = res.data || []
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  // 切换标签时不需要重新加载，使用计算属性过滤即可
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await cancelReservation(id)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      await loadReservations()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const getStatusType = (status) => {
  const types = { 0: 'primary', 1: 'success', 2: 'info', 3: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待使用', 1: '已使用', 2: '已取消', 3: '已过期' }
  return texts[status] || '未知'
}

const formatDuration = (minutes) => {
  if (!minutes) return '—'
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (mins === 0) {
    return `${hours}小时`
  }
  return `${hours}小时${mins}分钟`
}

onMounted(() => {
  loadReservations()
})
</script>

<style scoped>
.reservation-history {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40px;
}

.page-header {
  background: linear-gradient(135deg, #1a6dff 0%, #00b894 100%);
  color: white;
  padding: 40px 32px;
  margin-bottom: 24px;
  text-align: center;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.no-action {
  color: #c0c4cc;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
}

@media (max-width: 768px) {
  .page-content {
    padding: 0 16px;
  }
  
  .page-header {
    padding: 24px 16px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
}
</style>

