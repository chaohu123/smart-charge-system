import request from './request'

export const getOrderList = (params) => {
  return request({
    url: '/admin/order/list',
    method: 'get',
    params
  })
}

export const getOrderDetail = (id) => {
  return request({
    url: `/admin/order/${id}`,
    method: 'get'
  })
}

export const refundOrder = (id, reason) => {
  return request({
    url: `/admin/order/${id}/refund`,
    method: 'post',
    params: { reason }
  })
}

export const getOrderStatistics = (params) => {
  return request({
    url: '/admin/order/statistics',
    method: 'get',
    params
  })
}

