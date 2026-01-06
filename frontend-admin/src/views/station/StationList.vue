<template>
  <div class="station-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>充电站列表</span>
          <el-button type="primary" @click="handleAdd">新增充电站</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入充电站名称搜索"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="8">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table :data="stations" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="充电站名称" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="totalPiles" label="总桩数" width="100" />
        <el-table-column prop="availablePiles" label="可用桩数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'warning'">
              {{ scope.row.status === 0 ? '正常' : '维护中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadStations"
        @current-change="loadStations"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 编辑充电站对话框 -->
    <el-dialog v-model="showEditDialog" :title="currentStation ? '编辑充电站' : '新增充电站'" width="600px">
      <el-form :model="stationForm" :rules="stationRules" ref="stationFormRef" label-width="120px">
        <el-form-item label="充电站名称" prop="name">
          <el-input v-model="stationForm.name" placeholder="请输入充电站名称" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="stationForm.address" placeholder="请输入详细地址">
            <template #append>
              <el-button :icon="Location" @click="openMapDialog" title="在地图上选择位置" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number 
            v-model="stationForm.longitude" 
            :precision="6" 
            style="width: 100%"
            placeholder="请输入经度"
          />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number 
            v-model="stationForm.latitude" 
            :precision="6" 
            style="width: 100%"
            placeholder="请输入纬度"
          />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="stationForm.businessHours" placeholder="例：08:00-22:00" />
        </el-form-item>
        <el-form-item label="服务电话" prop="servicePhone">
          <el-input v-model="stationForm.servicePhone" placeholder="请输入服务电话" />
        </el-form-item>
        <el-form-item label="服务费(元/kWh)" prop="serviceFee">
          <el-input-number 
            v-model="stationForm.serviceFee" 
            :precision="2" 
            :min="0"
            style="width: 100%"
            placeholder="请输入服务费"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="stationForm.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">维护中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="免费停车">
          <el-switch v-model="stationForm.freeParking" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="带休息室">
          <el-switch v-model="stationForm.hasLounge" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="24小时营业">
          <el-switch v-model="stationForm.is24Hours" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="可预约">
          <el-switch v-model="stationForm.reservable" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 地图选择对话框 -->
    <el-dialog 
      v-model="showMapDialog" 
      title="选择位置" 
      width="800px" 
      @opened="initMap"
      @closed="destroyMap"
    >
      <div style="margin-bottom: 15px;">
        <el-input 
          v-model="mapSearchKeyword" 
          placeholder="搜索地点" 
          clearable
          @keyup.enter="searchLocation"
        >
          <template #append>
            <el-button :icon="Search" @click="searchLocation">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div id="map-container" style="width: 100%; height: 500px;"></div>
      <template #footer>
        <el-button @click="showMapDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmLocation" :disabled="!selectedLocation">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getStationList, addStation, updateStation, deleteStation, getStationDetail } from '@/api/station'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Search } from '@element-plus/icons-vue'
import axios from 'axios'

// 高德地图 Web 服务 REST API Key
const AMAP_KEY = 'f8d5316339c116559e1b92e643bb1aa5'

const stations = ref([])
const loading = ref(false)
const showEditDialog = ref(false)
const currentStation = ref(null)
const stationFormRef = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = ref({
  name: ''
})
const showMapDialog = ref(false)
const mapSearchKeyword = ref('')
const selectedLocation = ref(null)
let mapInstance = null
let marker = null
let geocoder = null
let placeSearch = null

const stationForm = ref({
  name: '',
  address: '',
  longitude: null,
  latitude: null,
  businessHours: '',
  servicePhone: '',
  serviceFee: null,
  status: 0,
  freeParking: 0,
  hasLounge: 0,
  is24Hours: 0,
  reservable: 1
})

