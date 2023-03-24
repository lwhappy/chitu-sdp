package com.chitu.bigdata.sdp.api.enums;

import com.chitu.cloud.model.AppCode;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/19 19:40
 */
public enum ResponseCode implements AppCode {
    //0-2500为系统设置模块状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统错误"),
    VALIDATE_FLAG_ERROR(501, "校验系统开关异常"),
    CALL_INTERFACE_ERROR(502, "调用BDP接口异常"),
    TOKEN_IS_EXPIRED(503, "Token已过期,请重新登录"),
    TOKEN_NOT_EXIST(504,"Token不存在"),
    REQUEST_SERVICE_ERROR(505, "请求服务失败"),
    APPROVE_ONLINE_EXCEPTION(506, "审批上线异常"),
    APPROVE_STATUS_ERROR(507, "%s状态不能执行审批操作"),
    APPROVER_EXCEPTION(508, "不能审批自己提交的申请"),
    APPROVE_NOT_PERMISSION(509, "当前审批阶段审批人不包含你"),
    LOSE_ERP_TOKEN(510,"缺失e-token"),
    WRONG_ACCOUNT_PASSWORD(511,"账号密码错误"),
    GET_LOCK_ERROR(5010, "加锁异常"),
    CLOSE_LOCK_ERROR(5011, "释放锁异常"),
    IS_LOCK_ERROR(5012, "判断是否加锁异常"),

    INVALID_ARGUMENT(400, "非法参数:%s"),
    ENGINE_NOT_EXIST(404,"该引擎不存在"),
    ENGINE_REPEAT(406,"引擎名称%s重复"),
    SQL_EXECUTE_FAIL(407,"sql执行失败"),
    ADD_ALL_FAIL(408,"用户已存在"),
    ENGINE_IS_USING(409,"引擎还在正常使用"),
    ENGINE_QUEUE_FAULT(410,"获取引擎队列请求yarn集群失败"),
    ENGINE_NAME_ILLEGAL(411,"引擎名称含有非法字符"),
    ENGINE_VERSION_ILLEGAL(412,"引擎版本含有非法字符"),
    ORGANIZE_NOT_DELETE(413,"最后一位项目管理员不能删除"),
    REGRESSION_EXIST(414,"connector集成测试已在测试中"),
    REGRESSION_NOT_EXIST(415,"connector集成测试已停止"),

    //2500-3500为项目管理模块状态码
    PROJECT_REPEAT(25002,"项目名称%s重复"),
    PROJECT_NO_PERMISSIONS(2500, "该项目未授权"),
    PROJECT_NAME_CHANGED(25001,"项目名不能更改"),
    HAVE_NO_PERMISSIONS(25003,"无权限操作"),
    FILE_IS_USING(25004,"项目中仍有作业，请先删除作业"),
    NO_ENGINE_USER(25005,"没有可以使用的引擎"),
    MISS_ARGUMENT(25006,"参数缺失"),
    PROJECT_CODE_FALSE(25007,"项目code不合规"),
    PROJECT_CODE_EXIST(25008,"项目code已存在"),
    PROJECT_CODE_CHANGED(25009,"项目code不能更改"),
    PROJECT_MISS_PERMISSION(25010,"用户权限不足"),
    ENGINE_IS_USED(25011,"引擎有在使用中，无法删除"),

    //3500-5000为JAR管理模块状态码
    JAR_IS_USING(35001,"当前jar有作业正在引用中"),
    JAR_NOT_NULL(35002,"上传文件不能为空"),
    JAR_UPLOAD_FAILED(35003,"上传JAR文件失败"),
    JAR_NAME_ERROR(35004,"JAR名称不合法"),
    JAR_TYPE_ERROR(35005,"文件类型不合法"),
    JAR_NAME_NULL(35006,"JAR包名称不能为空"),
    JAR_ADD_FAILED(35007,"当前JAR已存在,请直接更新"),

    //5000-7500为作业运维模块状态码
    JOB_IS_RUNNING(5001,"作业正在运行中"),
    JOB_STATUS_EXCEPTION(5002,"作业状态异常"),
    JOB_NOT_RUNNING(5003,"作业未在运行中"),
    VERSION_IS_NULL(5004,"作业版本为空"),
    VERSION_NOT_EXIST(5005,"作业版本不存在"),
    LOAD_JAR_EXCEPTION(5006,"加载UDF JAR异常"),
    YARN_NAME_EXIST(5007,"yarn已存在该jobName"),
    VALIDATE_JOB_NAME_FAIL(5008,"校验jobName失败，异常：%s"),
    ACQUIRE_JOB_LOCK_FAIL(5009,"获取作业操作锁失败"),
    JOB_NOT_EXISTS(5010,"任务不存在"),
    JOB_PARAMS_MISSING(5011,"启动任务参数缺失:[s%]"),
    ACTION_NOT_ALLOW(5012,"平台发版中，请5分钟后尝试，如10分钟以上不可用，请联系系统管理员或运维处理"),
    JOB_START_ERROR(5013,"作业启动失败"),

