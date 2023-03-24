/*
 * @Author: hjg
 * @Date: 2022-02-12 11:09:44
 * @LastEditTime: 2022-02-16 15:59:10
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \x6\example\src\utils\graph\data.js
 */
export const graphData = {
  nodes: [{
    id: 1,
    type: 'Source: KafkaSource-default_catalog.default_database.order_info',
    pact: 'Data Source',
    contents: 'Source: KafkaSource-default_catalog.default_database.order_info',
    parallelism: 1
  }, {
    id: 2,
    type: 'Calc(select=[DATE_FORMAT(create_time, _UTF-16LE\'yyyy-MM-dd HH:00\') AS dt, area_code, order_id])',
    pact: 'Operator',
    contents: 'Calc(select=[DATE_FORMAT(create_time, _UTF-16LE\'yyyy-MM-dd HH:00\') AS dt, area_code, order_id])',
    parallelism: 1,
    predecessors: [{
      id: 1,
      ship_strategy: 'FORWARD',
      side: 'second'
    }]
  }, {
    id: 4,
    type: 'GroupAggregate(groupBy=[dt, area_code], select=[dt, area_code, COUNT(order_id) AS order_num])',
    pact: 'Operator',
    contents: 'GroupAggregate(groupBy=[dt, area_code], select=[dt, area_code, COUNT(order_id) AS order_num])',
    parallelism: 1,
    predecessors: [{
      id: 2,
      ship_strategy: 'HASH',
      side: 'second'
    }]
  }, {
    id: 5,
    type: 'NotNullEnforcer(fields=[dt, area_code])',
    pact: 'Operator',
    contents: 'NotNullEnforcer(fields=[dt, area_code])',
    parallelism: 1,
    predecessors: [{
      id: 4,
      ship_strategy: 'FORWARD',
      side: 'second'
    }]
  }, {
    id: 6,
    type: 'Sink: Sink(table=[default_catalog.default_database.order_statistic], fields=[dt, area_code, order_num])',
    pact: 'Data Sink',
    contents: 'Sink: Sink(table=[default_catalog.default_database.order_statistic], fields=[dt, area_code, order_num])',
    parallelism: 1,
    predecessors: [{
      id: 5,
      ship_strategy: 'FORWARD',
      side: 'second'
    }]
  }]
}

export const graphDatas = {
  nodes: [{
    id: 1,
    type: 'Source: KafkaSource-default_catalog.default_database.order_info',
    pact: 'Data Source',
    contents: 'Source: KafkaSource-default_catalog.default_database.order_info',
    parallelism: 1
  }, {
    id: 3,
    type: 'Source: KafkaSource-default_catalog.default_database.vms_device_repair_record -&gt; MiniBatchAssigner(interval=[10000ms], mode=[ProcTime]) -&gt; Calc(select=[plate_number, repair_status, repair_no], where=[(enabled_flag = 1)])',
    pact: 'Data Source',
    contents: 'Source: KafkaSource-default_catalog.default_database.vms_device_repair_record -&gt; MiniBatchAssigner(interval=[10000ms], mode=[ProcTime]) -&gt; Calc(select=[plate_number, repair_status, repair_no], where=[(enabled_flag = 1)])',
    parallelism: 1
  }, {
    id: 2,
    type: 'Calc(select=[DATE_FORMAT(create_time, _UTF-16LE\'yyyy-MM-dd HH:00\') AS dt, area_code, order_id])',
    pact: 'Operator',
    contents: 'Calc(select=[DATE_FORMAT(create_time, _UTF-16LE\'yyyy-MM-dd HH:00\') AS dt, area_code, order_id])',
    parallelism: 1,
    predecessors: [{
      id: 1,
      ship_strategy: 'HASH',
      side: 'second'
    }, {
      id: 3,
      ship_strategy: 'HASH',
      side: 'second'
    }]
  }, {
    id: 4,
    type: 'Source: KafkaSource-default_catalog.default_database.order_info',
    pact: 'Data Source',
    contents: 'GroupAggregate(groupBy=[dt, area_code], select=[dt, area_code, COUNT(order_id) AS order_num])',
    parallelism: 1
  }, {
    id: 5,
    type: 'NotNullEnforcer(fields=[dt, area_code])',
    pact: 'Operator',
    contents: 'NotNullEnforcer(fields=[dt, area_code])',
    parallelism: 1,
    predecessors: [{
      id: 2,
      ship_strategy: 'HASH',
      side: 'second'
    }, {
      id: 4,
      ship_strategy: 'HASH',
      side: 'second'
    }]
  }, {
    id: 6,
    type: 'Sink: Sink(table=[default_catalog.default_database.order_statistic], fields=[dt, area_code, order_num])',
    pact: 'Data Sink',
    contents: 'Sink: Sink(table=[default_catalog.default_database.order_statistic], fields=[dt, area_code, order_num])',
    parallelism: 1,
    predecessors: [{
      id: 5,
      ship_strategy: 'HASH',
      side: 'second'
    }]
  }]
}

