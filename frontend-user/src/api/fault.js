import request from './request'

export const reportFault = (data) => {
  const formData = new FormData()
  formData.append('stationId', data.stationId)
  if (data.pileId) {
    formData.append('pileId', data.pileId)
  }
  formData.append('faultType', data.faultType)
  formData.append('description', data.description)
  if (data.images && data.images.length > 0) {
    data.images.forEach(file => {
      formData.append('images', file)
    })
  }
  
  return request({
    url: '/fault/report',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const getFaultReportList = () => {
  return request({
    url: '/fault/list',
    method: 'get'
  })
}

