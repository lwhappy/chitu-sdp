<template>
  <div class="senior"
       :class="fileType"
       v-loading="isLoading">
    <a-collapse class="collapse-container"
                :default-active-key="[1,2]"
                @change="collapseChange"
                :bordered="false">
      <template #expandIcon="props">
        <a-icon type="caret-right"
                :rotate="props.isActive ? 90 : 0" />
      </template>
      <a-collapse-panel key="1"
                        header="基础信息"
                        :style="customStyle">
        <div class="inner"
             :class="{'is-max':isMaxWidth}">
          <div class="row "
               :class="{'justify-start':!isMaxWidth}">
            <p class="label">计算引擎</p>
            <a-select class="select"
                      v-model="engine"
                      @change="change">
              <a-select-option v-for="item in engineList"
                               :key="item.id">
                {{ item.engineName }}
              </a-select-option>
            </a-select>
          </div>
          <div v-if="engine"
               class="row "
               :class="{'justify-start':!isMaxWidth}">

            <!-- <p class="label">引擎版本</p>
            <div class="engine-version">{{jobConfig.version}}</div> -->
            <p class="label">flink版本</p>
            <a-select class="select"
                      v-model="flinkVersion"
                      @change="flinkVersionChange">
              <a-select-option v-for="(item,key) in flinkVersionList"
                               :key="key"
                               :value="item.version">
                <div class="justify-start">{{ item.version }} <i class="chitutree-h5 chitutreebeta"
                     v-if="item.betaFlag == 1"></i></div>
              </a-select-option>
            </a-select>
          </div>
          <template v-if="fileType === 'SQL'">
            <div class="row"
                 :class="{'justify-start':!isMaxWidth}">
              <p class="label">选择UDX资源文件</p>
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
                        @change="handleChangeSource">
                <a-spin v-if="isFetchingSource"
                        slot="notFoundContent"
                        size="small" />
                <a-select-option v-for="(item,index) in sourceData"
                                 :value="JSON.stringify(item)"
                                 :key="String(item.id) + index">
                  <a-tooltip placement="topLeft">
                    <template slot="title">
                      <div>
                        <p> {{ item.name }}</p>
                        <p class="description">{{item.description}}</p>
                      </div>
                    </template>
                    <div>
                      <div>
                        <p> {{ item.name }}</p>
                        <p class="description">{{item.description}}</p>
                      </div>
                    </div>
                  </a-tooltip>
                </a-select-option>
              </a-select>

            </div>
            <div class="row"
                 :class="{'justify-start':!isMaxWidth}">
              <p class="label">选择UDX资源版本</p>
              <a-select placeholder="请选择UDX资源版本"
                        class="select-source"
                        dropdownClassName="select-source-option"
                        label-in-value
                        :allow-clear="true"
                        v-model="jarVersion"
                        :filter-option="false"
                        :not-found-content="isFetchingJarVersion ? undefined : null"
                        @change="handleChangeJarVersion">
                <a-spin v-if="isFetchingJarVersion"
                        slot="notFoundContent"
                        size="small" />
                <a-select-option v-for="(item,index) in jarVersionData"
                                 :value="JSON.stringify(item)"
                                 :key="String(item.id) + index">
                  <a-tooltip placement="topLeft">
                    <template slot="title">
                      <div>
                        <p> {{ item.version }}</p>
                        <p class="description">{{item.description}}</p>
                      </div>
                    </template>
                    <div>
                      <div>
                        <p> {{ item.version }}</p>
                        <p class="description">{{item.description}}</p>
                      </div>
                    </div>
                  </a-tooltip>
                </a-select-option>
              </a-select>
            </div>
          </template>
        </div>
      </a-collapse-panel>
      <a-collapse-panel class="flink-collapse"
                        key="2"
                        header="flink配置"
                        :style="customStyle"></a-collapse-panel>
    </a-collapse>
    <div v-show="collapseActive"
         class="flink-wrapper">
      <div class="config-editor-container"
           ref="configEditor"></div>
    </div>
  </div>
