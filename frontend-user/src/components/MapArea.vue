<template>
  <div class="map-area">
    <div id="map-container" class="map-container" ref="mapContainerRef">
      <!-- 地图占位组件 -->
      <div v-if="!mapLoaded" class="map-placeholder">
        <el-icon class="placeholder-icon"><Location /></el-icon>
        <p>正在加载地图...</p>
        <el-button type="primary" @click="initMap">重新加载</el-button>
      </div>
    </div>
    
    <!-- 地图控制按钮 -->
    <div class="map-controls">
      <el-button
        circle
        size="large"
        class="control-btn"
        @click="locateMe"
        title="定位到我的位置"
      >
        <el-icon><Aim /></el-icon>
      </el-button>
      <el-button
        circle
        size="large"
        class="control-btn"
        @click="zoomIn"
        title="放大"
      >
        <el-icon><Plus /></el-icon>
      </el-button>
      <el-button
        circle
        size="large"
        class="control-btn"
        @click="zoomOut"
        title="缩小"
      >
        <el-icon><Minus /></el-icon>
      </el-button>
    </div>
    
    <!-- 选中站点信息卡片 -->
    <transition name="slide-up">
      <div v-if="selectedStation" class="station-info-card">
        <div class="card-header">
          <h4>{{ selectedStation.name }}</h4>
          <el-button text circle @click="selectedStation = null">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div class="card-content">
          <div class="info-row">
            <el-tag :type="selectedStation.availablePiles > 0 ? 'success' : 'danger'" size="small">
              {{ selectedStation.availablePiles > 0 ? '可用' : '已满' }}
            </el-tag>
            <span class="distance">距离: {{ formatDistance(selectedStation.distance) }}</span>
          </div>
          <p class="address">
            <el-icon><Location /></el-icon>
            {{ selectedStation.address }}
          </p>
          <div class="meta-info">
            <span>可用: {{ selectedStation.availablePiles }}/{{ selectedStation.totalPiles }}</span>
            <span>价格: ¥{{ selectedStation.serviceFee || '—' }}/kWh</span>
          </div>
          <div class="card-actions">
            <el-button type="primary" @click="handleNavigate">导航</el-button>
            <el-button type="success" @click="handleReserve">预约</el-button>
            <el-button @click="handleViewDetail">查看详情</el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Aim, Plus, Minus, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  stations: {
    type: Array,
    default: () => []
  },
  center: {
    type: Array,
    default: () => [116.397428, 39.90923] // 默认北京
  }
})

const emit = defineEmits(['station-click', 'map-ready'])

const router = useRouter()
const mapContainerRef = ref(null)
const mapLoaded = ref(false)
const selectedStation = ref(null)
let map = null
let markers = []

const formatDistance = (distance) => {
  if (!distance) return '未知'
  if (distance < 1000) {
    return `${distance}m`
  }
  return `${(distance / 1000).toFixed(1)}km`
}

const initMap = async () => {
  if (!mapContainerRef.value) return
  
  // 检查是否已加载高德地图SDK
  if (typeof window.AMap === 'undefined') {
    // 如果没有加载SDK，显示占位符
    mapLoaded.value = false
    ElMessage.warning('地图SDK未加载，请检查配置')
    return
  }
  
  try {
    map = new window.AMap.Map('map-container', {
      zoom: 13,
      center: props.center,
      mapStyle: 'amap://styles/normal'
    })
    
    // 调整地图版权信息位置到右下角
    nextTick(() => {
      const copyrightElements = document.querySelectorAll('.amap-copyright')
      copyrightElements.forEach(el => {
        if (el instanceof HTMLElement) {
          el.style.left = 'auto'
          el.style.right = '10px'
          el.style.bottom = '10px'
          el.style.top = 'auto'
        }
      })
    })
    
    mapLoaded.value = true
    
    // 获取当前位置
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { longitude, latitude } = position.coords
          map.setCenter([longitude, latitude])
          emit('map-ready', { longitude, latitude })
        },
        () => {
          emit('map-ready', { longitude: props.center[0], latitude: props.center[1] })
        }
      )
    } else {
      emit('map-ready', { longitude: props.center[0], latitude: props.center[1] })
    }
    
    // 监听地图点击事件
    map.on('click', () => {
      selectedStation.value = null
    })
    
    // 加载站点标记
    updateMarkers()
  } catch (error) {
    console.error('地图初始化失败:', error)
    mapLoaded.value = false
    ElMessage.error('地图加载失败')
  }
}

