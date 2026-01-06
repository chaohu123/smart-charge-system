import request from './request'

export const login = (username, password) => {
  return request({
    url: '/admin/login',
    method: 'post',
    params: { username, password }
  })
}

export const getAdminInfo = () => {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

