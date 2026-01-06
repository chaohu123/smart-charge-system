<template>
  <div class="fault-report">
    <el-card>
      <template #header>
        <span>故障上报</span>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="充电站" prop="stationId">
          <el-select v-model="form.stationId" placeholder="请选择充电站" @change="loadPiles">
            <el-option
              v-for="station in stations"
              :key="station.id"
              :label="station.name"
              :value="station.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="充电桩" prop="pileId">
          <el-select v-model="form.pileId" placeholder="请选择充电桩">
            <el-option
              v-for="pile in piles"
              :key="pile.id"
              :label="pile.pileNumber"
              :value="pile.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型" prop="faultType">
          <el-select v-model="form.faultType" placeholder="请选择故障类型">
            <el-option label="无法启动" value="无法启动" />
            <el-option label="充电中断" value="充电中断" />
            <el-option label="显示异常" value="显示异常" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述故障情况"
          />
        </el-form-item>
        <el-form-item label="故障图片">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :file-list="imageList"
            :limit="3"
            :before-upload="beforeUpload"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNearbyStations } from '@/api/station'
import { getStationPiles } from '@/api/station'
import { reportFault } from '@/api/fault'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const stations = ref([])
const piles = ref([])
const imageList = ref([])
const uploadedImages = ref([])
const imageFiles = ref([])

const form = reactive({
  stationId: null,
  pileId: null,
  faultType: '',
  description: ''
})

const rules = {
  stationId: [{ required: true, message: '请选择充电站', trigger: 'change' }],
  faultType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
}

const uploadUrl = computed(() => {
  return '/api/file/upload/image'
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`
  }
})

onMounted(() => {
  loadStations()
})

const loadStations = async () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(async (position) => {
      const { longitude, latitude } = position.coords
      try {
        const res = await getNearbyStations({ longitude, latitude, radius: 10000 })
        if (res.code === 200) {
          stations.value = res.data.records || []
        }
      } catch (error) {
        console.error('加载充电站失败:', error)
      }
    })
  }
}

const loadPiles = async () => {
  if (!form.stationId) return
  try {
    const res = await getStationPiles(form.stationId)
    if (res.code === 200) {
      piles.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载充电桩失败')
  }
}

const beforeUpload = (file) => {
  imageFiles.value.push(file)
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    uploadedImages.value.push(response.data)
    ElMessage.success('图片上传成功')
  }
}

const handleRemove = (file) => {
  const index = uploadedImages.value.findIndex(url => url === file.response?.data)
  if (index > -1) {
    uploadedImages.value.splice(index, 1)
    imageFiles.value.splice(index, 1)
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await reportFault({
          stationId: form.stationId,
          pileId: form.pileId,
          faultType: form.faultType,
          description: form.description,
          images: imageFiles.value
        })
        if (res.code === 200) {
          ElMessage.success('故障上报成功')
          router.back()
        }
      } catch (error) {
        ElMessage.error('上报失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.fault-report {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}
</style>

