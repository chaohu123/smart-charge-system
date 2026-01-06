<template>
  <div class="analysis">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户增长分析</span>
          </template>
          <div id="userGrowthChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>充电频次分析</span>
          </template>
          <div id="frequencyChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>高峰时段分析</span>
          </template>
          <div id="peakHoursChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>设备利用率分析</span>
          </template>
          <div id="utilizationChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import * as echarts from 'echarts'
import { getUserGrowthAnalysis, getChargingFrequencyAnalysis, getPeakHoursAnalysis, getDeviceUtilizationAnalysis } from '@/api/analysis'

onMounted(() => {
  loadAnalysisData()
})

const loadAnalysisData = async () => {
  try {
    // 用户增长分析
    const userGrowthRes = await getUserGrowthAnalysis({})
    if (userGrowthRes.code === 200) {
      initUserGrowthChart(userGrowthRes.data)
    }

    // 充电频次分析
    const frequencyRes = await getChargingFrequencyAnalysis({})
    if (frequencyRes.code === 200) {
      initFrequencyChart(frequencyRes.data)
    }

    // 高峰时段分析
    const peakRes = await getPeakHoursAnalysis()
    if (peakRes.code === 200) {
      initPeakHoursChart(peakRes.data)
    }

    // 设备利用率分析
    const utilRes = await getDeviceUtilizationAnalysis()
    if (utilRes.code === 200) {
      initUtilizationChart(utilRes.data)
    }
  } catch (error) {
    console.error('加载分析数据失败:', error)
  }
}

const initUserGrowthChart = (data) => {
  const chart = echarts.init(document.getElementById('userGrowthChart'))
  chart.setOption({
    title: { text: '用户增长趋势' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: { type: 'value' },
    series: [{
      data: [120, 132, 101, 134, 90, 230],
      type: 'line',
      areaStyle: {}
    }]
  })
}

const initFrequencyChart = (data) => {
  const chart = echarts.init(document.getElementById('frequencyChart'))
  chart.setOption({
    title: { text: '充电频次分布' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      data: [
        { value: 35, name: '1-3次/月' },
        { value: 25, name: '4-6次/月' },
        { value: 20, name: '7-10次/月' },
        { value: 20, name: '10次以上/月' }
      ]
    }]
  })
}

const initPeakHoursChart = (data) => {
  const chart = echarts.init(document.getElementById('peakHoursChart'))
  chart.setOption({
    title: { text: '高峰时段分布' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.peakHours || [] },
    yAxis: { type: 'value' },
    series: [{
      data: data.peakCounts || [],
      type: 'bar'
    }]
  })
}

const initUtilizationChart = (data) => {
  const chart = echarts.init(document.getElementById('utilizationChart'))
  chart.setOption({
    title: { text: '设备利用率' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'gauge',
      data: [{ value: (data.avgUtilization || 0) * 100, name: '利用率' }],
      min: 0,
      max: 100
    }]
  })
}
</script>

<style scoped>
.analysis {
  padding: var(--spacing-lg);
  background-color: var(--content-bg);
}

:deep(.el-card) {
  border-radius: var(--border-radius);
  box-shadow: var(--card-shadow);
  background-color: var(--card-bg);
  margin-bottom: var(--spacing-lg);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-color);
  padding: var(--spacing-md) var(--spacing-lg);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

#userGrowthChart,
#frequencyChart,
#peakHoursChart,
#utilizationChart {
  height: 300px;
  width: 100%;
}
</style>

