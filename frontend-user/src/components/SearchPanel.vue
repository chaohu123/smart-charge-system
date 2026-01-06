<template>
  <div class="search-panel">
    <div class="panel-header">
      <h3>搜索充电站</h3>
    </div>
    
    <!-- 搜索输入框 -->
    <div class="search-input-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索站点名称或地址"
        size="large"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleClear"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <!-- 快速筛选标签 -->
    <div class="filter-section">
      <div class="filter-title">充电类型</div>
      <div class="filter-tags">
        <el-tag
          v-for="tag in chargeTypeTags"
          :key="tag.value"
          :type="filters.chargeType === tag.value ? 'primary' : 'info'"
          effect="plain"
          class="filter-tag"
          @click="handleChargeTypeFilter(tag.value)"
        >
          {{ tag.label }}
        </el-tag>
      </div>
    </div>
    
    <div class="filter-section">
      <div class="filter-title">状态筛选</div>
      <div class="filter-tags">
        <el-tag
          v-for="tag in statusTags"
          :key="tag.value"
          :type="filters.status === tag.value ? 'primary' : 'info'"
          effect="plain"
          class="filter-tag"
          @click="handleStatusFilter(tag.value)"
        >
          {{ tag.label }}
        </el-tag>
      </div>
    </div>
    
    <!-- 价格和距离筛选 -->
    <div class="filter-section">
      <div class="filter-title">价格范围</div>
      <el-slider
        v-model="priceRange"
        :min="0"
        :max="2"
        :step="0.1"
        :format-tooltip="formatPrice"
        @change="handlePriceChange"
      />
      <div class="range-display">
        <span>{{ formatPrice(priceRange[0]) }}</span>
        <span>{{ formatPrice(priceRange[1]) }}</span>
      </div>
    </div>
    
    <div class="filter-section">
      <div class="filter-title">距离范围</div>
      <el-slider
        v-model="distanceRange"
        :min="0"
        :max="10"
        :step="0.5"
        :format-tooltip="formatDistance"
        @change="handleDistanceChange"
      />
      <div class="range-display">
        <span>{{ formatDistance(distanceRange[0]) }}</span>
        <span>{{ formatDistance(distanceRange[1]) }}</span>
      </div>
    </div>
    
    <!-- 智能推荐充电站列表 -->
    <div class="recommended-section">
      <div class="section-title">
        <h4>智能推荐</h4>
        <el-button text size="small" @click="refreshRecommendations">刷新</el-button>
      </div>
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else-if="recommendedStations.length === 0" class="empty-state">
        <el-icon><Location /></el-icon>
        <p>暂无推荐充电站</p>
      </div>
      <div v-else class="station-list">
        <StationCard
          v-for="station in recommendedStations"
          :key="station.id"
          :station="station"
          @click="handleStationClick(station)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Search, Location, Loading } from '@element-plus/icons-vue'
import StationCard from './StationCard.vue'

const props = defineProps({
  recommendedStations: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['search', 'filter-change', 'station-click', 'refresh'])

const searchKeyword = ref(props.initialFilters?.keyword || '')
const filters = ref({
  chargeType: props.initialFilters?.chargeType || '', // 'fast', 'slow', ''
  status: props.initialFilters?.status || '' // 'available', 'all'
})
const priceRange = ref(props.initialFilters?.priceRange || [0, 2])
const distanceRange = ref(props.initialFilters?.distanceRange || [0, 10])

// 监听props变化，同步状态
watch(() => props.initialFilters, (newFilters) => {
  if (newFilters) {
    if (newFilters.keyword !== undefined) {
      searchKeyword.value = newFilters.keyword
    }
    if (newFilters.chargeType !== undefined) {
      filters.value.chargeType = newFilters.chargeType
    }
    if (newFilters.status !== undefined) {
      filters.value.status = newFilters.status
    }
    if (newFilters.priceRange) {
      priceRange.value = newFilters.priceRange
    }
    if (newFilters.distanceRange) {
      distanceRange.value = newFilters.distanceRange
    }
  }
}, { deep: true })

const chargeTypeTags = [
  { label: '全部', value: '' },
  { label: '快充', value: 'fast' },
  { label: '慢充', value: 'slow' }
]

const statusTags = [
  { label: '全部', value: '' },
  { label: '空闲', value: 'available' }
]

const handleSearch = () => {
  emit('search', searchKeyword.value)
}

const handleClear = () => {
  emit('search', '')
}

const handleChargeTypeFilter = (value) => {
  filters.value.chargeType = filters.value.chargeType === value ? '' : value
  emit('filter-change', { ...filters.value, priceRange: priceRange.value, distanceRange: distanceRange.value })
}

const handleStatusFilter = (value) => {
  filters.value.status = filters.value.status === value ? '' : value
  emit('filter-change', { ...filters.value, priceRange: priceRange.value, distanceRange: distanceRange.value })
}

const handlePriceChange = () => {
  emit('filter-change', { ...filters.value, priceRange: priceRange.value, distanceRange: distanceRange.value })
}

const handleDistanceChange = () => {
  emit('filter-change', { ...filters.value, priceRange: priceRange.value, distanceRange: distanceRange.value })
}

const formatPrice = (value) => {
  // el-slider 在 tooltip 回调里会直接传入 number；
  // 渲染区间文案时我们可能传入 undefined 或数组项，这里做保护。
  if (Array.isArray(value)) {
    value = value[0]
  }
  const num = Number(value)
  if (Number.isNaN(num)) {
    return '¥0.0/kWh'
  }
  return `¥${num.toFixed(1)}/kWh`
}

const formatDistance = (value) => {
  if (value === 0) return '0km'
  if (value < 1) return `${(value * 1000).toFixed(0)}m`
  return `${value.toFixed(1)}km`
}

const handleStationClick = (station) => {
  emit('station-click', station)
}

const refreshRecommendations = () => {
  emit('refresh')
}

watch(() => props.recommendedStations, () => {
  // 当推荐列表更新时，可以做一些处理
}, { deep: true })
</script>

<style scoped>
.search-panel {
  width: 380px;
  height: 100%;
  background: #fff;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.panel-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.search-input-section {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.filter-section {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.filter-title {
  font-size: 14px;
  font-weight: 500;
  color: #212121;
  margin-bottom: 12px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  cursor: pointer;
  transition: all 0.3s;
  padding: 6px 16px;
}

.filter-tag:hover {
  transform: translateY(-2px);
}

.range-display {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #757575;
}

.recommended-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #212121;
}

.loading-state,
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  color: #757575;
}

.loading-state .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #e0e0e0;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

.station-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

@media (max-width: 1200px) {
  .search-panel {
    width: 320px;
  }
}

@media (max-width: 768px) {
  .search-panel {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }
}
</style>

