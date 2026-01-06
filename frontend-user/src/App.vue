<template>
  <router-view v-slot="{ Component, route }">
    <keep-alive :include="['Home']">
      <component :is="Component" :key="route.fullPath" />
    </keep-alive>
  </router-view>
</template>

<script setup>
</script>

<style>
/* 全局样式已在 theme.css 中定义 */
</style>

