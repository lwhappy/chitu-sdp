package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import java.util.List;

/**
 * @author sutao
 * @create 2021-12-13 16:33
 */
@Data
public class FlinkTableGenerate {

    private String projectCode;
    private String jobName;

    private String metaTableType;

    private String flinkTableName;
    private String sourceTableName;
    private String address;

    /**
     * kafka特有参数，认证类型
     */
    private String certifyType;

    /**
     * doris特有参数
     */
    private String streamLoadUrl;

    /**
     * hbase特有参数
     */
    private String hbaseZnode;

    private String hadoopConfDir;


    private String databaseName;
    private String userName;
    private String pwd;

    private List<MetadataTableColumn> metadataTableColumnList;

    private String hudiCatalogPath;

}
