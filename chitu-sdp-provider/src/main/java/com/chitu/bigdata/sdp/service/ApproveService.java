

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-10
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.SdpApproveBO;
import com.chitu.bigdata.sdp.api.bo.SdpFileBO;
import com.chitu.bigdata.sdp.api.domain.DataStreamConfig;
import com.chitu.bigdata.sdp.api.enums.ApproveNotifyType;
import com.chitu.bigdata.sdp.api.enums.ApproveStatus;
import com.chitu.bigdata.sdp.api.enums.FileType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.api.vo.SdpApproveVO;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.mapper.*;
import com.chitu.bigdata.sdp.service.notify.ApproveNotifyService;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import com.chitu.cloud.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <pre>
 * 业务类
 * </pre>
 */
@Slf4j
@Service
public class ApproveService extends GenericService<SdpApprove, Long> {
    public ApproveService(@Autowired SdpApproveMapper sdpApproveMapper) {
        super(sdpApproveMapper);
    }
    
    public SdpApproveMapper getMapper() {
        return (SdpApproveMapper) super.genericMapper;
    }

    @Autowired
    SdpConfig sdpConfig;
    @Autowired
    private FileService fileService;
    @Autowired
    private SdpMetaTableConfigMapper metaTableConfigMapper;
    @Autowired
    private SdpUserMapper userMapper;
    @Autowired
    private SdpJarMapper jarMapper;
    @Autowired
    private ApproveNotifyService approveNotifyService;
    @Autowired
    private SdpProjectMapper sdpProjectMapper;
    @Autowired
    private SdpSysConfigService sdpSysConfigService;
    @Autowired
    private SdpMetaTableRelationMapper tableRelationMapper;

