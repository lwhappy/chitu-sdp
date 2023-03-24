<template>
  <div class="compare-version-container">
    <!-- tab切换 -->
    <div class="tab"
         ref="tab">
      <div :class="{'tab-checked':isTableActive === 'fileCompare'}"
           @click="tabChangeEvent('fileCompare',true)">{{ fileType }}对比</div>
      <div v-if="!isFirst"
           :class="{'tab-checked':isTableActive === 'flinkCompare'}"
           @click="tabChangeEvent('flinkCompare',true)">高级配置对比</div>
      <div v-if="!isFirst"
           :class="{'tab-checked':isTableActive === 'sourceCompare'}"
           @click="tabChangeEvent('sourceCompare',true)">资源配置对比</div>
    </div>
    <!-- 内容（编辑器） -->
    <div class="content">
      <!-- 标题 -->
      <div class="title"
           v-if="diffFlag">
        <div class="left">UAT环境</div>
        <div class="right"
             v-if="compareInfo.length > 1">
          <div>生产环境</div>
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

  </div>
</template>
<script>
  import _ from 'lodash'
  import moncaoEditor from './moncaoEditor.vue'
  import monaco from './moncao.vue'
  export default {
    components: { moncaoEditor, monaco },
    props: {
      dataInfo: {
        type: Array,
        default: () => []
      },
      fileType: {
        type: String,
        default: ''
      }
    },
    watch: {
    },
    computed: {
      isFirst () {
        return this.dataInfo.length > 1
      }
    },
    data () {
      return {
        compareInfo: [],
        isTableActive: 'fileCompare',
        editorData: {
          language: 'sql',
          oldValue: null,
          newValue: null
        },
        diffFlag: true,
        detailData: {},
      }
    },
    mounted () {
      this.init()
    },
    methods: {
      init () {
        this.compareInfo = _.cloneDeep(this.dataInfo)
        if (this.compareInfo.length > 1) {
          this.diffFlag = true
        } else {
          this.diffFlag = false
          this.compareInfo.push({})
        }
        this.tabChangeEvent('fileCompare')
      },
      // tab选中
      tabChangeEvent (type, isScroll) {
        if (isScroll) {
          this.$refs['tab'].scrollIntoView()
        }
        this.isTableActive = type
        let languague = 'sql'
        let oldValue = this.compareInfo[0].fileContent || ''
        let newValue = this.compareInfo[1].fileContent || ''
        if (type === 'fileCompare') {
          if (this.fileType === 'DS') {
            languague = 'json'
            oldValue = this.compareInfo[0].dataStreamConfig
            newValue = this.compareInfo[1].dataStreamConfig || ''
          }
        } else if (type === 'flinkCompare') {
          languague = 'json'
          oldValue = this.compareInfo[0].configContent
          newValue = this.compareInfo[1].configContent || ''
        } else if (type === 'sourceCompare') {
          languague = 'json'
          oldValue = this.compareInfo[0].sourceContent
          newValue = this.compareInfo[1].sourceContent || ''
        }
        this.$nextTick(() => {
          if (this.diffFlag) {
            this.$refs['moncaoEditor'].setEditor(languague, oldValue, newValue)
          } else {
            this.$refs['moncaoAngle'].setEditor(languague, oldValue)
          }
        })
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
  .compare-version-container {
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
      height: 290px;
      padding: 0 10px 10px 10px;
      .title {
        width: 100%;
        display: flex;
        color: #333;
        font-weight: 900;
        font-size: 12px;
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
