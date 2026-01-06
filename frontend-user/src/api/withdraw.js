import request from './request'

export const applyWithdraw = (amount) => {
  return request({
    url: '/withdraw/apply',
    method: 'post',
    params: { amount }
  })
}

export const getWithdrawRecords = (params) => {
  return request({
    url: '/withdraw/records',
    method: 'get',
    params
  })
}

