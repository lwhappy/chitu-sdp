package com.chitu.bigdata.sdp.service.validate.custom;

import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.ExplainDetail;
import org.apache.flink.table.api.TableConfig;
import org.apache.flink.table.api.TableException;
import org.apache.flink.table.api.internal.TableEnvironmentImpl;
import org.apache.flink.table.catalog.CatalogManager;
import org.apache.flink.table.catalog.FunctionCatalog;
import org.apache.flink.table.catalog.GenericInMemoryCatalog;
import org.apache.flink.table.delegation.Executor;
import org.apache.flink.table.delegation.ExecutorFactory;
import org.apache.flink.table.delegation.Planner;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.factories.PlannerFactoryUtil;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.table.functions.TableAggregateFunction;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.table.functions.UserDefinedFunctionHelper;
import org.apache.flink.table.module.ModuleManager;
import org.apache.flink.table.operations.ExplainOperation;
import org.apache.flink.table.operations.ModifyOperation;
import org.apache.flink.table.operations.Operation;
import org.apache.flink.table.operations.QueryOperation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 定制TableEnvironmentImpl
 *
 * @author wenmo
 * @since 2021/10/22 10:02
 **/
public class CustomTableEnvironmentImpl extends TableEnvironmentImpl {

    public CustomTableEnvironmentImpl(
            CatalogManager catalogManager,
            ModuleManager moduleManager,
            FunctionCatalog functionCatalog,
            TableConfig tableConfig,
            StreamExecutionEnvironment executionEnvironment,
            Planner planner,
            Executor executor,
            boolean isStreamingMode,
            ClassLoader userClassLoader) {
        super(
                catalogManager,
                moduleManager,
                tableConfig,
                executor,
                functionCatalog,
                planner,
                isStreamingMode,
                userClassLoader);
    }

    public static CustomTableEnvironmentImpl create(StreamExecutionEnvironment executionEnvironment){
        return create(executionEnvironment, EnvironmentSettings.newInstance().build(),TableConfig.getDefault());
    }

    public static CustomTableEnvironmentImpl create(
            StreamExecutionEnvironment executionEnvironment,
            EnvironmentSettings settings,
            TableConfig tableConfig) {

        // temporary solution until FLINK-15635 is fixed
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        final ModuleManager moduleManager = new ModuleManager();

        final CatalogManager catalogManager =
                CatalogManager.newBuilder()
                        .classLoader(classLoader)
                        .config(tableConfig.getConfiguration())
                        .defaultCatalog(
                                settings.getBuiltInCatalogName(),
                                new GenericInMemoryCatalog(
                                        settings.getBuiltInCatalogName(),
                                        settings.getBuiltInDatabaseName()))
                        .executionConfig(executionEnvironment.getConfig())
                        .build();

        final FunctionCatalog functionCatalog =
                new FunctionCatalog(tableConfig, catalogManager, moduleManager);

        final Executor executor =
                lookupExecutor(classLoader, settings.getExecutor(), executionEnvironment);

        final Planner planner =
                PlannerFactoryUtil.createPlanner(
                        settings.getPlanner(),
                        executor,
                        tableConfig,
                        catalogManager,
                        functionCatalog);

        return new CustomTableEnvironmentImpl(
                catalogManager,
                moduleManager,
                functionCatalog,
                tableConfig,
                executionEnvironment,
                planner,
                executor,
                settings.isStreamingMode(),
                classLoader);
    }

    private static Executor lookupExecutor(
            ClassLoader classLoader,
            String executorIdentifier,
            StreamExecutionEnvironment executionEnvironment) {
        try {
            final ExecutorFactory executorFactory =
                    FactoryUtil.discoverFactory(
                            classLoader, ExecutorFactory.class, executorIdentifier);
            final Method createMethod =
                    executorFactory
                            .getClass()
                            .getMethod("create", StreamExecutionEnvironment.class);

            return (Executor) createMethod.invoke(executorFactory, executionEnvironment);
        } catch (Exception e) {
            throw new TableException(
                    "Could not instantiate the executor. Make sure a planner module is on the classpath",
                    e);
        }
    }

    public SqlExplainResult explainSqlRecord(String statement, ExplainDetail... extraDetails) {
        SqlExplainResult record = new SqlExplainResult();
        List<Operation> operations = getParser().parse(statement);
        record.setParseTrue(true);
        if (operations.size() != 1) {
            throw new TableException(
                    "Unsupported SQL query! explainSql() only accepts a single SQL query, sql: " + statement);
        }
        List<Operation> operationlist = new ArrayList<>(operations);
        for (int i = 0; i < operationlist.size(); i++) {
            Operation operation = operationlist.get(i);
            if (operation instanceof ModifyOperation) {
                record.setType("error");
            } else if (operation instanceof ExplainOperation) {
                record.setType("error");
            } else if (operation instanceof QueryOperation) {
                record.setType("error");
            } else {
                record.setExplain(operation.asSummaryString());
                operationlist.remove(i);
                record.setType("error");
                i=i-1;
            }
        }
        record.setExplainTrue(true);
        if(operationlist.size()==0){
            //record.setExplain("DDL语句不进行解释。");
            return record;
        }
        record.setExplain(planner.explain(operationlist, extraDetails));
        return record;
    }

    public <T> void registerFunction(String name, TableFunction<T> tableFunction) {
        TypeInformation<T> typeInfo = UserDefinedFunctionHelper.getReturnTypeOfTableFunction(tableFunction);
        this.functionCatalog.registerTempSystemTableFunction(name, tableFunction, typeInfo);
    }

    public <T, ACC> void registerFunction(String name, AggregateFunction<T, ACC> aggregateFunction) {
        TypeInformation<T> typeInfo = UserDefinedFunctionHelper.getReturnTypeOfAggregateFunction(aggregateFunction);
        TypeInformation<ACC> accTypeInfo = UserDefinedFunctionHelper.getAccumulatorTypeOfAggregateFunction(aggregateFunction);
        this.functionCatalog.registerTempSystemAggregateFunction(name, aggregateFunction, typeInfo, accTypeInfo);
    }

    public <T, ACC> void registerFunction(String name, TableAggregateFunction<T, ACC> tableAggregateFunction) {
        TypeInformation<T> typeInfo = UserDefinedFunctionHelper.getReturnTypeOfAggregateFunction(tableAggregateFunction);
        TypeInformation<ACC> accTypeInfo = UserDefinedFunctionHelper.getAccumulatorTypeOfAggregateFunction(tableAggregateFunction);
        this.functionCatalog.registerTempSystemAggregateFunction(name, tableAggregateFunction, typeInfo, accTypeInfo);
    }
}