    public SdpApprove submitApply(SdpApproveBO approveBO) {
        SdpApprove approve = new SdpApprove();
        Context context = ContextUtils.get();
//        Yaml yaml = new Yaml();
//        InputStream input = ApproveService.class.getClassLoader().getResourceAsStream("bootstrap.yml");
//        Map<String, Object> map = yaml.loadAs(input, Map.class);
//        String env = ((Map<String, Object>)map.get("tracer")).get("env").toString();
        String env = sdpConfig.getEnvFromEnvHolder(log);
        boolean isUatOrDev =  StringUtils.isNotEmpty(env) && (env.equals("uat") || env.equals("dev"));
        if (log.isTraceEnabled()) {
            log.trace("====审批====[{}]项目，环境：[{}] -> {} {}" ,approveBO.getProjectName(),env,isUatOrDev,!approveBO.getProjectName().equals("状态转移测试") && !approveBO.getProjectName().equals("引擎引用测试"));
        }
        if(isUatOrDev){
            //uat/dev无需审批，直接上线
            if(!approveBO.getProjectName().equals("状态转移测试") && !approveBO.getProjectName().equals("引擎引用测试")){
                SdpFileBO fileBO = new SdpFileBO();
                fileBO.setId(Long.valueOf(approveBO.getId()));
                fileBO.setRemark(approveBO.getRemark());
                fileBO.setUserId(Long.valueOf(context.getUserId()));
                fileService.online(fileBO);
                return approve;
            }
        }

        SdpUser user = userMapper.selectById(Long.valueOf(context.getUserId()));

        /*SdpUser sdpUser4Param = new SdpUser();
        sdpUser4Param.setId(Long.valueOf(context.getUserId()));
        sdpUser4Param.setIsAdmin(1);
        SdpUser adminUser = userMapper.selectIsExist(sdpUser4Param);
        if(Objects.nonNull(adminUser) && StrUtil.isNotBlank(adminUser.getEmployeeNumber()) && adminUser.getEmployeeNumber().equals(user.getEmployeeNumber())){
            //如果当前人员是系统管理员无需审批，直接上线
            SdpFileBO fileBO = new SdpFileBO();
            fileBO.setId(Long.valueOf(approveBO.getId()));
            fileBO.setRemark(approveBO.getRemark());
            fileBO.setUserId(Long.valueOf(context.getUserId()));
            fileService.online(fileBO);
            return approve;
        }

        List<Whitelist> whitelists = sdpSysConfigService.queryWhitelist();
        List<String> whitelistAccounts = Optional.ofNullable(whitelists).orElse(new ArrayList<>()).stream().map(m -> m.getEmployeeNumber()).collect(Collectors.toList());
        if(whitelistAccounts.contains(user.getEmployeeNumber())){
            //如果当前人员是白名单人员无需审批，直接上线
            SdpFileBO fileBO = new SdpFileBO();
            fileBO.setId(Long.valueOf(approveBO.getId()));
            fileBO.setRemark(approveBO.getRemark());
            fileBO.setUserId(Long.valueOf(context.getUserId()));
            fileService.online(fileBO);
            return approve;
        }*/



        SdpFile file = fileService.get(Long.valueOf(approveBO.getId()));
        if (!sdpSysConfigService.isNeedTwoApprove()) {
            if (log.isTraceEnabled()){
                log.trace("只有一级审批");
            }
            //全部不需要二级审批
            SdpFile param = new SdpFile();
            param.setId(file.getId());
            param.setNeed2Approve(false);
            fileService.updateApproveFlag(param);

            approve.setFileId(file.getId());
            approve.setJobName(file.getFileName());
            approve.setProjectId(file.getProjectId());
            approve.setProjectName(approveBO.getProjectName());
            approve.setStatus(ApproveStatus.PENDING.toString());
            approve.setRemark(approveBO.getRemark());
            List<SdpUser> users =  userMapper.getProjectLeaders(file.getProjectId());
            List<String> userNames = new ArrayList<>();
            if(!CollectionUtils.isEmpty(users)){
                userNames = users.stream().map(x->x.getUserName()).collect(Collectors.toList());
                approve.setApprover(userNames.toString().replace("[","").replace("]","").replace(" ",""));
            }
            //查询是否存在申请记录，如果存在，则覆盖
            SdpApprove approve1 = this.getMapper().getApply(approve);
            approve.setCreatedBy(context.getUserId());
            approve.setCreationDate(new Timestamp(System.currentTimeMillis()));

            boolean isNeedSend = false;
            if(userNames.contains(user.getUserName())){
                //如果申请人就是一级审批人，则跳过一级审批
                approve.setApprover("system");
                approve.setApprover2("system");
                approve.setUpdatedBy("system");
                approve.setUpdatedBy2("system");
                approve.setStatus(ApproveStatus.AGREE.toString());
                approve.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                approve.setUpdationDate2(new Timestamp(System.currentTimeMillis()));

                //免一级，则直接上线
                SdpFileBO fileBO = new SdpFileBO();
                fileBO.setId(approve.getFileId());
                fileBO.setRemark(approve.getRemark());
                fileBO.setUserId(Long.valueOf(approve.getCreatedBy()));
                SdpJob job = fileService.online(fileBO);
                if(job == null){
                    throw new ApplicationException(ResponseCode.APPROVE_ONLINE_EXCEPTION);
                }
                isNeedSend = false;
            }else {
                isNeedSend = true;
            }

            if(approve1 != null){
                if(StringUtils.isNotEmpty(approve1.getUpdatedBy())){
                    throw new ApplicationException(ResponseCode.APPLY_EXISTS_EXCEPTION);
                }else{
                    approve.setId(approve1.getId());
                    this.getMapper().myUpdate(approve);
                }
            }else{
                this.getMapper().insert(approve);
            }


            if(isNeedSend){
                //发送跨声消息给对应的审批人
                List<String> userMunList = users.stream().map(x -> x.getEmployeeNumber()).collect(Collectors.toList());
                approveNotifyService.notifySend(userMunList,approve,ApproveNotifyType.SUBMIT.getType());
            }

            return approve;
        }

        if(FileType.DATA_STREAM.getType().equals(file.getFileType())){
            //FileType为DS的话是需要2级审批的
            //上线完成之后，将need2Approve标识清空
            SdpFile param = new SdpFile();
            param.setId(file.getId());
            param.setNeed2Approve(true);
            fileService.updateApproveFlag(param);
            file = fileService.get(Long.valueOf(approveBO.getId()));
        }

        approve.setFileId(file.getId());
        approve.setJobName(file.getFileName());
        approve.setProjectId(file.getProjectId());
        approve.setProjectName(approveBO.getProjectName());
        approve.setStatus(ApproveStatus.PENDING.toString());
        approve.setRemark(approveBO.getRemark());
        List<SdpUser> users =  userMapper.getProjectLeaders(file.getProjectId());
        List<String> userNames = new ArrayList<>();
        if(!CollectionUtils.isEmpty(users)){
            userNames = users.stream().map(x->x.getUserName()).collect(Collectors.toList());
            approve.setApprover(userNames.toString().replace("[","").replace("]","").replace(" ",""));
        }
        //查询是否存在申请记录，如果存在，则覆盖
        SdpApprove approve1 = this.getMapper().getApply(approve);
        approve.setCreatedBy(context.getUserId());
        approve.setCreationDate(new Timestamp(System.currentTimeMillis()));
        List<SdpUser> admins = null;
        if(userNames.contains(user.getUserName())){
            //如果申请人就是一级审批人，则跳过一级审批
            admins = userMapper.getCondition(null);
            List<String> users1 = admins.stream().map(x->x.getUserName()).collect(Collectors.toList());
            approve.setApprover2(users1.toString().replace("[","").replace("]","").replace(" ",""));
            approve.setApprover("system");
            approve.setUpdatedBy("system");
            approve.setStatus(ApproveStatus.APPROVING.toString());
            approve.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            if(!file.getNeed2Approve()){
                approve.setStatus(ApproveStatus.AGREE.toString());
                approve.setApprover2("system");
                approve.setUpdatedBy2("system");
                approve.setUpdationDate2(new Timestamp(System.currentTimeMillis()));

                //免一级，二级审批，则直接上线
                SdpFileBO fileBO = new SdpFileBO();
                fileBO.setId(approve.getFileId());
                fileBO.setRemark(approve.getRemark());
                fileBO.setUserId(Long.valueOf(approve.getCreatedBy()));
                SdpJob job = fileService.online(fileBO);
                if(job == null){
                    throw new ApplicationException(ResponseCode.APPROVE_ONLINE_EXCEPTION);
                }

                //上线完成之后，将need2Approve标识清空
                SdpFile param = new SdpFile();
                param.setId(file.getId());
                param.setNeed2Approve(false);
                fileService.updateApproveFlag(param);
            }
        }
        if(approve1 != null){
            if(StringUtils.isNotEmpty(approve1.getUpdatedBy())){
                throw new ApplicationException(ResponseCode.APPLY_EXISTS_EXCEPTION);
            }else{
                approve.setId(approve1.getId());
                this.getMapper().myUpdate(approve);
            }
        }else{
            this.getMapper().insert(approve);
        }
        //发送跨声消息给对应的审批人
        if (!CollectionUtils.isEmpty(admins)){
            List<String> userMuns = admins.stream().map(x -> x.getEmployeeNumber()).collect(Collectors.toList());
            approveNotifyService.notifySend(userMuns,approve,ApproveNotifyType.SUBMIT.getType());
        }else{
            List<String> userMunList = users.stream().map(x -> x.getEmployeeNumber()).collect(Collectors.toList());
            approveNotifyService.notifySend(userMunList,approve,ApproveNotifyType.SUBMIT.getType());
        }
        return approve;
    }

