package com.campus.qa.aspect;

import com.alibaba.fastjson2.JSON;
import com.campus.qa.entity.OperationLog;
import com.campus.qa.entity.User;
import com.campus.qa.service.OperationLogService;
import com.campus.qa.service.UserService;
import com.campus.qa.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 操作日志AOP切面
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 定义切点：拦截所有Controller中的public方法（排除查询和登录注册）
     */
    @Pointcut("execution(public * com.campus.qa.controller..*.*(..)) " +
            "&& !execution(* com.campus.qa.controller..*.get*(..)) " +
            "&& !execution(* com.campus.qa.controller..*.find*(..)) " +
            "&& !execution(* com.campus.qa.controller..*.list*(..)) " +
            "&& !execution(* com.campus.qa.controller..*.page*(..)) " +
            "&& !execution(* com.campus.qa.controller..*.load*(..)) " +
            "&& !execution(* com.campus.qa.controller.AuthController.login(..)) " +
            "&& !execution(* com.campus.qa.controller.AuthController.register(..))")
    public void operationLog() {
    }

    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLog()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        log.info("🔍 AOP拦截到操作: {}", joinPoint.getSignature());
        
        OperationLog operationLog = new OperationLog();
        
        try {
            // 获取HttpServletRequest
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return joinPoint.proceed();
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 获取用户信息
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    String username = jwtUtil.getUsernameFromToken(token);
                    User user = userService.findByUsername(username);
                    if (user != null) {
                        operationLog.setUserId(user.getId());
                        operationLog.setUsername(user.getUsername());
                    }
                } catch (Exception e) {
                    log.warn("解析Token失败: {}", e.getMessage());
                }
            }
            
            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = method.getName();
            
            // 设置模块和操作类型
            String module = getModuleName(className);
            String operationType = getOperationType(methodName);
            
            operationLog.setModule(module);
            operationLog.setOperationType(operationType);
            operationLog.setMethod(className + "." + methodName);
            
            // 获取请求参数
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    // 过滤掉HttpServletRequest等无法序列化的参数
                    StringBuilder params = new StringBuilder();
                    for (Object arg : args) {
                        if (arg != null && !(arg instanceof HttpServletRequest) && !(arg instanceof javax.servlet.http.HttpServletResponse)) {
                            if (params.length() > 0) {
                                params.append(", ");
                            }
                            String argStr = JSON.toJSONString(arg);
                            // 限制参数长度
                            if (argStr.length() > 500) {
                                argStr = argStr.substring(0, 500) + "...";
                            }
                            params.append(argStr);
                        }
                    }
                    operationLog.setRequestParams(params.toString());
                } catch (Exception e) {
                    operationLog.setRequestParams("参数序列化失败");
                }
            }
            
            // 获取IP地址
            String ipAddress = getIpAddress(request);
            operationLog.setIpAddress(ipAddress);
            
            // 设置操作描述
            operationLog.setDescription(getOperationDescription(module, operationType, methodName));
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 计算执行时长
            long duration = System.currentTimeMillis() - startTime;
            operationLog.setDuration(duration);
            operationLog.setStatus(1); // 成功
            
            // 异步保存日志
            saveLogAsync(operationLog);
            
            return result;
            
        } catch (Throwable e) {
            // 记录异常
            long duration = System.currentTimeMillis() - startTime;
            operationLog.setDuration(duration);
            operationLog.setStatus(0); // 失败
            operationLog.setErrorMsg(e.getMessage());
            
            // 异步保存日志
            saveLogAsync(operationLog);
            
            throw e;
        }
    }

    /**
     * 异步保存日志
     */
    private void saveLogAsync(OperationLog operationLog) {
        try {
            log.debug("📝 准备保存操作日志: {}", operationLog.getDescription());
            boolean result = operationLogService.save(operationLog);
            if (result) {
                log.info("✅ 操作日志保存成功: {} - {}", operationLog.getModule(), operationLog.getDescription());
            } else {
                log.warn("⚠️ 操作日志保存失败（返回false）");
            }
        } catch (Exception e) {
            log.error("❌ 保存操作日志失败，完整异常: ", e);
        }
    }

    /**
     * 根据Controller名称获取模块名
     */
    private String getModuleName(String className) {
        if (className.contains("User")) return "用户管理";
        if (className.contains("QA") || className.contains("Qa")) return "智能问答";
        if (className.contains("News")) return "校园资讯";
        if (className.contains("Activity")) return "活动管理";
        if (className.contains("Content")) return "内容管理";
        if (className.contains("Notification")) return "消息通知";
        if (className.contains("Log")) return "操作日志";
        return "系统管理";
    }

    /**
     * 根据方法名获取操作类型
     */
    private String getOperationType(String methodName) {
        if (methodName.startsWith("add") || methodName.startsWith("save") || methodName.startsWith("create")) {
            return "CREATE";
        }
        if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("modify")) {
            return "UPDATE";
        }
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        }
        return "OTHER";
    }

    /**
     * 生成操作描述
     */
    private String getOperationDescription(String module, String operationType, String methodName) {
        StringBuilder desc = new StringBuilder();
        desc.append(module).append(" - ");
        
        switch (operationType) {
            case "CREATE":
                desc.append("新增");
                break;
            case "UPDATE":
                desc.append("修改");
                break;
            case "DELETE":
                desc.append("删除");
                break;
            default:
                desc.append("操作");
        }
        
        return desc.toString();
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
}

