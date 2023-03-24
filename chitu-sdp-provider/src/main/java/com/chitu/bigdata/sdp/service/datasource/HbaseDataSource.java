package com.chitu.bigdata.sdp.service.datasource;

import cn.hutool.crypto.SecureUtil;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;
import com.chitu.bigdata.sdp.api.domain.MetadataTableColumn;
import com.chitu.bigdata.sdp.api.domain.SourceMeta;
import com.chitu.bigdata.sdp.api.enums.DataSourceOption;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.constant.FlinkDataTypeMapping;
import com.chitu.bigdata.sdp.utils.CommonSqlParser;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenyun
 * @description: TODO
 * @date 2022/3/28 9:28
 */
@Component("hbase")
@Slf4j
public class HbaseDataSource extends AbstractDataSource<Connection> {

    @Override
    public Connection getConnection(ConnectInfo connectInfo) throws Exception {
        String clientPort = "";
        StringBuffer quorum = new StringBuffer();
        String[] clients = connectInfo.getAddress().split(",");
        for (String zookper : clients) {
            quorum.append(zookper.split(":")[0]).append(",");
            clientPort = zookper.split(":")[1];
        }
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", quorum.substring(0, quorum.length() - 1));
        config.set("hbase.zookeeper.property.clientPort", clientPort);
        config.set("zookeeper.znode.parent", connectInfo.getHbaseZnode());
        HBaseAdmin.available(config);
        return ConnectionFactory.createConnection(config);
    }

    @Override
    public void closeConnection(Connection connection) {
        close(null, null, connection);
    }

    @Override
    public boolean tableExists(ConnectInfo connectInfo, String tableName) throws Exception {
        boolean result = false;
        Connection connection = null;
        Admin admin = null;
        try {
            connection = getConnection(connectInfo);
            admin = connection.getAdmin();
            result = admin.tableExists(TableName.valueOf(connectInfo.getDatabaseName() + ":" + tableName));
        } finally {
            close(null, admin, connection);
        }
        return result;
    }

