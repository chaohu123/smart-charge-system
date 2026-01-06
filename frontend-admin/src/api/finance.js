import request from './request'

export const getFinanceOverview = (params) => {
  return request({
    url: '/admin/finance/overview',
    method: 'get',
    params
  })
}

export const getRevenueList = (params) => {
  return request({
    url: '/admin/finance/revenue/list',
    method: 'get',
    params
  })
}

export const getFinanceStatistics = (params) => {
  return request({
    url: '/admin/finance/statistics',
    method: 'get',
    params
  })
}

