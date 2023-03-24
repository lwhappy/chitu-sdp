package com.chitu.bigdata.sdp.aspect;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.domain.ProjectUser;
import com.chitu.bigdata.sdp.api.enums.EnvironmentEnum;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.enums.UserRole;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.api.model.SdpProjectUser;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.mapper.SdpProjectUserMapper;
import com.chitu.bigdata.sdp.mapper.SdpUserMapper;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.exceptions.IbatisException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.chitu.cloud.model.ResponseCode.UNKOWN_EXCEPTION;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/20 15:21
 */
@Aspect
@Slf4j
@Order(1)
@Component
public class PermissionAspect {
    private final String[] whiteApi = new String[]{"uiConfiguration","securityConfiguration","swaggerResources","error"};
    private final String[] projectApi = new String[]{"addProjectUser","deleteProjectUser","changeRoles",
               "getDetail","updateProject","deleteProject","projectEngineUsers"};
    private final String[] projectWhiteApi = new String[]{"projectUserInfo","getProjectUser"};
    private final String[] systemApi = new String[]{"getEngineInfo","deleteEngine","addUser",
                           "addEngine","getEngineUser","getByName","deleteUser","addManager","deleteManager","getUserInfo"};

    private final String[] notNeedProjectId = new String[]{"getInfo","getCondition","addManager","deleteManager","getHREmployee"
                          ,"getEngineInfo","deleteEngine","addUser","addEngine","getEngineUser","getByName","deleteUser",
                          "engineProjects","engineQueues","getProjectInfo","addProject","getEngineByName","updateProject",
                          "deleteProject","searchProject","deleteProjectUser","changeRoles", "getProjects","getProjectHistory",
                         "getUserRole","getProjectUser","login","loginCallback","getTokenByTicket","queryBusinessLine",
                        "getUserInfo","logout","queryJar","deleteJar","history","referenceJobs","queryApply","redirectLogin",
                        "cancelApply","detailApply","executeApprove","submitApply","queryPending","globalTest","execute",
                         "uiConfiguration","securityConfiguration","swaggerResources","error","compareVersion","jobConf",
                         "fileHistory","runtimeLog","searchByJobId","getSysoper","saveSysoper","isWhiteList","projectList","queryCluster",
                        "getJobInfo","getLineage","updateHistoryLineageTableRelation","updateJobLineageTableRelation","opsDataSourceList",
                        "getUserList","getCondition","addUser","updateUser","deleteUser"
    };

    private final String[] dataSourceApi = new String[]{"dataSourceList","getDataSources","addDataSource"};

    @Autowired
    private SdpProjectUserMapper projectUserMapper;
    @Autowired
    private SdpUserMapper userMapper;
    @Autowired
    UserService userService;
    @Autowired
    private SdpConfig sdpConfig;
    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    private void pointCut(){}


    @Around("pointCut()")
    private Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        //校验Token
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        Enumeration<String> enumeration = request.getHeaderNames();
        JSONObject headers = new JSONObject();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            headers.put(name,value);
        }
        String token = headers.getString("token");
        if(StrUtil.isBlank(token)){
            return new ResponseData(ResponseCode.TOKEN_NOT_EXIST);
        }
        String userId = redisTmplate.opsForValue().get(token);
        if(StrUtil.isBlank(userId)){
            //白名单接口放过鉴权
            List<String> whiteApis = Arrays.asList(whiteApi);
            if(whiteApis.contains(methodName)){
                return joinPoint.proceed();
            }else{
                return new ResponseData(ResponseCode.TOKEN_IS_EXPIRED);
            }
        }else{
            redisTmplate.expire(token,1, TimeUnit.HOURS);
        }
//
//        String env = headers.getString("env");
//        if(StrUtil.isBlank(env)){
//            throw new ApplicationException(ResponseCode.LOSE_ENV);
//        }
//
//
//        if (Objects.isNull(headers.getString("e-token"))){
//            throw new ApplicationException(ResponseCode.LOSE_ERP_TOKEN);
//        }else {
//            String token = headers.getString("token");
//            String erpToken = headers.getString("e-token");
//            validateToken(token, erpToken);
//        }

