

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-3-29
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.SysconfigBO;
import com.chitu.bigdata.sdp.api.domain.DataSourceWriteContrl;
import com.chitu.bigdata.sdp.api.domain.NoticeUser;
import com.chitu.bigdata.sdp.api.domain.OperMaintenance;
import com.chitu.bigdata.sdp.api.domain.WhitelistUser;
import com.chitu.bigdata.sdp.api.enums.JobAction;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpSysConfig;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.constant.SdpSysConfigConstant;
import com.chitu.bigdata.sdp.mapper.SdpSysConfigMapper;
import com.chitu.bigdata.sdp.mapper.SdpUserMapper;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <pre>
 * 系统配置表业务类
 * </pre>
 */
@Service("sdpSysConfigService")
public class SdpSysConfigService extends GenericService<SdpSysConfig, Long> {
    @Autowired
    private SdpUserMapper userMapper;

    public SdpSysConfigService(@Autowired SdpSysConfigMapper sdpSysConfigMapper) {
        super(sdpSysConfigMapper);
    }
    
    public SdpSysConfigMapper getMapper() {
        return (SdpSysConfigMapper) super.genericMapper;
    }

    public ResponseData validateAction(String action){
        ResponseData data = new ResponseData();
        SdpSysConfig result = queryByKey(SdpSysConfigConstant.OPERATION_MAINTENANCE_KEY);
        if(result != null){
            OperMaintenance operMaintenance = JSON.parseObject(result.getConfigValue(), OperMaintenance.class);
            if(action.equalsIgnoreCase(JobAction.START.toString())){
                if(operMaintenance.getStart() != 1){
//                    data.setCode(ResponseCode.ACTION_NOT_ALLOW.getCode());
//                    data.setMsg(ResponseCode.ACTION_NOT_ALLOW.getMessage());
//                    data.setData(ResponseCode.ACTION_NOT_ALLOW.getMessage());
                    throw new ApplicationException(ResponseCode.ACTION_NOT_ALLOW);
                }
            }else if(action.equalsIgnoreCase(JobAction.STOP.toString())){
                if(operMaintenance.getStop() != 1){
//                    data.setCode(ResponseCode.ACTION_NOT_ALLOW.getCode());
//                    data.setMsg(ResponseCode.ACTION_NOT_ALLOW.getMessage());
//                    data.setData(ResponseCode.ACTION_NOT_ALLOW.getMessage());
                    throw new ApplicationException(ResponseCode.ACTION_NOT_ALLOW);
                }
            }
        }
        return data;
    }

    private SdpSysConfig queryByKey(String configKey){
        SdpSysConfig sdpSysConfig = new SdpSysConfig();
        sdpSysConfig.setConfigKey(configKey);
        sdpSysConfig.setEnabledFlag(1L);
        List<SdpSysConfig> sdpSysConfigs = selectAll(sdpSysConfig);
        return  CollectionUtils.isEmpty(sdpSysConfigs)?null:sdpSysConfigs.get(0);
    }



    /**
     * 系统运维查询
     * @return
     */
    public SysconfigBO querySysoper(){
        SysconfigBO sysconfigBO = new SysconfigBO();

        //作业运维
        OperMaintenance operMaintenance = Optional.ofNullable(getValByKey(SdpSysConfigConstant.OPERATION_MAINTENANCE_KEY, OperMaintenance.class)).orElse(new OperMaintenance());
        sysconfigBO.setOperMaintenance(operMaintenance);

        //白名单
        List<WhitelistUser> valListByKey = getValListByKey(SdpSysConfigConstant.WHITE_LIST_KEY, WhitelistUser.class);
        sysconfigBO.setWhitelists(valListByKey);

        //普通通知用户
        List<NoticeUser> commonNoticeUsers = getValListByKey(SdpSysConfigConstant.COMMON_NOTICE_USERS, NoticeUser.class);
        sysconfigBO.setCommonNoticeUsers(commonNoticeUsers);

        //告警通知用户
        List<NoticeUser> alarmNoticeUsers = getValListByKey(SdpSysConfigConstant.ALARM_NOTICE_USERS, NoticeUser.class);
        sysconfigBO.setAlarmNoticeUsers(alarmNoticeUsers);

        //数据库写入控制
        List<DataSourceWriteContrl> dataSourceWriteContrls = getValListByKey(SdpSysConfigConstant.DATASOURCE_WRITE_CONTRLS, DataSourceWriteContrl.class);
        sysconfigBO.setDataSourceWriteContrls(dataSourceWriteContrls);

        //集群资源校验开关
        Boolean resourceValidate = getValByKey(SdpSysConfigConstant.RESOURCE_VALIDATE, Boolean.class);
        sysconfigBO.setResourceValidate(resourceValidate);

        //告警全局开关
        Boolean alertSwitch = getValByKey(SdpSysConfigConstant.ALERT_GLOBAL_SWITCH, Boolean.class);
        sysconfigBO.setAlertGlobalSwitch(alertSwitch);

        //告警全局开关
        Boolean needTwoApprove = getValByKey(SdpSysConfigConstant.NEED_TWO_APPROVE, Boolean.class);
        sysconfigBO.setNeedTwoApprove(needTwoApprove);

        return sysconfigBO;
    }

