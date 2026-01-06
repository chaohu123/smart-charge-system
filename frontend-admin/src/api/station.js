import request from './request'

export const getStationList = (params) => {
  return request({
    url: '/admin/station/list',
    method: 'get',
    params
  })
}

export const getStationDetail = (id) => {
  return request({
    url: `/admin/station/${id}`,
    method: 'get'
  })
}

export const addStation = (data) => {
  return request({
    url: '/admin/station/add',
    method: 'post',
    data
  })
}

export const updateStation = (id, data) => {
  return request({
    url: `/admin/station/${id}`,
    method: 'put',
    data
  })
}

export const deleteStation = (id) => {
  return request({
    url: `/admin/station/${id}`,
    method: 'delete'
  })
}

