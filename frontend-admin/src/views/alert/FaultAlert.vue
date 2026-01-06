<template>
  <div class="fault-alert">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>故障报警系统</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.alertNo"
              placeholder="请输入报警编号搜索"
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
              v-model="searchForm.stationName"
              placeholder="请输入充电站名称搜索"
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
            <el-select v-model="searchForm.status" placeholder="筛选状态" clearable style="width: 100%">
              <el-option label="全部" :value="null" />
              <el-option label="未处理" :value="0" />
              <el-option label="处理中" :value="1" />
              <el-option label="已处理" :value="2" />
              <el-option label="已忽略" :value="3" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="searchForm.alertType" placeholder="筛选类型" clearable style="width: 100%">
              <el-option label="全部" :value="null" />
              <el-option label="设备离线" :value="0" />
              <el-option label="异常电流" :value="1" />
              <el-option label="异常电压" :value="2" />
              <el-option label="其他异常" :value="3" />
            </el-select>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 10px;">
          <el-col :span="24">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table :data="alerts" style="width: 100%" v-loading="loading">
        <el-table-column prop="alertNo" label="报警编号" width="180" />
        <el-table-column prop="stationName" label="充电站" width="150" />
        <el-table-column prop="pileNumber" label="充电桩" width="120" />
        <el-table-column prop="alertType" label="报警类型" width="120">
          <template #default="scope">
            <el-tag :type="getAlertTypeTag(scope.row.alertType)">
              {{ getAlertTypeText(scope.row.alertType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertLevel" label="报警级别" width="120">
          <template #default="scope">
            <el-tag :type="getAlertLevelTag(scope.row.alertLevel)">
              {{ getAlertLevelText(scope.row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentValue" label="电流值(A)" width="120">
          <template #default="scope">
            {{ scope.row.currentValue || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="voltageValue" label="电压值(V)" width="120">
          <template #default="scope">
            {{ scope.row.voltageValue || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" size="small" type="primary" @click="handleAlert(scope.row)">
              处理
            </el-button>
            <el-button v-if="scope.row.status === 0" size="small" type="danger" @click="handleEmergencyStop(scope.row)">
              紧急停机
            </el-button>
            <el-button v-if="scope.row.status === 0" size="small" @click="ignoreAlert(scope.row)">
              忽略
            </el-button>
            <el-button size="small" @click="viewDetail(scope.row)">
              详情
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
        @size-change="loadAlerts"
        @current-change="loadAlerts"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 处理报警对话框 -->
    <el-dialog v-model="showHandleDialog" title="处理报警" width="500px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="4" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showHandleDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmHandle">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 报警详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="报警详情" width="700px">
      <el-descriptions :column="2" border v-if="alertDetail">
        <el-descriptions-item label="报警编号">{{ alertDetail.alertNo }}</el-descriptions-item>
        <el-descriptions-item label="报警类型">
          <el-tag :type="getAlertTypeTag(alertDetail.alertType)">
            {{ getAlertTypeText(alertDetail.alertType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报警级别">
          <el-tag :type="getAlertLevelTag(alertDetail.alertLevel)">
            {{ getAlertLevelText(alertDetail.alertLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(alertDetail.status)">
            {{ getStatusText(alertDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="充电站">
          {{ alertDetail.stationName || `ID: ${alertDetail.stationId}` }}
        </el-descriptions-item>
        <el-descriptions-item label="充电桩">
          {{ alertDetail.pileNumber || `ID: ${alertDetail.pileId}` }}
        </el-descriptions-item>
        <el-descriptions-item label="电流值(A)">{{ alertDetail.currentValue || '--' }}</el-descriptions-item>
        <el-descriptions-item label="电压值(V)">{{ alertDetail.voltageValue || '--' }}</el-descriptions-item>
        <el-descriptions-item label="阈值">{{ alertDetail.thresholdValue || '--' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ alertDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ alertDetail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="处理人ID" v-if="alertDetail.handlerId">{{ alertDetail.handlerId }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="alertDetail.handleTime">{{ alertDetail.handleTime }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ alertDetail.description || '--' }}</el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2" v-if="alertDetail.handleRemark">{{ alertDetail.handleRemark }}</el-descriptions-item>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { getAlertList, getAlertDetail, handleAlert as handleAlertApi, ignoreAlert as ignoreAlertApi, emergencyStop } from '@/api/alert'
import { useAdminStore } from '@/stores/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Search } from '@element-plus/icons-vue'

const adminStore = useAdminStore()
const alerts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = ref({
  alertNo: '',
  stationName: '',
  status: null,
  alertType: null
})
const showHandleDialog = ref(false)
const showDetailDialog = ref(false)
const currentAlert = ref(null)
const alertDetail = ref(null)
const handleForm = ref({
  handleRemark: ''
})
let timer = null

onMounted(() => {
  loadAlerts()
  // 定时刷新
  timer = setInterval(loadAlerts, 10000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const loadAlerts = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.value.alertNo) {
      params.alertNo = searchForm.value.alertNo
    }
    if (searchForm.value.stationName) {
      params.stationName = searchForm.value.stationName
    }
    if (searchForm.value.status !== null) {
      params.status = searchForm.value.status
    }
    if (searchForm.value.alertType !== null) {
      params.alertType = searchForm.value.alertType
    }
    const res = await getAlertList(params)
    if (res.code === 200) {
      alerts.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载报警列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadAlerts()
}

const handleReset = () => {
  searchForm.value = {
    alertNo: '',
    stationName: '',
    status: null,
    alertType: null
  }
  currentPage.value = 1
  loadAlerts()
}

const getAlertTypeText = (type) => {
  const types = { 0: '设备离线', 1: '异常电流', 2: '异常电压', 3: '其他异常' }
  return types[type] || '未知'
}

const getAlertTypeTag = (type) => {
  const tags = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'danger' }
  return tags[type] || 'info'
}

const getAlertLevelText = (level) => {
  const levels = { 1: '一般', 2: '重要', 3: '紧急' }
  return levels[level] || '未知'
}

const getAlertLevelTag = (level) => {
  const tags = { 1: 'info', 2: 'warning', 3: 'danger' }
  return tags[level] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '未处理', 1: '处理中', 2: '已处理', 3: '已忽略' }
  return texts[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return tags[status] || 'info'
}

const handleAlert = (alert) => {
  currentAlert.value = alert
  handleForm.value.handleRemark = ''
  showHandleDialog.value = true
}

const confirmHandle = async () => {
  if (!currentAlert.value) return
  
  try {
    const res = await handleAlertApi(
      currentAlert.value.id,
      adminStore.adminInfo.id,
      handleForm.value.handleRemark
    )
    if (res.code === 200) {
      ElMessage.success(res.message || '处理成功，已自动创建运维工单')
      showHandleDialog.value = false
      loadAlerts()
    }
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

const ignoreAlert = async (alert) => {
  try {
    await ElMessageBox.confirm('确定要忽略此报警吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await ignoreAlertApi(alert.id)
    if (res.code === 200) {
      ElMessage.success('已忽略')
      loadAlerts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleEmergencyStop = async (alert) => {
  try {
    await ElMessageBox.confirm('确定要紧急停机吗？这将立即停止充电桩运行！', '紧急停机', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const res = await emergencyStop(alert.pileId)
    if (res.code === 200) {
      ElMessage.success('紧急停机成功')
      loadAlerts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('紧急停机失败')
    }
  }
}

const viewDetail = async (alert) => {
  showDetailDialog.value = true
  alertDetail.value = null
  try {
    const res = await getAlertDetail(alert.id)
    if (res.code === 200) {
      alertDetail.value = res.data
    } else {
      ElMessage.error('加载详情失败')
    }
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}
</script>

<style scoped>
.fault-alert {
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

.header-actions {
  display: flex;
  align-items: center;
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

