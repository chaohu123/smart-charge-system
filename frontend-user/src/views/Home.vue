<template>
  <div class="home-page">
    <!-- 顶部导航栏 -->
    <Header @city-change="handleCityChange" @login-success="handleLoginSuccess" />
    
    <!-- 快捷数据卡片区（移到顶部） -->
    <div class="stats-section">
      <div class="stats-container">
        <div class="stat-card">
          <div class="stat-icon available">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.availablePiles }}</div>
            <div class="stat-label">空闲桩数量</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon fast">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.fastPiles }}</div>
            <div class="stat-label">快充桩数量</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon distance">
            <el-icon><Location /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatDistance(stats.nearestDistance) }}</div>
            <div class="stat-label">最近充电站距离</div>
          </div>
        </div>
        <div class="stat-card favorite-card" @click="goToFavorites">
          <div class="stat-icon favorite">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.favoriteCount }}</div>
            <div class="stat-label">收藏充电站数量</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 主内容区 -->
    <div class="main-layout">
      <!-- 左侧搜索面板 -->
      <SearchPanel
        :recommended-stations="recommendedStations"
        :loading="loading"
        :initial-filters="currentFilters"
        @search="handleSearch"
        @filter-change="handleFilterChange"
        @station-click="handleStationClick"
        @refresh="loadRecommendedStations"
      />
      
      <!-- 右侧地图区域 -->
      <MapArea
        :stations="mapStations"
        :center="mapCenter"
        @station-click="handleMapStationClick"
        @map-ready="handleMapReady"
      />
    </div>
    
    <!-- 推荐充电站列表 -->
    <div class="recommended-list-section">
      <div class="section-container">
        <div class="section-header">
          <h2>推荐充电站</h2>
          <el-button text @click="$router.push('/station-search')">查看更多</el-button>
        </div>
        <div v-if="loading && !recommendedStations.length" class="loading-wrapper">
          <Skeleton type="card" :rows="3" />
        </div>
        <EmptyState
          v-else-if="!loading && recommendedStations.length === 0"
          description="附近5km内暂无可用充电站，您可以扩大搜索范围或使用地图、沿途搜索功能查找更多充电站。"
          icon="Location"
        />
        <div v-else class="station-grid">
          <div
            v-for="station in recommendedStations"
            :key="station.id"
            class="station-card-wrapper"
          >
            <StationCard
              :station="station"
              :is-favorite="favoriteMap[station.id] || false"
              @click="handleStationCardClick(station)"
              @navigate="handleNavigate(station)"
              @reserve="handleReserve(station)"
              @toggle-favorite="handleToggleFavorite(station)"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷预约悬浮面板 -->
    <transition name="slide-up">
      <div v-if="quickReserveVisible" class="quick-reserve-panel">
        <el-card class="quick-reserve-card" shadow="always">
          <div class="quick-reserve-header">
            <div>
              <h3>{{ quickReserveStation?.name }}</h3>
              <p class="quick-reserve-sub">
                距离 {{ formatDistance(quickReserveStation?.distance) }} ·
                价格 ¥{{ quickReserveStation?.serviceFee || '—' }}/kWh
              </p>
            </div>
            <el-button text circle @click="closeQuickReserve">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="quick-reserve-body">
            <el-form label-width="80px" size="small">
              <el-form-item label="充电枪">
                <el-select v-model="quickReserveGun" placeholder="请选择">
                  <el-option label="1号枪" value="1" />
                  <el-option label="2号枪" value="2" />
                </el-select>
              </el-form-item>
              <el-form-item label="时长">
                <el-select v-model="quickReserveDuration">
                  <el-option label="30分钟" :value="30" />
                  <el-option label="1小时" :value="60" />
                  <el-option label="2小时" :value="120" />
                  <el-option label="3小时" :value="180" />
                </el-select>
              </el-form-item>
              <el-form-item label="预计费用">
                <div class="fee-text">约 ¥{{ estimatedFee.toFixed(2) }}</div>
              </el-form-item>
            </el-form>
          </div>
          <div class="quick-reserve-footer">
            <el-button @click="closeQuickReserve">取消</el-button>
            <el-button type="primary" @click="confirmQuickReserve">前往预约</el-button>
          </div>
        </el-card>
      </div>
    </transition>
    <!-- 页脚 -->
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Lightning, Location, Star, Close } from '@element-plus/icons-vue'
import { getNearbyStations, getRecommendedStations, searchStations } from '@/api/station'
import { getFavoriteList, addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'
import Header from '@/components/Header.vue'
import SearchPanel from '@/components/SearchPanel.vue'
import MapArea from '@/components/MapArea.vue'
import StationCard from '@/components/StationCard.vue'
import Footer from '@/components/Footer.vue'
import Skeleton from '@/components/Skeleton.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const userStore = useUserStore()

// 数据状态
const loading = ref(false)
const recommendedStations = ref([])
const mapStations = ref([])
const mapCenter = ref([116.397428, 39.90923])
const favoriteMap = ref({})
const currentFilters = ref({})

// 统计数据
const stats = ref({
  availablePiles: 0,
  fastPiles: 0,
  nearestDistance: null,
  favoriteCount: 0
})

// 快捷预约相关
const quickReserveStation = ref(null)
const quickReserveGun = ref('1')
const quickReserveDuration = ref(60) // 分钟

const quickReserveVisible = computed(() => !!quickReserveStation.value)

const estimatedFee = computed(() => {
  if (!quickReserveStation.value) return 0
  const price = Number(quickReserveStation.value.serviceFee || 0)
  const hours = quickReserveDuration.value / 60
  const fee = price * hours
  return Number.isNaN(fee) ? 0 : fee
})

// 计算属性
const formatDistance = (distance) => {
  if (!distance) return '—'
  if (distance < 1000) {
    return `${distance}m`
  }
  return `${(distance / 1000).toFixed(1)}km`
}

// 加载推荐充电站
const loadRecommendedStations = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(async (position) => {
        const { longitude, latitude } = position.coords
        mapCenter.value = [longitude, latitude]
        
        try {
          // 优先使用智能推荐接口
          const recommendRes = await getRecommendedStations({
            longitude,
            latitude,
            radius: 5000
          })
          
          if (recommendRes.code === 200 && recommendRes.data && recommendRes.data.length > 0) {
            const MAX_RECOMMEND_SCORE = 110
            recommendedStations.value = recommendRes.data.map((item) => {
              const rawScore = typeof item.recommendScore === 'number' ? item.recommendScore : 0
              const normalized = Math.max(0, Math.min(5, (rawScore / MAX_RECOMMEND_SCORE) * 5))
              return {
                ...item,
                displayScore: normalized
              }
            })
            mapStations.value = [...recommendStations.value]
            updateStats()
            loading.value = false
            return
          }
        } catch (error) {
          console.log('智能推荐失败，使用普通查询:', error)
        }
        
        // 降级使用普通查询
        const res = await getNearbyStations({
          longitude,
          latitude,
          radius: 5000,
          current: 1,
          size: 10
        })
        
        if (res.code === 200) {
          recommendedStations.value = res.data.records || []
          mapStations.value = [...recommendedStations.value]
          updateStats()
        }
        loading.value = false
      }, () => {
        loading.value = false
      })
    }
  } catch (error) {
    console.error('获取附近充电站失败:', error)
    loading.value = false
  }
}

