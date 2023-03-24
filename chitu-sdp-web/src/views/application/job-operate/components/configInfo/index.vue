<!--
 * @Author: hjg
 * @Date: 2021-11-08 21:28:19
 * @LastEditTime: 2022-06-27 15:01:19
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\configInfo\index.vue
-->
<template>
  <div class="config-info">
    <!-- 上层切换和新增告警配置 -->
    <div class="top">
      <div :class="{'tab-checked':isTableActive === 'jobConfig'}"
           @click="tabChangeEvent('jobConfig')">作业配置</div>
      <div v-show="fileType === 'SQL'"
           :class="{'tab-checked':isTableActive === 'sqlConfig'}"
           @click="tabChangeEvent('sqlConfig')">SQL配置</div>
      <div v-show="fileType === 'DS'"
           :class="{'tab-checked':isTableActive === 'dsConfig'}"
           @click="tabChangeEvent('dsConfig')">DS配置</div>
      <div :class="{'tab-checked':isTableActive === 'resourceConfig'}"
           @click="tabChangeEvent('resourceConfig')">资源配置</div>
      <div :class="{'tab-checked':isTableActive === 'flinkConfig'}"
           @click="tabChangeEvent('flinkConfig')">Flink配置</div>
      <div v-show="fileType === 'SQL'"
           :class="{'tab-checked':isTableActive === 'jarConfig'}"
           @click="tabChangeEvent('jarConfig')"> UDX配置</div>
      <div :class="{'tab-checked':isTableActive === 'savePointConfig'}"
           @click="tabChangeEvent('savePointConfig')">保存点配置</div>
      <div v-show="$store.getters.env !== 'prod'"
           :class="{'tab-checked':isTableActive === 'runConfig'}"
           @click="tabChangeEvent('runConfig')">运行配置</div>
    </div>
    <!-- 内容 -->
    <div class="config-info-content">
      <component :is="isTableActive"
                 :active-name="isTableActive" />
    </div>
  </div>
</template>
<script>
  import jobConfig from './components/jobConfig.vue'
  import sqlConfig from './components/sqlConfig.vue'
  import resourceConfig from './components/resourceConfig.vue'
  import flinkConfig from './components/flinkConfig.vue'
  import jarConfig from './components/jarConfig.vue'
  import dsConfig from './components/dsConfig.vue'
  import savePointConfig from './components/save-point.vue'
  import runConfig from './components/runConfig.vue'

  export default {
    components: {
      jobConfig,
      sqlConfig,
      dsConfig,
      resourceConfig,
      flinkConfig,
      jarConfig,
      savePointConfig,
      runConfig
    },
    data () {
      return {
        isTableActive: 'jobConfig',
        fileType: this.$store.getters.jobInfo.fileType
      }
    },
    methods: {
      // tab选中
      tabChangeEvent (type) {
        this.isTableActive = type
        this.$store.dispatch('job/setConfigInfoActive', type)
      },
    },
    created () {
      let isTableActive = this.$store.getters.configInfoActive
      if (this.fileType === 'SQL') {
        if (isTableActive === 'dsConfig') {//sql类型没有dsConfig
          isTableActive = 'jobConfig'
        }
      } else if (this.fileType === 'DS') {
        if (isTableActive === 'sqlConfig' || isTableActive === 'jarConfig') {//DS类型没有sqlConfig和jarConfig
          isTableActive = 'jobConfig'
        }
      }
      this.isTableActive = isTableActive || 'jobConfig'
      this.$store.dispatch('job/setConfigInfoActive', this.isTableActive)
    }
  }
</script>
<style lang="scss" scoped>
  .config-info {
    width: 100%;
    height: 100%;
    .top {
      width: 100%;

      display: flex;
      > :first-child {
        border-left: 1px solid #ddd;
        border-radius: 4px 0px 0px 4px;
      }
      > :last-child {
        border-radius: 0 4px 4px 0;
      }
      div {
        cursor: pointer;
        border: 1px solid #ddd;
        border-left: 0;
        width: 80px;
        padding: 4px 0;
        text-align: center;

        font-size: 12px;
        color: #999;
      }
      .tab-checked {
        background-color: #006eff;
        border: 1px solid #006eff;
        color: #fff;
        font-weight: 500;
      }
    }
    .config-info-content {
      padding-top: 12px;
      height: calc(100% - 24px - 24px);
    }
  }
</style>
