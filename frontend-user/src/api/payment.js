import request from './request'

export const pay = (orderId, paymentMethod) => {
  return request({
    url: `/order/${orderId}/pay`,
    method: 'post',
    params: { paymentMethod }
  })
}

// 备用支付接口（通过PaymentController）
export const payByPayment = (orderId, paymentMethod) => {
  return request({
    url: '/payment/pay',
    method: 'post',
    params: { orderId, paymentMethod }
  })
}

export const recharge = (amount, paymentMethod) => {
  return request({
    url: '/payment/recharge',
    method: 'post',
    params: { amount, paymentMethod }
  })
}

export const getBalance = () => {
  return request({
    url: '/payment/balance',
    method: 'get'
  })
}

export const getPaymentRecords = (params) => {
  return request({
    url: '/payment/records',
    method: 'get',
    params
  })
}

