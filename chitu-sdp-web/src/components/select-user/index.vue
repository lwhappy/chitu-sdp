<!--
 * @Author: your name
 * @Date: 2021-10-17 14:21:32
 * @LastEditTime: 2022-06-24 11:44:28
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\components\select-user\index.vue
-->
<template>
  <div class="select-user-wrapper">

    <a-select class="select-user"
              show-search
              :show-arrow="false"
              placeholder="请输入姓名或工号"
              label-in-value
              :default-value="member"
              :filter-option="false"
              :not-found-content="isFetchingMember ? undefined : null"
              @search="searchMember"
              @select="handleChangeMember">
      <a-spin v-if="isFetchingMember"
              slot="notFoundContent"
              size="small" />
      <a-select-option v-for="(item,index) in memberData"
                       :value="JSON.stringify(item)"
                       :key="String(item.employeeNumber) + index">
        {{ item.name }}
      </a-select-option>
    </a-select>
    <div class="name-list justify-start">

      <div v-for="(item,index) in selectMember"
           class="name  justify-start"
           :key="item.employeeNumber"
           @click="deleteUser(index)">
        <p>{{item.name}}</p>
        <i class="chitutree-h5 chitutreeguanbi close"></i>
      </div>
    </div>
  </div>
</template>

<script>

  export default {
    name: "SelectUser",
    components: {
    },
    mixins: [],
    props: {

    },
    data () {
      return {
        member: [],
        selectMember: [],
        memberData: [],
        isFetchingMember: false,
      }
    },
    computed: {

    },
    mounted () {

    },
    methods: {

      handleChangeMember (value) {
        const member = JSON.parse(value.key)
        const find = this.selectMember.find(item => {
          return item.employeeNumber.toString() === member.employeeNumber
        })
        if (find) {
          return
        }
        this.selectMember.push(member)
      },
      async getMember (value) {
        const params = {
          "nameOrNumber": value || '' //引擎名称
        }
        this.isFetchingMember = true
        let res = await this.$http.post('/setting/userSetting/getHREmployee', params)
        this.isFetchingMember = false
        if (res.code === 0) {
          this.memberData = res.data
        }
      },
      searchMember (value) {
        this.getMember(value)
      },
      getUsers () {
        return this.selectMember
      },
      deleteUser (index) {
        this.selectMember.splice(index, 1)
      },
      init (users) {
        if (users) {
          this.selectMember = JSON.parse(JSON.stringify(users))
        }

      }
    },
  }
</script>
<style lang="scss" scoped>
  .select-user-wrapper {
    .select-user {
      width: 200px;
      height: 28px;
      /deep/ .ant-select-selection--single {
        height: 28px;
      }
      /deep/ .ant-select-selection__rendered {
        line-height: 28px;
      }
    }
    .name-list {
      padding-top: 8px;
      .name {
        margin-right: 8px;
        height: 24px;
        background: #eaeef7;
        // border: 1px solid #bda0ff;
        border-radius: 4px;
        padding: 0 8px;
        color: #93a1bb;
        font-weight: 500;
        .close {
          font-size: 12px !important;
          margin-left: 8px;
          // color: #9571e9;
          cursor: pointer;
          transform: scale(0.7);
        }
      }
    }
  }
</style>
