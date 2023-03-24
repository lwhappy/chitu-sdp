<!--
 * @Author: your name
 * @Date: 2021-10-17 14:21:32
 * @LastEditTime: 2022-07-04 17:43:36
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\components\search-autocomplete\index.vue
-->
<template>
  <div class="search-autocomplete">

    <a-auto-complete class="global-search"
                     size="large"
                     style="width: 100%"
                     :defaultActiveFirstOption="false"
                     :placeholder="autoMsg"
                     option-label-prop="title"
                     v-model="keyword"
                     @select="onSelect"
                     @search="handleSearch">
      <template v-if="dataSource.length"
                slot="dataSource">
        <a-select-option v-for="item in dataSource"
                         :key="String(item.id)"
                         :originalData="item"
                         :title="item.projectName">

          {{ item.projectName }}
        </a-select-option>
      </template>
      <a-input @pressEnter="handleClick"
               @change="handleClick">
      </a-input>
    </a-auto-complete>

  </div>
</template>

<script>
  export default {
    name: "Top",
    components: {
    },
    mixins: [],
    props: {
      autoMsg: {
        type: String,
        default: '搜索项目名称'
      }
    },
    data () {
      return {
        keyword: '',
        dataSource: [],
        productLineNameList: []
      }
    },
    computed: {

    },
    mounted () {

    },
    methods: {
      handleClick () {
        this.$nextTick(() => {
          this.$emit('search', this.keyword)
        })

      },
      onSelect (value, item) {
        this.keyword = item.data.attrs['originalData'].projectName
        this.$emit('search', this.keyword)
      },

      async handleSearch (value) {
        if (value) {
          const params = {
            "projectName": value //模糊搜索项目名
          }
          let res = await this.$http.post('/project/projectManagement/search', params)
          if (res.code === 0) {
            this.dataSource = res.data
          }
        } else {
          this.$emit('search', '')
        }

      },

    },
  }
</script>
<style lang="scss" scoped>
  .global-search {
    //自动补齐输入框
    /deep/ .ant-input {
      height: 28px !important;
      width: 184px;
    }
    /deep/ .search-btn {
      width: 28px;
      height: 28px;
      text-align: center;
      cursor: pointer;
      color: #fff;
      i {
        font-size: 14px;
      }
    }
  }
</style>
