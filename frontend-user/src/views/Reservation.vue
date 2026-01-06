<template>
  <div class="reservation">
    <el-container>
      <el-header>
        <h2>预约充电</h2>
      </el-header>
      <el-main>
        <el-card>
          <el-form :model="reservationForm" label-width="120px">
            <el-form-item label="选择充电站">
              <el-select v-model="reservationForm.stationId" placeholder="请选择充电站" @change="loadPiles">
                <el-option
                  v-for="station in stations"
                  :key="station.id"
                  :label="station.name"
                  :value="station.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="选择充电桩">
              <el-select v-model="reservationForm.pileId" placeholder="请先选择充电站">
                <el-option
                  v-for="pile in piles"
                  :key="pile.id"
                  :label="`${pile.pileNumber} - ${pile.type === 1 ? '快充' : '慢充'} - ${pile.power}kW`"
                  :value="pile.id"
                  :disabled="pile.status !== 0"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="预约时间">
              <el-date-picker
                v-model="reservationForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="预约时长">
              <el-select v-model="reservationForm.duration">
                <el-option label="30分钟" :value="30" />
                <el-option label="1小时" :value="60" />
                <el-option label="2小时" :value="120" />
                <el-option label="3小时" :value="180" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCreateReservation">提交预约</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="reservation-list">
          <h3>我的预约</h3>
          <el-table :data="reservations" style="width: 100%">
            <el-table-column prop="stationName" label="充电站" />
            <el-table-column prop="pileNumber" label="充电桩" />
            <el-table-column prop="startTime" label="开始时间" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 0"
                  type="danger"
                  size="small"
                  @click="handleCancel(scope.row.id)"
                >
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { createReservation, getReservationList, cancelReservation } from '@/api/reservation'
import { getNearbyStations } from '@/api/station'
import { getStationPiles } from '@/api/station'
import { ElMessage } from 'element-plus'

const reservationForm = reactive({
  stationId: null,
  pileId: null,
  startTime: '',
  duration: 60
})

const stations = ref([])
const piles = ref([])
const reservations = ref([])

onMounted(async () => {
  await loadStations()
  await loadReservations()
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
  if (!reservationForm.stationId) return
  try {
    const res = await getStationPiles(reservationForm.stationId)
    if (res.code === 200) {
      piles.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载充电桩失败')
  }
}

const loadReservations = async () => {
  try {
    const res = await getReservationList()
    if (res.code === 200) {
      reservations.value = res.data || []
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
  }
}

const handleCreateReservation = async () => {
  if (!reservationForm.pileId || !reservationForm.startTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await createReservation({
      pileId: reservationForm.pileId,
      startTime: reservationForm.startTime,
      duration: reservationForm.duration
    })
    if (res.code === 200) {
      ElMessage.success('预约成功')
      await loadReservations()
    }
  } catch (error) {
    ElMessage.error('预约失败')
  }
}

const handleCancel = async (id) => {
  try {
    const res = await cancelReservation(id)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      await loadReservations()
    }
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

const getStatusType = (status) => {
  const types = { 0: 'primary', 1: 'success', 2: 'info', 3: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待使用', 1: '已使用', 2: '已取消', 3: '已过期' }
  return texts[status] || '未知'
}
</script>

<style scoped>
.reservation {
  min-height: 100vh;
  background-color: var(--background-color-base);
}

.reservation .el-header {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light-color));
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-large);
  box-shadow: var(--box-shadow-light);
}

.reservation .el-header h2 {
  margin: 0;
  color: #fff;
  font-size: 20px;
}

.reservation .el-main {
  padding: var(--spacing-large);
}

.reservation .el-card {
  border-radius: var(--border-radius-medium);
  box-shadow: var(--box-shadow-light);
  margin-bottom: var(--spacing-large);
}

.reservation-list {
  margin-top: var(--spacing-large);
}

.reservation .el-form-item {
  margin-bottom: var(--spacing-medium);
}

.reservation .el-table {
  border-radius: var(--border-radius-small);
  overflow: hidden;
}
</style>

