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
  'table-name' = '******',
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

insert into sink_doris(f_sequence,f_random,f_random_str,ts) select * from datagen_source;