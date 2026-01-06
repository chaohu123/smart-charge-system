<template>
  <div class="station-card" @click="$emit('click', station)">
    <div class="card-cover">
      <img :src="station.imageUrl || defaultCover" alt="实景图" />
      <div class="cover-badge">
        <span>{{ distanceText }}</span>
        <span class="queue-text">{{ queueText }}</span>
      </div>
    </div>

    <div class="card-header">
      <h4 class="station-name">{{ station.name }}</h4>
      <el-tag
        :type="station.availablePiles > 0 ? 'success' : 'danger'"
        size="small"
        class="status-tag"
      >
        <span class="status-dot" :class="station.availablePiles > 0 ? 'available' : 'full'"></span>
        {{ station.availablePiles > 0 ? '可用' : '已满' }}
      </el-tag>
    </div>
    
    <div class="card-rating" v-if="typeof station.displayScore === 'number'">
      <el-rate
        v-model="station.displayScore"
        :max="5"
        disabled
        allow-half
        size="small"
        class="rating-stars"
      />
      <span class="rating-score">{{ station.displayScore.toFixed(1) }}</span>
      <span class="rating-count" v-if="station.evaluationCount">
        ({{ station.evaluationCount }}条评价)
      </span>
    </div>
    
    <p class="station-address">
      <el-icon><Location /></el-icon>
      {{ station.address }}
    </p>
    
    <div class="card-info">
      <div class="info-item">
        <el-icon><Lightning /></el-icon>
        <span>可用: {{ station.availablePiles }}/{{ station.totalPiles }}</span>
      </div>
      <div class="info-item">
        <el-icon><Location /></el-icon>
        <span>{{ distanceText }}</span>
      </div>
      <div class="info-item">
        <el-icon><Money /></el-icon>
        <span>¥{{ station.serviceFee != null ? station.serviceFee : '—' }}/kWh</span>
      </div>
    </div>
    
    <div class="card-actions">
      <el-button size="small" type="primary" plain @click.stop="$emit('navigate', station)">
        <el-icon><Guide /></el-icon>
        导航
      </el-button>
      <el-button size="small" type="primary" @click.stop="$emit('reserve', station)">
        <el-icon><Clock /></el-icon>
        预约
      </el-button>
      <el-button
        size="small"
        :type="isFavorite ? 'danger' : 'info'"
        text
        @click.stop="$emit('toggle-favorite', station)"
      >
        <el-icon><Star /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Location, Lightning, Money, Guide, Clock, Star } from '@element-plus/icons-vue'

const props = defineProps({
  station: {
    type: Object,
    required: true
  },
  isFavorite: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click', 'navigate', 'reserve', 'toggle-favorite'])

const defaultCover = 'https://via.placeholder.com/640x320?text=Charging+Station'

const distanceText = computed(() => {
  const distance = props.station.distance
  if (!distance) return '距离未知'
  if (distance < 1000) {
    return `${distance}m`
  }
  return `${(distance / 1000).toFixed(1)}km`
})

const queueText = computed(() => {
  // 若后端提供 waitingCount / queueStatus 可直接使用
  if (typeof props.station.waitingCount === 'number') {
    if (props.station.waitingCount === 0) return '无需排队'
    return `排队中：${props.station.waitingCount}人`
  }
  if (props.station.availablePiles > 0) {
    return '无需排队'
  }
  return '可能需要排队'
})
</script>

<style scoped>
.station-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.station-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
  border-color: #1a6dff;
}

.card-cover {
  position: relative;
  width: 100%;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cover-badge span {
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  backdrop-filter: blur(4px);
}

.queue-text {
  background: rgba(76, 175, 80, 0.9) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.station-name {
  flex: 1;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #212121;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  flex-shrink: 0;
  margin-left: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot.available {
  background: #4caf50;
}

.status-dot.full {
  background: #f44336;
}

.card-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.rating-stars {
  font-size: 14px;
}

.rating-score {
  font-size: 14px;
  font-weight: 600;
  color: #ff9800;
}

.rating-count {
  font-size: 12px;
  color: #757575;
}

.station-address {
  font-size: 13px;
  color: #757575;
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #757575;
}

.info-item .el-icon {
  font-size: 14px;
  color: #1a6dff;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-actions .el-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
</style>

