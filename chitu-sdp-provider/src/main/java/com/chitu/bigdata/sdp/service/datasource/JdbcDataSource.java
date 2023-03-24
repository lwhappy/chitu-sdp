package com.chitu.bigdata.sdp.service.datasource;

import cn.hutool.crypto.SecureUtil;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;
import com.chitu.bigdata.sdp.api.domain.MetadataTableColumn;
import com.chitu.bigdata.sdp.api.domain.SourceMeta;
import com.chitu.bigdata.sdp.api.enums.ConnectorReviewConf;
import com.chitu.bigdata.sdp.api.enums.DataSourceOption;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.constant.FlinkDataTypeMapping;
import com.chitu.bigdata.sdp.service.DataSourceService;
import com.chitu.bigdata.sdp.utils.CommonSqlParser;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.exception.ApplicationException;
import com.google.common.collect.Maps;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sutao
 * @create 2021-12-09 17:21
 */
@Component("mysql")
@Slf4j
public class JdbcDataSource extends AbstractDataSource<Connection> {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("无法加载驱动类", e);
        }
    }

    @Autowired
    private DataSourceService dataSourceService;

    private final String TABLE_EXISTS_SQL = "SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' AND TABLE_SCHEMA = ? AND TABLE_NAME = ?";

    private final String TABLE_SQL = "SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = ?";

    private final String FIELD_SQL = "SELECT COLUMN_NAME,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,COLUMN_KEY,COLUMN_COMMENT,IS_NULLABLE,NUMERIC_PRECISION,NUMERIC_SCALE FROM INFORMATION_SCHEMA.COLUMNS T WHERE TABLE_NAME = ? AND TABLE_SCHEMA = ?";

    private final String PRI = "PRI";

    private final String JDBC_FORMAT = "jdbc:mysql://%s?connectTimeout=60000";

    protected final String JDBC_FORM = "jdbc:mysql://%s";

    private final String URL_TIMEOUT = "connectTimeout=";

    private final String URL_TIME_CON = "jdbc:mysql://%s&connectTimeout=60000";

    private static final String URL_COMBINE = "?";
    @Override
    public Connection getConnection(ConnectInfo connectInfo) throws Exception {
        if (!connectInfo.getAddress().contains(URL_COMBINE)){
            connectInfo.setAddress(String.format(JDBC_FORMAT,connectInfo.getAddress()));
        }else if(!connectInfo.getAddress().contains(URL_TIMEOUT)){
            connectInfo.setAddress(String.format(URL_TIME_CON,connectInfo.getAddress()));
        }else {
            connectInfo.setAddress(String.format(JDBC_FORM,connectInfo.getAddress()));
        }
        return DriverManager.getConnection(connectInfo.getAddress(), connectInfo.getUsername(), connectInfo.getPwd());
    }

    @Override
    public void closeConnection(Connection connection) {
        close(null, null, connection);
    }

    @Override
    public boolean tableExists(ConnectInfo connectInfo, String tableName) throws Exception {
        boolean exists = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = getConnection(connectInfo);
            ps = conn.prepareStatement(TABLE_EXISTS_SQL);
            ps.setString(1, connectInfo.getDatabaseName());
            ps.setString(2, tableName);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt(1) >= 1) {
                    exists = true;
                }
            }
        } finally {
            close(rs, ps, conn);
        }
        return exists;
    }

    @Override
    public List<String> getTables(ConnectInfo connectInfo) throws Exception {
        List<String> tables = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = getConnection(connectInfo);
            ps = conn.prepareStatement(TABLE_SQL);
            ps.setString(1, connectInfo.getDatabaseName());
            rs = ps.executeQuery();
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        } finally {
            close(rs, ps, conn);
        }
        return tables;
    }

    @Override
    public List<MetadataTableColumn> getTableColumns(ConnectInfo connectInfo, String tableName) throws Exception {
        Connection conn = getConnection(connectInfo);
        List<MetadataTableColumn> metadataTableColumns = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(FIELD_SQL);
            ps.setString(1, tableName);
            ps.setString(2, connectInfo.getDatabaseName());
            rs = ps.executeQuery();
            MetadataTableColumn metadataTableColumn = null;
            while (rs.next()) {
                metadataTableColumn = new MetadataTableColumn();
                metadataTableColumn.setColumnName(rs.getString(1));
                metadataTableColumn.setColumnType(rs.getString(2));
                String primary = rs.getString(4);
                if (StringUtils.isNotEmpty(primary) && primary.equals(PRI)) {
                    metadataTableColumn.setPrimaryKey(true);
                }
                metadataTableColumns.add(metadataTableColumn);
            }
        } finally {
            close(rs, ps, conn);
        }
        return metadataTableColumns;
    }

    @Override
    public String generateDdl(FlinkTableGenerate flinkTableGenerate) {
        //转换成flink字段类型
        flinkTableGenerate.getMetadataTableColumnList().forEach(item -> {
            FlinkDataTypeMapping.MYSQL_TYPE_MAP.forEach((k, v) -> {
                if (StrUtil.equalsIgnoreCase(item.getColumnType(), k)) {
                    item.setColumnType(v);
                    return;
                }
            });
            //如果没匹配到，设置默认值
            if (!FlinkDataTypeMapping.MYSQL_TYPE_MAP.containsValue(item.getColumnType())) {
                item.setColumnType(FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE);
            }
        });
        StringBuilder sb = new StringBuilder();
        StringBuilder sbPrimaryKey = new StringBuilder();
        flinkTableGenerate.getMetadataTableColumnList().forEach(item -> {
            if (item.isPrimaryKey()) {
                sbPrimaryKey.append(StrUtils.strWithBackticks(item.getColumnName())).append(",");
            }
            sb.append("  ").append(StrUtils.strWithBackticks(item.getColumnName())).append(" ").append(item.getColumnType()).append(",").append("\n");
        });
        String tableDdl = String.format(
                "CREATE TABLE %s (\n" +
                        "%s" +
                        "  PRIMARY KEY (%s) NOT ENFORCED -- 主键字段\n" +
                        ") WITH (\n" +
                        "  'connector' = 'jdbc',\n" +
                        "  'url' = '%s',\n" +
                        "  'username' = '%s',\n" +
                        "  'password' = '%s',\n" +
                        "  'table-name' = '%s'\n" +
                        ");",
                StrUtils.strWithBackticks(flinkTableGenerate.getFlinkTableName()),
                sb.toString(),
                StrUtil.removeSuffix(sbPrimaryKey.toString(), ","),
                SecureUtil.md5(flinkTableGenerate.getAddress() + "/" + flinkTableGenerate.getDatabaseName()),
                flinkTableGenerate.getUserName(),
                SecureUtil.md5(flinkTableGenerate.getPwd()),
                flinkTableGenerate.getSourceTableName()
        );
        return tableDdl;
    }

    @Override
    public Map<String, String> replaceConnParam(Map<String, String> options, SdpDataSource sdpDataSource) {
        for (String key : options.keySet()) {
            switch (DataSourceOption.JdbcOption.ofOption(key)) {
                case URL:
                    options.put(key, String.format(JDBC_FORM,sdpDataSource.getDataSourceUrl()));
                    break;
                case USER_NAME:
                    options.put(key, sdpDataSource.getUserName());
                    break;
                case PWD:
                    options.put(key, sdpDataSource.getPassword());
                    break;
                default:
                    break;
            }
        }
        return options;
    }


    /**
     * 关闭数据库连接
     *
     * @param rs   结果集
     * @param ps   执行器
     * @param conn 连接
     */
    private void close(ResultSet rs, Statement ps, Connection conn) {
        try {
            if (null != rs) {
                rs.close();
            }
            if (null != ps) {
                ps.close();
            }
            if (null != conn) {
                conn.close();
            }
        } catch (Exception e) {
            log.error("close jdbc conn error:", e);
        }
    }

    @Override
    public void initTable(SourceMeta meta) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ConnectInfo connectInfo = getConnectInfo(meta.getDataSourceId());
        try {
            connection =  getConnection(connectInfo);
            for (String table : meta.getTables()) {
                String sql = String.format(ConnectorReviewConf.TRUNCATE_TABLE_SQL.getType(), table);
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.execute();
            }
        } catch (Exception e) {
            log.error("===清空mysql表异常:{}",e);
            throw new ApplicationException(ResponseCode.ERROR,"===清空mysql表异常");
        } finally {
            close(null,preparedStatement,connection);
        }
    }


    private ConnectInfo getConnectInfo(Long dataSourceId) {
        SdpDataSource dataSource = dataSourceService.getByIdWithPwdPlaintext(dataSourceId);
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setAddress(dataSource.getDataSourceUrl());
        connectInfo.setDatabaseName(dataSource.getDatabaseName());
        connectInfo.setUsername(dataSource.getUserName());
        connectInfo.setPwd(dataSource.getPassword());
        return connectInfo;
    }


    @Override
    public String modifyOption4ChangeEnv(String ddl, SdpDataSource sdpDataSource) throws Exception {
        CommonSqlParser commonSqlParser = new CommonSqlParser(ddl);
        SqlCreateTable sqlCreateTable = commonSqlParser.getCreateTableList().get(0);
        List<SqlNode> options = sqlCreateTable.getPropertyList().getList();

        Map<String,String> optionsMap = Maps.newHashMap();
        optionsMap.put(FlinkConfigKeyConstant.USERNAME,sdpDataSource.getUserName());
        Map<String, SqlTableOption> replaceOptionsMap = SqlParserUtil.getTableOptionMap(optionsMap);

        int offset1 = removeOption(options, FlinkConfigKeyConstant.USERNAME);
        if (-1 == offset1) {
            insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.USERNAME));
        } else {
            insertOption(options, offset1, replaceOptionsMap.get(FlinkConfigKeyConstant.USERNAME));
        }

        StringBuilder modifySql = flinkSqlBuilder(ddl, options);

        return modifySql.toString();
    }
}
