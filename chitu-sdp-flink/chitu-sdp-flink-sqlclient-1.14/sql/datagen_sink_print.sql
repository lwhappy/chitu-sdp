CREATE TABLE `source_datagen` (
 f_sequence INT,
 f_random INT,
 f_random_str STRING,
 ts AS localtimestamp,
 WATERMARK FOR ts AS ts
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

CREATE TABLE `sink_print` (
 f_sequence INT,
 f_random INT,
 f_random_str STRING,
 ts timestamp
) WITH (
  'connector' = 'print'
);

insert into sink_print select * from source_datagen;