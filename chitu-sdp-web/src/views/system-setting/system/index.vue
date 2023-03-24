<!--
 * @description: 系统设置/实时系统设置
 * @Author: lijianguo19
 * @Date: 2022-08-01 10:16:15
 * @FilePath: \src\views\system-setting\system\index.vue
-->

<template>
  <div class="system-operate"
       v-loading="isLoading">
    <div class="operate">
      <h3 class="title justify-start"><span class="line"></span>作业运维</h3>
      <div class="content row">
        <div class="justify-start">
          <p class="label">可执行状态</p>
          <a-checkbox-group v-model="state"
                            :options="operateStates"></a-checkbox-group>
          <p class="tip"><img src="@/assets/icons/warn-yellow.png">一般用于发版限制用户操作时使用
          </p>
        </div>
        <div class="justify-start">
          <p class="label">开启资源验证</p>
          <a-switch v-model="resourceValidate" />
          <p class="tip"><img src="@/assets/icons/warn-yellow.png">一般用于启动作业时校验资源是否充足
          </p>
        </div>
        <div class="justify-start">
          <p class="label">规则告警定时任务开关</p>
          <a-switch v-model="alertGlobalSwitch" />
          <p class="tip"><img src="@/assets/icons/warn-yellow.png">规则告警全局开启关闭
          </p>
        </div>
        <div class="justify-start">
          <p class="label">是否需要二级审批</p>
          <a-switch v-model="needTwoApprove" />
          <p class="tip"><img src="@/assets/icons/warn-yellow.png">如果选择OFF,只需要一级审批
          </p>
        </div>
      </div>
    </div>

    <div class="footer">
      <a-button style="margin-left:8px"
                @click="save"
                size="small"
                type="primary">保存</a-button>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'systemOperate',
    mixins: [],
    props: {

    },
    components: {

    },
    watch: {
      '$store.getters.isRemoveTag': {//监听关闭页签，关闭页签后清除缓存
        handler (val, oldVal) {
          if (val === oldVal) {
            return
          }
          if (this.$store.getters.removeRouteName.includes('SystemSettingSystem')) {
            this.$common.toClearCache(this);
          }
        }
      }
    },
    computed: {

    },
    data () {
      return {
        resourceValidate: false,
        alertGlobalSwitch: true,
        needTwoApprove: true,
        isLoading: false,
        state: [],
        operateStates: [{
          label: '启动/恢复',
          value: 'start'
        },
        {
          label: '停止/暂停',
          value: 'stop'
        }],
      }
    },
    created () { },
    mounted () {
      this.getData()
    },
    methods: {
      async getData () {
        this.isLoading = true
        let res = await this.$http.get('/sysconfig/getSysoper')
        this.isLoading = false
        if (res.code === 0) {
          this.resourceValidate = res.data.resourceValidate
          // 判断是否存在
          if (res.data.alertGlobalSwitch == undefined) {
            this.alertGlobalSwitch = true
          } else {
            this.alertGlobalSwitch = res.data.alertGlobalSwitch
          }
          if (res.data.needTwoApprove == undefined) {
            this.needTwoApprove = true
          } else {
            this.needTwoApprove = res.data.needTwoApprove
          }
          const start = res.data.operMaintenance.start
          const stop = res.data.operMaintenance.stop
          this.state = []
          if (start === 1) {
            this.state.push('start')
          }
          if (stop === 1) {
            this.state.push('stop')
          }
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      async save () {
        const operMaintenance = { start: 0, stop: 0 }
        if (this.state.includes('start')) {
          operMaintenance.start = 1
        }
        if (this.state.includes('stop')) {
          operMaintenance.stop = 1
        }
        const params = {
          operMaintenance: operMaintenance,
          resourceValidate: this.resourceValidate,
          alertGlobalSwitch: this.alertGlobalSwitch,
          needTwoApprove: this.needTwoApprove
        }
        let res = await this.$http.post('/sysconfig/saveSysoper', params)
        if (res.code === 0) {
          this.$message.success('保存成功')
        }
      },
    }
  }
</script>

<style lang="scss" scoped>
  .system-operate {
    color: #333;
    font-size: 12px;
    padding: 16px 56px 0;
    height: 100%;
    overflow: auto;
    .yellow {
      color: #ff9300;
      margin-right: 5px;
      font-size: 16px !important;
      margin-top: 2px;
    }
    /deep/ .ant-checkbox-wrapper {
      font-size: 12px;
    }
    // /deep/ .ant-checkbox-group {
    //   padding-left: 24px;
    //   .ant-checkbox-wrapper {
    //     margin-right: 24px;
    //   }
    // }
    .title {
      font-size: 14px;
      color: #333;
      font-weight: 600;
      .line {
        display: block;
        width: 4px;
        height: 14px;
        background: #006eff;
        margin-right: 8px;
      }
    }
    .content {
      // margin-left: 32px;
      align-items: flex-start;
      overflow: auto;
    }
    .operate {
      .title {
        margin-bottom: 16px;
      }
    }
    .white-list {
      .title {
        margin-top: 24px;
      }
      .content {
      }
    }
    .footer {
      .btns {
        height: 100%;
        width: 100%;
        margin-left: 32px;
        margin-top: 24px;
        .button-confirm {
          font-size: 12px;
          width: 72px;
          height: 27px;
        }
      }
    }
    .notice-tab {
      margin-top: 24px;
      .tab-self {
        height: 40px;
        color: #999;
        font-size: 14px;
        border-bottom: solid 1px #ddd;
        box-sizing: content-box;
        span {
          display: inline-block;
          width: 70px;
          height: 100%;
          cursor: pointer;
          text-align: center;
          margin-right: 30px;
          line-height: 44px;
        }
        .span-checked {
          color: #333;
          border-bottom: 2px solid #006eff;
        }
      }
      .tab-content {
        margin-top: 24px;
      }
    }
    .dataSourceWriteContrls {
      margin-top: 24px;
    }
    .row {
      margin-top: 16px;
      .label {
        width: 130px;
        text-align: right;
        margin-right: 8px;
        height: 28px;
        line-height: 28px;
      }
      .tip {
        margin-left: 10px;
        color: #ff9118;
        img {
          width: 13px;
          height: 13px;
          margin-right: 2px;
        }
      }
      .value {
        flex: 1;
        .textarea {
          width: 100%;
          height: 80px;
          max-height: 300px;
        }
      }
    }
    .footer {
      padding: 16px 0 16px 78px;
    }
  }
</style>
