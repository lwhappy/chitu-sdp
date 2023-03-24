

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-9
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.model.SdpDwsRunningResult;
import com.chitu.bigdata.sdp.service.SdpDwsRunningResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * 表现层控制类
 * </pre>
 */
//@RefreshScope
@RestController
//@RequestMapping(value = "/dq/sdpDwsRunningResult")
public class SdpDwsRunningResultController {
    private static final Logger logger = LoggerFactory.getLogger(SdpDwsRunningResultController.class);
    
    @Autowired
    private SdpDwsRunningResultService sdpDwsRunningResultService;

    //@PropertyFiltration(clazz = SdpDwsRunningResult.class, excludes = {"updationDate", "enabledFlag"}, includes = {"createdBy"})
    @RequestMapping("/{id}")
    public SdpDwsRunningResult get(@PathVariable("id") Long id) {
        SdpDwsRunningResult sdpDwsRunningResult = null;
        sdpDwsRunningResult = sdpDwsRunningResultService.get(id);

        return sdpDwsRunningResult;
    }
}