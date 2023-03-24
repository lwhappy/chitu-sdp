CREATE TABLE `sdp_job` (
  `id` BIGINT,
  `job_name` VARCHAR
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = '******',
  'password' = '******',
  'table-name' = 'sdp_job'
);

CREATE TABLE `sdp_job_instance` (
  `job_id` BIGINT,
  `flink_job_id` VARCHAR,
  is_latest int
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://******',
  'username' = '******',
  'password' = '******',
  'table-name' = 'sdp_job_instance'
);

CREATE TABLE `sink_doris` (
  `id` BIGINT,
  `flink_job_id` VARCHAR,
  `job_name` VARCHAR,
  PRIMARY KEY (id) NOT ENFORCED
) WITH (
  'connector' = 'starrocks',
  'jdbc-url' = 'jdbc:mysql://******',
  'load-url' = '******',
  'username' = '******',
  'password' = '******',
  'database-name' = 'moonfall_demo',
  'table-name' = 'sdp_job',
  'sink.buffer-flush.max-rows' = '1000000',
  'sink.buffer-flush.max-bytes' = '300000000',
  'sink.buffer-flush.interval-ms' = '15000',
  'sink.buffer-flush.enqueue-timeout-ms' = '600000',
  'sink.semantic' = 'at-least-once',
  'sink.properties.format' = 'json',            -- 1.15不写默认是csv格式
  'sink.properties.strip_outer_array' = 'true',
  'sink.max-retries' = '1',
  'sink.parallelism' = '1'
);


insert into sink_doris(id,flink_job_id,job_name)
select j.id,i.flink_job_id,j.job_name
from sdp_job j join sdp_job_instance i on i.job_id=j.id
where i.is_latest=1;