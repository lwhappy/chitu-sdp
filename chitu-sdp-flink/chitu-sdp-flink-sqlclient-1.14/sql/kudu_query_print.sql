CREATE TABLE `source_kudu` (
  `f_sequence` BIGINT,
  `f_random` BIGINT,
  `f_random_str` STRING,
  `ts` STRING
) WITH (
'connector' = 'kudu',
'kudu.masters' = '******',
'kudu.table' = 'impala::sdp_test.sink_test'
);

CREATE TABLE `sink_print` (
 f_sequence BIGINT,
 f_random BIGINT,
 f_random_str STRING,
 ts STRING
) WITH (
  'connector' = 'print'
);

insert into sink_print(f_sequence,f_random,f_random_str,ts) select * from source_kudu;