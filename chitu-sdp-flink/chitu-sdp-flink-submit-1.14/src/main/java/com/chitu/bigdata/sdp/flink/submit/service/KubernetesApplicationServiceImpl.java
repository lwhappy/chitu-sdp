package com.chitu.bigdata.sdp.flink.submit.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chitu.bigdata.sdp.api.flink.Application;
import com.chitu.bigdata.sdp.flink.common.enums.ResolveOrder;
import com.chitu.bigdata.sdp.flink.common.util.DeflaterUtils;
import com.chitu.bigdata.sdp.flink.submit.config.FlinkConfig;
import com.chitu.bigdata.sdp.flink.submit.constant.FlinkConstant;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.configuration.*;
import org.apache.flink.kubernetes.KubernetesClusterClientFactory;
import org.apache.flink.kubernetes.KubernetesClusterDescriptor;
import org.apache.flink.kubernetes.configuration.KubernetesConfigOptions;
import org.apache.flink.kubernetes.configuration.KubernetesDeploymentTarget;
import org.apache.flink.kubernetes.kubeclient.FlinkKubeClient;
import org.apache.flink.kubernetes.kubeclient.FlinkKubeClientFactory;
import org.apache.flink.runtime.jobgraph.SavepointConfigOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service("kubernetes-application")
public class KubernetesApplicationServiceImpl implements FlinkAppService {


    @Autowired
    private FlinkConfig flinkConfig;


    @Override
    public SubmitResponse start(Application application) {

        Map<String, Object> configMap = getConfigMap(application.getOptions());

        // 构建 用户jar main参数
        List<String> userJarProgramArgs = buildUserJarProgramArgs(application, configMap);

        // 构建 flink 部署参数
        Configuration flinkConfig = buildFlinkConfiguration(application, configMap);

        //application.setMainClass("com.chitu.bigdata.sdp.flink.client.Test1027");

        // 创建 kubernetesClusterDescriptor 对象
        FlinkKubeClient flinkKubeClient = FlinkKubeClientFactory.getInstance().fromConfiguration(flinkConfig, "client");
        KubernetesClusterDescriptor kubernetesClusterDescriptor = new KubernetesClusterDescriptor(flinkConfig, flinkKubeClient);

        try {
            // 用户 jar main方法参数和启动类
            final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(userJarProgramArgs.toArray(new String[userJarProgramArgs.size()]), application.getMainClass());

            KubernetesClusterClientFactory kubernetesClusterClientFactory = new KubernetesClusterClientFactory();
            // 集群资源描述
            final ClusterSpecification clusterSpecification = kubernetesClusterClientFactory.getClusterSpecification(flinkConfig);

            // 部署
            ClusterClientProvider<String> clusterClientProvider = kubernetesClusterDescriptor.deployApplicationCluster(clusterSpecification, applicationConfiguration);
            ClusterClient<String> clusterClient = clusterClientProvider.getClusterClient();
            String clusterId = clusterClient.getClusterId();
            return new SubmitResponse(clusterId, flinkConfig, clusterClient.getWebInterfaceURL());
        } catch (Exception e) {
            throw new RuntimeException("提交任务异常", e);
        } finally {
            kubernetesClusterDescriptor.close();
        }

    }

    private Configuration buildFlinkConfiguration(Application application, Map<String, Object> configMap) {

        // 加载flink配置文件全局默认配置
        Configuration configuration = GlobalConfiguration.loadConfiguration(flinkConfig.getFlinkHome() + "/conf");

        // 赋值高级配置中的部署参数
        configMap.entrySet().removeIf(entry -> entry.getKey().startsWith("table."));
        configMap.forEach((k, v) -> configuration.setString(k, v.toString()));

        // 部署模式
        configuration.setString(DeploymentOptions.TARGET, KubernetesDeploymentTarget.APPLICATION.getName());

        // flink程序作业名称
        configuration.set(PipelineOptions.NAME, application.getJobName());

        // k8s 集群id，唯一标识，用任务名来代替
        configuration.set(KubernetesConfigOptions.CLUSTER_ID, subStrJobName(application.getJobName(), application.getId().toString()));

        // 保存点
        if (StrUtil.isNotEmpty(application.getSavePoint())) {
            configuration.setString(SavepointConfigOptions.SAVEPOINT_PATH, application.getSavePoint());
        }

        // 用户 jar
        configuration.set(PipelineOptions.JARS, Collections.singletonList(application.getFlinkUserJar()));

        // 目前默认设置 优先加载应用程序类路径，默认是先从用户代码jar加载类
        configuration.setString("classloader.resolve-order", ResolveOrder.PARENT_FIRST.getName());

        // add flink conf configuration, mainly to set the log4j configuration
        if (!configuration.contains(DeploymentOptionsInternal.CONF_DIR)) {
            configuration.set(DeploymentOptionsInternal.CONF_DIR, flinkConfig.getFlinkHome() + "/conf");
        }

        //configuration.set(KubernetesConfigOptions.FLINK_CONF_DIR, flinkConfig.getFlinkHome() + "/conf");

        // k8s相关参数
        configuration.set(KubernetesConfigOptions.CONTAINER_IMAGE, application.getContainerImage());
        configuration.set(KubernetesConfigOptions.NAMESPACE, application.getNamespace());
        configuration.set(KubernetesConfigOptions.HADOOP_CONF_CONFIG_MAP, application.getHadoopConfigMapName());
        configuration.set(KubernetesConfigOptions.JOB_MANAGER_SERVICE_ACCOUNT, application.getJobManagerServiceAccount());
        configuration.set(KubernetesConfigOptions.REST_SERVICE_EXPOSED_TYPE, KubernetesConfigOptions.ServiceExposedType.NodePort);
        //configuration.set(KubernetesConfigOptions.KUBERNETES_JOBMANAGER_REPLICAS, 1);
        // 任务运行的容器镜像拉取策略，先默认每次都拉取，后续考虑根据任务构建推送镜像后动态指定
        configuration.set(KubernetesConfigOptions.CONTAINER_IMAGE_PULL_POLICY, KubernetesConfigOptions.ImagePullPolicy.Always);

        return configuration;
    }