// 更新统计数据
const updateStats = () => {
  if (recommendedStations.value.length === 0) return
  
  let availablePiles = 0
  let fastPiles = 0
  let nearestDistance = null
  
  recommendedStations.value.forEach(station => {
    availablePiles += station.availablePiles || 0
    // 假设type=1为快充
    if (station.type === 1) {
      fastPiles += station.totalPiles || 0
    }
    if (station.distance && (!nearestDistance || station.distance < nearestDistance)) {
      nearestDistance = station.distance
    }
  })
  
  stats.value = {
    ...stats.value,
    availablePiles,
    fastPiles,
    nearestDistance
  }
}

// 加载收藏列表
const loadFavorites = async () => {
  if (!userStore.token) return
  
  try {
    const res = await getFavoriteList()
    if (res.code === 200) {
      const favorites = res.data || []
      favoriteMap.value = {}
      favorites.forEach(fav => {
        favoriteMap.value[fav.stationId] = true
      })
      stats.value.favoriteCount = favorites.length
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
  }
}

// 检查单个站点收藏状态
const checkStationFavorite = async (stationId) => {
  if (!userStore.token) return false
  
  try {
    const res = await checkFavorite(stationId)
    if (res.code === 200) {
      favoriteMap.value[stationId] = res.data || false
      return res.data || false
    }
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
  return false
}

// 处理搜索
const handleSearch = async (keyword) => {
  if (!keyword) {
    loadRecommendedStations()
    return
  }
  
  loading.value = true
  try {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(async (position) => {
        const { longitude, latitude } = position.coords
        const res = await searchStations({
          keyword,
          longitude,
          latitude,
          radius: 10000
        })
        
        if (res.code === 200) {
          recommendedStations.value = res.data.records || []
          mapStations.value = [...recommendedStations.value]
          updateStats()
        }
        loading.value = false
      })
    }
  } catch (error) {
    console.error('搜索失败:', error)
    loading.value = false
  }
}

// 处理筛选变化
const handleFilterChange = async (filters) => {
  currentFilters.value = filters
  loading.value = true
  
  try {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(async (position) => {
        const { longitude, latitude } = position.coords
        const params = {
          longitude,
          latitude,
          radius: filters.distanceRange ? filters.distanceRange[1] * 1000 : 5000,
          current: 1,
          size: 20
        }
        
        if (filters.chargeType === 'fast') {
          params.type = 1
        } else if (filters.chargeType === 'slow') {
          params.type = 0
        }
        
        const res = await getNearbyStations(params)
        
        if (res.code === 200) {
          let stations = res.data.records || []
          
          // 价格筛选
          if (filters.priceRange) {
            stations = stations.filter(s => {
              const price = s.serviceFee || 0
              return price >= filters.priceRange[0] && price <= filters.priceRange[1]
            })
          }
          
          // 状态筛选
          if (filters.status === 'available') {
            stations = stations.filter(s => s.availablePiles > 0)
          }
          
          recommendedStations.value = stations.slice(0, 10)
          mapStations.value = [...stations]
          updateStats()
        }
        loading.value = false
      })
    }
  } catch (error) {
    console.error('筛选失败:', error)
    loading.value = false
  }
}

