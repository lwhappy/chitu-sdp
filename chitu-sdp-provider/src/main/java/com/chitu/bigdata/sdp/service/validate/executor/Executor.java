package com.chitu.bigdata.sdp.service.validate.executor;

import com.chitu.bigdata.sdp.service.validate.SqlManager;
import com.chitu.bigdata.sdp.service.validate.custom.CustomTableEnvironmentImpl;
import com.chitu.bigdata.sdp.service.validate.custom.CustomTableResultImpl;
import com.chitu.bigdata.sdp.service.validate.domain.ExecutorSetting;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.bigdata.sdp.service.validate.interceptor.FlinkInterceptor;
import com.chitu.bigdata.sdp.service.validate.util.Asserts;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.ExplainDetail;
import org.apache.flink.table.api.TableResult;

import java.util.Map;

/**
 * Executor
 * @author  wenmo
 * @since  2021/11/17
 **/
public abstract class Executor {

    protected StreamExecutionEnvironment environment;
    protected CustomTableEnvironmentImpl stEnvironment;
    protected ExecutorSetting executorSetting;

    protected SqlManager sqlManager = new SqlManager();
    protected boolean useSqlFragment = true;

    public SqlManager getSqlManager() {
        return sqlManager;
    }

    public boolean isUseSqlFragment() {
        return useSqlFragment;
    }

    public static Executor build(){
        return new LocalStreamExecutor(ExecutorSetting.DEFAULT);
    }


    public static Executor buildLocalExecutor(ExecutorSetting executorSetting){
        return new LocalStreamExecutor(executorSetting);
    }

    public CustomTableEnvironmentImpl getCustomTableEnvironmentImpl(){
        return stEnvironment;
    }

    protected void init(){
        initEnvironment();
        initStreamExecutionEnvironment();
    }

    private void initEnvironment(){
        if(executorSetting.getCheckpoint()!=null&&executorSetting.getCheckpoint()>0){
            environment.enableCheckpointing(executorSetting.getCheckpoint());
        }
        if(executorSetting.getParallelism()!=null&&executorSetting.getParallelism()>0){
            environment.setParallelism(executorSetting.getParallelism());
        }
    }

    private void initStreamExecutionEnvironment(){
        useSqlFragment = executorSetting.isUseSqlFragment();
        stEnvironment = CustomTableEnvironmentImpl.create(environment);
        if(executorSetting.getJobName()!=null&&!"".equals(executorSetting.getJobName())){
            stEnvironment.getConfig().getConfiguration().setString("pipeline.name", executorSetting.getJobName());
        }
        if(executorSetting.getConfig()!=null){
            for (Map.Entry<String, String> entry : executorSetting.getConfig().entrySet()) {
                stEnvironment.getConfig().getConfiguration().setString(entry.getKey(), entry.getValue());
            }
        }
    }

    public String pretreatStatement(String statement){
        return FlinkInterceptor.pretreatStatement(this,statement);
    }

    private boolean pretreatExecute(String statement){
        return !FlinkInterceptor.build(this,statement);
    }

    public String explainSql(String statement, ExplainDetail... extraDetails){
        statement = pretreatStatement(statement);
        if(pretreatExecute(statement)) {
            return stEnvironment.explainSql(statement,extraDetails);
        }else{
            return "";
        }
    }

    public TableResult executeSql(String statement){
        statement = pretreatStatement(statement);
        if(pretreatExecute(statement)) {
            return stEnvironment.executeSql(statement);
        }else{
            return CustomTableResultImpl.TABLE_RESULT_OK;
        }
    }

    public SqlExplainResult explainSqlRecord(String statement, ExplainDetail... extraDetails) {
        statement = pretreatStatement(statement);
        if(Asserts.isNotNullString(statement)&&pretreatExecute(statement)) {
            return stEnvironment.explainSqlRecord(statement,extraDetails);
        }else{
            return null;
        }
    }
}
