<template>
  <div class="coupons">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="可领取" name="available">
        <div class="coupon-list">
          <el-card
            v-for="coupon in availableCoupons"
            :key="coupon.id"
            class="coupon-card"
          >
            <div class="coupon-content">
              <div class="coupon-amount">
                <span class="symbol">¥</span>
                <span class="value">{{ coupon.amount }}</span>
              </div>
              <div class="coupon-info">
                <h3>{{ coupon.name }}</h3>
                <p>满{{ coupon.minAmount }}元可用</p>
                <p>有效期至：{{ coupon.endTime }}</p>
              </div>
              <el-button type="primary" @click="handleReceive(coupon.id)">领取</el-button>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的优惠券" name="my">
        <el-tabs v-model="myCouponStatus">
          <el-tab-pane label="未使用" name="0">
            <div class="coupon-list">
              <el-card
                v-for="userCoupon in myCoupons"
                :key="userCoupon.id"
                class="coupon-card"
              >
                <div class="coupon-content">
                  <div class="coupon-info">
                    <h3>优惠券</h3>
                    <p>状态：未使用</p>
                  </div>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
          <el-tab-pane label="已使用" name="1">
            <div class="coupon-list">
              <el-card
                v-for="userCoupon in usedCoupons"
                :key="userCoupon.id"
                class="coupon-card"
              >
                <div class="coupon-content">
                  <div class="coupon-info">
                    <h3>优惠券</h3>
                    <p>状态：已使用</p>
                  </div>
                </div>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAvailableCoupons, getMyCoupons, receiveCoupon } from '@/api/coupon'
import { ElMessage } from 'element-plus'

const activeTab = ref('available')
const myCouponStatus = ref('0')
const availableCoupons = ref([])
const myCoupons = ref([])
const usedCoupons = ref([])

onMounted(() => {
  loadAvailableCoupons()
  loadMyCoupons()
})

const loadAvailableCoupons = async () => {
  try {
    const res = await getAvailableCoupons()
    if (res.code === 200) {
      availableCoupons.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载优惠券失败')
  }
}

const loadMyCoupons = async () => {
  try {
    const res = await getMyCoupons(0)
    if (res.code === 200) {
      myCoupons.value = res.data || []
    }
    const usedRes = await getMyCoupons(1)
    if (usedRes.code === 200) {
      usedCoupons.value = usedRes.data || []
    }
  } catch (error) {
    ElMessage.error('加载我的优惠券失败')
  }
}

const handleReceive = async (couponId) => {
  try {
    const res = await receiveCoupon(couponId)
    if (res.code === 200) {
      ElMessage.success('领取成功')
      loadAvailableCoupons()
      loadMyCoupons()
    }
  } catch (error) {
    ElMessage.error('领取失败')
  }
}
</script>

<style scoped>
.coupons {
  padding: 20px;
}

.coupon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
  margin-top: 20px;
}

.coupon-card {
  cursor: pointer;
}

.coupon-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
  color: #F56C6C;
}

.coupon-amount .symbol {
  font-size: 20px;
}

.coupon-amount .value {
  font-size: 36px;
  font-weight: bold;
}

.coupon-info {
  flex: 1;
}

.coupon-info h3 {
  margin-bottom: 5px;
}

.coupon-info p {
  color: #666;
  font-size: 14px;
  margin: 3px 0;
}
</style>

