

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.service;


import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.ClusterInfoBO;
import com.chitu.bigdata.sdp.api.bo.SdpEngineBO;
import com.chitu.bigdata.sdp.api.bo.SysconfigBO;
import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import com.chitu.bigdata.sdp.api.domain.EngineUserInfo;
import com.chitu.bigdata.sdp.api.domain.QueueAndSourceConfig;
import com.chitu.bigdata.sdp.api.domain.SdpEngineInfo;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.flink.ResourceValidateResp;
import com.chitu.bigdata.sdp.api.flink.YarnSchedulerInfo;
import com.chitu.bigdata.sdp.api.flink.yarn.ResourceMemAndCpu;
import com.chitu.bigdata.sdp.api.flink.yarn.YarnClusterMetrics;
import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.bigdata.sdp.api.model.SdpEngineUser;
import com.chitu.bigdata.sdp.api.model.SdpProjectEngine;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.api.vo.SdpProjectResp;
import com.chitu.bigdata.sdp.config.CheckConfigProperties;
import com.chitu.bigdata.sdp.config.ClusterInfoConfig;
import com.chitu.bigdata.sdp.config.KubernetesClusterConfig;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.CommonConstant;
import com.chitu.bigdata.sdp.flink.common.util.HadoopUtils;
import com.chitu.bigdata.sdp.mapper.SdpEngineMapper;
import com.chitu.bigdata.sdp.mapper.SdpProjectMapper;
import com.chitu.bigdata.sdp.utils.PaginationUtils;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.ContextUtils;
import com.chitu.cloud.utils.MathUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * 引擎业务类
 * </pre>
 */
@Service
@Slf4j
@RefreshScope
public class EngineService extends GenericService<SdpEngine, Long> {

    @Autowired
    UserService userService;

    @Autowired
    EngineUserService engineUserService;

    @Autowired
    ProjectEngineService projectEngineService;

    @Autowired
    SdpProjectMapper sdpProjectMapper;

    @Autowired
    private SdpSysConfigService sdpSysConfigService;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    JobService jobService;
    @Autowired
    CheckConfigProperties checkConfigProperties;

    @Autowired
    private ClusterInfoConfig clusterInfoConfig;
    @Autowired
    private SdpEngineMapper engineMapper;
    @Autowired
    SdpConfig sdpConfig;

    @Autowired
    KubernetesClusterConfig kubernetesClusterConfig;

    private String CODE_REX = "([\\w-.])+";

    public EngineService(@Autowired SdpEngineMapper sdpEngineMapper) {
        super(sdpEngineMapper);
    }

    public SdpEngineMapper getMapper() {
        return (SdpEngineMapper) super.genericMapper;
    }


    public Pagination<SdpEngineInfo> getEngineInfo(SdpEngineBO sdpEngineBO) {
        sdpEngineBO.getVo().setEngineName(StrUtils.changeWildcard(sdpEngineBO.getVo().getEngineName()));
        Pagination<SdpEngine> pagination = PaginationUtils.getInstance4BO(sdpEngineBO);
        this.executePagination(p -> getMapper().searchPage(p), pagination);
        List<SdpEngine> sdpEngines = pagination.getRows();
        //封装引擎使用者到列表数据中

        ArrayList<SdpEngineInfo> sdpEngineInfos = new ArrayList<>();
        for (SdpEngine sdpEngine : sdpEngines) {
            SdpEngineInfo sdpEngineInfo = new SdpEngineInfo();
            BeanUtils.copyProperties(sdpEngine, sdpEngineInfo);

            ClusterInfo prodClusterInfo = getClusterInfo(EnvironmentEnum.PROD.getCode(), sdpEngine);
            ClusterInfo uatClusterInfo = getClusterInfo(EnvironmentEnum.UAT.getCode(), sdpEngine);
            sdpEngineInfo.setClusterName(prodClusterInfo.getClusterName());
            sdpEngineInfo.setUatClusterName(uatClusterInfo.getClusterName());
            sdpEngineInfos.add(sdpEngineInfo);
        }
        for (SdpEngineInfo sdpEngine : sdpEngineInfos) {
            List<EngineUserInfo> euis = engineUserService.getMapper().getEngineUsers(sdpEngine.getId());
            sdpEngine.setEngineUsers(euis);
        }
        Pagination<SdpEngineInfo> paginationData = Pagination.getInstance(sdpEngineBO.getPage(), sdpEngineBO.getPageSize());
        paginationData.setRows(sdpEngineInfos);
        paginationData.setRowTotal(pagination.getRowTotal());
        paginationData.setPageTotal(sdpEngineInfos.size());
        return paginationData;
    }

