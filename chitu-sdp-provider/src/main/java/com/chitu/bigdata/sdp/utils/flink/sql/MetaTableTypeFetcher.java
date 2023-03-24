package com.chitu.bigdata.sdp.utils.flink.sql;

import com.chitu.bigdata.sdp.api.enums.MetaTableType;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.*;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 通过解析flink sql获取元表类型，可能会存在某种类型暂时没有到兼容的情况。可以通过排查日志后续兼容。
 * @author zouchangzhen
 * @date 2022/6/13
 */
@Data
@Slf4j
public class MetaTableTypeFetcher {

    /**
     * 表名|catalog.db.table|视图 -> 元表类型映射关系
     */
    private Map<String, MetaTableType> tableTypeMap = Maps.newHashMap();


    public MetaTableTypeFetcher(RichSqlInsert richSqlInsert){
        Preconditions.checkNotNull(richSqlInsert,"insert语句不能为空");
        SqlNode source = richSqlInsert.getSource();
        SqlSelect sqlSelect = null;
        if(source instanceof SqlSelect){
            sqlSelect =(SqlSelect)source;
        }else {
            //INSERT INTO `sink_es6` VALUES ROW('1', u&'\6d4b\8bd5es6', 1, 1)
            String simpleName = Optional.ofNullable(source).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
            log.info("不明类型来源表1：{} -> {}",simpleName,Optional.ofNullable(source).map(m -> m.toString()).orElse("null"));
        }
        //sink表
        tableTypeMap.put(StrUtils.strWithoutBackticks(richSqlInsert.getTargetTable().toString()),MetaTableType.SINK);
        handleSqlSelect(sqlSelect);
    }

    private void handleSqlSelect(SqlSelect sqlSelect) {
        if(Objects.nonNull(sqlSelect) && Objects.nonNull(sqlSelect.getFrom())){
            SqlNode from = sqlSelect.getFrom();
            if(from instanceof  SqlJoin){
                fetchTableName((SqlJoin)from);
            }else if(from instanceof SqlIdentifier){
                String flinkTableName = ((SqlIdentifier) from).toString();
                tableTypeMap.put(StrUtils.strWithoutBackticks(flinkTableName),MetaTableType.SOURCE);
            }else if(from instanceof SqlSelect){
                //递归获取
                handleSqlSelect((SqlSelect)from);
            }else if(from instanceof SqlBasicCall){
                //防止获取到别名的情况
                SqlBasicCall sqlBasicCall = (SqlBasicCall) from;
                List<SqlNode> operandList = sqlBasicCall.getOperandList();
                boolean containSelect = false;
                if(!CollectionUtils.isEmpty(operandList)){
                    for (SqlNode sqlNode : operandList) {
                        if (sqlNode instanceof SqlSelect) {
                            containSelect = true;
                            break;
                        }
                    }
                }

                if(containSelect){
                    for (SqlNode sqlNode : operandList) {
                        if(sqlNode instanceof SqlSelect){
                            //递归获取
                            handleSqlSelect((SqlSelect)sqlNode);
                        }else {
                            String simpleName = Optional.ofNullable(sqlNode).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                            log.info("不明类型来源表2：{} -> {}",simpleName,Optional.ofNullable(sqlNode).map(m -> m.toString()).orElse("null"));
                        }
                    }
                }else {
                    //SqlIdentifier
                    String[] split = sqlBasicCall.toString().split("\\s+");
                    tableTypeMap.put(StrUtils.strWithoutBackticks(split[0]),MetaTableType.SOURCE);
                }
            }else {
                String simpleName = Optional.ofNullable(from).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                log.info("不明类型来源表3：{} -> {}",simpleName, Optional.ofNullable(from).map(m -> m.toString()).orElse("null"));
            }
        }
    }

    private   void fetchTableName(SqlJoin from){
        SqlNode left = from.getLeft();
        if(Objects.nonNull(left)){
            if(left instanceof  SqlJoin){
                //递归获取
                fetchTableName((SqlJoin)left);
            }else if (left instanceof SqlBasicCall){
                SqlBasicCall mLeft = (SqlBasicCall)left;
                String[] split = mLeft.toString().split("\\s+");
                tableTypeMap.put(StrUtils.strWithoutBackticks(split[0]), MetaTableType.SOURCE);
            }else if(left instanceof SqlSnapshot){
                String[] split = ((SqlSnapshot)left).toString().split("\\s+");
                tableTypeMap.put(StrUtils.strWithoutBackticks(split[0]),MetaTableType.SOURCE);
            }else if(left instanceof SqlIdentifier){
                String flinkTableName = ((SqlIdentifier) left).toString();
                tableTypeMap.put(StrUtils.strWithoutBackticks(flinkTableName),MetaTableType.SOURCE);
            }else {
                String simpleName = Optional.ofNullable(left).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                log.info("不明类型来源表4：{} -> {}",simpleName, Optional.ofNullable(left).map(m -> m.toString()).orElse("null"));
            }
        }

        SqlNode right = from.getRight();
        if(Objects.nonNull(right)){
            if(right instanceof SqlBasicCall){
                String[] split = ((SqlBasicCall)right).toString().split("\\s+");
                tableTypeMap.put(StrUtils.strWithoutBackticks(split[0]),MetaTableType.SOURCE);
            }else if(right instanceof SqlSnapshot){
                String[] split = ((SqlSnapshot)right).toString().split("\\s+");
                tableTypeMap.put(StrUtils.strWithoutBackticks(split[0]),MetaTableType.SOURCE);
            }else if(right instanceof SqlIdentifier){
                String flinkTableName = ((SqlIdentifier) right).toString();
                tableTypeMap.put(StrUtils.strWithoutBackticks(flinkTableName),MetaTableType.SOURCE);
            }else {
                String simpleName = Optional.ofNullable(right).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                log.info("不明类型来源表5：{} -> {}",simpleName,Optional.ofNullable(right).map(m -> m.toString()).orElse("null"));
            }
        }
    }
}
