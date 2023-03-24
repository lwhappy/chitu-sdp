

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.annotation.ValidateAdmin;
import com.chitu.bigdata.sdp.annotation.ValidateProjectLeader;
import com.chitu.bigdata.sdp.api.bo.SdpUserBO;
import com.chitu.bigdata.sdp.api.bo.UserSearchBO;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.bigdata.sdp.utils.PaginationUtils;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * 用户管理页面
 * </pre>
 */
//@RefreshScope
@RestController
@Api(tags = "系统用户管理")
@RequestMapping(value = "/setting/userSetting")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @ApiOperation(value = "用户管理页面-用户列表")
    @RequestMapping(value ="/getUserList",method= RequestMethod.POST)
    @ValidateAdmin
    public ResponseData getUserList(@RequestBody SdpUserBO userBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        Pagination<SdpUser> pagination = PaginationUtils.getInstance4BO(userBO);
        this.userService.getUserDetail(pagination);
        data.setData(pagination);
        return data;
    }

    @ApiOperation(value = "用户管理页面-查询条件补全")
    @RequestMapping(value ="/getCondition",method= RequestMethod.POST)
    public ResponseData getCondition(@RequestBody UserSearchBO userBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.userService.getCondition(userBO));
        return data;
    }

    @ApiOperation(value = "用户管理-添加")
    @RequestMapping(value ="/addUser",method= RequestMethod.POST)
    public ResponseData addUser(@RequestBody SdpUser user) {
        ResponseData data = new ResponseData<>();
        data.setData(this.userService.insertUser(user));
        data.ok();
        return data;
    }

    @ApiOperation(value = "用户管理-修改")
    @ValidateAdmin
    @RequestMapping(value ="/updateUser",method= RequestMethod.POST)
    public ResponseData updateUser(@RequestBody SdpUser user) {
        ResponseData data = new ResponseData<>();
        data.setData(this.userService.updateUser(user));
        data.ok();
        return data;
    }

    @ApiOperation(value = "用户管理-删除用户")
    @RequestMapping(value ="/deleteUser",method= RequestMethod.POST)
    public ResponseData deleteUser(@RequestBody SdpUser user) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.userService.delete(user));
        return data;
    }

    @ValidateProjectLeader
    @ApiOperation(value = "用户管理页面-名字/工号查询HR接口")
    @RequestMapping(value ="/getHREmployee",method= RequestMethod.POST)
    public ResponseData getHREmployee(@RequestBody UserSearchBO userBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.userService.getCondition(userBO));
        return data;
    }

}