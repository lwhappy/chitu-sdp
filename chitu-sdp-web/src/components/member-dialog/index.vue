<!--
 * @Author: hjg
 * @Date: 2021-10-15 11:19:56
 * @LastEditTime: 2022-07-19 15:21:20
 * @LastEditors: Please set LastEditors
 * @Description: 成员管理(项目管理-点击成员-展示)
 * @FilePath: \src\components\member-dialog\index.vue
-->
<template>
  <a-modal class="member-dialog"
           v-model="isShowMemberDialog"
           :mask-closable="false"
           title="成员管理"
           :footer="null"
           :dialog-style="dialogStyle"
           width="600px">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <div class="search-table">
      <!-- 姓名/工号搜素 -->
      <div class="data-search">
        <a-auto-complete class="auto-complete-restyle"
                         v-model="searchValue"
                         ref="autoComplete"
                         :data-source="dataSource"
                         :placeholder="inputMsg"
                         @search="onSearch">
          <template slot="dataSource">
            <a-select-option v-for="(item, index) in dataSource"
                             :key="'auto-search' + index"
                             :originData="item"
                             :title="item.text">
              {{ item.text }}
            </a-select-option>
          </template>
        </a-auto-complete>
        <a-button type="primary"
                  size="small"
                  icon="plus"
                  :disabled="projectInfo.projectUserRole.isAdmin !== 1 && (!projectInfo.projectUserRole.isLeader || (projectInfo.projectUserRole.isLeader && projectInfo.projectUserRole.isLeader !== 1))"
                  @click="addMemberVisible = true">
          添加成员
        </a-button>
      </div>
      <!-- 数据展示 -->
      <chitu-table :columns="columns"
                 row-key="employeeNumber"
                 :autoHight="false"
                 :data-source="tableData"
                 :scroll="{ y: '400px' }">
        <template #isLeader="{record }">
          <a-select style="width:120px"
                    :default-value="record.isLeader"
                    :disabled="(projectInfo.projectUserRole.isAdmin !== 1 && (!projectInfo.projectUserRole.isLeader || (projectInfo.projectUserRole.isLeader && projectInfo.projectUserRole.isLeader !== 1))) || record.id === projectInfo.projectUserRole.id"
                    @change="value => memberRoleChange(value, record)">
            <a-select-option v-for="(item, index) in roleData"
                             :value="item.value"
                             :key="'roleMember' + index">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </template>
        <template #delete="{ record }">
          <div class="common-action-container">
            <a-button type="link"
                      class="btn-danger"
                      :disabled="(projectInfo.projectUserRole.isAdmin !== 1 && (!projectInfo.projectUserRole.isLeader || (projectInfo.projectUserRole.isLeader && projectInfo.projectUserRole.isLeader !== 1))) || record.id === projectInfo.projectUserRole.id"
                      @click="deleteMember(record)">
              删除
            </a-button>
          </div>

        </template>
      </chitu-table>
    </div>
    <div class="footer justify-end">
      <a-button @click="cancelEvent"
                size="small">取消</a-button>
      <a-button style="margin-left:8px"
                @click="confirmEvent"
                type="primary"
                size="small">确定</a-button>
    </div>
    <!-- 添加成员 -->
    <a-modal class="add-member-dialog"
             v-model="addMemberVisible"
             :mask-closable="false"
             title="添加项目成员"
             :footer="null"
             :dialog-style="addDialogStyle"
             width="400px">
      <div class="search">
        <div class="label">项目成员</div>
        <a-select class="auto-complete"
                  mode="multiple"
                  v-model="values"
                  placeholder="使用者姓名/工号"
                  :auto-clear-search-value="false"
                  option-label-prop="label"
                  @search="onAddSearch">
          <a-select-option v-for="(item, index) in dataSource"
                           :value="item.employeeNumber + ',' + item.name + ',' + item.privateMobile"
                           :key="'employee-' + index"
                           :label="item.name"
                           :disabled="item.disabled">
            {{ item.employeeNumber }}, {{ item.name }}
          </a-select-option>
        </a-select>
      </div>
      <div class="footer justify-end">
        <a-button @click="addCancelEvent"
                  size="small">取消</a-button>
        <a-button style="margin-left:8px"
                  @click="addConfirmEvent"
                  type="primary"
                  size="small">确定</a-button>
      </div>
    </a-modal>
    <!-- 删除成员 -->
    <confirm-dialog :visible="deleteMemberConfirmVisible"
                    type="warning"
                    @close="deleteMemberConfirmVisible = false"
                    @confirm="deleteMemeberEvent(deleteMemberItem)">
      <template>
        <p class="word-break">删除项目数据后不可恢复,确定要<span class="warn-message">&nbsp;删除&nbsp;</span>吗？</p>
      </template>
    </confirm-dialog>
  </a-modal>
