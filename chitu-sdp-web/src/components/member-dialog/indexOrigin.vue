<!--
 * @Author: hjg
 * @Date: 2021-10-15 11:19:56
 * @LastEditTime: 2021-12-20 16:50:38
 * @LastEditors: Please set LastEditors
 * @Description: 成员管理(项目管理-点击成员-展示)
 * @FilePath: \bigdata-sdp-frontend2\src\components\member-dialog\index.vue
-->
<template>
  <a-modal class="member-dialog"
           v-model="isShowMemberDialog"
           :mask-closable="false"
           title="成员管理"
           :footer="null"
           width="500px">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <div class="search-table">
      <!-- 姓名/工号搜素 -->
      <div class="data-search">
        <input-search class="member-input-search"
                      ref="inputSearch"
                      :labelInfos="labelInfos"
                      :inputMsg="inputMsg"
                      :serchDataVisible="serchDataVisible"
                      :dataSource="dataSource"
                      @initDataSource="initDataSource"
                      @onSearch="onSearch"
                      @onSelect="onSelect" />
      </div>
      <!-- 数据展示 -->
      <a-table :columns="columns"
               row-key="employeeNumber"
               :data-source="tableData"
               :pagination="false"
               :loading="loading"
               :scroll="{y: 'calc(100% - 41px)'}"
               @change="handleTableChange">
        <div slot="userName"
             slot-scope="text, record">
          <span class="user-name"><i class="chitutree-h5 chitutreexitongguanliyuan"></i></span>
          {{ record.userName }}
        </div>
        <div slot="isLeader"
             slot-scope="text, record">
          <a-select :default-value="record.isLeader"
                    class="select-restyle"
                    @change="value => memberRoleChange(value, record)">
            <a-select-option v-for="(item, index) in roleData"
                             :value="item.value"
                             :key="'roleMember' + index">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </div>
        <div slot="delete"
             class="item-delete"
             slot-scope="text, record">
          <a-popconfirm v-if="tableData.length > 0"
                        title="确定删除吗?"
                        @confirm="() => deleteMemeberEvent(record)">
            <!-- <img src="" alt=""> -->
            删除
          </a-popconfirm>
        </div>
      </a-table>
    </div>
    <div class="footer justify-end">
      <a-button class="button-restyle button-confirm"
                @click="confirmEvent">确认</a-button>
      <a-button class="button-restyle button-cancel"
                @click="cancelEvent">取消</a-button>
    </div>
  </a-modal>
</template>

<script>
  import inputSearch from '@/components/input-search/index'
  export default {
    components: {
      inputSearch
    },
    data () {
      return {
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
        labelInfos: [{
          name: '添加成员',
          value: 'addMember'
        },
        {
          name: '搜索成员',
          value: 'searchMember'
        }],
        columns: [{ // 表列名
          title: '用户名',
          width: '40%',
          dataIndex: 'userName',
          scopedSlots: { customRender: 'userName' }
        },
        {
          title: '角色',
          width: '40%',
          // dataIndex: '',
          dataIndex: 'isLeader',
          scopedSlots: { customRender: 'isLeader' }
        },
        {
          title: '删除',
          dataIndex: '',
          scopedSlots: { customRender: 'delete' }
        }],
        tableData: [], // table数据
        httpData: [], // 本身使用者
        editData: [], // 添加使用者
        loading: false,
        projectInfo: {},
        dataSource: []
      }
    },
    computed: {

    },
    watch: {

    },
    methods: {
      open (item) {
        // console.log('---------open: ', item)
        this.projectInfo = JSON.parse(JSON.stringify(item))
        this.editData = []
        this.dataSource = []
        if (this.$refs.inputSearch) { // 去掉之前搜索框的值
          this.$refs.inputSearch.defaultValue = null
        }
        this.httpData = this.projectInfo.projectUsers
        this.tableData = this.projectInfo.projectUsers
        // console.log('======open-tableData: ', this.tableData)
        this.$forceUpdate()
        this.isShowMemberDialog = true
      },
      initDataSource (data) {
        // console.log('------------data:', data)
        if (data.flag) {
          this.dataSource = []
        }
        if (data.type === 'addMember') {
          this.tableData = this.editData.concat(this.httpData)
        }
      },
      // 检索
      onSearch (searchValue) {
        // console.log('-------onSearch: ', searchValue)
        if (searchValue.type === 'addMember') { // 添加用户模糊
          this.searchOfAddData(searchValue.value)
        } else if (searchValue.type === 'searchMember') { // 搜索用户模糊
          this.searchOfData(searchValue.value)
        }
      },
      // 添加用户模糊
      async searchOfAddData (searchText) {
        const params = { nameOrNumber: searchText }
        let res = await this.$http.post('/setting/userSetting/getHREmployee', params)
        // console.log('onSearch: ', res)
        if (res.code === 0) {
          this.dataSource = res.data
          this.dataSource.map(item => {
            item.text = item.employeeNumber + ', ' + item.name
          })
        }
      },
      // 搜索用户模糊
      async searchOfData (searchText) {
        const params = {
          id: this.projectInfo.id,
          nameOrNumber: searchText
        }
        let res = await this.$http.post('/project/projectManagement/getProjectUser', params)
        if (res.code === 0) {
          this.dataSource = res.data
          this.dataSource.map(item => {
            item.text = item.employeeNumber + ', ' + item.userName
          })
        }
      },
      // 选中项
      onSelect (selectedValue) {
        // console.log('-------onSelect: ', selectedValue)
        if (selectedValue.type === 'addMember') { // 添加用户选中
          this.selectedOfAdd(selectedValue.value)
        } else if (selectedValue.type === 'searchMember') {
          this.selectedOfSearch(selectedValue.value)
        }
      },
      // 添加用户选中
      selectedOfAdd (userInfo) {
        let userInfoArr = this.editData.filter(item => item.employeeNumber === userInfo.employeeNumber)
        let engineUsersArr = this.projectInfo.projectUsers.filter(item => item.employeeNumber === userInfo.employeeNumber)
        if (userInfoArr.length === 0 && engineUsersArr.length === 0) {
          let userInfoObj = {
            projectId: this.projectInfo.id,
            userName: userInfo.name,
            isLeader: 0,
            employeeNumber: userInfo.employeeNumber
          }
          this.editData.unshift(userInfoObj)
          this.tableData = this.editData.concat(...this.projectInfo.projectUsers)
          // console.log('------tableData: ', this.tableData)
        } else {
          this.$message.warning({ content: userInfo.text + ' 用户已存在', duration: 2 })
        }
      },
      // 搜索用户选中
      selectedOfSearch (userInfo) {
        this.tableData = [userInfo]
      },
      // 确认点击事件
      async confirmEvent () {
        if (this.editData.length > 0) {
          let res = await this.$http.post('/project/projectManagement/changeRoles', this.editData, {
            headers: {
              projectId: this.projectInfo.id
            }
          })
          // console.log('----res:', res)
        }
        this.isShowMemberDialog = false
        this.$emit('addSuccess')
      },
      // 取消点击事件
      cancelEvent () {
        this.isShowMemberDialog = false
        this.$emit('addSuccess')
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
        if (userInfo.id) {
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
            this.$message.success({ content: ' 删除成功', duration: 2 })
          } else {
            this.$message.error({ content: ' 删除失败', duration: 2 })
          }
        } else {
          this.editData = this.editData.filter(item => item.employeeNumber !== userInfo.employeeNumber)
        }
        this.tableData = this.editData.concat(this.httpData)
      },
      // 获取请求数据
      getUserList (params = {}) {
        // console.log('----------getUserList:', params)
      },
      // 分页，排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        // console.log('------handleTableChange:', pagination, filters, sorter)
      }
    },
    mounted () {

    }
  }
