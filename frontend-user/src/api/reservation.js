import request from './request'

export const createReservation = (data) => {
  return request({
    url: '/reservation/create',
    method: 'post',
    data: data
  })
}

export const getReservationList = (status) => {
  return request({
    url: '/reservation/list',
    method: 'get',
    params: { status }
  })
}

export const cancelReservation = (id) => {
  return request({
    url: `/reservation/${id}/cancel`,
    method: 'post'
  })
}

export const getPileReservations = (pileId) => {
  return request({
    url: `/reservation/pile/${pileId}`,
    method: 'get'
  })
}