    @Transactional(rollbackFor = Exception.class)
    public Object delete(SdpEngine sdpEngine) {
        if (0L == sdpEngine.getId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpEngine.getId());
        }
        //校验是否还有项目引用
        SdpProjectEngine sdpProjectEngine = new SdpProjectEngine();
        sdpProjectEngine.setEngineId(sdpEngine.getId());
        List<SdpProjectEngine> sdpProjectEngines = projectEngineService.getMapper().selectIsExist(sdpProjectEngine);
        if (!CollectionUtils.isEmpty(sdpProjectEngines)) {
            throw new ApplicationException(ResponseCode.ENGINE_IS_USING);
        }
        //用户disable
        SdpEngineUser sdpEngineUser = new SdpEngineUser();
        sdpEngineUser.setEnabledFlag(EnableType.UNABLE.getCode());
        sdpEngineUser.setEngineId(sdpEngine.getId());
        engineUserService.getMapper().updateDisable(sdpEngineUser);
        return disable(SdpEngine.class,sdpEngine.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Object addUser(List<EngineUserInfo> engineUserInfos) {
        String userId = ContextUtils.get().getUserId();
        for (EngineUserInfo engineUserInfo : engineUserInfos) {
            checkEngineUserInfo(engineUserInfo);
        }
        for (EngineUserInfo engineUserInfo : engineUserInfos) {
            SdpUser sdpuser = new SdpUser();
            sdpuser.setUserName(engineUserInfo.getUserName());
            sdpuser.setEmployeeNumber(engineUserInfo.getEmployeeNumber());
            SdpUser su = renewUserTable(userId, sdpuser);
            SdpEngineUser sdpEngineUser = new SdpEngineUser();
            sdpEngineUser.setEngineId(engineUserInfo.getEngineId());
            sdpEngineUser.setCreatedBy(userId);
            sdpEngineUser.setUserId(su.getId());
            sdpEngineUser.setCreationDate(new Timestamp(System.currentTimeMillis()));
            sdpEngineUser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            engineUserService.insert(sdpEngineUser);
        }

        return ResponseCode.SUCCESS.getMessage();
    }

    /**
     * 更新user表
     * @param userId
     * @param sdpuser
     * @return
     */

    private SdpUser renewUserTable(String userId, SdpUser sdpuser) {
        SdpUser su = userService.getMapper().selectExist(sdpuser);
        if (Objects.isNull(su)) {
            sdpuser.setIsAdmin(UserRole.ManagerEnum.COMMON_USER.getCode());
            sdpuser.setCreationDate(new Timestamp(System.currentTimeMillis()));
            sdpuser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            sdpuser.setCreatedBy(userId);
            userService.getMapper().insert(sdpuser);
            su = sdpuser;
            //被删除的数据恢复
        } else if (EnableType.UNABLE.getCode().equals(su.getEnabledFlag())) {
            su.setIsAdmin(UserRole.ManagerEnum.COMMON_USER.getCode());
            su.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            su.setUpdatedBy(userId);
            su.setEnabledFlag(EnableType.ENABLE.getCode());
            userService.update(su);
        }
        return su;
    }

    private void checkEngineUserInfo(EngineUserInfo engineUserInfo) {
        if (Objects.isNull(engineUserInfo.getEngineId()) || 0L == engineUserInfo.getEngineId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, engineUserInfo.getEngineId());
        }
        SdpEngine se = this.genericMapper.selectById(engineUserInfo.getEngineId());
        if (Objects.isNull(se)) {
            throw new ApplicationException(ResponseCode.ENGINE_NOT_EXIST);
        }
        if (Objects.isNull(engineUserInfo.getUserName())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, engineUserInfo.getUserName());
        }
        if (Objects.isNull(engineUserInfo.getEmployeeNumber()) || StringUtils.isEmpty(engineUserInfo.getEmployeeNumber())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, engineUserInfo.getEmployeeNumber());
        }
    }

    public List<SdpUser> getEngineUser(EngineUserInfo engineUserInfo) {
        if (Objects.isNull(engineUserInfo.getEngineId()) || 0 == engineUserInfo.getEngineId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, engineUserInfo.getEngineId());
        }


        return userService.getMapper().getEngineUser(engineUserInfo);

    }

    @Transactional(rollbackFor = Exception.class)
    public SdpEngine add(SdpEngineInfo sdpEngine) {
        checkSdpEngine(sdpEngine);
        String userId = ContextUtils.get().getUserId();
        SdpEngine se = new SdpEngine();
        se.setEngineName(sdpEngine.getEngineName());
        SdpEngine info = this.getMapper().getInfo(se);
        //判断是否重名
        if (Objects.nonNull(info)) {
            throw new ApplicationException(ResponseCode.ENGINE_REPEAT, sdpEngine.getEngineName());
        }
        SdpEngine instance = new SdpEngine();
        instance.setEngineName(sdpEngine.getEngineName());
        instance.setEngineUrl(sdpEngine.getEngineUrl());
        instance.setEngineVersion(sdpEngine.getEngineVersion());
        instance.setEngineCluster(sdpEngine.getEngineCluster());
        instance.setEngineQueue(sdpEngine.getEngineQueue());
        instance.setCreatedBy(userId);
        instance.setCreationDate(new Timestamp(System.currentTimeMillis()));
        instance.setUpdationDate(new Timestamp(System.currentTimeMillis()));
        instance.setEngineType(sdpEngine.getEngineType());
        instance.setUatEngineCluster(sdpEngine.getUatEngineCluster());
        instance.setUatEngineQueue(sdpEngine.getUatEngineQueue());
        instance.setNamespace(sdpEngine.getNamespace());
        instance.setUatNamespace(sdpEngine.getUatNamespace());
        insert(instance);
        Long engineId = instance.getId();
        //处理用户记录
        List<SdpUser> userInfo = sdpEngine.getUserInfo();
        //判空，兼容添加无用户引擎
        if (!Objects.isNull(userInfo) && !CollectionUtils.isEmpty(userInfo)) {
            for (SdpUser sdpUser : userInfo) {
                SdpEngineUser sdpEngineUser = new SdpEngineUser();
                sdpUser = renewUserTable(userId, sdpUser);
                sdpEngineUser.setEngineId(engineId);
                sdpEngineUser.setUserId(sdpUser.getId());
                sdpEngineUser.setCreatedBy(userId);
                sdpEngineUser.setCreationDate(new Timestamp(System.currentTimeMillis()));
                sdpEngineUser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                engineUserService.getMapper().insert(sdpEngineUser);
            }
        }
        return instance;
    }

    private void checkSdpEngine(SdpEngineInfo sdpEngine) {
//        if (StringUtils.isEmpty(sdpEngine.getEngineUrl()) || Objects.isNull(sdpEngine.getEngineUrl())) {
//            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpEngine.getEngineUrl());
//        }
        if (StringUtils.isEmpty(sdpEngine.getEngineName()) || Objects.isNull(sdpEngine.getEngineName())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpEngine.getEngineName());
        }
       /* if (StringUtils.isEmpty(sdpEngine.getEngineVersion()) || Objects.isNull(sdpEngine.getEngineVersion())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpEngine.getEngineVersion());
        }*/
        //校验引擎名称是否符合规则
        if (!sdpEngine.getEngineName().matches(CODE_REX)) {
            throw new ApplicationException(ResponseCode.ENGINE_NAME_ILLEGAL);
        }
       /* if (!sdpEngine.getEngineVersion().matches(CODE_REX)){
            throw new ApplicationException(ResponseCode.ENGINE_VERSION_ILLEGAL);
        }*/
    }

    public List<SdpEngine> getByName(SdpEngine sdpEngine) {
        Assert.notNull(sdpEngine, "request body cannot be null");
        if (Objects.isNull(sdpEngine.getEngineName())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpEngine.getEngineName());
        }
        return this.getMapper().getByName(sdpEngine.getEngineName());
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteUser(EngineUserInfo engineUserInfo) {
        if (Objects.isNull(engineUserInfo.getId()) || 0 == engineUserInfo.getId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, engineUserInfo.getId());
        }
        if (Objects.isNull(engineUserInfo.getEngineId()) || 0 == engineUserInfo.getEngineId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, engineUserInfo.getEngineId());
        }
        SdpEngineUser sdpEngineUser = new SdpEngineUser();
        sdpEngineUser.setEngineId(engineUserInfo.getEngineId());
        sdpEngineUser.setUserId(engineUserInfo.getId());
        SdpEngineUser engineUser = engineUserService.getMapper().selectIsExist(sdpEngineUser);
        if (Objects.isNull(engineUser)) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, "用户没有关联引擎");
        }
        return engineUserService.disable(SdpEngineUser.class,engineUser.getId());
    }

    public List<SdpProjectResp> getProject4Engine(SdpEngine sdpEngine) {
        if (Objects.isNull(sdpEngine.getId())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpEngine.getId());
        }
        return sdpProjectMapper.getProject4Engine(sdpEngine.getId());
    }

    public Object engineQueues(ClusterInfoBO clusterInfoBO) {
        String format = "%s/ws/v1/cluster/scheduler";
        SdpEngine engine = new SdpEngine();
        engine.setEngineType(clusterInfoBO.getEngineType());
        engine.setEngineCluster(clusterInfoBO.getClusterCode());
        engine.setUatEngineCluster(clusterInfoBO.getClusterCode());

        ClusterInfo clusterInfo = getClusterInfo(clusterInfoBO.getEnv(), engine);
        try {
            if (EngineTypeEnum.KUBERNETES.getType().equalsIgnoreCase(clusterInfoBO.getEngineType())) {
                //k8s namespace
                List<YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo> quenueInfos = Lists.newArrayList();
                for (String namespace : clusterInfo.getNamespaces()) {
                    YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo quenueInfo = new YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo();
                    quenueInfo.setQueueName(namespace);
                    quenueInfos.add(quenueInfo);
                }
                return quenueInfos;
            } else {
                //yarn queue
                if (Objects.nonNull(clusterInfo) && StrUtil.isNotBlank(clusterInfo.getHadoopConfDir())) {
                    String hadoopConfDir = clusterInfo.getHadoopConfDir();
                    String schedulerUrl = String.format(format, HadoopUtils.getRMWebAppURL(true, hadoopConfDir));
                    YarnSchedulerInfo yarnSchedulerInfo = com.chitu.bigdata.sdp.utils.HadoopUtils.httpGetDoResult(schedulerUrl, YarnSchedulerInfo.class);
                    if (Objects.nonNull(yarnSchedulerInfo)) {
                        ArrayList<YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo> queue = yarnSchedulerInfo.getScheduler().getSchedulerInfo().getQueues().getQueue();
                        return queue;
                    }
                }
            }
        } catch (Exception e) {
            throw new ApplicationException(ResponseCode.ENGINE_QUEUE_FAULT);
        }
        return null;
    }

    /**
     * 每个任务需要的资源计算公式：
     * 并行度/slots个数 = TM个数
     * 内存大小 = TM个数 * 每个TM的内存大小 + JM内存大小
     * CPU = TM个数 * slot个数 + JM CPU个数
     *
     * 检查yarn资源是否足够来启动当前job
     * @param jobId
     * @return
     */
    public ResourceValidateResp checkResource(Long jobId) {
        ResourceValidateResp validateResp = new ResourceValidateResp();
        //增加一个开关，防止生产和开发测试环境参数不一致导致提交不了任务，可以通过临时关闭资源校验
        SysconfigBO sysconfigBO = sdpSysConfigService.querySysoper();

        if (sysconfigBO.getResourceValidate()) {
            validateResp = checkJobResource(jobId);
        }
        return validateResp;
    }

    private ResourceValidateResp checkJobResource(Long jobId) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        ResourceValidateResp validateResp = new ResourceValidateResp();

        String errMsg = "当前作业预计所需资源: CPU：%s vcore 内存：%s GB\r\n当前队列参考剩余资源：CPU: %s vcore 内存: %s GB\r\n总资源剩余可用：CPU：%s vcore 内存：%s GB";
        QueueAndSourceConfig queueAndSourceConfig = getMapper().getQueueAndSourceConfig(jobId);

        log.info("环境: {},查询引擎队列信息：{}",env,Objects.nonNull(queueAndSourceConfig)? JSON.toJSONString(queueAndSourceConfig):"null");
        String xQueue = EnvironmentEnum.UAT.getCode().equals(env)?queueAndSourceConfig.getUatEngineQueue():queueAndSourceConfig.getEngineQueue();

        if(Objects.isNull(queueAndSourceConfig) || StrUtil.isBlank(xQueue)){
            throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG,"作业引擎配置不存在");
        }

        String hadoopConfDir = jobService.getHadoopConfDir(jobId);

        //请求获取队列资源
        ArrayList<YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo> quenueInfos = getYarnQueue(hadoopConfDir);

        //请求获取集群资源
        YarnClusterMetrics.ClusterMetrics clusterMetric = getYarnClusterMetrics(hadoopConfDir);

        Map<String, YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo> quenueInfoMap = Optional.ofNullable(quenueInfos).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo::getQueueName, m -> m, (k1, k2) -> k2));
        YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo quenueInfo = quenueInfoMap.get(xQueue);
        if(Objects.isNull(quenueInfo)){
            throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG,"["+xQueue+"]该引擎队列不存在");
        }

        //1.集群资源
        //yarn集群总核数
        Integer clusterTotalVCores = Optional.ofNullable(clusterMetric).map(m -> m.getTotalClusterResourcesAcrossPartition()).map(m -> m.getVCores()).orElse(0);
        //yarn集群内存
        Integer clusterTotalMen = Optional.ofNullable(clusterMetric).map(m -> m.getTotalClusterResourcesAcrossPartition()).map(m -> m.getMemory()).orElse(0);
        //yarn集群已使用核数
        Integer clusterUsedVCores = Optional.ofNullable(clusterMetric).map(m -> m.getTotalUsedResourcesAcrossPartition()).map(m -> m.getVCores()).orElse(0);
        //yarn集群已使用内存
        Integer clusterUsedMen = Optional.ofNullable(clusterMetric).map(m -> m.getTotalUsedResourcesAcrossPartition()).map(m -> m.getMemory()).orElse(0);
        Integer availableClusterVCores = clusterTotalVCores - clusterUsedVCores;
        BigDecimal availableClusterMem = MathUtils.div(BigDecimal.valueOf(clusterTotalMen - clusterUsedMen), CommonConstant._1024, 2);
        log.info("jobId:{} -> yarnCluster资源 " +
                        "clusterTotalVCores:{}," +
                        "clusterUsedVCores:{}," +
                        "clusterTotalMen:{}MB," +
                        "clusterUsedMen:{}MB," +
                        "availableClusterVCores:{}," +
                        "availableClusterMem:{}GB",
                jobId,
                clusterTotalVCores,
                clusterUsedVCores,
                clusterTotalMen,
                clusterUsedMen,
                availableClusterVCores,
                availableClusterMem);

        //2.JobManager资源检查
        //每个JM的内存
        int needJmMem = Integer.parseInt(queueAndSourceConfig.getJobManagerMem());
        //如果JM内存是1G的话，yarn资源是2G；非1G需要和容器最小内存比较，如果小于容器最小内存则取容器的最小内存，否则取配置的内存。
        ClusterInfoConfig.YarnConfig yarnConfig = clusterInfoConfig.getEnvMap().get(env);
        needJmMem = 1 == needJmMem ? 2 : (Math.max(needJmMem, yarnConfig.getDefaultConfig().getContainer().getMinMem()));
        //JM需要多少CPU,默认是1，目前是写死的
        Integer needJmVCores = 1;
        //JM需要多少内存
        //Integer needJmMem = 0 == Integer.valueOf(memPerJm) % 2 ? memPerJm : memPerJm + 1;

        //队列中JM能用的最大CPU数
