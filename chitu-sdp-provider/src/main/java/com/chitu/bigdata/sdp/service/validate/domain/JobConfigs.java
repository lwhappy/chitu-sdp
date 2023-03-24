package com.chitu.bigdata.sdp.service.validate.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
public class JobConfigs {

    private boolean useResult = true;
    private boolean fragment = true;
    private boolean useSession = false;
    private boolean useRemote = false;
    private String savePointPath = "";
    private String jobName = "草稿";
    private Integer clusterId = 0;
    private Integer checkPoint = 0;
    private Integer maxRowNum = 100;
    private Integer parallelism = 1;
    private String statement;
    private String type;
    private String session;
    private Integer taskId;
    private Integer clusterConfigurationId;
    private Integer savePointStrategy;
    private String configJson;
    private boolean useStatementSet;
    private static final ObjectMapper mapper = new ObjectMapper();

    private String address;
    private boolean useSqlFragment = false;
    private Integer checkpoint = 0;
    private GatewayConfig gatewayConfig;
    private boolean useRestAPI;

    private Map<String,String> config;

    public JobConfigs() {
    }

    public ExecutorSetting getExecutorSetting(){
        return new ExecutorSetting(checkpoint = 0,parallelism,useSqlFragment = false,savePointPath,jobName);
    }


}
