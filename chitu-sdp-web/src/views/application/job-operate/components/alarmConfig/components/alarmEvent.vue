<!--
 * @Author: hjg
 * @Date: 2021-11-08 22:12:19
 * @LastEditTime: 2022-07-20 15:37:57
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\alarmConfig\components\alarmEvent.vue
-->
<template>
  <div class="alarm-event">
    <a-table class="tale-data"
             v-loading="isLoading"
             v-defaultPage="!tableData || (tableData && tableData.length === 0)"
             row-key="id"
             bordered
             ref="alarmEventTable"
             :columns="columns"
             :data-source="tableData"
             :loading="loading"
             :pagination="false"
             :scroll="{y: 'calc(100% - 55px)'}"
             @change="handleTableChange">
      <!-- 规则名称 -->
      <div slot="ruleName"
           class="rule-name"
           :title="record.ruleName"
           slot-scope="text,record">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span>{{record.ruleName}}</span>
          </template>
          <span>{{ record.ruleName }}</span>
        </a-tooltip>
      </div>
      <!-- 告警规则 -->
      <div slot="indexName"
           slot-scope="text,record">{{ record.indexName | filterIndex }}</div>
      <!-- 告警事件 -->
      <div slot="alertContent"
           class="alert-content"
           slot-scope="text,record">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span>{{record.alertContent}}</span>
          </template>
          <span>{{ record.alertContent }}</span>
        </a-tooltip>
      </div>
    </a-table>
    <!-- 分页 -->
    <div class="footer-page">
      <Pagination :pagination="pagination"
                  @pageChange="pageChange"
                  @pageSizeChange="pageSizeChange" />
    </div>
  </div>
</template>
<script>
  import Pagination from '@/components/pagination/index'
  // import tableHeaderDrag from '../../../../../../mixins/table-header-drag'
  import tableSort from '../../../../../../mixins/table-sort'
  export default {
    name: 'alarmEvent',
    mixins: [tableSort],
    components: {
      Pagination
    },
    props: {
      activeName: {
        type: String,
        value: ''
      }
    },
    filters: {
      filterIndex (value) { // 指标过滤
        let text = '重启次数'
        if (value === 'NUMBER_CHECKPOINT') {
          text = 'checkpoint次数'
        } else if (value === 'DELAY') {
          text = '延迟'
        } else if (value === 'INTERRUPT_OPERATION') {
          text = '作业运行中断'
        } else if (value === 'KAFKA_FAIL_MSG') {
          text = 'topic数据解析失败'
        } else if (value === 'WAIT_RECORDS') {
          text = '待消费数'
        } else if (value === 'BACKPRESSURED') {
          text = '背压'
        } else if (value === 'CONSUME_RECORDS') {
          text = '最近N小时消费数'
        }
        return text
      }
    },
    computed: {
      columns () {
        return [{
          title: '规则名称',
          dataIndex: 'ruleName',
          width: 214,
          scopedSlots: { customRender: 'ruleName' }
        },
        {
          title: '告警时间',
          dataIndex: 'creationDate',
          defaultSortOrder: 'descend',
          sorter: () => this.handleTableChange,
          width: 214,
          scopedSlots: { customRender: 'creationDate' },
          sortOrder: this.sortedInfo.columnKey === 'creationDate' && this.sortedInfo.order
        },
        {
          title: '告警规则',
          dataIndex: 'indexName',
          width: 214,
          scopedSlots: { customRender: 'indexName' }
        },
        {
          title: '告警事件',
          dataIndex: 'alertContent',
          width: 214,
          scopedSlots: { customRender: 'alertContent' }
        }]
      }
    },
    data () {
      return {
        isLoading: false,
        headerDragData: {
          columnsName: 'columns',
          ref: 'alarmEventTable'
        },
        tableSortData: {
          columnsName: 'columns',
          ref: 'alarmEventTable'
        },
        tableData: [],
        loading: false,
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0,
          pageSizeOptions: ['10', '20', '40', '60']
        },
        params: {
          jobId: null,
          orderByClauses: [{
            field: "creation_date", //排序键名
            orderByMode: 1 //排序模式（0：正序，1：倒序）
          }],
          page: 1,
          pageSize: 20
        }
      }
    },
    watch: {
      activeName: {
        async handler (val) {
          if (val === 'alarmEvent') {
            this.init()
          }
        },
        immediate: true
      }
    },
    created () {

    },
    methods: {
      init () {
        this.getAlarmEventList(this.params)
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        // console.log('------handleTableChange:', pagination, filters, sorter)
        // console.log('------handleTableChange:', pagination, filters, sorter)
        this.resetSortMethods(sorter)
        sorter = this.sortedInfo
        if (sorter.order) {
          if (sorter.order === 'ascend') {
            this.params.orderByClauses[0].orderByMode = 0
          } else {
            this.params.orderByClauses[0].orderByMode = 1
          }
        }
        this.getAlarmEventList(this.params)
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.params.page = pageInfo.page
        this.getAlarmEventList(this.params)
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
        this.params.page = pageSizeInfo.current
        this.params.pageSize = pageSizeInfo.size
        this.getAlarmEventList(this.params)
      },
      // 获取告警事件列表
      async getAlarmEventList (params) {
        params.jobId = this.$store.getters.jobInfo.id
        this.isLoading = true
        let res = await this.$http.post('/alert/record/list', params, {
          headers: {
            projectId: this.$store.getters.jobInfo.projectId
          }
        })
        // console.log('getAlarmEventList-res: ', res)
        this.isLoading = false
        if (res.code === 0) {
          this.resetPagination(res.data)
          this.tableData = res.data.list
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 重置分页信息
      resetPagination (pageInfo) {
        this.pagination.current = pageInfo.pageNum
        this.pagination.total = pageInfo.total
      },
    }
  }
</script>
<style lang="scss" scoped>
  .alarm-event {
    width: 100%;
    height: 100%;
    .tale-data {
      height: calc(100% - 72px);
      /deep/ .ant-table-thead > tr > th {
        padding: 12px 16px;
        font-weight: 700;
        font-size: 12px;
        border-right: 1px solid #e8e8e8 !important;
      }
      /deep/ .ant-table-tbody > tr > td {
        padding: 7px;
      }
      /deep/ .ant-table-placeholder {
        visibility: hidden;
      }
    }
    /deep/ .ant-table-wrapper {
      height: calc(100% - 72px);
      .ant-spin-nested-loading {
        height: 100%;
        .ant-spin-container {
          height: 100%;
          .ant-table {
            height: 100%;
            .ant-table-content {
              height: 100%;
              .ant-table-scroll {
                overflow: hidden;
                height: 100%;
                .ant-table-body {
                  height: 100%;
                }
              }
            }
          }
        }
      }
    }
    .rule-name {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .alert-content {
      color: red;
    }
    .footer-page {
      height: 72px;
      padding: 20px 16px;
    }
  }
</style>
