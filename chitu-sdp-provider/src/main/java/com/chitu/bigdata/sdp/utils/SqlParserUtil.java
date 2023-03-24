package com.chitu.bigdata.sdp.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.enums.DataSourceType;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.sql.parser.ddl.SqlCreateCatalog;
import org.apache.flink.sql.parser.ddl.SqlCreateFunction;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.apache.flink.sql.parser.impl.FlinkSqlParserImpl;
import org.apache.flink.sql.parser.validate.FlinkSqlConformance;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sutao
 * @create 2022-03-16 11:19
 */
public class SqlParserUtil {


    public static List<SqlNode> getSqlNodes(String sql) throws SqlParseException {
        SqlParser parser = SqlParser.create(sql, SqlParser.configBuilder()
                .setParserFactory(FlinkSqlParserImpl.FACTORY)
                .setQuoting(Quoting.BACK_TICK)
                .setUnquotedCasing(Casing.TO_LOWER)
                .setQuotedCasing(Casing.UNCHANGED)
                .setConformance(FlinkSqlConformance.DEFAULT)
                .build()
        );
        return parser.parseStmtList().getList();
    }

    /**
     * sql是否是函数
     * @param sql
     * @return
     * @throws SqlParseException
     */
    public static boolean isSqlCreateFunction(String sql) throws SqlParseException{
        boolean isFunction = false;
        if(StrUtil.isBlank(sql)){
            return isFunction;
        }
        List<SqlNode> sqlNodes = getSqlNodes(sql);
        for (SqlNode sqlNode : sqlNodes) {
            if(sqlNode instanceof SqlCreateFunction){
                isFunction = true;
                break;
            }
        }
        return isFunction;
    }

    /**
     * 获取建表DDL
     * @param sql
     * @return
     * @throws SqlParseException
     */
    public static List<SqlCreateTable> getSqlCreateTable(String sql) throws SqlParseException{
        List<SqlCreateTable> sqlCreateTables = Lists.newArrayList();
        if(StrUtil.isBlank(sql)){
            return sqlCreateTables;
        }
        List<SqlNode> sqlNodes = getSqlNodes(sql);
        for (SqlNode sqlNode : sqlNodes) {
            if(Objects.isNull(sqlNode) || !(sqlNode instanceof SqlCreateTable)){
                continue;
            }
            sqlCreateTables.add((SqlCreateTable)sqlNode);
        }
        return sqlCreateTables;
    }

    /**
     * 获取建表选项 str-> str
     * @param sqlCreateTable
     * @return
     */
    public static Map<String,String> getSqlTableOptionMap(SqlCreateTable sqlCreateTable){
        if(Objects.isNull(sqlCreateTable)){
            return Collections.emptyMap();
        }
        Map<String,String> optionMap = Maps.newHashMap();
        List<SqlNode> list = sqlCreateTable.getPropertyList().getList();
        for (SqlNode option : list) {
            if(Objects.nonNull(option) && option instanceof SqlTableOption){
                SqlTableOption sqlTableOption =   (SqlTableOption) option;
                optionMap.put(sqlTableOption.getKeyString(),sqlTableOption.getValueString());
            }
        }
        return optionMap;
    }

    /**
     * 获取建表选项 str-> str
     * @param sqlCreateCatalog
     * @return
     */
    public static Map<String,String> getSqlTableOptionMap(SqlCreateCatalog sqlCreateCatalog){
        if(Objects.isNull(sqlCreateCatalog)){
            return Collections.emptyMap();
        }
        Map<String,String> optionMap = Maps.newHashMap();
        List<SqlNode> list = sqlCreateCatalog.getPropertyList().getList();
        for (SqlNode option : list) {
            if(Objects.nonNull(option) && option instanceof SqlTableOption){
                SqlTableOption sqlTableOption =   (SqlTableOption) option;
                optionMap.put(sqlTableOption.getKeyString(),sqlTableOption.getValueString());
            }
        }
        return optionMap;
    }

    /**
     * 由于SqlTableOption不好构建，所以通过这种方法获取SqlTableOption
     * 获取建表选项 str-> SqlTableOption
     * @param startupMode
     * @param timestamp
     * @return
     * @throws Exception
     */
    public static Map<String,SqlTableOption>  getTableOptionMap(String startupMode,String timestamp,String offsets) throws Exception{
        Preconditions.checkArgument(StrUtil.isNotBlank(startupMode),"startupMode不能为空");
        Map<String,SqlTableOption> sqlTableOptionMap = Maps.newHashMap();
        String sql ="CREATE TABLE KafkaTable (\n" +
                "  `user_id` BIGINT\n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                "  'topic' = 'user_behavior',\n" +
                "  'properties.bootstrap.servers' = 'localhost:9092',\n" +
                "  'properties.group.id' = 'testGroup',\n" +
                "  'scan.startup.mode' = '%s',\n" +
                "  'scan.startup.timestamp-millis' = '%s',\n" +
                "  'scan.startup.specific-offsets' = '%s',\n" +
                "  'format' = 'csv'\n" +
                ")";
        List<SqlNode> sqlCreateTables = getSqlNodes(String.format(sql,startupMode,StrUtil.isNotBlank(timestamp)?timestamp:"",StrUtil.isNotBlank(offsets)?offsets:""));
        List<SqlNode> list = ((SqlCreateTable)sqlCreateTables.get(0)).getPropertyList().getList();
        for (SqlNode option : list) {
            if(Objects.nonNull(option) && option instanceof SqlTableOption){
                SqlTableOption sqlTableOption =   (SqlTableOption) option;
                sqlTableOptionMap.put(sqlTableOption.getKeyString(),sqlTableOption);
            }
        }
        return sqlTableOptionMap;
    }

