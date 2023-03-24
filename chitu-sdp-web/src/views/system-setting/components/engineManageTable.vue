<!--
 * @Author: hjg
 * @Date: 2021-10-17 19:35:06
 * @LastEditTime: 2022-07-25 19:39:37
 * @LastEditors: Please set LastEditors
 * @Description: 引擎管理数据表格
 * @FilePath: \src\views\system-setting\components\engineManageTable.vue
-->
<template>
  <div class="engine-manage-table">
    <chitu-table v-loading="isLoading"
               row-key="id"
               ref="engineManageTable"
               :columns="columns"
               :data-source="tableDataEngine"
               @change="handleTableChange"
               :pagination="pagination"
               @pageChange="pageChange"
               @pageSizeChange="pageSizeChange">
      <template #userCount="{record}">
        <div class="use-count">
          <div class="exist"
               @click="showEnginerManageDialog(record)">
            <i class="chitutree-h5 chitutreechengyuan"></i>{{ record.engineUsers.length }}
          </div>
        </div>
      </template>
      <template #referProjectCount="{record}">
        <div class="refer-project-count"
             @click="openEngineUseCount(record)">
          项目数: {{ record.referProjectCount }}
        </div>
      </template>
      <template #operation="{record}">
        <div class="operation">
          <span @click="openEngineDetail(record)">详情</span>
          <a-divider type="vertical"></a-divider>
          <span class="delete-color"
                @click="handleDeleteEngine(record)">
            <i class="chitutree-h5 chitutreeshanchu"></i>删除
          </span>
        </div>
      </template>
    </chitu-table>
    <confirm-dialog :visible="deleteVisible"
                    type="warning"
                    @close="deleteVisible=false"
                    @confirm="deleteEngine(deleteItem)">
      <template>
        <p class="word-break">确定要<span class="warn-message">&nbsp;删除&nbsp;</span>吗？</p>
      </template>
    </confirm-dialog>
    <!-- 添加引擎 -->
    <add-engine-dialog ref="dialogVisible"
                       @confirmEvent="confirmEvent" />
    <!-- 引擎使用者 -->
    <engine-user-manage ref="engineUserDialog"
                        @confirm="engineUserConfirm"
                        @cancel="engineUserCancel" />
    <!-- 项目引用数 -->
    <engine-projects ref="engineProjects"
                     v-if="isShowProjectsDialog"
                     :isShowProjectsDialog="isShowProjectsDialog"
                     :engineInfo="engineInfo"
                     @closeProjectModal="closeProjectModal" />
    <!-- 引擎详情 -->
    <engine-detail ref="engineDetail" />
  </div>
</template>

