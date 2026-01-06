<template>
  <div class="invoice-list">
    <el-card>
      <template #header>
        <span>我的发票</span>
      </template>
      
      <el-empty v-if="invoices.length === 0 && !loading" description="暂无发票记录" />
      
      <el-table v-else :data="invoices" style="width: 100%">
        <el-table-column prop="invoiceNo" label="发票号" />
        <el-table-column prop="orderNo" label="关联订单" />
        <el-table-column prop="title" label="发票抬头" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === '已开票'"
              size="small"
              type="primary"
              @click="handleDownload(scope.row)"
            >
              下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getInvoiceList, downloadInvoice } from '@/api/invoice'
import { ElMessage } from 'element-plus'

const invoices = ref([])
const loading = ref(false)

onMounted(() => {
  loadInvoices()
})

const loadInvoices = async () => {
  loading.value = true
  try {
    const res = await getInvoiceList({ current: 1, size: 20 })
    if (res.code === 200) {
      invoices.value = res.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载发票列表失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const types = {
    '待开票': 'warning',
    '已开票': 'success',
    '已作废': 'info'
  }
  return types[status] || 'info'
}

const handleDownload = async (invoice) => {
  try {
    const res = await downloadInvoice(invoice.id)
    if (res.code === 200) {
      // 打开下载链接
      window.open(res.data, '_blank')
    }
  } catch (error) {
    ElMessage.error('下载失败')
  }
}
</script>

<style scoped>
.invoice-list {
  padding: 20px;
}
</style>

