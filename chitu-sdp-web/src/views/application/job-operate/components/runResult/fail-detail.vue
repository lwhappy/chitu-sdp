<!--
 * @Author: lw
 * @Date: 2021-11-08 21:32:02
 * @LastEditTime: 2022-01-13 17:18:42
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \bigdata-sdp-frontend2\src\views\application\job-operate\components\operateLog\index.vue
-->
<template>
  <div class="fail-detail">

    <p class="bread">
      <span class="blue back"
            @click="back">运行结果</span>
      ><span>写入失败</span>
    </p>
    <a-table class="tale-data"
             row-key="id"
             ref="operateLogTable"
             :columns="columns"
             :data-source="tableData"
             :loading="loading"
             :pagination="false"
             :scroll="{y: 'calc(100% - 55px)'}"
             @change="handleTableChange">
      <span class="data"
            slot="data"
            slot-scope="text,record">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span>{{record.data}}</span>
          </template>
          <span>{{record.data}}</span>
        </a-tooltip>
      </span>
      <span class="desc"
            slot="desc"
            slot-scope="text,record">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span>{{record.desc}}</span>
          </template>
          <span>{{record.desc}}</span>
        </a-tooltip>
      </span>
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
  export default {
    name: 'FailDetail',
    props: {
      showList: {
        type: Function,
        default: () => {

        }
      }

    },
    components: {
      Pagination
    },
    mixins: [],
    computed: {


    },
    data () {
      return {
        columns: [],
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
        order: 1,
        params: {
          sourceType: null,
          tableName: null,
          jobId: null,
          page: 1,
          pageSize: 20,
          orderByClauses: [{
            field: "ts",
            orderByMode: 1
          }],
        }

      }
    },
    created () {
    },
    methods: {
      back () {
        this.showList()
      },
      init (data) {
        if (data.dataSourceType === 'doris') {
          this.columns = [{
            title: '失败条数',
            dataIndex: 'batchCount',
            scopedSlots: { customRender: 'batchCount' }
          },
          {
            title: '异常原因',
            dataIndex: 'desc',
            scopedSlots: { customRender: 'desc' }
          },
          {
            title: '异常时间',
            dataIndex: 'failTime',
            scopedSlots: { customRender: 'failTime' },
            defaultSortOrder: 'descend',
            sortDirections: ['ascend', 'descend', 'ascend'],
            sorter: (a, b) => a.failTime - b.failTime
          }]
        } else {
          this.columns = [{
            title: '异常数据',
            dataIndex: 'data',
            scopedSlots: { customRender: 'data' }
          },
          {
            title: '异常原因',
            dataIndex: 'desc',
            scopedSlots: { customRender: 'desc' }
          },
          {
            title: '异常时间',
            dataIndex: 'failTime',
            scopedSlots: { customRender: 'failTime' },
            defaultSortOrder: 'descend',
            sortDirections: ['ascend', 'descend', 'ascend'],
            sorter: (a, b) => a.failTime - b.failTime
          }]
        }
        this.tableData = []
        this.pagination.total = 0
        this.params.jobId = this.$store.getters.jobInfo.id
        this.params.sourceType = data.dataSourceType
        this.params.tableName = data.metatableName
        this.params.databaseName = data.databaseName
        this.getData()
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        if (sorter.order === 'ascend') {
          this.order = 0
          this.getData()
        } else if (sorter.order === 'descend') {
          this.order = 1
          this.getData()
        }
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.params.page = pageInfo.page
        this.getData(this.params)
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
        this.params.page = pageSizeInfo.current
        this.params.pageSize = pageSizeInfo.size
        this.getData(this.params)
      },
      // 获取操作日志列表
      async getData () {
        this.params.orderByClauses[0].orderByMode = this.order
        let res = await this.$http.post('/runResult/queryFailData', this.params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.tableData = res.data.list
          this.pagination.total = res.data.total
        }
      },

    }
  }
</script>
<style lang="scss" scoped>
  .fail-detail {
    width: 100%;
    height: 100%;
    .bread {
      margin: 0 0 12px 0;
      font-weight: 600;
      font-size: 12px;
      .back {
        cursor: pointer;
      }
      span {
        margin: 3px 0;
      }
    }

    .tale-data {
      height: calc(100% - 72px);
      overflow-y: hidden;
      /deep/ .ant-table-thead > tr > th {
        padding: 12px 16px;
        font-weight: 700;
        font-size: 12px;
      }
      /deep/ .ant-table-tbody > tr > td {
        padding: 7px;
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
    .footer-page {
      height: 72px;
      padding: 20px 16px;
    }
  }
</style>
