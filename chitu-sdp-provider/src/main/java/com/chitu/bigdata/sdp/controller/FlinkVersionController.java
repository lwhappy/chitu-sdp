package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.config.FlinkConfigProperties;
import com.chitu.cloud.model.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/flink")
public class FlinkVersionController {


    @Autowired
    FlinkConfigProperties flinkConfigProperties;

    @RequestMapping(value="/version/list",method= RequestMethod.POST)
    public ResponseData start() {
        ResponseData responseData = new ResponseData<>();
        responseData.setData(flinkConfigProperties.getFlinkVersions()).ok();
        return responseData;
    }
}
