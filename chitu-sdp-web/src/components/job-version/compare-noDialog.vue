<!--
 * @Author: hjg
 * @Date: 2021-11-11 16:21:18
 * @LastEditTime: 2022-06-23 16:53:05
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\components\job-version\compare-noDialog.vue
-->
<template>
  <div class="job-version">

    <!-- tab切换 -->
    <div class="tab">
      <div :class="{'tab-checked':isTableActive === 'fileCompare'}"
           @click="tabChangeEvent('fileCompare')">{{ fileType }}对比</div>
      <div :class="{'tab-checked':isTableActive === 'flinkCompare'}"
           @click="tabChangeEvent('flinkCompare')">Flink配置对比</div>
      <div :class="{'tab-checked':isTableActive === 'sourceCompare'}"
           @click="tabChangeEvent('sourceCompare')">资源配置对比</div>
      <div :class="{'tab-checked':isTableActive === 'basicInformation'}"
           @click="tabChangeEvent('basicInformation')">基本信息</div>
    </div>
    <!-- 内容（编辑器） -->
    <div class="content"
         v-if="isTableActive !== 'basicInformation'"
         v-loading="isLoading">
      <!-- 标题 -->
      <div class="title"
           v-if="diffFlag">
        <div class="left">当前版本</div>
        <div class="right justify-between"
             v-if="dataInfo.length > 1">
          <div>对比版本<span>(版本{{ fileVersion }})</span></div>
        </div>
      </div>
      <!-- 编辑器 -->
      <div class="container">
        <moncao-editor v-show="diffFlag"
                       ref="moncaoEditor"
                       :editorData="editorData" />
        <monaco v-show="!diffFlag"
                ref="moncaoAngle"
                :editorData="editorData" />
      </div>
    </div>
    <!-- 基本信息 -->
    <div class="content"
         v-else>
      <base-information ref="basicInformation"
                        type="approve"></base-information>
    </div>
  </div>
</template>
<script>
  import moncaoEditor from './moncaoEditor.vue'
  import monaco from './moncao.vue'
  import baseInformation from '@/views/application/job-develop/components/basic-information.vue'
  export default {
    components: { moncaoEditor, monaco, baseInformation },
    props: {},
    data () {
      return {
        isLoading: false,
        fileVersion: null,
        jobId: null,
        type: null,
        fileType: null,
        isTableActive: 'fileCompare',
        dataInfo: [],
        editorData: {
          language: 'sql',
          oldValue: null,
          newValue: null
        },
        diffFlag: true,
        detailData: {},
        isDetail: false,
      }
    },
    methods: {
      // 打开弹框
      open (data) {
        this.jobId = data.jobId
        this.type = data.type
        this.fileType = data.fileType
        this.getCompareInfo()
        // console.log('---open---refs: ', this.$refs['moncaoEditor'])
      },
      // tab选中
      tabChangeEvent (type) {
        this.isTableActive = type
        let languague = 'sql'
        let oldValue = this.dataInfo[0].fileContent || ''
        let newValue = this.dataInfo[1].fileContent || ''
        if (type === 'fileCompare') {
          if (this.fileType === 'DS') {
            languague = 'json'
            oldValue = this.dataInfo[0].dataStreamConfig
            newValue = this.dataInfo[1].dataStreamConfig || ''
          }
        } else if (type === 'flinkCompare') {
          languague = 'json'
          oldValue = this.dataInfo[0].configContent
          newValue = this.dataInfo[1].configContent || ''
        } else if (type === 'sourceCompare') {
          languague = 'json'
          oldValue = this.dataInfo[0].sourceContent
          newValue = this.dataInfo[1].sourceContent || ''
        } else if (type === 'basicInformation') {
          this.$nextTick(async () => {
            this.$refs['basicInformation'].getDetail(this.jobId)
          })
        }
        if (type !== 'basicInformation') {
          this.$nextTick(() => {
            if (this.diffFlag) {
              this.$refs['moncaoEditor'].setEditor(languague, oldValue, newValue)
            } else {
              this.$refs['moncaoAngle'].setEditor(languague, oldValue)
            }
          })
        }


      },
      // 获取对比信息
      async getCompareInfo () {
        let params = {
          id: this.jobId,
          compareSource: this.type
        }
        this.isLoading = true
        let res = await this.$http.post('/file/version/compare', params)
        this.isLoading = false
        if (res.code === 0) {
          this.$forceUpdate()
          this.dataInfo = res.data
          if (res.data.length > 1) {
            this.fileVersion = res.data[1].fileVersion
            this.diffFlag = true
          } else {
            this.diffFlag = false
            this.dataInfo.push({})
          }
          this.tabChangeEvent('fileCompare')
        }
      },
      // 取消操作
      cancelEvent () {
        this.visible = false
      }
    },
    beforeDestroyed () {
      this.$refs['moncaoEditor'].clearEditor()
    }
  }
</script>
<style lang="scss" scoped>
  .job-version {
    border: solid 1px #ddd;
    width: 100%;
    z-index: 999;
    /deep/ .ant-modal {
      width: 70% !important;
      height: 80% !important;
      top: 10%;
    }
    /deep/ .ant-modal-content {
      border-radius: 0;
      height: 100%;
    }
    /deep/ .ant-modal-header {
      padding: 11px 16px;
      border-radius: 0;
      .ant-modal-title {
        font-weight: 700;
      }
    }
    /deep/ .ant-modal-close-x {
      width: 44px;
      height: 44px;
      line-height: 44px;
    }
    /deep/ .ant-modal-body {
      padding: 0;
      height: calc(100% - 45px);
    }
    .tab {
      height: 40px;
      // background-color: #f9f9f9;
      border-bottom: 1px solid #ddd;
      padding-left: 17px;
      display: flex;
      align-items: center;
      font-size: 14px;
      color: #999;
      div {
        width: auto;
        line-height: 40px;
        height: 100%;
        cursor: pointer;
        margin-right: 30px;
        font-weight: 600;
      }
      .tab-checked {
        color: #333;
        border-bottom: 2px solid #0066ff;
      }
    }
    .content {
      height: 355px;
      padding: 0 10px 10px 10px;
      .title {
        width: 100%;
        display: flex;
        color: #333;
        font-size: 14px;
        height: 30px;
        align-items: center;
        .left,
        .right {
          width: 50%;
        }
        span {
          color: #ccc;
        }
      }
      .container {
        width: 100%;
        height: calc(100% - 30px);
      }
      /deep/ .basic-information {
        height: 100%;
      }
    }
    .footer {
      border-top: 1px solid #ddd;
      height: 44px;
      line-height: 44px;
      padding-right: 16px;
      /deep/ .button-confirm {
        padding: 0;
      }
    }
  }
</style>
