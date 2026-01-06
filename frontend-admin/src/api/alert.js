import request from './request'

export const getAlertList = (params) => {
  return request({
    url: '/admin/alert/list',
    method: 'get',
    params
  })
}

export const getAlertDetail = (id) => {
  return request({
    url: `/admin/alert/${id}`,
    method: 'get'
  })
}

export const handleAlert = (id, handlerId, handleRemark) => {
  return request({
    url: `/admin/alert/${id}/handle`,
    method: 'put',
    params: { handlerId, handleRemark }
  })
}

export const ignoreAlert = (id) => {
  return request({
    url: `/admin/alert/${id}/ignore`,
    method: 'put'
  })
}

export const emergencyStop = (pileId) => {
  return request({
    url: `/admin/alert/emergency-stop/${pileId}`,
    method: 'post'
  })
}

