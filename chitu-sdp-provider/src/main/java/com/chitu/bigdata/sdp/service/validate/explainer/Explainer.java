package com.chitu.bigdata.sdp.service.validate.explainer;

import com.chitu.bigdata.sdp.service.validate.Operations;
import com.chitu.bigdata.sdp.service.validate.constant.FlinkSQLConstant;
import com.chitu.bigdata.sdp.service.validate.domain.ExplainResult;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.bigdata.sdp.service.validate.enums.SqlType;
import com.chitu.bigdata.sdp.service.validate.executor.Executor;
import com.chitu.bigdata.sdp.service.validate.job.JobParam;
import com.chitu.bigdata.sdp.service.validate.job.StatementParam;
import com.chitu.bigdata.sdp.service.validate.util.Asserts;
import com.chitu.bigdata.sdp.service.validate.util.SqlUtil;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Explainer
 *
 * @author wenmo
 * @since 2021/6/22
 **/
@Slf4j
public class Explainer {

    private Executor executor;
    private boolean useStatementSet;
    private String sqlSeparator = FlinkSQLConstant.SEPARATOR;
    private static String INSERT_REGEX = "(.)*((insert)|(INSERT)){1}( )+((into)|(INTO)){1}((\\n)|( )|(\\r))+(.)*";
    private static String LINE_REGEX = "line \\d{1,}";
    private static String COMMENT_REGEX = "(^\\s{0,}--|\\n\\s{0,}--|\\r\\n\\s{0,}--).{0,}";

    public Explainer(Executor executor) {
        this.executor = executor;
    }

    public static Explainer build(Executor executor) {
        return new Explainer(executor);
    }

    public Explainer(Executor executor, boolean useStatementSet, String sqlSeparator) {
        this.executor = executor;
        this.useStatementSet = useStatementSet;
        this.sqlSeparator = sqlSeparator;
    }

    public static Explainer build(Executor executor, boolean useStatementSet, String sqlSeparator) {
        return new Explainer(executor, useStatementSet, sqlSeparator);
    }

    public JobParam pretreatStatements(String[] statements) {
        List<StatementParam> ddl = new ArrayList<>();
        List<StatementParam> trans = new ArrayList<>();
        for (String item : statements) {
            String statement = executor.pretreatStatement(item);
            if (statement.isEmpty()) {
                continue;
            }
            // TODO 增加去除注释处理，原来不处理，下面逻辑会直接当做 ddl了
            String removeNotesStatement = SqlUtil.removeNotes(statement);
            // TODO 去除注释后再获取操作类型，之前没做处理会有问题
            SqlType operationType = Operations.getOperationType(removeNotesStatement);
            // TODO removeNotesStatement可能会为空，比如 insert sql被注释
            if (operationType.equals(SqlType.INSERT) || operationType.equals(SqlType.SELECT) || StrUtil.isEmpty(removeNotesStatement)) {
                trans.add(new StatementParam(statement, operationType));
                if (!useStatementSet) {
                    break;
                }
            } else {
                ddl.add(new StatementParam(statement, operationType));
            }
        }
        return new JobParam(ddl, trans);
    }

    public ExplainResult explainSql(String statement) {
        Integer line = 0;
        JobParam jobParam = pretreatStatements(SqlUtil.getStatements(statement, FlinkSQLConstant.SEPARATOR));
        List<SqlExplainResult> sqlExplainRecords = new ArrayList<>();
        int index = 1;
        boolean correct = true;
        for (StatementParam item : jobParam.getDdl()) {
            String currentSql = item.getValue().replace(";", "");
            SqlExplainResult record = new SqlExplainResult();
            try {
                LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(currentSql));
                lineNumberReader.skip(Long.MAX_VALUE);
                int line1 = lineNumberReader.getLineNumber();
                //将注释行替换为空行
                currentSql = currentSql.replaceAll(COMMENT_REGEX, StrUtils.LF);
                if (currentSql.trim().length() != 0) {
                    record = executor.explainSqlRecord(currentSql);
                }
                if (Asserts.isNull(record)) {
                    continue;
                }
                if (currentSql.trim().length() != 0 && !isInsertType(currentSql)) {
                    executor.executeSql(currentSql);
                }
                //每一段SQL的行数进行累加，如果当前SQL校验失败，则不会进行累加
                line += line1;
            } catch (Exception e) {
                log.warn("SQL校验失败", e);
                String message = e.getMessage();
                Integer line4 = handleLine(line, message, currentSql);
                record.setError(message.replaceAll(LINE_REGEX, "line " + line4));
                record.setLine(line4);
                record.setExplainTrue(false);
                record.setExplainTime(LocalDateTime.now());
                record.setSql(currentSql.replaceAll(COMMENT_REGEX, ""));
                record.setIndex(index);
                sqlExplainRecords.add(record);
                correct = false;
                break;
            }
            record.setExplainTrue(true);
            record.setExplainTime(LocalDateTime.now());
            record.setSql(currentSql.replaceAll(COMMENT_REGEX, ""));
            record.setIndex(index++);
            sqlExplainRecords.add(record);
        }
        if (correct && jobParam.getTrans().size() > 0) {
            for (StatementParam item : jobParam.getTrans()) {
                String currentSql = item.getValue().replace(";", "");
                SqlExplainResult record = new SqlExplainResult();
                try {
                    LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(currentSql));
                    lineNumberReader.skip(Long.MAX_VALUE);
                    int line1 = lineNumberReader.getLineNumber();
                    //将注释行替换为空行
                    currentSql = currentSql.replaceAll(COMMENT_REGEX, StrUtils.LF);
                    if (currentSql.trim().length() != 0) {
                        record.setExplain(executor.explainSql(currentSql));
                    }
                    record.setParseTrue(true);
                    record.setExplainTrue(true);
                    //每一段SQL的行数进行累加，如果当前SQL校验失败，则不会进行累加
                    line += line1;
                } catch (Exception e) {
                    log.warn("SQL校验失败", e);
                    String message = e.getMessage();
                    Integer line4 = handleLine(line, message, currentSql);
                    record.setError(message.replaceAll(LINE_REGEX, "line " + line4));
                    record.setLine(line4);
                    record.setParseTrue(false);
                    record.setExplainTrue(false);
                    correct = false;
                } finally {
                    record.setType("error");
                    record.setExplainTime(LocalDateTime.now());
                    record.setSql(currentSql.replaceAll(COMMENT_REGEX, ""));
                    record.setIndex(index++);
                    sqlExplainRecords.add(record);
                }
            }
        }
        return new ExplainResult(correct, sqlExplainRecords.size(), sqlExplainRecords);
    }

    //取出报错信息中的行数，与自己累加的行数进行相加，得到最终的报错行数
    private Integer handleLine(Integer line, String message, String statement) {
        Pattern pattern = Pattern.compile(LINE_REGEX);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String line2 = matcher.group();
            String line3 = line2.replace("line ", "");
            line += Integer.valueOf(line3);
        } else if (message.contains("CREATE CATALOG")) {
            String[] statementArr = statement.split(StrUtils.LF);
            for (int i = 0; i < statementArr.length; i++) {
                if (statementArr[i].toUpperCase().contains("CREATE CATALOG")) {
                    line += i + 1;
                    break;
                }
            }
        } else {
            line = 0;
        }
        return line;
    }

    private boolean isInsertType(String sql) {
        Pattern compile = Pattern.compile(INSERT_REGEX);
        Matcher matcher = compile.matcher(sql);
        while (matcher.find()) {
            return true;
        }
        return false;
    }


}
