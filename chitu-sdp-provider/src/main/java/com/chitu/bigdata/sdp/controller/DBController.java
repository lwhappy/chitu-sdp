package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.DBManageBO;
import com.chitu.bigdata.sdp.service.DBService;
import com.chitu.cloud.model.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/10 21:35
 */
@Slf4j
@RestController
@RequestMapping(value="/db")
public class DBController {

    @Autowired
    private DBService dbService;

    @RequestMapping(value="/execute",method= RequestMethod.POST)
    public ResponseData execute(@RequestBody DBManageBO dbManageBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(dbService.execute(dbManageBO));
        return data;
    }
}
