package com.chitu.bigdata.sdp.service.datasource;

import com.chitu.bigdata.sdp.api.domain.*;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.utils.CommonSqlParser;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.google.common.collect.Maps;
import com.xiaoleilu.hutool.io.FileUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.flink.sql.parser.ddl.SqlCreateCatalog;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 *
 * @author zouchangzhen
 * @date 2022/4/15
 */
@Component("hive")
@Slf4j
public class HiveDataSource extends AbstractDataSource<HiveConnection> {
    /**
     * 验证是否存在文件夹： 调用方 {@link com.chitu.bigdata.sdp.service.DataSourceService#checkConnect(SdpDataSource)}
     * @param connectInfo
     * @return
     * @throws Exception
     */
    @Override
    public HiveConnection getConnection(ConnectInfo connectInfo) throws Exception {
        boolean exist = FileUtil.exist(connectInfo.getAddress());
        if (!exist){
            throw new RuntimeException("hive-conf-dir不存在");
        }
        if(StrUtil.isNotBlank(connectInfo.getHadoopConfDir())){
            exist = FileUtil.exist(connectInfo.getHadoopConfDir());
            if (!exist){
                throw new RuntimeException("hadoop-conf-dir不存在");
            }
        }
        return new HiveConnection();
    }

    @Override
    public void closeConnection(HiveConnection hiveConnection) {
        //置空，让垃圾回收器回收
//        hiveConnection = null;
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
        String opt = "\n";
        if(StrUtil.isNotBlank(flinkTableGenerate.getHadoopConfDir())){
            opt = ",\n  'hadoop-conf-dir' = '" + flinkTableGenerate.getHadoopConfDir() + "'  \n";
        }
        String tableDdl = String.format(
                "CREATE CATALOG `%s` \n" +
                        "WITH (\n" +
                        "  'type' = 'hive',\n" +
                        "  'hive-conf-dir' = '%s'" +
                        "%s" +
                        ");",
                flinkTableGenerate.getFlinkTableName(),
                flinkTableGenerate.getAddress(),
                opt
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
        CommonSqlParser commonSqlParser = new CommonSqlParser(ddl);
        SqlCreateCatalog sqlCreateCatalog = commonSqlParser.getCreateCatalogList().get(0);
        List<SqlNode> options = sqlCreateCatalog.getPropertyList().getList();

        Map<String,String> optionsMap = Maps.newHashMap();
        optionsMap.put(FlinkConfigKeyConstant.HIVE_CONF_DIR,sdpDataSource.getDataSourceUrl());
        optionsMap.put(FlinkConfigKeyConstant.HADOOP_CONF_DIR,sdpDataSource.getHadoopConfDir());
        Map<String, SqlTableOption> replaceOptionsMap = SqlParserUtil.getTableOptionMap(optionsMap);

        int offset1 = removeOption(options, FlinkConfigKeyConstant.HIVE_CONF_DIR);
        if (-1 == offset1) {
            insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.HIVE_CONF_DIR));
        } else {
            insertOption(options, offset1, replaceOptionsMap.get(FlinkConfigKeyConstant.HIVE_CONF_DIR));
        }

        int offset2 = removeOption(options, FlinkConfigKeyConstant.HADOOP_CONF_DIR);
        if (-1 == offset2) {
            insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.HADOOP_CONF_DIR));
        } else {
            insertOption(options, offset2, replaceOptionsMap.get(FlinkConfigKeyConstant.HADOOP_CONF_DIR));
        }

        StringBuilder modifySql = flinkSqlBuilder(ddl, options);

        return modifySql.toString();
    }
}
