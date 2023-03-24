package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.bo.UserSearchBO;
import com.chitu.bigdata.sdp.api.domain.NoticeUser;
import com.chitu.bigdata.sdp.api.enums.NotifiUserType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpProjectUser;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.mapper.SdpUserMapper;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.ContextUtils;
import com.google.common.collect.Lists;
import com.xiaoleilu.hutool.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/12 19:28
 */
@Service
@Slf4j
public class UserService extends GenericService<SdpUser, Long> {
    public UserService(@Autowired SdpUserMapper sdpUserMapper) {
        super(sdpUserMapper);
    }

    public SdpUserMapper getMapper() {
        return (SdpUserMapper) super.genericMapper;
    }

    @Autowired
    ProjectUserService projectUserService;


    @Autowired
    private SdpUserMapper userMapper;
    @Autowired
    private SdpSysConfigService sdpSysConfigService;


    public SdpUser queryUser(String employeeNumber) {
        return userMapper.queryUser(employeeNumber);
    }

    public void getUserDetail(Pagination<SdpUser> pagination) {
        this.executePagination(p -> getMapper().searchPage(p), pagination);
    }

    public List<SdpUser> getCondition(UserSearchBO userBO) {

        return this.getMapper().getCondition(userBO.getNameOrNumber());
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseCode insertUser(SdpUser user) {
        String userId = ContextUtils.get().getUserId();
        SdpUser sdpUser = this.getMapper().selectIsExist(user);
        if(null != sdpUser){
            throw new ApplicationException(ResponseCode.ADD_ALL_FAIL);
        }
        user.setCreatedBy(userId);
        user.setCreationDate(new Timestamp(System.currentTimeMillis()));
        this.insert(user);
        return ResponseCode.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseCode updateUser(SdpUser user) {
        String userId = ContextUtils.get().getUserId();
        user.setUpdatedBy(userId);
        user.setUpdationDate(new Timestamp(System.currentTimeMillis()));
        this.update(user);
        return ResponseCode.SUCCESS;
    }



    public Object delete(SdpUser user) {
        if (null == user || null == user.getId() || 0L == user.getId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, user.getId());
        }
//        user.setEnabledFlag(0l);
        return delete(user.getId());
    }



    public List<SdpUser> queryListByNumbers(List<String> list) {
        if(CollectionUtil.isNotEmpty(list)){
            return userMapper.queryListByNumbers(list);
        }else {
            return  new ArrayList<SdpUser>();
        }
    }




    /**
     * 查询项目管理员+通知名单用户
     *
     * @param projectId
     * @return
     */
    public List<SdpUser> queryReceiveUserList(Long projectId, NotifiUserType notifiUserType) {
        List<SdpUser> users = Lists.newArrayList();

        //项目管理员
        List<SdpProjectUser> projectUserList = projectUserService.selectAll(new SdpProjectUser(projectId, 1));
        if (CollectionUtil.isNotEmpty(projectUserList)) {
            List<SdpUser> sdpUsers = this.getByIds(projectUserList.stream().map(SdpProjectUser::getUserId).toArray(Long[]::new));
            if(CollectionUtil.isNotEmpty(sdpUsers)){
                users.addAll(sdpUsers);
            }
        }

        //系统配置的通知名单
        if(Objects.nonNull(notifiUserType)){
            List<NoticeUser> noticeUsers = sdpSysConfigService.getValListByKey(notifiUserType.name(), NoticeUser.class);
            List<SdpUser> mNoticeUsers = Optional.ofNullable(noticeUsers).orElse(new ArrayList<>()).stream().map(m -> {
                SdpUser sdpUser = new SdpUser();
                sdpUser.setEmployeeNumber(m.getEmployeeNumber());
                sdpUser.setUserName(m.getUserName());
                return sdpUser;
            }).collect(Collectors.toList());

            if(CollectionUtil.isNotEmpty(mNoticeUsers)){
                List<SdpUser> sdpUsers = queryListByNumbers(mNoticeUsers.stream().map(SdpUser::getEmail).collect(Collectors.toList()));
                users.addAll(sdpUsers);
            }
        }

        //根据employeeNumber去重
        return users.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SdpUser::getEmployeeNumber))), ArrayList::new));
    }



    public List<SdpUser> queryReceiveUserList(NotifiUserType notifiUserType) {
        List<SdpUser> users = Lists.newArrayList();

        //系统配置的通知名单
        if(Objects.nonNull(notifiUserType)){
            List<NoticeUser> noticeUsers = sdpSysConfigService.getValListByKey(notifiUserType.name(), NoticeUser.class);
            List<SdpUser> mNoticeUsers = Optional.ofNullable(noticeUsers).orElse(new ArrayList<>()).stream().map(m -> {
                SdpUser sdpUser = new SdpUser();
                sdpUser.setEmployeeNumber(m.getEmployeeNumber());
                sdpUser.setUserName(m.getUserName());
                return sdpUser;
            }).collect(Collectors.toList());

            if(CollectionUtil.isNotEmpty(mNoticeUsers)){
                List<SdpUser> sdpUsers = queryListByNumbers(mNoticeUsers.stream().map(SdpUser::getEmail).collect(Collectors.toList()));
                users.addAll(sdpUsers);
            }
        }

        //根据employeeNumber去重
        return users.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SdpUser::getEmployeeNumber))), ArrayList::new));
    }


}
