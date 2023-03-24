package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;
import com.chitu.bigdata.sdp.api.domain.MetadataTableColumn;
import com.chitu.bigdata.sdp.api.enums.MetaTableType;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.service.DataSourceService;
import com.chitu.bigdata.sdp.service.FileService;
import com.chitu.bigdata.sdp.service.datasource.DataSourceFactory;
import com.chitu.bigdata.sdp.service.datasource.KafkaDataSource;
import com.chitu.bigdata.sdp.utils.CommonSqlParser;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.chitu.cloud.web.test.BaseTest;
import groovy.util.logging.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zouchangzhen
 * @date 2022/5/20
 */
@Slf4j
public class KafkaDataSourceTest  extends BaseTest {
    @Autowired
    KafkaDataSource kafkaDataSource;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    DataSourceFactory dataSourceFactory;
    @Autowired
    FileService fileService;

    @Test
    public void testWaitConsumedSum(){
        SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(246L);
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
        connectInfo.setDatabaseName(sdpDataSource.getDatabaseName());
        connectInfo.setUsername(sdpDataSource.getUserName());
        connectInfo.setPwd(sdpDataSource.getPassword());
        connectInfo.setCertifyType(sdpDataSource.getCertifyType());
        connectInfo.setHbaseZnode(sdpDataSource.getHbaseZnode());

        Long aLong = null;
        try {
            aLong = kafkaDataSource.waitConsumedNum(connectInfo, FlinkConfigKeyConstant.TIMESTAMP, "zcz", "test_fff.*",1653044248000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(aLong);
    }




    @Test
    public void generateDDL(){
        String topic = "test_a19";
        SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(246L);
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
        connectInfo.setDatabaseName(sdpDataSource.getDatabaseName());
        connectInfo.setUsername(sdpDataSource.getUserName());
        connectInfo.setPwd(sdpDataSource.getPassword());
        connectInfo.setCertifyType(sdpDataSource.getCertifyType());
        connectInfo.setHbaseZnode(sdpDataSource.getHbaseZnode());
        List<MetadataTableColumn> metadataTableColumnList = new ArrayList<>();
        try {
            //获取物理表字段信息
            metadataTableColumnList = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).getTableColumns(connectInfo, topic);
            System.out.println(metadataTableColumnList);
        }catch (Exception e){
            e.printStackTrace();
        }

        FlinkTableGenerate flinkTableGenerate = new FlinkTableGenerate();

        flinkTableGenerate.setAddress(sdpDataSource.getDataSourceUrl());
        flinkTableGenerate.setStreamLoadUrl(sdpDataSource.getStreamLoadUrl());
        flinkTableGenerate.setDatabaseName(sdpDataSource.getDatabaseName());
        flinkTableGenerate.setUserName(sdpDataSource.getUserName());
        flinkTableGenerate.setPwd(sdpDataSource.getPassword());
        flinkTableGenerate.setMetadataTableColumnList(metadataTableColumnList);
        flinkTableGenerate.setCertifyType(sdpDataSource.getCertifyType());
        flinkTableGenerate.setHbaseZnode(sdpDataSource.getHbaseZnode());
        flinkTableGenerate.setHadoopConfDir(sdpDataSource.getHadoopConfDir());


        flinkTableGenerate.setMetaTableType(MetaTableType.SOURCE.getType());
        flinkTableGenerate.setProjectCode("datahub_project_code");
        flinkTableGenerate.setJobName("datahub_job_name");
        flinkTableGenerate.setFlinkTableName("datahub_flink_table");
        flinkTableGenerate.setSourceTableName(topic);
        String flinkTableDdl = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).generateDdl(flinkTableGenerate);
        System.out.println(flinkTableDdl);

        try {
            CommonSqlParser sqlParser = new CommonSqlParser(flinkTableDdl);
            Map<String, String> options = SqlParserUtil.getSqlTableOptionMap(sqlParser.getCreateTableList().get(0));
            options = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).replaceConnParam(options, sdpDataSource);
            System.out.println(options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
        connectInfo.setUsername(sdpDataSource.getUserName());
        connectInfo.setPwd(sdpDataSource.getPassword());
        connectInfo.setCertifyType(sdpDataSource.getCertifyType());
        String errorMsg = kafkaDataSource.checkAsalConnect(connectInfo, topic, "groupId");
        System.out.println(errorMsg);
    }


}
