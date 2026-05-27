package com.campus.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.qa.entity.OperationLog;
import com.campus.qa.mapper.OperationLogMapper;
import com.campus.qa.service.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> 
        implements OperationLogService {
    
    @Override
    public void recordLog(String module, String operationType, String description,
                         String method, String params, String ip) {
        OperationLog log = new OperationLog();
        log.setModule(module);
        log.setOperationType(operationType);
        log.setDescription(description);
        log.setMethod(method);
        log.setRequestParams(params);
        log.setIpAddress(ip);
        log.setStatus(1); // 成功
        
        save(log);
    }
    
    @Override
    public IPage<OperationLog> pageLogs(Page<OperationLog> page, String module,
                                       String operationType, String username) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        
        if (StringUtils.hasText(operationType)) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        
        wrapper.orderByDesc(OperationLog::getCreateTime);
        
        return page(page, wrapper);
    }
}

