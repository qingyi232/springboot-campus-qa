import request from '@/utils/request'

// 获取总体统计数据
export function getOverallStatistics() {
  return request({
    url: '/api/registration-statistics/overall',
    method: 'get'
  })
}

// 获取报名趋势数据
export function getRegistrationTrend(days = 7) {
  return request({
    url: '/api/registration-statistics/trend',
    method: 'get',
    params: { days }
  })
}

// 获取活动分类统计
export function getCategoryStatistics() {
  return request({
    url: '/api/registration-statistics/category',
    method: 'get'
  })
}

// 获取热门活动排行
export function getPopularActivities(limit = 10) {
  return request({
    url: '/api/registration-statistics/popular',
    method: 'get',
    params: { limit }
  })
}

// 获取活动报名详情
export function getActivityDetail(activityId) {
  return request({
    url: `/api/registration-statistics/activity/${activityId}`,
    method: 'get'
  })
}

// 获取用户活跃度统计
export function getUserActivityStatistics(limit = 20) {
  return request({
    url: '/api/registration-statistics/user-activity',
    method: 'get',
    params: { limit }
  })
}

// 导出报名数据
export function exportData(activityId = null) {
  return request({
    url: '/api/registration-statistics/export',
    method: 'get',
    params: activityId ? { activityId } : {}
  })
}