    /**
     * sql是否是建表语句
     * @param sql
     * @return
     * @throws SqlParseException
     */
    public static boolean isSqlCreateTable(String sql) throws SqlParseException{
        boolean isFunction = false;
        if(StrUtil.isBlank(sql)){
            return isFunction;
        }
        List<SqlNode> sqlNodes = getSqlNodes(sql);
        for (SqlNode sqlNode : sqlNodes) {
            if(sqlNode instanceof SqlCreateTable){
                isFunction = true;
                break;
            }
        }
        return isFunction;
    }

    /**
     * sql是否是insert into 语句
     * @param sql
     * @return
     * @throws SqlParseException
     */
    public static boolean isSqlInsert(String sql) throws SqlParseException{
        boolean isFunction = false;
        if(StrUtil.isBlank(sql)){
            return isFunction;
        }
        List<SqlNode> sqlNodes = getSqlNodes(sql);
        for (SqlNode sqlNode : sqlNodes) {
            if(sqlNode instanceof RichSqlInsert){
                isFunction = true;
                break;
            }
        }
        return isFunction;
    }

    /**
     * 表名 -> 建表语句
     * @param sqlCreateTableList
     * @return
     */
    public static Map<String,SqlCreateTable> getSqlCreateTableMap(List<SqlCreateTable> sqlCreateTableList){
        if(CollectionUtils.isEmpty(sqlCreateTableList)){
            return Collections.emptyMap();
        }

        return sqlCreateTableList.stream().collect(Collectors.toMap(k -> k.getTableName().toString(), m -> m, (k1, k2) -> k2));
    }

