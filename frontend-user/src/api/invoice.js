import request from './request'

export const applyInvoice = (orderId, type, title, taxNumber, content) => {
  return request({
    url: '/invoice/apply',
    method: 'post',
    params: { orderId, type, title, taxNumber, content }
  })
}

export const getInvoiceList = (params) => {
  return request({
    url: '/invoice/list',
    method: 'get',
    params
  })
}

export const getInvoiceDetail = (id) => {
  return request({
    url: `/invoice/${id}`,
    method: 'get'
  })
}

export const downloadInvoice = (id) => {
  return request({
    url: `/invoice/download/${id}`,
    method: 'get'
  })
}

