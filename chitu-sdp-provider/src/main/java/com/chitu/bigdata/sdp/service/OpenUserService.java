package com.chitu.bigdata.sdp.service;

import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.api.vo.OpenLogin;
import com.chitu.bigdata.sdp.mapper.SdpUserMapper;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OpenUserService extends GenericService<SdpUser, Long> {

    public OpenUserService(@Autowired SdpUserMapper sdpUserMapper) {
        super(sdpUserMapper);
    }

    public SdpUserMapper getMapper() {
        return (SdpUserMapper) super.genericMapper;
    }

    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @Autowired
    private SdpUserMapper userMapper;

    public ResponseData openLogin(OpenLogin openLogin){
        // 账号密码校验
        if(StrUtil.isBlank(openLogin.getEmployeeNumber()) || StrUtil.isBlank(openLogin.getPassword())){
            return new ResponseData<>(ResponseCode.WRONG_ACCOUNT_PASSWORD);
        }
        SdpUser user = new SdpUser();
        user.setEmployeeNumber(openLogin.getEmployeeNumber());
        user.setPassword(openLogin.getPassword());
        List<SdpUser> userList = selectAll(user);
        if(CollectionUtils.isEmpty(userList)){
            return new ResponseData<>(ResponseCode.WRONG_ACCOUNT_PASSWORD);
        }
        // 登录成功生成token
        user.setLastLogin(new Timestamp(System.currentTimeMillis()));
        userMapper.updateByEmployeeNumber(user);
        String token = UUID.randomUUID().toString().replace("-","");

        ResponseData resp = new ResponseData();
        // 存入redis
        ValueOperations valueOperations = redisTmplate.opsForValue();
        valueOperations.set(token,userList.get(0).getId().toString(),1, TimeUnit.HOURS);
        resp.setData(token);
        resp.ok();
        return resp;
    }

    public ResponseData openLogout(HttpServletRequest request){
        String token = request.getHeader("token");
        if(StrUtil.isBlank(token)){
            return new ResponseData(ResponseCode.TOKEN_NOT_EXIST);
        }
        boolean res = redisTmplate.delete(token);
        if(!res){
            String userId = redisTmplate.opsForValue().get(token);
            if(StrUtil.isBlank(userId)){
                return new ResponseData(ResponseCode.TOKEN_NOT_EXIST);
            }else{
                return new ResponseData(ResponseCode.ERROR);
            }
        }
        ResponseData<String> resp = new ResponseData<>();
        resp.ok();
        return resp;
    }
}
