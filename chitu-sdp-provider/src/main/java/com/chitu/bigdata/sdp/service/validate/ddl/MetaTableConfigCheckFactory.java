package com.chitu.bigdata.sdp.service.validate.ddl;

import com.chitu.bigdata.sdp.api.enums.DataSourceType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import com.chitu.bigdata.sdp.mapper.SdpDataSourceMapper;
import com.chitu.bigdata.sdp.service.DataSourceService;
import com.chitu.bigdata.sdp.service.validate.ddl.check.KafkaMetaTableConfigCheck;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.cloud.model.ResponseData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * MetaTableConfig校验工厂类
 * @author zouchangzhen
 * @date 2022/4/1
 */
@Component
public class MetaTableConfigCheckFactory {

    @Autowired
    SdpDataSourceMapper sdpDataSourceMapper;
    @Autowired
    DataSourceService dataSourceService;

    /**
     * 数据格式的beanName
     * 例如 json格式的对应的是 {@link KafkaMetaTableConfigCheck}
     */
    public final static String BEAN_NAME = "%sMetaTableConfigCheck";

    @Autowired
    Map<String, AbstractMetaTableConfigCheck> metaTableConfigCheckMap = new ConcurrentHashMap<>(10);

    public AbstractMetaTableConfigCheck getMetaTableConfigCheck(DataSourceType dataSourceType) {
        AbstractMetaTableConfigCheck metaTableConfigCheck = metaTableConfigCheckMap.get(String.format(BEAN_NAME,dataSourceType.getType()));
        if (metaTableConfigCheck == null) {
            throw new RuntimeException(dataSourceType.getType() + "数据库类型的校验处理器不存在!");
        }
        return metaTableConfigCheck;
    }


    /**
     * 校验
     * @param metaTableConfigList
     * @return
     */
    public ResponseData checkEachItem(List<SdpMetaTableConfig> metaTableConfigList){
        ResponseData responseData = new ResponseData<>();
        responseData.ok();

        if(CollectionUtils.isEmpty(metaTableConfigList)){
            //如果是空，则不需要校验，校验通过
            return responseData;
        }

        //获取数据源信息
        List<Long> dataSourceIds = metaTableConfigList.stream().map(m -> m.getDataSourceId()).collect(Collectors.toList());
        List<SdpDataSource> dataSources = dataSourceService.getByIds(dataSourceIds.toArray(new Long[dataSourceIds.size()]));
        Map<Long, SdpDataSource> dataSourceMap = Optional.ofNullable(dataSources).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpDataSource::getId, m -> m, (k1, k2) -> k2));

        AtomicBoolean sqlValidate = new AtomicBoolean(true);
        List<Map<String,String>> validateErrorList = Lists.newArrayList();
        Map<String, String> validateErrorMap = null;

        for (SdpMetaTableConfig sdt : metaTableConfigList) {
            SdpDataSource sdpDataSource = dataSourceMap.get(sdt.getDataSourceId());
            if(Objects.isNull(sdpDataSource)){
                throw new RuntimeException("数据源不存在");
            }
            //数据源类型
            DataSourceType dataSourceType = DataSourceType.ofType(sdpDataSource.getDataSourceType());
            if(Objects.isNull(dataSourceType)){
                throw new RuntimeException("数据源类型不能为空");
            }

            if( DataSourceType.HIVE.name().equals(dataSourceType.name())
                    || DataSourceType.DATAGEN.name().equals(dataSourceType.name())
                    || DataSourceType.PRINT.name().equals(dataSourceType.name())

            ){
                 //如果是tidb或者hive，则不需要校验表名称是否一致
                continue;
            }

            SqlExplainResult sqlExplainResult =  getMetaTableConfigCheck(dataSourceType).check(sdt,sdpDataSource);
            if (Objects.nonNull(sqlExplainResult)) {
                validateErrorMap = Maps.newHashMapWithExpectedSize(3);
                validateErrorMap.put("flinkTableName", sdt.getFlinkTableName());
                validateErrorMap.put("errorMsg", sqlExplainResult.getError());
                validateErrorList.add(validateErrorMap);
                sqlValidate.set(false);
            }
        }

        if (!sqlValidate.get()) {
            responseData.setCode(ResponseCode.SQL_NOT_PASS.getCode());
            responseData.setMsg(ResponseCode.SQL_NOT_PASS.getMessage());
            responseData.setData(validateErrorList);
            return responseData;
        }

        return responseData;
    }
}