    //7500-8000为作业开发模块状态码
    FOLDER_NOT_NULL(7500,"目录不为空"),
    FOLDER_IS_EXISTS(7501,"目录已存在,不能重名"),
    FILE_IS_LOCKED(7502,"文件已锁定，请先解锁"),
    PARAM_IS_MISSING(7503,"必要参数%s缺失"),

    FILE_IS_EXISTS(8500, "作业已存在,不能重名"),
    JOB_IS_EXISTS(8501, "该文件已生成作业,不能删除"),
    SQL_NOT_PASS(8502, "验证失败:SQL未通过校验"),
    YAML_CONFIG_ERROR(8503, "yaml配置解析异常"),
    FILE_CONFORM_RULE(8504, "作业名称:%s,不符合规则"),
    FILE_CANNOT_CHANGE(8505, "作业名称不能更改"),
    FILE_NOT_EXISTS(8506, "找不到对应的文件"),
    UDF_FUNCTION_EXCEPTION(8507, "UDF函数依赖JAR未找到"),
    YAML_CONF_ILLEGAL(8508,"yaml参数无效，含有：%s"),
    YAML_CONF_EMPTY(8509,"yaml参数不能为空"),
    META_TABLE_EMPTY(8510,"元表ddl语句为空"),
    ETL_SQL_EMPTY(8511,"业务逻辑语句不能为空"),
    FILE_NOT_NULL(8512,"作业名称不为空"),
    FILE_DDL_EXCEPTION(8513,"业务逻辑中不能包含DDL操作"),
    APPLY_EXISTS_EXCEPTION(8514,"当前作业已提交审批，请先撤销再提交"),
    KAFKA_PERMISSION_FILED(8515,"kafka权限校验不通过"),
    META_TABLE_NOT_EXISTS(8516,"create table 名称对应元表不存在"),
    FILE_IS_APPROVING(8517, "该文件在审批中,不能删除,请先撤销申请"),
    JAR_DOWNLOAD_ERROR(8518, "JAR文件下载异常"),
    DAG_GENERATOR_ERROR(8519, "文件DAG生成失败"),
    DAG_PARAMS_MISS(8520, "生成DAG必要参数%s缺失"),
    DIS_ALLOW_IP(8521, "当前写入的是来源表，请谨慎操作!"),
    SQL_GRAMMAR_ERROR(8522, "SQL语法有误:%s"),
    UN_SUPPORT_NOTES(8523,"注释不支持使用//"),
    SQL_WARN_PASS(11006, "验证成功:存在警告问题需要处理"),
    SQL_OK_PASS(0, "验证成功"),

    //9000-9500为操作日志文件模块状态码
    JOB_ID_NULL(9001, "传入的作业id为空"),
    LOG_LACK_ARGUMENT(9002, "参数缺失"),
    LOG_NO_UPDATES(9003,"运行日志无更新"),
    COMMIT_YARN_FAILED(9004,"提交yarn失败，未返回任务id"),
    COMMIT_K8S_FAILED(9005,"提交k8s失败，未返回任务id"),

    //9500-10000数据源管理模块状态码
    EMPTY_DATASOURCE_ARGUMENT(9501, "入参项不含数据库"),
    DATASOURCE_NAME_INVALID(9502, "数据源名称不符合规则"),
    DATASOURCE_TYPE_INVALID(9503, "不支持该数据源类型"),
    DATASOURCE_NAME_EXIst(9504, "数据源名称已存在"),
    PARAMS_CHANGE_NOT_ALLOW(9506,"数据库名称不可修改"),
    MISS_PERMISSIONS(9507,"用户权限不足"),
    DATASOURCE_IS_USING(9508,"数据源正在使用，无法删除"),
    CONNECT_IS_FAILED(9509,"数据源验证失败"),
    CONNECT_IS_WRONG(9510,"%s"),
    PARAM_IS_WRONG(9511,"数据源搜索关键字长度限制4个字符"),
    //10000-10500元表模块状态码
    META_TABLE_PARAMS_INVALID(10000,"入参不符合"),
    METATABLE_NOT_REPETITION(10001,"元表引用名称不能重复"),
    METATABLE_ID_CANNOT_BE_EMPTY(10002,"元表id不能为空"),
    METATABLE_SQL_PARSE_ERROR(10003,"元表配置->SQL解析异常"),

    //10500-11000告警规则
    RULENAME_ALREADY_EXISTS(10501,"规则名称已存在"),

    WHITELIST_IS_EXISTS(11001,"白名单已存在,账号不能重复"),
    WHITELIST_INCLUDE_ADMIN(11002,"系统管理员默认在白名单内, 无需再添加"),
    WHITELIST_ADMIN_NOT_DEL(11003,"系统管理员不可移除白名单"),
    WHITELIST_ONLINE_TIP(11004,"上线成功，请打开作业运维查看"),

    COMMON_ERR(11005,"%s"),
    LOSE_ENV(11008,"缺失env")
    ;

    private int code;
    private String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }
}
