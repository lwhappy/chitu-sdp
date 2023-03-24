<!--
 * @description: 系统设置/引擎管理
 * @Author: lijianguo19
 * @Date: 2022-08-01 10:15:20
 * @FilePath: \src\views\system-setting\engine\index.vue
-->
<template>
  <div class="engine-manage-container">
    <div class="search-container">
      <div class="search-main justify-between">
        <div class="left-content justify-between">
          <p>引擎名称</p>
          <search-autocomplete ref="searchAuto"
                               autoMsg='搜索引擎名称...'
                               :isShowBtn="false"
                               :dataSource="dataSource"
                               @onChange="onChange"
                               @onSelect="onSelect" />
          <a-button @click="search"
                    style="margin-left:8px;"
                    type="primary"
                    size="small"
                    icon="search">
            查询
          </a-button>
          <a-button @click="reset"
                    style="margin-left:8px;"
                    size="small"
                    icon="undo">
            重置
          </a-button>
        </div>
        <div class="right-content justify-end">
          <div class="justify-start product-line">
            <a-button @click="showDialogEvent"
                      type="primary"
                      size="small"
                      icon="plus">
              <span>新建引擎</span>
            </a-button>
          </div>
        </div>
      </div>

    </div>
    <div class="table-container">
      <chitu-table v-loading="isLoading"
                 row-key="id"
                 ref="engineManageTable"
                 :columns="columns"
                 :data-source="tableDataEngine"
                 @change="handleTableChange"
                 :pagination="pagination"
                 @pageChange="pageChange"
                 @pageSizeChange="pageSizeChange">
        <!-- <template #userCount="{record}">
          <div class="use-count">
            <div class="exist"
                 @click="showEnginerManageDialog(record)">
              <i class="chitutree-h5 chitutreechengyuan"></i>{{ record.engineUsers.length }}
            </div>
          </div>
        </template> -->
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
    </div>

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
  import searchAutocomplete from '@/components/search-autocomplete/commonIndex'
  import addEngineDialog from '../components/addEngineDialog.vue'
  import engineUserManage from '../components/engineUserManage.vue'
  import engineProjects from '../components/engineProjects.vue'
  import engineDetail from '../components/engineDetail.vue'
  import ConfirmDialog from '@/components/confirm-dialog'
  import tableSort from '@/mixins/table-sort'
  export default {
    name: 'engineManageTable',
    mixins: [tableSort],
    props: {},
    components: {
      searchAutocomplete,
      addEngineDialog,
      engineUserManage,
      engineProjects,
      engineDetail,
      ConfirmDialog
    },
    watch: {
      '$store.getters.isRemoveTag': {//监听关闭页签，关闭页签后清除缓存
        handler (val, oldVal) {
          if (val === oldVal) {
            return
          }
          if (this.$store.getters.removeRouteName.includes('SystemSettingEngine')) {
            this.$common.toClearCache(this);
          }
        }
      },
      params: {
        handler () {
        },
        deep: true
      }
    },
    computed: {
      columns () {
        return [
          {
            title: '引擎名称',
            width: 268,
            dataIndex: 'engineName'
          },
          // {
          //   title: '系统环境',
          //   width: 110,
          //   dataIndex: 'envType'
          // },
          // {
          //   title: '引擎集群',
          //   width: 180,
          //   dataIndex: 'clusterName'
          // },
          {
            title: '集群部署类型',
            width: 268,
            dataIndex: 'engineType',
            ellipsis: true
          },
          // {
          //   title: '引擎队列',
          //   width: 268,
          //   dataIndex: 'engineQueue',
          //   ellipsis: true
          // },
          // {
          //   title: '引擎版本',
          //   dataIndex: 'engineVersion',
          //   sortDirections: ['descend', 'ascend'],
          //   width: 120,
          //   sorter: () => this.handleTableChange,
          //   sortOrder: this.sortedInfo.columnKey === 'engineVersion' && this.sortedInfo.order
          // },
          {
            title: '创建时间',
            dataIndex: 'creationDate',
            defaultSortOrder: 'descend',
            sortDirections: ['descend', 'ascend'],
            scopedSlots: { customRender: 'creationDate' },
            width: 180,
            sorter: () => this.handleTableChange,
            sortOrder: this.sortedInfo.columnKey === 'creationDate' && this.sortedInfo.order
          },
          // {
          //   title: '引擎使用者',
          //   dataIndex: 'userCount',
          //   sortDirections: ['descend', 'ascend'],
          //   sorter: () => this.handleTableChange,
          //   width: 120,
          //   scopedSlots: { customRender: 'userCount' },
          //   sortOrder: this.sortedInfo.columnKey === 'userCount' && this.sortedInfo.order
          // },
          {
            title: '项目引用数',
            dataIndex: 'referProjectCount',
            sortDirections: ['descend', 'ascend'],
            sorter: () => this.handleTableChange,
            width: 120,
            scopedSlots: { customRender: 'referProjectCount' },
            sortOrder: this.sortedInfo.columnKey === 'referProjectCount' && this.sortedInfo.order
          },
          {
            title: '操作',
            dataIndex: 'operation',
            fixed: 'right',
            width: 120,
            scopedSlots: { customRender: 'operation' }
          }]
      }
    },
    data () {
      return {
        dataSource: [],
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
      reset () {
        this.$refs.searchAuto.defaultValue = null
        this.params.vo.engineName = null
        this.search()
      },
      search () {
        this.params.page = 1
        this.getEngineManagerList(this.params)
      },
      // 输入值变化时搜索补全
      async onChange (value) {
        this.params.vo.engineName = value
        const params = { engineName: value }
        let res = await this.$http.post('/setting/engineSetting/getByName', params)
        if (res.code === 0) {
          res.data.map(item => {
            // item.text = item.engineName + ', ' + item.engineVersion
            item.text = item.engineName
            item.label = item.engineName
          })
          this.dataSource = res.data
        }
      },
      // 选中后进行搜索
      onSelect (value) {
        this.params.vo.engineName = value.engineName
        this.search()
      },
      showDialogEvent () {
        this.$refs.dialogVisible.isShowDialog = true

      },
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
        if (data) this.getEngineManagerList(this.params)
      },
      engineUserCancel () {
        this.getEngineManagerList(this.params)
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
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
      },
      // 分页数据变化
      pageChange (pageInfo) {
        this.params.page = pageInfo.page
        this.getEngineManagerList(this.params)
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
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
  .engine-manage-container {
    width: 100%;
    height: 100%;
    color: #333;
    font-size: 14px;
    font-weight: 400;
    .search-container {
      padding-bottom: 8px;
      background: #eff1f6;
      .search-main {
        height: 56px;

        border-bottom: 1px solid #dee2ea;
        box-sizing: border-box;
        background: #ffffff;
        .left-content {
          font-size: 12px;
          color: #2e2c37;
          p {
            margin: 0 8px 0 20px;
          }
        }
        .right-content {
          .product-line {
            margin-right: 16px;
            p {
              margin: 0 8px 0 20px;
            }
          }
        }
      }
    }
    .table-container {
      padding: 12px 16px;
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
