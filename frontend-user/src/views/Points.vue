<template>
  <div class="points">
    <el-card>
      <template #header>
        <div class="header-content">
          <span>我的积分</span>
          <span class="points-balance">当前积分：{{ pointsBalance }}</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="积分记录" name="records">
          <el-table :data="pointsRecords" style="width: 100%">
            <el-table-column prop="points" label="积分变化">
              <template #default="scope">
                <span :style="{ color: scope.row.points > 0 ? '#67C23A' : '#F56C6C' }">
                  {{ scope.row.points > 0 ? '+' : '' }}{{ scope.row.points }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="createTime" label="时间" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPointsBalance, getPointsRecords } from '@/api/points'
import { ElMessage } from 'element-plus'

const activeTab = ref('records')
const pointsBalance = ref(0)
const pointsRecords = ref([])

onMounted(() => {
  loadPointsData()
})

const loadPointsData = async () => {
  try {
    const balanceRes = await getPointsBalance()
    if (balanceRes.code === 200) {
      pointsBalance.value = balanceRes.data || 0
    }
    
    const recordsRes = await getPointsRecords({ current: 1, size: 20 })
    if (recordsRes.code === 200) {
      pointsRecords.value = recordsRes.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载积分数据失败')
  }
}
</script>

<style scoped>
.points {
  padding: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-balance {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}
</style>

