package com.chitu.bigdata.sdp.aspect;

import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.mapper.SdpProjectUserMapper;
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
public class ValidateProjectLeaderAspect {

    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @Autowired
    private SdpProjectUserMapper projectUserMapper;

    @Autowired
    private UserService userService;

    @Pointcut("@annotation(com.chitu.bigdata.sdp.annotation.ValidateProjectLeader)")
    private void pointCut(){}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 请求头数据
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("token");
        // 非空校验
        if(StrUtil.isBlank(token)){
            return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS);
        }
        String userId = redisTmplate.opsForValue().get(token);
        if(StrUtil.isBlank(userId)){
            return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS);
        }
        // 查询是否超级管理员
        SdpUser sdpUser = userService.get(Long.valueOf(userId));
        if(null != sdpUser && null != sdpUser.getIsAdmin() && sdpUser.getIsAdmin() == 1){
            return point.proceed();
        }
        // 校验项目人员是否包含此人
        int cnt = projectUserMapper.queryProjectUserIsLeader(userId);
        if(cnt <= 0){
            return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS);
        }
        return point.proceed();
    }
}
