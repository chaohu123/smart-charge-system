import request from './request'

export const scanQRCode = (qrCode) => {
  return request({
    url: '/pile/scan',
    method: 'post',
    params: { qrCode }
  })
}

export const generateQRCode = (pileId) => {
  return request({
    url: `/pile/${pileId}/qrcode`,
    method: 'post'
  })
}


