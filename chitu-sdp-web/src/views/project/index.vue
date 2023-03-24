<template>
  <div class="project-container">
    <div class="search-container">
      <div class="search-main justify-between">
        <div class="left-content justify-between">
          <p>参与项目数<span class="sum">{{projectTotal.projectTotal}}</span></p>
          <a-divider type="vertical" />
          <p>参与作业数<span class="sum">{{projectTotal.jobTotal}}</span></p>
          <a-divider type="vertical" />
          <p class="justify-start">
            <a-tooltip placement="topLeft">
              <template slot="title">
                <span>统计你参与项目的成员个数，并根据成员去重</span>
              </template>
              <div>
                项目成员数<span class="sum">{{projectTotal.employeeTotal}}</span>
              </div>
            </a-tooltip>
          </p>
        </div>
        <div class="right-content justify-end">
          <div class="justify-start product-line">
            <p>业务线</p>
            <a-select v-model="productLine.productLineName"
                      @focus="getProductLineNameList"
                      @select="handerProductChange"
                      placeholder="请选择"
                      style="width: 184px;"
                      :allowClear="true"
                      class="productLine">
              <a-select-option v-for="(item)  in productLineNameList"
                               :key="item.code">
                {{ item.name }}
              </a-select-option>
            </a-select>
            <p>项目名称</p>
            <search-autocomplete ref="searchRef"
                                 autoMsg="请搜索"
                                 @search="search" />
            <a-button @click="query"
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
        </div>
      </div>

    </div>
    <div class="group-button">
      <a-button @click="add"
                type="primary"
                size="small"
                icon="plus">
        新建项目
      </a-button>
    </div>
    <div class="project-list">
      <div class="sub-list">
        <chitu-table v-loading="isLoading"
                   :columns="columns"
                   :dataSource="dataList"
                   rowKey="id"
                   @change="handleChange"
                   :pagination="pagination"
                   @pageChange="pageChange"
                   @pageSizeChange="pageSizeChange">
          <template #priorityTitle>
            <span>
              项目等级
              <img class="pointer"
                   width="14"
                   height="14"
                   src="@/assets/icons/ask.png"
                   alt=""
                   @click="openDrawer">
            </span>
          </template>
          <template #priority="{record}">
            <span :class="[record.priority == 1?'priority1':record.priority == 2?'priority2':'']">
              {{ priorityPrefix + record.priority}}
            </span>
          </template>
        </chitu-table>
      </div>
    </div>
    <member-dialog ref="memberDialog"
                   @addSuccess="addSuccess"
                   @confirm="memberConfirm"
                   @cancel="memberCancel" />

    <add-project ref="addProject"
                 @addSuccess="addSuccess"
                 @confirm="projectConfirm"
                 @cancel="projectCancel" />
    <confirm-dialog :visible="deleteVisible"
                    type="warning"
                    @close="deleteVisible=false"
                    @confirm="confirmDelete(deleteItem)">
      <template>
        <p class="word-break">删除项目数据后不可恢复，确定要<span class="warn-message">&nbsp;删除&nbsp;</span>吗？</p>
      </template>
    </confirm-dialog>
    <drawer ref="drawer"
            :drawerVisible="drawerVisible"
            @closeDrawer="drawerVisible = false" />
  </div>
