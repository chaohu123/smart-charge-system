<template>
  <div class="page">
    <div class="toolbar">
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="标题">
          <el-input v-model="query.title" placeholder="输入标题搜索" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="未发布" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="openDialog()">新增活动</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="状态" required>
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="未发布" :value="0" />
            <el-option label="进行中" :value="1" />
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
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            rows="4"
            placeholder="请输入描述"
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityList, addActivity, updateActivity, deleteActivity } from '@/api/operation'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  title: '',
  status: '',
  current: 1,
  size: 10
})

const dialog = reactive({
  visible: false,
  title: '新增活动'
})

const form = reactive({
  id: null,
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  status: 0
})

onMounted(() => {
  loadData()
})

const resetQuery = () => {
  query.title = ''
  query.status = ''
  query.current = 1
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getActivityList(query)
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
    dialog.title = '编辑活动'
    Object.assign(form, row)
  } else {
    dialog.title = '新增活动'
    Object.assign(form, {
      id: null,
      title: '',
      description: '',
      startTime: '',
      endTime: '',
      status: 0
    })
  }
}

const handleSubmit = async () => {
  if (!form.title) {
    ElMessage.warning('请输入标题')
    return
  }
  const payload = { ...form }
  let res
  if (form.id) {
    res = await updateActivity(form.id, payload)
  } else {
    res = await addActivity(payload)
  }
  if (res.code === 200) {
    ElMessage.success('保存成功')
    dialog.visible = false
    loadData()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除活动「${row.title}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    const res = await deleteActivity(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  })
}

const statusText = (s) => {
  const map = { 0: '未发布', 1: '进行中', 2: '已结束' }
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
</style>

