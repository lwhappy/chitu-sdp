package com.chitu.bigdata.sdp.service;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.util.JdbcConstants;
import com.chitu.bigdata.sdp.api.domain.TableNameAndAlias;
import com.chitu.bigdata.sdp.constant.EnvConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


@Slf4j
public class EnvSQLParser {

    @Getter
    private String rawSql;

    @Getter
    private String beautifulRawSql;

    @Getter
    private String env;

    @Getter
    private Set<String> includeTableNames;

    @Getter
    private Set<String> excludeTableNames;

    @Getter
    private String envSql;

    @Getter
    private String message;

    @Getter
    private boolean success;

    private AtomicBoolean parsed = new AtomicBoolean(false);

    public EnvSQLParser(String env, String rawSql, Set<String> excludeTableNames) {
        this(env, rawSql,null, excludeTableNames);
    }

    public EnvSQLParser(String env, String rawSql, Set<String> includeTableNames, Set<String> excludeTableNames) {
        this.env = env;
        this.rawSql = rawSql;
        this.includeTableNames = includeTableNames == null ? new HashSet<>() : includeTableNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        this.excludeTableNames = excludeTableNames == null ? new HashSet<>() : excludeTableNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        this.beautifulRawSql = beautifySql(rawSql).trim();
    }

    /**
     * 解析sql并且添加数据隔离语句
     * @return 如果sql发生了修改, 返回true
     */
    public boolean parseAndAddEnv() {
        if(!parsed.compareAndSet(false, true)) {
            return success;
        }
        success = doParseAndAddEnv();
        return success;
    }

    public boolean needParse() {
        return beautifulRawSql.startsWith("select") || beautifulRawSql.startsWith("SELECT");
    }

