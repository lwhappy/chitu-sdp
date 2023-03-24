## Flink DataStream流作业快速入门
### 前提条件
##### **进入项目**

要有对应的项目，进入到对应的项目进行开发，可以在`项目管理`中`进入项目`或者在`作业开发`中切换项目，若没有对应的项目则需要让项目管理员或者系统管理员进行添加。

![](image/quickStart/进入项目.png)

![](image/quickStart/切换项目.png)


##### **计算引擎**

`计算引擎`即运行的集群环境，比如yarn集群，需在nacos配置文件中配置，项目中要有关联的`计算引擎`，在开发作业时才能选择对应的引擎进行运行，具体可在`用户手册`的`引擎管理`中查看。


##### **准备jar作业**

首先先写dataStream流作业，写完后打jar包上传，这里以datagen输出到print控制台为例



引入依赖

```
<properties>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>
    <flink.version>1.14.3</flink.version>
    <scala.binary.version>2.11</scala.binary.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-table-api-java-bridge_${scala.binary.version}</artifactId>
        <version>${flink.version}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```



编写流作业代码

```
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Demo03_DataGen_Print {

    public static void main(String[] args) {
    
        //通过该方式获取高级配置定义的main参数
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        parameterTool.toMap().forEach((k, v) -> {
            //  System.out.println(k + ":" + v);
        });
        String jobName = parameterTool.get("main.param.pipeline.name","Demo03_DataGen_Print");
        System.out.println("jobName = " + jobName);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        env.setParallelism(1);
        tableEnv.getConfig().getConfiguration().setString(PipelineOptions.NAME,jobName);
        
        String orderSql="CREATE TABLE order_info (\n" +
            "    id INT,\n" +        "    user_id BIGINT,\n" +
            "    total_amount DOUBLE,\n" +
            "    create_time AS localtimestamp,\n" +
            "    WATERMARK FOR create_time AS create_time\n" +
            ") WITH (\n" +
                "    'connector' = 'datagen',\n" +
                "    'rows-per-second'='1',\n" +
                "    'fields.id.kind'='sequence',\n" +
                "    'fields.id.start'='1',\n" +
                "    'fields.id.end'='1000000',\n" +
                "    'fields.user_id.kind'='random',\n" +
                "    'fields.user_id.min'='1',\n" +
                "    'fields.user_id.max'='1000000',\n" +
                "    'fields.total_amount.kind'='random',\n" +
                "    'fields.total_amount.min'='1',\n" +
                "    'fields.total_amount.max'='1000'\n" +
                ")";
                
        String sinkSql="CREATE TABLE sink_order_info (\n" +
            "    id INT,\n" +        "    user_id BIGINT,\n" +
            "    total_amount DOUBLE,\n" +
            "    create_time AS localtimestamp,\n" +
            "    WATERMARK FOR create_time AS create_time\n" +
            ") WITH (\n" +
            "    'connector' = 'print'\n" +
            ")";
         
        tableEnv.executeSql(orderSql);
        tableEnv.executeSql(sinkSql);
        tableEnv.executeSql("insert into sink_order_info select id,user_id,total_amount from order_info");
        
     }
 }

```



### 步骤一：上传jar包

选择`实时开发`菜单中的`资源管理`，上传打包好的作业jar包。
![fdfd5534244c174ee1f50beef7045987.png](image/quickStart/上传jar.png)



### 步骤二：创建jar作业

然后在作业开发中选择DS文件类型的作业，选择上传的jar包资源及版本，填写运行入口。

![37297992a211d375bf1d87f41c39b30c.png](image/quickStart/新建jar作业.png)



### 步骤三：发布jar作业

![](image/quickStart/发布jar.png)



### 步骤四：运行jar任务
在发布完UAT环境之后，在作业运维中可以看到发布的作业，刚上线的作业为初始状态，点击启动。
![bfd7e2edf72ccdb8a2ec2a8b9f09d202.png](image/quickStart/运行jar.png)



### 步骤五：查看运行情况

启动后可以看到作业在`运行中`的状态，flink jar作业就启动起来了，可点击右侧`flink_ui`中查看运行状态。
![bfd7e2edf72ccdb8a2ec2a8b9f09d202.png](image/quickStart/运行中jar.png)
![](image/quickStart/jar_flink_ui.png)
