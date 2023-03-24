package com.chitu.bigdata.sdp.utils;

import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.service.validate.util.SqlUtil;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWith;
import org.apache.flink.sql.parser.ddl.SqlCreateCatalog;
import org.apache.flink.sql.parser.ddl.SqlCreateFunction;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlCreateView;
import org.apache.flink.sql.parser.dml.RichSqlInsert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zouchangzhen
 * @date 2022/5/17
 */
@Data
public class CommonSqlParser {
    private String sql;
    private List<SqlNode> sqlNodes ;
    private List<SqlCreateFunction> createFunctionList = Lists.newArrayList();
    private List<SqlCreateTable> createTableList = Lists.newArrayList();
    private List<SqlCreateView> createViewList = Lists.newArrayList();
    private List<SqlCreateCatalog> createCatalogList = Lists.newArrayList();
    private List<RichSqlInsert>  insertSqlList = Lists.newArrayList();
    /**
     * 临时视图
     * WITH orders_with_total AS (
     *     SELECT order_id, price + tax AS total
     *     FROM Orders
     * )
     * SELECT order_id, SUM(total)
     * FROM orders_with_total
     * GROUP BY order_id;
     */
    private List<SqlWith> sqlWithList = Lists.newArrayList();

    public CommonSqlParser(String sql) throws Exception {
        this.sql = sql;
        if(StrUtil.isNotBlank(sql)){
            sql = SqlUtil.removeNote(sql);
            sql = SqlUtil.clearNotes(sql);
            if(StrUtil.isNotBlank(sql)) {
                sqlNodes = SqlParserUtil.getSqlNodes(sql);
                for (SqlNode sqlNode : sqlNodes) {
                    if (sqlNode instanceof SqlCreateFunction) {
                        createFunctionList.add((SqlCreateFunction) sqlNode);
                    } else if (sqlNode instanceof SqlCreateTable) {
                        createTableList.add((SqlCreateTable) sqlNode);
                    } else if (sqlNode instanceof SqlCreateView) {
                        createViewList.add((SqlCreateView) sqlNode);
                    } else if (sqlNode instanceof SqlCreateCatalog) {
                        createCatalogList.add((SqlCreateCatalog) sqlNode);
                    }else if(sqlNode instanceof RichSqlInsert){
                        insertSqlList.add((RichSqlInsert) sqlNode);
                    }
                }
            }
        }
    }

    /**
     * 获取元表信息
     * @return
     */
   public Map<String,Object> getMetaTable(){
       Map<String, Object> tables = new HashMap<>();
       for (SqlCreateTable sqlCreateTable : createTableList) {
           tables.put(StrUtils.strWithoutBackticks(sqlCreateTable.getTableName().toString()), sqlCreateTable);
       }

       for (SqlCreateCatalog sqlCreateCatalog : createCatalogList) {
           tables.put(StrUtils.strWithoutBackticks(sqlCreateCatalog.getCatalogName().toString()), sqlCreateCatalog);
       }
       return tables;
    }

    /**
     * 获取视图信息
     * @return
     */
    public Map<String,Object> getViews(){
        Map<String, Object> tables = new HashMap<>();
        for (SqlCreateView sqlCreateView : createViewList) {
            tables.put(StrUtils.strWithoutBackticks(sqlCreateView.getViewName().toString()), sqlCreateView);
        }
        return tables;
    }
}