    /**
     * 系统运维保存
     * @param sysconfigBO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public SysconfigBO saveSysoper(SysconfigBO sysconfigBO){
        Context context = ContextUtils.get();
        String userId = Optional.ofNullable(context).map(m->m.getUserId()).orElse("");

        //作业运维保存
        this.upsertByKey(SdpSysConfigConstant.OPERATION_MAINTENANCE_KEY, sysconfigBO.getOperMaintenance(),userId);

        //白名单保存
        this.upsertByKey(SdpSysConfigConstant.WHITE_LIST_KEY, sysconfigBO.getWhitelists(),userId);

        //常规通知用户
        this.upsertByKey(SdpSysConfigConstant.COMMON_NOTICE_USERS, sysconfigBO.getCommonNoticeUsers(),userId);

        //告警通知用户
        this.upsertByKey(SdpSysConfigConstant.ALARM_NOTICE_USERS, sysconfigBO.getAlarmNoticeUsers(),userId);

        //数据库写入控制
        this.upsertByKey(SdpSysConfigConstant.DATASOURCE_WRITE_CONTRLS, sysconfigBO.getDataSourceWriteContrls(),userId);

        //集群资源校验开关
        this.upsertByKey(SdpSysConfigConstant.RESOURCE_VALIDATE, sysconfigBO.getResourceValidate(),userId);

        //告警全局开关
        this.upsertByKey(SdpSysConfigConstant.ALERT_GLOBAL_SWITCH, sysconfigBO.getAlertGlobalSwitch(),userId);

        //是否需要二级审批
        this.upsertByKey(SdpSysConfigConstant.NEED_TWO_APPROVE, sysconfigBO.getNeedTwoApprove(),userId);

        return sysconfigBO;
    }


    public boolean isNeedTwoApprove(){
        Boolean needTwoApprove = getValByKey(SdpSysConfigConstant.NEED_TWO_APPROVE, Boolean.class);
        return Optional.ofNullable(needTwoApprove).orElse(true);
    }

    public boolean isWhiteList(){
        Context context = ContextUtils.get();
        SdpUser sdpUser4Param = new SdpUser();
        sdpUser4Param.setId(Long.valueOf(context.getUserId()));
        sdpUser4Param.setIsAdmin(1);
        SdpUser adminUser = userMapper.selectIsExist(sdpUser4Param);
        if(Objects.nonNull(adminUser)){
            //系统管理员，默认就是白名单用户
           return true;
        }

        SdpUser user = userMapper.selectById(Long.valueOf(context.getUserId()));
        List<WhitelistUser> whitelists = getValListByKey(SdpSysConfigConstant.WHITE_LIST_KEY, WhitelistUser.class);
        List<String> whitelistAccounts = Optional.ofNullable(whitelists).orElse(new ArrayList<>()).stream().map(m -> m.getEmployeeNumber()).collect(Collectors.toList());
        if(whitelistAccounts.contains(user.getEmployeeNumber())){
           return true;
        }

        return false;
    }


    /**
     * 插入或者更新
     * @param key
     * @param val
     * @param userId
     */
    private void upsertByKey(String key,Object val,String userId) {
        SdpSysConfig sdpSysConfig = queryByKey(key);
        if (Objects.nonNull(sdpSysConfig)) {
            sdpSysConfig.setConfigValue(JSON.toJSONString(val));
            if (StrUtil.isNotBlank(userId)) {
                sdpSysConfig.setUpdatedBy(userId);
            }
            updateSelective(sdpSysConfig);
        }else {
            sdpSysConfig = new SdpSysConfig();
            sdpSysConfig.setConfigKey(key);
            sdpSysConfig.setEnabledFlag(1L);
            sdpSysConfig.setConfigValue(JSON.toJSONString(val));
            if (StrUtil.isNotBlank(userId)) {
                sdpSysConfig.setCreatedBy(userId);
                sdpSysConfig.setUpdatedBy(userId);
            }
            this.getMapper().insert(sdpSysConfig);
        }
    }

    /**
     * 获取某个val的具体对象
     * @param key
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public  <T> T getValByKey(String key,Class<T> clazz)  {
        SdpSysConfig sdpSysConfig = queryByKey(key);
        if (Objects.nonNull(sdpSysConfig) && StrUtil.isNotBlank(sdpSysConfig.getConfigValue())) {
            String configValue = sdpSysConfig.getConfigValue();
            return JSON.parseObject(configValue, clazz);
        }else {
            return null;
        }
    }

    /**
     * 获取某个val的具体数组
     * @param key
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public  <T> List<T> getValListByKey(String key,Class<T> clazz){
        SdpSysConfig sdpSysConfig = queryByKey(key);
        if (Objects.nonNull(sdpSysConfig) && StrUtil.isNotBlank(sdpSysConfig.getConfigValue())) {
            String configValue = sdpSysConfig.getConfigValue();
            return JSON.parseArray(configValue, clazz);
        }else {
            return  new ArrayList<T>();
        }
    }
}