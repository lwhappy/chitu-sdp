package com.chitu.bigdata.sdp.test;

import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.SdpMetaTableConfigBO;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.service.DataSourceService;
import com.chitu.bigdata.sdp.service.FileService;
import com.chitu.bigdata.sdp.service.MetaTableConfigService;
import com.chitu.bigdata.sdp.service.datasource.DataSourceFactory;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.web.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author sutao
 * @create 2021-12-10 16:32
 */
public class DataSourceTest extends BaseTest {

    @Autowired
    MetaTableConfigService metaTableConfigService;

    @Autowired
    DataSourceFactory dataSourceFactory;

    @Autowired
    DataSourceService dataSourceService;

    @Autowired
    FileService fileService;

    @Autowired
    private DataSourceService sdpDataSourceService;

    @Test
    public void addMetaTableConfig() {

        List<SqlExplainResult> sqlExplainResults = fileService.executeValidate("CREATE TABLE company_weixin_record (\n" +
                "  id INT,\n" +
                "  mobile VARCHAR,\n" +
                "  old_mobile VARCHAR,\n" +
                "  weixin VARCHAR,\n" +
                "  weixin_flag VARCHAR,\n" +
                "  status VARCHAR,\n" +
                "  enabled_flag TINYINT,\n" +
                "  created_by VARCHAR,\n" +
                "  creation_date VARCHAR,\n" +
                "  updated_by VARCHAR,\n" +
                "  updation_date VARCHAR,\n" +
                "  trace_id VARCHAR,\n" +
                "  customer_id INT,\n" +
                "  PRIMARY KEY (mobile) NOT ENFORCED -- 主键字段\n" +
                ") WITH (\n" +
                "  'connector' = 'jdbc',\n" +
                "  'url' = '5993d57bb83e97f3d7be846d21d4cbd9',\n" +
                "  'username' = 'bdata',\n" +
                "  'password' = '******',\n" +
                "  'table-name' = 'company_weixin_record',\n" +
                "  'sink.parallelism' = '1'\n" +
                ");");

        System.out.println("");

    }

    @Test
    public void queryMetaTableConfig() {
        SdpMetaTableConfigBO sdpMetaTableConfigBO = new SdpMetaTableConfigBO();
        sdpMetaTableConfigBO.setFileId(264L);
        ResponseData responseData = metaTableConfigService.queryMetaTableConfig(sdpMetaTableConfigBO);
        System.out.println(JSONObject.toJSONString(responseData));
    }

    @Test
    public void testConn() {
        SdpDataSource sdpDataSource = new SdpDataSource();
        sdpDataSource.setDataSourceType("mysql");
        sdpDataSource.setDataSourceUrl("******");
        sdpDataSource.setDatabaseName("******");
        sdpDataSource.setUserName("******");
        sdpDataSource.setPassword("******");
        ResponseData responseData = sdpDataSourceService.checkConnect(sdpDataSource);
        System.out.println(JSONObject.toJSONString(responseData));
    }


    @Test
    public void tableExistVerify() throws Exception {
        SdpMetaTableConfigBO sdpMetaTableConfigBO = new SdpMetaTableConfigBO();

        //mysql
        sdpMetaTableConfigBO.setDataSourceId(78L);
        sdpMetaTableConfigBO.setMetaTableName("t_presto_audit_log");
        ResponseData responseData = metaTableConfigService.tableExistVerify(sdpMetaTableConfigBO);
        System.out.println(JSONObject.toJSONString(responseData));

        //es
        sdpMetaTableConfigBO.setDataSourceId(7L);
        sdpMetaTableConfigBO.setMetaTableName("movie_info_20200617");
        sdpMetaTableConfigBO.setMetaTableName("movie_template1");
        responseData = metaTableConfigService.tableExistVerify(sdpMetaTableConfigBO);
        System.out.println(JSONObject.toJSONString(responseData));


        //kafka
        sdpMetaTableConfigBO.setDataSourceId(54L);
        sdpMetaTableConfigBO.setMetaTableName("crm-customer_book_car_visit");
        responseData = metaTableConfigService.tableExistVerify(sdpMetaTableConfigBO);
        System.out.println(JSONObject.toJSONString(responseData));

        //doris
        sdpMetaTableConfigBO.setDataSourceId(31L);
        sdpMetaTableConfigBO.setMetaTableName("testTable");
        responseData = metaTableConfigService.tableExistVerify(sdpMetaTableConfigBO);
        System.out.println(JSONObject.toJSONString(responseData));


    }

    @Test
    public void generateDdl() throws Exception {
        SdpMetaTableConfigBO sdpMetaTableConfigBO = new SdpMetaTableConfigBO();

        //mysql
//        sdpMetaTableConfigBO.setFileId(501L);
//        sdpMetaTableConfigBO.setDataSourceId(4L);
//        sdpMetaTableConfigBO.setFlinkTableName("sdp_meta_table_config");
//        sdpMetaTableConfigBO.setMetaTableName("sdp_meta_table_config");
//        ResponseData responseData = metaTableConfigService.generateDdl(sdpMetaTableConfigBO);
//        System.out.println(responseData.getData());

        //es
        sdpMetaTableConfigBO.setDataSourceId(33L);
        sdpMetaTableConfigBO.setFileId(685L);
        sdpMetaTableConfigBO.setFlinkTableName("users");
        sdpMetaTableConfigBO.setMetaTableName("users");
        sdpMetaTableConfigBO.setMetaTableType("sink");
        ResponseData responseData = metaTableConfigService.generateDdl(sdpMetaTableConfigBO);
        System.out.println(responseData.getData());

        //kafka   order_info_01   test_tex  doris_test_01  crm_customer_repeat_detail2_par    test_kafka_json  bigdata-fund_budget_fillin_source
        //sdpMetaTableConfigBO.setDataSourceId(55L);
        //sdpMetaTableConfigBO.setDataSourceId(68L);
//        sdpMetaTableConfigBO.setDataSourceId(6L);
//        sdpMetaTableConfigBO.setFlinkTableName("crm-customer_book_car_visit");
//        sdpMetaTableConfigBO.setMetaTableName("crm-customer_book_car_visit");
//        sdpMetaTableConfigBO.setMetaTableType("source");
//        responseData = metaTableConfigService.generateDdl(sdpMetaTableConfigBO);
//        System.out.println(responseData.getData());
//
//        //doris
//        sdpMetaTableConfigBO.setDataSourceId(31L);
//        sdpMetaTableConfigBO.setFlinkTableName("testTable");
//        sdpMetaTableConfigBO.setMetaTableName("testTable");
//        responseData = metaTableConfigService.generateDdl(sdpMetaTableConfigBO);
//        System.out.println(responseData.getData());

    }

}
