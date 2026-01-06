<template>
  <div v-if="loading" class="loading-overlay">
    <div class="loading-spinner">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>{{ text || '加载中...' }}</p>
    </div>
  </div>
</template>

<script setup>
import { Loading } from '@element-plus/icons-vue'

defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  text: {
    type: String,
    default: '加载中...'
  }
})
</script>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  background: var(--background-color-card);
  padding: var(--spacing-extra-large);
  border-radius: var(--border-radius-large);
  text-align: center;
  box-shadow: var(--box-shadow-dark);
}

.loading-spinner .el-icon {
  font-size: 32px;
  color: var(--primary-color);
  margin-bottom: var(--spacing-small);
}

.loading-spinner p {
  margin: 0;
  color: var(--text-color-secondary);
}
</style>


