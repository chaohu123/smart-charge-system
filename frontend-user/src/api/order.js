import request from './request'

export const startCharging = (pileId, qrCode) => {
  return request({
    url: '/order/start',
    method: 'post',
    params: { pileId, qrCode }
  })
}

export const stopCharging = (orderId) => {
  return request({
    url: `/order/${orderId}/stop`,
    method: 'post'
  })
}

export const getCurrentOrder = () => {
  return request({
    url: '/order/current',
    method: 'get'
  })
}

export const getOrderList = (params) => {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

export const getOrderDetail = (id) => {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

export const getOrderStatus = (id) => {
  return request({
    url: `/order/${id}/status`,
    method: 'get'
  })
}

export const cancelOrder = (orderId) => {
  return request({
    url: `/order/${orderId}/cancel`,
    method: 'post'
  })
}

export const refundOrder = (orderId, reason) => {
  return request({
    url: `/order/${orderId}/refund`,
    method: 'post',
    params: { reason }
  })
}

