<!--
 * @Author: hjg
 * @Date: 2021-10-18 20:16:56
 * @LastEditTime: 2022-06-24 09:27:39
 * @LastEditors: Please set LastEditors
 * @Description: 添加系统管理员
 * @FilePath: \src\views\system-setting\components\addSysDialog.vue
-->
<template>
  <a-modal class="add-sys-dialog"
           v-model="isShowDialog"
           title="新增系统管理员"
           width="400px"
           :footer="null">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <div class="search">
      <div class="label">系统管理员</div>
      <a-select class="auto-complete"
                mode="multiple"
                v-model="values"
                placeholder="请输入姓名/工号"
                :auto-clear-search-value="false"
                option-label-prop="label"
                @search="onSearch">
        <a-select-option v-for="(item, index) in dataSource"
                         :value="item.employeeNumber + ',' + item.name"
                         :key="'employee-' + index"
                         :label="item.name">
          {{ item.employeeNumber }}, {{ item.name }}
        </a-select-option>
      </a-select>
    </div>
    <div class="footer justify-end">
      <a-button @click="cancelEvent"
                size="small">取消</a-button>
      <a-button style="margin-left:8px"
                @click="confirmEvent"
                type="primary"
                size="small">确定</a-button>
    </div>
  </a-modal>
</template>

<script>
  export default {
    props: {

    },
    data () {
      return {
        values: [],
        dataSource: [],
        isShowDialog: false
      }
    },
    methods: {
      async onSearch (searchText) {
        // console.log('onSearch', searchText)
        const params = { nameOrNumber: searchText }
        let res = await this.$http.post('/setting/userSetting/getHREmployee', params)
        // console.log('onSearch: ', res)
        if (res.code === 0) {
          this.dataSource = res.data
        }
      },
      // onSelect(value) {
      //   // console.log('onSelect', value)
      //   // console.log('onSelect-values', this.values)
      // },
      // onChange(value) {
      //   // console.log('onChange', value)
      // },
      // 确认点击事件
      async confirmEvent () {
        let params = []
        this.values.forEach(item => {
          let userInfoArr = item.split(',')
          let userInfoObj = {
            userName: userInfoArr[1],
            isAdmin: 1,
            employeeNumber: userInfoArr[0]
          }
          params.push(userInfoObj)
        })
        if (params.length > 0) {
          let res = await this.$http.post('/setting/userSetting/addManager', params)
          // // console.log('--------res: ', res)
          if (res.code === 0) {
            this.$message.success({ content: '添加成功', duration: 2 })
            this.values = []
            this.isShowDialog = false
            this.$emit('confirmEvent', true)
          } else {
            this.$message.error({ content: res.msg, duration: 2 })
          }
        }
        // this.isShowDialog = false
        // this.$emit('confirmEvent', false)
      },
      // 取消点击事件
      cancelEvent () {
        this.values = []
        this.isShowDialog = false
        this.$emit('cancelEvent', false)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .add-sys-dialog {
    /deep/ .ant-modal-body {
      padding: 0;
    }

    .search {
      height: 28px;
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
          overflow-y: auto;
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
