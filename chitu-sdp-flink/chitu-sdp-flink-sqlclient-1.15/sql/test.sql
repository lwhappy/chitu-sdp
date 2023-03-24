CREATE TABLE `source_bigdata_customer_repeat_audit` (
  `id` INT,
  `customer_id` INT,
  `audit_id` BIGINT,
  `audit_name` VARCHAR,
  `audit_result` VARCHAR,
  `remark` VARCHAR,
  `audit_code` VARCHAR,
  enabled_flag TINYINT,
  PRIMARY KEY (`id`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = '******',
  'password' = '******',
  'table-name' = '******'
);

CREATE TABLE `print` (

  `customer_id` int,
  `repeat_flag` int
) WITH (
  'connector' = 'print'
);

CREATE TABLE `bigdata_customer_repeat_audit` (
  `id` int,
  `customer_id` int,
  `audit_id` VARCHAR,
  `audit_name` VARCHAR,
  `audit_time` VARCHAR,
  `audit_result` VARCHAR,
  `remark` VARCHAR,
  `enabled_flag` VARCHAR,
  `created_by` VARCHAR,
  `creation_date` VARCHAR,
  `updated_by` VARCHAR,
  `updation_date` VARCHAR,
  `trace_id` VARCHAR,
  `audit_code` VARCHAR,
  proctime as PROCTIME()
) WITH (
  'connector' = 'kafka',
  'properties.bootstrap.servers' = '******',
  'topic' = '******',
  'properties.group.id' = 'sdp-crm_business-print_repeat_test',
  'scan.startup.mode' = 'earliest-offset',
  'scan.topic-partition-discovery.interval' = '10000',
  'format' = 'canal-json',
  'canal-json.ignore-parse-errors' = 'false' -- 不可更改，false表示平台开启解析失败监控
);
CREATE TABLE `source_bigdata_customer_repeat_result` (
  `id` INT,
  `customer_id` INT,
  `customer_short_name` VARCHAR,
  `customer_code` VARCHAR,
  `customer_name` VARCHAR,
  `location_num` INT,
  `mobile_num` INT,
  `invoice_num` INT,
  `customer_name_num` INT,
  `insert_date` DATE,
  `bd_creation_date` TIMESTAMP,
  `bd_updation_date` TIMESTAMP,
  `record_time` TIMESTAMP,
  `cooperate_time` TIMESTAMP,
  `pay_type` VARCHAR,
  `last_send_time` TIMESTAMP,
  `last_pay_time` TIMESTAMP,
  `market_id` BIGINT,
  `market_name` VARCHAR,
  `assistant_id` BIGINT,
  `assistant_name` VARCHAR,
  `assistant_leader_id` BIGINT,
  `assistant_leader_name` VARCHAR,
  `calculation_id` BIGINT,
  `calculation_name` VARCHAR,
  `senior_manager_id` BIGINT,
  `senior_manager_name` VARCHAR,
  `elementary_calculation_id` BIGINT,
  `elementary_calculation_name` VARCHAR,
  `customer_channel` VARCHAR,
  `address` VARCHAR,
  `data_time` TIMESTAMP,
  `etl_date` DATE,
  `pickup_address` VARCHAR,
  `deduction_count` INT,
  `deduction_company` VARCHAR,
  `repeat_result` VARCHAR,
  `batch_number` VARCHAR,
  `verify_state` VARCHAR,
  `mobile_verify_state` VARCHAR,
  `invoice_verify_state` VARCHAR,
  `location_verify_state` VARCHAR,
  `repeat_info_phone` VARCHAR,
  `audit_status` VARCHAR,
  `repeat_flag` VARCHAR,
  `enabled_flag` TINYINT,
  `created_by` VARCHAR,
  `creation_date` TIMESTAMP,
  `updated_by` VARCHAR,
  `updation_date` TIMESTAMP,
  `trace_id` VARCHAR,
  `second_cooperate_time` TIMESTAMP,
  `develop_time` TIMESTAMP,
  `operation_type` INT,
  `business_flag` VARCHAR,
  `verify_time` TIMESTAMP,
  `market_org_path` VARCHAR,
  `customer_name_verify_state` VARCHAR,
  `last_follow_time` TIMESTAMP,
  `last_follow_remark` VARCHAR,
  `audit_code` VARCHAR,
  `repeat_tag` TINYINT,
  `customer_status` VARCHAR,
  `assignment_person` VARCHAR,
  `customer_status_reason` VARCHAR,
  PRIMARY KEY (`id`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = 'app_bigdata_sdp_rw',
  'password' = '******',
  'table-name' = 'bigdata_customer_repeat_result'
);





create view tmp_01
as
select s2.customer_id
    , s2.repeat_flag as repeat_flag
from (select t0.customer_id
                , case when (t1.audit_code='0' and t1.enabled_flag=1)
                             or (t1.audit_code = t2.audit_code and t2.audit_result='60' and t1.enabled_flag=1 and t2.enabled_flag=1)
                           then 1 else 0 end as repeat_flag  -- 0-正常，1-预警
     from bigdata_customer_repeat_audit t0
     left join source_bigdata_customer_repeat_result FOR SYSTEM_TIME AS OF t0.proctime t1
       on t0.customer_id = t1.customer_id
     left join source_bigdata_customer_repeat_audit FOR SYSTEM_TIME AS OF t0.proctime t2
       on t0.customer_id = t2.customer_id
       -- 未审核 或者 审核结果是重复不可算新客
         )  s2;


     insert into print(customer_id,repeat_flag)
     select
     customer_id,max(repeat_flag)
     from tmp_01 group by customer_id;


