<template>
  <div class="notifications">
    <el-card>
      <template #header>
        <div class="header-content">
          <div class="header-left">
            <span class="header-title">订单消息与系统消息</span>
            <span class="header-subtitle">查看订单相关提醒与系统通知，支持分类、分页和删除</span>
          </div>
          <div class="header-right">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="unread-badge">
              <el-button @click="handleMarkAllRead" type="primary" text>全部标记为已读</el-button>
            </el-badge>
          </div>
        </div>
      </template>
      
      <!-- 消息类型切换：订单消息 / 系统消息 / 全部 -->
      <el-tabs v-model="activeType" @tab-change="handleTypeChange">
        <el-tab-pane label="全部消息" name=""></el-tab-pane>
        <el-tab-pane label="订单消息" name="订单"></el-tab-pane>
        <el-tab-pane label="系统消息" name="系统"></el-tab-pane>
      </el-tabs>

      <!-- 阅读状态筛选 -->
      <div class="status-filter">
        <el-radio-group v-model="activeStatus" size="small" @change="handleStatusChange">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="0">未读</el-radio-button>
          <el-radio-button label="1">已读</el-radio-button>
        </el-radio-group>
      </div>
      
      <el-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />
      
      <div v-else class="notification-list">
        <el-card
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-card"
          :class="{ 'unread': notification.isRead === 0 }"
          @click="handleOpenDetail(notification)"
        >
          <div class="notification-content">
            <div class="notification-header">
              <h4>{{ notification.title }}</h4>
              <el-tag :type="getTypeColor(notification.type)" size="small">
                {{ notification.type }}
              </el-tag>
            </div>
            <p class="notification-text">{{ notification.content }}</p>
            <div class="notification-footer">
              <span class="notification-time">{{ notification.createTime }}</span>
              <div class="notification-actions" @click.stop>
                <el-button
                  v-if="notification.isRead === 0"
                  text
                  size="small"
                  @click="handleRead(notification)"
                >
                  标记已读
                </el-button>
                <el-button
                  text
                  size="small"
                  type="danger"
                  @click="handleDelete(notification)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination-wrapper">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :current-page="current"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>

      <!-- 消息详情弹窗 -->
      <el-dialog
        v-model="detailVisible"
        :title="selectedNotification?.title || '消息详情'"
        width="520px"
        destroy-on-close
      >
        <div v-if="selectedNotification" class="detail-body">
          <div class="detail-header">
            <el-tag :type="getTypeColor(selectedNotification.type)" size="small">
              {{ selectedNotification.type }}
            </el-tag>
            <span class="detail-time">{{ selectedNotification.createTime }}</span>
          </div>
          <div class="detail-content">
            <p>{{ selectedNotification.content }}</p>
          </div>
        </div>
        <template #footer>
          <div class="detail-footer">
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button
              type="primary"
              v-if="selectedNotification && selectedNotification.isRead === 0"
              @click="handleRead(selectedNotification)"
            >
              标记为已读
            </el-button>
            <el-button
              type="danger"
              v-if="selectedNotification"
              @click="handleDelete(selectedNotification)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getNotificationList,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification
} from '@/api/notification'
import { ElMessage } from 'element-plus'

// 分类：订单 / 系统 / 全部
const activeType = ref('')
// 状态：全部 / 未读 / 已读
const activeStatus = ref('')
const loading = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 详情弹窗
const detailVisible = ref(false)
const selectedNotification = ref(null)

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
})

const loadNotifications = async () => {
  loading.value = true
  try {
    const params = {
      current: current.value,
      size: pageSize.value
    }
    if (activeStatus.value !== '') {
      params.isRead = parseInt(activeStatus.value)
    }
    if (activeType.value) {
      params.type = activeType.value
    }
    const res = await getNotificationList(params)
    if (res.code === 200) {
      const data = res.data || {}
      notifications.value = data.records || data.rows || []
      total.value = data.total || data.count || notifications.value.length
    }
  } catch (error) {
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const handleTypeChange = () => {
  current.value = 1
  loadNotifications()
}

const handleStatusChange = () => {
  current.value = 1
  loadNotifications()
}

const handlePageChange = (page) => {
  current.value = page
  loadNotifications()
}

const handleRead = async (notification) => {
  if (notification.isRead === 0) {
    try {
      const res = await markAsRead(notification.id)
      if (res.code === 200) {
        notification.isRead = 1
        loadUnreadCount()
        // 刷新列表以更新未读高亮
        loadNotifications()
      }
    } catch (error) {
      ElMessage.error('标记失败')
    }
  }
}

const handleOpenDetail = (notification) => {
  selectedNotification.value = { ...notification }
  detailVisible.value = true
  // 打开详情时自动标记为已读
  handleRead(notification)
}

const handleMarkAllRead = async () => {
  try {
    const res = await markAllAsRead()
    if (res.code === 200) {
      ElMessage.success('全部标记为已读')
      loadNotifications()
      loadUnreadCount()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (notification) => {
  try {
    const res = await deleteNotification(notification.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 关闭详情弹窗
      if (selectedNotification.value && selectedNotification.value.id === notification.id) {
        detailVisible.value = false
      }
      // 从当前列表中移除
      notifications.value = notifications.value.filter(item => item.id !== notification.id)
      // 重新加载未读数量
      loadUnreadCount()
      // 如果当前页被删空且不是第一页，自动跳转到上一页
      if (notifications.value.length === 0 && current.value > 1) {
        current.value -= 1
        loadNotifications()
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const getTypeColor = (type) => {
  const colors = {
    '订单': 'primary',
    '系统': 'info',
    '活动': 'success',
    '优惠券': 'warning'
  }
  return colors[type] || 'info'
}
</script>

<style scoped>
.notifications {
  padding: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
}

.header-subtitle {
  font-size: 13px;
  color: #909399;
}

.header-right {
  display: flex;
  align-items: center;
}

.unread-badge :deep(.el-badge__content.is-fixed) {
  top: 8px;
}

.status-filter {
  margin: 12px 0;
  display: flex;
  justify-content: flex-end;
}

.notification-list {
  margin-top: 20px;
}

.notification-card {
  margin-bottom: 15px;
  cursor: pointer;
  transition: transform 0.2s;
}

.notification-card:hover {
  transform: translateY(-2px);
}

.notification-card.unread {
  border-left: 4px solid #409EFF;
  background-color: #f0f9ff;
}

.notification-content {
  padding: 10px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notification-header h4 {
  margin: 0;
}

.notification-text {
  color: #666;
  margin: 10px 0;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-actions {
  display: flex;
  gap: 8px;
}

.notification-time {
  color: #999;
  font-size: 12px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.detail-time {
  font-size: 12px;
}

.detail-content {
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

