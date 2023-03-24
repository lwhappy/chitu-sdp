

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import com.alibaba.fastjson.JSONArray;
import com.chitu.bigdata.sdp.api.bo.SdpProjectBO;
import com.chitu.bigdata.sdp.api.bo.SdpProjectBTO;
import com.chitu.bigdata.sdp.api.domain.ProjectCountInfo;
import com.chitu.bigdata.sdp.api.domain.ProjectUser;
import com.chitu.bigdata.sdp.api.domain.SdpEngineInfo;
import com.chitu.bigdata.sdp.api.domain.SdpProjectInfo;
import com.chitu.bigdata.sdp.api.enums.EnableType;
import com.chitu.bigdata.sdp.api.enums.PriorityLevel;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.enums.UserRole;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.api.vo.SdpProjectResp;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.mapper.SdpFileMapper;
import com.chitu.bigdata.sdp.mapper.SdpProjectMapper;
import com.chitu.bigdata.sdp.utils.PaginationUtils;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.ContextUtils;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * 项目业务类
 * </pre>
 */
@Slf4j
@Service
public class ProjectService extends GenericService<SdpProject, Long> {

    @Autowired
    ProjectEngineService projectEngineService;

    @Autowired
    EngineService engineService;

    @Autowired
    ProjectUserService projectUserService;

    @Autowired
    UserService userService;

    @Autowired
    FileService fileService;

    @Autowired
    SdpFileMapper sdpFileMapper;

    @Autowired
    SdpConfig sdpConfig;

    private static final  String SDP_APP_NAME_PREFFIX = "赤兔_%s";


    public ProjectService(@Autowired SdpProjectMapper sdpProjectMapper) {
        super(sdpProjectMapper);
    }

    public SdpProjectMapper getMapper() {
        return (SdpProjectMapper) super.genericMapper;
    }

    private String CODE_REX="([\\w-_])+";

