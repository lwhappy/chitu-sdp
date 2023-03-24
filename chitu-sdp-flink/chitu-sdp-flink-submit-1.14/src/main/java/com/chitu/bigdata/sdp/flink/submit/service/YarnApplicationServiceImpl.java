package com.chitu.bigdata.sdp.flink.submit.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.flink.Application;
import com.chitu.bigdata.sdp.flink.common.conf.ConfigConst;
import com.chitu.bigdata.sdp.flink.common.enums.ApplicationType;
import com.chitu.bigdata.sdp.flink.common.enums.DevelopmentMode;
import com.chitu.bigdata.sdp.flink.common.enums.ExecutionMode;
import com.chitu.bigdata.sdp.flink.common.enums.ResolveOrder;
import com.chitu.bigdata.sdp.flink.common.util.HdfsUtils;
import com.chitu.bigdata.sdp.flink.submit.config.FlinkConfig;
import com.chitu.bigdata.sdp.flink.submit.constant.FlinkConstant;
import com.chitu.bigdata.sdp.flink.submit.util.FlinkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sutao
 * @create 2021-10-17 11:07
 */
@Service("yarn-application")
@Slf4j
public class YarnApplicationServiceImpl implements FlinkAppService {


    @Autowired
    private FlinkConfig flinkConfig;


    /**
     * 启动 flink job
     *
     * @param application
     * @return SubmitResponse
     */
    public SubmitResponse start(Application application) {

        if (DevelopmentMode.FLINKSQL.getValue().equals(application.getJobType())) {
            application.setFlinkUserJar(String.format("%s/%s", HdfsUtils.getDefaultFS().concat(ConfigConst.APP_PLUGINS()),flinkConfig.getSqlJar()));
        }
        Map<String, Object> optionMap = getOptionMap(application.getOptions());
        optionMap.put(ConfigConst.KEY_FLINK_SQL(null), application.getFlinkSql());
        optionMap.put(ConfigConst.KEY_JOB_ID(), application.getId());
        ResolveOrder resolveOrder = ResolveOrder.of(application.getResolveOrder());

        //获取dstream main参数
        Map<String, Object> mainArgsMap = new HashMap<>();
        optionMap.forEach((k, v) -> {
            if (StrUtil.startWith(k, FlinkConstant.MAIN_ARGS_PREFIX)) {
                mainArgsMap.put(k, v);
            }
        });

        //提交参数
        SubmitRequest submitInfo = new SubmitRequest(
                flinkConfig.getFlinkHome(),
                FlinkUtils.getFlinkVersion(),
                FlinkUtils.getFlinkDefaultConfig(),
                application.getFlinkUserJar(),
                application.getMainClass(),
                mainArgsMap,
                DevelopmentMode.of(application.getJobType()),
                ExecutionMode.of(application.getExecutionMode()),
                resolveOrder,
                application.getJobName(),
                null,
                ApplicationType.of(application.getAppType()).getName(),
                application.getSavePoint(),
                null,
                optionMap,
                application.getUdfPath()
        );

        SubmitResponse submitResponse = FlinkSubmit.submit(submitInfo);
        return submitResponse;
    }

    private Map<String, Object> getOptionMap(String options) {
        Map<String, Object> map = JSON.parseObject(options, Map.class);
        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }


    /**
     * 停止flink job
     *
     * @param application
     * @return String
     */
    public String stop(Application application) {
        log.info("begin stop flink job，{}", JSONObject.toJSONString(application));
        String savePointDir = FlinkSubmit.stop(
                flinkConfig.getFlinkHome(),
                ExecutionMode.of(application.getExecutionMode()),
                application.getAppId(),
                application.getJobId(),
                application.getSavePointed(),
                application.getDrain(),
                flinkConfig.getCancelTimeout());
        log.info("stop flink job finish，{}", savePointDir);
        return savePointDir;
    }

    /**
     * 触发flink job 添加 savepoint
     *
     * @param application
     * @return String
     */
    public String triggerSavepoint(Application application) {
        log.info("begin triggerSavepoint for flink job，{}", JSONObject.toJSONString(application));
        application.setSavePoint(application.getSavePoint() + "/" + application.getJobId());
        application.setSavePointed(true);
        String savePointDir = FlinkSubmit.triggerSavepoint(
                flinkConfig.getFlinkHome(),
                ExecutionMode.of(application.getExecutionMode()),
                application.getAppId(),
                application.getJobId(),
                application.getSavePointed(),
                flinkConfig.getTriggerSavepointTimeout(),
                application.getSavePoint()
                );
        log.info("triggerSavepoint for flink job finish，{}", savePointDir);
        return savePointDir;
    }


}
