import request from './request'

export const getAvailableCoupons = () => {
  return request({
    url: '/coupon/list',
    method: 'get'
  })
}

export const getMyCoupons = (status) => {
  return request({
    url: '/coupon/my',
    method: 'get',
    params: { status }
  })
}

export const receiveCoupon = (couponId) => {
  return request({
    url: `/coupon/${couponId}/receive`,
    method: 'post'
  })
}