    private boolean doParseAndAddEnv() {
        try {
            if(!needParse()) {
                return false;
            }
            List<SQLStatement> stmtList = SQLUtils.parseStatements(beautifulRawSql, JdbcConstants.MYSQL);
            if(stmtList.size() != 1) {
                recordMsg(String.format("expected stmtList size is 1, but got: %s", stmtList.toString()));
                return false;
            }
            SQLStatement sqlStatement = stmtList.get(0);
            if(!(sqlStatement instanceof SQLSelectStatement)) {
                recordMsg(String.format("expected SQLSelectStatement, but got: %s", sqlStatement.getClass()));
                return false;
            }
            SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
            SQLSelectQuery selectQuery = sqlSelectStatement.getSelect().getQuery();
            if(selectQuery instanceof SQLUnionQuery){
                StringBuffer resultSql = new StringBuffer();
//                String resultSql = "";
                SQLUnionOperator operator = ((SQLUnionQuery) selectQuery).getOperator();
                List<SQLSelectQuery> relations = ((SQLUnionQuery) selectQuery).getRelations();
                for(SQLSelectQuery unionSql : relations){
                    boolean result = handleNomalSelect(unionSql);
                    if(result){
                        resultSql.append(envSql).append("\r\n").append(operator).append("\r\n");
//                        resultSql += this.envSql + "\r\n" + operator + "\r\n";
                    }
                }
                //这里切割掉最后一个语句后面的UNION
                int lastUnion = resultSql.lastIndexOf(operator.toString());
                this.envSql = resultSql.substring(0,lastUnion);
            }else {
                return handleNomalSelect(selectQuery);
            }
        } catch (Throwable e) {
            log.error("为sql语句添加env的时候发生错误", e);
            recordMsg(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean handleNomalSelect(SQLSelectQuery selectQuery) {
        MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock)selectQuery;
        if(addEnvToQueryBlock(queryBlock)) {
            envSql = beautifySql(SQLUtils.toMySqlString(queryBlock));
            return true;
        } else {
            return false;
        }
    }

    private void recordMsg(String message) {
        this.message = message;
    }

    private String beautifySql(final String sql) {
        return sql.replaceAll("[\r\n]", " ").replaceAll("\\s{2,}", " ");
    }

    private boolean addEnvToQueryBlock(MySqlSelectQueryBlock queryBlock) {
        SQLTableSource ts = queryBlock.getFrom();
        Set<TableNameAndAlias> tables = retrieveAllTables(ts, false);
        tables = filterUnnecessaryTable(tables);
        //处理from语句里的子查询
        boolean handleSubQueryInFrom = handleSubQueryInFrom(ts);
        boolean handleSubQueryInWhere = false;
        boolean setNewWhereExpr = false;

        if(!tables.isEmpty()) {
            SQLExpr whereExpr = queryBlock.getWhere();
            //根据表名, 生成对应的where过滤语句
            List<SQLExpr> addedExprList = tables.stream().map(t -> {
                SQLExpr envKey;
                String columnName = EnvConstants.ENV_COLUMN_NAME;
                if (t.getTableAlias() == null) {
                    envKey = new SQLIdentifierExpr(columnName);
                } else {
                    envKey = new SQLPropertyExpr(new SQLIdentifierExpr(t.getTableAlias()), columnName);
                }
                SQLExpr value = new SQLCharExpr(env);
                SQLBinaryOpExpr envClauses = new SQLBinaryOpExpr(envKey, SQLBinaryOperator.Equality, value);
                if(!t.isAddIsNull()) {
                    return envClauses;
                } else {
                    //如果是外连接查询, 要对被加上 or a.community_id is null的判断
                    if (t.getTableAlias() == null) {
                        envKey = new SQLIdentifierExpr(columnName);
                    } else {
                        envKey = new SQLPropertyExpr(new SQLIdentifierExpr(t.getTableAlias()), columnName);
                    }
                    SQLBinaryOpExpr isNullExpr = new SQLBinaryOpExpr(envKey, SQLBinaryOperator.Is, new SQLNullExpr());
                    return new SQLBinaryOpExpr(envClauses, SQLBinaryOperator.BooleanOr, isNullExpr);
                }

            }).collect(Collectors.toList());
            handleSubQueryInWhere = handleSubQueryInWhere(whereExpr);
            SQLExpr newWhereExpr = buildNewWhereExpr(whereExpr, addedExprList);
            setNewWhereExpr = newWhereExpr != whereExpr;
            queryBlock.setWhere(newWhereExpr);
        }
        //只要有一部分sql修改过就判断整个sql已变化
        return handleSubQueryInFrom || handleSubQueryInWhere || setNewWhereExpr;
    }

    /**
     * 处理from语句的子查询
     * @param ts
     * @return
     */
    private boolean handleSubQueryInFrom(SQLTableSource ts) {
        if(ts != null) {
            if(ts instanceof SQLSubqueryTableSource) {
                SQLSubqueryTableSource subQueryTableSource = (SQLSubqueryTableSource) ts;
                MySqlSelectQueryBlock subMysqlQueryBlock = (MySqlSelectQueryBlock)subQueryTableSource.getSelect().getQuery();
                //递归处理子查询
                return addEnvToQueryBlock(subMysqlQueryBlock);
            } else if(ts instanceof SQLJoinTableSource) {
                SQLJoinTableSource tableSource = (SQLJoinTableSource)ts;
                boolean left = handleSubQueryInFrom(tableSource.getLeft());
                boolean right = handleSubQueryInFrom(tableSource.getRight());
                return left || right;
            }
        }
        return false;

    }

    /**
     * 处理where语句的子查询
     * @param sqlExpr
     * @return
     */
    private boolean handleSubQueryInWhere(SQLExpr sqlExpr) {
        if(sqlExpr != null) {
            if(sqlExpr instanceof SQLInSubQueryExpr) {
                SQLInSubQueryExpr subQueryExpr = (SQLInSubQueryExpr) sqlExpr;
                MySqlSelectQueryBlock subMysqlQueryBlock = (MySqlSelectQueryBlock)subQueryExpr.getSubQuery().getQuery();
                //递归处理子查询
                return addEnvToQueryBlock(subMysqlQueryBlock);
            } else if(sqlExpr instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) sqlExpr;
                boolean left = handleSubQueryInWhere(sqlBinaryOpExpr.getLeft());
                boolean right = handleSubQueryInWhere(sqlBinaryOpExpr.getRight());
                return left || right;
            }
        }
        return false;
    }

    /**
     * where语句里添加community_id in语句
     * @param whereExpr
     * @param addedExprList
     * @return
     */
    private SQLExpr buildNewWhereExpr(SQLExpr whereExpr, List<SQLExpr> addedExprList) {
        if(addedExprList == null || addedExprList.isEmpty()) return whereExpr;
        SQLExpr newWhereClause = null;
        if(whereExpr == null) {
            if(addedExprList.size() == 1) {
                newWhereClause = addedExprList.get(0);
            } else {
                SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr(addedExprList.get(0), SQLBinaryOperator.BooleanAnd, addedExprList.get(1));
                addedExprList.get(0).setParent(sqlBinaryOpExpr);
                addedExprList.get(1).setParent(sqlBinaryOpExpr);
                newWhereClause = buildNewWhereExpr(sqlBinaryOpExpr, addedExprList.subList(2, addedExprList.size()));
                sqlBinaryOpExpr.setParent(newWhereClause);
            }
        } else {
            SQLBinaryOpExpr sqlBinaryOpExpr = new SQLBinaryOpExpr(whereExpr, SQLBinaryOperator.BooleanAnd, addedExprList.get(0));
            addedExprList.get(0).setParent(sqlBinaryOpExpr);
            if(addedExprList.size() == 1) {
                newWhereClause = sqlBinaryOpExpr;
            } else {
                newWhereClause = buildNewWhereExpr(sqlBinaryOpExpr, addedExprList.subList(1, addedExprList.size()));
                sqlBinaryOpExpr.setParent(newWhereClause);
            }
        }
        return newWhereClause;
    }

    private Set<TableNameAndAlias> filterUnnecessaryTable(Set<TableNameAndAlias> tables) {
        return tables.stream().filter(x -> !excludeTableNames.contains(x.getTableName())).collect(Collectors.toSet());
    }

    /**
     * 从from table列表里获取所有表名
     * @param ts
     * @return
     */
    private Set<TableNameAndAlias> retrieveAllTables(SQLTableSource ts, boolean addIsNull) {
        Set<TableNameAndAlias> tables = new HashSet<>();
        if(ts instanceof SQLExprTableSource) {
            SQLExprTableSource tableSource = (SQLExprTableSource)ts;
            SQLIdentifierExpr tableExpr = (SQLIdentifierExpr) tableSource.getExpr();
            String tableName = tableExpr.getName().toLowerCase();
            String tableAlias = tableSource.getAlias();
            tables.add(new TableNameAndAlias(tableName, tableAlias, addIsNull));
        } else if(ts instanceof SQLJoinTableSource) {
            SQLJoinTableSource tableSource = (SQLJoinTableSource)ts;
            //判断是不是外连接查询
            boolean addIsNullToLeft = false;
            boolean addIsNullToRight = false;
            if(tableSource.getJoinType() != null) {
                if(tableSource.getJoinType().equals(SQLJoinTableSource.JoinType.FULL_OUTER_JOIN)) {
                    addIsNullToLeft = true;
                    addIsNullToRight = true;
                } else if(tableSource.getJoinType().equals(SQLJoinTableSource.JoinType.LEFT_OUTER_JOIN)) {
                    addIsNullToRight = true;
                } else if(tableSource.getJoinType().equals(SQLJoinTableSource.JoinType.RIGHT_OUTER_JOIN)) {
                    addIsNullToLeft = true;
                }
            }
            tables.addAll(retrieveAllTables(tableSource.getLeft(), addIsNullToLeft));
            tables.addAll(retrieveAllTables(tableSource.getRight(), addIsNull || addIsNullToRight));
        }else if(ts instanceof SQLUnionQueryTableSource){
            SQLUnionQueryTableSource tableSource = (SQLUnionQueryTableSource)ts;
            SQLUnionQuery unionQuery = tableSource.getUnion();
            List<SQLSelectQuery> relations = unionQuery.getRelations();
//            for(SQLSelectQuery relation : relations){
//                String rawSql = relation.toString();
//                this.beautifulRawSql = beautifySql(rawSql).trim();
//                doParseAndAddEnv();
//                String aa = this.envSql;
//            }
//            setNewWhereExpr = true;
//            addSelectStatementConditionUnion(tables,unionQuery);
        }
        return tables;
    }

    private void addSelectStatementConditionUnion(Set<TableNameAndAlias> tables,SQLUnionQuery sqlUnionQuery) {
        if(sqlUnionQuery.getLeft() instanceof SQLUnionQuery) {
            SQLUnionQuery temQuery = (SQLUnionQuery)sqlUnionQuery.getLeft();
            addSelectStatementConditionUnion(tables,temQuery);
        }
        if(sqlUnionQuery.getLeft() instanceof SQLSelectQueryBlock) {
            MySqlSelectQueryBlock left = (MySqlSelectQueryBlock)sqlUnionQuery.getLeft();
            tables.addAll(retrieveAllTables(left.getFrom(),false));
        }
        if(sqlUnionQuery.getRight() instanceof SQLSelectQueryBlock) {
            MySqlSelectQueryBlock right = (MySqlSelectQueryBlock) sqlUnionQuery.getRight();
            tables.addAll(retrieveAllTables(right.getFrom(),false));
        }
    }

    /**
     * 得到调试的打印信息, 在sql无需处理的时候可能返回null
     * @return
     */
    public String getLogInfo() {
        if(!parsed.get()) {
            return "Not parsed yet, invoke parseAndAddEnv() to parse and modify sql!";
        }
        if(success) {
            return String.format("Successfully add env to sql, env: %s\n Before env sql======: %s \n After env sql======: %s",
                    env, beautifulRawSql, envSql);
        } else {
            if(message != null) {
                return String.format(" Add env to sql error, message: %s\n Before env sql======: %s \n After env sql======: %s",
                        message, beautifulRawSql, envSql);
            }
        }
        return null;
    }

}