    public Pagination<SdpProjectResp> projectList(SdpProjectBTO sdpProjectBTO) {
        String userId = ContextUtils.get().getUserId();
        sdpProjectBTO.setUserId(Long.valueOf(userId == null ? "0" : userId));
        sdpProjectBTO.setProjectName(StrUtils.changeWildcard(sdpProjectBTO.getProjectName()));
        List<SdpProject> projectInfo = this.getMapper().projectList(sdpProjectBTO);
        Pagination<SdpProject> instance = pageHelper(projectInfo, sdpProjectBTO.getPage(), sdpProjectBTO.getPageSize());
        List<SdpProjectResp> sps = new ArrayList<>();
        SdpUser sdpUser = userService.get(Long.valueOf(userId == null ? "0":userId));
        ProjectUser projectUser1 = new ProjectUser();
        BeanUtils.copyProperties(sdpUser,projectUser1);
        for (SdpProject presp : instance.getRows()) {
            SdpProjectResp sdpProjectResp = new SdpProjectResp();
            BeanUtils.copyProperties(presp, sdpProjectResp);
            boolean flag = true;
            ArrayList<ProjectUser> leaders = new ArrayList<>();
            ArrayList<ProjectUser> managers = new ArrayList<>();
            ArrayList<ProjectUser> generals = new ArrayList<>();
            List<ProjectUser> userList = presp.getUserList();
            if (CollectionUtils.isEmpty(userList)) {
                sdpProjectResp.setProjectUsers(JSONArray.toJSONString(userList));
                sdpProjectResp.setProjectOwner(JSONArray.toJSONString(userList));
            } else {
                for (ProjectUser projectUser : userList) {
                    if(StrUtil.isBlank(projectUser.getUserName())){
                        continue;
                    }
                    projectUser.setIsLeader(projectUser.getProjectLeader());
                    if (UserRole.ProjectUserEnum.RESPONSIBLE_PERSON.getCode().equals(projectUser.getProjectLeader())) {
                        projectUser.setId(projectUser.getUserId());
                        leaders.add(projectUser);
                    }
                    if (UserRole.ProjectUserEnum.ORGANIZE_USER.getCode().equals(projectUser.getProjectLeader())) {
                        projectUser.setId(projectUser.getUserId());
                        managers.add(projectUser);
                        if (Objects.nonNull(projectUser.getId()) && userId.equals(projectUser.getId().toString()) && flag){
                            sdpProjectResp.setProjectUserRole(projectUser);
                            flag = false;
                        }
                    }
                    if (UserRole.ProjectUserEnum.GENERAL_USER.getCode().equals(projectUser.getProjectLeader())) {
                        projectUser.setId(projectUser.getUserId());
                        generals.add(projectUser);
                        if (Objects.nonNull(projectUser.getId()) && userId.equals(projectUser.getId().toString()) && flag){
                            sdpProjectResp.setProjectUserRole(projectUser);
                            flag = false;
                        }
                    }
                }
                if (flag){
                    projectUser1.setProjectId(presp.getId());
                    sdpProjectResp.setProjectUserRole(projectUser1);
                }
                managers.addAll(generals);
                sdpProjectResp.setProjectOwner(JSONArray.toJSONString(leaders));
                sdpProjectResp.setProjectUsers(JSONArray.toJSONString(managers));
            }
            //处理引擎数据
            if(Objects.isNull(presp.getEngineList()) || CollectionUtils.isEmpty(presp.getEngineList())){
                sdpProjectResp.setProjectEngines(JSONArray.toJSONString(new ArrayList<String>()));
            }else{
                for (SdpEngineInfo engineInfo : presp.getEngineList()) {
                    engineInfo.setId(engineInfo.getEngineId());
                }
            }
            sdpProjectResp.setProjectEngines(JSONArray.toJSONString(presp.getEngineList()));
            sps.add(sdpProjectResp);
        }
        if (CollectionUtils.isEmpty(sps)) {
            return null;
        }
        Pagination<SdpProjectResp> paginationData = Pagination.getInstance(sdpProjectBTO.getPage(), sdpProjectBTO.getPageSize());
        //统计实际的项目数量，作业数量，用户数量
        ProjectCountInfo projectCountInfo  =  this.getMapper().allCount(sdpProjectBTO);
        paginationData.setRows(sps);
        paginationData.setRowTotal(instance.getRowTotal());
        paginationData.setPageTotal(instance.getRowTotal()% sdpProjectBTO.getPageSize() == 0 ?  instance.getRowTotal()/ sdpProjectBTO.getPageSize() : instance.getRowTotal()/ sdpProjectBTO.getPageSize()+1);
        paginationData.setCount(true);
        ProjectCountInfo projectCountInfo1 = new ProjectCountInfo();
        BeanUtils.copyProperties(paginationData, projectCountInfo1);
        projectCountInfo1.setProjectTotal(projectCountInfo.getProjectTotal());
        projectCountInfo1.setJobTotal(projectCountInfo.getJobTotal());
        projectCountInfo1.setEmployeeTotal(projectCountInfo.getEmployeeTotal());
        return projectCountInfo1;
    }
    public static Pagination<SdpProject> pageHelper(List list, Integer pageNum, Integer pageSize) {
        Pagination<SdpProject> instance = Pagination.getInstance(pageNum,pageSize);
        int total = list.size();
        instance.setRowTotal(total);
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);
        if (startIndex > endIndex) {
            instance.setRows(new ArrayList<>());
            return instance;
        } else {
            instance.setRows(list.subList(startIndex, endIndex));
            return instance;
        }
    }

    /**
     * 封装项目责任人，使用者，管理者
     * @param userId
     * @param sps
     */
    private void buildProjectUsers(String userId, List<SdpProjectResp> sps) {
        for (SdpProjectResp sp : sps) {
            Long projectId = sp.getId();
            List<SdpEngine> pes = engineService.getMapper().getProjectEngines(projectId);
            sp.setProjectEngines(JSONArray.toJSONString(pes));
            List<ProjectUser> sus = userService.getMapper().getProjectUser(projectId);
            //获取当前登录用户的角色信息
            ProjectUser mup = userService.getMapper().getUser4Project(Long.valueOf(userId == null ? "0": userId), projectId);
            ArrayList<ProjectUser> leaders = new ArrayList<>();
            ArrayList<ProjectUser> managers = new ArrayList<>();
            ArrayList<ProjectUser> generals = new ArrayList<>();
            if (CollectionUtils.isEmpty(sus)) {
                sp.setProjectUsers(JSONArray.toJSONString(sus));
                sp.setProjectOwner(JSONArray.toJSONString(sus));
            } else {
                for (ProjectUser projectUser : sus) {
                    if (UserRole.ProjectUserEnum.RESPONSIBLE_PERSON.getCode().equals(projectUser.getIsLeader())) {
                        leaders.add(projectUser);
                    }
                    if (UserRole.ProjectUserEnum.ORGANIZE_USER.getCode().equals((projectUser.getIsLeader()))) {
                        managers.add(projectUser);
                    }
                    if (UserRole.ProjectUserEnum.GENERAL_USER.getCode().equals((projectUser.getIsLeader()))) {
                        generals.add(projectUser);
                    }
                }
                managers.addAll(generals);
                sp.setProjectOwner(JSONArray.toJSONString(leaders));
                sp.setProjectUsers(JSONArray.toJSONString(managers));
            }
                sp.setProjectUserRole(mup);

        }
    }

    @Transactional(rollbackFor = Exception.class)
    public SdpProject add(SdpProjectInfo sdpProjectInfo) {
        String userId = ContextUtils.get().getUserId();
        //验证项目是否重名
        List<SdpProject> sp = this.getMapper().getByName(sdpProjectInfo.getProjectName());
        if (!CollectionUtils.isEmpty(sp)) {
            throw new ApplicationException(ResponseCode.PROJECT_REPEAT, sdpProjectInfo.getProjectName());
        }
        checkSdpProjectInfo(sdpProjectInfo);
        //校验project_code的规则和是否重复
        validateProjectCode(sdpProjectInfo.getProjectCode());
        //插入项目信息
        SdpProject sdpProject = insertProject(sdpProjectInfo, userId);
        Long projectId = sdpProject.getId();
        //插入项目引擎信息
        insertProjectEngines(sdpProjectInfo, projectId);
         //插入项目负责人和管理者信息
        insertProjectLeader(sdpProjectInfo, userId, projectId);

        List<ProjectUser> projectUsers = sdpProjectInfo.getProjectUsers();
        if (Objects.isNull(projectUsers) || CollectionUtils.isEmpty(projectUsers)) {
            return sdpProject;
        } else {
            //插入项目普通用户信息
            insertUsers(userId, sdpProject, projectUsers);
        }
        return sdpProject;
    }

    private void validateProjectCode(String projectCode) {
       if (!projectCode.matches(CODE_REX)){
           throw new ApplicationException(ResponseCode.PROJECT_CODE_FALSE);
       }
       SdpProject sp = this.getMapper().getProjectByCode(projectCode);
       if (Objects.nonNull(sp)){
           throw new ApplicationException(ResponseCode.PROJECT_CODE_EXIST);
       }
    }

    /**
     * 插入项目普通用户信息
     * @param userId
     * @param sdpProject
     * @param projectUsers
     */
    private void insertUsers(String userId, SdpProject sdpProject, List<ProjectUser> projectUsers) {

            projectUsers.forEach(x ->{
                SdpUser sdpUser = new SdpUser();
                sdpUser.setUserName(x.getUserName());
                sdpUser.setEmployeeNumber(x.getEmployeeNumber());
                if (StrUtil.isNotBlank(x.getPrivateMobile())){
                    sdpUser.setPrivateMobile(x.getPrivateMobile());
                }
                sdpUser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                sdpUser = renewUserTable(userId, sdpUser);
                SdpProjectUser spu = new SdpProjectUser();
                spu.setProjectId(sdpProject.getId());
                spu.setUserId(sdpUser.getId());
                spu.setIsLeader(UserRole.ProjectUserEnum.ORGANIZE_USER.getCode());
                List<SdpProjectUser> projectUserList = projectUserService.getMapper().selectIsExist(spu);
                if (CollectionUtils.isEmpty(projectUserList)) {
                    spu.setIsLeader(UserRole.ProjectUserEnum.GENERAL_USER.getCode());
                    List<SdpProjectUser> sdpProjectUsers = projectUserService.getMapper().selectIsExist(spu);
                    if (Objects.isNull(sdpProjectUsers) || CollectionUtils.isEmpty(sdpProjectUsers)) {
                        spu.setCreationDate(new Timestamp(System.currentTimeMillis()));
                        spu.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                        spu.setCreatedBy(userId);
                        projectUserService.getMapper().insert(spu);
                    } else {
                        spu.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                        spu.setUpdatedBy(userId);
                        projectUserService.update(spu);
                    }
                }
            });


    }

    /**
     * 插入项目负责人和管理者信息
     * @param sdpProjectInfo
     * @param userId
     * @param projectId
     */
    private void insertProjectLeader(SdpProjectInfo sdpProjectInfo, String userId, Long projectId) {
        //处理负责人信息同时添加为管理者
        List<SdpUser> projectLeaderList = sdpProjectInfo.getProjectLeader();

            projectLeaderList.forEach(x -> {
                SdpProjectUser sdpProjectUser = new SdpProjectUser();
                SdpProjectUser sdpGeneral = new SdpProjectUser();
                sdpProjectUser.setProjectId(projectId);
                x = renewUserTable(userId, x);
                x = userService.getMapper().selectIsExist(x);
                sdpProjectUser.setUserId(x.getId());
                sdpProjectUser.setCreationDate(new Timestamp(System.currentTimeMillis()));
                sdpProjectUser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                sdpProjectUser.setCreatedBy(userId);
                BeanUtils.copyProperties(sdpProjectUser, sdpGeneral);
                sdpProjectUser.setIsLeader(UserRole.ProjectUserEnum.RESPONSIBLE_PERSON.getCode());
                projectUserService.getMapper().insert(sdpProjectUser);
                sdpGeneral.setIsLeader(UserRole.ProjectUserEnum.ORGANIZE_USER.getCode());
                projectUserService.getMapper().insert(sdpGeneral);
            });

    }

    /**
     * 插入项目引擎信息
     * @param sdpProjectInfo
     * @param projectId
     */
    private void insertProjectEngines(SdpProjectInfo sdpProjectInfo, Long projectId) {
        List<SdpEngine> projectEngines = sdpProjectInfo.getProjectEngines();

            projectEngines.forEach(x -> {
                SdpProjectEngine sdpProjectEngine = new SdpProjectEngine();
                sdpProjectEngine.setEngineId(x.getId());
                sdpProjectEngine.setProjectId(projectId);
                sdpProjectEngine.setCreationDate(new Timestamp(System.currentTimeMillis()));
                sdpProjectEngine.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                projectEngineService.getMapper().insert(sdpProjectEngine);
            });

    }

    /**
     * 插入项目信息
     * @param sdpProjectInfo
     * @param userId
     * @return
     */
    private SdpProject insertProject(SdpProjectInfo sdpProjectInfo, String userId) {
        SdpProject sdpProject = new SdpProject();
        sdpProject.setProjectName(sdpProjectInfo.getProjectName());
        sdpProject.setProjectCode(sdpProjectInfo.getProjectCode());
        sdpProject.setProductLineName(sdpProjectInfo.getProductLineName());
        sdpProject.setProductLineCode(sdpProjectInfo.getProductLineCode());
        sdpProject.setCreationDate(new Timestamp(System.currentTimeMillis()));
        sdpProject.setUpdationDate(new Timestamp(System.currentTimeMillis()));
        sdpProject.setPriority(sdpProjectInfo.getPriority()==null? PriorityLevel.P2.getType():sdpProjectInfo.getPriority());
        sdpProject.setCreatedBy(userId);
        sdpProject.setForbidUdxUpdation(sdpProjectInfo.getForbidUdxUpdation());
        sdpProject.setAllowJobAddEdit(sdpProjectInfo.getAllowJobAddEdit());
        insertSelective(sdpProject);
        return sdpProject;
    }

    private void checkSdpProjectInfo(SdpProjectInfo sdpProjectInfo) {
        if (StringUtils.isEmpty(sdpProjectInfo.getProjectName()) || Objects.isNull(sdpProjectInfo.getProjectName())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpProjectInfo.getProjectName());
        }
        if (Objects.isNull(sdpProjectInfo.getProjectEngines()) || CollectionUtils.isEmpty(sdpProjectInfo.getProjectEngines())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpProjectInfo.getProjectEngines());
        }
        if (Objects.isNull(sdpProjectInfo.getProjectLeader()) || CollectionUtils.isEmpty(sdpProjectInfo.getProjectLeader())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpProjectInfo.getProjectLeader());
        }
        if (Objects.isNull(sdpProjectInfo.getProjectCode()) ) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpProjectInfo.getProjectCode());
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public Object update(SdpProjectInfo sdpProjectInfo) {
        checkSdpProjectInfo(sdpProjectInfo);
        String userId = ContextUtils.get().getUserId();
        Long projectInfoId = sdpProjectInfo.getId();
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectId(projectInfoId);
        checkPermission(projectUser);
        //校验项目名不能修改
        SdpProject sdpProject = this.getMapper().selectById(projectInfoId);
        //找出该项目下所有没有修改过等级的任务
//        List<SdpJob> sdpJobList = jobService.getMapper().queryByPriority(sdpProject);
        if (!sdpProjectInfo.getProjectName().equals(sdpProject.getProjectName())) {
            throw new ApplicationException(ResponseCode.PROJECT_NAME_CHANGED);
        }
        //检验项目code不能修改
        if (!StringUtils.isEmpty(sdpProject.getProjectCode()) && !sdpProjectInfo.getProjectCode().equals(sdpProject.getProjectCode())) {
            throw new ApplicationException(ResponseCode.PROJECT_NAME_CHANGED);
        }else{
            sdpProject.setProjectCode(sdpProjectInfo.getProjectCode());
            sdpProject.setProductLineName(sdpProjectInfo.getProductLineName());
            sdpProject.setProductLineCode(sdpProjectInfo.getProductLineCode());
            sdpProject.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            sdpProject.setPriority(sdpProjectInfo.getPriority()==null? PriorityLevel.P2.getType():sdpProjectInfo.getPriority());
            sdpProject.setUpdatedBy(userId);
            sdpProject.setForbidUdxUpdation(sdpProjectInfo.getForbidUdxUpdation());
            sdpProject.setAllowJobAddEdit(sdpProjectInfo.getAllowJobAddEdit());
            updateSelective(sdpProject);
            //修改该项目下作业的等级
//            if(!CollectionUtils.isEmpty(sdpJobList)){
//                List<SdpJob> sdpJobList1 = sdpJobList.stream().map(x->{
//                    x.setPriority(sdpProjectInfo.getPriority());
//                    return x;
//                }).collect(Collectors.toList());
//                jobService.updatePriority(sdpJobList1);
//            }
        }
        //处理leader项目负责人
        List<SdpUser> sdpUsers = updateProjectLeader(sdpProjectInfo, userId, projectInfoId);
        //处理用户信息，进行user表更新处理
        ArrayList<ProjectUser> proUsers = updateUsers(sdpProjectInfo, userId);
        //用户信息进行update
        updateProjectUsers(projectInfoId, sdpUsers, proUsers,userId);
        //处理引擎信息
        updateProjectEngines(sdpProjectInfo, projectInfoId);
        return ResponseCode.SUCCESS;
    }

    /**
     * 处理项目引擎更新
     * @param sdpProjectInfo
     * @param projectInfoId
     */
    private void updateProjectEngines(SdpProjectInfo sdpProjectInfo, Long projectInfoId) {
        List<SdpEngine> projectEngines = sdpProjectInfo.getProjectEngines();
        List<SdpEngine> sdpEngines = new ArrayList<>();

        projectEngines.forEach(x -> sdpEngines.add(x));
        List<SdpProjectEngine> spes = projectEngineService.getMapper().getProjectEngines(projectInfoId);
        Iterator<SdpProjectEngine> spesIt = spes.iterator();

        while (spesIt.hasNext()) {
            SdpProjectEngine sdpProjectEngine = spesIt.next();
            Iterator<SdpEngine> engineIterator = sdpEngines.iterator();
            while (engineIterator.hasNext()) {
                SdpEngine sdpEngine = engineIterator.next();
                if (sdpProjectEngine.getEngineId().equals(sdpEngine.getId())) {
                    sdpProjectEngine.setEnabledFlag(EnableType.ENABLE.getCode());
                    projectEngineService.update(sdpProjectEngine);
                    engineIterator.remove();
                    spesIt.remove();
                }
            }
        }
        //删除的引擎必须要检验是否file要使用
        Iterator<SdpProjectEngine> speit = spes.iterator();
        while (speit.hasNext()) {
            SdpProjectEngine sdpProjectEngine = speit.next();
            List<SdpFile> sf = fileService.getMapper().getEngine4File(projectInfoId, sdpProjectEngine.getEngineId());
            if (Objects.isNull(sf) || CollectionUtils.isEmpty(sf)) {
                projectEngineService.getMapper().updateDisable(projectInfoId, sdpProjectEngine.getEngineId());
            }else{
                throw new ApplicationException(ResponseCode.ENGINE_IS_USED);
            }
        }
        //添加新增的引擎
        Iterator<SdpEngine> engineIterator = sdpEngines.iterator();
        while (engineIterator.hasNext()) {
            SdpEngine sdpEngine = engineIterator.next();
            SdpProjectEngine sdpProjectEngine = new SdpProjectEngine();
            sdpProjectEngine.setEnabledFlag(EnableType.ENABLE.getCode());
            sdpProjectEngine.setEngineId(sdpEngine.getId());
            sdpProjectEngine.setProjectId(projectInfoId);
            projectEngineService.getMapper().insert(sdpProjectEngine);

        }
    }

    /**
     * 处理项目用户信息更新
     * @param projectInfoId
     * @param sdpUsers
     * @param proUsers
     */
    private void updateProjectUsers(Long projectInfoId, List<SdpUser> sdpUsers, ArrayList<ProjectUser> proUsers,String userId) {
        List<SdpProjectUser> sdpProjectUsers = projectUserService.getMapper().getUserBypid(projectInfoId);

        //处理项目负责人同时为项目管理员
        sdpUsers.forEach(x -> {
            SdpProjectUser spu = new SdpProjectUser();
            spu.setProjectId(projectInfoId);
            spu.setUserId(x.getId());
            spu.setIsLeader(UserRole.ProjectUserEnum.ORGANIZE_USER.getCode());
            spu.setCreationDate(new Timestamp(System.currentTimeMillis()));
            spu.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            spu.setUpdatedBy(userId);
            spu.setCreatedBy(userId);
            projectUserService.getMapper().insert(spu);
        });

        sdpProjectUsers.forEach(x -> {
            projectUserService.disable(SdpProjectUser.class,x.getId());
        });

        proUsers.forEach(x -> {
            SdpProjectUser spu = new SdpProjectUser();
            spu.setProjectId(projectInfoId);
            spu.setUserId(x.getId());
            //处理新增的，没有leader数据
            if (Objects.isNull(x.getIsLeader()) || StringUtils.isEmpty(x.getIsLeader())) {
                spu.setIsLeader(UserRole.ProjectUserEnum.GENERAL_USER.getCode());
            } else {
                spu.setIsLeader(x.getIsLeader());
            }
            List<SdpProjectUser> projectUserList = projectUserService.getMapper().selectIsExist(spu);
            if (CollectionUtils.isEmpty(projectUserList)) {
                spu.setCreationDate(new Timestamp(System.currentTimeMillis()));
                spu.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                spu.setUpdatedBy(userId);
                spu.setCreatedBy(userId);
                projectUserService.getMapper().insert(spu);
            }
        });
    }

    /**
     * 处理项目用户user表信息
     * @param sdpProjectInfo
     * @param userId
     * @return
     */
    private ArrayList<ProjectUser> updateUsers(SdpProjectInfo sdpProjectInfo, String userId) {
        List<ProjectUser> projectUsers = sdpProjectInfo.getProjectUsers();
        ArrayList<ProjectUser> proUsers = new ArrayList<>();
            projectUsers.forEach(x -> {
                //取出null值
                if (Objects.isNull(x)) {
                    return;
                }
                SdpUser sdpUser = new SdpUser();
                sdpUser.setUserName(x.getUserName());
                sdpUser.setEmployeeNumber(x.getEmployeeNumber());
                if (StrUtil.isNotBlank(x.getPrivateMobile())) {
                    sdpUser.setPrivateMobile(x.getPrivateMobile());
                }
                sdpUser = renewUserTable(userId, sdpUser);
                x.setId(sdpUser.getId());
                proUsers.add(x);
            });
        return proUsers;
    }

    /**
     * 处理项目负责人信息
     * @param sdpProjectInfo
     * @param userId
     * @param projectInfoId
     * @return
     */
    private List<SdpUser> updateProjectLeader(SdpProjectInfo sdpProjectInfo, String userId, Long projectInfoId) {

        List<SdpUser> projectLeader = sdpProjectInfo.getProjectLeader();
        List<SdpUser> sdpUsers = new ArrayList<>();

        projectLeader.forEach(x -> {
            x = renewUserTable(userId,x);
            sdpUsers.add(x);
        });
        //先删除后插入
        List<SdpProjectUser> spus = projectUserService.getMapper().getProjectResponsibles(projectInfoId);

        spus.forEach(x -> {
            projectUserService.disable(SdpProjectUser.class,x.getId());
        });


        sdpUsers.forEach(x -> {
            SdpProjectUser spu = new SdpProjectUser();
            spu.setProjectId(projectInfoId);
            spu.setUserId(x.getId());
            spu.setIsLeader(UserRole.ProjectUserEnum.RESPONSIBLE_PERSON.getCode());
            spu.setCreationDate(new Timestamp(System.currentTimeMillis()));
            spu.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            spu.setCreatedBy(userId);
            projectUserService.getMapper().insert(spu);
        });


        return sdpUsers;
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(SdpProjectInfo sdpProjectInfo) {
        if (Objects.isNull(sdpProjectInfo.getId()) || 0L == sdpProjectInfo.getId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, sdpProjectInfo.getId());
        }
        ProjectUser pu = new ProjectUser();
        pu.setProjectId(sdpProjectInfo.getId());
        checkPermission(pu);
        SdpFile sdpFile = new SdpFile();
        sdpFile.setProjectId(sdpProjectInfo.getId());
        sdpFile.setEnabledFlag(EnableType.ENABLE.getCode());
        List<SdpFile> fileList = sdpFileMapper.selectByInstance(sdpFile);
        if (Objects.nonNull(fileList) && !CollectionUtils.isEmpty(fileList)) {
            throw new ApplicationException(ResponseCode.FILE_IS_USING.getCode(), ResponseCode.FILE_IS_USING.getMessage());
        }
        List<SdpProjectEngine> projectEngines = projectEngineService.getMapper().getProjectEngines(sdpProjectInfo.getId());
        for (SdpProjectEngine projectEngine : projectEngines) {
            projectEngineService.disable(SdpProjectEngine.class,projectEngine.getId());
        }
        List<SdpProjectUser> projectLeaders = projectUserService.getMapper().getProjectLeaders(sdpProjectInfo.getId());
        for (SdpProjectUser projectLeader : projectLeaders) {
            projectUserService.disable(SdpProjectUser.class,projectLeader.getId());
        }
        List<SdpProjectUser> projectUsers = projectUserService.getMapper().getProjectUsers(sdpProjectInfo.getId());
        for (SdpProjectUser projectUser : projectUsers) {
            projectUserService.disable(SdpProjectUser.class,projectUser.getId());
        }


        //将用户表关联的最近访问的项目id置空
        List<SdpUser> sdpUsers = userService.selectAll(new SdpUser(sdpProjectInfo.getId()));
        sdpUsers.forEach(item->{
            item.setProjectId(null);
        });
        userService.update(sdpUsers.stream().toArray(SdpUser[]::new));

        return disable(SdpProject.class,sdpProjectInfo.getId());
    }


    public List<SdpProject> search(SdpProjectInfo sdpProjectInfo) {
        return this.getMapper().searchByName(sdpProjectInfo.getProjectName());
    }

    private void ckeckProjectUser(ProjectUser projectUser) {

        if (Objects.isNull(projectUser.getProjectId()) || 0 == projectUser.getProjectId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, projectUser.getProjectId());
        }
        if (Objects.isNull(projectUser.getUserName())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, projectUser.getUserName());
        }
        if (Objects.isNull(projectUser.getEmployeeNumber()) || StringUtils.isEmpty(projectUser.getEmployeeNumber())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, projectUser.getEmployeeNumber());
        }
    }

    public Object deleteProjectUser(ProjectUser projectUser) {

        if (Objects.isNull(projectUser.getProjectId()) || 0 == projectUser.getProjectId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, projectUser.getProjectId());
        }
        if (Objects.isNull(projectUser.getId()) || 0 == projectUser.getId()) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, projectUser.getId());
        }
        if (Objects.isNull(projectUser.getIsLeader())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, projectUser.getIsLeader());
        }
        checkPermission(projectUser);
        SdpProjectUser sdpProjectUser = new SdpProjectUser();
        sdpProjectUser.setProjectId(projectUser.getProjectId());
        sdpProjectUser.setIsLeader(projectUser.getIsLeader());
        if (UserRole.ProjectUserEnum.ORGANIZE_USER.getCode().equals(projectUser.getIsLeader())){
            List<SdpProjectUser> sdpProjectUsers = projectUserService.selectAll(sdpProjectUser);
            if (sdpProjectUsers.size() < 2){
                throw new ApplicationException(ResponseCode.ORGANIZE_NOT_DELETE);
            }
        }
        sdpProjectUser.setUserId(projectUser.getId());
        int result = projectUserService.getMapper().disableByInstance(sdpProjectUser);
        if (1 == result) {
            return ResponseCode.SUCCESS;
        }

        return ResponseCode.SQL_EXECUTE_FAIL.getMessage();
    }

    private void checkPermission(ProjectUser projectUser) {
        Long projectId = projectUser.getProjectId();
        String userId = ContextUtils.get().getUserId();
        ProjectUser user4Project = userService.getMapper().getUser4Project(Long.valueOf(userId), projectId);
        if( 0 == user4Project.getIsAdmin()) {
            if (Objects.isNull(user4Project.getIsLeader())){
                throw new ApplicationException(ResponseCode.PROJECT_MISS_PERMISSION);
            }
            if ( 0 == user4Project.getIsLeader()) {
                throw new ApplicationException(ResponseCode.PROJECT_MISS_PERMISSION);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Object changeRoles(List<ProjectUser> projectUser) {
        String userId = ContextUtils.get().getUserId();
        projectUser.forEach(x -> ckeckProjectUser(x));
        projectUser.forEach(x -> checkPermission(x));
        projectUser.forEach(x -> {
            //根据传进来数据是否含有isAdmin判断新增还是修改角色
            if (Objects.isNull(x.getIsAdmin())) {
                //这个是新增
                SdpUser sdpUser = new SdpUser();
                sdpUser.setUserName(x.getUserName());
                sdpUser.setEmployeeNumber(x.getEmployeeNumber());
                sdpUser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                if (StrUtil.isNotBlank(x.getPrivateMobile())){
                    sdpUser.setPrivateMobile(x.getPrivateMobile());
                }
                sdpUser = renewUserTable(userId, sdpUser);
                //判断是否已有数据，有就更新，没有就插入,一插入就是普通用户
                SdpProjectUser sdpProjectUser = new SdpProjectUser();
                sdpProjectUser.setUserId(sdpUser.getId());
                sdpProjectUser.setProjectId(x.getProjectId());
                //判断是否已是高级角色
                sdpProjectUser.setIsLeader(UserRole.ProjectUserEnum.GENERAL_USER.getCode());
                List<SdpProjectUser> projectUsers = projectUserService.getMapper().selectIsExist(sdpProjectUser);
                if (CollectionUtils.isEmpty(projectUsers)) {
                    sdpProjectUser.setIsLeader(x.getIsLeader());
                    List<SdpProjectUser> sdpProjectUsers = projectUserService.getMapper().selectIsExist(sdpProjectUser);
                    if (Objects.isNull(sdpProjectUsers) || CollectionUtils.isEmpty(sdpProjectUsers)) {
                        sdpProjectUser.setCreationDate(new Timestamp(System.currentTimeMillis()));
                        sdpProjectUser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                        sdpProjectUser.setCreatedBy(userId);
                        this.projectUserService.getMapper().insert(sdpProjectUser);
                    } else {
                        sdpProjectUser.setId(sdpProjectUsers.get(0).getId());
                        sdpProjectUser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                        sdpProjectUser.setUpdatedBy(userId);
                        projectUserService.update(sdpProjectUser);
                    }
                }
            } else {
                //isAdmin不为空就是修改角色
                SdpProjectUser sdpProjectUser = new SdpProjectUser();
                //查回原来数据更新
                if (UserRole.ProjectUserEnum.ORGANIZE_USER.getCode().equals(x.getIsLeader())) {
                    sdpProjectUser.setIsLeader(UserRole.ProjectUserEnum.GENERAL_USER.getCode());
                } else {
                    sdpProjectUser.setIsLeader(UserRole.ProjectUserEnum.ORGANIZE_USER.getCode());
                }
                sdpProjectUser.setUserId(x.getId());
                sdpProjectUser.setProjectId(x.getProjectId());
                List<SdpProjectUser> sdpProjectUsers = projectUserService.getMapper().selectIsExist(sdpProjectUser);
                //有的就直接update
                if (!CollectionUtils.isEmpty(sdpProjectUsers)) {
                    sdpProjectUser.setId(sdpProjectUsers.get(0).getId());
                    sdpProjectUser.setIsLeader(x.getIsLeader());
                    sdpProjectUser.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                    sdpProjectUser.setUpdatedBy(userId);
                    projectUserService.update(sdpProjectUser);
                }
                //重复的需要删除处理
                List<SdpProjectUser> spus = projectUserService.getMapper().selectIsExist(sdpProjectUser);
                if(Objects.nonNull(spus) && spus.size() > 1){
                    for (int i = 0; i < spus.size();i ++){
                        if ( i == 0){
                            continue;
                        }
                        SdpProjectUser user = spus.get(i);
                        Long id = user.getId();
                        projectUserService.disable(SdpProjectUser.class,id);
                    }
                }
            }
        });

        return ResponseCode.SUCCESS;
    }



    public List<SdpEngine> getEngineByName(SdpEngine sdpEngine) {
        Long id = getPermissionsId();
        //项目本身已有的引擎
        List<SdpEngine> previousEngines = projectEngineService.getMapper().getEngines(sdpEngine.getProjectId());
        //当前用户有权限的引擎
        List<SdpEngine> currentUsersEngine = engineService.getMapper().getEngineByName(sdpEngine.getEngineName(), id);
        if(!CollectionUtils.isEmpty(currentUsersEngine)){
            previousEngines.addAll(currentUsersEngine);
        }
        previousEngines.removeAll(Collections.singleton(null));
        List<SdpEngine> result = previousEngines.stream().distinct().collect(Collectors.toList());
        return result;
    }

    /**
     * 权限使用
     *
     * @return
     */
    private Long getPermissionsId() {
        String userId = ContextUtils.get().getUserId();
        Long id = 0L;
        //本地测试需要不鉴定
        if (Objects.nonNull(userId) || !StringUtils.isEmpty(userId)) {
            SdpUser sdpUser = userService.getMapper().selectById(Long.valueOf(userId));
            if (UserRole.ManagerEnum.COMMON_USER.getCode().equals(sdpUser.getIsAdmin())) {
                id = Long.valueOf(userId);
            }
        } else {
            throw new ApplicationException(ResponseCode.NO_ENGINE_USER.getCode(), ResponseCode.NO_ENGINE_USER.getMessage());
        }
        return id;
    }

    public List<SdpProject> getProjects( SdpProject sdpProject) {
        String userId = ContextUtils.get().getUserId();
        if (Objects.nonNull(userId)) {
            SdpUser sdpUser = userService.getMapper().selectById(Long.valueOf(userId));
            if (UserRole.ManagerEnum.PLATFORM_ADMIN.getCode().equals(sdpUser.getIsAdmin())) {
                userId = null;
            }
        }
        String projectName = Optional.ofNullable(sdpProject).map(m -> m.getProjectName()).orElse(null);
        return this.getMapper().getProjects(Long.valueOf(userId == null ? "0" : userId),projectName);
    }

    /**
     * 更新user表
     *
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
            sdpuser.setUpdatedBy(userId);
            userService.getMapper().insert(sdpuser);
            su = sdpuser;
            //被删除的数据恢复
        } else if (EnableType.UNABLE.getCode().equals(su.getEnabledFlag())) {
            su.setIsAdmin(UserRole.ManagerEnum.COMMON_USER.getCode());
            su.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            su.setEnabledFlag(EnableType.ENABLE.getCode());
            su.setUpdatedBy(userId);
            userService.updateSelective(su);
        }
        return su;
    }

    public ProjectUser getProjectHistory() {
        String userId = ContextUtils.get().getUserId();
        ProjectUser projectUser = userService.getMapper().getProjectHistory(Long.valueOf(userId == null ? "0" : userId));
       if (Objects.isNull(projectUser)){
           //如果没有查到最近访问的项目，则查询该用户有权限的项目
           projectUser = userService.getMapper().queryProjects4User(Long.valueOf(userId == null ? "0" : userId));
       }
        return projectUser;
    }

    public List<ProjectUser> projectUserInfo(SdpProject sdpProject) {
        if (Objects.isNull(sdpProject.getId())) {
            throw new ApplicationException(ResponseCode.MISS_ARGUMENT.getCode(), ResponseCode.MISS_ARGUMENT.getMessage());
        }

        return userService.getMapper().getUserInfos(sdpProject);
    }

    public Object getUserRole(Long projectId) {
        String userId = ContextUtils.get().getUserId();
        ProjectUser mup = userService.getMapper().getUser4Project(Long.valueOf(userId == null ? "0": userId), projectId);
        return mup;
    }

    public List<ProjectUser> getProjectUser(SdpProject sdpProject) {
        if (Objects.isNull(sdpProject.getId())) {
            throw new ApplicationException(ResponseCode.MISS_ARGUMENT.getCode(), ResponseCode.MISS_ARGUMENT.getMessage());
        }

        return userService.getMapper().getProUserAndAdmin(sdpProject);
    }

    public Pagination<SdpProjectResp> getProjectInfo(SdpProjectBO sdpProjectBO) {
        String userId = ContextUtils.get().getUserId();
        sdpProjectBO.setUserId(Long.valueOf(userId == null ? "0" : userId));
        sdpProjectBO.getVo().setProjectName(StrUtils.changeWildcard(sdpProjectBO.getVo().getProjectName()));
        Pagination<SdpProject> pagination = PaginationUtils.getInstance4BO(sdpProjectBO);
        this.executePagination(p -> getMapper().getProjectInfo(p), pagination);
        List<SdpProject> presps = pagination.getRows();
        List<SdpProjectResp> sps = new ArrayList<>();
        for (SdpProject presp : presps) {
            SdpProjectResp sdpProjectResp = new SdpProjectResp();
            BeanUtils.copyProperties(presp, sdpProjectResp);
            sps.add(sdpProjectResp);
        }
        if (CollectionUtils.isEmpty(sps)) {
            return null;
        }

        //封装项目责任人，使用者，管理者
        buildProjectUsers(userId, sps);
        Pagination<SdpProjectResp> paginationData = Pagination.getInstance(sdpProjectBO.getPage(), sdpProjectBO.getPageSize());
        //统计实际的项目数量，作业数量，用户数量
        long projects = 0;
        long files = 0;
        long users = 0;
        try {
            projects = this.getMapper().projectsCount(sdpProjectBO);
            files = this.getMapper().filesCount(sdpProjectBO);
            users = this.getMapper().usersCount(sdpProjectBO);
        } catch (Exception e) {
            logger.info("获取实际项目统计异常:{}", e.getMessage());
        }
        paginationData.setRows(sps);
        paginationData.setRowTotal(pagination.getRowTotal());
        paginationData.setPageTotal(sps.size());
        paginationData.setCount(true);
        ProjectCountInfo projectCountInfo = new ProjectCountInfo();
        BeanUtils.copyProperties(paginationData, projectCountInfo);
        projectCountInfo.setProjectTotal(projects);
        projectCountInfo.setJobTotal(files);
        projectCountInfo.setEmployeeTotal(users);
        return projectCountInfo;
    }

}

