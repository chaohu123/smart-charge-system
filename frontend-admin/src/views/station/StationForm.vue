<template>
  <div class="station-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑充电站' : '新增充电站' }}</span>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="充电站名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number v-model="form.longitude" :precision="6" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number v-model="form.latitude" :precision="6" />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="form.businessHours" placeholder="例：08:00-22:00" />
        </el-form-item>
        <el-form-item label="服务电话" prop="servicePhone">
          <el-input v-model="form.servicePhone" />
        </el-form-item>
        <el-form-item label="服务费" prop="serviceFee">
          <el-input-number v-model="form.serviceFee" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">维护中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addStation, updateStation, getStationDetail } from '@/api/station'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  name: '',
  address: '',
  longitude: 0,
  latitude: 0,
  businessHours: '',
  servicePhone: '',
  serviceFee: 0,
  status: 0
})

const rules = {
  name: [{ required: true, message: '请输入充电站名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

onMounted(async () => {
  if (route.params.id) {
    isEdit.value = true
    await loadStationDetail(route.params.id)
  }
})

const loadStationDetail = async (id) => {
  try {
    const res = await getStationDetail(id)
    if (res.code === 200) {
      Object.assign(form, res.data)
    }
  } catch (error) {
    ElMessage.error('加载充电站信息失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let res
        if (isEdit.value) {
          res = await updateStation(route.params.id, form)
        } else {
          res = await addStation(form)
        }
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
          router.push('/station')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}
</script>

<style scoped>
.station-form {
  padding: var(--spacing-lg);
  background-color: var(--content-bg);
}

:deep(.el-card) {
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  background-color: var(--card-bg);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-color);
  padding: var(--spacing-md) var(--spacing-lg);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.el-form-item) {
  margin-bottom: var(--spacing-md);
}

:deep(.el-button) {
  border-radius: var(--border-radius);
}
</style>

