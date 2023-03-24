package com.chitu.bigdata.sdp.api.domain;


import lombok.Data;

/**
 * 数据源连接信息
 * @author 587694
 */
@Data
public class ConnectInfo {

    private String address;
    private String databaseName;
    private String username;
    private String pwd;
    private String certifyType;
    private String hbaseZnode;
    private String hadoopConfDir;
    private String hudiCatalogPath;
}