</template>
<script>
  import memberDialog from '@/components/member-dialog/index'
  import addProject from './components/add-project'
  import SearchAutocomplete from '@/components/search-autocomplete/index'
  import ConfirmDialog from '@/components/confirm-dialog'
  import drawer from '@/components/priority-drawer.vue'
  const columns = (vm) => {
    return [
      {
        dataIndex: 'projectName',
        title: '项目名称',
        width: 150,
        customRender: (text, record) => {
          return {
            children: vm.$createElement('a-button', {
              attrs: { type: 'link' },
              on: {
                click: () => {
                  vm.gotoJob(record)
                }
              }
            }, text)
          }
        }
      },
      {
        dataIndex: 'projectCode',
        title: '项目编码',
        width: 150,
        customRender: (text, record) => {
          return {
            children: vm.$createElement('span', {
              on: {
                click: () => {
                  vm.gotoJob(record)
                }
              }
            }, text)
          }
        }
      },
      {
        dataIndex: 'productLineName',
        title: '业务线',
        width: 150
      },
      // {
      //   dataIndex: 'priority',
      //   title: '项目等级',
      //   customRender: (text, record) => {
      //     return vm.priorityPrefix + record.priority
      //   },
      //   width: 100
      // },
      {
        dataIndex: 'priority',
        scopedSlots: { title: 'priorityTitle', customRender: 'priority' },
        // customRender: (text, record) => {
        //   return vm.priorityPrefix + record.priority
        // },
        // scopedSlots: {
        //   title: 'priorityTitle'
        // },
        width: 100
      },
      {
        dataIndex: 'onlineJobNum',
        title: '发布作业数',
        width: 100,
        sortDirections: ['ascend', 'descend', 'ascend'],
        sorter: (a, b) => a.onlineJobNum - b.onlineJobNum,
        customRender: (text, record) => {
          if (text == 0) {
            return {
              children: vm.$createElement('span', {
                style: {
                  marginLeft: '6px'
                },
              }, text)
            }
          }
          return {
            children: vm.$createElement('a-button', {
              attrs: { type: 'link' },
              on: {
                click: () => {
                  vm.gotoOperate(record)
                }
              }
            }, text)
          }
        }
      },
      {
        title: '创建时间',
        dataIndex: 'creationDate',
        scopedSlots: { customRender: 'creationDate' },
        width: 150,
        defaultSortOrder: 'descend',
        sortDirections: ['ascend', 'descend', 'ascend'],
        sorter: (a, b) => a.creationDate - b.creationDate,
      },
      {
        title: '创建人',
        key: 'createdBy',
        dataIndex: 'createdBy',
        scopedSlots: { customRender: 'createdBy' },
        width: 130
      },
      {
        title: '项目负责人',
        key: 'projectOwner',
        dataIndex: 'projectOwner',
        width: 150,
        customRender: (text, record) => {
          return {
            children: record.projectOwner.map(item => {
              return vm.$createElement('span', {}, item.userName)
            })

          }
        }
      },
      {
        title: '项目成员',
        key: 'projectUsers',
        dataIndex: 'projectUsers',
        width: 150,
        customRender: (text, record) => {
          return {
            children: vm.$createElement('span', {
              style: {
                color: '#0066FF',
                cursor: 'pointer'
              },
              on: {
                click: () => {
                  vm.popMemberDialog(record)
                }
              }
            }, [
              vm.$createElement('i', {
                class: "chitutree-h5 chitutreechengyuan",
              }),
              vm.$createElement('span', {
                style: {
                  marginLeft: '4px'
                },
              }, `成员：${text.length}`)
            ])
          }
        }
      },
      {
        title: '操作',
        key: 'operate',
        dataIndex: 'operate',
        fixed: 'right',
        width: 250,
        customRender: (text, record) => {
          return {
            children: vm.$createElement('div', {
              class: "common-action-container ",
            }, [
              vm.$createElement('a-button', {
                attrs: { type: 'link', disabled: record.projectUserRole && (record.projectUserRole.isAdmin === 1 || record.projectUserRole.isLeader === 1 || record.projectUserRole.isLeader === 0) ? false : true },
                on: {
                  click: () => {
                    vm.gotoJob(record)
                  }
                }
              }, '进入项目'),
              vm.$createElement('a-button', {
                attrs: { type: 'link', disabled: record.projectUserRole && (record.projectUserRole.isAdmin === 1 || record.projectUserRole.isLeader === 1) ? false : true },
                on: {
                  click: () => {
                    vm.edit(record)
                  }
                }
              }, [
                vm.$createElement('i', {
                  class: "chitutree-h5 chitutreebianji",
                }),
                vm.$createElement('span', {}, '编辑'),
              ]),
              vm.$createElement('a-divider', {
                attrs: {
                  type: 'vertical'
                }
              }),
              vm.$createElement('a-button', {
                class: "btn-danger",
                attrs: { type: 'link', disabled: record.projectUserRole && (record.projectUserRole.isAdmin === 1 || record.projectUserRole.isLeader === 1) ? false : true },
                on: {
                  click: () => {
                    vm.handleDelete(record)
                  }
                }
              }, [
                vm.$createElement('i', {
                  class: "chitutree-h5 chitutreeshanchu",
                }),
                vm.$createElement('span', {}, '删除'),
              ])
            ])
          }
        }
      },
    ]
  };
  export default {
    data () {
      return {
        deleteVisible: false,
        drawerVisible: false,
        deleteItem: null,
        projectName: '',
        isLoading: false,
        productLineNameList: [],
        dataList: [],
        columns: columns(this),
        isShowMemberDialog: false,

        isNew: true,
        projectDialog: {
          title: '新建项目'
        },
        page: 1,
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0
        },
        projectTotal: {},
        order: {
          field: 'creation_date',
          value: 1
        },
        productLine: {},
        priorityPrefix: ''
      };
    },
    components: {
      memberDialog, addProject, SearchAutocomplete, ConfirmDialog, drawer
    },
    computed: {
      isAdmin () {
        return this.$store.getters.userInfo.isAdmin == 1
      }
    },
    created () {
      this.init()
    },
    watch: {
      '$store.getters.isRemoveTag': {//监听关闭页签，关闭页签后清除缓存
        handler (val, oldVal) {
          if (val === oldVal) {
            return
          }
          if (this.$store.getters.removeRouteName.includes('ProjectList')) {
            this.$common.toClearCache(this);
          }
        }
      }
    },
    mounted () {

    },
    methods: {
      init () {
        if (this.$route.query.from === 'login') {//登录后跳转到最近一次访问的项目
          this.getProjectHistory()
        }
        this.getList()
        const globalPriority = this.$common.getPriority()
        this.priorityPrefix = globalPriority.labelPrefix
      },
      reset () {
        this.projectName = ''
        this.$refs.searchRef.keyword = ''
        this.productLine.productLineName = ''
        this.pagination.current = 1
        this.getList()
      },
      search (serachData) {
        this.projectName = serachData
      },
      query () {
        this.pagination.current = 1
        this.getList()
      },
      handerProductChange (value) {
        let selectItem = this.productLineNameList.find((item) => item.code === value)
        this.productLine.productLineCode = selectItem.code
        this.productLine.productLineName = selectItem.name
      },
      // 添加业务线
      async getProductLineNameList () {
        let res = await this.$http.get('/project/projectManagement/queryBusinessLine')
        if (res.code === 0) {
          this.productLineNameList = res.data
        } else {
          this.$message.error(res.msg)
        }
      },
      async getProjectHistory () {
        let res = await this.$http.post('/project/projectManagement/getProjectHistory')
        if (res.code === 0 && res.data && res.data.projectId && res.data.projectName) {
          this.$router.push({
            name: 'JobDevelop',
            query: {//预留query
              projectId: res.data.projectId,
              projectName: encodeURIComponent(res.data.projectName),
              projectCode: res.data.projectCode
            }
          })
        }
      },
      // async getProjectTotal () {
      //   let res = await this.$http.post('/project/projectManagement/projectCount')
      //   if (res.code === 0) {
      //     this.projectTotal = res.data
      //   } else {
      //     this.$message.error(res.msg);
      //   }
      // },
      async getList () {

        const params = {
          orderByClauses: [{
            field: "is_leader",
            orderByMode: 1
          }, {
            field: this.order.field, //排序键名
            orderByMode: this.order.value //排序模式（1：正序，0：倒序）
          }],
          page: this.pagination.current,
          pageSize: this.pagination.defaultPageSize,
          projectName: this.projectName, //项目名称
          productLineName: this.productLine.productLineName || "" //项目名称
          // vo: {
          //   projectName: projectName || "", //项目名称
          //   productLineName: this.productLine.productLineName || "" //项目名称
          // }

        }
        this.dataList = []
        this.pagination.total = 0
        this.projectTotal.projectTotal = 0
        this.projectTotal.jobTotal = 0
        this.projectTotal.employeeTotal = 0
        // const url = '/project/projectManagement/projectInfo'
        const url = '/project/projectManagement/projectList'
        this.isLoading = true
        let res = await this.$http.post(url, params)
        this.isLoading = false
        if (res.code === 0) {
          if (res.data) {
            this.pagination.total = res.data.rowTotal
            this.projectTotal.projectTotal = res.data.projectTotal
            this.projectTotal.jobTotal = res.data.jobTotal
            this.projectTotal.employeeTotal = res.data.employeeTotal
            if (res.data.rows) {
              this.dataList = res.data.rows
              this.dataList = this.dataList.map(item => {
                item.projectEngines = JSON.parse(item.projectEngines)
                item.projectOwner = JSON.parse(item.projectOwner)
                item.projectUsers = JSON.parse(item.projectUsers)
                return item
              })
            } else {
              this.dataList = []
            }
          }
        } else {
          this.$message.error(res.msg);
        }
      },
      popMemberDialog (item) {
        this.$refs.memberDialog.open(item)
      },
      gotoJob (item) {
        if (item.projectUserRole.isLeader !== 1 && item.projectUserRole.isLeader !== 0 && item.projectUserRole.isAdmin !== 1) {//不是系统管理员也不是项目成员不让他跳
          this.$message.error('当前暂无权限，请联系管理员' + item.projectOwner[0].userName)
          return
        }
        this.$router.push({
          name: 'JobDevelop',
          query: {//预留query
            projectId: item.id,
            projectName: encodeURIComponent(item.projectName),
            projectCode: item.projectCode
          }
        })
      },
      //跳转作业运维
      gotoOperate (item) {
        if (item.projectUserRole.isLeader !== 1 && item.projectUserRole.isLeader !== 0 && item.projectUserRole.isAdmin !== 1) {//不是系统管理员也不是项目成员不让他跳
          this.$message.error('当前暂无权限，请联系管理员' + item.projectOwner[0].userName)
          return
        }
        this.$router.push({
          name: 'JobOperate',
          query: {//预留query
            projectId: item.id,
            projectName: encodeURIComponent(item.projectName),
            projectCode: item.projectCode
          }
        })
      },
      add () {
        if (!this.isAdmin) {
          this.$message.warning('您不是管理员，没有权限新建项目，若需新建项目，请联系管理员苏涛10、邹倡振~');
          return
        }
        const obj = {
          isNew: true,
          data: null,
          title: '新建项目'
        }
        this.$refs.addProject.open(obj)
      },
      edit (item) {

        const obj = {
          isNew: false,
          data: item,
          title: '编辑项目'
        }
        this.$refs.addProject.open(obj)
      },
      handleDelete (item) {
        this.deleteItem = item
        this.deleteVisible = true
      },
      async isDeleteCurrentProject () {
        let res = await this.$http.post('/project/projectManagement/getProjectHistory')
        if (res.code === 0 && res.data && res.data.projectId && res.data.projectName) {
          const currentProject = {
            id: res.data.projectId,
            name: encodeURIComponent(res.data.projectName),
            code: res.data.projectCode
          }
          this.$store.dispatch('global/saveCurrentProject', currentProject)

        }
      },
      async confirmDelete (item) {
        const params = {
          id: item.id //项目id
        }
        let res = await this.$http.post('/project/projectManagement/delete', params, {
          headers: {
            projectId: item.id
          }
        })
        if (res.code === 0) {
          this.$message.success('删除成功');
          this.getList()
          if (item.id.toString() === this.$store.getters.currentProject.id.toString()) {//删除的是当前访问的项目，重新获取最近的项目
            const currentProject = {
              id: '',
              name: '',
              code: ''
            }
            this.$store.dispatch('global/saveCurrentProject', currentProject)
            this.isDeleteCurrentProject()

            // const projectTag = this.$store.state.tagsView.visitedViews.find(item => item.firstRouteName === 'ProjectList')
            // console.log('projectTag', projectTag)
            // this.$store.dispatch('tagsView/delOthersViews', projectTag).then(() => {
            // })//删除非项目管理的所有页签

          }
        } else {
          this.$message.error(res.msg);
        }
      },
      projectConfirm (data) {
        console.log('confirm', data)
      },
      projectCancel (data) {
        console.log('cancel', data)
      },
      memberConfirm (data) {
        console.log('confirm', data)
      },
      memberCancel (data) {
        console.log('cancel', data)
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.page = pageInfo.page
        this.pagination.current = pageInfo.page
        this.getList()
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        this.pagination.defaultPageSize = pageSizeInfo.size
        this.page = 1
        this.pagination.current = 1
        this.getList()
      },
      addSuccess () {
        this.page = 1
        this.pagination.current = 1
        this.getList()
      },
      handleChange (pagination, filters, sorter) {
        // console.log(sorter)
        this.order.field = this.$common.toLine(sorter.field)
        if (sorter.order === 'ascend') {
          this.order.value = 0
          this.getList()
        } else if (sorter.order === 'descend') {
          this.order.value = 1
          this.getList()
        }
      },
      openDrawer () {
        this.drawerVisible = true
      },
    }
  };