</template>

<script>
  import ConfirmDialog from '@/components/confirm-dialog'
  export default {
    components: {
      ConfirmDialog
    },
    data () {
      return {
        userId: sessionStorage.getItem('userId'),
        values: [],
        searchValue: null,
        addMemberVisible: false,
        deleteMemberConfirmVisible: false,
        deleteMemberItem: null,
        dialogStyle: {
          // marginLeft: '100px'
        },
        addDialogStyle: {
          marginLeft: 'calc(50% - 150px + 100px)'
        },
        isShowMemberDialog: false,
        roleData: [{
          label: '普通成员',
          value: 0
        },
        {
          label: '项目管理员',
          value: 1
        }],
        serchDataVisible: false,
        inputMsg: '请输入姓名/工号',
        columns: [{ // 表列名
          title: '用户名',
          dataIndex: 'userName',
          scopedSlots: { customRender: 'userName' }
        },
        {
          title: '角色',
          // dataIndex: '',
          dataIndex: 'isLeader',
          scopedSlots: { customRender: 'isLeader' }
        },
        {
          title: '操作',
          dataIndex: '',
          scopedSlots: { customRender: 'delete' }
        }],
        tableData: [], // table数据
        httpData: [], // 本身使用者
        editData: [], // 添加使用者
        loading: false,
        projectInfo: {
          projectUserRole: {
            isLeader: 0,
            isAdmin: 0
          }
        },
        dataSource: []
      }
    },
    computed: {

    },
    watch: {

    },
    methods: {
      // 添加成员确定
      async addConfirmEvent () {
        if (this.values.length > 0) {
          let params = []
          this.values.forEach(item => {
            let userInfoArr = item.split(',')
            let userInfoObj = {
              projectId: this.projectInfo.id,
              userName: userInfoArr[1],
              employeeNumber: userInfoArr[0],
              isLeader: 0,
              privateMobile: userInfoArr[2]
            }
            params.push(userInfoObj)
          })
          let res = await this.$http.post('/project/projectManagement/changeRoles', params, {
            headers: {
              projectId: this.projectInfo.id
            }
          })
          if (res.code === 0) {
            this.values = []
            this.dataSource = []
            this.getUserList()
            this.$message.success({ content: '添加成功', duration: 2 })
            this.addMemberVisible = false
          } else {
            this.$message.error({ content: '添加失败', duration: 2 })
          }
        } else {
          this.values = []
          this.dataSource = []
          this.addMemberVisible = false
        }
      },
      // 关闭添加成员弹框
      addCancelEvent () {
        this.values = []
        this.dataSource = []
        this.addMemberVisible = false
      },
      deleteMember (item) {
        this.deleteMemberItem = item
        this.deleteMemberConfirmVisible = true
      },
      // 打开成员管理弹框
      open (item) {
        // console.log('---------open: ', item)
        this.projectInfo = JSON.parse(JSON.stringify(item))
        this.editData = []
        this.dataSource = []
        this.searchValue = ''
        if (this.$refs.inputSearch) { // 去掉之前搜索框的值
          this.$refs.inputSearch.defaultValue = null
        }
        this.httpData = this.projectInfo.projectUsers
        this.tableData = this.projectInfo.projectUsers
        // console.log('======open-tableData: ', this.tableData)
        this.$forceUpdate()
        this.isShowMemberDialog = true
      },
      // 检索
      onSearch (searchText) {
        if (searchText === '') {
          this.tableData = this.httpData
        } else {
          let nameArr = this.httpData.filter(item => item.userName && item.userName.search(searchText) != -1)
          let numberArr = this.httpData.filter(item => item.employeeNumber && item.employeeNumber.search(searchText) != -1)
          let arr = nameArr.concat(numberArr)
          // 拼接后去重
          let map = new Map()
          arr.filter(item => {
            if (!map.has(item.employeeNumber)) {
              map.set(item.employeeNumber, item)
            }
          })
          this.tableData = [...map.values()]
        }
      },
      // 添加成员检索
      async onAddSearch (searchText) {
        const params = { nameOrNumber: searchText }
        let res = await this.$http.post('/setting/userSetting/getHREmployee', params)
        // console.log('onSearch: ', res)
        if (res.code === 0) {
          this.dataSource = res.data
          this.dataSource.forEach(item => {
            item.disabled = false
            let arr = this.httpData.filter(tableItem => tableItem.employeeNumber === item.employeeNumber)
            if (arr.length > 0) {
              item.disabled = true
            }
          })
        }
      },
      // 确认点击事件
      async confirmEvent () {
        if (this.editData.length > 0) {
          await this.$http.post('/project/projectManagement/changeRoles', this.editData, {
            headers: {
              projectId: this.projectInfo.id
            }
          })
          // console.log('----res:', res)
        }
        this.searchValue = null
        this.isShowMemberDialog = false
        this.$emit('addSuccess')
      },
      // 取消点击事件
      cancelEvent () {
        this.searchValue = null
        this.isShowMemberDialog = false
        // this.$emit('addSuccess')
      },
      // 修改成员角色
      memberRoleChange (value, item) {
        // console.log('-----------memberRoleChange', value, item)
        let arr = this.editData.filter(member => member.employeeNumber === item.employeeNumber)
        if (arr.length > 0) {
          this.editData.forEach((member, index) => {
            if (member.id === item.id) {
              this.editData[index].isLeader = value
            }
          })
        } else {
          item.isLeader = value
          this.editData.push(item)
        }
        // console.log('-----------editData', this.editData)
      },
      // 删除项目成员
      async deleteMemeberEvent (userInfo) {
        // console.log('-----------deleteMemeberEvent', userInfo)
        const params = {
          projectId: userInfo.projectId,
          isLeader: userInfo.isLeader,
          id: userInfo.id
        }
        let res = await this.$http.post('/project/projectManagement/deleteProjectUser', params, {
          headers: {
            projectId: this.projectInfo.id
          }
        })
        // console.log('-----------deleteMemeberEvent:', res)
        if (res.code === 0) {
          this.httpData = this.httpData.filter(item => item.id !== params.id)
          this.projectInfo.projectUsers = this.httpData
          this.editData = this.editData.filter(item => item.id !== params.id)
          this.$message.success({ content: '删除成功', duration: 2 })
        } else {
          this.$message.error({ content: '删除失败', duration: 2 })
        }
        this.tableData = this.httpData
      },
      // 获取请求数据
      async getUserList () {
        // console.log('----------getUserList:', params)
        const params = {
          id: this.projectInfo.id
        }
        let res = await this.$http.post('/project/projectManagement/projectUserInfo', params, {
          headers: {
            projectId: this.projectInfo.id
          }
        })
        if (res.code === 0) {
          this.httpData = res.data
          this.tableData = res.data
        }
      },
      // 分页，排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        console.log('------handleTableChange:', pagination, filters, sorter)
      }
    },
    mounted () {

    }
  }
