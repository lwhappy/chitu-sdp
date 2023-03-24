<!--
 * @Author: hjg
 * @Date: 2021-11-11 16:21:18
 * @LastEditTime: 2022-06-28 15:34:11
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\components\job-version\index.vue
-->
<template>
  <a-modal class="job-version"
           ref="jobVersion"
           v-model="visible"
           :mask-closable="false"
           title="作业版本"
           :footer="null">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <!-- tab切换 -->
    <div class="tab">
      <div :class="{'tab-checked':isTableActive === 'fileCompare'}"
           @click="tabChangeEvent('fileCompare')">{{ fileType }}对比</div>
      <div :class="{'tab-checked':isTableActive === 'flinkCompare'}"
           @click="tabChangeEvent('flinkCompare')">Flink配置对比</div>
      <div :class="{'tab-checked':isTableActive === 'sourceCompare'}"
           @click="tabChangeEvent('sourceCompare')">资源配置对比</div>
      <!-- <div :class="{'tab-checked':isTableActive === 'basicInfo'}"
           @click="tabChangeEvent('basicInfo')">基础信息</div> -->
    </div>
    <!-- 内容（编辑器） -->
    <div class="content"
         v-loading="isLoading">
      <!-- 标题 -->
      <div class="title">
        <div class="left"
             v-if="type === 'file'">当前编辑版本</div>
        <div class="left"
             v-if="type === 'job'">当前运行版本</div>
        <div class="right justify-between">
          <div>对比版本<span>(版本{{ fileVersion }})</span></div>
        </div>
      </div>
      <!-- 编辑器 -->
      <div class="container">
        <moncao-editor ref="moncaoEditor"
                       :editorData="editorData" />
      </div>
    </div>
    <!-- 页脚 -->
    <div class="footer justify-end">
      <!-- <a-button class="button-restyle button-confirm">
        <slot class="job-version-slot"></slot>
      </a-button> -->
      <a-button class="button-restyle button-cancel"
                @click="cancelEvent">取消</a-button>
      <slot class="job-version-slot"></slot>

    </div>
  </a-modal>
</template>
<script>
  import moncaoEditor from './moncaoEditor.vue'
  export default {
    components: { moncaoEditor },
    data () {
      return {
        isLoading: false,
        fileVersion: null,
        visible: false, // 不适用props原因:作业运维页面数据定时刷新，通道不支持。现象：本页面会卡顿式出现
        jobId: null,
        type: null,
        fileType: null,
        isTableActive: 'fileCompare',
        dataInfo: null,
        editorData: {
          language: 'sql',
          oldValue: null,
          newValue: null
        }
      }
    },
    methods: {
      // 打开弹框
      open (data) {
        this.jobId = data.jobId
        this.type = data.type
        this.fileType = data.fileType
        this.visible = true
        this.getCompareInfo()
        // console.log('---open---refs: ', this.$refs['moncaoEditor'])
      },
      // tab选中
      tabChangeEvent (type) {
        this.isTableActive = type
        let languague = 'sql'
        let oldValue = this.dataInfo[0].fileContent
        let newValue = this.dataInfo[1].fileContent
        if (type === 'fileCompare') {
          if (this.fileType === 'DS') {
            languague = 'json'
            oldValue = this.dataInfo[0].dataStreamConfig
            newValue = this.dataInfo[1].dataStreamConfig
          }
        } else if (type === 'flinkCompare') {
          languague = 'json'
          oldValue = this.dataInfo[0].configContent
          newValue = this.dataInfo[1].configContent
        } else if (type === 'sourceCompare') {
          languague = 'json'
          oldValue = this.dataInfo[0].sourceContent
          newValue = this.dataInfo[1].sourceContent
        }
        this.$refs['moncaoEditor'].setEditor(languague, oldValue, newValue)
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
          this.dataInfo = res.data
          this.fileVersion = res.data[1].fileVersion
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
      height: 50px;
      background-color: #f9f9f9;
      border-bottom: 1px solid #ddd;
      padding-left: 17px;
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #999;
      div {
        width: auto;
        line-height: 50px;
        height: 100%;
        cursor: pointer;
        margin-right: 30px;
        font-weight: 600;
      }
      .tab-checked {
        color: #333;
        border-bottom: 2px solid #006eff;
      }
    }
    .content {
      height: calc(100% - 50px - 54px);
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
    }
    .footer {
      border-top: 1px solid #ddd;
      height: 44px;
      line-height: 44px;
      padding-right: 16px;
      background-color: #fff;
      /deep/ .button-confirm {
        padding: 0;
      }
    }
  }
</style>