</script>

<style lang="scss" scoped>
  .project-container {
    height: 100%;
    .search-container {
      padding-bottom: 8px;
      background: #eff1f6;
      .search-main {
        height: 56px;
        padding: 0 16px;
        border-bottom: 1px solid #dee2ea;
        box-sizing: border-box;
        background: #ffffff;
        .left-content {
          font-size: 12px;
          .sum {
            margin-left: 6px;
            font-size: 16px;
            font-weight: 900;
            color: #0066ff;
          }
        }
        .right-content {
          .product-line {
            margin-right: 16px;
            p {
              margin: 0 8px 0 20px;
            }
          }
          .productLine {
            /deep/ .ant-select-selection--single {
              height: 28px;
              .ant-select-selection__rendered {
                line-height: 28px;
              }
            }
          }
        }
      }
    }

    .guide-component {
      width: 100%;
      height: 100%;
    }
    .group-button {
      padding: 12px 16px;
    }
    .project-list {
      // height: calc(100% - 131px);
      padding: 0 16px;
      .sub-list {
        height: 100%;
        .priority1 {
          color: #ff5555;
        }
        .priority2 {
          color: #ff9118;
        }
        // /deep/ tbody tr td {
        //   text-overflow: ellipsis;
        //   overflow: hidden;
        //   white-space: nowrap;
        //   padding: 3.5px 16px;
        // }
      }
    }
  }
</style>