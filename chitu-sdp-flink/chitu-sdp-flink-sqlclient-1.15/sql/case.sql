CREATE TABLE `user_ticket_order_source` (
   binlog_json VARCHAR
  ,proctime as PROCTIME()
) WITH (
'connector' = 'kafka',
'properties.bootstrap.servers' = '******',
'topic' = 'crm_activity-user_ticket_order_test',
'properties.group.id' = 'sdp-bigdata_marketing_platform-market_platform_activity_realtime_statistic_test',
'scan.startup.mode' = 'latest-offset',
'scan.topic-partition-discovery.interval' = '10000',
'format' = 'raw'
);

CREATE TABLE `dwd_mark_coupon_receive_use_detail_dim` (
   `coupon_code` VARCHAR,
  `detail_id` INT,
  `activity_id` INT,
  `coupon_type` INT,
  `use_status` INT,
  `waybill_number` VARCHAR,
  `waybill_amount` DECIMAL(20, 2),
  `deduction_amount` DECIMAL(20, 2),
  `deduction_date` VARCHAR,
  PRIMARY KEY (`coupon_code`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = '******',
  'password' = '******',
  'table-name' = '******'
);

CREATE TABLE `coupon_order_info_sink` (
  `coupon_code` VARCHAR,
  `use_status` INT,
  `waybill_number` VARCHAR,
  `deduction_amount` DECIMAL(20, 2),
  `deduction_date` VARCHAR,
  PRIMARY KEY (`coupon_code`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = '******',
  'password' = '******',
  'table-name' = '******'
);

CREATE TABLE `coupon_order_info_sink_print` (
  `coupon_code` VARCHAR,
  `use_status` INT,
  `waybill_number` VARCHAR,
  `deduction_amount` DECIMAL(20, 2),
  `deduction_date` VARCHAR,
  PRIMARY KEY (`coupon_code`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'print'
);

CREATE TABLE `coupon_detail_id_dim` (
  `detail_id` INT,
  `coupon_code` VARCHAR,
  `activity_id` INT,
  `coupon_type` INT,
  `use_status` INT,
  `waybill_number` VARCHAR,
  `waybill_amount` DECIMAL(20, 2),
  `deduction_amount` DECIMAL(20, 2),
  `deduction_date` VARCHAR
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = '******',
  'password' = '******',
  'table-name' = '******'
);

CREATE TABLE `activity_detail_use_coupon_sink` (
    `id` INT,
  `use_coupon_time` VARCHAR,
  `use_coupon_count` INT,
  `revenue_amount` DECIMAL(20, 2),
  `deduction_amount` DECIMAL(20, 2),
  PRIMARY KEY (`id`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = '******',
  'password' = '******',
  'table-name' = '******'
);

CREATE TABLE `activity_detail_use_coupon_sink_print` (
    `id` INT,
  `use_coupon_time` VARCHAR,
  `use_coupon_count` INT,
  `revenue_amount` DECIMAL(20, 2),
  `deduction_amount` DECIMAL(20, 2),
  PRIMARY KEY (`id`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'print'
);

CREATE TABLE `xyj_detail_coupon_use_modify_view_detail_print` (
   v1_detail_id INT,
   v1_coupon_code VARCHAR,
   t2_coupon_code VARCHAR,
   t2_deduction_amount DECIMAL(20, 2),
   v1_deduction_amount DECIMAL(20, 2),
   t2_waybill_amount DECIMAL(20, 2),
   v1_deduction_time VARCHAR,
   t2_deduction_date VARCHAR
) WITH (
  'connector' = 'print'
);

CREATE TEMPORARY FUNCTION JsonAnalyze as 'com.chitu.bigdata.sdp.flink.client.JsonAnalyze';

-- 轩辕剑送券优惠券的绑券和用券数据视图
create view xyj_coupon_waybill_view
    as
select
       json_value(t1.jsonStr,'$.ticket_no')              as coupon_code
      ,json_value(t1.jsonStr,'$.waybill_number')         as waybill_number
      ,json_value(t1.jsonStr,'$.deduction_time')         as deduction_time
      ,cast(json_value(t1.jsonStr,'$.deduction_amount')  as  DECIMAL(20, 2))
                                                         as deduction_amount
      ,json_value(t1.jsonStr,'$.binlog_type')            as data_type
      ,t2.detail_id                                      as detail_id

      ,t0.proctime                                       as proctime
  from user_ticket_order_source t0
  join lateral table(JsonAnalyze(binlog_json))       as t1(jsonStr) on true
  join dwd_mark_coupon_receive_use_detail_dim  for system_time as of t0.proctime t2
    on  json_value(t1.jsonStr,'$.ticket_no') = t2.coupon_code
 where json_value(t1.jsonStr,'$.ticket_no') like '%XY'
  and ( json_value(t1.jsonStr,'$.binlog_type') = 'INSERT'
    or( json_value(t1.jsonStr,'$.binlog_type') = 'UPDATE'
              and json_value(t1.jsonStr,'$.deduction_time_old') = '-9999'
              and json_value(t1.jsonStr,'$.deduction_time') is not null))   -- 新增，或者抵扣时间从空变为有值
 ;



 -- 把优惠券运单绑定数据，或者优惠券的抵扣数据写入优惠券表
insert into coupon_order_info_sink
select
   coupon_code
  ,if(data_type = 'INSERT',1,2) as use_status
  ,waybill_number
  ,deduction_amount  as deduction_amount
  ,deduction_time as deduction_date
  from xyj_coupon_waybill_view;


--   insert into coupon_order_info_sink_print
-- select
--    coupon_code
--   ,if(data_type = 'INSERT',1,2) as use_status
--   ,waybill_number
--   ,deduction_amount  as deduction_amount
--   ,deduction_time as deduction_date
--   from xyj_coupon_waybill_view;


    -- 合并 买券、用券、反审单等应触发明细表金额修改的视图
   create view xyj_detail_coupon_use_modify_view_detail
as
   select
   v1.detail_id as v1_detail_id,
   v1.coupon_code as v1_coupon_code,
   t2.coupon_code t2_coupon_code,
   t2.deduction_amount as t2_deduction_amount,
   v1.deduction_amount as v1_deduction_amount,
   t2.waybill_amount as t2_waybill_amount,
   v1.deduction_time as v1_deduction_time,
   t2.deduction_date as t2_deduction_date
     from xyj_coupon_waybill_view v1
     join coupon_detail_id_dim  for system_time as of v1.proctime as t2
       on v1.detail_id = t2.detail_id
    where v1.data_type = 'UPDATE' -- 抵扣
     and (t2.use_status = 2 or v1.coupon_code = t2.coupon_code)


--   create view xyj_detail_coupon_use_modify_view
-- as
--    select v1.detail_id
--          ,count(1)                                            as use_coupon_count
--          ,sum(if(v1.coupon_code = t2.coupon_code,0,t2.deduction_amount)) + min(v1.deduction_amount)
--                                                               as deduction_amount  --  当前优惠券的抵扣金额取最新的，v1的发散的 直接min取一条就好了
--          ,sum(t2.waybill_amount)                              as revenue_amount
--          ,least(min(v1.deduction_time),ifnull(min(t2.deduction_date),'9999'))    as use_coupon_time
--          ,min(v1.proctime)                                         as proctime
--      from xyj_coupon_waybill_view v1
--      join coupon_detail_id_dim  for system_time as of v1.proctime as t2
--        on v1.detail_id = t2.detail_id
--     where v1.data_type = 'UPDATE' -- 抵扣
--      and (t2.use_status = 2 or v1.coupon_code = t2.coupon_code)
--  group by v1.detail_id;



  create view xyj_detail_coupon_use_modify_view
as
   select max(v1_detail_id) as v1_detail_id
         ,count(1)                                            as use_coupon_count
--          ,sum(if(v1_coupon_code = t2_coupon_code,0,t2_deduction_amount)) + min(v1_deduction_amount) as deduction_amount  --  当前优惠券的抵扣金额取最新的，v1的发散的 直接min取一条就好了
         ,sum(if(v1_coupon_code = t2_coupon_code,v1_deduction_amount,t2_deduction_amount)) as deduction_amount
         ,sum(t2_waybill_amount)                              as revenue_amount
         ,least(min(v1_deduction_time),ifnull(min(t2_deduction_date),'9999'))    as use_coupon_time
     from xyj_detail_coupon_use_modify_view_detail
 group by v1_coupon_code;




-- 合并后的数据写入明细表
insert into xyj_detail_coupon_use_modify_view_detail_print
select
*
from xyj_detail_coupon_use_modify_view_detail;






insert into activity_detail_use_coupon_sink_print
select
       v1_detail_id as id
      ,use_coupon_time
      ,cast(use_coupon_count as int ) as use_coupon_count
      ,cast(revenue_amount as DECIMAL(20, 2)) as revenue_amount
      ,cast(deduction_amount as DECIMAL(20, 2)) as deduction_amount
from xyj_detail_coupon_use_modify_view;