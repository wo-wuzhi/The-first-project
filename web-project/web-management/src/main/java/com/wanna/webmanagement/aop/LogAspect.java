package com.wanna.webmanagement.aop;

import com.wanna.webmanagement.mapper.OperateLogMapper;
import com.wanna.webmanagement.pojo.OperateLog;
import com.wanna.webmanagement.util.BaseContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperateLogMapper operateLogMapper;


    @Around("@annotation(com.wanna.webmanagement.anno.Log)")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {     //非@around用joinPoint是pjp父类

        Integer operateEmpId = BaseContext.getCurrentId();          // 拦截器放进去的登录用户
        LocalDateTime operateTime = LocalDateTime.now();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        String methodParams = Arrays.toString(joinPoint.getArgs());

        long begin = System.currentTimeMillis();
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long costTime = System.currentTimeMillis() - begin;
            String returnValue = (result == null) ? null : result.toString();

            OperateLog operateLog = new OperateLog();
            operateLog.setOperateEmpId(operateEmpId);
            operateLog.setOperateTime(operateTime);
            operateLog.setClassName(className);
            operateLog.setMethodName(methodName);
            operateLog.setMethodParams(methodParams);
            operateLog.setReturnValue(returnValue);
            operateLog.setCostTime(costTime);

            try {
                operateLogMapper.insert(operateLog);
            } catch (Exception e) {
                log.error("记录操作日志失败: {}", e.getMessage(), e);
            }
        }
    }
}
