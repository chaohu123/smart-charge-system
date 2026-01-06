<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card clickable" @click="handleCardClick('/station')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon><Location /></el-icon>
            </div>
            <div class="stat-info">
              <p class="label">充电站总数</p>
              <p class="value">{{ stats.totalStations }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card clickable" @click="handleCardClick('/pile')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="stat-info">
              <p class="label">充电桩总数</p>
              <p class="value">{{ stats.totalPiles }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card clickable" @click="handleCardClick('/order')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <p class="label">今日订单</p>
              <p class="value">{{ stats.todayOrders }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card clickable" @click="handleCardClick('/finance')">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C;">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <p class="label">今日收入</p>
              <p class="value">¥{{ stats.todayRevenue }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card-clickable" @click="handleCardClick('/order')">
          <template #header>
            <span>订单趋势</span>
          </template>
          <div id="orderChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card-clickable" @click="handleCardClick('/finance')">
          <template #header>
            <span>收入趋势</span>
          </template>
          <div id="revenueChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getDashboardData } from '@/api/monitor'
import { Location, Setting, Document, Money } from '@element-plus/icons-vue'

const router = useRouter()

const stats = ref({
  totalStations: 0,
  totalPiles: 0,
  todayOrders: 0,
  todayRevenue: 0
})

let orderChart = null
let revenueChart = null

onMounted(() => {
  loadDashboardData()
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initCharts()
  }, 100)
})

onUnmounted(() => {
  if (orderChart) {
    orderChart.dispose()
  }
  if (revenueChart) {
    revenueChart.dispose()
  }
})

const loadDashboardData = async () => {
  try {
    const res = await getDashboardData()
    if (res.code === 200) {
      stats.value = {
        totalStations: res.data.totalStations || 0,
        totalPiles: res.data.totalPiles || 0,
        todayOrders: res.data.todayOrders || 0,
        todayRevenue: res.data.todayRevenue ? res.data.todayRevenue.toFixed(2) : '0.00'
      }
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

// 处理卡片点击事件
const handleCardClick = (path) => {
  router.push(path)
}

const initCharts = () => {
  const orderChartEl = document.getElementById('orderChart')
  const revenueChartEl = document.getElementById('revenueChart')
  
  if (!orderChartEl || !revenueChartEl) {
    console.error('图表容器不存在')
    return
  }
  
  // 订单趋势图
  orderChart = echarts.init(orderChartEl)
  orderChart.setOption({
    color: ['#1976D2'],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '订单数',
      type: 'line',
      smooth: true,
      data: [120, 132, 101, 134, 90, 230, 210],
      areaStyle: {
        opacity: 0.3
      },
      lineStyle: {
        width: 3
      }
    }]
  })
  
  // 添加点击事件
  orderChart.on('click', () => {
    router.push('/order')
  })

  // 收入趋势图
  revenueChart = echarts.init(revenueChartEl)
  revenueChart.setOption({
    color: ['#4CAF50'],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '收入',
      type: 'bar',
      data: [8200, 9320, 9010, 9340, 12900, 13300, 13200],
      itemStyle: {
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
  
  // 添加点击事件
  revenueChart.on('click', () => {
    router.push('/finance')
  })

  // 响应式调整图表大小
  window.addEventListener('resize', () => {
    if (orderChart) orderChart.resize()
    if (revenueChart) revenueChart.resize()
  })
}
</script>

<style scoped>
.dashboard {
  padding: var(--spacing-lg);
  background-color: var(--content-bg);
}

.dashboard-header {
  margin-bottom: var(--spacing-lg);
}

.dashboard-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.dashboard-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.stat-card {
  background: var(--card-bg);
  border-radius: var(--border-radius);
  padding: var(--spacing-lg);
  box-shadow: var(--card-shadow);
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.stat-card.clickable {
  cursor: pointer;
}

.chart-card-clickable {
  cursor: pointer;
}

.chart-card-clickable:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.stat-title {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.primary {
  background: rgba(25, 118, 210, 0.1);
  color: var(--admin-primary);
}

.stat-icon.success {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.stat-icon.warning {
  background: rgba(255, 193, 7, 0.1);
  color: #FFC107;
}

.stat-icon.danger {
  background: rgba(244, 67, 54, 0.1);
  color: #F44336;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.stat-change {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-change.positive {
  color: #4CAF50;
}

.stat-change.negative {
  color: #F44336;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.chart-card {
  background: var(--card-bg);
  border-radius: var(--border-radius);
  padding: var(--spacing-lg);
  box-shadow: var(--card-shadow);
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.chart-container {
  height: 300px;
}

.dashboard-row {
  margin-top: var(--spacing-lg);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  margin-right: var(--spacing-md);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.stat-info .label {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: var(--spacing-xs);
}

.stat-info .value {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-container {
  height: 300px;
  width: 100%;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

:deep(.el-card) {
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
}

:deep(.el-card__header) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
  padding: var(--spacing-md) var(--spacing-lg);
}

:deep(.el-table) {
  border-radius: var(--border-radius);
  overflow: hidden;
}
</style>

