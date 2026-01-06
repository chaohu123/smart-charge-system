import request from './request'

export const getTicketList = (params) => {
  return request({
    url: '/admin/maintenance/list',
    method: 'get',
    params: {
      current: params.current || 1,
      size: params.size || 10,
      ...(params.status !== undefined && { status: params.status }),
      ...(params.stationId !== undefined && { stationId: params.stationId }),
      ...(params.ticketNo && { ticketNo: params.ticketNo }),
      ...(params.stationName && { stationName: params.stationName })
    }
  })
}

export const getTicketDetail = (id) => {
  return request({
    url: `/admin/maintenance/${id}`,
    method: 'get'
  })
}

export const createTicket = (data) => {
  return request({
    url: '/admin/maintenance/create',
    method: 'post',
    data
  })
}

export const updateTicketStatus = (id, status, isNormal, remark) => {
  return request({
    url: `/admin/maintenance/${id}/status`,
    method: 'put',
    params: { 
      status,
      ...(isNormal !== undefined && { isNormal }),
      ...(remark && { remark })
    }
  })
}

export const assignTicket = (id, assigneeId) => {
  return request({
    url: `/admin/maintenance/${id}/assign`,
    method: 'put',
    params: { assigneeId }
  })
}

export const getMaintenanceRecordList = (params) => {
  return request({
    url: '/admin/maintenance/record/list',
    method: 'get',
    params
  })
}

export const createMaintenanceRecord = (data) => {
  return request({
    url: '/admin/maintenance/record/create',
    method: 'post',
    data
  })
}

