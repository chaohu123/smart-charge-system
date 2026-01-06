import request from './request'

export const getPileList = (params) => {
  return request({
    url: '/admin/pile/list',
    method: 'get',
    params
  })
}

export const getPileDetail = (id) => {
  return request({
    url: `/admin/pile/${id}`,
    method: 'get'
  })
}

export const addPile = (data) => {
  return request({
    url: '/admin/pile/add',
    method: 'post',
    data
  })
}

export const updatePile = (id, data) => {
  return request({
    url: `/admin/pile/${id}`,
    method: 'put',
    data
  })
}

export const deletePile = (id) => {
  return request({
    url: `/admin/pile/${id}`,
    method: 'delete'
  })
}

export const restartPile = (id) => {
  return request({
    url: `/admin/pile/${id}/restart`,
    method: 'post'
  })
}