<script>
  import addEngineDialog from './addEngineDialog.vue'
  import engineUserManage from './engineUserManage.vue'
  import engineProjects from './engineProjects.vue'
  import engineDetail from './engineDetail.vue'
  import ConfirmDialog from '@/components/confirm-dialog'
  // import tableHeaderDrag from '../../../mixins/table-header-drag'
  import tableSort from '../../../mixins/table-sort'
  export default {
    name: 'engineManageTable',
    mixins: [tableSort],
    props: {
      keyword: {
        type: Object,
        default: () => {
          return {}
        }
      }
    },
    components: {
      addEngineDialog,
      engineUserManage,
      engineProjects,
      engineDetail,
      ConfirmDialog
    },
    watch: {
      keyword: {
        handler (value, oldVaue) {
          console.log('---------watch-keyword: ', value, oldVaue)
          if (!value.engineName) {
            this.params.vo.engineName = null
          } else {
            this.params.vo.engineName = value.engineName
          }
          this.params.page = 1
          this.getEngineManagerList(this.params)
        },
        deepp: true
      },
      params: {
        handler (value, oldVaue) {
          console.log('---------watch-params: ', value, oldVaue)
        },
        deep: true
      }
    },
    computed: {
      columns () {
        return [{
          title: '引擎名称',
          width: 268,
          dataIndex: 'engineName'
        },
        {
          title: '引擎队列',
          width: 268,
          dataIndex: 'engineQueue',
          ellipsis: true
        },
        {
          title: '引擎版本',
          dataIndex: 'engineVersion',
          sortDirections: ['descend', 'ascend'],
          width: 268,
          sorter: () => this.handleTableChange,
          sortOrder: this.sortedInfo.columnKey === 'engineVersion' && this.sortedInfo.order
        },
        {
          title: '创建时间',
          dataIndex: 'creationDate',
          defaultSortOrder: 'descend',
          sortDirections: ['descend', 'ascend'],
          scopedSlots: { customRender: 'creationDate' },
          width: 268,
          sorter: () => this.handleTableChange,
          sortOrder: this.sortedInfo.columnKey === 'creationDate' && this.sortedInfo.order
        },
        {
          title: '引擎使用者',
          dataIndex: 'userCount',
          sortDirections: ['descend', 'ascend'],
          sorter: () => this.handleTableChange,
          width: 268,
          scopedSlots: { customRender: 'userCount' },
          sortOrder: this.sortedInfo.columnKey === 'userCount' && this.sortedInfo.order
        },
        {
          title: '项目引用数',
          dataIndex: 'referProjectCount',
          sortDirections: ['descend', 'ascend'],
          sorter: () => this.handleTableChange,
          width: 268,
          scopedSlots: { customRender: 'referProjectCount' },
          sortOrder: this.sortedInfo.columnKey === 'referProjectCount' && this.sortedInfo.order
        },
        {
          title: '操作',
          dataIndex: 'operation',
          width: 263,
          scopedSlots: { customRender: 'operation' }
        }]
      }
    },
    data () {
      return {
        deleteItem: null,
        deleteVisible: false,
        headerDragData: {
          columnsName: 'columns',
          ref: 'engineManageTable'
        },
        tableSortData: {
          columnsName: 'columns',
          ref: 'engineManageTable'
        },
        tableDataEngine: [],
        isLoading: false,
        pagination: {
          current: 5,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0,
          pageSizeOptions: ['10', '20', '40', '60']
        },
        params: {
          orderByClauses: [{
            field: "creation_date", //排序键名
            orderByMode: 1 //排序模式（0：正序，1：倒序）
          }],
          page: 1,
          pageSize: 20,
          vo: {
            engineName: null
          }
        },
        isShowProjectsDialog: false,
        engineInfo: {}
      }
    },
    methods: {
      // 打开引擎使用数弹框
      openEngineUseCount (engineInfo) {
        this.isShowProjectsDialog = true
        this.engineInfo = engineInfo
      },
      // 关闭引擎使用数弹框
      closeProjectModal () {
        this.isShowProjectsDialog = false
      },
      // 打开引擎详情
      openEngineDetail (engineInfo) {
        this.$refs.engineDetail.open(engineInfo)
      },
      handleDeleteEngine (item) {
        this.deleteItem = item
        this.deleteVisible = true
      },
      // 删除系统管理员
      async deleteEngine (engineInfo) {
        const params = { id: engineInfo.id }
        let res = await this.$http.post('/setting/engineSetting/delete', params)
        // console.log(res)
        if (res.code === 0) {
          this.getEngineManagerList(this.params)
          this.$message.success({ content: '删除成功', duration: 2 })
        } else {
          this.$message.error(res.msg)
        }
      },
      // 重置分页信息
      resetPagination (pageInfo) {
        this.pagination.current = pageInfo.page
        this.pagination.total = pageInfo.rowTotal
      },
      engineUserConfirm (data) {
        // console.log('confirm', data)
        if (data) this.getEngineManagerList(this.params)
      },
      engineUserCancel (data) {
        console.log('cancel', data)
        this.getEngineManagerList(this.params)
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        // console.log('------handleTableChange:', pagination, filters, sorter)
        let sortObj = {}
        this.params.orderByClauses = []
        this.resetSortMethods(sorter)
        sorter = this.sortedInfo
        if (sorter.order) {
          if (sorter.field === 'engineVersion') {
            sortObj.field = 'engine_version'
          } else if (sorter.field === 'creationDate') {
            sortObj.field = 'creation_date'
          } else if (sorter.field === 'userCount') {
            sortObj.field = 'user_count'
          } else if (sorter.field === 'referProjectCount') {
            sortObj.field = 'refer_project_count'
          }
          // sorter.order = this.sortedInfo.order
          if (sorter.order === 'ascend') {
            sortObj.orderByMode = 0
          } else {
            // 探索：只有升序和降序
            sortObj.orderByMode = 1
          }
          this.params.orderByClauses.push(sortObj)
          this.getEngineManagerList(this.params)
        }
      },
      // 展示引擎使用者弹框
      showEnginerManageDialog (record) {
        this.$refs.engineUserDialog.open(record)
        // // console.log('------showEnginerManageDialog:', record)
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.params.page = pageInfo.page
        this.getEngineManagerList(this.params)
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
        this.params.page = pageSizeInfo.current
        this.params.pageSize = pageSizeInfo.size
        this.getEngineManagerList(this.params)
      },
      // 获取引擎列表
      async getEngineManagerList (params) {
        this.isLoading = true
        let res = await this.$http.post('/setting/engineSetting/engineInfo', params)
        this.isLoading = false
        if (res.code === 0) {
          this.tableDataEngine = [...res.data.rows]
          this.resetPagination(res.data)
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      confirmEvent (data) {
        if (data) {
          this.params.page = 1
          this.params.orderByClauses[0].field = 'creation_date'
          this.params.orderByClauses[0].orderByMode = 1
          this.getEngineManagerList(this.params)
        }
      }
    },
    mounted () {

    },
    created () {
      this.getEngineManagerList(this.params)
    }
  }
</script>

<style lang="scss" scoped>
  .engine-manage-table {
    width: 100%;
    height: 100%;
    color: #333;
    font-size: 14px;
    font-weight: 400;
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
    .tale-data {
      height: calc(100% - 72px);
      // overflow-y: scroll;
      /deep/ .ant-table-thead > tr > th {
        padding: 10.5px 16px;
        font-weight: 700;
        font-size: 12px;
      }
      /deep/ .ant-table-tbody > tr > td {
        padding: 6px 16px;
      }
    }
    .use-count,
    .operation {
      // font-size: 14px;
      font-weight: 400;
      color: #0066ff;
      .exist {
        cursor: pointer;
        i {
          font-size: 18px !important;
        }
      }
      .noexit {
        color: #ccc;
        cursor: default;
      }
      .delete {
        cursor: pointer;
      }
      i {
        margin-right: 6px;
      }
    }
    .refer-project-count {
      // font-size: 14px;
      font-weight: 400;
      color: #0066ff;
      cursor: pointer;
    }
    .creation-date {
      // font-size: 14px;
      // color: #ccc;
    }
    .operation {
      span {
        margin-right: 16px;
        cursor: pointer;
      }
    }
    .footer-page {
      height: 72px;
      padding: 20px 16px;
    }
  }
</style>
