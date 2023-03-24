<template>
  <div ref="compareEditor"
       class="compareEditor"></div>
</template>
<script>
  import * as monaco from 'monaco-editor'
  export default {
    props: {
      height: {
        type: Number,
        default: 600
      }
    },
    data () {
      return {
        defaultOpts: { // 主要配置
          value: '', // 编辑器的值
          theme: 'vs', // 编辑器主题：vs, hc-black, or vs-dark，更多选择详见官网
          roundedSelection: true, // 右侧不显示编辑器预览框
          autoIndent: true, // 自动缩进
          readOnly: true, // 是否只读
          language: 'sql', // 语言
          automaticLayout: true
        },
        oldValue: null,
        newValue: null
      }
    },
    methods: {
      // 设置编辑器
      setEditor (language, oldValue, newValue) {
        // setEditor() {
        // 初始化container的内容，销毁之前生成的编辑器
        this.$refs.compareEditor.innerHTML = ''
        this.defaultOpts.language = language
        // 初始化编辑器实例
        this.monacoDiffInstance = monaco.editor.createDiffEditor(this.$refs['compareEditor'], this.defaultOpts)
        // let oldSql = 'CREATE TABLE bigdata_report_search_record_uat (\r\n    code VARCHAR    --接口\r\n    ,title VARCHAR  --接口名称\r\n    ,dbType VARCHAR --数据源类型\r\n    ,executeTime BIGINT     --执行时长，毫秒\r\n    ,`status` BOOLEAN       --查询情况，true or false\r\n    ,employeeId VARCHAR     --员工id，访问者？责任人？\r\n    ,startTime VARCHAR\r\n  ) WITH (\r\n  ata_report_search_record_uat,szzb-bg-uat-etl-12:9092\r\n\r\n\r\nCREATE TABLE s_exception_detail (\r\n    stat_date VARCHAR \r\n    ,code VARCHAR    --接口\r\n    ,title VARCHAR  --接口名称\r\n    ,dbType VARCHAR --数据源类型\r\n    ,executeTime BIGINT     --执行时长，毫秒\r\n    ,`status` VARCHAR       --查询情况，true or false\r\n    ,employeeId VARCHAR     --员工id，访问者？责任人？\r\n    ,startTime VARCHAR\r\n) WITH (\r\n  '//10.83.192.4:3306/streamx',\r\n  'connector.table' = 's_exception_detail',\r\n  'connector.username' = 'root',\r\n  'connector.password' = 'chitu2021!'\r\n);\r\n\r\nCREATE TABLE s_summary (\r\n    stat_date VARCHAR    \r\n    ,all_cnt BIGINT  \r\n    ,code_cnt BIGINT\r\n    ,PRIMARY KEY (stat_date) NOT ENFORCED\r\n) WITH (\r\n  'connector.type' = 'jdbc', \r\n  'connector.url' = 'jdbc:mysql://10.83.192.4:3306/streamx',\r\n  'connector.table' = 's_summary',\r\n  'connector.username' = 'root',\r\n  'connector.password' = 'chitu2021!'\r\n);\r\n\r\nINSERT INTO s_summary\r\nSELECT\r\n  stat_date\r\n  ,sum(cnt) as all_cnt\r\n  ,count(1) as code_cnt\r\nfrom (\r\n  SELECT\r\n    sum(1) as cnt\r\n    ,code\r\n    ,substring(startTime, 1, 10) as stat_date\r\n  from bigdata_report_search_record_uat\r\n  group by code, substring(startTime, 1, 10)\r\n)\r\ngroup by stat_date\r\n;\r\n\r\nINSERT INTO s_exception_detail\r\nSELECT\r\n  substring(startTime, 1, 10) as stat_date\r\n  ,code\r\n  ,title\r\n  ,dbType\r\n  ,executeTime\r\n  ,cast(`status` as VARCHAR)\r\n  ,employeeId\r\n  ,startTime\r\nFROM bigdata_report_search_record_uat\r\nwhere status is not true\r\n   or (executeTime is not null and executeTime >= 2000)\r\n;\r\n\r\n\r\n\r\n\r\n'
        // let newSql = 'CREATE TABLE bigdata_report_search_record_uat (\r\n   \r\n \r\n   code VARCHAR    --接口\r\n    ,title VARCHAR  --接口名称\r\n    ,dbType VARCHAR --数据源类型\r\n    ,executeTime BIGINT     --执行时长，毫秒\r\n    ,`status` BOOLEAN       --查询情况，true or false\r\n    ,employeeId VARCHAR     --员工id，访问者？责任人？\r\n    ,startTime VARCHAR\r\n  ) WITH (\r\n  ata_report_search_record_uat,szzb-bg-uat-etl-12:9092\r\n\r\n\r\nCREATE TABLE s_exception_detail (\r\n    stat_date VARCHAR \r\n    ,code VARCHAR    --接口\r\n    ,title VARCHAR  --接口名称\r\n    ,dbType VARCHAR --数据源类型\r\n    ,executeTime BIGINT     --执行时长，毫秒\r\n    ,`status` VARCHAR       --查询情况，true or false\r\n    ,employeeId VARCHAR     --员工id，访问者？责任人？\r\n    ,startTime VARCHAR\r\n) WITH (\r\n  '//10.83.192.4:3306/streamx',\r\n  'connector.table' = 's_exception_detail',\r\n  'connector.username' = 'root',\r\n  'connector.password' = 'chitu2021!'\r\n);\r\n\r\nCREATE TABLE s_summary (\r\n    stat_date VARCHAR    \r\n    ,all_cnt BIGINT  \r\n    ,code_cnt BIGINT\r\n    ,PRIMARY KEY (stat_date) NOT ENFORCED\r\n) WITH (\r\n  'connector.type' = 'jdbc', \r\n  'connector.url' = 'jdbc:mysql://10.83.192.4:3306/streamx',\r\n  'connector.table' = 's_summary',\r\n  'connector.username' = 'root',\r\n  'connector.password' = 'chitu2021!'\r\n);\r\n\r\nINSERT INTO s_summary\r\nSELECT\r\n  stat_date\r\n  ,sum(cnt) as all_cnt\r\n  ,count(1) as code_cnt\r\nfrom (\r\n  SELECT\r\n    sum(1) as cnt\r\n    ,code\r\n    ,substring(startTime, 1, 10) as stat_date\r\n  from bigdata_report_search_record_uat\r\n  group by code, substring(startTime, 1, 10)\r\n)\r\ngroup by stat_date\r\n;\r\n\r\nINSERT INTO s_exception_detail\r\nSELECT\r\n  substring(startTime, 1, 10) as stat_date\r\n  ,code\r\n  ,title\r\n  ,dbType\r\n  ,executeTime\r\n  ,cast(`status` as VARCHAR)\r\n  ,employeeId\r\n  ,startTime\r\nFROM bigdata_report_search_record_uat\r\nwhere status is not true\r\n   or (executeTime is not null and executeTime >= 2000)\r\n;\r\n\r\n\r\n\r\n\r\n'
        this.oldValue = oldValue
        this.newValue = newValue
        this.monacoDiffInstance.setModel({
          // oldValue为以前的值
          original: monaco.editor.createModel(this.oldValue, this.defaultOpts.language),
          // oldValue为新的值
          modified: monaco.editor.createModel(this.newValue, this.defaultOpts.language)
        })
      },
      clearEditor () {
        this.$refs.compareEditor.innerHTML = ''
      }
    }
  }
</script>
<style lang="scss" scoped>
  .container {
    width: 100%;
    height: 100%;
    overflow: hidden;
    .compareEditor {
      height: 100%;
      /deep/ .monaco-diff-editor .diffViewport {
        background: none;
      }
      // /deep/ .diffOverview {
      //   background: #fff;
      //   display: none;
      //   .diffViewport {
      //     background: #fff;
      //     canvas {
      //       display: none;
      //     }
      //   }
      // }
      // /deep/ .vs {
      //   width: calc(100% + 10px);
      // }
    }
  }
</style>
