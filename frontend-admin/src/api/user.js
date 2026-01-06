import request from './request'

export const getUserList = (params) => {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

export const getUserDetail = (id) => {
  return request({
    url: `/admin/user/${id}`,
    method: 'get'
  })
}

export const updateUser = (id, data) => {
  return request({
    url: `/admin/user/${id}`,
    method: 'put',
    data
  })
}

export const updateUserStatus = (id, status) => {
  return request({
    url: `/admin/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export const updateCreditScore = (id, creditScore, reason) => {
  return request({
    url: `/admin/user/${id}/credit`,
    method: 'put',
    params: { creditScore, reason }
  })
}

export const deleteUser = (id) => {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
}

