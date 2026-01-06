<template>
  <div class="monitor">
    <!-- 实时状态大屏（仅系统管理员可见） -->
    <div v-if="adminStore.adminInfo && (adminStore.adminInfo.role === 0 || adminStore.adminInfo.role === '0')" class="realtime-dashboard">
      <el-card class="dashboard-card">
        <template #header>
          <div class="card-header">
            <span>实时状态大屏{{ selectedStationId ? ` - ${selectedStation?.name}` : '' }}</span>
            <el-button v-if="selectedStationId" type="primary" @click="viewStationDetail(null)">查看所有站点</el-button>
            <el-button v-else type="primary" @click="loadDashboardStats()">刷新数据</el-button>
          </div>
        </template>
        <el-row :gutter="20" v-if="displayStats || dashboardStats !== null">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">总桩数</div>
              <div class="stat-value">{{ displayStats.totalPiles || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">在线率</div>
              <div class="stat-value">{{ displayStats.onlineRate || 0 }}%</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">使用率</div>
              <div class="stat-value">{{ displayStats.usageRate || 0 }}%</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">实时功率</div>
              <div class="stat-value">{{ displayStats.realtimePower || 0 }}kW</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;" v-if="displayStats || dashboardStats !== null">
          <el-col :span="12">
            <div class="stat-item">
              <div class="stat-label">当日充电量</div>
              <div class="stat-value large">{{ displayStats.todayPower || 0 }}kWh</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat-item">
              <div class="stat-label">可用桩数</div>
              <div class="stat-value large">{{ displayStats.availablePiles || 0 }} / {{ displayStats.totalPiles || 0 }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 站点列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>充电站列表</span>
          <el-button v-if="selectedStationId" @click="selectedStationId = null">返回列表</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div v-if="!selectedStationId" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchForm.stationName"
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
            <el-input
              v-model="searchForm.address"
              placeholder="请输入地址搜索"
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
      
      <!-- 设备搜索栏 -->
      <div v-if="selectedStationId" style="margin-bottom: 20px;">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="deviceSearchForm.pileNumber"
              placeholder="请输入桩号搜索"
              clearable
              @clear="handleDeviceSearch"
              @keyup.enter="handleDeviceSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="8">
            <el-select v-model="deviceSearchForm.status" placeholder="筛选状态" clearable style="width: 100%">
              <el-option label="全部" :value="null" />
              <el-option label="空闲" :value="0" />
              <el-option label="占用" :value="1" />
              <el-option label="故障" :value="2" />
              <el-option label="离线" :value="3" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-button type="primary" @click="handleDeviceSearch">搜索</el-button>
            <el-button @click="handleDeviceReset">重置</el-button>
          </el-col>
        </el-row>
      </div>
      
      <!-- 站点详情 -->
      <div v-if="selectedStationId" class="station-detail">
        <div class="detail-header">
          <h2>{{ selectedStation?.name }} - 设备列表</h2>
          <el-button type="primary" @click="loadStationStats(selectedStationId)">刷新数据</el-button>
        </div>
        
        <!-- 设备列表 -->
        <el-table :data="devices" style="width: 100%" v-loading="deviceLoading">
          <el-table-column prop="pileNumber" label="桩号" width="120" />
          <el-table-column prop="power" label="功率(kW)" width="120" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="scope">
              {{ scope.row.type === 0 ? '慢充' : '快充' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="电价(元/kWh)" width="120" />
          <el-table-column prop="lastHeartbeat" label="最后心跳" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button v-if="scope.row.status === 2" size="small" type="warning" @click="handleRestart(scope.row)">
                重启
              </el-button>
              <el-button size="small" @click="handleConfig(scope.row)">配置</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 设备列表分页 -->
        <el-pagination
          v-if="selectedStationId"
          v-model:current-page="deviceCurrentPage"
          v-model:page-size="devicePageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="deviceTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleDevicePageSizeChange"
          @current-change="handleDevicePageChange"
          style="margin-top: 20px; justify-content: flex-end;"
        />
      </div>
      
      <!-- 站点列表 -->
      <el-table v-else :data="stations" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewStationDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 站点列表分页 -->
      <el-pagination
        v-if="!selectedStationId"
        v-model:current-page="stationCurrentPage"
        v-model:page-size="stationPageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="stationTotal"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleStationPageSizeChange"
        @current-change="handleStationPageChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 配置充电桩对话框 -->
    <el-dialog v-model="showConfigDialog" title="配置充电桩" width="500px">
      <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="100px">
        <el-form-item label="价格(元/kWh)" prop="price">
          <el-input-number 
            v-model="configForm.price" 
            :min="0" 
            :precision="2" 
            :step="0.1"
            style="width: 100%"
            placeholder="请输入电价"
          />
        </el-form-item>
        <el-form-item label="功率(kW)" prop="power">
          <el-input-number 
            v-model="configForm.power" 
            :min="0" 
            :precision="2"
            style="width: 100%"
            placeholder="请输入功率"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="configForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="慢充" :value="0" />
            <el-option label="快充" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="configForm.status" placeholder="请选择状态" style="width: 100%" @change="handleStatusChange">
            <el-option label="空闲" :value="0" />
            <el-option label="占用" :value="1" />
            <el-option label="故障" :value="2" />
            <el-option label="离线" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item 
          v-if="configForm.status === 2" 
          label="故障原因" 
          prop="faultReason"
          :rules="[{ required: true, message: '请填写故障原因', trigger: 'blur' }]"
        >
          <el-input 
            v-model="configForm.faultReason" 
            type="textarea" 
            :rows="3"
            placeholder="请详细描述故障原因，运维人员将收到此信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmConfig">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { getStationList, getStationStats, getPileStatusList, restartPile, configPile } from '@/api/monitor'
import { useAdminStore } from '@/stores/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const adminStore = useAdminStore()
const stations = ref([])
const devices = ref([])
const selectedStationId = ref(null)
const selectedStation = ref(null)
const stationStats = ref(null)
const dashboardStats = ref(null)
const loading = ref(false)
const deviceLoading = ref(false)

// 站点列表分页
const stationCurrentPage = ref(1)
const stationPageSize = ref(10)
const stationTotal = ref(0)
const searchForm = ref({
  stationName: '',
  address: ''
})

// 设备搜索表单
const deviceSearchForm = ref({
  pileNumber: '',
  status: null
})

const showConfigDialog = ref(false)
const configFormRef = ref(null)
const currentDevice = ref(null)
const configForm = ref({
  price: null,
  power: null,
  type: null,
  status: null,
  faultReason: ''
})

// 设备列表分页
const deviceCurrentPage = ref(1)
const devicePageSize = ref(10)
const deviceTotal = ref(0)

const handleStatusChange = (value) => {
  // 当状态不是故障时，清空故障原因
  if (value !== 2) {
    configForm.value.faultReason = ''
  }
}
const configRules = {
  price: [
    { type: 'number', message: '请输入有效的价格', trigger: 'blur' }
  ],
  power: [
    { type: 'number', message: '请输入有效的功率', trigger: 'blur' }
  ]
}
let timer = null

// 计算显示的统计数据：如果选择了站点，显示站点数据；否则显示所有站点汇总数据
const displayStats = computed(() => {
  if (selectedStationId.value && stationStats.value) {
    return {
      totalPiles: stationStats.value.totalPiles || 0,
      onlinePiles: stationStats.value.onlinePiles || 0,
      onlineRate: stationStats.value.onlineRate || 0,
      availablePiles: stationStats.value.availablePiles || 0,
      usingPiles: stationStats.value.usingPiles || 0,
      usageRate: stationStats.value.usageRate || 0,
      realtimePower: stationStats.value.realtimePower || 0,
      todayPower: stationStats.value.todayPower || 0
    }
  }
  return dashboardStats.value
})

onMounted(() => {
  loadStations()
  // 仅系统管理员加载实时状态大屏数据
  if (adminStore.adminInfo && (adminStore.adminInfo.role === 0 || adminStore.adminInfo.role === '0')) {
    loadDashboardStats()
    timer = setInterval(() => {
      if (selectedStationId.value) {
        // 如果选择了站点，刷新站点数据
        loadStationStats(selectedStationId.value)
        loadDevices()
      } else {
        // 否则刷新所有站点汇总数据（仅系统管理员）
        if (adminStore.adminInfo && (adminStore.adminInfo.role === 0 || adminStore.adminInfo.role === '0')) {
          loadDashboardStats()
        }
      }
    }, 5000)
  } else {
    // 其他角色定时刷新设备列表
    if (selectedStationId.value) {
      timer = setInterval(loadDevices, 5000)
    }
  }
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const loadStations = async () => {
  loading.value = true
  try {
    const params = {
      current: stationCurrentPage.value,
      size: stationPageSize.value
    }
    if (searchForm.value.stationName) {
      params.name = searchForm.value.stationName
    }
    if (searchForm.value.address) {
      params.address = searchForm.value.address
    }
    const res = await getStationList(params)
    if (res.code === 200) {
      // 检查返回的是分页数据还是数组
      if (res.data && res.data.records) {
        // 分页数据
        stations.value = res.data.records || []
        stationTotal.value = Number(res.data.total) || 0
      } else if (Array.isArray(res.data)) {
        // 数组数据（兼容旧接口）
        stations.value = res.data
        stationTotal.value = res.data.length
      } else {
        stations.value = []
        stationTotal.value = 0
      }
    }
  } catch (error) {
    ElMessage.error('加载站点列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  stationCurrentPage.value = 1
  loadStations()
}

const handleReset = () => {
  searchForm.value = {
    stationName: '',
    address: ''
  }
  stationCurrentPage.value = 1
  loadStations()
}

const handleStationPageChange = (page) => {
  stationCurrentPage.value = page
  loadStations()
}

const handleStationPageSizeChange = (size) => {
  stationPageSize.value = size
  stationCurrentPage.value = 1
  loadStations()
}

const loadDashboardStats = async () => {
  try {
    // 汇总所有站点的统计数据（获取所有站点，不分页）
    // 传递一个很大的size值来获取所有站点
    const res = await getStationList({ current: 1, size: 10000 })
    if (res.code === 200 && res.data) {
      // 处理分页数据或数组数据
      let stationList = []
      if (res.data && res.data.records) {
        // 分页数据
        stationList = res.data.records || []
      } else if (Array.isArray(res.data)) {
        // 数组数据（兼容旧接口）
        stationList = res.data
      }
      let totalPiles = 0
      let totalOnlinePiles = 0
      let totalAvailablePiles = 0
      let totalUsingPiles = 0
      let totalRealtimePower = 0
      let totalTodayPower = 0
      
      for (const station of stationList) {
        // 首先使用站点列表中的总桩数和可用桩数（已经由后端动态计算）
        const stationTotalPiles = station.totalPiles || 0
        const stationAvailablePiles = station.availablePiles || 0
        
        // 然后获取详细的统计数据
        const statsRes = await getStationStats(station.id)
        if (statsRes.code === 200 && statsRes.data) {
          const stats = statsRes.data
          // 使用 getStationStats 返回的总桩数（更准确，因为它是实时统计的）
          totalPiles += stats.totalPiles || 0
          totalOnlinePiles += stats.onlinePiles || 0
          // 使用 getStationStats 返回的可用桩数（考虑了维护状态）
          totalAvailablePiles += stats.availablePiles || 0
          totalUsingPiles += stats.usingPiles || 0
          totalRealtimePower += stats.realtimePower || 0
          totalTodayPower += stats.todayPower || 0
        } else {
          // 如果获取详细统计失败，使用站点列表中的数据
          totalPiles += stationTotalPiles
          totalAvailablePiles += stationAvailablePiles
        }
      }
      
      dashboardStats.value = {
        totalPiles,
        onlinePiles: totalOnlinePiles,
        onlineRate: totalPiles > 0 ? Math.round((totalOnlinePiles / totalPiles) * 100 * 100) / 100 : 0,
        availablePiles: totalAvailablePiles,
        usingPiles: totalUsingPiles,
        usageRate: totalPiles > 0 ? Math.round((totalUsingPiles / totalPiles) * 100 * 100) / 100 : 0,
        realtimePower: Math.round(totalRealtimePower * 100) / 100,
        todayPower: Math.round(totalTodayPower * 100) / 100
      }
    } else {
      // 即使没有数据，也设置一个空对象，确保大屏显示
      dashboardStats.value = {
        totalPiles: 0,
        onlinePiles: 0,
        onlineRate: 0,
        availablePiles: 0,
        usingPiles: 0,
        usageRate: 0,
        realtimePower: 0,
        todayPower: 0
      }
    }
  } catch (error) {
    ElMessage.error('加载实时状态大屏数据失败')
    // 即使加载失败，也设置一个空对象，确保大屏显示
    dashboardStats.value = {
      totalPiles: 0,
      onlinePiles: 0,
      onlineRate: 0,
      availablePiles: 0,
      usingPiles: 0,
      usageRate: 0,
      realtimePower: 0,
      todayPower: 0
    }
  }
}

const loadStationStats = async (stationId) => {
  try {
    const res = await getStationStats(stationId)
    if (res.code === 200) {
      stationStats.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载站点统计数据失败')
  }
}

const viewStationDetail = async (station) => {
  if (station) {
    selectedStationId.value = station.id
    selectedStation.value = station
    // 重置分页
    deviceCurrentPage.value = 1
    devicePageSize.value = 10
    await Promise.all([
      loadStationStats(station.id),
      loadDevices()
    ])
    
    // 定时刷新已经在onMounted中设置，会自动根据selectedStationId.value判断刷新哪个数据
  } else {
    selectedStationId.value = null
    selectedStation.value = null
    stationStats.value = null
    devices.value = []
    deviceTotal.value = 0
    // 恢复显示所有站点汇总数据（仅系统管理员）
    if (adminStore.adminInfo && (adminStore.adminInfo.role === 0 || adminStore.adminInfo.role === '0')) {
      loadDashboardStats()
    }
  }
}

const loadDevices = async () => {
  if (!selectedStationId.value) {
    devices.value = []
    deviceTotal.value = 0
    return
  }
  
  deviceLoading.value = true
  try {
    const params = { 
      stationId: selectedStationId.value,
      current: deviceCurrentPage.value,
      size: devicePageSize.value
    }
    if (deviceSearchForm.value.pileNumber) {
      params.pileNumber = deviceSearchForm.value.pileNumber
    }
    if (deviceSearchForm.value.status !== null) {
      params.status = deviceSearchForm.value.status
    }
    const res = await getPileStatusList(params)
    if (res.code === 200) {
      // 检查返回的是分页数据还是数组
      if (res.data && res.data.records) {
        // 分页数据
        devices.value = res.data.records || []
        const total = Number(res.data.total) || 0
        deviceTotal.value = total
        
        // 如果当前页超出范围，重置到第一页
        if (total > 0 && deviceCurrentPage.value > 1) {
          const maxPage = Math.ceil(total / devicePageSize.value)
          if (deviceCurrentPage.value > maxPage) {
            deviceCurrentPage.value = 1
            // 重新加载第一页
            return loadDevices()
          }
        }
      } else if (Array.isArray(res.data)) {
        // 数组数据（兼容旧接口）
        devices.value = res.data
        deviceTotal.value = res.data.length
      } else {
        devices.value = []
        deviceTotal.value = 0
      }
    } else {
      devices.value = []
      deviceTotal.value = 0
      ElMessage.error(res.message || '加载设备列表失败')
    }
  } catch (error) {
    devices.value = []
    deviceTotal.value = 0
    ElMessage.error('加载设备列表失败')
  } finally {
    deviceLoading.value = false
  }
}

const handleDevicePageChange = (page) => {
  deviceCurrentPage.value = page
  loadDevices()
}

const handleDevicePageSizeChange = (size) => {
  devicePageSize.value = size
  deviceCurrentPage.value = 1
  loadDevices()
}

const handleDeviceSearch = () => {
  deviceCurrentPage.value = 1
  loadDevices()
}

const handleDeviceReset = () => {
  deviceSearchForm.value = {
    pileNumber: '',
    status: null
  }
  deviceCurrentPage.value = 1
  loadDevices()
}

const getStatusType = (status) => {
  const types = { 0: 'success', 1: 'primary', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '空闲', 1: '占用', 2: '故障', 3: '离线' }
  return texts[status] || '未知'
}

const handleRestart = async (device) => {
  try {
    await ElMessageBox.confirm('确定要重启这个充电桩吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await restartPile(device.id)
    if (res.code === 200) {
      ElMessage.success('重启成功')
      loadDevices()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重启失败')
    }
  }
}

const handleConfig = (device) => {
  currentDevice.value = device
  configForm.value = {
    price: device.price ? Number(device.price) : null,
    power: device.power ? Number(device.power) : null,
    type: device.type !== undefined ? device.type : null,
    status: device.status !== undefined ? device.status : null,
    faultReason: ''
  }
  showConfigDialog.value = true
}

const confirmConfig = async () => {
  if (!configFormRef.value) return
  
  await configFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const config = {}
        if (configForm.value.price !== null) {
          config.price = configForm.value.price
        }
        if (configForm.value.power !== null) {
          config.power = configForm.value.power
        }
        if (configForm.value.type !== null) {
          config.type = configForm.value.type
        }
        if (configForm.value.status !== null) {
          config.status = configForm.value.status
        }
        // 如果设置为故障，传递故障原因
        if (configForm.value.status === 2 && configForm.value.faultReason) {
          config.faultReason = configForm.value.faultReason
        }
        
        const res = await configPile(currentDevice.value.id, config)
        if (res.code === 200) {
          ElMessage.success('配置成功')
          showConfigDialog.value = false
          configForm.value.faultReason = ''
          loadDevices()
        }
      } catch (error) {
        ElMessage.error('配置失败')
      }
    }
  })
}
</script>

<style scoped>
.monitor {
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

.realtime-dashboard {
  margin-bottom: 20px;
}

.dashboard-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.dashboard-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.dashboard-card .card-header {
  color: white;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.stat-item .stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.stat-item .stat-value {
  font-size: 32px;
  font-weight: bold;
}

.stat-item .stat-value.large {
  font-size: 48px;
}

.station-detail {
  padding: 20px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h2 {
  margin: 0;
  color: var(--text-primary);
}

.stat-box {
  text-align: center;
  padding: 20px;
}

.stat-box .stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.stat-box .stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--admin-primary);
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
