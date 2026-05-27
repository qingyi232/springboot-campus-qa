import request from '@/utils/request'

// 提交反馈
export function submitFeedback(data) {
  return request({
    url: '/api/feedback/submit',
    method: 'post',
    data
  })
}

// 分页查询反馈列表（管理员）
export function getFeedbackPage(params) {
  return request({
    url: '/api/feedback/page',
    method: 'get',
    params
  })
}

// 获取用户的反馈列表
export function getMyFeedback(params) {
  return request({
    url: '/api/feedback/my',
    method: 'get',
    params
  })
}

// 获取反馈详情
export function getFeedbackDetail(id) {
  return request({
    url: `/api/feedback/${id}`,
    method: 'get'
  })
}

// 处理反馈（管理员）
export function handleFeedback(id, data) {
  return request({
    url: `/api/feedback/${id}/handle`,
    method: 'post',
    data
  })
}

// 删除反馈（管理员）
export function deleteFeedback(id) {
  return request({
    url: `/api/feedback/${id}`,
    method: 'delete'
  })
}

// 获取待处理反馈数量
export function getPendingCount() {
  return request({
    url: '/api/feedback/pending-count',
    method: 'get'
  })
}


