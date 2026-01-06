<template>
  <div class="withdraw-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>提现管理</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.withdrawNo"
              placeholder="请输入提现单号搜索"
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
            <el-select v-model="searchForm.status" placeholder="筛选状态" clearable style="width: 100%">
              <el-option label="全部" :value="null" />
              <el-option label="待审核" :value="0" />
              <el-option label="审核通过" :value="1" />
              <el-option label="审核拒绝" :value="2" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table :data="withdraws" style="width: 100%" v-loading="loading">
        <el-table-column prop="withdrawNo" label="提现单号" width="180" />
        <el-table-column prop="userPhone" label="用户手机" width="120" />
        <el-table-column prop="userNickname" label="用户昵称" width="120" />
        <el-table-column prop="amount" label="提现金额" width="120">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: 600;">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bankName" label="银行名称" width="120" />
        <el-table-column prop="bankAccount" label="银行账号" width="150" show-overflow-tooltip />
        <el-table-column prop="rejectReason" label="拒绝原因" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column prop="updateTime" label="审核时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="success"
              @click="handleAudit(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="danger"
              @click="handleAudit(scope.row, 2)"
            >
              拒绝
            </el-button>
            <el-button size="small" @click="handleViewDetail(scope.row)">
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
        @size-change="loadWithdraws"
        @current-change="loadWithdraws"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 审核对话框 -->
    <el-dialog
      v-model="showAuditDialog"
      :title="auditForm.status === 1 ? '审核通过' : '审核拒绝'"
      width="500px"
    >
      <el-form :model="auditForm" label-width="100px" v-if="auditForm.status === 2">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <div v-else style="padding: 20px; text-align: center;">
        <p>确定要通过该提现申请吗？</p>
        <p style="color: #909399; font-size: 14px; margin-top: 10px;">
          提现单号：{{ currentWithdraw?.withdrawNo }}<br />
          提现金额：<span style="color: #f56c6c; font-weight: 600;">¥{{ currentWithdraw?.amount }}</span>
        </p>
      </div>
      <template #footer>
        <el-button @click="showAuditDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="提现详情" width="700px">
      <el-descriptions :column="2" border v-if="withdrawDetail">
        <el-descriptions-item label="提现单号">{{ withdrawDetail.withdrawNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(withdrawDetail.status)">
            {{ getStatusText(withdrawDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户手机">{{ withdrawDetail.userPhone || '--' }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ withdrawDetail.userNickname || '--' }}</el-descriptions-item>
        <el-descriptions-item label="提现金额">
          <span style="color: #f56c6c; font-weight: 600;">¥{{ withdrawDetail.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="银行名称">{{ withdrawDetail.bankName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="银行账号">{{ withdrawDetail.bankAccount || '--' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ withdrawDetail.remark || '--' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ withdrawDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ withdrawDetail.updateTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="审核管理员ID" v-if="withdrawDetail.adminId">{{ withdrawDetail.adminId }}</el-descriptions-item>
        <el-descriptions-item label="拒绝原因" :span="2" v-if="withdrawDetail.rejectReason">
          {{ withdrawDetail.rejectReason }}
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
import { getWithdrawList, auditWithdraw } from '@/api/withdraw'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'

const withdraws = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = ref({
  withdrawNo: '',
  userPhone: '',
  status: null
})

const showAuditDialog = ref(false)
const showDetailDialog = ref(false)
const currentWithdraw = ref(null)
const withdrawDetail = ref(null)
const auditForm = ref({
  status: 1,
  reason: ''
})

onMounted(() => {
  loadWithdraws()
})

const loadWithdraws = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.value.withdrawNo) {
      params.withdrawNo = searchForm.value.withdrawNo
    }
    if (searchForm.value.userPhone) {
      params.userPhone = searchForm.value.userPhone
    }
    if (searchForm.value.status !== null) {
      params.status = searchForm.value.status
    }
    const res = await getWithdrawList(params)
    if (res.code === 200) {
      withdraws.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载提现列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadWithdraws()
}

const handleReset = () => {
  searchForm.value = {
    withdrawNo: '',
    userPhone: '',
    status: null
  }
  currentPage.value = 1
  loadWithdraws()
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '审核通过', 2: '审核拒绝' }
  return texts[status] || '未知'
}

const handleAudit = (row, status) => {
  currentWithdraw.value = row
  auditForm.value = {
    status: status,
    reason: ''
  }
  showAuditDialog.value = true
}

const confirmAudit = async () => {
  if (!currentWithdraw.value) return
  
  if (auditForm.value.status === 2 && !auditForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    const res = await auditWithdraw(
      currentWithdraw.value.id,
      auditForm.value.status,
      auditForm.value.reason || null
    )
    if (res.code === 200) {
      ElMessage.success('审核成功')
      showAuditDialog.value = false
      currentWithdraw.value = null
      auditForm.value = {
        status: 1,
        reason: ''
      }
      loadWithdraws()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

const handleViewDetail = (row) => {
  withdrawDetail.value = row
  showDetailDialog.value = true
}
</script>

<style scoped>
.withdraw-list {
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