    private List<String> buildUserJarProgramArgs(Application application, Map<String, Object> configMap) {
        List<String> userJarProgramArguments = new ArrayList<>();

        // 1.dstream程序 用户自定义的 main参数
        configMap.forEach((k, v) -> {
            if (StrUtil.startWith(k, FlinkConstant.MAIN_ARGS_PREFIX)) {
                userJarProgramArguments.add("--" + k);
                userJarProgramArguments.add(v.toString());
            }
        });

        // 2.用户jar执行sql
        userJarProgramArguments.add("--sql");
        userJarProgramArguments.add(application.getFlinkSql());

        // 3.高级配置中的table参数
        Map<String, String> tableConfigMap = new HashMap<>();
        configMap.forEach((k, v) -> {
            if (k.startsWith("table.")) {
                tableConfigMap.put(k, v.toString());
            }
        });
        String tableConfigZip = DeflaterUtils.zipString(JSON.toJSONString(tableConfigMap, SerializerFeature.QuoteFieldNames));
        // 目前命名 yaml.conf 不够见名知意，先兼容 yarn app 模式时指定的名称
        userJarProgramArguments.add("--yaml.conf");
        userJarProgramArguments.add(tableConfigZip);

        return userJarProgramArguments;
    }


    private Map<String, Object> getConfigMap(String options) {
        Map<String, Object> map = JSON.parseObject(options, Map.class);
        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }


    @Override
    public String stop(Application application) {
        Configuration flinkConfig = new Configuration();
        flinkConfig.set(DeploymentOptions.TARGET, KubernetesDeploymentTarget.APPLICATION.getName());
        flinkConfig.set(KubernetesConfigOptions.CLUSTER_ID, application.getAppId());
        flinkConfig.set(KubernetesConfigOptions.NAMESPACE, application.getNamespace());
        // 创建 kubernetesClusterDescriptor 对象
        FlinkKubeClient flinkKubeClient = FlinkKubeClientFactory.getInstance().fromConfiguration(flinkConfig, "client");
        KubernetesClusterDescriptor kubernetesClusterDescriptor = new KubernetesClusterDescriptor(flinkConfig, flinkKubeClient);
        try {
            ClusterClientProvider<String> clusterClientProvider = kubernetesClusterDescriptor.retrieve(application.getAppId());
            ClusterClient<String> clusterClient = clusterClientProvider.getClusterClient();
            clusterClient.cancel(JobID.fromHexString(application.getJobId())).get(30, TimeUnit.SECONDS);
            return null;
        } catch (Exception e) {
            throw new RuntimeException("cancel job exception", e);
        } finally {
            kubernetesClusterDescriptor.close();
        }
    }

    @Override
    public String triggerSavepoint(Application application) {

        Configuration configuration = new Configuration();
        configuration.set(DeploymentOptions.TARGET, KubernetesDeploymentTarget.APPLICATION.getName());
        configuration.set(KubernetesConfigOptions.CLUSTER_ID, application.getAppId());
        configuration.set(KubernetesConfigOptions.NAMESPACE, application.getNamespace());
        // 创建 kubernetesClusterDescriptor 对象
        FlinkKubeClient flinkKubeClient = FlinkKubeClientFactory.getInstance().fromConfiguration(configuration, "client");
        KubernetesClusterDescriptor kubernetesClusterDescriptor = new KubernetesClusterDescriptor(configuration, flinkKubeClient);
        try {
            // application.getAppId() 等价 application.getJobName().replace("_", "-")
            ClusterClientProvider<String> clusterClientProvider = kubernetesClusterDescriptor.retrieve(application.getAppId());
            ClusterClient<String> clusterClient = clusterClientProvider.getClusterClient();
            return clusterClient.triggerSavepoint(JobID.fromHexString(application.getJobId()), application.getSavePoint() + "/" + application.getJobId()).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("job triggerSavepoint exception", e);
        } finally {
            kubernetesClusterDescriptor.close();
        }
    }


    public void killCluster(Application application) {
        Configuration flinkConfig = new Configuration();
        flinkConfig.set(DeploymentOptions.TARGET, KubernetesDeploymentTarget.APPLICATION.getName());
        flinkConfig.set(KubernetesConfigOptions.CLUSTER_ID, application.getAppId());
        flinkConfig.set(KubernetesConfigOptions.NAMESPACE, application.getNamespace());
        FlinkKubeClient flinkKubeClient = FlinkKubeClientFactory.getInstance().fromConfiguration(flinkConfig, "client");
        KubernetesClusterDescriptor kubernetesClusterDescriptor = new KubernetesClusterDescriptor(flinkConfig, flinkKubeClient);
        try {
            kubernetesClusterDescriptor.killCluster(application.getAppId());
        } catch (Exception e) {
            throw new RuntimeException("killCluster exception", e);
        } finally {
            kubernetesClusterDescriptor.close();
        }
    }

    private String subStrJobName(String jobName, String jobId) {
        // 截取jobName的前22位
        int len = 22;
        String subJobName = "";
        if (jobName.length() >= len) {
            subJobName = jobName.substring(0, len);
        } else {
            subJobName = jobName;
        }
        return subJobName.replace("_", "-") + "-" + jobId;
    }

}
