<template>
  <data-hub-drawer v-loading="loading"
                   class="change-prod-drawer"
                   :drawerVisible="visible"
                   :maskClosable="false"
                   :showExpand="false"
                   isMaxWith
                   title="转生产环境"
                   @close="closeDrawer">
    <!-- 集群信息 -->
    <template #content>
      <h3 class="form-title justify-start"
          v-show="fileType!=='DS'">
        <div class="data-source-relation justify-start">
          <span class="line "></span>
          数据源映射关系
        </div>
      </h3>
      <div v-show="fileType!=='DS'"
           class="default-tip">
        <p> 1.转环境时需配置UAT环境与生产环境的数据源实例映射关系，系统会默认匹配同一名称的生产环境数据源实例与UAT环境数据源实例，无法默认带出则需手动匹配；</p>
        <p> 2.若无数据源实例，则需进入生产环境的“实时开发-数据源管理”页面，新建数据源实例;</p>
      </div>
      <div class="table-container"
           v-if="fileType!=='DS'">
        <EnvTable v-if="envTableVisible"
                  :tableData="tableData" />
      </div>
      <h3 class="form-title justify-start mt-10 ">
        <div class="data-source-preview justify-start">
          <span class="line"></span>
          <span>同步内容预览</span>
        </div>
      </h3>
      <div class="default-tip">
        <template v-if="fileType!=='DS'">
          <template v-if="compareVersions.length>1">
            <p>非首次转环境：</p>
            <p>1.仅同步作业代码到生产环境，但是source kafka的消费位置(scan.startup.mode
              )选项信息以生产环境的为主，不会进行覆盖，如果需要修改，需要切换到生产环境-作业运维-启动-勾选修改消费位置进行修改；</p>
            <p>2.高级配置、资源配置不会同步到生产环境，如果需要修改，需要切换到生产环境-作业开发-高级配置、生产环境-作业开发-资源配置进行修改；</p>
            <p>3.转环境只是将代码从UAT转到生产, 上线还需要自己手动发布并经过审批；</p>
          </template>
          <template v-else>
            <p>首次转环境：</p>
            <p>1.同步作业代码、高级配置、资源配置到生产环境；</p>
            <p>2.转环境只是将代码从UAT转到生产, 上线还需要自己手动发布并经过审批；</p>
          </template>
        </template>
        <template v-else>
          <template v-if="compareVersions.length>1">
            <p>非首次转环境：</p>
            <p>1.仅同步DS配置到生产环境；</p>
            <p>2.高级配置、资源配置不会同步到生产环境，如果需要修改，需要切换到生产环境-作业开发-高级配置、生产环境-作业开发-资源配置进行修改；</p>
            <p>3.转环境只是将代码从UAT转到生产, 上线还需要自己手动发布并经过审批；</p>
          </template>
          <template v-else>
            <p>首次转环境：</p>
            <p>1.同步DS配置、高级配置、资源配置到生产环境</p>
            <p>2.转环境只是将代码从UAT转到生产, 上线还需要自己手动发布并经过审批；</p>
          </template>
        </template>
      </div>
      <CompareVersion v-if="envTableVisible"
                      :dataInfo="compareVersions"
                      :fileType="fileType" />
    </template>
    <template #footer>
      <a-space>
        <a-button size="small"
                  @click="closeDrawer">
          取消
        </a-button>
        <a-button type="primary"
                  ghost
                  size="small"
                  :loading="isLoading"
                  @click="handleSubmit(false)">
          确认 (不自动切换环境)
        </a-button>
        <a-button type="primary"
                  size="small"
                  :loading="isLoading"
                  @click="handleSubmit(true)">
          确认
        </a-button>
      </a-space>
    </template>
  </data-hub-drawer>
</template>

