package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.vo.OpenLogin;
import com.chitu.bigdata.sdp.service.OpenUserService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@Api(tags="开源系统登录")
@RequestMapping(value = "/auth/open")
@Slf4j
public class OpenLoginController {

    @Autowired
    private OpenUserService userService;

    @ApiOperation(value="登录")
    @PostMapping(value = "/login")
    public ResponseData login(@RequestBody OpenLogin openLogin){
        return userService.openLogin(openLogin);
    }

    @ApiOperation(value="退出")
    @PostMapping(value = "/logout")
    public ResponseData login(HttpServletRequest request){
        return userService.openLogout(request);
    }


}
