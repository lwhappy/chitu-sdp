CREATE TABLE `datagen_source` (
 f_sequence INT,
 f_random INT,
 f_random_str STRING,
 ts as DATE_FORMAT(NOW(),'yyyy-MM-dd HH:mm:ss'),
 dt as DATE_FORMAT(NOW(),'yyyy-MM-dd'),
 hr as DATE_FORMAT(NOW(),'HH')
) WITH (
 'connector' = 'datagen',
 'rows-per-second'='1',
 'fields.f_sequence.kind'='sequence',
 'fields.f_sequence.start'='1',
 'fields.f_sequence.end'='1000',
 'fields.f_random.min'='1',
 'fields.f_random.max'='1000',
 'fields.f_random_str.length'='10'
);

CREATE CATALOG `sink_hive`
WITH (
  'type' = 'hive',
  'hive-conf-dir' = '/opt/apache/hive-one-ticket/conf',
  'hadoop-conf-dir' = '/opt/apache/hadoop-one-ticket/etc/hadoop'
);

insert into sink_hive.sdp_test.sdp_hive_0922 select * from datagen_source;


/**
drop table `sdp_test.sdp_hive_0922`

CREATE TABLE `sdp_test.sdp_hive_0922`(
  `f_sequence` int,
  `f_random` int,
  `f_random_str` string,
  `ts` string)
PARTITIONED BY (
  `dt` string,
  `hr` string)
stored as orc TBLPROPERTIES (
  'sink.partition-commit.trigger'='process-time',
  'sink.partition-commit.delay'='0s',
  'sink.partition-commit.policy.kind'='metastore,success-file'
)

select * from `sdp_test.sdp_hive_0922`


注意：

1、没改源码之前，环境变量对应的hdfs和当前hadoop-conf-dir对应的hdfs都会写入文件
2、当前hive-conf-dir对应的hive表查询不到数据，数据会写入环境变量对应的hdfs下


hdfs dfs -ls /user/hive/warehouse/sdp_test.db/sdp_hive_0922




 */