import request from './request'

export const addFavorite = (stationId) => {
  return request({
    url: '/favorite/add',
    method: 'post',
    params: { stationId }
  })
}

export const removeFavorite = (stationId) => {
  return request({
    url: '/favorite/remove',
    method: 'delete',
    params: { stationId }
  })
}

export const getFavoriteList = () => {
  return request({
    url: '/favorite/list',
    method: 'get'
  })
}

export const checkFavorite = (stationId) => {
  return request({
    url: '/favorite/check',
    method: 'get',
    params: { stationId }
  })
}

