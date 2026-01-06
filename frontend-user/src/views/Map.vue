<template>
  <div class="map-container">
    <div id="map" class="map"></div>
    <div class="map-controls">
      <el-card>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索充电站"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filterType" placeholder="充电类型" @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="快充" :value="1" />
          <el-option label="慢充" :value="0" />
        </el-select>
        <el-button type="primary" @click="locateMe">定位</el-button>
      </el-card>
    </div>
    <div class="station-list-panel" v-if="selectedStation">
      <el-card>
        <div class="station-info-header">
          <h3>{{ selectedStation.name }}</h3>
          <el-tag :type="selectedStation.availablePiles > 0 ? 'success' : 'danger'" size="small">
            {{ selectedStation.availablePiles > 0 ? '可用' : '已满' }}
          </el-tag>
        </div>
        <p class="station-address">
          <el-icon><Location /></el-icon>
          {{ selectedStation.address }}
        </p>
        <div class="station-meta">
          <span>可用: {{ selectedStation.availablePiles }}/{{ selectedStation.totalPiles }}</span>
          <span v-if="selectedStation.distance">距离: {{ selectedStation.distance }}m</span>
          <span>服务费: ¥{{ selectedStation.serviceFee }}/kWh</span>
        </div>
        <div class="station-actions">
          <el-button type="primary" @click="goToStationDetail">查看详情</el-button>
          <el-button type="success" @click="handleNavigate">导航</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getNearbyStations } from '@/api/station'

const router = useRouter()
const searchKeyword = ref('')
const filterType = ref('')
const selectedStation = ref(null)
let map = null

onMounted(() => {
  initMap()
})

const initMap = () => {
  // 初始化高德地图
  // 需要引入高德地图SDK
  if (window.AMap) {
    map = new AMap.Map('map', {
      zoom: 13,
      center: [116.397428, 39.90923] // 默认北京
    })
    
    // 获取当前位置
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((position) => {
        const { longitude, latitude } = position.coords
        map.setCenter([longitude, latitude])
        loadStations(longitude, latitude)
      })
    }
  }
}

const loadStations = async (longitude, latitude) => {
  try {
    const res = await getNearbyStations({
      longitude,
      latitude,
      radius: 10000,
      type: filterType.value || undefined
    })
    if (res.code === 200) {
      const stations = res.data.records || []
      
      // 清除旧标记
      map.clearMap()
      
      stations.forEach(station => {
        // 根据状态设置不同颜色的标记
        let iconColor = '#00C853' // 默认绿色（可用）
        if (station.availablePiles === 0) {
          iconColor = '#F44336' // 红色（已满）
        } else if (station.status === 1) {
          iconColor = '#FF9800' // 橙色（维护中）
        }
        
        const marker = new AMap.Marker({
          position: [station.longitude, station.latitude],
          title: station.name,
          icon: new AMap.Icon({
            size: new AMap.Size(32, 32),
            image: `data:image/svg+xml;base64,${btoa(`<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32"><circle cx="16" cy="16" r="12" fill="${iconColor}"/></svg>`)}`,
            imageSize: new AMap.Size(32, 32)
          }),
          label: {
            content: `<div style="background: white; padding: 2px 6px; border-radius: 4px; font-size: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">${station.availablePiles}/${station.totalPiles}</div>`,
            direction: 'bottom'
          }
        })
        
        marker.on('click', () => {
          selectedStation.value = station
          map.setCenter([station.longitude, station.latitude])
          map.setZoom(15)
        })
        
        map.add(marker)
      })
      
      // 自动调整地图视野
      if (stations.length > 0) {
        map.setFitView(null, false, [50, 50, 50, 50])
      }
    }
  } catch (error) {
    console.error('加载充电站失败:', error)
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  // 使用高德地图地理编码API搜索
  if (window.AMap && window.AMap.plugin) {
    AMap.plugin('AMap.Geocoder', () => {
      const geocoder = new AMap.Geocoder()
      geocoder.getLocation(searchKeyword.value, (status, result) => {
        if (status === 'complete' && result.geocodes.length > 0) {
          const location = result.geocodes[0].location
          map.setCenter([location.lng, location.lat])
          map.setZoom(15)
          loadStations(location.lng, location.lat)
        } else {
          ElMessage.warning('未找到该位置')
        }
      })
    })
  }
}

const handleFilter = () => {
  if (map && navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((position) => {
      const { longitude, latitude } = position.coords
      loadStations(longitude, latitude)
    })
  }
}

const locateMe = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((position) => {
      const { longitude, latitude } = position.coords
      map.setCenter([longitude, latitude])
    })
  }
}

const goToStationDetail = () => {
  if (selectedStation.value) {
    router.push(`/station/${selectedStation.value.id}`)
  }
}

const handleNavigate = () => {
  if (selectedStation.value && window.AMap) {
    // 使用高德地图导航
    AMap.plugin('AMap.Driving', () => {
      const driving = new AMap.Driving({
        map: map,
        panel: 'panel'
      })
      
      // 获取当前位置
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((position) => {
          const { longitude, latitude } = position.coords
          driving.search(
            [longitude, latitude],
            [selectedStation.value.longitude, selectedStation.value.latitude],
            (status, result) => {
              if (status === 'complete') {
                ElMessage.success('导航路线已规划')
              } else {
                ElMessage.error('导航规划失败')
              }
            }
          )
        })
      }
    })
  }
}
</script>

<style scoped>
.map-container {
  position: relative;
  width: 100%;
  height: 100vh;
}

.map {
  width: 100%;
  height: 100%;
}

.map-controls {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 1000;
  width: 300px;
}

.station-list-panel {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  width: 90%;
  max-width: 500px;
}

.station-info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-sm);
}

.station-info-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 600;
  flex: 1;
}

.station-address {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  margin: var(--spacing-sm) 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.station-meta {
  display: flex;
  gap: var(--spacing-md);
  margin: var(--spacing-sm) 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.station-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
}

.station-actions .el-button {
  flex: 1;
}

@media (max-width: 768px) {
  .map-controls {
    width: calc(100% - 40px);
    left: 20px;
  }
  
  .station-list-panel {
    width: calc(100% - 40px);
  }
  
  .station-meta {
    flex-direction: column;
    gap: var(--spacing-xs);
  }
}
</style>

