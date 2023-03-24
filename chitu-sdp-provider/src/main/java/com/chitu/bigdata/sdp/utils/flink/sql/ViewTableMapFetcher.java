package com.chitu.bigdata.sdp.utils.flink.sql;

import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.ReUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.*;
import org.apache.flink.sql.parser.ddl.SqlCreateView;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 视图 物理表关系获取
 * @author zouchangzhen
 * @date 2022/6/14
 */
@Data
@Slf4j
public class ViewTableMapFetcher {
    /**
     * 视图 -> 表|视图|catalog.db.table
     */
    private Map<String, Set<String>> viewTableMap = Maps.newHashMap();
    /**
     * 视图集合。
     * 要保证视图的顺序，视图和视图可能有依赖（比如v1，v2:v2使用v1创建）
     */
    private List<String> viewNames = Lists.newArrayList();

    /**
     * 临时变量,遍历视图收集flink table 使用
     */
    private String viewName = "";
    public ViewTableMapFetcher(List<SqlCreateView> sqlCreateViews,List<SqlWith> sqlWithList){
        if(CollectionUtil.isNotEmpty(sqlCreateViews)){
            for (SqlCreateView view : sqlCreateViews) {

                String viewName = StrUtils.strWithoutBackticks(view.getViewName().toString());
                this.viewName = viewName;
                viewNames.add(viewName);

                SqlNode query = view.getQuery();
                if(query instanceof SqlSelect){
                    handleSqlSelect((SqlSelect)query) ;
                }else {
                    //INSERT INTO `sink_es6` VALUES ROW('1', u&'\6d4b\8bd5es6', 1, 1)
                    String simpleName = Optional.ofNullable(query).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                    log.info("不明类型来源表1：{} -> {}",simpleName,Optional.ofNullable(query).map(m -> m.toString()).orElse("null"));
                }
            }
        }

        if(CollectionUtil.isNotEmpty(sqlWithList)){
            for (SqlWith sqlWith : sqlWithList) {
                //WITH `vts_kafka_s` AS (SELECT `data`, `id` AS `x_id`, `type`, `database`, `table`, `ts`
                //FROM `vts_kafka`) (SELECT *
                //FROM `vts_kafka_s`)
                String sql = sqlWith.toString();
                String viewName = ReUtil.getGroup1("\\s*WITH\\s+(.*)\\s+AS\\s*\\(.*", sql);
                if(StrUtil.isBlank(viewName)){
                    continue;
                }
                viewName = StrUtils.strWithoutBackticks(viewName);
                this.viewName = viewName;
                viewNames.add(viewName);
                //(SqlWithItem)((SqlNodeList)sqlWith.getOperandList().get(0)).getList().get(0)
                for (SqlNode sqlNode : sqlWith.getOperandList()) {
                    if (sqlNode instanceof SqlNodeList) {
                        SqlNodeList sqlNodeList =   (SqlNodeList)sqlNode;
                        for (SqlNode node : sqlNodeList.getList()) {
                            if(node instanceof  SqlWithItem){
                                for (SqlNode sqlNode1 : ((SqlWithItem) node).getOperandList()) {
                                    if(sqlNode1 instanceof SqlSelect){
                                        handleSqlSelect(((SqlSelect) sqlNode1));
                                    }
                                }
                            }
                        }

                    }else {
                        //String simpleName = Optional.ofNullable(sqlNode).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                        // log.info("不明类型来源表2：{} -> {}",simpleName,sqlNode.toString());
                    }
                }
            }
        }

    }

    private void collectTable(String flinkTableName){
        if (StrUtil.isBlank(flinkTableName) || flinkTableName.equals(viewName)) {
           return;
        }
        Set<String> tableNames = viewTableMap.getOrDefault(viewName, new HashSet<>());
        tableNames.add(flinkTableName);
        viewTableMap.put(viewName, tableNames);
    }

    private void handleSqlSelect(SqlSelect sqlSelect) {
        if(Objects.nonNull(sqlSelect) && Objects.nonNull(sqlSelect.getFrom())){
            SqlNode from = sqlSelect.getFrom();
            if(from instanceof SqlJoin){
                fetchTableName((SqlJoin)from);
            }else if(from instanceof SqlIdentifier){
                String flinkTableName = ((SqlIdentifier) from).toString();
                collectTable(StrUtils.strWithoutBackticks(flinkTableName));
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
                            log.info("不明类型来源表3：{} -> {}",simpleName,Optional.ofNullable(sqlNode).map(m -> m.toString()).orElse("null"));
                        }
                    }
                }else {
                    //SqlIdentifier
                    String[] split = sqlBasicCall.toString().split("\\s+");
                    collectTable(StrUtils.strWithoutBackticks(split[0]));
                }
            }else {
                String simpleName = Optional.ofNullable(from).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                log.info("不明类型来源表4：{} -> {}",simpleName, Optional.ofNullable(from).map(m -> m.toString()).orElse("null"));
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
                collectTable(StrUtils.strWithoutBackticks(split[0]));
            }else if(left instanceof SqlSnapshot){
                String[] split = ((SqlSnapshot)left).toString().split("\\s+");
                collectTable(StrUtils.strWithoutBackticks(split[0]));
            }else if(left instanceof SqlIdentifier){
                String flinkTableName = ((SqlIdentifier) left).toString();
                collectTable(StrUtils.strWithoutBackticks(flinkTableName));
            }else {
                String simpleName = Optional.ofNullable(left).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                log.info("不明类型来源表5：{} -> {}",simpleName, Optional.ofNullable(left).map(m -> m.toString()).orElse("null"));
            }
        }

        SqlNode right = from.getRight();
        if(Objects.nonNull(right)){
            if(right instanceof SqlBasicCall){
                String[] split = ((SqlBasicCall)right).toString().split("\\s+");
                collectTable(StrUtils.strWithoutBackticks(split[0]));
            }else if(right instanceof SqlSnapshot){
                String[] split = ((SqlSnapshot)right).toString().split("\\s+");
                collectTable(StrUtils.strWithoutBackticks(split[0]));
            }else if(right instanceof SqlIdentifier){
                String flinkTableName = ((SqlIdentifier) right).toString();
                collectTable(StrUtils.strWithoutBackticks(flinkTableName));
            }else {
                String simpleName = Optional.ofNullable(right).map(m -> m.getClass()).map(m -> m.getSimpleName()).orElse("null");
                log.info("不明类型来源表6：{} -> {}",simpleName, Optional.ofNullable(right).map(m -> m.toString()).orElse("null"));
            }
        }
    }

    /**
     * 获取实际表名集合(非视图)
     * @return
     */
    public List<String> getFlinkTableList(String viewName, List<String> flinkTables){
        Set<String> tables = viewTableMap.get(viewName);
        if(CollectionUtil.isEmpty(tables)){
            return flinkTables.stream().distinct().collect(Collectors.toList());
        }
        for (String table : tables) {
            if (viewNames.contains(table)) {
                //递归获取物理表
                getFlinkTableList(table,flinkTables);
            }else {
                flinkTables.add(table);
            }
        }
        return flinkTables.stream().distinct().collect(Collectors.toList());
    }
}
