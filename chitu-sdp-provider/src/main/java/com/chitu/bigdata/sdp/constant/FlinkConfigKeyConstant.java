package com.chitu.bigdata.sdp.constant;

/**
 * @author sutao
 * @create 2021-11-30 12:34
 */
public class FlinkConfigKeyConstant {

    public static final String JOBMANAGER_MEMORY = "jobmanager.memory.process.size";
    public static final String TASKMANAGER_MEMORY = "taskmanager.memory.process.size";
    public static final String PARALLELISM = "parallelism.default";

    public static final String APPLICATION_QUEUE = "yarn.application.queue";

    public static final String CHECKPOINT_DIR = "state.checkpoints.dir";

    public static final String CHECKPOINT_RETENTION = "execution.checkpointing.externalized-checkpoint-retention";

    public static final String CHECKPOINT_NUM_RETAINED = "state.checkpoints.num-retained";

    public static final String YARN_QUEUE = "yarn.application.queue";

    public static final String STARTUP_TIMESTAMP = "scan.startup.timestamp-millis";
    public static final String STARTUP_OFFSETS = "scan.startup.specific-offsets";
    public static final String STARTUP_MODE = "scan.startup.mode";

    public static final String FORMAT = "format";

    public static final String CONNECTOR = "connector";
    public static final String TOPIC = "topic";
    public static final String TOPIC_PATTERN = "topic-pattern";

    public static final String TIMESTAMP = "timestamp";
    public static final String EARLIEST_OFFSET = "earliest-offset";
    public static final String LATEST_OFFSET = "latest-offset";
    public static final String SPECIFIC_OFFSETS = "specific-offsets";
    public static final String GROUP_OFFSETS = "group-offsets";

    public static final String ENV_JAVA_OPTS = "env.java.opts";
    public static final String ENV_JAVA_OPTS_TASKMANAGER = "env.java.opts.taskmanager";

    public static final String TASKMANAGER_NUMBEROFTASKSLOTS = "taskmanager.numberOfTaskSlots";
    public static final String TABLE_EXEC_SINK_UPSERT_MATERIALIZE = "table.exec.sink.upsert-materialize";
    public static final String EXECUTION_CHECKPOINTING_INTERVAL = "execution.checkpointing.interval";
    /**
     * 端到端延迟监控间隔
     */
    public static final String METRICS_LATENCY_INTERVAL = "metrics.latency.interval";



    public static final String PROPERTIES_SECURITY_PROTOCOL = "properties.security.protocol";
    public static final String PROPERTIES_SASL_MECHANISM = "properties.sasl.mechanism";
    public static final String PROPERTIES_SASL_JAAS_CONFIG = "properties.sasl.jaas.config";
    public static final String USERNAME = "username";
    public static final String TIDB_USERNAME = "tidb.username";
    public static final String HIVE_CONF_DIR_FOR_HUDI = "hive.conf.dir";
    public static final String CATALOG_PATH = "catalog.path";


    public static final String HIVE_CONF_DIR = "hive-conf-dir";
    public static final String HADOOP_CONF_DIR = "hadoop-conf-dir";

    public static final String DATABASE_NAME = "database-name";
    public static final String TABLE_NAME = "table-name";
    public static final String ZOOKEEPER_ZNODE_PARENT = "zookeeper.znode.parent";

    public static final String KUDU_TABLE = "kudu.table";

    public static final String PROPERTIES_GROUP_ID = "properties.group.id";
    public static final String PROPERTIES_BOOTSTRAP_SERVERS = "properties.bootstrap.servers";





    public static boolean isConfigured(String config) {
        switch (config) {
            case JOBMANAGER_MEMORY:
            case TASKMANAGER_MEMORY:
            case PARALLELISM:
                return true;
            default:
                return false;
        }

    }

    public static boolean isDefaultConfig(String config){
        switch (config){
            case APPLICATION_QUEUE:
            case CHECKPOINT_DIR:
            case CHECKPOINT_RETENTION:
            case CHECKPOINT_NUM_RETAINED:
//            case NUMBER_OF_TASKSLOTS:
                return true;
            default:
                return false;
        }
    }







}
