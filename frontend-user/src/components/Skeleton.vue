<template>
  <div class="skeleton-container">
    <div v-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-header">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-title"></div>
      </div>
      <div class="skeleton-content">
        <div class="skeleton-line" v-for="i in lines" :key="i" :style="{ width: getRandomWidth() }"></div>
      </div>
    </div>
    
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div class="skeleton-item" v-for="i in rows" :key="i">
        <div class="skeleton-avatar-small"></div>
        <div class="skeleton-item-content">
          <div class="skeleton-line"></div>
          <div class="skeleton-line" style="width: 60%"></div>
        </div>
      </div>
    </div>
    
    <div v-else-if="type === 'table'" class="skeleton-table">
      <div class="skeleton-table-row" v-for="i in rows" :key="i">
        <div class="skeleton-table-cell" v-for="j in cols" :key="j"></div>
      </div>
    </div>
    
    <div v-else class="skeleton-default">
      <div class="skeleton-line" v-for="i in lines" :key="i" :style="{ width: getRandomWidth() }"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'default', // default, card, list, table
    validator: (value) => ['default', 'card', 'list', 'table'].includes(value)
  },
  rows: {
    type: Number,
    default: 3
  },
  cols: {
    type: Number,
    default: 4
  },
  lines: {
    type: Number,
    default: 3
  }
})

const getRandomWidth = () => {
  const widths = ['100%', '80%', '60%', '90%', '70%']
  return widths[Math.floor(Math.random() * widths.length)]
}
</script>

<style scoped>
.skeleton-container {
  padding: var(--spacing-medium);
}

.skeleton-card {
  background: var(--background-color-card);
  border-radius: var(--border-radius-medium);
  padding: var(--spacing-large);
  box-shadow: var(--box-shadow-light);
}

.skeleton-header {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-medium);
}

.skeleton-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  margin-right: var(--spacing-medium);
}

.skeleton-avatar-small {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  margin-right: var(--spacing-medium);
  flex-shrink: 0;
}

.skeleton-title {
  height: 20px;
  width: 200px;
  border-radius: var(--border-radius-small);
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-content {
  margin-top: var(--spacing-medium);
}

.skeleton-line {
  height: 16px;
  border-radius: var(--border-radius-small);
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  margin-bottom: var(--spacing-small);
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-medium);
}

.skeleton-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-medium);
  background: var(--background-color-card);
  border-radius: var(--border-radius-medium);
  box-shadow: var(--box-shadow-light);
}

.skeleton-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-small);
}

.skeleton-table {
  width: 100%;
}

.skeleton-table-row {
  display: flex;
  gap: var(--spacing-small);
  margin-bottom: var(--spacing-small);
}

.skeleton-table-cell {
  flex: 1;
  height: 40px;
  border-radius: var(--border-radius-small);
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-default {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-small);
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>

