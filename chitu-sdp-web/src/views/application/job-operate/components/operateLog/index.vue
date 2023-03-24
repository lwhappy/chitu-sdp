<!--
 * @Author: hjg
 * @Date: 2021-11-08 21:32:02
 * @LastEditTime: 2022-01-13 17:18:42
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \bigdata-sdp-frontend2\src\views\application\job-operate\components\operateLog\index.vue
-->
<template>
  <div class="operate-log">
    <a-table class="tale-data"
             v-loading="isLoading"
             v-defaultPage="!tableData || (tableData && tableData.length === 0)"
             row-key="id"
             ref="operateLogTable"
             :columns="columns"
             :data-source="tableData"
             :loading="loading"
             :pagination="false"
             :scroll="{y: 'calc(100% - 55px)'}"
             @change="handleTableChange">
      <div slot="status"
           class="rule-name"
           slot-scope="text,record">
        <span v-if="record.status === '成功'">{{ record.status }}</span>

        <span v-else
              style="color:red;cursor:pointer"
              @click="popTip(record)">{{ record.status }}</span>

      </div>
    </a-table>
    <!-- 分页 -->
    <div class="footer-page">
      <Pagination :pagination="pagination"
                  @pageChange="pageChange"
                  @pageSizeChange="pageSizeChange" />
    </div>
    <tip-dialog :visible="popConfirm"
                type="warning"
                title="失败原因"
                @close="popConfirm=false"
                @confirm="popConfirm=false">
      <div>{{errMessage}}</div>
    </tip-dialog>
  </div>
</template>
<script>
  import Pagination from '@/components/pagination/index'
  import tableSort from '../../../../../mixins/table-sort'
  import TipDialog from '@/components/tip-dialog'

  export default {
    name: 'operateLog',
    components: {
      Pagination, TipDialog
    },
    mixins: [tableSort],
    computed: {
      columns () {
        return [{
          title: '操作事件',
          dataIndex: 'action',
          scopedSlots: { customRender: 'action' }
        },
        {
          title: '操作状态',
          dataIndex: 'status',
          scopedSlots: { customRender: 'status' }
        },
        {
          title: '操作人',
          dataIndex: 'createdBy',
          scopedSlots: { customRender: 'createdBy' }
        },
        {
          title: '操作时间',
          dataIndex: 'creationDate',
          defaultSortOrder: 'descend',
          sorter: () => this.handleTableChange,
          scopedSlots: { customRender: 'creationDate' },
          sortOrder: this.sortedInfo.columnKey === 'creationDate' && this.sortedInfo.order
        }]
      }
    },
    data () {
      return {
        isLoading: false,
        errMessage: '',
        popConfirm: false,
        tableData: [],
        loading: false,
        tableSortData: {
          columnsName: 'columns',
          ref: 'operateLogTable'
        },
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0,
          pageSizeOptions: ['10', '20', '40', '60']
        },
        params: {
          orderByClauses: [{
            field: 'creation_date',
            orderByMode: 1 //排序模式（0：正序，1：倒序）
          }],
          jobId: null,
          page: 1,
          pageSize: 20
        }
      }
    },
    props: {
      activeName: {
        type: String,
        value: ''
      }
    },
    watch: {
      activeName: {
        async handler (val) {
          if (val === 'operateLog') {
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
        this.getOperateLogList(this.params)
      },
      popTip (record) {
        this.popConfirm = true
        this.errMessage = record.message
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        this.resetSortMethods(sorter)
        sorter = this.sortedInfo
        if (sorter.order) {
          if (sorter.order === 'ascend') {
            this.params.orderByClauses[0].orderByMode = 0
          } else {
            this.params.orderByClauses[0].orderByMode = 1
          }
        }
        this.getOperateLogList(this.params)
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.params.page = pageInfo.page
        this.getOperateLogList(this.params)
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
        this.params.page = pageSizeInfo.current
        this.params.pageSize = pageSizeInfo.size
        this.getOperateLogList(this.params)
      },
      // 获取操作日志列表
      async getOperateLogList (params) {
        params.jobId = this.$store.getters.jobInfo.id
        this.isLoading = true
        let res = await this.$http.post('/log/searchByJobId', params)
        this.isLoading = false
        if (res.code === 0) {
          this.tableData = res.data.rows
          this.resetPagination(res.data)
        }
      },
      // 重置分页信息
      resetPagination (pageInfo) {
        this.pagination.current = pageInfo.page
        this.pagination.total = pageInfo.rowTotal
      }
    }
  }
</script>
<style lang="scss" scoped>
  .operate-log {
    width: 100%;
    height: 100%;
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
