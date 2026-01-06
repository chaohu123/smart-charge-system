<template>
  <div class="maintenance">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>运维工单</span>
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-input
              v-model="searchForm.ticketNo"
              placeholder="工单号"
              style="width: 200px;"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-input
              v-model="searchForm.stationName"
              placeholder="充电站名称"
              style="width: 200px;"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="searchForm.status"
              placeholder="状态"
              style="width: 150px;"
              clearable
            >
              <el-option label="全部" :value="undefined" />
              <el-option label="待处理" :value="0" />
              <el-option label="处理中" :value="1" />
              <el-option label="已完成" :value="2" />
            </el-select>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="showCreateDialog = true">创建工单</el-button>
          </div>
        </div>
      </template>
      
      <!-- 创建工单对话框 -->
      <el-dialog v-model="showCreateDialog" title="创建工单" @close="ticketForm = { stationId: null, pileId: null, faultType: '', description: '' }">
        <el-form :model="ticketForm" label-width="100px">
          <el-form-item label="充电站ID">
            <el-input-number v-model="ticketForm.stationId" :min="1" />
          </el-form-item>
          <el-form-item label="充电桩ID">
            <el-input-number v-model="ticketForm.pileId" :min="1" />
          </el-form-item>
          <el-form-item label="故障类型">
            <el-input v-model="ticketForm.faultType" />
          </el-form-item>
          <el-form-item label="故障描述">
            <el-input v-model="ticketForm.description" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreate">确定</el-button>
        </template>
      </el-dialog>
      
      <el-table :data="tickets" style="width: 100%">
        <el-table-column prop="ticketNo" label="工单号" />
        <el-table-column prop="stationName" label="充电站" />
        <el-table-column prop="pileNumber" label="充电桩" />
        <el-table-column prop="faultType" label="故障类型" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" @click="viewRecords(scope.row)">维护记录</el-button>
            <el-button v-if="scope.row.status === 0 || scope.row.status === 1" size="small" type="success" @click="showCompleteDialog(scope.row)">
              完成维护
            </el-button>
            <el-button v-if="scope.row.status === 0 || scope.row.status === 1" size="small" type="primary" @click="showRecordDialog(scope.row)">
              添加记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 维护记录对话框 -->
    <el-dialog v-model="showRecordDialogVisible" :title="currentTicket ? `添加维护记录 - ${currentTicket.ticketNo}` : '添加维护记录'" width="600px">
      <el-form :model="recordForm" label-width="120px">
        <el-form-item label="维护类型" required>
          <el-select v-model="recordForm.maintenanceType" placeholder="请选择维护类型">
            <el-option label="日常维护" value="日常维护" />
            <el-option label="故障维修" value="故障维修" />
            <el-option label="定期检查" value="定期检查" />
          </el-select>
        </el-form-item>
        <el-form-item label="维护内容" required>
          <el-input v-model="recordForm.maintenanceContent" type="textarea" :rows="4" placeholder="请输入维护内容" />
        </el-form-item>
        <el-form-item label="维护结果">
          <el-input v-model="recordForm.maintenanceResult" type="textarea" :rows="4" placeholder="请输入维护结果" />
        </el-form-item>
        <el-form-item label="维护时间" required>
          <el-date-picker
            v-model="recordForm.maintenanceTime"
            type="datetime"
            placeholder="选择维护时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="下次维护时间">
          <el-date-picker
            v-model="recordForm.nextMaintenanceTime"
            type="datetime"
            placeholder="选择下次维护时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="维护费用">
          <el-input-number v-model="recordForm.cost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateRecord">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 维护记录列表对话框 -->
    <el-dialog v-model="showRecordsDialog" title="维护记录" width="800px">
      <el-table :data="records" style="width: 100%">
        <el-table-column prop="recordNo" label="记录编号" width="180" />
        <el-table-column prop="maintenanceType" label="维护类型" width="120" />
        <el-table-column prop="maintenanceContent" label="维护内容" show-overflow-tooltip />
        <el-table-column prop="maintenanceResult" label="维护结果" show-overflow-tooltip />
        <el-table-column prop="maintenanceTime" label="维护时间" width="180" />
        <el-table-column prop="cost" label="费用" width="100">
          <template #default="scope">
            {{ scope.row.cost ? `¥${scope.row.cost}` : '--' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    
    <!-- 完成维护对话框 -->
    <el-dialog v-model="showCompleteDialogVisible" title="完成维护" width="500px">
      <el-form :model="completeForm" label-width="120px">
        <el-form-item label="是否能正常运行" required>
          <el-radio-group v-model="completeForm.isNormal">
            <el-radio :label="true">能正常运行</el-radio>
            <el-radio :label="false">不能正常运行</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="完成备注">
          <el-input v-model="completeForm.remark" type="textarea" :rows="4" placeholder="请输入完成备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCompleteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 工单详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="工单详情" width="700px">
      <el-descriptions :column="2" border v-if="ticketDetail">
        <el-descriptions-item label="工单号">{{ ticketDetail.ticketNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(ticketDetail.status)">
            {{ getStatusText(ticketDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="充电站">
          {{ ticketDetail.stationName || `ID: ${ticketDetail.stationId}` }}
        </el-descriptions-item>
        <el-descriptions-item label="充电桩">
          {{ ticketDetail.pileNumber || (ticketDetail.pileId ? `ID: ${ticketDetail.pileId}` : '--') }}
        </el-descriptions-item>
        <el-descriptions-item label="故障类型">{{ ticketDetail.faultType || '--' }}</el-descriptions-item>
        <el-descriptions-item label="处理人ID" v-if="ticketDetail.assigneeId">{{ ticketDetail.assigneeId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ ticketDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ ticketDetail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">
          <div style="white-space: pre-wrap;">{{ ticketDetail.description || '--' }}</div>
        </el-descriptions-item>
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
import { getTicketList, getTicketDetail, createTicket, updateTicketStatus, getMaintenanceRecordList, createMaintenanceRecord } from '@/api/maintenance'
import { useAdminStore } from '@/stores/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const adminStore = useAdminStore()
const tickets = ref([])
const showCreateDialog = ref(false)
const showRecordDialogVisible = ref(false)
const showRecordsDialog = ref(false)
const showDetailDialog = ref(false)
const ticketDetail = ref(null)
const currentTicket = ref(null)
const records = ref([])
const ticketForm = ref({
  stationId: null,
  pileId: null,
  faultType: '',
  description: ''
})
const recordForm = ref({
  ticketId: null,
  stationId: null,
  pileId: null,
  maintenanceType: '',
  maintenanceContent: '',
  maintenanceResult: '',
  maintenanceTime: '',
  nextMaintenanceTime: '',
  cost: null
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = ref({
  ticketNo: '',
  stationName: '',
  status: undefined
})

// 完成维护对话框
const showCompleteDialogVisible = ref(false)
const completeForm = ref({
  isNormal: true,
  remark: ''
})

onMounted(() => {
  loadTickets()
})

const loadTickets = async () => {
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.value.ticketNo) {
      params.ticketNo = searchForm.value.ticketNo
    }
    if (searchForm.value.stationName) {
      params.stationName = searchForm.value.stationName
    }
    if (searchForm.value.status !== undefined) {
      params.status = searchForm.value.status
    }
    
    const res = await getTicketList(params)
    if (res.code === 200 && res.data) {
      tickets.value = res.data.records || []
      total.value = Number(res.data.total) || 0
      // 确保total至少等于records.length
      if (total.value < tickets.value.length) {
        total.value = tickets.value.length
      }
    }
  } catch (error) {
    ElMessage.error('加载工单列表失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadTickets()
}

const handleReset = () => {
  searchForm.value = {
    ticketNo: '',
    stationName: '',
    status: undefined
  }
  currentPage.value = 1
  loadTickets()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadTickets()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadTickets()
}

const handleCreate = async () => {
  if (!ticketForm.value.stationId || !ticketForm.value.faultType || !ticketForm.value.description) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const res = await createTicket(ticketForm.value)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      showCreateDialog.value = false
      loadTickets()
    }
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'success' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待处理', 1: '处理中', 2: '已完成' }
  return texts[status] || '未知'
}

const handleView = async (row) => {
  showDetailDialog.value = true
  ticketDetail.value = null
  try {
    const res = await getTicketDetail(row.id)
    if (res.code === 200) {
      ticketDetail.value = res.data
    } else {
      ElMessage.error('加载工单详情失败')
    }
  } catch (error) {
    ElMessage.error('加载工单详情失败')
  }
}

const showCompleteDialog = (row) => {
  currentTicket.value = row
  completeForm.value = {
    isNormal: true,
    remark: ''
  }
  showCompleteDialogVisible.value = true
}

const confirmComplete = async () => {
  if (!currentTicket.value) return
  
  try {
    const res = await updateTicketStatus(currentTicket.value.id, 2, completeForm.value.isNormal, completeForm.value.remark)
    if (res.code === 200) {
      ElMessage.success('工单已完成')
      showCompleteDialogVisible.value = false
      loadTickets()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const showRecordDialog = (ticket) => {
  currentTicket.value = ticket
  recordForm.value = {
    ticketId: ticket.id,
    stationId: ticket.stationId,
    pileId: ticket.pileId,
    maintenanceType: '',
    maintenanceContent: '',
    maintenanceResult: '',
    maintenanceTime: new Date().toISOString().slice(0, 19),
    nextMaintenanceTime: '',
    cost: null
  }
  showRecordDialogVisible.value = true
}

const handleCreateRecord = async () => {
  if (!recordForm.value.maintenanceType || !recordForm.value.maintenanceContent || !recordForm.value.maintenanceTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const data = {
      ...recordForm.value,
      maintainerId: adminStore.adminInfo.id
    }
    const res = await createMaintenanceRecord(data)
    if (res.code === 200) {
      ElMessage.success('添加维护记录成功')
      showRecordDialogVisible.value = false
      loadTickets()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const viewRecords = async (ticket) => {
  currentTicket.value = ticket
  try {
    const res = await getMaintenanceRecordList({
      ticketId: ticket.id,
      current: 1,
      size: 100
    })
    if (res.code === 200) {
      records.value = res.data.records || []
      showRecordsDialog.value = true
    }
  } catch (error) {
    ElMessage.error('加载维护记录失败')
  }
}
</script>

<style scoped>
.maintenance {
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

