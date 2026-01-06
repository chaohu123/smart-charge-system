import request from './request'

export const getNotificationList = (params) => {
  return request({
    url: '/notification/list',
    method: 'get',
    params
  })
}

export const getUnreadCount = () => {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

export const markAsRead = (id) => {
  return request({
    url: `/notification/${id}/read`,
    method: 'put'
  })
}

export const markAllAsRead = () => {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}

// 删除单条通知
export const deleteNotification = (id) => {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}

