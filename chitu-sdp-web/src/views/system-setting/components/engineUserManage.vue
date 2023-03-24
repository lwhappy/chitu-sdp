<!--
 * @Author: hjg
 * @Date: 2021-10-19 15:37:25
 * @LastEditTime: 2022-07-13 16:28:24
 * @LastEditors: Please set LastEditors
 * @Description: 引擎使用者
 * @FilePath: \src\views\system-setting\components\engineUserManage.vue
-->
<template>
  <a-modal class="engine-user-manage"
           v-model="isShowDialog"
           title="引擎使用者"
           :footer="null"
           width="600px">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <div class="search-table">
      <!-- 姓名/工号搜素 -->
      <div class="data-search">
        <a-auto-complete class="auto-complete-restyle"
                         ref="autoComplete"
                         v-model="searchValue"
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
        <a-button @click="addEngineUserVisible = true"
                  type="primary"
                  size="small"
                  icon="plus">
          添加使用者
        </a-button>
      </div>
      <!-- 数据展示 -->
      <chitu-table row-key="id"
                 :columns="columns"
                 v-loading="isLoading"
                 :autoHight="false"
                 :dataSource="tableData"
                 :scroll="{y:'400px'}"
                 @change="handleTableChange">
        <template #delete="{record}">
          <span class="item-delete"
                @click="handleDeleteMemeber(record)">
            <i class="chitutree-h5 chitutreeshanchu"></i>删除
          </span>
        </template>
      </chitu-table>
    </div>
    <div class="footer justify-end">
      <a-button @click="cancelEvent"
                size="small">取消</a-button>
      <a-button style="margin-left:8px"
                @click="confirmEvent"
                size="small"
                type="primary">确定</a-button>
    </div>
    <!-- 添加使用者 -->
    <a-modal class="add-member-dialog"
             v-model="addEngineUserVisible"
             :mask-closable="false"
             title="添加使用者"
             :footer="null"
             :dialog-style="addDialogStyle"
             width="400px">
      <div class="search">
        <div class="label">引擎使用者</div>
        <a-select class="auto-complete"
                  mode="multiple"
                  v-model="values"
                  placeholder="请输入姓名/工号"
                  :auto-clear-search-value="false"
                  option-label-prop="label"
                  @search="onAddSearch">
          <a-select-option v-for="(item, index) in dataSource"
                           :value="item.employeeNumber + ',' + item.name"
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
                  size="small"
                  type="primary">确定</a-button>
      </div>
    </a-modal>
    <confirm-dialog :visible="deleteVisible"
                    type="warning"
                    @close="deleteVisible=false"
                    @confirm="deleteMemeberEvent(deleteItem)">
      <template>
        <p class="word-break">确定要<span class="warn-message">&nbsp;删除&nbsp;</span>吗？</p>
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
        deleteItem: null,
        deleteVisible: false,
        isLoading: false,
        searchValue: null,
        addEngineUserVisible: false,
        values: [],
        dialogStyle: {
          // marginLeft: '100px'
        },
        addDialogStyle: {
          marginLeft: 'calc(50% - 150px + 100px)'
        },
        isShowDialog: false,
        serchDataVisible: false,
        inputMsg: '请输入姓名/工号',
        labelInfos: [{
          name: '添加使用者',
          value: 'addEngineUser'
        },
        {
          name: '搜索使用者',
          value: 'searchEngineUser'
        }],
        searchData: null,
        // loading: true,
        columns: [{ // 表列名
          title: '用户名',
          dataIndex: 'userName',
          scopedSlots: { userName: 'userName' }
        },
        {
          title: '操作',
          dataIndex: '',
          scopedSlots: { customRender: 'delete' }
        }],
        tableData: [], // table数据
        httpData: [], // 本身使用者
        editData: [], // 添加使用者
        engineInfo: {},
        dataSource: []
      }
    },
    computed: {

    },
    watch: {

    },
    methods: {
      // 添加使用者弹框确定按钮
      async addConfirmEvent () {
        if (this.values.length > 0) {
          let params = []
          this.values.forEach(item => {
            let userInfoArr = item.split(',')
            let userInfoObj = {
              engineId: this.engineInfo.id,
              userName: userInfoArr[1],
              employeeNumber: userInfoArr[0]
            }
            params.push(userInfoObj)
          })
          let res = await this.$http.post('/setting/engineSetting/addUser', params)
          if (res.code === 0) {
            this.values = []
            this.dataSource = []
            this.getUserList()
            this.$message.success({ content: ' 添加成功', duration: 2 })
            this.addEngineUserVisible = false
          } else {
            this.$message.error({ content: ' 添加失败', duration: 2 })
          }
        } else {
          this.values = []
          this.dataSource = []
          this.addEngineUserVisible = false
        }
      },
      // 关闭添加引擎使用者弹框
      addCancelEvent () {
        this.addEngineUserVisible = false
      },
      // 打开引擎使用者弹框
      open (engineInfo) {
        // console.log('-----------open', engineInfo)
        this.editData = []
        this.dataSource = []
        if (this.$refs.inputSearch) { // 去掉之前搜索框的值
          this.$refs.inputSearch.defaultValue = null
        }
        this.engineInfo = JSON.parse(JSON.stringify(engineInfo))
        this.tableData = this.engineInfo.engineUsers
        this.httpData = this.engineInfo.engineUsers
        this.isShowDialog = true
      },
      // 检索
      onSearch (searchText) {
        if (searchText === '') {
          this.tableData = this.httpData
        } else {
          let nameArr = this.httpData.filter(item => item.userName.search(searchText) != -1)
          let numberArr = this.httpData.filter(item => item.employeeNumber.search(searchText) != -1)
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
        this.isShowDialog = false
        this.searchValue = null
        this.$emit('confirm', true)
      },
      // 取消点击事件
      cancelEvent () {
        this.isShowDialog = false
        this.searchValue = null
        this.$emit('cancel', true)
      },
      handleDeleteMemeber (item) {
        this.deleteItem = item
        this.deleteVisible = true
      },
      // 删除项目成员
      async deleteMemeberEvent (userInfo) {
        // console.log('-----------deleteMemeberEvent', userInfo)
        const params = {
          id: userInfo.id,
          engineId: this.engineInfo.id
        }
        let res = await this.$http.post('/setting/engineSetting/deleteUser', params)
        // console.log('-----------------deleteMemeberEvent-res: ', res)
        if (res.code === 0) {
          this.httpData = this.httpData.filter(item => item.id !== params.id)
          this.engineInfo.engineUsers = this.httpData
          this.$message.success({ content: ' 删除成功', duration: 2 })
        } else {
          this.$message.error({ content: ' 删除失败', duration: 2 })
        }
        this.tableData = this.httpData
      },
      // 获取请求数据
      async getUserList () {
        // console.log('----------getUserList:', params)
        const params = {
          engineId: this.engineInfo.id
        }
        this.isLoading = true
        let res = await this.$http.post('/setting/engineSetting/getEngineUser', params)
        this.isLoading = false
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
  .engine-user-manage {
    width: 100%;
    // /deep/ .ant-modal-content {
    //   border-radius: 0;
    // }
    /deep/ .ant-select {
      font-size: 12px;
    }
    // /deep/ .ant-modal-header {
    //   padding: 11px 16px;
    //   border-radius: 0;
    //   .ant-modal-title {
    //     font-weight: 700;
    //   }
    // }
    // /deep/ .ant-modal-close-x {
    //   width: 44px;
    //   height: 44px;
    //   line-height: 44px;
    // }
    /deep/ .ant-modal-body {
      padding: 0;
    }
    // /deep/ .ant-select-selection {
    //   border: 0 !important;
    //   box-shadow: 0 0px 0px #fff !important;
    // }
    // /deep/ .ant-select-selection:active {
    //   border: 0 !important;
    //   box-shadow: 0 0px 0px #fff !important;
    // }
    // /deep/ .ant-select-selection:focus {
    //   border: 0 !important;
    //   box-shadow: 0 0px 0px #fff !important;
    // }
    .search-table {
      margin: 12px 16px;
      min-height: 328px;
      max-height: 600px;
      .open-add {
        display: flex;
        justify-content: flex-end;
        button {
          width: auto;
        }
      }
      // /deep/ .ant-table-thead {
      //   height: 32px;
      //   th {
      //     padding: 0 0 0 2px;
      //     font-size: 12px;
      //     color: #000;
      //     line-height: 40px;
      //     height: 40px;
      //     overflow: hidden;
      //   }
      // }
      // /deep/ .ant-table-tbody > tr > td {
      //   padding: 0 0 0 2px;
      //   font-size: 12px;
      //   color: #000;
      //   line-height: 40px;
      //   height: 40px;
      //   overflow: hidden;
      // }
      // /deep/ .ant-table-wrapper {
      //   height: calc(100% - 36px - 28px);
      //   .ant-spin-nested-loading {
      //     height: 100%;
      //     .ant-spin-container {
      //       height: 100%;
      //       .ant-table {
      //         height: 100%;
      //         .ant-table-content {
      //           height: 100%;
      //           .ant-table-scroll {
      //             height: 100%;
      //             .ant-table-body {
      //               // height: 100%;
      //             }
      //           }
      //         }
      //       }
      //     }
      //   }
      // }
    }
    .data-search {
      margin-top: 8px;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      .auto-complete-restyle {
        width: 240px;
        margin-right: 12px;
      }
      /deep/ .ant-input {
        height: 28px;
      }
      .extrat {
        margin-left: 0;
        width: auto;
        padding: 0 4px;
      }
      /deep/ .input-info {
        width: 400px;
        margin-left: 0;
      }
      .engine-input-search /deep/ .label-info {
        width: 100px;
        .select-restyle {
          width: 100px;
        }
      }
      /deep/ .ant-select-selection__rendered {
        margin-left: 0;
        //   margin-right: 30px;
      }
    }
    .item-delete {
      color: #f95353;
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
      width: 100px;
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
  .add-member-dialog {
    // /deep/ .ant-modal-header {
    //   padding: 11px 16px;
    //   border-radius: 0;
    //   .ant-modal-title {
    //     font-weight: 700;
    //   }
    // }
    // /deep/ .ant-modal-close-x {
    //   width: 44px;
    //   height: 44px;
    //   line-height: 44px;
    // }
    /deep/ .ant-modal-body {
      padding: 0;
    }
    // /deep/ .ant-modal-content {
    //   border-radius: 0;
    // }
    .search {
      height: 28px;
      justify-content: space-between;
      margin: 26px 16px 85px;
      .label {
        width: 60px;
        font-size: 12px;
        color: #333;
        line-height: 28px;
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
          max-height: 80px;
          // overflow-y: auto;
          // &::-webkit-scrollbar {
          //   //整体样式
          //   height: 12px;
          //   width: 6px;
          // }
          // &::-webkit-scrollbar-thumb {
          //   //滑动滑块条样式
          //   border-radius: 6px;
          //   background: #ab90e8;
          //   height: 20px;
          // }
          // &::-webkit-scrollbar-track {
          //   //轨道的样式
          //   background-color: #fff;
          // }
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
