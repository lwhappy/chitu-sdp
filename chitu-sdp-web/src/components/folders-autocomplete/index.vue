
<template>
  <a-auto-complete class="folders-search"
                   size="large"
                   :placeholder="autoMsg"
                   option-label-prop="title"
                   v-model="keyword"
                   @change="onChange"
                   @select="onSelect"
                   @search="handleSearch">
    <template v-if="dataSource.length"
              slot="dataSource">
      <a-select-option v-for="item in dataSource"
                       class="tree-search-option"
                       :key="String(item.id)"
                       :originalData="item"
                       :title="item.name">

        <div class="folder"><img class="img-folder"
               src="@/assets/icons/fold-new.png"> <span>{{ item.name }}</span></div>

      </a-select-option>
    </template>
    <a-input>

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
        default: '搜索目录名'
      }
    },
    data () {
      return {
        keyword: '',
        confirmKeyword: '',
        dataSource: [],
      }
    },
    computed: {

    },
    watch: {
      '$route.query.projectId': {
        async handler (val) {
          if (val) {
            this.dataSource = []
          }
        },
        immediate: true
      }
    },
    created () {
    },
    mounted () {

    },
    methods: {
      onChange () {
        if (this.confirmKeyword !== this.keyword) {
          this.$emit('clearFolder')
        }
      },
      handleClick () {
        this.$emit('search', this.keyword)
      },
      onSelect (value, item) {
        this.confirmKeyword = this.keyword
        // console.log('onSelect', item)
        const originalData = item.data.attrs['originalData']
        this.$emit('searchSelect', originalData)
      },

      async handleSearch (value) {
        if (value) {

          const params = {
            folderName: value,
            projectId: Number(this.$route.query.projectId)
          }
          let res = await this.$http.post('/folder/searchFolders', params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0 && res.data) {
            let data = res.data
            this.dataSource = data
            const rootName = '作业开发'
            if (rootName.indexOf(value) >= 0) {
              const obj = {
                enabledFlag: 1,
                id: "0",
                isLeaf: true,
                name: "作业开发",
              }
              this.dataSource.unshift(obj)
            }
          }
        } else {
          this.$emit('search', '')
        }

      },


    },
  }
</script>
<style lang="scss" scoped>
  .folders-search {
    /deep/ .ant-select-selection {
      // background: #fafafa;
      .ant-select-selection__rendered {
        line-height: 30px;
      }
    }
    //自动补齐输入框
    /deep/ .ant-input {
      background: none;
      // border-radius: 0;
      height: 28px !important;
      width: 222px;
    }
  }
  .tree-search-option {
    .folder {
      .img-folder {
        width: 16px;
        height: 16px;
      }
    }
    .file {
      i {
        margin-right: 3px;
      }
    }
  }
</style>