</script>

<style lang="scss" scoped>
  .word-break {
    word-break: break-all;
  }

  .warn-message {
    color: #faad14;
  }

  .member-dialog {
    width: 100%;
    /deep/ .ant-modal-close-x {
      width: 44px;
      height: 44px;
      line-height: 44px;
    }

    .search-table {
      min-height: 328px;
      max-height: 600px;
      margin: 12px 16px;
    }

    /deep/ .ant-modal-body {
      padding: 0;
    }

    .data-search {
      margin: 8px 0;
      display: flex;
      align-items: center;

      .auto-complete-restyle {
        width: 184px;
        margin-right: 12px;
      }

      /deep/ .ant-input {
        height: 28px;
      }
    }
    .footer {
      height: 44px;
      border-top: 1px solid #ddd;
      padding-right: 16px;
    }
  }

  .add-member-dialog {
    /deep/ .ant-modal-body {
      padding: 0;
    }

    .search {
      min-height: 100px;
      max-height: 200px;
      justify-content: space-between;
      margin: 30px 16px 70px;

      .label {
        font-size: 12px;
        margin-bottom: 8px;
      }

      .auto-complete {
        width: 100%;
        height: 28px;

        /deep/ .ant-input {
          height: 28px;
        }

        /deep/ .ant-select-selection__rendered {
          line-height: 28px;
        }
        /deep/ .ant-select-selection--multiple {
          max-height: 200px !important;
          overflow-y: auto !important;
        }
      }
    }

    .footer {
      height: 44px;
      border-top: 1px solid #ddd;
      padding-right: 16px;
    }
  }
</style>