const stationRules = {
  name: [
    { required: true, message: '请输入充电站名称', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' }
  ],
  longitude: [
    { required: true, type: 'number', message: '请输入经度', trigger: 'blur' }
  ],
  latitude: [
    { required: true, type: 'number', message: '请输入纬度', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadStations()
})

const loadStations = async () => {
  loading.value = true
  try {
    const params = { 
      current: currentPage.value, 
      size: pageSize.value 
    }
    
    // 添加搜索参数
    if (searchForm.value.name) {
      params.name = searchForm.value.name
    }
    
    const res = await getStationList(params)
    console.log('充电站列表完整响应:', JSON.stringify(res, null, 2))
    
    if (res.code === 200) {
      // res.data 就是 PageResult 对象
      const pageData = res.data
      
      console.log('分页数据:', pageData)
      console.log('total 值:', pageData?.total, '类型:', typeof pageData?.total)
      console.log('records 数量:', pageData?.records?.length)
      
      // 处理分页数据
      if (pageData && pageData.records) {
        const records = Array.isArray(pageData.records) ? pageData.records : []
        
        // 检查返回的记录数是否超过每页条数
        if (records.length > pageSize.value) {
          console.warn(`返回的记录数 ${records.length} 超过每页条数 ${pageSize.value}，进行截取`)
          // 截取到正确的数量
          stations.value = records.slice(0, pageSize.value)
        } else {
          stations.value = records
        }
        
        console.log('实际显示的记录数:', stations.value.length, '请求的每页条数:', pageSize.value)
        
        // 处理 total 值：确保是数字类型
        let totalValue = 0
        if (pageData.total !== null && pageData.total !== undefined) {
          // 转换为数字，处理字符串、数字等情况
          const numTotal = Number(pageData.total)
          if (!isNaN(numTotal) && numTotal >= 0) {
            totalValue = numTotal
          } else {
            console.warn('total 值无效:', pageData.total, '使用 records 长度')
            totalValue = pageData.records.length
          }
        } else {
          // 如果 total 为空但有数据，使用 records 的长度
          console.warn('后端未返回 total，使用 records 长度')
          totalValue = pageData.records.length
        }
        
        total.value = totalValue
      } else if (Array.isArray(pageData)) {
        // 如果返回的是数组，说明没有分页信息
        stations.value = pageData
        total.value = pageData.length
      } else {
        stations.value = []
        total.value = 0
      }
      
      console.log('最终设置的数据:', {
        stationsCount: stations.value.length,
        total: total.value,
        totalType: typeof total.value
      })
      
      // 调试：检查每个站点的总桩数和可用桩数
      if (stations.value.length > 0) {
        console.log('站点数据详情:')
        stations.value.forEach(station => {
          console.log(`站点[${station.name}] - 总桩数: ${station.totalPiles}, 可用桩数: ${station.availablePiles}, 状态: ${station.status === 1 ? '维护中' : '正常'}`)
        })
      }
    } else {
      console.error('响应码不是200:', res)
      stations.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载充电站列表失败:', error)
    ElMessage.error('加载充电站列表失败')
    stations.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadStations()
}

const handleReset = () => {
  searchForm.value = {
    name: ''
  }
  currentPage.value = 1
  loadStations()
}

const handleAdd = () => {
  currentStation.value = null
  resetForm()
  showEditDialog.value = true
}

const handleEdit = async (row) => {
  currentStation.value = row
  try {
    const res = await getStationDetail(row.id)
    if (res.code === 200) {
      const data = res.data
      stationForm.value = {
        name: data.name || '',
        address: data.address || '',
        longitude: data.longitude ? Number(data.longitude) : null,
        latitude: data.latitude ? Number(data.latitude) : null,
        businessHours: data.businessHours || '',
        servicePhone: data.servicePhone || '',
        serviceFee: data.serviceFee ? Number(data.serviceFee) : null,
        status: data.status !== undefined ? data.status : 0,
        freeParking: data.freeParking !== undefined ? data.freeParking : 0,
        hasLounge: data.hasLounge !== undefined ? data.hasLounge : 0,
        is24Hours: data.is24Hours !== undefined ? data.is24Hours : 0,
        reservable: data.reservable !== undefined ? data.reservable : 1
      }
      showEditDialog.value = true
    }
  } catch (error) {
    ElMessage.error('加载充电站信息失败')
  }
}

const confirmEdit = async () => {
  if (!stationFormRef.value) return
  
  await stationFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const data = {
          name: stationForm.value.name,
          address: stationForm.value.address,
          longitude: stationForm.value.longitude,
          latitude: stationForm.value.latitude,
          businessHours: stationForm.value.businessHours,
          servicePhone: stationForm.value.servicePhone,
          serviceFee: stationForm.value.serviceFee,
          status: stationForm.value.status,
          freeParking: stationForm.value.freeParking,
          hasLounge: stationForm.value.hasLounge,
          is24Hours: stationForm.value.is24Hours,
          reservable: stationForm.value.reservable
        }
        
        let res
        if (currentStation.value) {
          res = await updateStation(currentStation.value.id, data)
        } else {
          res = await addStation(data)
        }
        
        if (res.code === 200) {
          ElMessage.success(currentStation.value ? '编辑成功' : '新增成功')
          showEditDialog.value = false
          currentStation.value = null
          resetForm()
          loadStations()
        }
      } catch (error) {
        ElMessage.error(currentStation.value ? '编辑失败' : '新增失败')
      }
    }
  })
}

