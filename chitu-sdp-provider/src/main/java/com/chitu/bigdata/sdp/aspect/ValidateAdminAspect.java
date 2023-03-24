package com.chitu.bigdata.sdp.aspect;

import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.cloud.model.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Order(3)
@Component
public class ValidateAdminAspect {

    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @Autowired
    private UserService userService;

    @Pointcut("@annotation(com.chitu.bigdata.sdp.annotation.ValidateAdmin)")
    private void pointCut(){}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("token");
        String uid = request.getHeader("X-uid");
        // 非空校验
        if(StrUtil.isBlank(token) || StrUtil.isBlank(uid)){
            return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS);
        }
        // 校验token跟uid是否匹配
        String userId = redisTmplate.opsForValue().get(token);
        if(StrUtil.isBlank(userId) || !uid.equals(userId)){
            return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS);
        }
        // 查询是否超级管理员
        SdpUser sdpUser = userService.get(Long.valueOf(uid));
        if(!(null != sdpUser && null != sdpUser.getIsAdmin() && sdpUser.getIsAdmin() == 1)){
            return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS);
        }
        return point.proceed();
    }
}