// 处理站点点击（搜索面板）
const handleStationClick = (station) => {
  mapCenter.value = [station.longitude, station.latitude]
  mapStations.value = [station]
}

// 处理地图站点点击
const handleMapStationClick = (station) => {
  // 地图点击已在MapArea组件中处理
}

// 处理地图就绪
const handleMapReady = (position) => {
  mapCenter.value = [position.longitude, position.latitude]
  loadRecommendedStations()
}

// 处理城市切换
const handleCityChange = (cityInfo) => {
  if (!cityInfo || !cityInfo.longitude || !cityInfo.latitude) {
    ElMessage.warning('城市信息不完整')
    return
  }
  
  // 更新地图中心点到选中城市
  const newCenter = [cityInfo.longitude, cityInfo.latitude]
  mapCenter.value = newCenter
  
  // 提示用户
  ElMessage.success(`已切换到${cityInfo.label}`)
  
  // 重新加载该城市的充电站（使用更大的搜索半径，确保能找到数据）
  loadStationsByCity(cityInfo.longitude, cityInfo.latitude)
}

// 根据指定坐标加载充电站
const loadStationsByCity = async (longitude, latitude) => {
  if (loading.value) return
  
  loading.value = true
  try {
    // 优先使用智能推荐接口（扩大搜索半径到10km，确保能找到数据）
    const recommendRes = await getRecommendedStations({
      longitude,
      latitude,
      radius: 10000
    })
    
    if (recommendRes.code === 200 && recommendRes.data && recommendRes.data.length > 0) {
      const MAX_RECOMMEND_SCORE = 110
      recommendedStations.value = recommendRes.data.map((item) => {
        const rawScore = typeof item.recommendScore === 'number' ? item.recommendScore : 0
        const normalized = Math.max(0, Math.min(5, (rawScore / MAX_RECOMMEND_SCORE) * 5))
        return {
          ...item,
          displayScore: normalized
        }
      })
      mapStations.value = [...recommendedStations.value]
      updateStats()
      loading.value = false
      return
    }
  } catch (error) {
    console.log('智能推荐失败，使用普通查询:', error)
  }
  
  // 降级使用普通查询（扩大搜索半径到10km）
  try {
    const res = await getNearbyStations({
      longitude,
      latitude,
      radius: 10000,
      current: 1,
      size: 20
    })
    
    if (res.code === 200) {
      const stations = res.data.records || []
      recommendedStations.value = stations.slice(0, 10)
      mapStations.value = [...stations]
      updateStats()
      
      if (stations.length === 0) {
        ElMessage.info('该城市暂无充电站数据')
      }
    }
  } catch (error) {
    console.error('获取充电站失败:', error)
    ElMessage.error('加载充电站失败')
  } finally {
    loading.value = false
  }
}

