CREATE CATALOG `sink_hive`
WITH (
  'type' = 'hive',
  'hive-conf-dir' = '/opt/apache/hive-bigdata/conf',
  'hadoop-conf-dir' = '/opt/apache/hadoop-bigdata/etc/hadoop/'
);

-- CREATE CATALOG `sink_hive`
-- WITH (
--   'type' = 'hive',
--   'hive-conf-dir' = '/opt/apache/hive-one-ticket/conf',
--   'hadoop-conf-dir' = '/opt/apache/hadoop-one-ticket/etc/hadoop'
-- );

CREATE TABLE `sink_print` (
 f_sequence INT,
 f_random INT,
 f_random_str STRING,
 ts STRING
) WITH (
  'connector' = 'print'
);

-- insert into sink_print select f_sequence,f_random,f_random_str,ts from sink_hive.sdp_test.sdp_hive_0922 where ts > '2022-09-21';

insert into sink_print select f_sequence,f_random,f_random_str,ts from sink_hive.sdp_test.bigdata_hive_0922 where dt >= '2022-09-21';