    @Override
    public List<MetadataTableColumn> getTableColumns(ConnectInfo connectInfo, String tableName) throws Exception {
        List<MetadataTableColumn> metadataTableColumns = new ArrayList<>();
        Connection connection = null;
        Admin admin = null;
        Table table = null;
        try {
            connection = getConnection(connectInfo);
            admin = connection.getAdmin();
            table = connection.getTable(TableName.valueOf(connectInfo.getDatabaseName() + ":" + tableName));
            Map<String, String> qualifiers = getQualifiers(table);
            Set<Map.Entry<String, String>> set = qualifiers.entrySet();
            if (!set.isEmpty()) {
                Iterator<Map.Entry<String, String>> iterator = set.iterator();
                MetadataTableColumn metadataTableColumn;
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    metadataTableColumn = new MetadataTableColumn();
                    metadataTableColumn.setColumnName(entry.getKey());
                    metadataTableColumn.setColumnType("ROW<" + entry.getValue() + ">");
                    metadataTableColumns.add(metadataTableColumn);
                }
            } else {
                TableDescriptor descriptor = admin.getDescriptor(TableName.valueOf(connectInfo.getDatabaseName() + ":" + tableName));
                ColumnFamilyDescriptor[] families = descriptor.getColumnFamilies();
                MetadataTableColumn metadataTableColumn;
                for (ColumnFamilyDescriptor family : families) {
                    metadataTableColumn = new MetadataTableColumn();
                    metadataTableColumn.setColumnName(family.getNameAsString());
                    metadataTableColumn.setColumnType("ROW<q1 " + FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE + ">");
                    metadataTableColumns.add(metadataTableColumn);
                }
            }
        } finally {
            close(table, admin, connection);
        }
        return metadataTableColumns;
    }

    public Map<String, String> getQualifiers(Table table) throws IOException {
        Map<String, String> result = new HashMap();

        Map<String,Set<String>> tempSet = Maps.newHashMap();

        Scan scan = new Scan();
        scan.setLimit(100);
        ResultScanner resultScanner = table.getScanner(scan);
        for (Result result1 : resultScanner) {
            for (Cell cell : result1.rawCells()) {
                String family = Bytes.toString(CellUtil.cloneFamily(cell));
                String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));

                String tQualifier = qualifier + " " + FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE;

                Set<String> qualifierSet = tempSet.getOrDefault(family, new LinkedHashSet<>());
                qualifierSet.add(tQualifier);
                tempSet.put(family,qualifierSet);

                /*if (StringUtils.isNotEmpty(result.get(family))) {
                    String qualifier1 = result.get(family) + "," + qualifier + " " + FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE;
                    result.put(family, qualifier1);
                } else {
                    String qualifier1 = qualifier + " " + FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE;
                    result.put(family, qualifier1);
                }*/
            }
        }

        if(!CollectionUtils.isEmpty(tempSet)){
            for(Map.Entry<String,Set<String>> m : tempSet.entrySet()){
                String qualifiers = m.getValue().stream().collect(Collectors.joining(","));
                result.put(m.getKey(),qualifiers);
            }
        }
        return result;
    }

    @Override
    public String generateDdl(FlinkTableGenerate flinkTableGenerate) {
        StringBuilder columns = new StringBuilder();
        columns.append("  ").append("`rowkey`").append(" ").append(FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE).append(",").append("\n");
        flinkTableGenerate.getMetadataTableColumnList().forEach(item -> {
            columns.append("  ").append(StrUtils.strWithBackticks(item.getColumnName())).append(" ").append(item.getColumnType()).append(",").append("\n");
        });
        String tableDdl = String.format(
                "CREATE TABLE %s (\n" +
                        "%s" +
                        "  PRIMARY KEY (`rowkey`) NOT ENFORCED -- 主键字段\n" +
                        ") WITH (\n" +
                        "  'connector' = 'hbase-2.2',\n" +
                        "  'table-name' = '%s',\n" +
                        "  'zookeeper.quorum' = '%s',\n" +
                        "  'zookeeper.znode.parent' = '%s'\n" +
                        ");",
                StrUtils.strWithBackticks( flinkTableGenerate.getFlinkTableName()),
                columns,
                flinkTableGenerate.getDatabaseName() + ":" + flinkTableGenerate.getSourceTableName(),
                SecureUtil.md5(flinkTableGenerate.getAddress()),
                flinkTableGenerate.getHbaseZnode()
        );
        return tableDdl;
    }

    @Override
    public Map<String, String> replaceConnParam(Map<String, String> options, SdpDataSource sdpDataSource) {
        for (String key : options.keySet()) {
            switch (DataSourceOption.HbaseOption.ofOption(key)) {
                case QUORUM:
                    options.put(key, sdpDataSource.getDataSourceUrl());
                    break;
                default:
                    break;
            }
        }
        return options;
    }

    @Override
    public void initTable(SourceMeta meta) {

    }



    @Override
    public String modifyOption4ChangeEnv(String ddl, SdpDataSource sdpDataSource) throws Exception {
        CommonSqlParser commonSqlParser = new CommonSqlParser(ddl);
        SqlCreateTable sqlCreateTable = commonSqlParser.getCreateTableList().get(0);
        List<SqlNode> options = sqlCreateTable.getPropertyList().getList();

        Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateTable);
        String fullTableName = sqlTableOptionMap.get(FlinkConfigKeyConstant.TABLE_NAME);
        int idx = fullTableName.indexOf(":");
        String tableName = fullTableName.substring(idx+1);

        Map<String,String> optionsMap = Maps.newHashMap();
        optionsMap.put(FlinkConfigKeyConstant.TABLE_NAME,sdpDataSource.getDatabaseName() + ":" +tableName);
        optionsMap.put(FlinkConfigKeyConstant.ZOOKEEPER_ZNODE_PARENT,sdpDataSource.getHbaseZnode());
        Map<String, SqlTableOption> replaceOptionsMap = SqlParserUtil.getTableOptionMap(optionsMap);

        int offset1 = removeOption(options, FlinkConfigKeyConstant.TABLE_NAME);
        if (-1 == offset1) {
            insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.TABLE_NAME));
        } else {
            insertOption(options, offset1, replaceOptionsMap.get(FlinkConfigKeyConstant.TABLE_NAME));
        }

        int offset2 = removeOption(options, FlinkConfigKeyConstant.ZOOKEEPER_ZNODE_PARENT);
        if (-1 == offset2) {
            insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.ZOOKEEPER_ZNODE_PARENT));
        } else {
            insertOption(options, offset2, replaceOptionsMap.get(FlinkConfigKeyConstant.ZOOKEEPER_ZNODE_PARENT));
        }

        StringBuilder modifySql = flinkSqlBuilder(ddl, options);

        return modifySql.toString();
    }

    @Override
    public List<String> getTables(ConnectInfo connectInfo) {
        return null;
    }


    /**
     * 关闭hbase连接
     *
     * @param table
     * @param admin
     * @param conn
     */
    private void close(Table table, Admin admin, Connection conn) {
        try {
            if (null != table) {
                table.close();
            }
            if (null != admin) {
                admin.close();
            }
            if (null != conn) {
                conn.close();
            }
        } catch (Exception e) {
            log.error("close hbase conn error:", e);
        }
    }

}
