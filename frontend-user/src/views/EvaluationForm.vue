<template>
  <div class="evaluation-form">
    <el-card>
      <template #header>
        <span>评价充电站</span>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="总体评分" prop="score">
          <el-rate v-model="form.score" :max="5" />
        </el-form-item>
        <el-form-item label="环境评分" prop="environmentScore">
          <el-rate v-model="form.environmentScore" :max="5" />
        </el-form-item>
        <el-form-item label="服务评分" prop="serviceScore">
          <el-rate v-model="form.serviceScore" :max="5" />
        </el-form-item>
        <el-form-item label="设备评分" prop="equipmentScore">
          <el-rate v-model="form.equipmentScore" :max="5" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容"
          />
        </el-form-item>
        <el-form-item label="评价图片">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :file-list="imageList"
            :limit="3"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交评价</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createEvaluation, uploadEvaluationImage } from '@/api/evaluation'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const imageList = ref([])
const uploadedImages = ref([])

const form = reactive({
  stationId: null,
  orderId: null,
  score: 5,
  environmentScore: 5,
  serviceScore: 5,
  equipmentScore: 5,
  content: ''
})

const rules = {
  score: [
    { required: true, message: '请选择总体评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' }
  ]
}

const uploadUrl = computed(() => {
  return '/api/evaluation/upload'
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`
  }
})

onMounted(() => {
  if (route.query.stationId) {
    form.stationId = parseInt(route.query.stationId)
  }
  if (route.query.orderId) {
    form.orderId = parseInt(route.query.orderId)
  }
})

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
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const evaluationData = {
          ...form,
          images: uploadedImages.value.join(',')
        }
        const res = await createEvaluation(evaluationData)
        if (res.code === 200) {
          ElMessage.success('评价成功')
          router.back()
        }
      } catch (error) {
        ElMessage.error('评价失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.evaluation-form {
  padding: 20px;
}
</style>

