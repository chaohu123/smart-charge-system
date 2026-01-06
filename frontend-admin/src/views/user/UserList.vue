<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div>
            <el-input
              v-model="keyword"
              placeholder="搜索手机号/昵称"
              style="width: 200px; margin-right: 10px;"
              @keyup.enter="loadUsers"
            />
            <el-select v-model="status" placeholder="状态" @change="loadUsers" style="width: 120px; margin-right: 10px;">
              <el-option label="全部" value="" />
              <el-option label="正常" :value="0" />
              <el-option label="禁用" :value="1" />
            </el-select>
            <el-button type="primary" @click="loadUsers">搜索</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="creditScore" label="信用分" />
        <el-table-column prop="balance" label="余额" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              size="small"
              :type="scope.row.status === 0 ? 'warning' : 'success'"
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 0 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="info" @click="handleCreditScore(scope.row)">
              信用分
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              删除
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
        @size-change="loadUsers"
        @current-change="loadUsers"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserList, updateUserStatus, updateCreditScore, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const users = ref([])
const keyword = ref('')
const status = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  loading.value = true
  try {
    const params = { 
      current: currentPage.value, 
      size: pageSize.value 
    }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    if (status.value !== '') {
      params.status = status.value
    }
    const res = await getUserList(params)
    if (res.code === 200) {
      users.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  router.push(`/user/edit/${row.id}`)
}

const handleStatusChange = async (row) => {
  try {
    const newStatus = row.status === 0 ? 1 : 0
    const res = await updateUserStatus(row.id, newStatus)
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      loadUsers()
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleCreditScore = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的信用分', '调整信用分', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^\d+$/,
      inputErrorMessage: '信用分必须为数字'
    })
    
    const creditScore = parseInt(value)
    if (creditScore < 0 || creditScore > 1000) {
      ElMessage.warning('信用分范围：0-1000')
      return
    }
    
    const res = await updateCreditScore(row.id, creditScore)
    if (res.code === 200) {
      ElMessage.success('信用分更新成功')
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('信用分更新失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.nickname || row.phone}" 吗？此操作不可恢复！`,
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false
      }
    )
    
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.user-list {
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