// 处理登录成功
const handleLoginSuccess = () => {
  loadFavorites()
  loadRecommendedStations()
}

// 处理站点卡片点击
const handleStationCardClick = (station) => {
  router.push(`/station/${station.id}`)
}

// 处理导航
const handleNavigate = (station) => {
  if (!station || !station.longitude || !station.latitude) {
    ElMessage.warning('该充电站位置信息不完整')
    return
  }
  
  // 使用高德地图导航
  const url = `https://uri.amap.com/navigation?to=${station.longitude},${station.latitude}&toName=${encodeURIComponent(station.name || '充电站')}&mode=car&policy=1&src=mychargingapp`
  window.open(url, '_blank')
}

// 处理预约
const handleReserve = (station) => {
  if (!userStore.token) {
    ElMessage.info('请先登录后再预约充电')
    router.push('/login')
    return
  }
  quickReserveStation.value = station
  quickReserveGun.value = '1'
  quickReserveDuration.value = 60
}

const closeQuickReserve = () => {
  quickReserveStation.value = null
}

const confirmQuickReserve = () => {
  if (!quickReserveStation.value) return
  router.push({
    path: '/reservation',
    query: {
      stationId: quickReserveStation.value.id,
      duration: quickReserveDuration.value,
      gunNo: quickReserveGun.value
    }
  })
}

