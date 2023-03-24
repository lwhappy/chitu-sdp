package com.chitu.bigdata.sdp.controller;

import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.api.vo.TokenResp;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author chenyun
 * @create 2021-10-12 11:01
 */
@RestController
@Api(tags="系统登录")
@RequestMapping(value = "/auth")
@Slf4j
public class LoginController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @ApiOperation(value="登录回调")
    @GetMapping(value = "/login/callback")
    public void loginCallback(@RequestParam(value = "ticket") String ticket,HttpServletResponse response, HttpServletRequest request) throws Exception{
        String redirectUrl = "https://" + request.getServerName() + ":" + request.getServerPort();
        redirectUrl += "/#/?ticket=" + ticket;
        response.sendRedirect(redirectUrl);
    }

    @ApiOperation(value="跳转校验token")
    @PostMapping(value = "/redirectLogin")
    public ResponseData redirectLogin(@RequestBody TokenResp token ) {
        ResponseData<TokenResp> resp = new ResponseData<>();
//        TokenResp tokenResp = loginService.redirectLogin(token);
//        resp.setData(tokenResp).ok();
        return resp;
    }
    @ApiOperation(value="获取用户信息")
    @GetMapping(value = "/getUserInfo")
    public ResponseData getUserInfo(@RequestParam(value = "employeeNumber") String employeeNumber,@RequestParam(value = "token") String token) {
        ResponseData resp = new ResponseData<>();
        if(StrUtil.isBlank(employeeNumber) || StrUtil.isBlank(token)){
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT,"employeeNumber token");
        }
        String userId = redisTmplate.opsForValue().get(token);
        if(StrUtil.isBlank(userId)){
            return new ResponseData(ResponseCode.TOKEN_NOT_EXIST);
        }
        SdpUser user = userService.get(Long.valueOf(userId));
        if (Objects.isNull(user)){
            throw new ApplicationException(ResponseCode.TOKEN_NOT_EXIST);
        }else {
            if(!user.getEmployeeNumber().equals(employeeNumber)){
                throw new ApplicationException(ResponseCode.INVALID_ARGUMENT,"employeeNumber token");
            }
            resp.setData(user).ok();
        }
        return resp;
    }



}
