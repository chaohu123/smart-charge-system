<template>
  <div class="route-search page-container">
    <div class="page-header">
      <h2>沿途搜索</h2>
      <p class="subtitle">规划行程中的充电站</p>
    </div>

    <div class="main-content">
      <el-card>
        <el-form :model="routeForm" label-width="100px">
          <el-form-item label="起点">
            <el-input
              v-model="routeForm.startLocation"
              placeholder="请输入起点地址"
              @blur="geocodeLocation('start')"
            >
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="终点">
            <el-input
              v-model="routeForm.endLocation"
              placeholder="请输入终点地址"
              @blur="geocodeLocation('end')"
            >
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="车辆续航">
            <el-input-number
              v-model="routeForm.vehicleRange"
              :min="100"
              :max="1000"
              :step="10"
              placeholder="公里"
              style="width: 100%"
            />
            <span class="form-tip">单位：公里</span>
          </el-form-item>

          <el-form-item label="充电类型">
            <el-radio-group v-model="routeForm.chargeType">
              <el-radio :label="1">快充</el-radio>
              <el-radio :label="0">慢充</el-radio>
              <el-radio :label="null">不限</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="searchRoute" :loading="loading" style="width: 100%">
              搜索沿途充电站
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 地图显示 -->
      <el-card v-if="showMap" class="map-card">
        <template #header>
          <span>路线规划</span>
        </template>
        <div id="routeMap" class="route-map"></div>
      </el-card>

      <!-- 充电站列表 -->
      <el-card v-if="stations.length > 0" class="stations-card">
        <template #header>
          <span>沿途充电站 ({{ stations.length }})</span>
        </template>
        <div class="station-list">
          <div
            v-for="(station, index) in stations"
            :key="station.id"
            class="station-item"
            @click="goToStationDetail(station.id)"
          >
            <div class="station-index">{{ index + 1 }}</div>
            <div class="station-info">
              <h3>{{ station.name }}</h3>
              <p class="address">{{ station.address }}</p>
              <div class="station-meta">
                <span>距离: {{ station.distance }}m</span>
                <span>可用: {{ station.availablePiles }}/{{ station.totalPiles }}</span>
                <span>服务费: ¥{{ station.serviceFee }}/kWh</span>
              </div>
            </div>
            <el-button type="primary" size="small" @click.stop="navigateToStation(station)">
              导航
            </el-button>
          </div>
        </div>
      </el-card>

      <el-empty v-if="!loading && stations.length === 0 && routeForm.startLocation && routeForm.endLocation" description="未找到沿途充电站" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { searchRouteStations } from '@/api/station'

const router = useRouter()
const loading = ref(false)
const showMap = ref(false)
const stations = ref([])
const routeForm = reactive({
  startLocation: '',
  endLocation: '',
  startLng: null,
  startLat: null,
  endLng: null,
  endLat: null,
  vehicleRange: 400,
  chargeType: null
})

let routeMap = null

onMounted(() => {
  // 尝试获取当前位置作为起点
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((position) => {
      // 可以自动填充起点，但这里不自动填充，让用户手动输入
    })
  }
})

const geocodeLocation = async (type) => {
  const address = type === 'start' ? routeForm.startLocation : routeForm.endLocation
  if (!address || !window.AMap) return

  try {
    AMap.plugin('AMap.Geocoder', () => {
      const geocoder = new AMap.Geocoder()
      geocoder.getLocation(address, (status, result) => {
        if (status === 'complete' && result.geocodes.length > 0) {
          const location = result.geocodes[0].location
          if (type === 'start') {
            routeForm.startLng = location.lng
            routeForm.startLat = location.lat
          } else {
            routeForm.endLng = location.lng
            routeForm.endLat = location.lat
          }
        }
      })
    })
  } catch (error) {
    console.error('地理编码失败:', error)
  }
}

const searchRoute = async () => {
  if (!routeForm.startLocation || !routeForm.endLocation) {
    ElMessage.warning('请填写起点和终点')
    return
  }

  // 如果还没有坐标，先进行地理编码
  if (!routeForm.startLng || !routeForm.startLat) {
    await geocodeLocation('start')
  }
  if (!routeForm.endLng || !routeForm.endLat) {
    await geocodeLocation('end')
  }

  if (!routeForm.startLng || !routeForm.endLng) {
    ElMessage.error('无法获取起点或终点的坐标，请检查地址是否正确')
    return
  }

  loading.value = true
  try {
    const res = await searchRouteStations({
      startLng: routeForm.startLng,
      startLat: routeForm.startLat,
      endLng: routeForm.endLng,
      endLat: routeForm.endLat,
      vehicleRange: routeForm.vehicleRange,
      chargeType: routeForm.chargeType
    })

    if (res.code === 200) {
      stations.value = res.data || []
      if (stations.value.length > 0) {
        showMap.value = true
        await nextTick()
        initRouteMap()
      } else {
        ElMessage.info('未找到沿途充电站')
      }
    }
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error('搜索沿途充电站失败:', error)
  } finally {
    loading.value = false
  }
}