    /**
     * 建表语句是否是kafka并且是source表判断
     * @param sqlCreateTable
     * @return
     */
    public static boolean isSourceKafka(SqlCreateTable sqlCreateTable){
        Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateTable);
        String dataSourceType = sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.CONNECTOR, "");
        String metaTableType = sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.STARTUP_MODE, "");
        //1.判断是kafka，2.属性scan.startup.mode存在则代表是source表
        return DataSourceType.KAFKA.getType().equalsIgnoreCase(dataSourceType) && com.xiaoleilu.hutool.util.StrUtil.isNotBlank(metaTableType);
    }

    /**
     * 建表语句是否是kafka并且是source表并且格式是canal-json的
     * @param sqlCreateTable
     * @return
     */
    public static boolean isSourceKafkaAndCanal(SqlCreateTable sqlCreateTable){
        boolean sourceKafka = isSourceKafka(sqlCreateTable);
        //1.判断是kafka，2.属性scan.startup.mode存在则代表是source表,3 并且格式是canal-json的
        return sourceKafka &&  "canal-json".equalsIgnoreCase(SqlParserUtil.getSqlTableOptionMap(sqlCreateTable).getOrDefault(FlinkConfigKeyConstant.FORMAT, ""));
    }

    /**
     * 判断是哪个数据源类型
     * @param sqlCreateTable 建表语句
     * @param expectDataSourceType 期待的数据源类型
     * @return
     */
    public static boolean isExpectDataSourceType(SqlCreateTable sqlCreateTable,String expectDataSourceType){
        if(StrUtil.isBlank(expectDataSourceType)){
            return false;
        }
        Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateTable);
        String dataSourceType = sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.CONNECTOR, "");
        //判断是哪个数据源的类型
        return dataSourceType.contains(expectDataSourceType);
    }

    /**
     * 获取flink tableName
     * @param catalogDbTablename
     * @return
     */
    public static String  getFlinkTableName(String catalogDbTablename){
        if(com.xiaoleilu.hutool.util.StrUtil.isBlank(catalogDbTablename)){
            return  "";
        }
        String[] split = catalogDbTablename.split("\\.");
        if(3 == split.length){
            //catalog.db.table
            return  split[0];
        }else if(1 == split.length){
            //table
            return  split[0];
        }else {
            return null;
        }

    }

    /**
     * 获取数据库名称
     * @param catalogDbTablename
     * @return
     */
    public static  String getDbName(String catalogDbTablename){
        if(com.xiaoleilu.hutool.util.StrUtil.isBlank(catalogDbTablename)){
            return  "";
        }
        String[] split = catalogDbTablename.split("\\.");
        if(3 == split.length){
            //catalog.db.table
            return  split[1];
        }else {
            return null;
        }

    }

    /**
     * 获取物理表名称
     * @param catalogDbTablename
     * @return
     */
    public static  String getTableName(String catalogDbTablename){
        if(com.xiaoleilu.hutool.util.StrUtil.isBlank(catalogDbTablename)){
            return  "";
        }
        String[] split = catalogDbTablename.split("\\.");
        if(3 == split.length){
            //catalog.db.table
            return  split[2];
        }else {
            return null;
        }
    }

    /**
     * 是否是 catalog.db.table格式
     * @param catalogDbTablename
     * @return
     */
    public static  boolean isCatalogDbTable(String catalogDbTablename){
        if(com.xiaoleilu.hutool.util.StrUtil.isBlank(catalogDbTablename)){
            return  false;
        }
        String[] split = catalogDbTablename.split("\\.");
        return 3 == split.length;
    }

    /**
     * 由于SqlTableOption不好构建，所以通过这种方法获取SqlTableOption
     * 获取建表选项 str-> SqlTableOption
     * @return
     * @throws Exception
     */
    public static Map<String,SqlTableOption>  getTableOptionMap(Map<String,String> optionMap) throws Exception{
        if(MapUtil.isEmpty(optionMap)){
          return   Collections.emptyMap();
        }

        StringBuilder optionBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : optionMap.entrySet()) {
            optionBuilder.append("'").append(entry.getKey()).append("' = '").append(entry.getValue()).append("',\n");
        }

        String sql ="CREATE TABLE KafkaTable (\n" +
                "  `user_id` BIGINT\n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                 optionBuilder.toString() +
                "  'format' = 'csv'\n" +
                ")";

        List<SqlNode> sqlCreateTables = getSqlNodes(sql);

        Map<String, SqlTableOption> tableOptionMap = getTableOptionMap((SqlCreateTable) sqlCreateTables.get(0));

        return tableOptionMap;
    }

    public static  Map<String,SqlTableOption>  getTableOptionMap(SqlCreateTable sqlCreateTable) {
        if(Objects.isNull(sqlCreateTable)){
            return Collections.emptyMap();
        }
        List<SqlNode> sqlNodes = sqlCreateTable.getPropertyList().getList();
        Map<String,SqlTableOption> sqlTableOptionMap = Maps.newHashMap();
        for (SqlNode option : sqlNodes) {
            if(Objects.nonNull(option) && option instanceof SqlTableOption){
                SqlTableOption sqlTableOption =   (SqlTableOption) option;
                sqlTableOptionMap.put(sqlTableOption.getKeyString(),sqlTableOption);
            }
        }
        return  sqlTableOptionMap;
    }


    /**
     * 删除选项，返回下标位置
     *
     * @param options
     * @param optionKey
     * @return
     */
    public static int removeOption(List<SqlNode> options, String optionKey) {
        int location = -1;
        if (CollectionUtils.isEmpty(options)) {
            return location;
        }
        int index = -1;
        final Iterator<SqlNode> each = options.iterator();
        while (each.hasNext()) {
            ++index;
            SqlTableOption next = (SqlTableOption) each.next();
            if (Objects.nonNull(next) && next.getKeyString().equals(optionKey)) {
                each.remove();
                location = index;
            }
        }
        return location;
    }

    /**
     * 按照下标写回去
     *
     * @param options
     * @param location
     * @param sqlTableOption
     * @return
     */
    public static  List<SqlNode> insertOption(List<SqlNode> options, int location, SqlTableOption sqlTableOption) {
        if (CollectionUtils.isEmpty(options)) {
            return options;
        }
        if (location >= 0) {
            options.add(location, sqlTableOption);
        }
        return options;
    }

    /**
     * 避免去掉字段的注释,options中注释就没有办法保留了
     * @param ddl
     * @param options
     * @return
     */
    public static StringBuilder flinkSqlBuilder(String ddl, List<SqlNode> options) {
        String keyword = ReUtil.getGroup1(".*(\\s*WITH\\s*\\().*", ddl);
        int index = ddl.lastIndexOf(keyword);
        String prefix = ddl.substring(0, index + keyword.length());
        StringBuilder modifySql = new StringBuilder(prefix);

        modifySql.append(StrUtils.LF);
        String kv = "'%s' = '%s'";
        for (int j = 0, xlen = options.size(); j < xlen; j++) {
            SqlTableOption sqlTableOption = (SqlTableOption) options.get(j);
            if (Objects.isNull(sqlTableOption)) {
                continue;
            }
            if (j == xlen - 1) {
                modifySql.append(String.format(kv, sqlTableOption.getKeyString(), sqlTableOption.getValueString())).append(StrUtils.LF);
            } else {
                modifySql.append(String.format(kv, sqlTableOption.getKeyString(), sqlTableOption.getValueString())).append(StrUtils.DLF);
            }
        }
        modifySql.append(");");
        return modifySql;
    }

    public static void main(String[] args) throws Exception {
        Map<String,String> x = new HashMap<>();
        x.put("x","d");
        Map<String, SqlTableOption> tableOptionMap = getTableOptionMap(x);
        SqlTableOption sqlTableOption = tableOptionMap.get("x");
    }

}