const resetForm = () => {
  stationForm.value = {
    name: '',
    address: '',
    longitude: null,
    latitude: null,
    businessHours: '',
    servicePhone: '',
    serviceFee: null,
    status: 0,
    freeParking: 0,
    hasLounge: 0,
    is24Hours: 0,
    reservable: 1
  }
  if (stationFormRef.value) {
    stationFormRef.value.resetFields()
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个充电站吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteStation(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadStations()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 打开地图选择对话框
const openMapDialog = () => {
  showMapDialog.value = true
  selectedLocation.value = null
  mapSearchKeyword.value = ''
  // 地图初始化将在对话框打开后自动触发（通过@opened事件）
}

// 初始化地图
const initMap = () => {
  if (typeof window.AMap === 'undefined') {
    ElMessage.warning('地图SDK未加载，请检查配置')
    return
  }
  
  try {
    // 如果有旧地图实例，先销毁
    if (mapInstance) {
      mapInstance.destroy()
      mapInstance = null
    }
    
    // 确定地图中心点
    let center = [116.397428, 39.90923] // 默认北京
    if (stationForm.value.longitude && stationForm.value.latitude) {
      center = [Number(stationForm.value.longitude), Number(stationForm.value.latitude)]
    }
    
    // 创建地图实例
    mapInstance = new window.AMap.Map('map-container', {
      zoom: 15,
      center: center,
      mapStyle: 'amap://styles/normal'
    })
    
    // 如果有已有位置，添加标记
    if (stationForm.value.longitude && stationForm.value.latitude) {
      addMarker(center)
      selectedLocation.value = {
        lng: stationForm.value.longitude,
        lat: stationForm.value.latitude,
        address: stationForm.value.address || ''
      }
    }
    
    // 异步加载地理编码插件
    window.AMap.plugin('AMap.Geocoder', () => {
      geocoder = new window.AMap.Geocoder({
        city: '全国'
      })
    })
    
    // 异步加载地点搜索插件
    window.AMap.plugin('AMap.PlaceSearch', () => {
      placeSearch = new window.AMap.PlaceSearch({
        city: '全国',
        citylimit: false,
        map: mapInstance,
        panel: false,
        pageSize: 10,
        pageIndex: 1
      })
    })
    
    // 监听地图点击事件
    mapInstance.on('click', (e) => {
      const { lng, lat } = e.lnglat
      addMarker([lng, lat])
      // 逆地理编码获取地址
      if (geocoder) {
        geocoder.getAddress([lng, lat], (status, result) => {
          if (status === 'complete' && result.info === 'OK') {
            const address = result.regeocode.formattedAddress
            selectedLocation.value = {
              lng: lng,
              lat: lat,
              address: address
            }
          } else {
            selectedLocation.value = {
              lng: lng,
              lat: lat,
              address: ''
            }
          }
        })
      } else {
        // 如果地理编码服务还未加载，先设置位置，稍后获取地址
        selectedLocation.value = {
          lng: lng,
          lat: lat,
          address: ''
        }
        // 等待地理编码服务加载完成后再获取地址
        const checkGeocoder = setInterval(() => {
          if (geocoder) {
            clearInterval(checkGeocoder)
            geocoder.getAddress([lng, lat], (status, result) => {
              if (status === 'complete' && result.info === 'OK') {
                selectedLocation.value.address = result.regeocode.formattedAddress
              }
            })
          }
        }, 100)
        setTimeout(() => clearInterval(checkGeocoder), 5000) // 5秒超时
      }
    })
    
    // 获取当前位置
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { longitude, latitude } = position.coords
          mapInstance.setCenter([longitude, latitude])
        },
        () => {
          // 获取位置失败，使用默认位置
        }
      )
    }
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败')
  }
}

