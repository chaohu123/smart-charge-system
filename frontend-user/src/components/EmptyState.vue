<template>
  <div class="empty-state">
    <el-empty :description="description">
      <template #image>
        <el-icon :size="80" :color="iconColor" class="empty-state__icon">
          <component :is="iconComponent" />
        </el-icon>
      </template>
      <template #description>
        <div class="empty-state__content">
          <p class="empty-state__title">{{ description }}</p>
          <p v-if="subDescription" class="empty-state__subtitle">{{ subDescription }}</p>
        </div>
      </template>
      <el-button v-if="showButton" type="primary" @click="$emit('action')" class="empty-state__button">
        {{ buttonText }}
      </el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { 
  Document, 
  Box, 
  ShoppingCart, 
  Wallet, 
  Refresh 
} from '@element-plus/icons-vue'

const props = defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  subDescription: {
    type: String,
    default: ''
  },
  icon: {
    type: [String, Object],
    default: 'Box'
  },
  iconColor: {
    type: String,
    default: '#C0C4CC'
  },
  showButton: {
    type: Boolean,
    default: false
  },
  buttonText: {
    type: String,
    default: '去添加'
  }
})

defineEmits(['action'])

// 图标映射
const iconMap = {
  Document,
  Box,
  ShoppingCart,
  Wallet,
  Refresh
}

// 计算图标组件
const iconComponent = computed(() => {
  if (typeof props.icon === 'object') {
    return props.icon
  }
  return iconMap[props.icon] || Box
})
</script>

<style scoped>
.empty-state {
  padding: 80px 24px;
  text-align: center;
}

.empty-state__content {
  margin-top: 16px;
}

.empty-state__title {
  font-size: 16px;
  font-weight: 500;
  color: #757575;
  margin: 0 0 8px 0;
}

.empty-state__subtitle {
  font-size: 14px;
  color: #9E9E9E;
  margin: 0;
}

.empty-state__icon {
  display: block;
  margin: 0 auto;
}

.empty-state__button {
  margin-top: 24px;
}
</style>


