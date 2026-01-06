import request from './request'

// 获取提现列表（管理员）
export const getWithdrawList = (params) => {
  return request({
    url: '/withdraw/list',
    method: 'get',
    params
  })
}

// 审核提现
export const auditWithdraw = (id, status, reason) => {
  return request({
    url: '/withdraw/audit',
    method: 'post',
    params: { id, status, reason }
  })
}

// 获取提现详情（可选，如果需要的话）
export const getWithdrawDetail = (id) => {
  return request({
    url: `/withdraw/${id}`,
    method: 'get'
  })
}

