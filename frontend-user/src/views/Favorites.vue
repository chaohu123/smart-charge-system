<template>
  <div class="favorites-page">
    <!-- 页面头部 -->
    <div class="favorites__header">
      <!-- 返回按钮 -->
      <el-button 
        class="favorites__back-button" 
        text 
        @click="handleGoBack"
      >
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      
      <!-- 标题和副标题（居中） -->
      <div class="favorites__header-content">
        <h1 class="favorites__title">我的收藏</h1>
        <p class="favorites__subtitle">查看您收藏的充电站</p>
      </div>
      
      <!-- 主页按钮 -->
      <el-button 
        class="favorites__home-button" 
        text 
        @click="handleGoHome"
      >
        <el-icon><HomeFilled /></el-icon>
        <span>主页</span>
      </el-button>
    </div>

    <div class="page-content">
      <div v-if="loading" class="loading-wrapper">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <el-empty v-else-if="displayedFavorites.length === 0" description="暂无收藏的充电站" />
      
      <div v-else>
        <div class="favorites-grid">
          <div
            v-for="favorite in displayedFavorites"
            :key="favorite.id"
            class="favorite-card-wrapper"
          >
            <StationCard
              v-if="stationMap[favorite.stationId]"
              :station="stationMap[favorite.stationId]"
              :is-favorite="true"
              @click="goToStation(favorite.stationId)"
              @navigate="handleNavigate(stationMap[favorite.stationId])"
              @reserve="handleReserve(stationMap[favorite.stationId])"
              @toggle-favorite="handleRemoveFavorite(favorite.stationId)"
            />
          </div>
        </div>
        
        <!-- 分页组件 -->
        <div v-if="total > 0" class="favorites__pagination">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="5"
            :total="total"
            layout="total, prev, pager, next, jumper"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 地图对话框 -->
    <el-dialog
      v-model="mapDialogVisible"
      title="查看地图"
      width="90%"
      :before-close="handleMapDialogClose"
      class="map-dialog"
    >
      <div class="map-dialog__content">
        <div :id="`favorite-map-container-${currentStation?.id || 'default'}`" class="map-dialog__map" ref="mapContainerRef"></div>
        <div class="map-dialog__info">
          <div class="map-info-card">
            <h4>{{ currentStation?.name || '充电站' }}</h4>
            <p class="map-info-address">
              <el-icon><Location /></el-icon>
              {{ currentStation?.address || '地址信息待完善' }}
            </p>
            <div class="map-info-actions">
              <el-button type="primary" @click="handleStartNavigation">
                <el-icon><Guide /></el-icon>
                开始导航
              </el-button>
            </div>
          </div>
          <div v-if="showNavigationRoute" class="navigation-route">
            <h5>导航路线</h5>
            <div class="route-info">
              <div class="route-item">
                <span class="route-label">距离：</span>
                <span class="route-value">{{ navigationDistance || '计算中...' }}</span>
              </div>
              <div class="route-item">
                <span class="route-label">预计时间：</span>
                <span class="route-value">{{ navigationDuration || '计算中...' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 充电桩列表对话框 -->
    <el-dialog
      v-model="pilesDialogVisible"
      :title="`${currentStation?.name || '充电站'} - 充电桩列表`"
      width="80%"
      :before-close="handlePilesDialogClose"
    >
      <div v-if="pilesLoading" class="piles-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <el-table v-else :data="piles" style="width: 100%">
        <el-table-column prop="pileNumber" label="桩号" width="120" />
        <el-table-column prop="power" label="功率(kW)" width="120">
          <template #default="scope">
            {{ formatPower(scope.row.power) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            {{ scope.row.type === 1 ? '快充' : '慢充' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getPileStatusType(scope.row.status)">
              {{ getPileStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                v-if="scope.row.status === 0"
                type="primary"
                size="small"
                @click="handlePileReserve(scope.row)"
              >
                预约
              </el-button>
              <el-button
                v-if="scope.row.status === 0"
                type="success"
                size="small"
                @click="handlePileCharge(scope.row)"
              >
                立即充电
              </el-button>
              <span v-if="scope.row.status !== 0" class="no-action">—</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handlePilesDialogClose">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavoriteList, removeFavorite } from '@/api/favorite'
import { getStationDetail, getStationPiles } from '@/api/station'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Loading, 
  ArrowLeft, 
  HomeFilled, 
  Location, 
  Guide 
} from '@element-plus/icons-vue'
import StationCard from '@/components/StationCard.vue'

const router = useRouter()
const favorites = ref([])
const stationMap = ref({})
const loading = ref(true)

// 分页相关
const currentPage = ref(1)
const pageSize = 5
const total = computed(() => favorites.value.length)

// 计算当前页显示的收藏列表
const displayedFavorites = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return favorites.value.slice(start, end)
})

// 地图对话框相关
const mapDialogVisible = ref(false)
const mapContainerRef = ref(null)
const showNavigationRoute = ref(false)
const navigationDistance = ref('')
const navigationDuration = ref('')
const currentStation = ref(null)
let mapInstance = null
let navigationMarker = null
let navigationRoute = null

// 充电桩列表对话框相关
const pilesDialogVisible = ref(false)
const piles = ref([])
const pilesLoading = ref(false)

onMounted(() => {
  loadFavorites()
})

onUnmounted(() => {
  // 清理地图实例
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
})

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavoriteList()
    if (res.code === 200) {
      favorites.value = res.data || []
      // 并行加载所有充电站详情
      if (favorites.value.length > 0) {
        await Promise.all(
          favorites.value.map(favorite => loadStationInfo(favorite.stationId))
        )
      }
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const loadStationInfo = async (stationId) => {
  try {
    const res = await getStationDetail(stationId)
    if (res.code === 200 && res.data) {
      stationMap.value[stationId] = res.data
    }
  } catch (error) {
    console.error('加载充电站信息失败:', error)
  }
}

// 取消收藏
const handleRemoveFavorite = async (stationId) => {
  if (!stationId) return
  
  try {
    await ElMessageBox.confirm(
      '确定要取消收藏该充电站吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await removeFavorite(stationId)
    if (res.code === 200) {
      ElMessage.success('取消收藏成功')
      // 从列表中移除
      favorites.value = favorites.value.filter(f => f.stationId !== stationId)
      delete stationMap.value[stationId]
      
      // 如果当前页没有数据了，跳转到上一页
      if (displayedFavorites.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
      }
    } else {
      ElMessage.error(res.message || '取消收藏失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
      ElMessage.error('取消收藏失败')
    }
  }
}

const goToStation = (stationId) => {
  router.push(`/station/${stationId}`)
}

// 导航操作（打开地图对话框）
const handleNavigate = (station) => {
  if (!station) return
  
  if (!station.longitude || !station.latitude) {
    ElMessage.warning('该充电站位置信息不完整')
    return
  }
  
  currentStation.value = station
  mapDialogVisible.value = true
  
  // 等待对话框打开后再初始化地图
  setTimeout(() => {
    initMapDialog()
  }, 300)
}

// 初始化地图对话框
const initMapDialog = () => {
  if (!mapContainerRef.value || !currentStation.value) return
  
  // 检查是否已加载高德地图SDK
  if (typeof window.AMap === 'undefined') {
    ElMessage.warning('地图SDK未加载，请检查配置')
    return
  }
  
  try {
    // 清除旧地图
    if (mapInstance) {
      mapInstance.destroy()
      mapInstance = null
    }
    
    const center = [Number(currentStation.value.longitude), Number(currentStation.value.latitude)]
    const mapId = `favorite-map-container-${currentStation.value.id || 'default'}`
    
    mapInstance = new window.AMap.Map(mapId, {
      zoom: 15,
      center: center,
      mapStyle: 'amap://styles/normal'
    })
    
    // 添加充电站标记
    if (navigationMarker) {
      mapInstance.remove(navigationMarker)
    }
    
    navigationMarker = new window.AMap.Marker({
      position: center,
      title: currentStation.value.name,
      icon: new window.AMap.Icon({
        size: new window.AMap.Size(40, 40),
        image: 'https://webapi.amap.com/theme/v1.3/markers/n/mid_r.png',
        imageSize: new window.AMap.Size(40, 40)
      })
    })
    
    mapInstance.add(navigationMarker)
    
    // 获取当前位置并规划路线
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { longitude, latitude } = position.coords
          planRoute([longitude, latitude], center)
        },
        () => {
          ElMessage.warning('获取当前位置失败，无法规划路线')
        }
      )
    }
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败')
  }
}

// 规划导航路线
const planRoute = (start, end) => {
  if (!mapInstance || typeof window.AMap === 'undefined') return
  
  try {
    // 使用高德地图路径规划
    const driving = new window.AMap.Driving({
      map: mapInstance,
      panel: null,
      hideMarkers: false
    })
    
    driving.search(
      new window.AMap.LngLat(start[0], start[1]),
      new window.AMap.LngLat(end[0], end[1]),
      (status, result) => {
        if (status === 'complete' && result.routes && result.routes.length > 0) {
          const route = result.routes[0]
          navigationDistance.value = formatDistance(route.distance)
          navigationDuration.value = formatDurationFromSeconds(route.time)
          showNavigationRoute.value = true
          
          // 保存路线对象，用于后续导航
          navigationRoute = route
        } else {
          ElMessage.warning('路线规划失败')
        }
      }
    )
  } catch (error) {
    console.error('路线规划失败:', error)
  }
}

// 格式化距离
const formatDistance = (distance) => {
  if (!distance) return '未知'
  if (distance < 1000) {
    return `${distance}米`
  }
  return `${(distance / 1000).toFixed(1)}公里`
}

// 格式化时长（从秒数）
const formatDurationFromSeconds = (seconds) => {
  if (!seconds) return '未知'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
}

// 开始导航
const handleStartNavigation = () => {
  if (!currentStation.value?.longitude || !currentStation.value?.latitude) {
    ElMessage.warning('充电站位置信息不完整')
    return
  }
  
  // 使用高德地图导航
  const lng = Number(currentStation.value.longitude)
  const lat = Number(currentStation.value.latitude)
  const name = currentStation.value.name || '充电站'
  
  // 打开高德地图导航
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { longitude, latitude } = position.coords
        // 使用高德地图导航URL
        const url = `https://uri.amap.com/navigation?to=${lng},${lat},${name}&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=1`
        window.open(url, '_blank')
        ElMessage.success('已打开导航')
      },
      () => {
        // 如果获取位置失败，直接使用目标位置导航
        const url = `https://uri.amap.com/navigation?to=${lng},${lat},${name}&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=1`
        window.open(url, '_blank')
        ElMessage.success('已打开导航')
      }
    )
  } else {
    // 浏览器不支持定位，直接导航
    const url = `https://uri.amap.com/navigation?to=${lng},${lat},${name}&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=1`
    window.open(url, '_blank')
    ElMessage.success('已打开导航')
  }
}

// 关闭地图对话框
const handleMapDialogClose = () => {
  mapDialogVisible.value = false
  showNavigationRoute.value = false
  navigationDistance.value = ''
  navigationDuration.value = ''
  navigationRoute = null
  
  // 清理地图实例
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
  if (navigationMarker) {
    navigationMarker = null
  }
}

// 预约操作（打开充电桩列表对话框）
const handleReserve = async (station) => {
  if (!station) return
  
  currentStation.value = station
  pilesDialogVisible.value = true
  await loadPiles()
}

// 加载充电桩列表
const loadPiles = async () => {
  if (!currentStation.value?.id) return
  
  pilesLoading.value = true
  try {
    const res = await getStationPiles(currentStation.value.id)
    if (res.code === 200) {
      piles.value = res.data || []
    } else {
      ElMessage.error('加载充电桩列表失败')
    }
  } catch (error) {
    console.error('加载充电桩列表失败:', error)
    ElMessage.error('加载充电桩列表失败')
  } finally {
    pilesLoading.value = false
  }
}

// 格式化功率
const formatPower = (power) => {
  if (power === null || power === undefined || power === '') {
    return '--'
  }
  const num = Number(power)
  if (isNaN(num)) {
    return '--'
  }
  return num.toFixed(1)
}

// 获取充电桩状态类型
const getPileStatusType = (status) => {
  const types = {
    0: 'success', // 空闲
    1: 'warning', // 占用
    2: 'danger',  // 故障
    3: 'info'     // 离线
  }
  return types[status] || 'info'
}

// 获取充电桩状态文本
const getPileStatusText = (status) => {
  const texts = {
    0: '空闲',
    1: '占用',
    2: '故障',
    3: '离线'
  }
  return texts[status] || '未知'
}

// 充电桩预约
const handlePileReserve = (pile) => {
  router.push({
    path: '/reservation',
    query: { 
      stationId: currentStation.value.id,
      pileId: pile.id
    }
  })
  pilesDialogVisible.value = false
}

// 充电桩立即充电
const handlePileCharge = (pile) => {
  router.push({
    path: '/station/' + currentStation.value.id,
    query: { pileId: pile.id }
  })
  pilesDialogVisible.value = false
}

// 关闭充电桩列表对话框
const handlePilesDialogClose = () => {
  pilesDialogVisible.value = false
  piles.value = []
  currentStation.value = null
}

// 分页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 返回
const handleGoBack = () => {
  router.back()
}

// 主页
const handleGoHome = () => {
  router.push('/')
}
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 40px;
}

.favorites__header {
  background: linear-gradient(135deg, #1a6dff 0%, #00b894 100%);
  color: white;
  padding: 40px 32px;
  margin-bottom: 24px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.favorites__back-button {
  position: absolute;
  left: 32px;
  color: white;
  font-size: 16px;
}

.favorites__back-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.favorites__header-content {
  flex: 1;
  text-align: center;
}

.favorites__title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
}

.favorites__subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.favorites__home-button {
  position: absolute;
  right: 32px;
  color: white;
  font-size: 16px;
}

.favorites__home-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.page-content {
  max-width: 1920px;
  margin: 0 auto;
  padding: 0 32px;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #757575;
}

.loading-wrapper .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
}

.favorite-card-wrapper {
  transition: transform 0.3s;
}

.favorite-card-wrapper:hover {
  transform: translateY(-4px);
}

.favorites__pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 地图对话框样式 */
.map-dialog__content {
  display: flex;
  gap: 20px;
  height: 600px;
}

.map-dialog__map {
  flex: 1;
  height: 100%;
  min-height: 500px;
}

.map-dialog__info {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.map-info-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.map-info-card h4 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #333;
}

.map-info-address {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin: 0 0 16px;
}

.map-info-actions {
  display: flex;
  gap: 12px;
}

.navigation-route {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.navigation-route h5 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #333;
}

.route-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.route-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.route-label {
  color: #666;
}

.route-value {
  color: #333;
  font-weight: 500;
}

/* 充电桩列表对话框样式 */
.piles-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #757575;
}

.piles-loading .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.no-action {
  color: #999;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 1400px) {
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}

@media (max-width: 768px) {
  .page-content {
    padding: 0 16px;
  }
  
  .favorites__header {
    padding: 24px 16px;
  }
  
  .favorites__title {
    font-size: 24px;
  }
  
  .favorites__back-button {
    left: 16px;
  }
  
  .favorites__home-button {
    right: 16px;
  }
  
  .favorites-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .map-dialog__content {
    flex-direction: column;
    height: auto;
  }
  
  .map-dialog__map {
    height: 400px;
  }
  
  .map-dialog__info {
    width: 100%;
  }
}
</style>
