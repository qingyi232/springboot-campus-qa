import request from '@/utils/request';

export function getRegistrations(current, size) {
  return request({
    url: '/activity-registration/page',
    method: 'get',
    params: { current, size },
  });
}

export function auditRegistration(id, auditStatus) {
  return request({
    url: `/activity-registration/${id}/audit`,
    method: 'put',
    data: { auditStatus },
  });
}