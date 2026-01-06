<template>
  <div class="scan-charge">
    <el-card>
      <template #header>
        <span>扫码充电</span>
      </template>
      
      <div class="scan-area">
        <div class="scan-box">
          <el-icon class="scan-icon"><Camera /></el-icon>
          <p>请扫描充电桩上的二维码</p>
          <el-button type="primary" @click="handleScan">开始扫描</el-button>
        </div>
      </div>

      <el-divider>或</el-divider>

      <div class="manual-input">
        <el-input
          v-model="qrCode"
          placeholder="请输入二维码内容"
          @keyup.enter="handleManualInput"
        >
          <template #append>
            <el-button @click="handleManualInput">确认</el-button>
          </template>
        </el-input>
      </div>

      <div v-if="scannedPile" class="pile-info">
        <el-card>
          <h3>{{ scannedPile.pileNumber }}</h3>
          <p>功率: {{ scannedPile.power }}kW</p>
          <p>类型: {{ scannedPile.type === 1 ? '快充' : '慢充' }}</p>
          <p>状态: 
            <el-tag :type="scannedPile.status === 0 ? 'success' : 'warning'">
              {{ getStatusText(scannedPile.status) }}
            </el-tag>
          </p>
          <el-button
            v-if="scannedPile.status === 0"
            type="primary"
            size="large"
            @click="handleStartCharging"
            style="width: 100%; margin-top: 15px;"
          >
            开始充电
          </el-button>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { scanQRCode } from '@/api/pile'
import { startCharging } from '@/api/order'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'

const router = useRouter()
const qrCode = ref('')
const scannedPile = ref(null)

const handleScan = () => {
  // 这里应该集成真实的扫码功能（如使用html5-qrcode库）
  ElMessage.info('扫码功能需要调用设备摄像头，请使用手动输入或真实扫码库')
}

const handleManualInput = async () => {
  if (!qrCode.value) {
    ElMessage.warning('请输入二维码内容')
    return
  }

  try {
    const res = await scanQRCode(qrCode.value)
    if (res.code === 200) {
      scannedPile.value = res.data
      ElMessage.success('扫码成功')
    }
  } catch (error) {
    ElMessage.error('无效的二维码')
    scannedPile.value = null
  }
}

const getStatusText = (status) => {
  const texts = { 0: '空闲', 1: '占用', 2: '故障', 3: '离线' }
  return texts[status] || '未知'
}

const handleStartCharging = async () => {
  try {
    const res = await startCharging(null, scannedPile.value.qrCode)
    if (res.code === 200) {
      ElMessage.success('开始充电')
      router.push('/charging')
    }
  } catch (error) {
    ElMessage.error('启动充电失败')
  }
}
</script>

<style scoped>
.scan-charge {
  padding: 20px;
  max-width: 500px;
  margin: 0 auto;
}

.scan-area {
  text-align: center;
  padding: 40px 20px;
}

.scan-box {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px;
}

.scan-icon {
  font-size: 64px;
  color: #409EFF;
  margin-bottom: 20px;
}

.manual-input {
  margin: 20px 0;
}

.pile-info {
  margin-top: 20px;
}

.pile-info h3 {
  margin-bottom: 15px;
}

.pile-info p {
  margin: 10px 0;
  color: #666;
}
</style>