// 添加标记
const addMarker = (position) => {
  if (marker) {
    mapInstance.remove(marker)
  }
  
  marker = new window.AMap.Marker({
    position: position,
    draggable: true,
    cursor: 'move'
  })
  
  mapInstance.add(marker)
  mapInstance.setCenter(position)
  
  // 监听标记拖拽事件
  marker.on('dragend', (e) => {
    const { lng, lat } = e.lnglat
    if (geocoder) {
      geocoder.getAddress([lng, lat], (status, result) => {
        if (status === 'complete' && result.info === 'OK') {
          const address = result.regeocode.formattedAddress
          selectedLocation.value = {
            lng: lng,
            lat: lat,
            address: address
          }
        } else {
          selectedLocation.value = {
            lng: lng,
            lat: lat,
            address: ''
          }
        }
      })
    } else {
      // 如果地理编码服务还未加载，先设置位置
      selectedLocation.value = {
        lng: lng,
        lat: lat,
        address: ''
      }
      // 等待地理编码服务加载完成后再获取地址
      const checkGeocoder = setInterval(() => {
        if (geocoder) {
          clearInterval(checkGeocoder)
          geocoder.getAddress([lng, lat], (status, result) => {
            if (status === 'complete' && result.info === 'OK') {
              selectedLocation.value.address = result.regeocode.formattedAddress
            }
          })
        }
      }, 100)
      setTimeout(() => clearInterval(checkGeocoder), 5000) // 5秒超时
    }
  })
}

