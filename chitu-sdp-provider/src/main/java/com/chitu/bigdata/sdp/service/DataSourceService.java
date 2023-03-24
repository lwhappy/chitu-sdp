

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-12-9
 * </pre>
 */

package com.chitu.bigdata.sdp.service;


import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.ProjectUser;
import com.chitu.bigdata.sdp.api.enums.DataSourceType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.api.vo.DataSourceResp;
import com.chitu.bigdata.sdp.config.CheckConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.*;
import com.chitu.bigdata.sdp.service.datasource.AbstractDataSource;
import com.chitu.bigdata.sdp.service.datasource.DataSourceFactory;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.bigdata.sdp.utils.TripleDesCipher;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.ContextUtils;
import com.chitu.cloud.utils.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * 业务类
 * </pre>
 */
@Service("sdpDataSourceService")
@RefreshScope
@Slf4j
public class DataSourceService extends GenericService<SdpDataSource, Long> {

    @Autowired
    CheckConfigProperties checkConfigProperties;
    @Autowired
    SdpConfig sdpConfig;

    /**
     * mysql用户名称必须包含sdp
     */
    private final static String MYSQL_USER_CONTAIN_SDP = "sdp";

    public DataSourceService(@Autowired SdpDataSourceMapper sdpDataSourceMapper) {
        super(sdpDataSourceMapper);
    }

    public SdpDataSourceMapper getMapper() {
        return (SdpDataSourceMapper) super.genericMapper;
    }

    @Autowired
    private UserService userService;
    @Autowired
    private SdpMetaTableConfigMapper metaTableConfigMapper;
    @Autowired
    private DataSourceFactory dataSourceFactory;
    @Autowired
    private SdpUserMapper userMapper;
    @Autowired
    TripleDesCipher tripleDesCipher;
    @Autowired
    private SdpJobMapper jobMapper;
    @Autowired
    private MetaTableConfigService metaTableConfigService;
    @Autowired
    private SdpMetaTableRelationMapper relationMapper;
    @Autowired
    private SdpMetaTableRelationService relationService;
    @Autowired
    ProjectService projectService;



    private String NAME_REX = "^[A-Za-z0-9]+(\\w)+";

    private String PWD_REX = "(\\*)+";

    private String SOURCE_URL = "/([\\w|-]*)\\?";

    private static final String URL_COMBINE = "?";


    public Object dataSourceList(SdpDataSourceBO bo) {
        String dataSourceName = bo.getDataSourceName();
        bo.setDataSourceName(StrUtils.changeWildcard(dataSourceName));
        PageHelper.startPage(bo.getPage(), bo.getPageSize());
        checkFilter(bo);
        Page<DataSourceResp> sdpDataSourcePage = (Page) this.getMapper().dataSourceList(bo);
        Pagination pagination = Pagination.getInstance(bo.getPage(), bo.getPageSize());
        //统计元表数 作业数
        datasourceCountJobTable(sdpDataSourcePage, pagination,bo);
        pagination.setRows(sdpDataSourcePage);
        pagination.setRowTotal((int) sdpDataSourcePage.getTotal());
        pagination.setPageTotal(sdpDataSourcePage.getPages());
        return pagination;
    }

    private void checkFilter(SdpDataSourceBO bo) {
        if (!StringUtils.isEmpty(bo.getMetaTableName())) {
            bo.setMetaTableName(bo.getMetaTableName().trim());
        }
        if (!StringUtils.isEmpty(bo.getTableName())) {
            bo.setTableName(bo.getTableName().trim());
        }
        if (!StringUtils.isEmpty(bo.getJobName())) {
            bo.setJobName(bo.getJobName().trim());
        }
        if (!StringUtils.isEmpty(bo.getProjectName())) {
            bo.setProjectName(bo.getProjectName().trim());
        }
        if (!StringUtils.isEmpty(bo.getDataSourceName())) {
            bo.setDataSourceName(bo.getDataSourceName().trim());
        }
    }

