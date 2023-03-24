<!--
 * @Author: hjg
 * @Date: 2021-11-08 19:05:03
 * @LastEditTime: 2022-07-05 16:11:45
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\drawer.vue
-->
<template>
  <a-drawer :closable="false"
            :mask="false"
            class="job-drawer"
            :class="{'is-max':isMax}"
            :visible="drawerVisible">
    <!-- 头部信息 -->
    <div class="title">
      <!-- <i class="chitutree-h5 chitutreezuoyeyunwei"></i> -->
      <a-icon v-if="!isMax"
              class="close-drawer"
              type="double-left"
              @click="isMax=!isMax" />
      <a-icon v-if="isMax"
              class="close-drawer"
              type="double-right"
              @click="isMax=!isMax" />
      <span>作业运维</span>
      <div class="job-status"
           v-if="$store.getters.jobInfo && $store.getters.jobInfo.jobStatus"
           :class="$store.getters.jobInfo.jobStatus">
        {{ $store.getters.jobInfo.jobStatus | filterStatus}}
      </div>
      <i class="chitutree-h5 chitutreeguanbi remove"
         @click="closeDrawer"></i>
    </div>
    <!-- tab切换 -->
    <div class="tab">
      <div :class="{'tab-checked':isTableActive === 'runLog'}"
           @click="tabChangeEvent('runLog')">运行日志</div>
      <div :class="{'tab-checked':isTableActive === 'runResult'}"
           @click="tabChangeEvent('runResult')">运行结果</div>
      <div :class="{'tab-checked':isTableActive === 'configInfo'}"
           @click="tabChangeEvent('configInfo')">配置信息</div>
      <div :class="{'tab-checked':isTableActive === 'alarmConfig'}"
           @click="tabChangeEvent('alarmConfig')">告警配置</div>
      <div :class="{'tab-checked':isTableActive === 'operateLog'}"
           @click="tabChangeEvent('operateLog')">操作日志</div>
    </div>
    <!-- 内容 -->
    <div class="content">
      <component :is="isTableActive"
                 :active-name="isTableActive"
                 :key="refreshTime" />
    </div>
  </a-drawer>
</template>
<script>
  import configInfo from './configInfo/index.vue'
  import alarmConfig from './alarmConfig/index.vue'
  import runLog from './runLog/index.vue'
  import operateLog from './operateLog/index.vue'
  import runResult from './runResult/index.vue'
  export default {
    props: {
      drawerVisible: {
        type: Boolean,
        default: false
      }
    },
    components: {
      configInfo,
      alarmConfig,
      runLog,
      operateLog,
      runResult
    },
    filters: {
      filterStatus (status) { // 当前状态过滤映射
        let text = null
        if (status === 'INITIALIZE') {
          text = '初始状态'
        } else if (status === 'RUNNING') {
          text = '运行中'
        } else if (status === 'PAUSED') {
          text = '暂停状态'
        } else if (status === 'SFAILED') {
          text = '启动失败'
        } else if (status === 'RFAILED') {
          text = '恢复失败'
        } else if (status === 'FINISHED') {
          text = '成功'
        } else if (status === 'TERMINATED') {
          text = '停止状态'
        }
        return text
      }
    },
    watch: {
      '$store.getters.jobInfo': {
        handler () {
          this.refreshTime = new Date().getTime()
          console.log('isTableActive', this.isTableActive)
        },
        deep: true
      }
    },
    data () {
      return {
        isMax: false,
        refreshTime: null,
        isTableActive: 'runLog'
      }
    },
    methods: {
      // tab选中
      tabChangeEvent (type) {
        this.isTableActive = type
      },
      // 关闭抽屉
      closeDrawer () {
        this.$emit('closeDrawer')
      }
    }
  }
</script>
<style lang="scss" scoped>
  .job-drawer {
    top: 72px;
    width: 905px !important;
    height: calc(100% - 72px);
    &.is-max {
      width: calc(100vw - 178px) !important;
    }
    /deep/ .ant-drawer-content-wrapper {
      width: 100% !important;
      .ant-drawer-body {
        padding: 0;
        height: 100%;
        .title {
          height: 40px;
          line-height: 40px;
          padding-left: 16px;
          border-bottom: 1px solid #d9d9d9;
          font-size: 16px;
          display: flex;
          align-items: center;
          position: relative;
          .close-drawer {
            cursor: pointer;
            color: #2c2f37;
          }
          .remove {
            position: absolute;
            right: 0;
            cursor: pointer;
          }
          i {
            margin-right: 8px;
          }
          span {
            font-weight: 600;
          }
          div {
            margin-left: 8px;
            width: 72px;
            height: 20px;
            line-height: 20px;
            color: #fff;
            text-align: center;
          }
          .job-status {
            font-size: 12px;
          }
          .INITIALIZE {
            background: #efe3ff;
            border-radius: 2px;
            color: #2c2f37;
          }
          .TERMINATED {
            background-color: #ddd;
            color: #93a1bb;
            border-radius: 2px;
          }
          .RUNNING {
            background-color: #dfecff;
            color: #2c2f37;
            border-radius: 2px;
          }
          .PAUSED {
            background-color: #eef0f4;
            color: #2c2f37;
            border-radius: 2px;
          }
          .SFAILED {
            background: #ffe0e0;
            color: #f95353;
            border-radius: 2px;
          }
          .RFAILED {
            background-color: #fff4e7;
            color: #ff9118;
            border-radius: 2px;
          }
          .FINISHED {
            background-color: #e2f6de;
            color: #33cc22;
            border-radius: 2px;
          }
        }
        .tab {
          height: 40px;
          background-color: #f9f9f9;
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
          height: calc(100% - 40px - 40px);
          padding: 12px 16px;
        }
      }
    }
  }
</style>