//        Integer maxAmVCores= getResource(quenueInfo.getAmResourceLimit(),0);
        //队列中JM能用的最大CPU数【每个用户】
        Integer maxAmVCores4User = getResource(quenueInfo.getUserAmResourceLimit(), 0);
        //队列中JM已使用的CPU数
        Integer usedAmVCores = getResource(quenueInfo.getUsedAMResource(), 0);
        //队列中JM能用的最大内存
//        Integer maxAmMem= getResource(quenueInfo.getAmResourceLimit(),1);
        //队列中JM能用的最大内存【每个用户】
        Integer maxAmMem4User = getResource(quenueInfo.getUserAmResourceLimit(), 1);
        //队列中JM已使用的内存
        Integer usedAmMem = getResource(quenueInfo.getUsedAMResource(), 1);

        //队列中JM可用的CPU数
        Integer availableAmVCores = maxAmVCores4User - usedAmVCores;
        Integer availableAmVCores1 = availableAmVCores;
        //需要和集群资源比较 队列资源和集群资源比较，取最小的一方
        availableAmVCores = Math.min(availableAmVCores, availableClusterVCores);

        //队列中JM可用的内存
        BigDecimal availableAmMemBd = MathUtils.div(BigDecimal.valueOf(maxAmMem4User - usedAmMem), CommonConstant._1024, 2);
        Double availableAmMem1 = availableAmMemBd.doubleValue();
        //队列资源和集群资源比较，取最小的一方
        Double availableAmMem = Math.min(availableAmMemBd.doubleValue(), availableClusterMem.doubleValue());

        log.info("jobId:{} -> JobManager资源 " +
                        "maxAmVCores:{}," +
                        "usedAmVCores:{}," +
                        "maxAmMem:{}MB," +
                        "usedAmMem:{}MB," +
                        "availableAmVCores:{}," +
                        "availableAmMem:{}GB," +
                        "needJmVCores:{}," +
                        "needJmMem:{}GB",
                jobId,
                maxAmVCores4User,
                usedAmVCores,
                maxAmMem4User,
                usedAmMem,
                availableAmVCores1,
                availableAmMem1,
                needJmVCores,
                needJmMem);
        if (needJmVCores > availableAmVCores || Double.valueOf(needJmMem) > availableAmMem) {
            errMsg = String.format(errMsg,
                    needJmVCores,
                    needJmMem,
                    availableAmVCores1,
                    availableAmMem1,
                    availableClusterVCores,
                    availableClusterMem
            );
//            throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG,errMsg);
            validateResp.setSuccess(false);
            CheckConfigProperties.CheckConfig configs = checkConfigProperties.getEnvMap().get(env);
            validateResp.setNotice("资源不足,请跨声联系：" + configs.getResource().getNotice());
            validateResp.setTask(new ResourceMemAndCpu(needJmVCores, needJmMem));
            validateResp.setQueue(new ResourceMemAndCpu(availableAmVCores1, Integer.valueOf(availableAmMem1.intValue())));
            validateResp.setCluster(new ResourceMemAndCpu(availableClusterVCores, Integer.valueOf(availableClusterMem.intValue())));
            log.info("============JM资源不足============");
            return validateResp;
        }

        //3.TaskManager资源检查
        //每个TM的slot数量
        String slotNumStrPerTm = jobService.getSlotNum(queueAndSourceConfig.getConfigContent());
        Double slotNumPerTm = Double.valueOf(slotNumStrPerTm);
        //TM数量
        Double tmNum = Math.ceil(Double.valueOf(queueAndSourceConfig.getParallelism()) / slotNumPerTm);
        //每个TM需要的内存
        Integer memPerTm = Integer.valueOf(queueAndSourceConfig.getTaskManagerMem());

        //TM需要多少CPU
        Double needTmVCores = tmNum * slotNumPerTm;
        //需要和容器最小核数比较，如果小于容器最小核数则取容器的最小核数，否则取配置的计算出来的核数。
        needTmVCores = Math.max(needTmVCores, yarnConfig.getDefaultConfig().getContainer().getMinVcores());
        //JM需要多少内存
        Double needTmMen = tmNum * memPerTm;
        //需要和容器最小内存比较，如果小于容器最小内存则是容器的最小内存，否则是配置的内存。
        needTmMen = Math.max(needTmMen, yarnConfig.getDefaultConfig().getContainer().getMinMem());
        //Double needTmMen = tmNum * memPerTm + (memPerTm % 2 ==0?0:tmNum);

        //队列中能使用的最大CPU数
        Integer maxTotalVCores = getResource(quenueInfo.getMaxEffectiveCapacity(), 0);
        //队列中已使用CPU数
        Integer usedVCores = getResource(quenueInfo.getResourcesUsed(), 0);
        //队列中能使用的最大内存
        Integer maxTotalMem = getResource(quenueInfo.getMaxEffectiveCapacity(), 1);
        //队列中已使用内存
        Integer usedMem = getResource(quenueInfo.getResourcesUsed(), 1);

        //队列中TM可用的CPU(队列中最大的核数 - 已经使用的核数)
        Integer availableTmVCores = maxTotalVCores - usedVCores;
        Integer availableTmVCores1 = availableTmVCores;
        //队列资源和集群资源比较，取最小的一方
        availableTmVCores = Math.min(availableTmVCores, availableClusterVCores);

        //队列中TM可用的内存 队列资源和集群资源比较，取最小的一方
        BigDecimal availableTmMemBd = MathUtils.div(BigDecimal.valueOf(maxTotalMem - usedMem), CommonConstant._1024, 2);
        Double availableTmMem1 = availableTmMemBd.doubleValue();
        //队列资源和集群资源比较，取最小的一方
        Double availableTmMem = Math.min(availableTmMemBd.doubleValue(), availableClusterMem.doubleValue());

        log.info("jobId:{} -> TaskManager资源 " +
                        "maxTotalVCores:{}," +
                        "usedVCores:{}," +
                        "maxTotalMem:{}MB," +
                        "usedMem:{}MB," +
                        "availableTmVCores:{}," +
                        "availableTmMem:{}GB," +
                        "needTmVCores:{}," +
                        "needTmMen:{}GB",
                jobId,
                maxTotalVCores,
                usedVCores,
                maxTotalMem,
                usedMem,
                availableTmVCores1,
                availableTmMem1,
                needTmVCores,
                needTmMen);
        if (needTmVCores > availableTmVCores || needTmMen > availableTmMem) {
            errMsg = String.format(errMsg,
                    needTmVCores,
                    needTmMen,
                    availableTmVCores1,
                    availableTmMem1,
                    availableClusterVCores,
                    availableClusterMem
            );
//            throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG,errMsg);
            validateResp.setSuccess(false);
            CheckConfigProperties.CheckConfig configs = checkConfigProperties.getEnvMap().get(env);
            validateResp.setNotice("资源不足,请跨声联系：" + configs.getResource().getNotice());
            validateResp.setTask(new ResourceMemAndCpu(Integer.valueOf(needTmVCores.intValue()), Integer.valueOf(needTmMen.intValue())));
            validateResp.setQueue(new ResourceMemAndCpu(availableTmVCores1, Integer.valueOf(availableTmMem1.intValue())));
            validateResp.setCluster(new ResourceMemAndCpu(availableClusterVCores, Integer.valueOf(availableClusterMem.intValue())));
            log.info("============TM资源不足============");
            return validateResp;
        }

        //当TM和JM都满足时，也需要返回集群资源信息
        validateResp.setTask(new ResourceMemAndCpu(Integer.valueOf(needTmVCores.intValue() + needJmVCores), Integer.valueOf(needTmMen.intValue() + needJmMem)));
        validateResp.setQueue(new ResourceMemAndCpu(availableTmVCores1 + availableAmVCores1, Integer.valueOf(availableTmMem1.intValue() + availableAmMem1.intValue())));
        validateResp.setCluster(new ResourceMemAndCpu(availableClusterVCores, Integer.valueOf(availableClusterMem.intValue())));
        return validateResp;
    }

    /**
     * 获取资源信息
     * @param resourceMemAndCpu
     * @param type 0：cpu  !0: mem
     * @return
     */
    private Integer getResource(ResourceMemAndCpu resourceMemAndCpu, int type) {
        if (Objects.isNull(resourceMemAndCpu)) {
            return CommonConstant.ZERO_FOR_INT;
        }
        if (CommonConstant.ZERO_FOR_INT == type) {
            //cpu
            return Optional.ofNullable(resourceMemAndCpu.getVCores()).orElse(CommonConstant.ZERO_FOR_INT);
        } else {
            //内存
            return Optional.ofNullable(resourceMemAndCpu.getMemory()).orElse(CommonConstant.ZERO_FOR_INT);
        }

    }

    /**
     * 请求yarn获取队列
     * @return
     */
    public ArrayList<YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo> getYarnQueue(String hadoopConfDir) {
        String format = "%s/ws/v1/cluster/scheduler";
        String schedulerUrl = String.format(format, HadoopUtils.getRMWebAppURL(true, hadoopConfDir));
        //String schedulerUrl = String.format(format,"http://szzb-bg-uat-etl-07:8088/");
        ArrayList<YarnSchedulerInfo.Scheduler.SchedulerInfo.Queues.QuenueInfo> quenueInfos = null;
        try {
            YarnSchedulerInfo yarnSchedulerInfo = restTemplate.getForObject(schedulerUrl, YarnSchedulerInfo.class);
            quenueInfos = yarnSchedulerInfo.getScheduler().getSchedulerInfo().getQueues().getQueue();
        } catch (Exception e) {
            log.error("请求yarn获取队列出错", e);
            throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG, "请求yarn获取队列失败");
        }
        return quenueInfos;
    }

    /**
     * 请求yarn获取集群资源
     * @return
     */
    public com.chitu.bigdata.sdp.api.flink.yarn.YarnClusterMetrics.ClusterMetrics getYarnClusterMetrics(String hadoopConfDir) {
        //请求获取集群资源
        String metricsUrl = "%s/ws/v1/cluster/metrics";
        metricsUrl = String.format(metricsUrl, HadoopUtils.getRMWebAppURL(true, hadoopConfDir));
        YarnClusterMetrics.ClusterMetrics clusterMetrics = null;
        try {
            YarnClusterMetrics yarnClusterMestrics = restTemplate.getForObject(metricsUrl, YarnClusterMetrics.class);
            clusterMetrics = yarnClusterMestrics.getClusterMetrics();
        } catch (Exception e) {
            log.error("请求yarn获取集群资源出错", e);
            throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG, "请求yarn获取集群资源失败");
        }
        return clusterMetrics;
    }

    /**
     * 根据环境和引擎类型获取相应的引擎信息集合
     * @param env
     * @param engineType
     * @return
     */
    public List<ClusterInfo> getClusterInfos(String env, String engineType) {
        //String env = sdpConfig.getEnvFromEnvHolder(log);
        if (EngineTypeEnum.YARN.getType().equalsIgnoreCase(engineType)) {
            //yarn
            return clusterInfoConfig.getEnvMap().get(env).getClusters();
        } else {
            //k8s
            return kubernetesClusterConfig.getEnvMap().get(env).getClusters();
        }
    }

    /**
     * 兼容上线时 不同引擎类型切换 状态同步使用
     * @param env
     * @return
     */
    public List<ClusterInfo> getClusterInfos(String env) {
        List<ClusterInfo> clustersAll = clusterInfoConfig.getEnvMap().get(env).getClusters();
        clustersAll.addAll(kubernetesClusterConfig.getEnvMap().get(env).getClusters());
        return clustersAll;
    }


    /**
     * 根据环境和相关引擎信息获取单个集群信息
     * @param env
     * @param engine
     * @return
     */
    public ClusterInfo getClusterInfo(String env, SdpEngine engine) {
        Preconditions.checkArgument(StrUtil.isNotBlank(env), "环境变量不能为空");
        Preconditions.checkArgument(Objects.nonNull(engine), "引擎不能为空");
        Preconditions.checkArgument(StrUtil.isNotBlank(engine.getEngineType()), "引擎类型不能为空");
        Preconditions.checkArgument(StrUtil.isNotBlank(engine.getEngineCluster()) && StrUtil.isNotBlank(engine.getUatEngineCluster()), "集群编码不能为空");

        //根据env和引擎类型获取相应的集群信息
        List<ClusterInfo> clusterInfos = getClusterInfos(env, engine.getEngineType());

        //根据环境获取不同的引擎集群code
        String engineCluster = "";
        if (EnvironmentEnum.UAT.getCode().equalsIgnoreCase(env)) {
            //uat环境
            engineCluster = engine.getUatEngineCluster();
        } else {
            //prod环境
            engineCluster = engine.getEngineCluster();
        }

        String finalEngineCluster = engineCluster;
        Optional<ClusterInfo> first = clusterInfos.stream().filter(x -> x.getClusterCode().equals(finalEngineCluster)).findFirst();
        return first.isPresent() ? first.get() : null;
    }
}