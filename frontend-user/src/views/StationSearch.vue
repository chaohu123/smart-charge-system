<template>
  <div class="station-search">
    <el-card>
      <template #header>
        <div class="search-header">
          <el-input
            v-model="keyword"
            placeholder="搜索充电站名称或地址"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </template>

      <div class="filters">
        <el-select v-model="filters.type" placeholder="充电类型" @change="handleSearch" clearable>
          <el-option label="全部" value="" />
          <el-option label="快充" :value="1" />
          <el-option label="慢充" :value="0" />
        </el-select>
        
        <el-select v-model="filters.sortBy" placeholder="排序方式" @change="handleSearch">
          <el-option label="距离最近" value="distance" />
          <el-option label="价格最低" value="price" />
          <el-option label="评分最高" value="rating" />
        </el-select>

        <el-input-number
          v-model="filters.maxPrice"
          :min="0"
          :max="10"
          :precision="2"
          placeholder="最高价格"
          @change="handleSearch"
          style="width: 150px;"
        />
      </div>

      <div class="station-list">
        <el-empty v-if="stations.length === 0 && !loading" description="暂无搜索结果" />
        
        <el-card
          v-for="station in stations"
          :key="station.id"
          class="station-card"
          @click="router.push(`/station/${station.id}`)"
        >
          <div class="station-info">
            <h3>{{ station.name }}</h3>
            <p class="address">{{ station.address }}</p>
            <div class="station-meta">
              <span>可用: {{ station.availablePiles }}/{{ station.totalPiles }}</span>
              <span v-if="station.distance" class="distance">距离: {{ station.distance }}m</span>
              <span class="price">服务费: ¥{{ station.serviceFee }}/kWh</span>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { searchStations, getNearbyStations } from '@/api/station'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const keyword = ref(route.query.keyword || '')
const loading = ref(false)
const stations = ref([])

const filters = reactive({
  type: '',
  sortBy: 'distance',
  maxPrice: null
})

onMounted(() => {
  handleSearch()
})

const handleSearch = async () => {
  loading.value = true
  try {
    let res
    if (keyword.value) {
      // 使用搜索接口
      res = await searchStations({
        keyword: keyword.value,
        type: filters.type || undefined,
        minPrice: 0,
        maxPrice: filters.maxPrice || undefined,
        current: 1,
        size: 20
      })
    } else {
      // 使用附近充电站接口
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(async (position) => {
          const { longitude, latitude } = position.coords
          res = await getNearbyStations({
            longitude,
            latitude,
            radius: 10000,
            type: filters.type || undefined,
            current: 1,
            size: 20
          })
          if (res.code === 200) {
            stations.value = res.data.records || []
            sortStations()
          }
        })
        return
      } else {
        res = await searchStations({
          type: filters.type || undefined,
          current: 1,
          size: 20
        })
      }
    }
    
    if (res.code === 200) {
      stations.value = res.data.records || []
      sortStations()
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const sortStations = () => {
  if (filters.sortBy === 'distance') {
    stations.value.sort((a, b) => (a.distance || 0) - (b.distance || 0))
  } else if (filters.sortBy === 'price') {
    stations.value.sort((a, b) => (a.serviceFee || 0) - (b.serviceFee || 0))
  } else if (filters.sortBy === 'rating') {
    // 这里需要根据评分排序，暂时按名称排序
    stations.value.sort((a, b) => a.name.localeCompare(b.name))
  }
}
</script>

<style scoped>
.station-search {
  padding: 20px;
}

.search-header {
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.station-list {
  margin-top: 20px;
}

.station-card {
  margin-bottom: 15px;
  cursor: pointer;
  transition: transform 0.2s;
}

.station-card:hover {
  transform: translateY(-2px);
}

.station-info h3 {
  margin-bottom: 10px;
}

.address {
  color: #666;
  margin-bottom: 10px;
}

.station-meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 14px;
}

.distance {
  color: var(--primary-color);
}

.price {
  color: #F56C6C;
}
</style>

