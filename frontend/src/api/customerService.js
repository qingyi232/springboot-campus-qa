import request from '@/utils/request'

// 获取或创建客服会话
export function getSession() {
  return request({
    url: '/api/customer-service/session',
    method: 'get'
  })
}

// 获取会话详情
export function getSessionDetail(sessionId) {
  return request({
    url: `/api/customer-service/session/${sessionId}`,
    method: 'get'
  })
}

// 管理员接入会话
export function acceptSession(sessionId) {
  return request({
    url: `/api/customer-service/session/${sessionId}/accept`,
    method: 'post'
  })
}

// 关闭会话
export function closeSession(sessionId) {
  return request({
    url: `/api/customer-service/session/${sessionId}/close`,
    method: 'post'
  })
}

// 发送消息
export function sendMessage(data) {
  return request({
    url: '/api/customer-service/message',
    method: 'post',
    data
  })
}

// 获取会话消息历史
export function getSessionMessages(sessionId) {
  return request({
    url: `/api/customer-service/session/${sessionId}/messages`,
    method: 'get'
  })
}

// 标记消息已读
export function markAsRead(sessionId) {
  return request({
    url: `/api/customer-service/session/${sessionId}/read`,
    method: 'post'
  })
}

// 获取等待中的会话列表（管理员）
export function getWaitingSessions(params) {
  return request({
    url: '/api/customer-service/waiting-sessions',
    method: 'get',
    params
  })
}

// 获取服务中的会话列表（管理员）
export function getServingSessions(params) {
  return request({
    url: '/api/customer-service/serving-sessions',
    method: 'get',
    params
  })
}

// 获取管理员的会话列表
export function getMySession(params) {
  return request({
    url: '/api/customer-service/my-sessions',
    method: 'get',
    params
  })
}

// 获取在线状态
export function getOnlineStatus(userId, role) {
  return request({
    url: '/api/customer-service/online-status',
    method: 'get',
    params: { userId, role }
  })
}

// 获取在线人数
export function getOnlineCount() {
  return request({
    url: '/api/customer-service/online-count',
    method: 'get'
  })
}


