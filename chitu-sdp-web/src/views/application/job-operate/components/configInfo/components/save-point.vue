<template>
  <div class="save-point-config">
    <a-table class="tale-data"
             v-loading="isLoading"
             v-defaultPage="!tableData || (tableData && tableData.length === 0)"
             row-key="id"
             :columns="columns"
             :data-source="tableData"
             :loading="loading"
             :pagination="false"
             :scroll="{y: 'calc(100% - 55px)'}"
             bordered
             @change="handleTableChange">
      <div slot="operateStatus"
           class="rule-name"
           slot-scope="text,record">
        <span style="color: #33cc22;"
              v-if="record.operateStatus === 'SUCCESS'">成功</span>

        <span v-else
              style="color:#F95353;cursor:pointer"
              @click="popTip(record)">失败</span>
      </div>
      <span slot="savepointName"
            slot-scope="text,record">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span>{{record.savepointName}}</span>
          </template>
          <span>{{ record.savepointName }}</span>
        </a-tooltip>
      </span>
      <span slot="filePath"
            slot-scope="text,record">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span>{{record.filePath}}</span>
          </template>
          <span>{{ record.filePath }}</span>
        </a-tooltip>
      </span>
    </a-table>
    <!-- 分页 -->
    <!-- <div class="footer-page">
      <Pagination :pagination="pagination"
                  @pageChange="pageChange"
                  @pageSizeChange="pageSizeChange" />
    </div> -->
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
  // import Pagination from '@/components/pagination/index'
  import TipDialog from '@/components/tip-dialog'

  export default {
    name: 'SavePointConfig',
    components: {
      TipDialog
    },
    mixins: [],
    computed: {
      columns () {
        return [{
          title: '编号',
          width: '5%',
          key: 'orderNum',
          dataIndex: 'orderNum',
          scopedSlots: { customRender: 'orderNum' }
        },
        {
          title: '保存点名称',
          width: '20%',
          key: 'savepointName',
          dataIndex: 'savepointName',
          scopedSlots: { customRender: 'savepointName' }
        },
        {
          title: '保存点路径',
          width: '37%',
          key: 'filePath',
          dataIndex: 'filePath',
          scopedSlots: { customRender: 'filePath' }
        },
        {
          title: '状态',
          width: '5%',
          key: 'operateStatus',
          dataIndex: 'operateStatus',
          defaultSortOrder: 'descend',
          scopedSlots: { customRender: 'operateStatus' },
        }, {
          title: '触发时间',
          width: '18%',
          key: 'triggerTime',
          dataIndex: 'triggerTime',
          defaultSortOrder: 'descend',
          scopedSlots: { customRender: 'triggerTime' },
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
          if (val === 'savePointConfig') {
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
        this.getList()
      },
      popTip (record) {
        this.popConfirm = true
        this.errMessage = record.operateErrMsg
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        sorter = this.sortedInfo
        if (sorter.order) {
          if (sorter.order === 'ascend') {
            this.params.orderByClauses[0].orderByMode = 0
          } else {
            this.params.orderByClauses[0].orderByMode = 1
          }
        }
        this.getList()
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.params.page = pageInfo.page
        this.getList()
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
        this.params.page = pageSizeInfo.current
        this.params.pageSize = pageSizeInfo.size
        this.getList()
      },
      // 获取操作日志列表
      async getList () {
        const jobId = this.$store.getters.jobInfo.id
        const url = `/savepoint/query?jobId=${jobId}`
        this.isLoading = true
        let res = await this.$http.get(url, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          this.tableData = res.data
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
  .save-point-config {
    width: 100%;
    height: 100%;
    .tale-data {
      height: calc(100% - 72px);
      overflow-y: hidden;
      /deep/ .ant-table-thead > tr > th {
        padding: 12px 16px;
        font-weight: 700;
        font-size: 12px;
        border-right: 1px solid #e8e8e8 !important;
      }
      /deep/ .ant-table-placeholder {
        visibility: hidden;
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
