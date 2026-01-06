import request from './request'

export const getUserGrowthAnalysis = (params) => {
  return request({
    url: '/admin/analysis/user-growth',
    method: 'get',
    params
  })
}

export const getChargingFrequencyAnalysis = (params) => {
  return request({
    url: '/admin/analysis/charging-frequency',
    method: 'get',
    params
  })
}

export const getPeakHoursAnalysis = () => {
  return request({
    url: '/admin/analysis/peak-hours',
    method: 'get'
  })
}

export const getDeviceUtilizationAnalysis = () => {
  return request({
    url: '/admin/analysis/device-utilization',
    method: 'get'
  })
}

