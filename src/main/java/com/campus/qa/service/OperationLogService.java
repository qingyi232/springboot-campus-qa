package com.campus.qa.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.qa.entity.OperationLog;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<OperationLog> {
    
    /**
     * 记录操作日志
     */
    void recordLog(String module, String operationType, String description, 
                   String method, String params, String ip);
    
    /**
     * 分页查询操作日志
     */
    IPage<OperationLog> pageLogs(Page<OperationLog> page, String module, 
                                 String operationType, String username);
}


