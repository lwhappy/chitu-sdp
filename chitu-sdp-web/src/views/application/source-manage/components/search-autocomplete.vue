
<template>
  <a-auto-complete class="global-search"
                   size="large"
                   :placeholder="autoMsg"
                   :defaultActiveFirstOption="false"
                   option-label-prop="title"
                   v-model="keyword"
                   @select="onSelect"
                   @search="handleSearch">
    <template v-if="dataSource.length"
              slot="dataSource">
      <a-select-option v-for="item in dataSource"
                       :key="String(item.id)"
                       :originalData="item"
                       :title="item.name">

        {{ item.name }}
      </a-select-option>
    </template>
    <a-input @pressEnter="handleClick"
             @change="handleClick">
    </a-input>
  </a-auto-complete>
</template>

<script>

  export default {
    name: "JarSearch",
    components: {
    },
    mixins: [],
    props: {
      autoMsg: {
        type: String,
        default: '搜索jar包名称'
      }
    },
    data () {
      return {
        oldProjectId: '',
        keyword: '',
        dataSource: [],
      }
    },
    computed: {

    },
    created () {
    },
    mounted () {

    },
    watch: {
      $route: {
        handler (val) {
          if (val) {
            if (this.$route.query.projectId !== this.oldProjectId && this.$route.name === 'SourceManage') {
              this.init()
              this.oldProjectId = this.$route.query.projectId
            }
          }
        },
        deep: true,
        immediate: true
      }
    },
    methods: {
      init () {
        this.keyword = ''
        this.dataSource = []
      },
      handleClick () {
        this.$nextTick(() => {
          this.$emit('search', this.keyword)
        })
      },
      onSelect (value, item) {
        this.keyword = item.data.attrs['originalData'].name
        this.$emit('search', this.keyword)
      },

      async handleSearch (value) {
        if (value) {

          const params = {
            orderByClauses: [{
              field: "creation_date",
              orderByMode: 1
            }],
            page: 1,
            pageSize: 20,
            vo: {
              projectId: Number(this.$route.query.projectId),
              name: value,
              type: 'first'
            }
          }
          let res = await this.$http.post('/jar/queryJar', params)
          if (res.code === 0 && res.data) {
            this.dataSource = res.data.rows
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
