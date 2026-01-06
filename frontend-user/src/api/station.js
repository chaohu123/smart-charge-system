import request from './request'

export const getNearbyStations = (params) => {
  return request({
    url: '/station/nearby',
    method: 'get',
    params
  })
}

export const getStationDetail = (id) => {
  return request({
    url: `/station/${id}`,
    method: 'get'
  })
}

export const getStationPiles = (id) => {
  return request({
    url: `/station/${id}/piles`,
    method: 'get'
  })
}

export const searchStations = (params) => {
  return request({
    url: '/station/search',
    method: 'get',
    params
  })
}

export const getStationEvaluations = (id, params) => {
  return request({
    url: `/station/${id}/evaluations`,
    method: 'get',
    params
  })
}

export const getRecommendedStations = (params) => {
  return request({
    url: '/station/recommend',
    method: 'get',
    params
  })
}

export const searchRouteStations = (params) => {
  return request({
    url: '/station/route-search',
    method: 'post',
    data: params
  })
}
