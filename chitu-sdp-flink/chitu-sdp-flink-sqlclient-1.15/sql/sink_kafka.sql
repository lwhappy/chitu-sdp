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

insert into sink_kafka(f_sequence,f_random,f_random_str,ts) select * from datagen_source;