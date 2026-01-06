import request from './request'

// 活动管理
export const getActivityList = (params) =>
  request({
    url: '/admin/operation/activity/list',
    method: 'get',
    params
  })

export const addActivity = (data) =>
  request({
    url: '/admin/operation/activity',
    method: 'post',
    data
  })

export const updateActivity = (id, data) =>
  request({
    url: `/admin/operation/activity/${id}`,
    method: 'put',
    data
  })

export const deleteActivity = (id) =>
  request({
    url: `/admin/operation/activity/${id}`,
    method: 'delete'
  })

// 优惠券管理
export const getCouponList = (params) =>
  request({
    url: '/admin/operation/coupon/list',
    method: 'get',
    params
  })

export const addCoupon = (data) =>
  request({
    url: '/admin/operation/coupon',
    method: 'post',
    data
  })

export const updateCoupon = (id, data) =>
  request({
    url: `/admin/operation/coupon/${id}`,
    method: 'put',
    data
  })

export const updateCouponStatus = (id, status) =>
  request({
    url: `/admin/operation/coupon/${id}/status`,
    method: 'put',
    params: { status }
  })

export const deleteCouponAdmin = (id) =>
  request({
    url: `/admin/operation/coupon/${id}`,
    method: 'delete'
  })