// 搜索地点 - 使用高德地图 REST API
const searchLocation = async () => {
  if (!mapSearchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  if (!mapInstance) {
    ElMessage.warning('地图未初始化')
    return
  }
  
  const keyword = mapSearchKeyword.value.trim()
  console.log('开始搜索地点:', keyword)
  
  // 方案1: 使用 POI 搜索 API
  try {
    console.log('尝试 POI 搜索...')
    const poiResponse = await axios.get('https://restapi.amap.com/v3/place/text', {
      params: {
        key: AMAP_KEY,
        keywords: keyword,
        city: '', // 全国搜索
        extensions: 'all',
        offset: 20,
        page: 1,
        output: 'JSON'
      }
    })
    
    console.log('POI 搜索响应:', poiResponse.data)
    
    // 检查响应状态
    if (poiResponse.data.status === '1') {
      // 成功响应
      if (poiResponse.data.pois && poiResponse.data.pois.length > 0) {
        // 找到结果，使用第一个
        const poi = poiResponse.data.pois[0]
        console.log('找到 POI 结果:', poi)
        
        if (poi.location) {
          const location = poi.location.split(',')
          const lng = parseFloat(location[0])
          const lat = parseFloat(location[1])
          
          if (lng && lat && !isNaN(lng) && !isNaN(lat)) {
            handleSearchSuccess(lng, lat, poi.name, poi.address)
            return
          }
        }
      } else {
        console.log('POI 搜索无结果，尝试地理编码...')
      }
    } else {
      // API 返回错误
      console.error('POI 搜索 API 错误:', poiResponse.data.info || poiResponse.data)
      if (poiResponse.data.info) {
        console.error('错误信息:', poiResponse.data.info)
      }
    }
  } catch (error) {
    console.error('POI 搜索请求失败:', error)
    if (error.response) {
      console.error('响应数据:', error.response.data)
    }
  }
  
  // 方案2: 使用地理编码 API
  try {
    console.log('尝试地理编码搜索...')
    const geoResponse = await axios.get('https://restapi.amap.com/v3/geocode/geo', {
      params: {
        key: AMAP_KEY,
        address: keyword,
        output: 'JSON'
      }
    })
    
    console.log('地理编码响应:', geoResponse.data)
    
    if (geoResponse.data.status === '1') {
      if (geoResponse.data.geocodes && geoResponse.data.geocodes.length > 0) {
        const geocode = geoResponse.data.geocodes[0]
        console.log('找到地理编码结果:', geocode)
        
        if (geocode.location) {
          const location = geocode.location.split(',')
          const lng = parseFloat(location[0])
          const lat = parseFloat(location[1])
          
          if (lng && lat && !isNaN(lng) && !isNaN(lat)) {
            handleSearchSuccess(
              lng, 
              lat, 
              keyword, 
              geocode.formatted_address || geocode.address || geocode.province + geocode.city + geocode.district + geocode.street
            )
            return
          }
        }
      } else {
        console.log('地理编码无结果')
      }
    } else {
      console.error('地理编码 API 错误:', geoResponse.data.info || geoResponse.data)
    }
  } catch (error) {
    console.error('地理编码请求失败:', error)
    if (error.response) {
      console.error('响应数据:', error.response.data)
    }
  }
  
  // 所有方案都失败
  console.log('所有搜索方案都失败')
  ElMessage.warning('未找到相关地点，请尝试其他关键词或在地图上直接点击选择位置')
}

// 处理搜索成功的统一方法
const handleSearchSuccess = (lng, lat, name, address) => {
  const position = [lng, lat]
  addMarker(position)
  
  // 构建完整地址
  let fullAddress = address || ''
  if (name && name !== address) {
    fullAddress = fullAddress ? `${fullAddress} ${name}` : name
  }
  
  selectedLocation.value = {
    lng: lng,
    lat: lat,
    address: fullAddress || name || mapSearchKeyword.value
  }
  
  mapInstance.setCenter(position)
  mapInstance.setZoom(16)
  ElMessage.success(`找到地点：${name || mapSearchKeyword.value}`)
  console.log('搜索成功，位置:', { lng, lat, name, address: fullAddress })
}

// 确认选择位置
const confirmLocation = () => {
  if (!selectedLocation.value) {
    ElMessage.warning('请先选择位置')
    return
  }
  
  stationForm.value.longitude = Number(selectedLocation.value.lng.toFixed(6))
  stationForm.value.latitude = Number(selectedLocation.value.lat.toFixed(6))
  if (selectedLocation.value.address) {
    stationForm.value.address = selectedLocation.value.address
  }
  
  showMapDialog.value = false
  ElMessage.success('位置已选择')
}

// 销毁地图
const destroyMap = () => {
  if (marker && mapInstance) {
    mapInstance.remove(marker)
    marker = null
  }
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
  geocoder = null
  placeSearch = null
  selectedLocation.value = null
}
</script>

<style scoped>
.station-list {
  padding: var(--spacing-lg);
  background-color: var(--content-bg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.el-card) {
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  background-color: var(--card-bg);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-color);
  padding: var(--spacing-md) var(--spacing-lg);
}

:deep(.el-table) {
  border-radius: var(--border-radius);
  overflow: hidden;
}

:deep(.el-button) {
  border-radius: var(--border-radius);
}

:deep(.el-tag) {
  border-radius: var(--border-radius);
}
</style>
