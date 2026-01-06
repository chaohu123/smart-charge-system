<template>
  <div
    class="pull-refresh"
    @touchstart="onTouchStart"
    @touchmove="onTouchMove"
    @touchend="onTouchEnd"
    :style="{ transform: `translateY(${translateY}px)` }"
  >
    <div class="pull-refresh-indicator" v-if="status !== 'normal'">
      <el-icon v-if="status === 'pulling'" class="is-loading"><Loading /></el-icon>
      <el-icon v-else-if="status === 'loading'"><Loading /></el-icon>
      <span>{{ statusText }}</span>
    </div>
    <slot></slot>
    <div class="load-more-indicator" v-if="hasMore && loadingMore">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载更多...</span>
    </div>
    <div class="load-more-indicator" v-if="!hasMore && list.length > 0">
      <span>没有更多了</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  onRefresh: {
    type: Function,
    required: true
  },
  onLoadMore: {
    type: Function,
    default: null
  },
  hasMore: {
    type: Boolean,
    default: true
  },
  list: {
    type: Array,
    default: () => []
  },
  threshold: {
    type: Number,
    default: 60
  }
})

const emit = defineEmits(['refresh', 'load-more'])

const status = ref('normal') // normal, pulling, loading
const translateY = ref(0)
const startY = ref(0)
const currentY = ref(0)
const loadingMore = ref(false)

const statusText = computed(() => {
  const texts = {
    normal: '',
    pulling: '下拉刷新',
    loading: '正在刷新...'
  }
  return texts[status.value]
})

const onTouchStart = (e) => {
  if (status.value === 'loading') return
  startY.value = e.touches[0].clientY
}

const onTouchMove = (e) => {
  if (status.value === 'loading') return
  
  currentY.value = e.touches[0].clientY
  const deltaY = currentY.value - startY.value
  
  // 只在顶部且向下拉时触发
  if (window.scrollY === 0 && deltaY > 0) {
    e.preventDefault()
    translateY.value = Math.min(deltaY, props.threshold * 1.5)
    
    if (translateY.value >= props.threshold) {
      status.value = 'pulling'
    } else {
      status.value = 'normal'
    }
  }
}

const onTouchEnd = () => {
  if (status.value === 'loading') return
  
  if (translateY.value >= props.threshold) {
    status.value = 'loading'
    translateY.value = props.threshold
    
    // 执行刷新
    Promise.resolve(props.onRefresh()).finally(() => {
      setTimeout(() => {
        status.value = 'normal'
        translateY.value = 0
      }, 300)
    })
  } else {
    status.value = 'normal'
    translateY.value = 0
  }
}

// 上拉加载更多
let scrollTimer = null
const handleScroll = () => {
  if (loadingMore.value || !props.hasMore || !props.onLoadMore) return
  
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 距离底部50px时触发
  if (scrollTop + windowHeight >= documentHeight - 50) {
    loadingMore.value = true
    Promise.resolve(props.onLoadMore()).finally(() => {
      loadingMore.value = false
    })
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.pull-refresh {
  transition: transform 0.3s ease;
  min-height: 100vh;
}

.pull-refresh-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  color: var(--text-color-secondary);
  font-size: 14px;
  gap: var(--spacing-small);
}

.load-more-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  color: var(--text-color-secondary);
  font-size: 14px;
  gap: var(--spacing-small);
  padding: var(--spacing-medium);
}
</style>

