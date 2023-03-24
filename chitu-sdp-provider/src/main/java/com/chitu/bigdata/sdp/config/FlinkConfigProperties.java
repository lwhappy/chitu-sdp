package com.chitu.bigdata.sdp.config;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author sutao
 * @create 2021-11-30 11:45
 */
@Component
@ConfigurationProperties(prefix = "flink.config")
@Data
public class FlinkConfigProperties {

    private Map<String, String> defaultMap;

    private String sqlTemplate;

    private String dStreamTemplate;

    private List<Map<String, Object>> flinkVersions;

    public Map<String, Object> getFlinkVersion(String version){
        if(StrUtil.isEmpty(version)){
            return null;
        }else{
            return flinkVersions.stream().filter(map -> version.equals(map.get("version"))).findFirst().get();
        }
    }

}
