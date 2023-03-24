package com.chitu.bigdata.sdp.service.datasource;

import com.chitu.bigdata.sdp.api.domain.*;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * print数据源
 * @author zouchangzhen
 * @date 2022/4/26
 */
@Component("print")
@Slf4j
public class PrintDataSource extends   AbstractDataSource<PrintConnection> {


    @Override
    public PrintConnection getConnection(ConnectInfo connectInfo) throws Exception {
        return new PrintConnection();
    }

    @Override
    public void closeConnection(PrintConnection printConnection) {
        //置空，让垃圾回收器回收
        printConnection = null;
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
                        " f_random_str STRING\n" +
                        ") WITH (\n" +
                        "  'connector' = 'print'\n" +
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
