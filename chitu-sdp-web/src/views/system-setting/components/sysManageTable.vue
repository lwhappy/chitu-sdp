<!--
 * @Author: hjg
 * @Date: 2021-10-17 19:34:34
 * @LastEditTime: 2022-07-25 19:39:41
 * @LastEditors: Please set LastEditors
 * @Description: 系统管理员数据表格
 * @FilePath: \src\views\system-setting\components\sysManageTable.vue
-->
<template>
  <div class="sys-manage-table">
    <!-- :components="components" -->
    <chitu-table v-loading="isLoading"
               ref="sysManageTable"
               row-key="id"
               :columns="columns"
               :dataSource="tableData"
               @change="handleTableChange"
               :pagination="pagination"
               @pageChange="pageChange"
               @pageSizeChange="pageSizeChange">
      <template #userName="{record}">
        <div class="user-info">
          <i class="chitutree-h5 chitutreexitongguanliyuan"></i>
          {{ record.userName }}
        </div>
      </template>
      <template #creationDate="{record}">
        <div class="creation-date">
          {{ record.creationDate }}
        </div>
      </template>
      <template #isAdmin="{record}">
        <div class="is-admin">
          {{ record.isAdmin | filterRole }}
        </div>
      </template>
      <template #operation="{record}">
        <div class="operation">
          <div class="delete-disabled"
               v-if="record.id === userId">
            <i class="chitutree-h5 chitutreeshanchu"></i>移除
          </div>
          <div v-else
               @click="handleDeleteSysManager(record)">
            <i class="chitutree-h5 chitutreeshanchu"></i>移除
          </div>
        </div>
      </template>
    </chitu-table>
    <!-- 删除成员 -->
    <confirm-dialog :visible="deleteVisible"
                    type="warning"
                    @close="deleteVisible=false"
                    @confirm="deleteSysManager(deleteItem)">
      <template>
        <p class="word-break">确定要<span class="warn-message">&nbsp;移除&nbsp;</span>管理员吗？</p>
      </template>
    </confirm-dialog>
    <add-sys-dialog ref="dialogVisible"
                    @confirmEvent="confirmEvent"
                    @cancelEvent="cancelEvent" />
  </div>
</template>

<script>
  import addSysDialog from './addSysDialog.vue'
  import ConfirmDialog from '@/components/confirm-dialog'
  // import tableHeaderDrag from '../../../mixins/table-header-drag'
  import tableSort from '../../../mixins/table-sort'
  export default {
    name: 'sysManageTable',
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
      addSysDialog,
      ConfirmDialog
    },
    watch: {
      keyword: {
        handler (value, oldVaue) {
          console.log('---------watch: ', value, oldVaue)
          if (!value.userName) {
            this.params.vo.userName = null
            this.params.vo.employeeNumber = null
          } else {
            this.params.vo.userName = value.userName
            this.params.vo.employeeNumber = value.employeeNumber
          }
          this.params.page = 1
          this.getSysManagerList(this.params)
        },
        deep: true
      }
    },
    computed: {
      columns () {
        return [{
          title: '用户名',
          dataIndex: 'userName',
          width: 150,
          scopedSlots: { customRender: 'userName' }
        },
        {
          title: '创建时间',
          dataIndex: 'creationDate',
          defaultSortOrder: 'descend',
          width: 150,
          sorter: () => this.handleTableChange,
          scopedSlots: { customRender: 'creationDate' },
          sortOrder: this.sortedInfo.columnKey === 'creationDate' && this.sortedInfo.order
        },
        {
          title: '角色',
          dataIndex: 'isAdmin',
          width: 150,
          scopedSlots: { customRender: 'isAdmin' }
        },
        {
          title: '操作',
          dataIndex: 'operation',
          width: 100,
          fixed: "right",
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
          ref: 'sysManageTable'
        },
        tableSortData: {
          columnsName: 'columns',
          ref: 'sysManageTable'
        },
        userId: sessionStorage.getItem('userId'),
        tableData: [],
        isLoading: false,
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
            field: "creation_date", //排序键名
            orderByMode: 1 //排序模式（0：正序，1：倒序）
          }],
          page: 1,
          pageSize: 20,
          vo: {
            userName: null,
            employeeNumber: null
          }
        }
      }
    },
    methods: {
      // 重置分页信息
      resetPagination (pageInfo) {
        this.pagination.current = pageInfo.page
        this.pagination.total = pageInfo.rowTotal
      },
      handleDeleteSysManager (item) {
        this.deleteItem = item
        this.deleteVisible = true
      },
      // 删除系统管理员
      async deleteSysManager (userInfo) {
        const params = { id: userInfo.id }
        let res = await this.$http.post('/setting/userSetting/delete', params)
        // console.log('----------deleteSysManager:', res)
        if (res.code === 0) {
          this.$message.success({ content: '删除成功', duration: 2 })
          this.getSysManagerList(this.params)
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        // console.log('------handleTableChange:', pagination, filters, sorter)
        this.resetSortMethods(sorter)
        sorter = this.sortedInfo
        if (sorter.order) {
          if (sorter.order === 'ascend') {
            this.params.orderByClauses[0].orderByMode = 0
          } else {
            this.params.orderByClauses[0].orderByMode = 1
          }
          this.getSysManagerList(this.params)
        }
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.params.page = pageInfo.page
        this.getSysManagerList(this.params)
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
        this.params.page = pageSizeInfo.current
        this.params.pageSize = pageSizeInfo.size
        this.getSysManagerList(this.params)
      },
      confirmEvent (data) {
        // console.log('---------confirmEvent:', data)
        if (data) {
          this.params.page = 1
          this.params.orderByClauses[0].field = 'creation_date'
          this.params.orderByClauses[0].orderByMode = 1
          this.getSysManagerList(this.params)
        }
      },
      cancelEvent (data) {
        console.log('---------cancelEvent:', data)
      },
      // 获取系统管理员列表
      async getSysManagerList (params) {
        this.isLoading = true
        let res = await this.$http.post('/setting/userSetting/getInfo', params)
        this.isLoading = false
        if (res.code === 0) {
          this.tableData = res.data.rows
          this.resetPagination(res.data)
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      }
    },
    filters: {
      filterRole (value) {
        let text = '系统管理员'
        if (value === 0) {
          text = '普通用户'
        }
        return text
      }
    },
    mounted () { },
    created () {
      // console.log('--------created:', this.params)
      this.getSysManagerList(this.params)
    }
  }
</script>

<style lang="scss" scoped>
  .warn-message {
    color: #faad14;
  }
  .sys-manage-table {
    width: 100%;
    height: 100%;
    color: #333;
    font-size: 14px;
    font-weight: 400;
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
    .user-info {
      font-size: 14px;
      color: #333;
      font-weight: 600;
      i {
        margin-right: 6px;
        color: #ccc;
        font-size: 18px !important;
      }
    }
    .is-admin {
      font-size: 14px;
      color: #333;
    }
    .creation-date {
      font-size: 14px;
      // color: #ccc;
    }
    .operation {
      color: #f95353;
      font-size: 14px;
      font-weight: 400;
      cursor: pointer;
      .delete-disabled {
        color: #ccc;
        cursor: not-allowed;
      }
      i {
        margin-right: 6px;
      }
    }
    .footer-page {
      height: 72px;
      padding: 20px 16px;
    }
  }
</style>
