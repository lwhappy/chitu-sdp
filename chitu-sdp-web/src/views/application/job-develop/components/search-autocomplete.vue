
<template>
  <a-auto-complete class="tree-search"
                   size="large"
                   style="width: 100%"
                   :placeholder="autoMsg"
                   option-label-prop="title"
                   v-model="keyword"
                   @select="onSelect"
                   @search="handleSearch">
    <template v-if="dataSource.length"
              slot="dataSource">
      <a-select-option v-for="item in dataSource"
                       class="tree-search-option"
                       :key="String(item.key)"
                       :originalData="item">

        <div v-if="item.type==='folder'"
             class="folder">
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>{{ item.name }}</span>
            </template>
            <img class="img-folder"
                 src="@/assets/icons/fold-new.png">
            <span>{{ item.name }}</span>
          </a-tooltip>

        </div>
        <div v-else-if="item.type==='file'"
             class="file">
          <template v-if="item.fileType === 'SQL'">
            <i v-if="Number(item.isOnLine) === 0"
               class="chitutree-h5 chitutreeSQL"></i>
            <i v-if="Number(item.isOnLine) === 1"
               class="chitutree-h5 chitutreeSQL green"></i>
          </template>
          <template v-else-if="item.fileType === 'DS'">
            <i v-if="Number(item.isOnLine) === 0"
               class="chitutree-h5 chitutreeDS"></i>
            <i v-if="Number(item.isOnLine) === 1"
               class="chitutree-h5 chitutreeDS cyanine"></i>
          </template>
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>{{ item.name }}</span>
            </template>
            <span>{{ item.name }}</span>
          </a-tooltip>
        </div>
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
        default: '搜索目录名或文件名称'
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
            if (this.$route.query.projectId !== this.oldProjectId && this.$route.name === 'JobDevelop') {
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
        this.$emit('search', this.keyword)
      },
      onSelect (value, item) {
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
          let res = await this.$http.post('/folder/searchFolder', params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0 && res.data) {
            let data = res.data
            data = data.map(item => {
              item.key = item.id
              if (item.type === 'file') {
                item.key = 'file_' + item.id
              }
              return item
            })
            this.dataSource = data
          }
        } else {
          this.$emit('search', '')
        }

      },


    },
  }
</script>
<style lang="scss" scoped>
  .tree-search {
    /deep/ .ant-select-selection {
      background: #fafafa;
      .ant-select-selection__rendered {
        line-height: 30px;
      }
    }
    //自动补齐输入框
    /deep/ .ant-input {
      border: none;
      border-radius: 0;
      height: 28px;
      width: 222px;
      margin-right: 16px;
      &:focus,
      &:hover {
        border-color: #d9d9d9 !important;
        box-shadow: none;
      }
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
