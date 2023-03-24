package com.chitu.bigdata.sdp.interceptor;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.builder.SQLBuilderFactory;
import com.alibaba.druid.sql.builder.SQLDeleteBuilder;
import com.alibaba.druid.sql.builder.SQLUpdateBuilder;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.chitu.bigdata.sdp.constant.EnvConstants;
import com.chitu.bigdata.sdp.service.EnvSQLParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.*;

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MybatisInterceptor.class);

    private static final String DELEGATE = "delegate.";
    private static final String MAPPED_STATEMENT = "mappedStatement";
    private static final String CONFIGURATION = "configuration";
    private static final String ROW_BOUNDS = "rowBounds";
    private static final String BOUND_SQL = "boundSql";
    private static final String BOUND_SQL_SQL = "boundSql.sql";
    private static final String BOUND_ADDITIONAL_PARAMETERS = "boundSql.additionalParameters";
    private static final String ROW_BOUNDS_LIMIT = "rowBounds.limit";
    private static final String ROW_BOUNDS_OFFSET = "rowBounds.offset";
    private static final Map<String, String> MAP = new HashMap<>();

    private Set<String> excludeTables = new HashSet<>();

    static {
        MAP.put(MAPPED_STATEMENT, DELEGATE + MAPPED_STATEMENT);
        MAP.put(CONFIGURATION, DELEGATE + CONFIGURATION);
        MAP.put(ROW_BOUNDS, DELEGATE + ROW_BOUNDS);
        MAP.put(BOUND_SQL, DELEGATE + BOUND_SQL);
        MAP.put(BOUND_SQL_SQL, DELEGATE + BOUND_SQL_SQL);
        MAP.put(ROW_BOUNDS_LIMIT, DELEGATE + ROW_BOUNDS_LIMIT);
        MAP.put(ROW_BOUNDS_OFFSET, DELEGATE + ROW_BOUNDS_OFFSET);
        MAP.put(BOUND_ADDITIONAL_PARAMETERS, DELEGATE + BOUND_ADDITIONAL_PARAMETERS);
    }

    private Environment environment;

    public MybatisInterceptor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object intercept(final Invocation invocation) throws Throwable {
        try {
            String env = EnvHolder.getEnv();
            if (StringUtils.isEmpty(env)) {
               //env = "prod";
                return invocation.proceed();
            }

            StatementHandler statementHandler = getStatementHandler(invocation);
            final boolean delegate = isDelegate(statementHandler);
            final MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            final BoundSql boundSql = getBoundSql(delegate, metaObject);

            String beautifySql = beautifySql(boundSql.getSql());
            if(beautifySql.toLowerCase(Locale.ROOT).startsWith("select")){
                EnvSQLParser sqlParser = new EnvSQLParser(env, boundSql.getSql(), excludeTables);
                if (!sqlParser.needParse()) {
                    return invocation.proceed();
                }

                boolean success = sqlParser.parseAndAddEnv();
                if (logger.isInfoEnabled()) {
                    String logInfo = sqlParser.getLogInfo();
                    if (StringUtils.isNotBlank(logInfo)) {
                        //logger.info("Env sql parse result: {}", logInfo);
                    }
                }
                if (!success) {
                    return invocation.proceed();
                }
                if (logger.isTraceEnabled()) {
                    logger.info("加上环境变量之后的sql: {}", sqlParser.getEnvSql());
                }
                setValue(metaObject, delegate, BOUND_SQL_SQL, sqlParser.getEnvSql());
            } else {
                //1.给实例中的env赋值
                this.appendEnv4Param(env, boundSql);
                //2.sql调整
                List<SQLStatement> stmtList = SQLUtils.parseStatements(beautifySql, JdbcConstants.MYSQL);
                if (CollectionUtils.isNotEmpty(stmtList)) {
                    StringBuilder sqlBuilder = new StringBuilder();
                    for (SQLStatement sqlStatement : stmtList) {
                        if (sqlStatement instanceof SQLInsertStatement) {
                            MySqlInsertStatement sqlInsertStatement = (MySqlInsertStatement) sqlStatement;
                            SQLExprTableSource tableSource = (SQLExprTableSource) sqlInsertStatement.getTableSource();
                            SQLIdentifierExpr tableExpr = (SQLIdentifierExpr) tableSource.getExpr();
                            if (excludeTables.contains(tableExpr.getName())) {
                                //忽略处理的表
                                continue;
                            }

                            boolean isExistSetEnv = false;
                            for (int i = 0, len = sqlInsertStatement.getColumns().size(); i < len; i++) {
                                SQLExpr sqlExpr = sqlInsertStatement.getColumns().get(i);

                                SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) sqlExpr;
                                if (EnvConstants.ENV_COLUMN_NAME.equals(sqlIdentifierExpr.getName())) {
                                    isExistSetEnv = true;
                                    break;
                                }
                            }

                            if (!isExistSetEnv) {
                                //sql中不存在env字段，往后面追加,sql insert into 存在env字段,不需要调整，上面appendEnv4Param已经根据环境变量赋值
                                SQLIdentifierExpr sqlIdentifierExpr = new SQLIdentifierExpr(EnvConstants.ENV_COLUMN_NAME);
                                sqlInsertStatement.addColumn(sqlIdentifierExpr);

                                SQLExpr value = new SQLCharExpr(env);
                                List<SQLInsertStatement.ValuesClause> valuesList = sqlInsertStatement.getValuesList();
                                for (SQLInsertStatement.ValuesClause valuesClause : valuesList) {
                                    valuesClause.getValues().add(value);
                                }

                                //处理ON DUPLICATE KEY UPDATE,
                                List<SQLExpr> duplicateKeyUpdate = sqlInsertStatement.getDuplicateKeyUpdate();
                                if (CollectionUtils.isNotEmpty(duplicateKeyUpdate)) {
                                    SQLMethodInvokeExpr sqlMethodInvokeExpr = new SQLMethodInvokeExpr("VALUES", null, sqlIdentifierExpr);
                                    SQLBinaryOpExpr envSqlBinary = new SQLBinaryOpExpr(sqlIdentifierExpr, SQLBinaryOperator.Equality, sqlMethodInvokeExpr);
                                    duplicateKeyUpdate.add(envSqlBinary);
                                }
                            }
                            sqlBuilder.append(sqlInsertStatement.toString());
                        } else if (sqlStatement instanceof SQLUpdateStatement) {
                            MySqlUpdateStatement mySqlUpdateStatement = (MySqlUpdateStatement) sqlStatement;
                            SQLExprTableSource tableSource = (SQLExprTableSource) mySqlUpdateStatement.getTableSource();
                            SQLIdentifierExpr tableExpr = (SQLIdentifierExpr) tableSource.getExpr();
                            if (excludeTables.contains(tableExpr.getName())) {
                                //忽略处理的表
                                continue;
                            }

                            SQLUpdateBuilder sqlUpdateBuilder = SQLBuilderFactory.createUpdateBuilder(sqlStatement.toString(), DbType.mysql);
                            String alias = tableSource.getAlias();
                            List<SQLUpdateSetItem> items = mySqlUpdateStatement.getItems();

                            boolean isExistSetEnv = false;
                            for (SQLUpdateSetItem item : items) {
                                if (item.getColumn() instanceof SQLPropertyExpr) {
                                    SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) item.getColumn();
                                    if (EnvConstants.ENV_COLUMN_NAME.equalsIgnoreCase(sqlPropertyExpr.getName())) {
                                        isExistSetEnv = true;
                                        break;
                                    }
                                } else {
                                    SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) item.getColumn();
                                    if (EnvConstants.ENV_COLUMN_NAME.equalsIgnoreCase(sqlIdentifierExpr.getName())) {
                                        isExistSetEnv = true;
                                        break;
                                    }
                                }

                            }

                            String envExpr = StrUtil.isBlank(alias) ? EnvConstants.ENV_COLUMN_NAME + "='" + env + "'" : alias + "." + EnvConstants.ENV_COLUMN_NAME + "='" + env + "'";
                            if (!isExistSetEnv) {
                                //set加上环境参数设置
                                sqlUpdateBuilder.set(envExpr);
                            }

                            //where加上环境条件
                            sqlUpdateBuilder.whereAnd(envExpr);
                            sqlBuilder.append(sqlUpdateBuilder.toString());
                        } else if (sqlStatement instanceof SQLDeleteStatement) {
                            MySqlDeleteStatement sqlDeleteStatement = (MySqlDeleteStatement) sqlStatement;
                            SQLExprTableSource tableSource = (SQLExprTableSource) sqlDeleteStatement.getTableSource();
                            SQLIdentifierExpr tableExpr = (SQLIdentifierExpr) tableSource.getExpr();
                            if (excludeTables.contains(tableExpr.getName())) {
                                //忽略处理的表
                                continue;
                            }

                            SQLDeleteBuilder sqlDeleteBuilder = SQLBuilderFactory.createDeleteBuilder(sqlStatement.toString(), DbType.mysql);
                            String alias = sqlDeleteStatement.getAlias();

                            //where加上环境条件
                            String envExpr = StrUtil.isBlank(alias) ? EnvConstants.ENV_COLUMN_NAME + "='" + env + "'" : alias + "." + EnvConstants.ENV_COLUMN_NAME + "='" + env + "'";
                            sqlDeleteBuilder.whereAnd(envExpr);
                            sqlBuilder.append(sqlDeleteBuilder.toString());
                        } else {
                            logger.info("不明sql类型: {} - {}", Objects.nonNull(sqlStatement) ? sqlStatement.getClass().getName() : "null", boundSql.getSql());
                        }
                    }

                    if (sqlBuilder.length() > 0) {
                        if (logger.isTraceEnabled()) {
                            logger.info("加上环境变量之后的sql: {}", sqlBuilder.toString());
                        }
                        setValue(metaObject, delegate, BOUND_SQL_SQL, sqlBuilder.toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("拦截器修改env sql的时候出现错误", e);
        }
        return invocation.proceed();
    }

    /**
     * 给实体类的env属性赋值
     * @param env
     * @param boundSql
     * @throws IllegalAccessException
     */
    private void appendEnv4Param(String env, BoundSql boundSql) throws IllegalAccessException {
        //1.存在env字段,则需要根据环境变量给该字段赋值
        Object parameterObject = Optional.ofNullable(boundSql).map(m -> m.getParameterObject()).orElse(null);
        if(Objects.isNull(parameterObject)){
            return;
        }
        if(parameterObject instanceof Map){
            //集合处理
            //MapperMethod.ParamMap
            //DefaultSqlSession.StrictMap
            Map strictMap =  (Map) parameterObject;
            if(MapUtil.isNotEmpty(strictMap)){
                for (Object coll : strictMap.values()) {
                    if(coll instanceof Iterable){
                        Iterable iterable =  (Iterable)coll;
                        for (Object item : iterable) {
                            Field envField = FieldUtils.getField(item.getClass(), EnvConstants.ENV_COLUMN_NAME, true);
                            if(Objects.nonNull(envField)){
                                FieldUtils.writeField(envField,item, env,true);
                            }
                        }
                    }
                }
            }
        }else {
            //单个对象处理
            Field envField = FieldUtils.getField(parameterObject.getClass(), EnvConstants.ENV_COLUMN_NAME, true);
            if(Objects.nonNull(envField)){
                FieldUtils.writeField(envField,parameterObject, env,true);
            }
        }
    }

    @Override
    public Object plugin(final Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(final Properties properties) {
//        String excludeTablesStr = environment.getProperty("env.mybatis.excludeTables", "");
        String excludeTablesStr = "sdp_user,sdp_token,sdp_project,sdp_project_user,sdp_engine,sdp_project_engine,sdp_engine_user,sdp_jar,sdp_data_source_mapping_rel,mysql_connect_cannal,mysql_connect_dim,mysql_connect_join,mysql_connect_normal,regression_result";
        this.excludeTables = new HashSet<>(Arrays.asList(excludeTablesStr.split(",")));
    }

    private StatementHandler getStatementHandler(final Invocation invocation) throws Exception {
        StatementHandler target = (StatementHandler)invocation.getTarget();
        while(Proxy.isProxyClass(target.getClass())) {
            Plugin plugin = (Plugin)Proxy.getInvocationHandler(target);
            Field field = plugin.getClass().getDeclaredField("target");
            field.setAccessible(true);
            target = (StatementHandler)field.get(plugin);
        }
        return target;
    }

    private boolean isDelegate(final StatementHandler statementHandler) {
        return statementHandler instanceof RoutingStatementHandler;
    }

    private void setValue(final MetaObject metaObject, final boolean delegate, final String key, final Object value) {
        metaObject.setValue(key(delegate, key), value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(final MetaObject metaObject, final boolean delegate, final String key) {
        return (T) metaObject.getValue(key(delegate, key));
    }


    private String key(final boolean delegate, final String key) {
        return delegate ? MAP.get(key) : key;
    }

    private BoundSql getBoundSql(final boolean delegate, final MetaObject metaObject) {
        return getValue(metaObject, delegate, BOUND_SQL);
    }

    public static String beautifySql(final String sql) {
        if(StrUtil.isBlank(sql)){
            return "";
        }
        return sql.replaceAll("[\r\n]", " ").replaceAll("\\s{2,}", " ").trim();
    }
}