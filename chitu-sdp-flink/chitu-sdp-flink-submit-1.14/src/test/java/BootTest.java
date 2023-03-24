import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.flink.Application;
import com.chitu.bigdata.sdp.flink.common.util.DeflaterUtils;
import com.chitu.bigdata.sdp.flink.submit.service.KubernetesApplicationServiceImpl;
import com.chitu.cloud.web.test.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class BootTest extends BaseTest {


    @Autowired
    KubernetesApplicationServiceImpl kubernetesApplicationService;

    @Test
    public void start() {
        String sql = "CREATE TABLE `datagen_source` (\n" +
                " f_sequence INT,\n" +
                " f_random INT,\n" +
                " f_random_str STRING,\n" +
                " ts as DATE_FORMAT(NOW(),'yyyy-MM-dd HH:mm:ss'),\n" +
                " dt as DATE_FORMAT(NOW(),'yyyy-MM-dd'),\n" +
                " hr as DATE_FORMAT(NOW(),'HH')\n" +
                ") WITH (\n" +
                " 'connector' = 'datagen',\n" +
                " 'rows-per-second'='1',\n" +
                " 'fields.f_sequence.kind'='sequence',\n" +
                " 'fields.f_sequence.start'='1',\n" +
                " 'fields.f_sequence.end'='1000',\n" +
                " 'fields.f_random.min'='1',\n" +
                " 'fields.f_random.max'='1000',\n" +
                " 'fields.f_random_str.length'='10'\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE `datagen` (\n" +
                " f_sequence INT,\n" +
                " f_random INT,\n" +
                " f_random_str STRING,\n" +
                " ts AS localtimestamp,\n" +
                " WATERMARK FOR ts AS ts\n" +
                ") WITH (\n" +
                " 'connector' = 'datagen',\n" +
                " 'rows-per-second'='1',\n" +
                " 'fields.f_sequence.kind'='sequence',\n" +
                " 'fields.f_sequence.start'='1',\n" +
                " 'fields.f_sequence.end'='10000',\n" +
                " 'fields.f_random.min'='1',\n" +
                " 'fields.f_random.max'='1000',\n" +
                " 'fields.f_random_str.length'='10'\n" +
                ");\n" +
                "CREATE TABLE `print` (\n" +
                " lenth int\n" +
                ") WITH (\n" +
                "  'connector' = 'print'\n" +
                ");\n" +
                "\n" +
                "CREATE CATALOG `sink_hive`\n" +
                "WITH (\n" +
                "  'type' = 'hive',\n" +
                "  'hive-conf-dir' = '/opt/apache/hive-one-ticket/conf',\n" +
                "  'hadoop-conf-dir' = '/opt/apache/hadoop-one-ticket/etc/hadoop/'\n" +
                ");\n" +
                "\n" +
                "\n" +
                "insert into sink_hive.sdp_test.bigdata_hive_0922 select * from datagen_source;\n";


        System.out.println(DeflaterUtils.zipString(sql));

        Application application = JSONObject.parseObject("{\n" +
                "  \"allowNonRestored\": false,\n" +
                "  \"appType\": 2,\n" +
                "  \"drain\": false,\n" +
                "  \"executionMode\": 4,\n" +
                "  \"flameGraph\": false,\n" +
                "  \"flinkSql\": \"eNrNVMFymzAQvfMVe5PpBOzm1mRyoC6JPbWhg+l4esIULUZjkKgkt/HfR8imMWnjHjrTKSe0+94T+94O0yQM0hDS4P0ihA3Ndb5FnimxlwVuYORAmSn8tkdeIMyj9KoryJxT0bw4ZkpLWKXJPHowVa0gV/DBSGf3cbIM0lEUr0fuFTmYx1suPUphNrtpmhuliGsIVP+RYHGVfAU3mxHXcWE9T2fdZ5NCcI6FFpLAHZDTYMQoECl+KK9F6Sk0IEruyFtbLxnWVPnPA/s7Ztv9+RWU0rnUF1TweMdkMhkCjr75DeO/kvte/niB2lnu18i3urIo4ri3jjP9XaJ/F2WwgloUea1Zg2bapjXltbnE+J98BJPDCaTV/57AP4lgmEArGdfWf4PTFZjTmUsvbbLoQZDTIA0W8QNsFOO7rGLfceM8s/WhRUvsGt0XHt88o1p6lB1Fx6LV47zNiwrHtis4epoVO9TjDnji5VSI9gLz2D/joi5O1fFw98LlpzgJki9w/zmapvM4guVhYV3q1sSM3Pi7Az62EpXyv7Jttxx+WZsB/T0t/R5MjCTjCqXuXBPw0wFf0TbTZhd7sq1mk3fX16CwNnbCGyil2ezhH83ozaNVmKTdxsdgze4J/aWj82zdgcqt8wRiw6C7\",\n" +
                "  \"id\": 1141,\n" +
                "  \"jobName\": \"st-demo-pro-1115\",\n" +
                "  \"jobType\": 2,\n" +
                "  \"options\": \"{\\\"state.checkpoints.num-retained\\\":\\\"5\\\",\\\"env.java.opts\\\":\\\"-Dproject_code=st_demo_pro -Dflink_job_name=st-demo-pro-114 -Dyarn_container_id=$CONTAINER_ID\\\",\\\"state.checkpoint-storage\\\":\\\"filesystem\\\",\\\"parallelism.default\\\":1,\\\"yarn.application.queue\\\":\\\"sdp_flink\\\",\\\"taskmanager.memory.process.size\\\": \\\"2G\\\",\\\"execution.checkpointing.mode\\\":\\\"EXACTLY_ONCE\\\",\\\"table.exec.source.idle-timeout\\\":\\\"60000\\\",\\\"table.exec.mini-batch.size\\\":\\\"1000\\\",\\\"table.optimizer.distinct-agg.split.enabled\\\":\\\"true\\\",\\\"execution.checkpointing.unaligned\\\":\\\"false\\\",\\\"state.backend.incremental\\\":\\\"true\\\",\\\"restart-strategy.fixed-delay.attempts\\\":\\\"10\\\",\\\"jobmanager.memory.process.size\\\":\\\"1G\\\",\\\"execution.checkpointing.interval\\\":\\\"10000\\\",\\\"table.exec.state.ttl\\\":\\\"90000000\\\",\\\"table.exec.mini-batch.enabled\\\":\\\"true\\\",\\\"restart-strategy.fixed-delay.delay\\\":\\\"1000\\\",\\\"execution.checkpointing.externalized-checkpoint-retention\\\":\\\"RETAIN_ON_CANCELLATION\\\",\\\"table.exec.hive.fallback-mapred-writer\\\":\\\"false\\\",\\\"restart-strategy\\\": \\\"fixed-delay\\\",\\\"state.backend\\\":\\\"rocksdb\\\",\\\"execution.checkpointing.max-concurrent-checkpoints\\\":\\\"1\\\",\\\"table.exec.mini-batch.allow-latency\\\":\\\"10s\\\",\\\"state.checkpoints.dir\\\":\\\"hdfs://bigbigworld/sdp/ck/\\\"}\",\n" +
                "  \"resolveOrder\": 0,\n" +
                "  \"savePointed\": false\n" +
                "}", Application.class);

        application.setFlinkSql(DeflaterUtils.zipString(sql));
        application.setNamespace("hadoop-test-ha");
        application.setHadoopConfigMapName("hadoop-one-ticket");
        application.setContainerImage("*****/bigdata-application/flink-application-sdp-sql-client:1.14.3");
        application.setFlinkUserJar("local:///opt/flink/usrlib/bigdata-sdp-flink-sqlclient-1.14-1.0.0-SNAPSHOT.jar");
        kubernetesApplicationService.start(application);
    }




    @Test
    public void stop() {

        Application application = new Application();
        application.setJobName("st-demo-pro-114");
        application.setNamespace("hadoop-test-ha");
        new KubernetesApplicationServiceImpl().killCluster(application);

    }





}
