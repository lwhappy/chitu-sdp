<template>
  <div class="DS"
       :class="{'is-max':isMaxWidth}">
    <div class="row">
      <p class="label">选择UDX资源文件</p>
      <div class="justify-between">
        <a-select placeholder="请选择UDX资源文件"
                  class="select-source"
                  dropdownClassName="select-source-option"
                  show-search
                  label-in-value
                  :allow-clear="true"
                  v-model="source"
                  :filter-option="false"
                  :not-found-content="isFetchingSource ? undefined : null"
                  @search="handleSourceSearch"
                  @change="handleChangeSource"
                  :disabled="!isAllowEdit">
          <a-spin v-if="isFetchingSource"
                  slot="notFoundContent"
                  size="small" />
          <a-select-option v-for="(item,index) in sourceData"
                           :value="JSON.stringify(item)"
                           :key="String(item.id) + index"
                           @mouseenter="sourceMouseenter(item)">
            <a-tooltip placement="topLeft">
              <template slot="title">
                <div>
                  <p class="name"> {{ item.name }}</p>
                  <p class="description">{{item.description}}</p>
                </div>
              </template>
              <div>
                <div>
                  <p class="name"> {{ item.name }}</p>
                  <p class="description">{{item.description}}</p>
                </div>
              </div>
            </a-tooltip>
          </a-select-option>
        </a-select>
      </div>

    </div>
    <div class="row">
      <p class="label">选择UDX资源版本</p>
      <div class="justify-between">
        <a-select placeholder="请选择UDX资源版本"
                  class="select-source"
                  dropdownClassName="select-source-option"
                  label-in-value
                  :allow-clear="true"
                  v-model="jarVersion"
                  :filter-option="false"
                  :not-found-content="isFetchingJarVersion ? undefined : null"
                  @change="handleChangeJarVersion"
                  :disabled="!isAllowEdit">
          <a-spin v-if="isFetchingJarVersion"
                  slot="notFoundContent"
                  size="small" />
          <a-select-option v-for="(item,index) in jarVersionData"
                           :value="JSON.stringify(item)"
                           :key="String(item.id) + index"
                           @mouseenter="jarVersionMouseenter(item)">
            <a-tooltip placement="topLeft">
              <template slot="title">
                <div>
                  <p class="name"> {{ item.version }}</p>
                  <p class="description">{{item.description}}</p>
                </div>
              </template>
              <div>
                <div>
                  <p class="name"> {{ item.version }}</p>
                  <p class="description">{{item.description}}</p>
                </div>
              </div>
            </a-tooltip>
          </a-select-option>
        </a-select>
      </div>

    </div>
    <div class="row">
      <p class="label">Main-Class</p>
      <a-input v-model="dataStreamConfig.mainClass"
               class="main-class"
               placeholder="请输入jar包的Main-Class，示例：com.chitu.Application"
               :disabled="!isAllowEdit" />
    </div>
  </div>
</template>

