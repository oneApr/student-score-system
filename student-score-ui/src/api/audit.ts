import request from '@/utils/request'

// 成绩审核 - 分页查询
export function getAuditList(params: any) {
    return request({
        url: '/admin/audit/page',
        method: 'get',
        params
    })
}

// 审核通过
export function auditPass(id: number) {
    return request({
        url: `/admin/audit/pass/${id}`,
        method: 'put'
    })
}

// 审核驳回
export function auditReject(id: number, comment: string) {
    return request({
        url: `/admin/audit/reject/${id}`,
        method: 'put',
        params: { comment }
    })
}
