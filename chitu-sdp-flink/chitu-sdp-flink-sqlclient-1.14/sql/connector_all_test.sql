CREATE TABLE `datagen_source` (
 f_sequence INT,
 f_random INT,
 f_random_str STRING,
 ts as DATE_FORMAT(NOW(),'yyyy-MM-dd HH:mm:ss')
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

CREATE TABLE `hive_datagen_source` (
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

CREATE TABLE `sink_mysql` (
  `f_sequence` BIGINT,
  `f_random` BIGINT,
  `f_random_str` VARCHAR,
  `ts` VARCHAR,
  PRIMARY KEY (`f_sequence`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = '******',
  'password' = '******',
  'table-name' = 'sink_test'
);


CREATE TABLE `sink_doris` (
  `f_sequence` BIGINT,
  `f_random` BIGINT,
  `f_random_str` VARCHAR,
  `ts` VARCHAR,
  PRIMARY KEY (f_sequence) NOT ENFORCED
) WITH (
  'connector' = 'starrocks',
  'jdbc-url' = 'jdbc:mysql://******',
  'load-url' = '******',
  'username' = '******',
  'password' = '******',
  'database-name' = '******',
  'table-name' = 'sink_test',
  'sink.buffer-flush.max-rows' = '1000000',
  'sink.buffer-flush.max-bytes' = '300000000',
  'sink.buffer-flush.interval-ms' = '15000',
  'sink.buffer-flush.enqueue-timeout-ms' = '600000',
  'sink.semantic' = 'at-least-once',
  --'sink.properties.format' = 'json',            -- 1.15不写默认是csv格式
  --'sink.properties.strip_outer_array' = 'true',
  'sink.properties.column_separator' = '\x01',    -- csv格式才需要指定
  'sink.properties.row_delimiter' = '\x02',       -- csv格式才需要指定
  'sink.max-retries' = '1',
  'sink.parallelism' = '1'
);

CREATE TABLE `sink_hbase` (
  `rowkey` VARCHAR,
  `f1` ROW<f_sequence bigint,f_random bigint,f_random_str VARCHAR,ts VARCHAR>,
  PRIMARY KEY (`rowkey`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = 'hbase-2.2',
  'table-name' = 'sdp_test:sink_test',
  'zookeeper.quorum' = '******',
  'zookeeper.znode.parent' = '/hbase-sdp'
);


CREATE CATALOG `sink_hive`
WITH (
  'type' = 'hive',
  'hive-conf-dir' = '/opt/apache/hive-one-ticket/conf',
  'hadoop-conf-dir' = '/opt/apache/hadoop-one-ticket/etc/hadoop'
);


CREATE TABLE `sink_kafka` (
  `f_sequence` bigint,
  `f_random` bigint,
  `f_random_str` VARCHAR,
  `ts` VARCHAR
) WITH (
  'connector' = 'kafka',
  'properties.bootstrap.servers' = '******',
  'topic' = 'sdp_test',
  'sink.partitioner' = 'round-robin',
  'format' = 'json',
  'properties.acks' ='all',
  'properties.batch.size' = '16384',
  'properties.linger.ms' = '50',
  'properties.buffer.memory' ='33554432'
);

CREATE TABLE `sink_kudu` (
  `f_sequence` BIGINT,
  `f_random` BIGINT,
  `f_random_str` VARCHAR,
  `ts` VARCHAR
) WITH (
'connector' = 'kudu',
'kudu.masters' = '******',
'kudu.table' = 'impala::sdp_test.sink_test',
'kudu.primary-key-columns' = 'f_sequence'
);




insert into sink_kafka(f_sequence,f_random,f_random_str,ts) select * from datagen_source;

insert into sink_doris(f_sequence,f_random,f_random_str,ts) select * from datagen_source;

insert into sink_hive.sdp_test.sdp_hive_0922 select * from hive_datagen_source;

insert into sink_mysql(f_sequence,f_random,f_random_str,ts) select * from datagen_source;

insert into sink_hbase select CONCAT_WS('_', SUBSTRING(MD5(f_random_str), 0, 6), f_random_str),ROW(f_sequence,f_random,f_random_str,ts) from datagen_source;

insert into sink_kudu(f_sequence,f_random,f_random_str,ts) select * from datagen_source;