</template>

<script>
  import * as monaco from "monaco-editor"

  export default {
    name: "SeniorConfig",
    data () {
      return {
        collapseActive: true,
        customStyle: 'border-radius: 4px;margin-bottom: 0;border: 0;overflow: hidden;background:#FFF',
        isLoading: false,
        engineList: [],
        projectId: '',
        selectId: '',
        jobConfig: {},
        engine: '',
        engineVersionList: [{ name: 'flink-1.13' }],
        flinkVersion: '',
        // engineVersion: 'flink-1.13',
        flinkYaml: '',
        monacoInstance: null,
        isFetchingSource: false,
        isFetchingJarVersion: false,
        sourceData: [],
        jarVersionData: [],
        source: '',
        jarVersion: '',
        flinkVersionList: []
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
      fileType: {
        type: String,
        default: ''
      },
      isMaxWidth: {
        type: Boolean,
        default: true
      }
    },
    computed: {
    },
    components: {
    },
    watch: {
      config: {
        async handler (val) {

          if (val) {

            this.jobConfig = JSON.parse(JSON.stringify(val))
            this.engine = this.jobConfig.engine
            this.flinkVersion = this.jobConfig.flinkVersion
            if (this.jobConfig.jarId && this.jobConfig.jarName && this.jobConfig.jarVersion) {
              const selectSource = {
                name: this.jobConfig.jarName,
                version: this.jobConfig.jarVersion,
                id: this.jobConfig.jarId
              }
              this.source = { key: JSON.stringify(selectSource), label: selectSource.name }

            }
            this.flinkYaml = this.jobConfig.flinkYaml
            // if (!this.flinkYaml) {
            //   this.flinkYaml = 'deployment:\n'
            //   this.flinkYaml += 'property: #@see: https://ci.apache.org/projects/flink/flink-docs-release-1.12/deployment/config.html\n'
            //   this.flinkYaml += '$internal.application.main:\n'
            //   this.flinkYaml += 'yarn.application.name:\n'
            //   this.flinkYaml += 'yarn.application.queue:\n'
            //   this.flinkYaml += 'taskmanager.numberOfTaskSlots: 1\n'
            //   this.flinkYaml += 'parallelism.default: 2\n'
            //   this.flinkYaml += 'jobmanager.memory:\n'
            //   this.flinkYaml += 'flink.size:\n'
            //   this.flinkYaml += 'heap.size:\n'
            //   this.flinkYaml += 'jvm-metaspace.size:\n'
            //   this.flinkYaml += 'jvm-overhead.max:\n'
            //   this.flinkYaml += 'off-heap.size:\n'
            //   this.flinkYaml += 'process.size:\n'
            //   this.flinkYaml += 'taskmanager.memory:\n'
            //   this.flinkYaml += 'flink.size:\n'
            //   this.flinkYaml += 'framework.heap.size:\n'
            //   this.flinkYaml += 'framework.off-heap.size:\n'
            //   this.flinkYaml += 'managed.size:\n'
            //   this.flinkYaml += 'process.size:\n'
            //   this.flinkYaml += 'task.heap.size:\n'
            //   this.flinkYaml += 'task.off-heap.size:\n'
            //   this.flinkYaml += 'jvm-metaspace.size:\n'
            //   this.flinkYaml += 'jvm-overhead.max:\n'
            //   this.flinkYaml += 'jvm-overhead.min:\n'
            //   this.flinkYaml += 'managed.fraction: 0.4\n'
            //   this.flinkYaml += 'checkpoints:\n'
            //   this.flinkYaml += 'enable: true\n'
            //   this.flinkYaml += 'interval: 30000\n'
            //   this.flinkYaml += 'mode: EXACTLY_ONCE\n'
            //   this.flinkYaml += 'timeout: 300000\n'
            //   this.flinkYaml += 'unaligned: true\n'
            //   this.flinkYaml += 'watermark:\n'
            //   this.flinkYaml += 'interval: 10000\n'
            //   this.flinkYaml += '# 状态后端\n'
            //   this.flinkYaml += 'state:\n'
            //   this.flinkYaml += 'backend: # see https://ci.apache.org/projects/flink/flink-docs-rel'
            // }
            this.isLoading = true
            //获取到文件详情后再获取engine和jar包
            this.getEngine()//保存时需要提交默认的flink引擎配置，没点作业配置，也需要先加载好
            await this.getSource()
            if (!this.source) {
              this.isLoading = false
              return
            }
            await this.getJarVersion(this.jobConfig.jarName)
            this.isLoading = false
            if (this.jarVersionData && this.jarVersionData.length) {//找到与version匹配的项并选中
              const findItem = this.jarVersionData.filter(item => item.version === this.jobConfig.jarVersion)
              if (findItem && findItem.length) {
                this.jarVersion = { key: JSON.stringify(findItem[0]), label: findItem[0].version }
              }

            }
          }


        },
        immediate: true,
        deep: true
      },
      isShow: {
        async handler (val) {
          if (val && !this.monacoInstance) {
            this.init()
          }
          if (val) {
            //获取到文件详情后再获取engine和jar包
            this.getEngine()//每次展开配置时都加载最新的配置
            await this.getSource()
            if (!this.source) {
              return
            }
            await this.getJarVersion(this.jobConfig.jarName)
            if (this.jarVersionData && this.jarVersionData.length) {//找到与version匹配的项并选中
              const findItem = this.jarVersionData.filter(item => item.version === this.jobConfig.jarVersion)
              if (findItem && findItem.length) {
                this.jarVersion = { key: JSON.stringify(findItem[0]), label: findItem[0].version }
              }

            }
          }

        },
        immediate: true
      }
    },
    created () {

    },
    mounted () {
      this.getFlinkVersionList()
    },
    methods: {
      // 获取flink版本
      async getFlinkVersionList () {
        const params = {}
        const res = await this.$http.post('/flink/version/list', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.flinkVersionList = res.data
        }
      },
      init () {
        // // console.log('monaco.languages.getLanguages()', monaco.languages.getLanguages())
        // 初始化编辑器实例
        this.monacoInstance = monaco.editor.create(
          this.$refs["configEditor"], {
          value: this.flinkYaml,
          language: 'yaml',
          minimap: {
            enabled: false
          },
          automaticLayout: true,
          scrollBeyondLastLine: false
          // wordWrap: 'on'
        });

      },
      change (value) {
        this.selectId = value
        const findItem = this.engineList.filter(item => {
          return String(item.id) === String(value)
        })
        this.jobConfig.engine = findItem[0].engineName
        this.jobConfig.engineId = findItem[0].id
        this.jobConfig.version = findItem[0].engineVersion
        // this.$emit('change', this.jobConfig)
      },
      async getEngine () {//获取引擎
        const params = { projectId: Number(this.$route.query.projectId) }
        const res = await this.$http.post('/file/getFileEngines', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.engineList = res.data
          if (!this.engine) {//默认传第一行
            this.engine = this.engineList[0].engineName
            this.jobConfig.engine = this.engineList[0].engineName
            this.jobConfig.engineId = this.engineList[0].id
            this.jobConfig.version = this.engineList[0].engineVersion
          } else {
            const findItem = this.engineList.filter(item => item.engineName === this.engine)
            if (findItem && findItem.length) {//修复服务器数据有engine而没有engineId的情况，确保点保存时一定有engineId
              this.jobConfig.engine = findItem[0].engineName
              this.jobConfig.engineId = findItem[0].id
              this.jobConfig.version = findItem[0].engineVersion
            }
          }
        }
      },
      handleSourceSearch (value) {
        this.getSource(value)
      },
      flinkVersionChange (value) {
        this.jobConfig.flinkVersion = value
      },
      async handleChangeSource (value) {
        if (value) {
          const jar = JSON.parse(value.key)
          this.jobConfig.jarName = jar.name
          this.jobConfig.jarId = jar.id
          // this.jobConfig.jarVersion = jar.version//默认选择主版本
          await this.getJarVersion(this.jobConfig.jarName)
          if (this.jarVersionData && this.jarVersionData.length) {//切换资源时默认选中第一个
            this.jarVersion = { key: JSON.stringify(this.jarVersionData[0]), label: this.jarVersionData[0].version }
            this.jobConfig.jarVersion = this.jarVersionData[0].version
          }

        } else {
          this.jobConfig.jarVersion = ''
          this.jobConfig.jarName = ''
          this.jobConfig.jarId = ''
        }

      },

      handleChangeJarVersion (value) {
        if (value) {
          const selectVersion = JSON.parse(value.key)
          this.jobConfig.jarVersion = selectVersion.version
          this.jobConfig.jarId = selectVersion.id
        } else {
          this.jobConfig.jarVersion = ''
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
          const findCurrent = this.sourceData.filter(item => {
            // return String(item.id) === String(this.jobConfig.jarId)
            return item.name === this.jobConfig.jarName//不能用id,jarId是子版本的id
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
      collapseChange (key) {
        if (key.includes('2')) {
          this.collapseActive = true
        } else {
          this.collapseActive = false
        }
      }
    }
  }
</script>
<style lang="scss">
  .chitutreebeta {
    font-size: 40px !important;
    font-weight: 100 !important;
    color: #298dff;
    margin-left: 4px;
  }
  .select-source-option {
    .ant-select-dropdown-content .ant-select-dropdown-menu {
      max-height: 220px !important;
    }
  }
</style>
<style lang="scss" scoped>
  .senior {
    .collapse-container {
      /deep/ .ant-collapse-header {
        font-size: 14px !important;
        font-weight: 900;
        padding: 5px 16px;
        padding-left: 40px;
        i {
          color: #006fff !important;
        }
      }
      /deep/ .ant-collapse-item {
        font-size: 12px;
      }
      .flink-collapse {
        /deep/ .ant-collapse-content-box {
          padding: 12px 16px 0 16px;
        }
      }
    }
    width: 100%;
    font-size: 12px;
    height: calc(100% - 32px);
    display: flex;
    justify-content: flex-start;
    align-items: stretch;
    flex-direction: column;
    overflow-x: hidden;
    overflow-y: auto;
    padding: 0 16px;
    .common-title {
      padding: 10px 0;
      i {
        color: #006fff;
        margin-right: 8px;
      }
      .name {
        font-size: 14px;
        font-weight: 900;
      }
    }
    .inner {
      width: 100%;
      .row {
        .label {
          width: 130px;
        }
        .select {
          width: 325px !important;
        }
        .select-source {
          width: 325px !important;
        }
      }
      &.is-max {
        display: flex;
        flex-wrap: wrap;
        .row {
          width: calc(25% - 8px);
          margin-right: 8px;
          .select {
            width: 100% !important;
          }
          .select-source {
            width: 100% !important;
          }
        }
      }
    }
    .flink-wrapper {
      flex: 1;
      display: flex;
      justify-content: flex-start;
      align-items: stretch;
      flex-direction: column;
      margin-top: 10px;
      .config-editor-container {
        width: 100%;
        height: 90%;
      }
    }
    h3 {
      color: #333;
      font-size: 12px;
      margin-bottom: 8px;
    }
    .select {
      width: 100%;
      font-size: 12px;
    }
    .row {
      width: 100%;
      margin-top: 12px;
      .engine-version {
        height: 32px;
        line-height: 32px;
      }
      .label {
        margin-bottom: 10px;
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
    }
    &.DS {
      .config-editor-container {
        height: calc(100vh - 370px);
      }
    }
  }
</style>