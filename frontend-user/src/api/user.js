import request from './request'

export const register = (data) => {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export const login = (phone, password) => {
  return request({
    url: '/user/login',
    method: 'post',
    params: { phone, password }
  })
}

export const getUserInfo = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export const updateUser = (data) => {
  return request({
    url: '/user/update',
    method: 'put',
    data
  })
}

export const realNameAuth = (realName, idCard) => {
  return request({
    url: '/user/realname',
    method: 'post',
    params: { realName, idCard }
  })
}

export const addVehicle = (data) => {
  return request({
    url: '/user/vehicle/add',
    method: 'post',
    data
  })
}

export const getVehicleList = () => {
  return request({
    url: '/user/vehicle/list',
    method: 'get'
  })
}

export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const changePassword = (oldPassword, newPassword) => {
  return request({
    url: '/user/password/change',
    method: 'post',
    params: { oldPassword, newPassword }
  })
}

export const searchUsers = (params) => {
  return request({
    url: '/user/search',
    method: 'get',
    params
  })
}

