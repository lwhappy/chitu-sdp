package com.chitu.bigdata.sdp.service.validate;


import com.chitu.bigdata.sdp.service.validate.constant.FlinkConstant;
import com.chitu.bigdata.sdp.service.validate.constant.NetConstant;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ClusterServiceImpl
 *
 * @author wenmo
 * @since 2021/5/28 14:02
 **/
public class ClusterServiceImpl {

    public String buildEnvironmentAddress() {
            return buildLocalEnvironmentAddress();
    }

    public String buildLocalEnvironmentAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            if(inetAddress!=null) {
                return inetAddress.getHostAddress()+ NetConstant.COLON+ FlinkConstant.FLINK_REST_DEFAULT_PORT;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return FlinkConstant.LOCAL_HOST;
    }

}