<script>
  import EnvTable from './envTable.vue'
  import CompareVersion from './compareVersion'
  import intro from 'intro.js'
  import 'intro.js/minified/introjs.min.css'
  export default {
    name: '',
    components: { EnvTable, CompareVersion },
    props: {
      visible: {
        type: Boolean,
        default: false
      },
      fileData: {
        type: Object,
        default: () => null
      },

    },
    data () {
      return {
        loading: false,
        isLoading: false,
        fileType: '',
        fileId: '',
        projectId: '',
        tableData: [],
        compareVersions: [],
        envTableVisible: false,
      };
    },
    computed: {

    },
    watch: {
      visible (value) {
        if (value) {
          this.init()
          this.getCompareData()
        }
      }
    },
    created () { },
    mounted () {
    },
    methods: {
      showFeatureGuide () {
        const guideKey = `sdp-data-source-guide`
        if (!localStorage.getItem(guideKey)) {
          this.handleGuideStart(guideKey)
        }
      },
      handleGuideStart (guideKey) {
        let introInstance = null
        if (this.fileType !== 'DS') {
          introInstance = intro().setOptions({
            showButtons: true,
            doneLabel: '我知道了',
            showBullets: false,
            showProgress: false,
            showStepNumbers: false,
            prevLabel: '上一步',
            nextLabel: '下一步',
            steps: [
              {
                title: '功能引导',
                element: document.querySelector(`.data-source-relation`),
                intro:
                  `转环境时需配置UAT环境与生产环境的数据源实例映射关系，系统会默认匹配同一名称的生产环境数据源实例与UAT环境数据源实例，无法默认带出则需手动匹配，若无数据源实例，则需进入生产环境的“实时开发-数据源管理”页面，新建数据源实例`
              },
              {
                title: '功能引导',
                element: document.querySelector(`.data-source-preview`),
                intro:
                  `这里展示了本次转环境的对比内容`
              }
            ]
          })
        } else {
          introInstance = intro().setOptions({
            showButtons: true,
            doneLabel: '我知道了',
            showBullets: false,
            showProgress: false,
            showStepNumbers: false,
            steps: [
              {
                title: '功能引导',
                element: document.querySelector(`.data-source-preview`),
                intro:
                  `这里展示了本次转环境的对比内容`
              }
            ]
          })
        }
        introInstance.start()
        introInstance.onexit(() => {
          localStorage.setItem(guideKey, '1')
        })
      },
      init () {
        if (this.fileData) {
          this.projectId = this.fileData.projectId
          this.fileId = this.fileData.id
          this.fileType = this.fileData.fileType
          this.tableData = []
          this.compareVersions = []
          this.loading = false
          this.isLoading = false
          setTimeout(() => {
            this.showFeatureGuide()
          }, 500)
        }
      },
      async getCompareData () {
        this.loading = true
        let res = await this.$http.post('/dataSourceMappingRel/compare', {
          id: this.fileId
        }, {
          headers: {
            projectId: this.projectId
          }
        })
        this.loading = false
        if (res.code === 0) {
          this.tableData = res.data.sdpDataSourceMappingRels
          this.compareVersions = res.data.compareVersions
          this.envTableVisible = true
        }
      },

      closeDrawer () {
        this.envTableVisible = false
        this.$emit('close')
      },
      // 转环境
      async handleSubmit (flag) {
        const isCompare = this.tableData.every(item => {
          return item.prodDataSourceId
        })
        if (!isCompare) {
          this.$message.warning('请选择生产环境数据源实例')
          return
        }
        // 验证表名是否存在
        const hasProdTable = this.tableData.every(item => {
          return item.hasProdTable
        })
        if (!hasProdTable) {
          this.$message.warning('生产环境表名不存在')
          return
        }
        this.isLoading = true
        let res = await this.$http.post('/dataSourceMappingRel/changeEnv', {
          fileId: this.fileId,
          sdpDataSourceMappingRels: this.tableData
        }, {
          headers: {
            projectId: this.projectId
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          this.$message.success('转生产环境成功')
          if (flag) {
            const prodEnv = 'prod'
            this.$store.dispatch('global/setEnv', prodEnv)
            sessionStorage.setItem('env', prodEnv)
            localStorage.removeItem('openFiles')
            window.__intercept__ = 0
            window.location.reload()
          } else {
            this.closeDrawer()
          }
        } else {
          this.$message.error(res.msg);
        }
      }
    },
  }
</script>
<style lang="scss" scoped>
  .change-prod-drawer {
    .mt-10 {
      margin-top: 10px;
    }
    .default-tip {
      margin: 10px 0;
      line-height: 26px;
      background: #fff7e6;
      border: 1px solid #ffcc88;
      padding: 0 8px;
      color: #ff9118;
      font-size: 12px;
      word-wrap: break-word;
      word-break: normal;
      i {
        margin-right: 6px;
      }
    }
    .form-title {
      font-size: 14px;
      color: #333;
      font-weight: 600;
      margin-bottom: 12px;
      .line {
        display: block;
        width: 4px;
        height: 14px;
        background: #006eff;
        margin-right: 8px;
      }
      .tip {
        margin-left: 12px;
        font-size: 12px;
        color: #ff5555;
        .chitutreeicon_warning_tips {
          font-size: 16px !important;
        }
      }
    }
  }
</style>