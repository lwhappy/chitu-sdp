package com.chitu.bigdata.sdp.api.enums;

/**
 * @author 587694
 */

public enum  ConnectorReviewConf {
    /**
     * kafka的消费组前缀
     */
    GROUP_ID("sdp_review"),
    TRUNCATE_TABLE_SQL("truncate table %s"),
    CHECK_DATA_LOST("select order_id from (select order_id,update_time from %s  order by order_id asc) t where (select 1 from %s where order_id = t.order_id-1 ) is null and t.update_time < ?;"),
    CHECK_DORIS_LOST("select b.order_id from (select order_id-1 as order_id,update_time from %s)b where b.order_id not in (select a.order_id from %s a inner join (select order_id-1 as order_id from %s)t on a.order_id = t.order_id ) and b.update_time < ?;"),
    CHECK_DATA_EQUALS("select * from %s where order_id in (%s);"),
    JOIN_DATA_LOST("select mcd.order_id from %s mcd where not exists (select  mcj.order_id from %s mcj where mcj.order_id = mcd.order_id) and mcd.order_id <(select max(order_id) from %s where current_time < ? );"),
    CANNAL_JOB_ID("1118"),
    NORMAL_JOB_ID("1120"),
    NORMAL_KAFKA_DATA("normal"),
    CANNAL_KAFKA_DATA("cannal"),
    NORMAL_DIM_DATA("dim_normal"),
    JOIN_MYSQL_DATA("join_mysql"),
    CONTROLLER_RUN("1"),
    CONTROLLER_STOP("0"),
    DATA_EQUALS_INTERVAL("300000"),
    DATA_LOST_INTERVAL("900000"),
    LOST_CHECK_INTERVAL("400000"),
    REDIS_KEY("connector::mysql"),
    DIM_KEY("connector::dim"),
    RUN_CONNECTOR_JOB("RUN::JOB"),
    RUN_DIM_JOB("RUN::DIM"),
    REGRESSION_SUCCESS("SUCCESS"),
    REGRESSION_FAIL("FAIL"),
    MYSQL_TYPE("mysql"),
    REGRESSION_DATA_EQUALS("data_equals"),
    REGRESSION_DATA_LOST("data_lost"),

    ;
    private String type;

    ConnectorReviewConf(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
