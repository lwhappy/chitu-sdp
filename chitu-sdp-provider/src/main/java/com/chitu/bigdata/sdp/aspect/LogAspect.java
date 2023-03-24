package com.chitu.bigdata.sdp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/20 15:12
 */
@Aspect
@Slf4j
@Order(2)
@Component
public class LogAspect {

    // 切点：所有被RequestMapping注解修饰的方法会织入advice
    @Pointcut("execution(public * com.chitu.bigdata.sdp.controller.*.*(..))")
    private void pointCut(){}

    // Before表示advice将在目标方法执行前执行
    @Before("pointCut()")
    private void before(JoinPoint joinPoint) {
        String methodName = getMethodName(joinPoint);
        Object[] args = joinPoint.getArgs();
        if(args.length > 0){
            if(!(args[0] instanceof MultipartFile)){
                try{
//                    log.info("方法：[{}],请求参数==> {}",methodName, JSON.toJSONString(args[0], SerializerFeature.DisableCircularReferenceDetect));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        Object result = joinPoint.proceed(args);
        long endTime = System.currentTimeMillis();
        String methodName = getMethodName(joinPoint);
//        log.info("方法：[{}],执行耗时==> {} s", methodName,(endTime - startTime)/1000);
//        log.info("方法：[{}],返回结果==> {}\n\t", methodName, JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect));
        return result;
    }

    private String getMethodName(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName + "()";
    }
}
