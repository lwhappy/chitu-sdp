CREATE TABLE `kafka_source` (
  id bigint,
  name string
) WITH (
  'connector' = 'kafka',
  'properties.bootstrap.servers' = '******',
  'topic' = 'sdp_test',
  'properties.group.id' = 'sdp-example_demo-kafka_source_test',
  'scan.startup.mode' = 'latest-offset',
  'scan.topic-partition-discovery.interval' = '10000',
  'format' = 'json',
  'json.fail-on-missing-field' = 'false',  -- 为true时 序列化字段值为空 则报错
  'json.ignore-parse-errors' = 'false'     -- 没修改源码前，为false时 序列化数据 结构或者类型 不对时 则报错
);

CREATE TABLE `sink_print` (
  id bigint,
  name string
) WITH (
  'connector' = 'print'
);

insert into sink_print select * from kafka_source;