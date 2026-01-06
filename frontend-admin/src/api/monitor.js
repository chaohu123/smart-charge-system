import request from './request'

export const getDashboardData = () => {
  return request({
    url: '/admin/monitor/dashboard',
    method: 'get'
  })
}

export const getStationList = (params) => {
  return request({
    url: '/admin/monitor/stations',
    method: 'get',
    params
  })
}

export const getStationStats = (stationId) => {
  return request({
    url: `/admin/monitor/station/${stationId}/stats`,
    method: 'get'
  })
}

export const getPileStatusList = (params) => {
  return request({
    url: '/admin/monitor/piles',
    method: 'get',
    params
  })
}

export const getPileStatus = (id) => {
  return request({
    url: `/admin/monitor/pile/${id}`,
    method: 'get'
  })
}

export const restartPile = (id) => {
  return request({
    url: `/admin/monitor/pile/${id}/restart`,
    method: 'post'
  })
}

export const configPile = (id, data) => {
  return request({
    url: `/admin/monitor/pile/${id}/config`,
    method: 'post',
    data
  })
}

