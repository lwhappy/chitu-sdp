package com.chitu.bigdata.sdp.service.datasource;

import com.chitu.bigdata.sdp.api.domain.*;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * datagen数据源
 * @author zouchangzhen
 * @date 2022/4/26
 */
@Component("datagen")
@Slf4j
public class DatagenDataSource extends   AbstractDataSource<DatagenConnection> {

    @Override
    public DatagenConnection getConnection(ConnectInfo connectInfo) throws Exception {
        return new DatagenConnection();
    }

    @Override
    public void closeConnection(DatagenConnection datagenConnection) {
        //置空，让垃圾回收器回收
        datagenConnection = null;
    }

    @Override
    public boolean tableExists(ConnectInfo connectInfo, String tableName) throws Exception {
        return false;
    }

    @Override
    public List<String> getTables(ConnectInfo connectInfo) throws Exception {
        return null;
    }

    @Override
    public List<MetadataTableColumn> getTableColumns(ConnectInfo connectInfo, String tableName) throws Exception {
        return null;
    }

    @Override
    public String generateDdl(FlinkTableGenerate flinkTableGenerate) {
        String tableDdl = String.format(
                "CREATE TABLE `%s` (\n" +
                        " f_sequence INT,\n" +
                        " f_random INT,\n" +
                        " f_random_str STRING,\n" +
                        " ts AS localtimestamp,\n" +
                        " WATERMARK FOR ts AS ts\n" +
                        ") WITH (\n" +
                        " 'connector' = 'datagen',\n" +
                        " 'rows-per-second'='1',\n" +
                        " 'fields.f_sequence.kind'='sequence',\n" +
                        " 'fields.f_sequence.start'='1',\n" +
                        " 'fields.f_sequence.end'='1000',\n" +
                        " 'fields.f_random.min'='1',\n" +
                        " 'fields.f_random.max'='1000',\n" +
                        " 'fields.f_random_str.length'='10'\n" +
                        ");",
                flinkTableGenerate.getFlinkTableName()
        );
        return tableDdl;
    }

    @Override
    public Map<String, String> replaceConnParam(Map<String, String> options, SdpDataSource sdpDataSource) {
        return options;
    }

    @Override
    public void initTable(SourceMeta meta) {

    }


    @Override
    public String modifyOption4ChangeEnv(String ddl, SdpDataSource sdpDataSource) throws Exception {
        return ddl;
    }
}
