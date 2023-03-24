package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.MetadataTableColumn;
import com.chitu.bigdata.sdp.service.datasource.ElasticSearchDataSource;
import com.chitu.cloud.web.test.BaseTest;
import groovy.util.logging.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/6/1
 */
@Slf4j
public class ElasticSearchDataSourceTest  extends BaseTest {
    @Autowired
    ElasticSearchDataSource elasticSearchDataSource;


    /**
     * es6
     * @throws Exception
     */
    @Test
    public void testElasticSearch6DataSource() throws Exception{
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setAddress("******");
        connectInfo.setUsername("******");
        connectInfo.setPwd("******");
        RestHighLevelClient connection = elasticSearchDataSource.getConnection(connectInfo);
        elasticSearchDataSource.closeConnection(connection);

        boolean tableExists = elasticSearchDataSource.tableExists(connectInfo, "myuser_testes6_20220601");
        System.out.println(tableExists);

         tableExists = elasticSearchDataSource.tableExists(connectInfo, "myuser");
        System.out.println(tableExists);

        List<String> tables = elasticSearchDataSource.getTables(connectInfo);
        System.out.println(tables);

        List<MetadataTableColumn> cols = elasticSearchDataSource.getTableColumns(connectInfo, "myuser_testes6_20220601");
        System.out.println(cols);
    }

}
