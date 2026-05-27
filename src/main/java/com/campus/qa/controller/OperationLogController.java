package com.campus.qa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.qa.common.Result;
import com.campus.qa.entity.OperationLog;
import com.campus.qa.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/log")
@CrossOrigin
public class OperationLogController {
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    public Result<IPage<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String username) {
        Page<OperationLog> page = new Page<>(current, size);
        IPage<OperationLog> result = operationLogService.pageLogs(page, module, operationType, username);
        return Result.success(result);
    }
    
    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    public Result<OperationLog> getById(@PathVariable Long id) {
        OperationLog log = operationLogService.getById(id);
        return log != null ? Result.success(log) : Result.error("日志不存在");
    }
}


