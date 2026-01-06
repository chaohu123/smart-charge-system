<template>
  <div class="page">
    <div class="toolbar">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="输入名称搜索" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="未发布" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="openDialog()">新增优惠券</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="amount" label="面值/折扣" width="110" />
      <el-table-column prop="minAmount" label="门槛金额" width="110" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button
              text
              type="warning"
              size="small"
              @click="handleStatus(row, row.status === 1 ? 0 : 1)"
            >
              {{ row.status === 1 ? '下架' : '发布' }}
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :current-page="query.current"
        :page-size="query.size"
        :total="total"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" placeholder="请选择">
            <el-option label="满减" value="满减" />
            <el-option label="折扣" value="折扣" />
          </el-select>
        </el-form-item>
        <el-form-item label="面值/折扣" required>
          <el-input-number v-model="form.amount" :min="0" :step="0.1" :precision="2" />
        </el-form-item>
        <el-form-item label="使用门槛">
          <el-input-number v-model="form.minAmount" :min="0" :step="0.1" :precision="2" />
        </el-form-item>
        <el-form-item label="发放数量">
          <el-input-number v-model="form.totalCount" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="未发布" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCouponList,
  addCoupon,
  updateCoupon,
  updateCouponStatus,
  deleteCouponAdmin
} from '@/api/operation'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  name: '',
  status: '',
  current: 1,
  size: 10
})

const dialog = reactive({
  visible: false,
  title: '新增优惠券'
})

const form = reactive({
  id: null,
  name: '',
  type: '满减',
  amount: 0,
  minAmount: 0,
  totalCount: 0,
  status: 0,
  startTime: '',
  endTime: ''
})

onMounted(() => {
  loadData()
})

const resetQuery = () => {
  query.name = ''
  query.status = ''
  query.current = 1
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCouponList(query)
    if (res.code === 200) {
      const data = res.data || {}
      list.value = data.records || data.rows || []
      total.value = data.total || list.value.length
    }
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  query.current = page
  loadData()
}

const openDialog = (row) => {
  dialog.visible = true
  if (row) {
    dialog.title = '编辑优惠券'
    Object.assign(form, row)
  } else {
    dialog.title = '新增优惠券'
    Object.assign(form, {
      id: null,
      name: '',
      type: '满减',
      amount: 0,
      minAmount: 0,
      totalCount: 0,
      status: 0,
      startTime: '',
      endTime: ''
    })
  }
}

const handleSubmit = async () => {
  if (!form.name) {
    ElMessage.warning('请输入名称')
    return
  }
  const payload = { ...form }
  let res
  if (form.id) {
    res = await updateCoupon(form.id, payload)
  } else {
    res = await addCoupon(payload)
  }
  if (res.code === 200) {
    ElMessage.success('保存成功')
    dialog.visible = false
    loadData()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除优惠券「${row.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await deleteCouponAdmin(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  })
}

const handleStatus = async (row, status) => {
  const res = await updateCouponStatus(row.id, status)
  if (res.code === 200) {
    ElMessage.success('状态更新成功')
    loadData()
  }
}

const statusText = (s) => {
  const map = { 0: '未发布', 1: '已发布', 2: '已结束' }
  return map[s] ?? '未知'
}
const statusType = (s) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[s] ?? 'info'
}
</script>

<style scoped>
.page {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-form {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.action-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}
</style>