    private void datasourceCountJobTable(Page<DataSourceResp> sdpDataSourcePage, Pagination pagination,SdpDataSourceBO bo) {
        if (!CollectionUtils.isEmpty(sdpDataSourcePage)) {
            List<DataSourceResp> rows = pagination.getRows();
            for (DataSourceResp datasource : sdpDataSourcePage) {
                try {
                    SdpMetaTableRelation sdpMetaTableRelation = new SdpMetaTableRelation();
                    sdpMetaTableRelation.setDataSourceId(datasource.getId());
                    if (!StringUtils.isEmpty(bo.getTableName())) {
                        sdpMetaTableRelation.setMetaTableName(bo.getTableName());
                    }
                    if (!StringUtils.isEmpty(bo.getMetaTableName())) {
                        sdpMetaTableRelation.setFlinkTableName(bo.getMetaTableName());
                    }
                    Integer countTable = relationMapper.countMetaTable(sdpMetaTableRelation);
                    //List<SdpMetaTableRelation> sdpMetaTableRelations = relationService.selectAll(sdpMetaTableRelation);
                    datasource.setCountMetaTable(countTable);
                    SdpJob sdpJob = new SdpJob();
                    datasource.setCountJob(jobMapper.countJob(datasource.getId(),bo.getJobName()));
                    SdpMetaTableConfig sdpMetaTableConfig = new SdpMetaTableConfig();
                    sdpMetaTableConfig.setDataSourceId(datasource.getId());
                    List<SdpMetaTableConfig> datasourceList = metaTableConfigService.selectAll(sdpMetaTableConfig);
                    datasource.setIsUsed(CollectionUtils.isEmpty(datasourceList) ? 0 : 1);
                } catch (Exception e) {
                    logger.error("count metatable or job error=", e);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Object add(SdpDataSource sds) {
        checkParams(sds);
        //校验是否重名(项目下不重名检验)
        List<SdpDataSource> instance = this.getMapper().selectByName(sds);
        if (!CollectionUtils.isEmpty(instance)) {
            throw new ApplicationException(ResponseCode.DATASOURCE_NAME_EXIst);
        }

        if (Objects.nonNull(sds.getDataSourceUrl())) {
            sds.setDataSourceUrl(sds.getDataSourceUrl().trim());
        }
        if (Objects.nonNull(sds.getStreamLoadUrl())) {
            sds.setStreamLoadUrl(sds.getStreamLoadUrl().trim());
        }
        if (Objects.nonNull(sds.getDatabaseName())) {
            sds.setDatabaseName(sds.getDatabaseName().trim());
        }
        String userId = ContextUtils.get().getUserId();
        sds.setCreatedBy(userId);
        sds.setUpdatedBy(userId);
        sds.setPassword(tripleDesCipher.encrypt(sds.getPassword()));
        int count = insertSelective(sds);
        List<SdpDataSource> newList = this.getMapper().selectByName(sds);
        if (CollectionUtils.isEmpty(newList) || newList.size() > 1) {
            throw new ApplicationException(ResponseCode.DATASOURCE_NAME_EXIst);
        }

        return count;
    }

    private void checkParams(SdpDataSource sds) {
        if (DataSourceType.PRINT.getType().equals(sds.getDataSourceType()) || DataSourceType.DATAGEN.getType().equals(sds.getDataSourceType())) {
            return;
        }
        if (Objects.isNull(sds.getDataSourceName())) {
            throw new ApplicationException(ResponseCode.EMPTY_DATASOURCE_ARGUMENT);
        }
        if (!sds.getDataSourceName().matches(NAME_REX)) {
            throw new ApplicationException(ResponseCode.DATASOURCE_NAME_INVALID);
        }
        if (Objects.isNull(sds.getDataSourceType())) {
            throw new ApplicationException(ResponseCode.DATASOURCE_TYPE_INVALID);
        }
        if (Objects.isNull(sds.getDataSourceUrl())) {
            throw new ApplicationException(ResponseCode.EMPTY_DATASOURCE_ARGUMENT);
        }

        if (sds.getDataSourceUrl().endsWith(StrUtil.SLASH)) {
            sds.setDataSourceUrl(StrUtil.subBefore(sds.getDataSourceUrl(), StrUtil.SLASH, true));
        }


        //mysql截取数据库名称
        getDbName(sds);
//        if(!DataSourceType.KAFKA.getType().equals(sds.getDataSourceType()) && !DataSourceType.ES.getType().equals(sds.getDataSourceType())){
//            if (Objects.isNull(sds.getDatabaseName())){
//                throw new ApplicationException(ResponseCode.EMPTY_DATASOURCE_ARGUMENT);
//            }
//        }
    }

    private void getDbName(SdpDataSource sds) {
        if (DataSourceType.MYSQL.getType().equals(sds.getDataSourceType())) {
            String dataSourceUrl = sds.getDataSourceUrl();
            String dbName = null;
            if (sds.getDataSourceUrl().contains(URL_COMBINE)) {
                Pattern compile = Pattern.compile(SOURCE_URL);
                Matcher errorMa = compile.matcher(dataSourceUrl);
                while (errorMa.find()) {
                    dbName = errorMa.group(1);
                    sds.setDatabaseName(dbName);
                }
                if (Objects.isNull(dbName)) {
                    throw new ApplicationException(ResponseCode.EMPTY_DATASOURCE_ARGUMENT);
                }
            } else {
                String databaseName = StrUtil.subAfter(dataSourceUrl, StrUtil.SLASH, true);
                if (StrUtil.isBlank(databaseName)) {
                    throw new ApplicationException(ResponseCode.EMPTY_DATASOURCE_ARGUMENT);
                }
                sds.setDatabaseName(databaseName);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Object updateInstance(SdpDataSource sds) {
        String userId = ContextUtils.get().getUserId();
        //进行权限检验
        if (!checkDataSourcePermission(sds, userId)) {
            throw new ApplicationException(ResponseCode.PROJECT_MISS_PERMISSION);
        }
        checkParams(sds);
        //判断是否更改不能变更的部分
        reviewParams(sds);
        if (Objects.nonNull(sds.getDataSourceUrl())) {
            sds.setDataSourceUrl(sds.getDataSourceUrl().trim());
        }
        if (Objects.nonNull(sds.getStreamLoadUrl())) {
            sds.setStreamLoadUrl(sds.getStreamLoadUrl().trim());
        }
        if (Objects.nonNull(sds.getDatabaseName())) {
            sds.setDatabaseName(sds.getDatabaseName().trim());
        }
        sds.setUpdationDate(new Timestamp(System.currentTimeMillis()));
        sds.setUpdatedBy(userId);

        //增加这个判断是防止密码没有传过来被置空了
        if (StrUtil.isNotBlank(sds.getPassword())) {
            sds.setPassword(tripleDesCipher.encrypt(sds.getPassword()));
        }
        int update = update(sds);
        if (!StringUtils.isEmpty(sds.getDataSourceName())) {
            //数据源名称 项目级唯一
            List<SdpDataSource> newList = this.getMapper().selectByName(sds);
            if (!CollectionUtils.isEmpty(newList) && newList.size() > 1) {
                throw new ApplicationException(ResponseCode.DATASOURCE_NAME_EXIst);
            }
        }


        return update;
    }


    private boolean checkDataSourcePermission(SdpDataSource sds, String userId) {
        SdpDataSource sdpDataSource = this.get(sds.getId());
        if (Objects.nonNull(sdpDataSource)) {
            Long owner = sdpDataSource.getOwner();
            if (owner.equals(Long.valueOf(userId))) {
                return true;
            }
        }
        SdpProject sdpProject = new SdpProject();
        sdpProject.setId(sds.getProjectId());
        List<ProjectUser> userList = userMapper.getProUserAndAdmin(sdpProject);
        for (ProjectUser projectUser : userList) {
            if (projectUser.getId().equals(Long.valueOf(userId))) {
                if (1 == projectUser.getIsAdmin()) {
                    return true;
                }
                if (projectUser.getIsLeader() != 0) {
                    return true;
                }
            }
        }
        return false;
    }


    private void reviewParams(SdpDataSource sds) {
        SdpDataSource sdpDataSource = getByIdWithPwdPlaintext(sds.getId());
//        if (!sdpDataSource.getDataSourceName().equals(sds.getDataSourceName())) {
//            throw new ApplicationException(ResponseCode.PARAMS_CHANGE_NOT_ALLOW);
//        }
        if (!sdpDataSource.getDataSourceType().equals(sds.getDataSourceType())) {
            throw new ApplicationException(ResponseCode.PARAMS_CHANGE_NOT_ALLOW);
        }
        if (!DataSourceType.KAFKA.getType().equals(sdpDataSource.getDataSourceType())
                && !DataSourceType.ES.getType().equals(sdpDataSource.getDataSourceType()) && !DataSourceType.HIVE.getType().equals(sdpDataSource.getDataSourceType())
                && !DataSourceType.DATAGEN.getType().equals(sdpDataSource.getDataSourceType())
                && !DataSourceType.PRINT.getType().equals(sdpDataSource.getDataSourceType())
        ) {
            if (!sdpDataSource.getDatabaseName().equals(sds.getDatabaseName())) {
                throw new ApplicationException(ResponseCode.PARAMS_CHANGE_NOT_ALLOW);
            }
        }
        //处理密码部分
        if (Objects.nonNull(sds.getPassword()) && StringUtils.isNotEmpty(sds.getPassword())) {
            if (sds.getPassword().matches(PWD_REX)) {
                sds.setPassword(sdpDataSource.getPassword());
            }
        }
        //判断是否有权限进行操作
//        checkPermission(sdpDataSource);
    }

    private void checkPermission(SdpDataSource sdpDataSource) {
        Long projectId = sdpDataSource.getProjectId();
        String userId = ContextUtils.get().getUserId();
        //当用户非责任人才去判断是否管理员
        if (!sdpDataSource.getOwner().equals(Long.valueOf(userId))) {
            ProjectUser mup = userService.getMapper().getUser4Project(Long.valueOf(userId == null ? "0" : userId), projectId);
            if (0 == mup.getIsAdmin()) {
                if (Objects.isNull(mup.getIsLeader())) {
                    throw new ApplicationException(ResponseCode.PROJECT_MISS_PERMISSION);
                }
                if (0 == mup.getIsLeader()) {
                    throw new ApplicationException(ResponseCode.PROJECT_MISS_PERMISSION);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Object deleteInstance(SdpDataSource sds) {
        String userId = ContextUtils.get().getUserId();
        if (!checkDataSourcePermission(sds, userId)) {
            throw new ApplicationException(ResponseCode.PROJECT_MISS_PERMISSION);
        }
        if (Objects.isNull(sds.getId())) {
            throw new ApplicationException(ResponseCode.EMPTY_DATASOURCE_ARGUMENT);
        }
        //判断是否有作业在使用该数据源
        List<SdpMetaTableConfig> metaTableList = metaTableConfigMapper.getUsingSource(sds.getId());
        if (!CollectionUtils.isEmpty(metaTableList)) {
            throw new ApplicationException(ResponseCode.DATASOURCE_IS_USING);
        }
        return disable(SdpDataSource.class,sds.getId());
    }

    public ResponseData checkConnect(SdpDataSource sds) {
//        String userId = ContextUtils.get().getUserId();
//        if (!checkDataSourcePermission(sds,userId)) {
//            throw new ApplicationException(ResponseCode.PROJECT_MISS_PERMISSION);
//        }
        if (Objects.isNull(sds.getDataSourceType())) {
            throw new ApplicationException(ResponseCode.DATASOURCE_TYPE_INVALID);
        }
        if (Objects.isNull(sds.getDataSourceUrl())) {
            throw new ApplicationException(ResponseCode.EMPTY_DATASOURCE_ARGUMENT);
        }
        if (sds.getDataSourceUrl().endsWith(StrUtil.SLASH)) {
            sds.setDataSourceUrl(StrUtil.subBefore(sds.getDataSourceUrl(), StrUtil.SLASH, true));
        }
        getDbName(sds);
        //对数据源id不为null并且密码为******需要重新获取密码
        if (Objects.nonNull(sds.getPassword()) && StringUtils.isNotEmpty(sds.getPassword())) {
            if (Objects.nonNull(sds.getId()) && sds.getPassword().matches(PWD_REX)) {
                SdpDataSource sdpDataSource = getByIdWithPwdPlaintext(sds.getId());
                sds.setPassword(sdpDataSource.getPassword());
            }
        }
        ConnectInfo connectInfo = new ConnectInfo();
        String dataSourceUrl = sds.getDataSourceUrl().trim();
        connectInfo.setAddress(dataSourceUrl);
//        if (Objects.nonNull(sds.getDatabaseName()) && StringUtils.isNotEmpty(sds.getDatabaseName())) {
//            String databaseName = sds.getDatabaseName().trim();
//            connectInfo.setDatabaseName(databaseName);
//        }
        if (Objects.nonNull(sds.getCertifyType())) {
            String certifyType = sds.getCertifyType().trim();
            connectInfo.setCertifyType(certifyType);
        }
        if (Objects.nonNull(sds.getUserName())) {
            connectInfo.setUsername(sds.getUserName());
        }
        if (Objects.nonNull(sds.getPassword())) {
            connectInfo.setPwd(sds.getPassword());
        }
        if (StringUtils.isNotEmpty(sds.getHbaseZnode())) {
            connectInfo.setHbaseZnode(sds.getHbaseZnode());
        }
        if (StringUtils.isNotEmpty(sds.getHadoopConfDir())) {
            connectInfo.setHadoopConfDir(sds.getHadoopConfDir());
        }
        if (StringUtils.isNotEmpty(sds.getHudiCatalogPath())) {
            connectInfo.setHudiCatalogPath(sds.getHudiCatalogPath());
        }
        AbstractDataSource<AutoCloseable> dataSource = dataSourceFactory.getDataSource(sds.getDataSourceType());
        AutoCloseable connection = null;
        ResponseData data = new ResponseData<>();
        try {
            connection = dataSource.getConnection(connectInfo);
            if (Objects.nonNull(connection)) {
                data.ok();
                return data;
            } else {
                throw new ApplicationException(ResponseCode.CONNECT_IS_FAILED);
            }
        } catch (Exception e) {
            logger.error("连接数据源类型{}，数据库{},连接失败{}", sds.getDataSourceType(), sds.getDatabaseName(), e);
            throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG, e.getMessage());
        } finally {
            try {
                dataSource.closeConnection(connection);
            } catch (Exception e) {
                logger.error("释放数据源类型{}，数据库{},连接失败{}", sds.getDataSourceType(), sds.getDatabaseName(), e);
            }
        }
    }

//    private void assembleAddress(SdpDataSource sds, ConnectInfo connectInfo) {
//        String dataSourceUrl = sds.getDataSourceUrl().trim();
//        if (Objects.nonNull(sds.getDatabaseName()) && StringUtils.isNotEmpty(sds.getDatabaseName())) {
//            String databaseName = sds.getDatabaseName().trim();
//            connectInfo.setAddress(String.format(ADDR_FORMAT,dataSourceUrl,databaseName));
//        }else{
//            connectInfo.setAddress(dataSourceUrl);
//        }
//
//    }

    public List<SdpDataSource> getDataSources(SdpDataSource sds) {
        //指定环境请求
        String env = sds.getEnv();
        if(StrUtil.isNotBlank(env)){
            String headEnv = sdpConfig.getEnvFromEnvHolder(null);
            try {
                EnvHolder.clearEnvAndAddEnv(env);
                return this.getMapper().getDataSources(sds);
            }finally {
                //重置回去
                EnvHolder.clearEnvAndAddEnv(headEnv);
            }
        }else {
            return this.getMapper().getDataSources(sds);
        }
    }

    /**
     * <p>密码需要解密使用的时候，调用该方法。</p>
     * <p>注意： 这个方法查询结果不能响应到前端,涉及到安全问题</p>
     * @param id
     * @return
     */
    public SdpDataSource getByIdWithPwdPlaintext(Long id) {
        SdpDataSource sdpDataSource = this.getMapper().selectById(id);
        if (Objects.nonNull(sdpDataSource)) {
            sdpDataSource.setPassword(tripleDesCipher.decrypt(sdpDataSource.getPassword()));
        }
        return sdpDataSource;
    }


}