//        String userId = Optional.ofNullable(ContextUtils.get()).map( m -> m.getUserId()).orElse(null);

        //部分接口不做权限控制:兼容由于header前端本地使用小写问题
        Long projectId = headers.getLong("projectId") == null ? headers.getLong("projectid") : headers.getLong("projectId");
        //处理系统配置模块权限(部分接口才会在head传入projectId，所有部分权限检验需放置前面)
        SdpUser sdpUser = userMapper.selectById(Long.valueOf(userId == null ? "0" : userId));
        ResponseData responseData = checkSystemPermissions(sdpUser, methodName);
        if (null == responseData){
            return joinPoint.proceed();
        }else if(responseData.getCode() != UNKOWN_EXCEPTION.getCode()){
            return responseData;
        }

        if(projectId == null ){
            List<String> notNeedProjectIds = Arrays.asList(notNeedProjectId);
            if (notNeedProjectIds.contains(methodName)) {
                return joinPoint.proceed();
            }else {
                return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS.getCode(), ResponseCode.HAVE_NO_PERMISSIONS.getMessage());
            }
        }

        //系统管理员账号放过
        if (Objects.nonNull(sdpUser) && UserRole.ManagerEnum.PLATFORM_ADMIN.getCode().equals( sdpUser.getIsAdmin())){
            //记录实际所处项目，下次登录可以直接跳转
            sdpUser.setProjectId(projectId);
            userService.update(sdpUser);
            return joinPoint.proceed();
        }

        //处理验证项目模块中项目管理员的权限
        responseData = checkProjectPermissions(userId, projectId, methodName);
        if (null == responseData){
            return joinPoint.proceed();
        }else if(responseData.getCode() != UNKOWN_EXCEPTION.getCode()){
            return responseData;
        }
        //处理数据源权限
        responseData = checkDataSourcePermission(methodName, userId, projectId);
        if (null == responseData){
            return joinPoint.proceed();
        }else if(responseData.getCode() != UNKOWN_EXCEPTION.getCode()){
            return responseData;
        }

        List<SdpProjectUser> projects1 = projectUserMapper.queryProject4User(userId);
        if(CollectionUtils.isNotEmpty(projects1)){
            List<Long> projects = projects1.stream().map(x->x.getProjectId()).collect(Collectors.toList());
            if(!projects.contains(projectId)){
                return new ResponseData(ResponseCode.PROJECT_NO_PERMISSIONS);
            }
        }else {
            return new ResponseData(ResponseCode.PROJECT_NO_PERMISSIONS);
        }
        //记录实际所处项目，下次登录可以直接跳转
        sdpUser.setProjectId(projectId);
        userService.update(sdpUser);
        return joinPoint.proceed();
    }


    private ResponseData checkDataSourcePermission( String methodName, String userId, Long projectId) {
        List<String> dataSourceApis = Arrays.asList(dataSourceApi);
        if(Objects.nonNull(userId)  && dataSourceApis.contains(methodName)){
            if (EnvironmentEnum.DEV.getCode().equals(sdpConfig.getEnv()) || EnvironmentEnum.UAT.getCode().equals(sdpConfig.getEnv())){
                return null;
            }
            SdpProject sdpProject = new SdpProject();
            sdpProject.setId(projectId);
            List<ProjectUser> userList = userMapper.getProUserAndAdmin(sdpProject);
            Long[] ids = userList.stream().map(x -> x.getId()).toArray(Long[]::new);
            List<Long> idList = Arrays.asList(ids);
            if (!idList.contains(Long.valueOf(userId))){
                return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS.getCode(),ResponseCode.HAVE_NO_PERMISSIONS.getMessage());
            }else{
                return null;
            }
        }
        return new ResponseData();
    }

    private ResponseData checkProjectPermissions(String userId, Long projectId, String methodName) {
        List<String> projectApis = Arrays.asList(projectApi);
        List<String> whiteList = Arrays.asList(projectWhiteApi);
        if (whiteList.contains(methodName)){
            return null;
        }
        if(Objects.nonNull(userId)  && projectApis.contains(methodName)){
            List<SdpProjectUser> projectManger =   projectUserMapper.selectProjectManger(Long.valueOf(userId),projectId);
           if (CollectionUtils.isEmpty(projectManger)){
               return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS);
           }
            return null;
        }
        return new ResponseData();
    }


    private ResponseData checkSystemPermissions( SdpUser sdpUser, String methodName) {
        List<String> systemApis = Arrays.asList(systemApi);
        if (systemApis.contains(methodName)) {
            if (Objects.nonNull(sdpUser) && !UserRole.ManagerEnum.PLATFORM_ADMIN.getCode().equals(sdpUser.getIsAdmin())) {
                return new ResponseData(ResponseCode.HAVE_NO_PERMISSIONS);
            }
            return null;
        }
        return new ResponseData();
    }

    @AfterThrowing(value = "execution(public * com.chitu.bigdata.sdp.controller.*.*(..))", throwing = "e")
    @Transactional(propagation= Propagation.NOT_SUPPORTED)
    public Exception afterThrowing(Exception e) {
        if (SQLException.class.isAssignableFrom(e.getClass())  || DataAccessException.class.isAssignableFrom(e.getClass())
                || IbatisException.class.isAssignableFrom(e.getClass())  ){
            log.error("执行sql异常", e);
            throw new ApplicationException(ResponseCode.SQL_EXECUTE_FAIL);
        }
        return e;
    }


}
