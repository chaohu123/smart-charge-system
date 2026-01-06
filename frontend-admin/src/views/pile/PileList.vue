<template>
  <div class="pile-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>充电桩列表</span>
          <el-button type="primary" @click="showAddDialog = true">新增充电桩</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchForm.pileNumber"
              placeholder="请输入桩号搜索"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="8">
            <el-select
              v-model="searchForm.stationId"
              placeholder="请选择充电站搜索"
              clearable
              filterable
              style="width: 100%"
              @change="handleSearch"
            >
              <el-option
                v-for="station in stations"
                :key="station.id"
                :label="station.name"
                :value="station.id"
              />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table :data="piles" style="width: 100%" v-loading="loading">
        <el-table-column prop="pileNumber" label="桩号" width="120" />
        <el-table-column prop="stationName" label="所属充电站" />
        <el-table-column prop="power" label="功率(kW)" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            {{ scope.row.type === 1 ? '快充' : '慢充' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="电价(元/kWh)" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadPiles"
        @current-change="loadPiles"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 编辑充电桩对话框 -->
    <el-dialog v-model="showEditDialog" :title="currentPile ? '编辑充电桩' : '新增充电桩'" width="600px">
      <el-form :model="pileForm" :rules="pileRules" ref="pileFormRef" label-width="120px">
        <el-form-item label="所属充电站" prop="stationId" v-if="!currentPile">
          <el-select v-model="pileForm.stationId" placeholder="请选择充电站" style="width: 100%" filterable>
            <el-option 
              v-for="station in stations" 
              :key="station.id" 
              :label="station.name" 
              :value="station.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="桩号" prop="pileNumber">
          <el-input v-model="pileForm.pileNumber" placeholder="请输入桩号" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="pileForm.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="功率(kW)" prop="power">
          <el-input-number 
            v-model="pileForm.power" 
            :min="0" 
            :precision="2"
            style="width: 100%"
            placeholder="请输入功率"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="pileForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="慢充" :value="0" />
            <el-option label="快充" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="协议类型" prop="protocol">
          <el-input v-model="pileForm.protocol" placeholder="请输入协议类型" />
        </el-form-item>
        <el-form-item label="价格(元/kWh)" prop="price">
          <el-input-number 
            v-model="pileForm.price" 
            :min="0" 
            :precision="2" 
            :step="0.1"
            style="width: 100%"
            placeholder="请输入电价"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="pileForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="空闲" :value="0" />
            <el-option label="占用" :value="1" />
            <el-option label="故障" :value="2" />
            <el-option label="离线" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getPileList, addPile, updatePile, deletePile } from '@/api/pile'
import { getStationList } from '@/api/station'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const piles = ref([])
const stations = ref([])
const loading = ref(false)
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const currentPile = ref(null)
const pileFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = ref({
  pileNumber: '',
  stationId: null
})

const pileForm = ref({
  stationId: null,
  pileNumber: '',
  model: '',
  power: null,
  type: 0,
  protocol: '',
  price: null,
  status: 0
})

const pileRules = {
  stationId: [
    { required: true, message: '请选择充电站', trigger: 'change' }
  ],
  pileNumber: [
    { required: true, message: '请输入桩号', trigger: 'blur' }
  ],
  power: [
    { required: true, type: 'number', message: '请输入功率', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  price: [
    { required: true, type: 'number', message: '请输入电价', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadPiles()
  loadStations()
})

const loadStations = async () => {
  try {
    const res = await getStationList({ current: 1, size: 1000 })
    if (res.code === 200) {
      stations.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载充电站列表失败:', error)
  }
}

const loadPiles = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    
    // 添加搜索参数
    if (searchForm.value.pileNumber) {
      params.pileNumber = searchForm.value.pileNumber
    }
    if (searchForm.value.stationId) {
      params.stationId = searchForm.value.stationId
    }
    
    const res = await getPileList(params)
    if (res.code === 200) {
      piles.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载充电桩列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadPiles()
}

const handleReset = () => {
  searchForm.value = {
    pileNumber: '',
    stationId: null
  }
  currentPage.value = 1
  loadPiles()
}

const getStatusType = (status) => {
  const types = { 0: 'success', 1: 'primary', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '空闲', 1: '占用', 2: '故障', 3: '离线' }
  return texts[status] || '未知'
}

const handleEdit = (row) => {
  currentPile.value = row
  pileForm.value = {
    stationId: row.stationId,
    pileNumber: row.pileNumber || '',
    model: row.model || '',
    power: row.power ? Number(row.power) : null,
    type: row.type !== undefined ? row.type : 0,
    protocol: row.protocol || '',
    price: row.price ? Number(row.price) : null,
    status: row.status !== undefined ? row.status : 0
  }
  showEditDialog.value = true
}

const confirmEdit = async () => {
  if (!pileFormRef.value) return
  
  await pileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const data = {
          stationId: pileForm.value.stationId,
          pileNumber: pileForm.value.pileNumber,
          model: pileForm.value.model,
          power: pileForm.value.power,
          type: pileForm.value.type,
          protocol: pileForm.value.protocol,
          price: pileForm.value.price,
          status: pileForm.value.status
        }
        
        let res
        if (currentPile.value) {
          // 编辑
          res = await updatePile(currentPile.value.id, data)
        } else {
          // 新增
          res = await addPile(data)
        }
        
        if (res.code === 200) {
          ElMessage.success(currentPile.value ? '编辑成功' : '新增成功')
          showEditDialog.value = false
          currentPile.value = null
          resetForm()
          loadPiles()
        }
      } catch (error) {
        ElMessage.error(currentPile.value ? '编辑失败' : '新增失败')
      }
    }
  })
}

const resetForm = () => {
  pileForm.value = {
    stationId: null,
    pileNumber: '',
    model: '',
    power: null,
    type: 0,
    protocol: '',
    price: null,
    status: 0
  }
  if (pileFormRef.value) {
    pileFormRef.value.resetFields()
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个充电桩吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deletePile(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadPiles()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 监听新增对话框
watch(showAddDialog, (newVal) => {
  if (newVal) {
    currentPile.value = null
    resetForm()
    showEditDialog.value = true
    showAddDialog.value = false
  }
})
</script>

<style scoped>
.pile-list {
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