// 处理收藏切换
const handleToggleFavorite = async (station) => {
  if (!userStore.token) {
    ElMessage.info('请先登录')
    router.push('/login')
    return
  }
  
  if (!station || !station.id) {
    ElMessage.error('充电站信息不完整')
    return
  }
  
  const isFavorite = favoriteMap.value[station.id]
  
  try {
    if (isFavorite) {
      const res = await removeFavorite(station.id)
      if (res.code === 200) {
        favoriteMap.value[station.id] = false
        stats.value.favoriteCount = Math.max(0, stats.value.favoriteCount - 1)
        ElMessage.success('已取消收藏')
      } else {
        ElMessage.error(res.message || '取消收藏失败')
      }
    } else {
      const res = await addFavorite(station.id)
      if (res.code === 200) {
        favoriteMap.value[station.id] = true
        stats.value.favoriteCount++
        ElMessage.success('已添加收藏')
      } else {
        ElMessage.error(res.message || '收藏失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 初始化
let isFirstLoad = true
onMounted(() => {
  // 恢复保存的状态
  restoreState()
  if (isFirstLoad) {
    loadRecommendedStations()
    loadFavorites()
    isFirstLoad = false
  }
})

// 组件激活时（从其他页面返回）
onActivated(() => {
  // 恢复保存的状态
  restoreState()
  // 如果数据为空，才重新加载
  if (recommendedStations.value.length === 0) {
    loadRecommendedStations()
  }
  // 总是刷新收藏状态（因为可能在其他页面修改了收藏）
  loadFavorites()
})

// 保存状态到sessionStorage
const saveState = () => {
  try {
    const state = {
      searchKeyword: currentFilters.value.keyword || '',
      chargeType: currentFilters.value.chargeType || null,
      status: currentFilters.value.status || null,
      priceRange: currentFilters.value.priceRange || [0, 2],
      distanceRange: currentFilters.value.distanceRange || [0, 10],
      mapCenter: mapCenter.value,
      recommendedStations: recommendedStations.value,
      mapStations: mapStations.value
    }
    sessionStorage.setItem('homeState', JSON.stringify(state))
  } catch (error) {
    console.error('保存状态失败:', error)
  }
}

// 恢复状态
const restoreState = () => {
  try {
    const savedState = sessionStorage.getItem('homeState')
    if (savedState) {
      const state = JSON.parse(savedState)
      if (state.searchKeyword) {
        currentFilters.value.keyword = state.searchKeyword
      }
      if (state.chargeType !== null) {
        currentFilters.value.chargeType = state.chargeType
      }
      if (state.status !== null) {
        currentFilters.value.status = state.status
      }
      if (state.priceRange) {
        currentFilters.value.priceRange = state.priceRange
      }
      if (state.distanceRange) {
        currentFilters.value.distanceRange = state.distanceRange
      }
      if (state.mapCenter) {
        mapCenter.value = state.mapCenter
      }
      // 只有在数据为空时才恢复数据，避免覆盖新数据
      if (recommendedStations.value.length === 0 && state.recommendedStations) {
        recommendedStations.value = state.recommendedStations
        mapStations.value = state.mapStations || state.recommendedStations
        updateStats()
      }
    }
  } catch (error) {
    console.error('恢复状态失败:', error)
  }
}

// 监听状态变化，自动保存
watch([recommendedStations, mapStations, mapCenter, currentFilters], () => {
  saveState()
}, { deep: true })

// 跳转到收藏页面
const goToFavorites = () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/favorites')
}

// 监听用户登录状态
watch(() => userStore.token, (newToken) => {
  if (newToken) {
    loadFavorites()
  } else {
    favoriteMap.value = {}
    stats.value.favoriteCount = 0
  }
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
}

.main-layout {
  display: flex;
  height: calc(100vh - 72px);
  min-height: 600px;
}

.quick-reserve-panel {
  position: fixed;
  top: 100px;
  right: 40px;
  z-index: 1100;
}

.quick-reserve-card {
  width: 320px;
}

.quick-reserve-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.quick-reserve-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #212121;
}

.quick-reserve-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #757575;
}

.quick-reserve-body {
  margin-top: 4px;
}

.fee-text {
  font-size: 14px;
  font-weight: 600;
  color: #1a6dff;
}

.quick-reserve-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(20px);
  opacity: 0;
}

/* 快捷数据卡片区（顶部） */
.stats-section {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 24px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.stats-container {
  max-width: 1920px;
  margin: 0 auto;
  padding: 0 32px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.stat-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.favorite-card {
  cursor: pointer;
}

.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.2);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon.available {
  background: linear-gradient(135deg, #4caf50, #66bb6a);
}

.stat-icon.fast {
  background: linear-gradient(135deg, #ff9800, #ffb74d);
}

.stat-icon.distance {
  background: linear-gradient(135deg, #2196f3, #64b5f6);
}

.stat-icon.favorite {
  background: linear-gradient(135deg, #e91e63, #f06292);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #212121;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #757575;
}

/* 推荐充电站列表 */
.recommended-list-section {
  background: #f5f6fa;
  padding: 60px 0;
}

.section-container {
  max-width: 1920px;
  margin: 0 auto;
  padding: 0 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #212121;
  margin: 0;
}

.loading-wrapper {
  padding: 20px 0;
}

.station-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
}

.station-card-wrapper {
  transition: transform 0.3s;
}

.station-card-wrapper:hover {
  transform: translateY(-4px);
}

@media (max-width: 1400px) {
  .station-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}

@media (max-width: 1200px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .main-layout {
    flex-direction: column;
    height: auto;
  }
}

@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: 1fr;
    padding: 0 16px;
  }
  
  .section-container {
    padding: 0 16px;
  }
  
  .station-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .section-header h2 {
    font-size: 24px;
  }
  
  .stat-value {
    font-size: 28px;
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
    font-size: 28px;
  }
}
</style>