</script>

<style lang="scss" scoped>
  .member-dialog {
    width: 100%;
    /deep/ .ant-modal-content {
      border-radius: 0;
    }
    /deep/ .ant-select {
      font-size: 12px;
    }
    /deep/ .ant-modal-header {
      padding: 11px 16px;
      border-radius: 0;
      .ant-modal-title {
        font-weight: 700;
      }
    }
    /deep/ .ant-modal-close-x {
      width: 44px;
      height: 44px;
      line-height: 44px;
    }
    /deep/ .ant-modal-body {
      padding: 0;
    }
    /deep/ .ant-select-selection {
      border: 0 !important;
      box-shadow: 0 0px 0px #fff !important;
    }
    /deep/ .ant-select-selection:active {
      border: 0 !important;
      box-shadow: 0 0px 0px #fff !important;
    }
    /deep/ .ant-select-selection:focus {
      border: 0 !important;
      box-shadow: 0 0px 0px #fff !important;
    }
    .search-table {
      margin: 12px 16px;
      height: 328px;
      /deep/ .ant-table-thead {
        height: 32px;
        th {
          padding: 0 0 0 2px;
          font-size: 12px;
          color: #000;
          line-height: 40px;
        }
      }
      /deep/ .ant-table-tbody > tr > td {
        padding: 0 0 0 2px;
        font-size: 12px;
        color: #000;
        line-height: 40px;
        .user-name {
          color: #ccc;
          margin-right: 8px;
        }
      }
    }
    /deep/ .ant-table-wrapper {
      height: calc(100% - 28px);
      .ant-spin-nested-loading {
        height: 100%;
        .ant-spin-container {
          height: 100%;
          .ant-table {
            height: 100%;
            .ant-table-content {
              height: 100%;
              .ant-table-scroll {
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
    .data-search {
      margin-bottom: 8px;
      .member-input-search /deep/ .label-info {
        width: 90px;
        .select-restyle {
          width: 90px;
        }
      }
      /deep/ .input-info {
        width: 400px;
        margin-left: 0;
      }
    }
    .item-delete {
      color: #714dab;
      cursor: pointer;
    }
    .input-restyle input {
      height: 28px;
      border-radius: 0;
    }
    .select-restyle {
      width: 100px;
      height: 100%;
      line-height: 28px;
      /deep/ .ant-select-selection--single {
        height: 28px;
      }
      /deep/ .ant-select-selection__rendered {
        height: 28px;
        line-height: 28px;
      }
      /deep/.ant-select-selection {
        height: 28px;
        border-radius: 0;
        border: 0;
        // border: 1px solid #000;
        color: #000;
        margin-left: -5px;
      }
      /deep/ .ant-select-selection:active {
        border: 0;
      }
      /deep/ svg {
        color: #000;
      }
    }
    .footer {
      border-top: 1px solid #ddd;
      height: 44px;
      line-height: 44px;
      padding-right: 16px;
    }
  }
</style>