import request from './request'

export const createEvaluation = (data) => {
  return request({
    url: '/evaluation/create',
    method: 'post',
    data
  })
}

export const uploadEvaluationImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/evaluation/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const likeEvaluation = (id) => {
  return request({
    url: `/evaluation/${id}/like`,
    method: 'post'
  })
}

export const unlikeEvaluation = (id) => {
  return request({
    url: `/evaluation/${id}/unlike`,
    method: 'post'
  })
}

export const checkLikeStatus = (id) => {
  return request({
    url: `/evaluation/${id}/like/status`,
    method: 'get'
  })
}

export const getEvaluationLikeCount = (id) => {
  return request({
    url: `/evaluation/${id}/like/count`,
    method: 'get'
  })
}

export const deleteEvaluation = (id) => {
  return request({
    url: `/evaluation/${id}`,
    method: 'delete'
  })
}

