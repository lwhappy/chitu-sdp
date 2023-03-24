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

CREATE TABLE `sink_es` (
  `f_sequence` BIGINT,
  `f_random` BIGINT,
  `f_random_str` VARCHAR,
  `ts` VARCHAR,
  PRIMARY KEY (`f_sequence`) NOT ENFORCED -- 主键字段
) WITH (
  'connector' = '******',
  'hosts' = '******',
  'username' = '******',
  'password' = '******',
  'index' = 'sink_test',
  'document-type' = 'sink_test'
);

insert into sink_es(f_sequence,f_random,f_random_str,ts) select * from datagen_source;