const initRouteMap = () => {
  if (!window.AMap) {
    ElMessage.warning('地图SDK未加载')
    return
  }

  if (routeMap) {
    routeMap.destroy()
  }

  routeMap = new AMap.Map('routeMap', {
    zoom: 10,
    center: [
      (routeForm.startLng + routeForm.endLng) / 2,
      (routeForm.startLat + routeForm.endLat) / 2
    ]
  })

  // 添加起点和终点标记
  const startMarker = new AMap.Marker({
    position: [routeForm.startLng, routeForm.startLat],
    title: '起点',
    icon: new AMap.Icon({
      size: new AMap.Size(32, 32),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/start.png',
      imageSize: new AMap.Size(32, 32)
    })
  })

  const endMarker = new AMap.Marker({
    position: [routeForm.endLng, routeForm.endLat],
    title: '终点',
    icon: new AMap.Icon({
      size: new AMap.Size(32, 32),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/end.png',
      imageSize: new AMap.Size(32, 32)
    })
  })

  routeMap.add([startMarker, endMarker])

  // 添加路线
  AMap.plugin('AMap.Driving', () => {
    const driving = new AMap.Driving({
      map: routeMap,
      panel: null
    })

    driving.search(
      [routeForm.startLng, routeForm.startLat],
      [routeForm.endLng, routeForm.endLat],
      (status, result) => {
        if (status === 'complete') {
          // 路线规划成功
        }
      }
    )
  })

  // 添加充电站标记
  stations.value.forEach((station, index) => {
    const marker = new AMap.Marker({
      position: [station.longitude, station.latitude],
      title: station.name,
      icon: new AMap.Icon({
        size: new AMap.Size(28, 28),
        image: `data:image/svg+xml;base64,${btoa(`<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28"><circle cx="14" cy="14" r="10" fill="#409EFF"/><text x="14" y="18" text-anchor="middle" fill="white" font-size="12" font-weight="bold">${index + 1}</text></svg>`)}`,
        imageSize: new AMap.Size(28, 28)
      })
    })

    marker.on('click', () => {
      goToStationDetail(station.id)
    })

    routeMap.add(marker)
  })

  // 调整视野
  if (stations.value.length > 0) {
    routeMap.setFitView(null, false, [50, 50, 50, 50])
  }
}

const goToStationDetail = (stationId) => {
  router.push(`/station/${stationId}`)
}

const navigateToStation = (station) => {
  if (!window.AMap) return

  AMap.plugin('AMap.Driving', () => {
    const driving = new AMap.Driving({
      map: routeMap,
      panel: null
    })

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((position) => {
        const { longitude, latitude } = position.coords
        driving.search(
          [longitude, latitude],
          [station.longitude, station.latitude],
          (status, result) => {
            if (status === 'complete') {
              ElMessage.success('导航路线已规划')
            }
          }
        )
      })
    }
  })
}
</script>

<style scoped>
.route-search {
  min-height: 100vh;
  background-color: var(--background-color-base);
}

.page-header {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light-color));
  color: white;
  padding: var(--spacing-xl);
  text-align: center;
}

.page-header h2 {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: var(--font-size-xxl);
}

.subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: var(--font-size-md);
}

.main-content {
  padding: var(--spacing-lg);
}

.map-card {
  margin-top: var(--spacing-lg);
}

.route-map {
  width: 100%;
  height: 400px;
  border-radius: var(--border-radius-medium);
  overflow: hidden;
}

.stations-card {
  margin-top: var(--spacing-lg);
}

.station-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.station-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--background-color-base);
  border-radius: var(--border-radius-medium);
  cursor: pointer;
  transition: all var(--transition-base);
}

.station-item:hover {
  background: var(--background-color-hover);
  transform: translateY(-2px);
  box-shadow: var(--box-shadow-light);
}

.station-index {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.station-info {
  flex: 1;
}

.station-info h3 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.address {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin: var(--spacing-xs) 0;
}

.station-meta {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xs);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.form-tip {
  margin-left: var(--spacing-sm);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

@media (max-width: 768px) {
  .station-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .station-meta {
    flex-direction: column;
    gap: var(--spacing-xs);
  }
}
</style>