<script>
  export default {
    name: "DSConfig",
    data () {
      return {
        isAllowEdit: true,
        isLoading: false,
        isFetchingSource: false,
        isFetchingJarVersion: false,
        sourceData: [],
        jarVersionData: [],
        source: '',
        jarVersion: '',
        dataStreamConfig: {
          jarId: '', //jar包id
          jarName: '', //jar包名称
          jarVersion: '', //jar包版本
          mainClass: '' //主函数名称
        }
      }
    },
    props: {
      config: {
        type: Object,
        default: () => {
          return {}
        }
      },
      isShow: {
        type: Boolean,
        default: false
      },
      isMaxWidth: {
        type: Boolean,
        default: true
      },
      fileId: {
        type: [String, Number],
        default: ''
      }
    },
    computed: {
      isProdEnv () {
        return this.$store.getters.env === 'prod'
      }
    },
    components: {
    },
    watch: {
      config: {
        handler (val) {
          if (val) {
            if (this.isProdEnv) {
              this.isAllowEditFile
            }
            this.dataStreamConfig = JSON.parse(JSON.stringify(val))
            if (this.dataStreamConfig.jarId && this.dataStreamConfig.jarName && this.dataStreamConfig.jarVersion) {
              const selectSource = {
                name: this.dataStreamConfig.jarName,
                version: this.dataStreamConfig.jarVersion,
                id: this.dataStreamConfig.jarId
              }
              this.source = { key: JSON.stringify(selectSource), label: selectSource.name }//label-in-value模式
              // if (this.sourceData.length) {//此时的情况是用户打开了ds配置，修改了配置，再点解锁，没有触发isShow的watch,需要恢复到初始化的配置
              //   const findCurrent = this.sourceData.filter(item => {//如果jar包已经删除，但仍有这个配置，则不默认选择
              //     return String(item.id) === String(this.dataStreamConfig.jarId)
              //   })
              //   if (findCurrent.length === 0) {
              //     this.source = ''
              //     this.jarVersion = ''
              //   }
              // }

            }

          }
        },
        immediate: true,
        deep: true
      },
      isShow: {
        async handler (val) {
          if (val) {
            this.isLoading = true
            await this.getSource()
            if (!this.source) {
              this.isLoading = false
              return
            }
            await this.getJarVersion(this.dataStreamConfig.jarName)
            this.isLoading = false
            if (this.jarVersionData && this.jarVersionData.length) {
              const findItem = this.jarVersionData.filter(item => item.version === this.dataStreamConfig.jarVersion)
              if (findItem && findItem.length) {
                this.jarVersion = { key: JSON.stringify(findItem[0]), label: findItem[0].version }
              }

            }
          }
        }
      }
    },
    created () {
    },
    methods: {
      async isAllowEditFile () {
        let res = await this.$http.post('/file/allowEditFile', {
          id: this.fileDetail.id
        }, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.isAllowEdit = res.data.allowEdit
        }
      },
      handleSourceSearch (value) {
        this.getSource(value)
      },
      async handleChangeSource (value) {
        if (value) {
          const jar = JSON.parse(value.key)
          this.dataStreamConfig.jarName = jar.name
          this.dataStreamConfig.jarId = jar.id
          this.dataStreamConfig.jarVersion = ''//先赋空，以免获取版本号比较慢，然后点保存，把切换前的版本号提交上去
          // this.dataStreamConfig.jarVersion = jar.version//默认选择主版本
          await this.getJarVersion(this.dataStreamConfig.jarName)
          if (this.jarVersionData && this.jarVersionData.length) {
            this.dataStreamConfig.jarVersion = this.jarVersionData[0].version
            this.dataStreamConfig.jarId = this.jarVersionData[0].id
            this.jarVersion = { key: JSON.stringify(this.jarVersionData[0]), label: this.jarVersionData[0].version }//默认选择第一个最新的
          }
        } else {
          this.dataStreamConfig.jarVersion = ''
          this.dataStreamConfig.jarName = ''
          this.dataStreamConfig.jarId = ''
        }

      },

      handleChangeJarVersion (value) {
        if (value) {
          const selectVersion = JSON.parse(value.key)
          this.dataStreamConfig.jarVersion = selectVersion.version
          this.dataStreamConfig.jarId = selectVersion.id
        } else {
          this.dataStreamConfig.jarVersion = ''
        }

      },

      async getSource (value) {
        const params = {
          name: value || '', //引擎名称
          projectId: Number(this.$route.query.projectId),
          version: 'v1'
        }
        this.isFetchingSource = true
        let res = await this.$http.post('/jar/searchJar', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isFetchingSource = false
        if (res.code === 0) {
          this.sourceData = res.data
          const findCurrent = this.sourceData.filter(item => {//如果jar包已经删除，但仍有这个配置，则不默认选择
            // return String(item.id) === String(this.dataStreamConfig.jarId)
            return item.name === this.dataStreamConfig.jarName//不能用id,jarId是子版本的id
          })
          if (findCurrent.length === 0) {
            this.source = ''
          }
        }
      },
      async getJarVersion (jarName) {
        const params = {
          orderByClauses: [{
            field: "updation_date",
            orderByMode: 1
          }],
          page: 1,
          pageSize: 2000,
          vo: {
            name: jarName,
            projectId: Number(this.$route.query.projectId)
          }
        }
        this.jarVersionData = []
        this.isFetchingJarVersion = true
        let res = await this.$http.post('/jar/queryJar', params)
        this.isFetchingJarVersion = false
        if (res.code === 0) {
          if (res.data) {
            if (res.data.rows) {
              this.jarVersionData = res.data.rows


            } else {
              this.jarVersionData = []
            }
            return this.jarVersionData
          }
        } else {
          this.$message.error(res.msg);
        }
      },

    },
    mounted () {

    }
  }
</script>
<style lang="scss">
  .select-source-option {
    .ant-select-dropdown-content .ant-select-dropdown-menu {
      max-height: 220px !important;
    }
    .name {
      color: #0066ff;
    }
    .description {
      color: #999;
    }
  }
</style>
<style lang="scss" scoped>
  .DS {
    width: 100%;
    font-size: 12px;
    padding: 0 16px;
    .row {
      margin-top: 12px;
      .label {
        margin-bottom: 10px;
      }
      .main-class {
        width: 100%;
      }
      .select-source {
        width: 100%;
        font-size: 12px;
        /deep/ .name {
          width: 100%;
          display: block;
        }
        /deep/ .description {
          display: none;
        }
      }
      .description {
        width: 100px;
        text-align: right;
        margin-right: 16px;
        height: 32px;
        overflow: hidden;
      }
      .col {
        width: 45%;
      }
      /deep/ input {
        font-size: 12px;
      }
      .tip {
        color: #999;
      }
    }
    &.is-max {
      display: flex;
      flex-wrap: wrap;
      .row {
        width: calc(50% - 6px);
        margin-right: 6px;
      }
    }
  }
</style>