const updateMarkers = () => {
  if (!map || !props.stations.length) return
  
  // 清除旧标记
  markers.forEach(marker => map.remove(marker))
  markers = []
  
  props.stations.forEach(station => {
    // 根据状态设置不同颜色的标记
    let iconColor = '#4CAF50' // 绿色（可用）
    if (station.availablePiles === 0) {
      iconColor = '#F44336' // 红色（已满）
    } else if (station.status === 1) {
      iconColor = '#FF9800' // 橙色（维护中）
    }
    
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32"><circle cx="16" cy="16" r="12" fill="${iconColor}" stroke="#fff" stroke-width="2"/><text x="16" y="20" text-anchor="middle" fill="#fff" font-size="12" font-weight="bold">⚡</text></svg>`
    // 使用 UTF-8 转码后再 btoa，避免非 Latin1 字符导致报错
    const svgBase64 = btoa(unescape(encodeURIComponent(svg)))

    const marker = new window.AMap.Marker({
      position: [station.longitude, station.latitude],
      title: station.name,
      icon: new window.AMap.Icon({
        size: new window.AMap.Size(32, 32),
        image: `data:image/svg+xml;base64,${svgBase64}`,
        imageSize: new window.AMap.Size(32, 32)
      }),
      label: {
        content: `<div style="background: white; padding: 2px 6px; border-radius: 4px; font-size: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.2); white-space: nowrap;">${station.availablePiles}/${station.totalPiles}</div>`,
        direction: 'bottom',
        offset: new window.AMap.Pixel(0, 5)
      }
    })
    
    marker.on('click', () => {
      selectedStation.value = station
      emit('station-click', station)
      map.setCenter([station.longitude, station.latitude])
      map.setZoom(15)
    })
    
    map.add(marker)
    markers.push(marker)
  })
  
  // 自动调整视野
  if (props.stations.length > 0) {
    map.setFitView(null, false, [50, 50, 50, 50])
  }
}

const locateMe = () => {
  if (!map) return
  
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { longitude, latitude } = position.coords
        map.setCenter([longitude, latitude])
        map.setZoom(15)
        ElMessage.success('已定位到您的位置')
      },
      () => {
        ElMessage.warning('定位失败，请检查定位权限')
      }
    )
  }
}

const zoomIn = () => {
  if (map) {
    map.zoomIn()
  }
}

const zoomOut = () => {
  if (map) {
    map.zoomOut()
  }
}

const handleNavigate = () => {
  if (!selectedStation.value) return
  router.push({
    path: '/map',
    query: { stationId: selectedStation.value.id }
  })
}

const handleReserve = () => {
  if (!selectedStation.value) return
  router.push({
    path: '/reservation',
    query: { stationId: selectedStation.value.id }
  })
}

const handleViewDetail = () => {
  if (!selectedStation.value) return
  router.push(`/station/${selectedStation.value.id}`)
}

watch(() => props.stations, () => {
  updateMarkers()
}, { deep: true })

watch(() => props.center, (newCenter, oldCenter) => {
  if (map && newCenter && newCenter.length === 2) {
    // 检查中心点是否真的改变了
    const centerChanged = !oldCenter || 
      Math.abs(newCenter[0] - oldCenter[0]) > 0.001 || 
      Math.abs(newCenter[1] - oldCenter[1]) > 0.001
    
    if (centerChanged) {
      map.setCenter(newCenter)
      // 中心点改变后，等待地图更新完成再更新标记
      setTimeout(() => {
        updateMarkers()
      }, 300)
    }
  }
})

onMounted(() => {
  nextTick(() => {
    initMap()
  })
})
</script>

<style scoped>
.map-area {
  position: relative;
  flex: 1;
  height: 100%;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
  position: relative;
}

/* 调整高德地图版权信息位置到右下角 */
.map-container :deep(.amap-copyright) {
  left: auto !important;
  right: 10px !important;
  bottom: 10px !important;
  top: auto !important;
}

.map-container :deep(.amap-logo) {
  left: auto !important;
  right: 10px !important;
  bottom: 10px !important;
  top: auto !important;
}

.map-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #757575;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #e0e0e0;
}

.map-controls {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1000;
}

.control-btn {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border: none;
}

.control-btn:hover {
  background: #f5f5f5;
}

.station-info-card {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.card-content {
  padding: 20px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.distance {
  font-size: 14px;
  color: #757575;
}

.address {
  font-size: 14px;
  color: #757575;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-info {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #757575;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-actions .el-button {
  flex: 1;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translateX(-50%) translateY(100%);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translateX(-50%) translateY(100%);
  opacity: 0;
}

@media (max-width: 768px) {
  .station-info-card {
    width: calc(100% - 40px);
    max-width: none;
  }
  
  .map-controls {
    top: 10px;
    right: 10px;
    gap: 8px;
  }
  
  .control-btn {
    width: 40px;
    height: 40px;
  }
}
</style>

