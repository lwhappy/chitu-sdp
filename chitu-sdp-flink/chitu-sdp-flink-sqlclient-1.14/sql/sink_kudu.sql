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

insert into sink_kudu(f_sequence,f_random,f_random_str,ts) select * from datagen_source;