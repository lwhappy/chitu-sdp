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


insert into sink_hbase select CONCAT_WS('_', SUBSTRING(MD5(f_random_str), 0, 6), f_random_str),ROW(f_sequence,f_random,f_random_str,ts) from datagen_source;