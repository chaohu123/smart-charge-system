import request from './request'

export const getPointsBalance = () => {
  return request({
    url: '/points/balance',
    method: 'get'
  })
}

export const getPointsRecords = (params) => {
  return request({
    url: '/points/records',
    method: 'get',
    params
  })
}

