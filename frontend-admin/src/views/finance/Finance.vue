<template>
  <div class="finance">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <p class="label">今日收入</p>
          <p class="value">¥{{ todayRevenue }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <p class="label">本月收入</p>
          <p class="value">¥{{ monthRevenue }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <p class="label">累计收入</p>
          <p class="value">¥{{ totalRevenue }}</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <p class="label">待提现金额</p>
          <p class="value">¥{{ pendingWithdraw }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>收入统计</span>
      </template>
      <div id="revenueChart" style="height: 400px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getFinanceOverview, getFinanceStatistics } from '@/api/finance'

const todayRevenue = ref(0)
const monthRevenue = ref(0)
const totalRevenue = ref(0)
const pendingWithdraw = ref(0)

onMounted(() => {
  loadFinanceData()
  initChart()
})

const loadFinanceData = async () => {
  try {
    const res = await getFinanceOverview({})
    if (res.code === 200) {
      todayRevenue.value = res.data.todayRevenue || 0
      monthRevenue.value = res.data.monthRevenue || 0
      totalRevenue.value = res.data.totalRevenue || 0
      pendingWithdraw.value = res.data.pendingWithdraw || 0
    }
  } catch (error) {
    console.error('加载财务数据失败:', error)
  }
}

const initChart = async () => {
  try {
    const res = await getFinanceStatistics({})
    if (res.code === 200 && res.data.dailyRevenue) {
      const dailyRevenue = res.data.dailyRevenue
      const dates = Object.keys(dailyRevenue).sort()
      const values = dates.map(date => dailyRevenue[date])
      
      const chart = echarts.init(document.getElementById('revenueChart'))
      chart.setOption({
        title: { text: '收入趋势' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: { type: 'value' },
        series: [{
          data: values,
          type: 'line',
          areaStyle: {}
        }]
      })
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}
</script>

<style scoped>
.finance {
  padding: var(--spacing-lg);
  background-color: var(--content-bg);
}

.label {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: var(--spacing-sm);
}

.value {
  font-size: 28px;
  font-weight: 600;
  color: var(--admin-primary);
}

:deep(.el-card) {
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  background-color: var(--card-bg);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-color);
  padding: var(--spacing-md) var(--spacing-lg);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

#revenueChart {
  height: 400px;
  width: 100%;
}
</style>