    public Pagination<SdpApprove> queryApply(SdpApproveBO approveBO) {
        String currentUser = approveBO.getVo().getCurrentUser();
        SdpUser user = userMapper.selectById(Long.valueOf(currentUser));
        String type = approveBO.getVo().getType();
        if(type.equals("approve")){
            approveBO.getVo().setCurrentUser(user.getUserName());
        }
        //当开始时间等于结束时间时，结束时间加1
        String startTime = approveBO.getVo().getStartTime();
        String endTime = approveBO.getVo().getEndTime();
        if(StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime) && startTime.equals(endTime)){
            try{
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = format.parse(endTime);
                Date date1 = new Date(date.getTime() + 24 * 3600 * 1000);
                String endTime1 = format.format(date1);
                approveBO.getVo().setEndTime(endTime1);
            }catch (Exception e){
                logger.error("===查询结束时间格式化异常===");
            }
        }
        Pagination<SdpApprove> pagination = Pagination.getInstance4BO(approveBO);
        this.executePagination(p -> getMapper().queryApply(p), pagination);
        pagination.getRows().forEach(x->{
            String updatedBy = x.getUpdatedBy();
            String updatedBy2 = x.getUpdatedBy2();
            String approve = x.getApprover();
            String approve2 = x.getApprover2();
            if(StringUtils.isNotEmpty(updatedBy)){
                //一级已经批过了
                if(StringUtils.isEmpty(updatedBy2)){
                    //二级未批
                    if(StringUtils.isNotEmpty(approve2) && approve2.contains(user.getUserName())){
                        if(x.getStatus().equals(ApproveStatus.APPROVING.toString())){
                            x.setCanApprove(true);
                        }else{
                            x.setCanApprove(false);
                        }
                    }else{
                        x.setCanApprove(false);
                    }
                }else{
                    //二级已经批过了
                    x.setCanApprove(false);
                }
            }else {
                //一级未批
                if(StringUtils.isNotEmpty(approve) && approve.contains(user.getUserName())){
                    if(x.getStatus().equals(ApproveStatus.PENDING.toString())){
                        x.setCanApprove(true);
                    }else{
                        x.setCanApprove(false);
                    }
                }else{
                    x.setCanApprove(false);
                }
            }
        });
        return pagination;
    }

    public Integer cancelApply(SdpApproveBO approveBO) {
        SdpApprove approve = new SdpApprove();
        approve.setId(Long.valueOf(approveBO.getId()));
        approve.setStatus(ApproveStatus.CANCEL.toString());
        return this.getMapper().cancelApply(approve);
    }

    public SdpApproveVO detailApply(SdpApproveBO approveBO) {
        SdpApproveVO result = new SdpApproveVO();
        Long fileId = approveBO.getFileId();
        SdpFile file = fileService.get(fileId);
        result.setJobName(file.getFileName());
        SdpProject sdpProject = sdpProjectMapper.selectById(file.getProjectId());
        result.setProjectName(sdpProject.getProjectName());
        result.setProjectCode(sdpProject.getProjectCode());
        SdpApprove approve = this.getMapper().myGet(Long.valueOf(approveBO.getId()));
        String metaTable = "";
        String type = file.getFileType();
        if(type.equals(FileType.SQL_STREAM.getType())){
            List<SdpMetaTableConfig> metaTables = metaTableConfigMapper.queryByFileId(fileId);
            if(!CollectionUtils.isEmpty(metaTables)){
                List<String> flinkTable = metaTables.stream().map(x->x.getFlinkTableName()).collect(Collectors.toList());
                metaTable = flinkTable.toString()
                        .replace("[","")
                        .replace("]","")
                        .replace(" ","");
            }
            result.setContent(file.getContent());
            result.setFileType(FileType.SQL_STREAM.getType());
//            result.setDescription(approve.getDescription().replace("项目管理员","你"));
        }else{
            String dsConfig = file.getDataStreamConfig();
            if(StringUtils.isNotEmpty(dsConfig)){
                DataStreamConfig dsConfig1 = JSON.parseObject(dsConfig, DataStreamConfig.class);
                Long jarId = dsConfig1.getJarId();
                if(null != jarId){
                    SdpJar sdpJar = jarMapper.myGet(jarId);
                    if(null != sdpJar){
                        sdpJar.setMainClass(dsConfig1.getMainClass());
                        result.setContent(JSON.toJSONString(sdpJar));
                    }
                }
            }
            result.setFileType(FileType.DATA_STREAM.getType());
//            result.setDescription("上线申请,待你审批");
        }
        result.setDescription(approve.getRemark());
        result.setMetaTable(metaTable);
        JSONObject approveFlow = new JSONObject();
        approveFlow.put("createdBy",approve.getCreatedBy());
        approveFlow.put("creationDate",approve.getCreationDate());

        approveFlow.put("approver",approve.getApprover());
        approveFlow.put("opinion",approve.getOpinion());
        if(StringUtils.isEmpty(approve.getUpdatedBy())){
            long differ = System.currentTimeMillis() - approve.getCreationDate().getTime();
            long day = differ/(24*60*60*1000);
            long hour = (differ/(60*60*1000)-day*24);
            long min = ((differ/(60*1000))-day*24*60-hour*60);
            long s = (differ/1000-day*24*60*60-hour*60*60-min*60);
            String awaitTime = day+"天"+hour+"小时"+min+"分"+s+"秒";
            approveFlow.put("awaitTime",awaitTime);
        }else{
            approveFlow.put("updatedBy",approve.getUpdatedBy());
            approveFlow.put("updationDate",approve.getUpdationDate());
        }
        if(StringUtils.isEmpty(approve.getUpdatedBy2())){
            long differ = System.currentTimeMillis() - (approve.getUpdationDate()==null?approve.getCreationDate().getTime():approve.getUpdationDate().getTime());
            long day = differ/(24*60*60*1000);
            long hour = (differ/(60*60*1000)-day*24);
            long min = ((differ/(60*1000))-day*24*60-hour*60);
            long s = (differ/1000-day*24*60*60-hour*60*60-min*60);
            String awaitTime2 = day+"天"+hour+"小时"+min+"分"+s+"秒";
            approveFlow.put("awaitTime2",awaitTime2);
        }else{
            approveFlow.put("updatedBy2",approve.getUpdatedBy2());
            approveFlow.put("updationDate2",approve.getUpdationDate2());
        }
        approveFlow.put("approver2",approve.getApprover2());
        approveFlow.put("opinion2",approve.getOpinion2());
        if(approve.getStatus().equals(ApproveStatus.PENDING.toString())){
            //说明此时一级未审批
            approveFlow.put("status",approve.getStatus());
            approveFlow.put("status2",approve.getStatus());
        }else if(approve.getStatus().equals(ApproveStatus.APPROVING.toString())){
            //说明此时已经过了一级审批，二级未批
            approveFlow.put("status",ApproveStatus.AGREE.toString());
            approveFlow.put("status2",ApproveStatus.PENDING.toString());
        }else if(approve.getStatus().equals(ApproveStatus.AGREE.toString())){
            //此时二级已经审批
            approveFlow.put("status",approve.getStatus());
            approveFlow.put("status2",approve.getStatus());
        }else if(approve.getStatus().equals(ApproveStatus.CANCEL.toString())){
            if(StringUtils.isNotEmpty(approve.getUpdatedBy2())){
                approveFlow.put("status",ApproveStatus.AGREE.toString());
                approveFlow.put("status2",approve.getStatus());
            }else{
                approveFlow.put("status",approve.getStatus());
            }
        }else if(approve.getStatus().equals(ApproveStatus.DISAGREE.toString())){
            if(StringUtils.isNotEmpty(approve.getUpdatedBy2())){
                approveFlow.put("status",ApproveStatus.AGREE.toString());
                approveFlow.put("status2",approve.getStatus());
            }else{
                approveFlow.put("status",approve.getStatus());
            }
        }
        result.setApproveFlow(approveFlow);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Object executeApprove(SdpApproveBO approveBO) {
        Object result = null;
        String currUser = ContextUtils.get().getUserId();
        String status = approveBO.getStatus();
        SdpApprove approve = new SdpApprove();
        BeanUtils.copyProperties(approveBO,approve);
        approve.setId(Long.valueOf(approveBO.getId()));
        SdpApprove approve1 = get(approve.getId());
        approve.setRemark(approve1.getRemark());
        approve.setJobName(approve1.getJobName());
        approve.setProjectName(approve1.getProjectName());
        approve.setUpdatedBy2(approve1.getUpdatedBy2());
        approve.setCreatedBy(approve1.getCreatedBy());
        approve.setCreationDate(approve1.getCreationDate());
        if (Objects.nonNull(approve1.getUpdationDate())){
            approve.setUpdationDate(approve1.getUpdationDate());
        }
        if (Objects.nonNull(approve1.getUpdationDate2())){
            approve.setUpdationDate2(approve1.getUpdationDate2());
        }
        if(!approve1.getStatus().equals(ApproveStatus.PENDING.toString()) && !approve1.getStatus().equals(ApproveStatus.APPROVING.toString())){
            throw new ApplicationException(ResponseCode.APPROVE_STATUS_ERROR,ApproveStatus.valueOf(approve1.getStatus()).getStatus());
        }
        if(currUser.equals(approve1.getCreatedBy())){
            throw new ApplicationException(ResponseCode.APPROVER_EXCEPTION);
        }
        List<SdpUser> admins = userMapper.getCondition(null);
        List<String> users = admins.stream().map(x->x.getUserName()).collect(Collectors.toList());
        SdpUser sdpUser = userMapper.selectById(Long.valueOf(approve1.getCreatedBy()));
        SdpUser sdpUser1 = userMapper.selectById(Long.valueOf(currUser));
        ArrayList<String> list = new ArrayList<>();

        if (!sdpSysConfigService.isNeedTwoApprove()) {
            if (log.isTraceEnabled()){
                log.trace("只有一级审批");
            }
            //没有二级审批
            //此时为一级审批
            if(!approve1.getApprover().contains(sdpUser1.getUserName())){
                throw new ApplicationException(ResponseCode.APPROVE_NOT_PERMISSION);
            }

            if(status.equals(ApproveStatus.AGREE.toString())){
                //不需要二级审批
                approve.setStatus(ApproveStatus.AGREE.toString());
                approve.setApprover2("system");
                approve.setUpdatedBy2("system");
                approve.setUpdationDate2(new Timestamp(System.currentTimeMillis()));
                //一级审批完就直接上线
                SdpFileBO fileBO = new SdpFileBO();
                fileBO.setId(approveBO.getFileId());
                fileBO.setRemark(approve1.getRemark());
                fileBO.setUserId(Long.valueOf(approve1.getCreatedBy()));
                SdpJob job = fileService.online(fileBO);
                if (job == null) {
                    throw new ApplicationException(ResponseCode.APPROVE_ONLINE_EXCEPTION);
                }
            }
            approve.setUpdatedBy(currUser);
            approve.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            updateSelective(approve);

            //审核结果通知（申请人）
            list.add(sdpUser.getEmployeeNumber());
            approveNotifyService.notifySend(list,approve,ApproveNotifyType.EXECUTE.getType());
            result = approve;
            return result;
        }

        if(StringUtils.isEmpty(approve1.getUpdatedBy())){
            //此时为一级审批
            if(!approve1.getApprover().contains(sdpUser1.getUserName())){
                throw new ApplicationException(ResponseCode.APPROVE_NOT_PERMISSION);
            }
            SdpFile sdpFile = fileService.get(approveBO.getFileId());
            if(status.equals(ApproveStatus.AGREE.toString())){
                //不需要二级审批
                if(!sdpFile.getNeed2Approve()){
                    approve.setStatus(ApproveStatus.AGREE.toString());
                    approve.setApprover2("system");
                    approve.setUpdatedBy2("system");
                    approve.setUpdationDate2(new Timestamp(System.currentTimeMillis()));
                    //免二级审批，则一级审批完就直接上线
                    SdpFileBO fileBO = new SdpFileBO();
                    fileBO.setId(approveBO.getFileId());
                    fileBO.setRemark(approve1.getRemark());
                    fileBO.setUserId(Long.valueOf(approve1.getCreatedBy()));
                    SdpJob job = fileService.online(fileBO);
                    if(job == null){
                        throw new ApplicationException(ResponseCode.APPROVE_ONLINE_EXCEPTION);
                    }
                    //审批完成之后，将need2Approve标识清空
                    SdpFile param = new SdpFile();
                    param.setId(approveBO.getFileId());
                    param.setNeed2Approve(false);
                    fileService.updateApproveFlag(param);
                }else{
                    approve.setStatus(ApproveStatus.APPROVING.toString());
                    approve.setApprover2(users.toString().replace("[","").replace("]","").replace(" ",""));
                }
            }
            approve.setUpdatedBy(currUser);
            approve.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            updateSelective(approve);

            //发送跨声通知二级审核
            if(null != sdpFile.getNeed2Approve() && sdpFile.getNeed2Approve()){
                if (ApproveStatus.AGREE.name().equals(status)) {
                    List<String> userNums = admins.stream().map(x -> x.getEmployeeNumber()).collect(Collectors.toList());
                    approveNotifyService.notifySend(userNums, approve, ApproveNotifyType.SUBMIT.getType());
                }
            }
            //审核结果通知（申请人）
            list.add(sdpUser.getEmployeeNumber());
            approveNotifyService.notifySend(list,approve,ApproveNotifyType.EXECUTE.getType());
            result = approve;
        }else{
            //此时为二级审批
            if(!approve1.getApprover2().contains(sdpUser1.getUserName())){
                throw new ApplicationException(ResponseCode.APPROVE_NOT_PERMISSION);
            }
            approve.setUpdatedBy2(currUser);
            approve.setUpdationDate2(new Timestamp(System.currentTimeMillis()));
            approve.setOpinion2(approveBO.getOpinion());
            approve.setOpinion(null);
            updateSelective(approve);
            result = approve;
            if(status.equals(ApproveStatus.AGREE.toString())){
                SdpFileBO fileBO = new SdpFileBO();
                fileBO.setId(approveBO.getFileId());
                fileBO.setRemark(approve1.getRemark());
                fileBO.setUserId(Long.valueOf(approve1.getCreatedBy()));
                SdpJob job = fileService.online(fileBO);
                if(job == null){
                    throw new ApplicationException(ResponseCode.APPROVE_ONLINE_EXCEPTION);
                }
                result = job;
            }
            //审批完成之后，将need2Approve标识清空
            SdpFile param = new SdpFile();
            param.setId(approveBO.getFileId());
            param.setNeed2Approve(false);
            fileService.updateApproveFlag(param);
            //发送跨声返回审核结果
            list.add(sdpUser.getEmployeeNumber());
            approveNotifyService.notifySend(list,approve,ApproveNotifyType.EXECUTE.getType());
        }
        return result;
    }

    public Integer queryPending(SdpApproveBO approveBO) {
        SdpUser user = userMapper.selectById(Long.valueOf(approveBO.getUserId()));
        int count1 = this.getMapper().queryPending1(user.getUserName(),ApproveStatus.PENDING.toString());
        int count2 = this.getMapper().queryPending2(user.getUserName(),ApproveStatus.APPROVING.toString());
        return count1 + count2;
    }
}