<!--
 * @Author: hjg
 * @Date: 2021-11-09 10:20:04
 * @LastEditTime: 2022-07-19 15:55:29
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\alarmConfig\components\alarmDrawer.vue
-->
<template>
  <div class="alarm-drawer"
       title="告警配置">
    <!-- 内容 -->
    <div class="content">
      <a-breadcrumb separator="">
        <a-breadcrumb-item href=""
                           @click.native="goback">{{activeName=='alarmEvent'?'告警事件':'告警规则'}}</a-breadcrumb-item>
        <a-breadcrumb-separator>
          <a-icon type="right"
                  style="font-size:12px" />
        </a-breadcrumb-separator>
        <a-breadcrumb-item>
          {{type=='addAlarm'?'新增':'编辑'}}告警配置
        </a-breadcrumb-item>
      </a-breadcrumb>
      <!-- 详细规则 -->
      <div class="rules-detail">
        <component ref="rules"
                   :jobInfo="jobInfo"
                   :editRuleInfo="editRuleInfo"
                   :projectUsers="projectUsers"
                   :params="params"
                   :is="type" />
      </div>
      <!-- 页脚 -->
      <div class="footer">
        <a-button @click="cancelEvent"
                  size="small">取消</a-button>
        <a-button style="margin-left:8px"
                  @click="confirmEvent"
                  size="small"
                  type="primary">确定</a-button>
      </div>
    </div>
  </div>
</template>
<script>
  import editAlarm from './editAlarm.vue'
  import addAlarm from './addAlarm.vue'
  export default {
    name: 'alarmDrawer',
    props: {
      activeName: {
        type: String,
        default: 'alarmEvent'
      },
      drawerConfigVisible: {
        type: Boolean,
        default: false
      },
      type: {
        type: String,
        default: 'addAlarm'
      },
      jobInfo: {
        type: Object,
        default: () => {
          return {}
        }
      },
      editRuleInfo: {
        type: Object,
        default: () => {
          return {}
        }
      },
      projectUsers: {
        type: Array,
        default: () => {
          return []
        }
      }
    },
    components: {
      addAlarm,
      editAlarm
    },
    data () {
      return {
        params: {
          alertRate: null, // 告警频率
          effectiveState: 'STOP', // 生效状态:STOP/START
          effectiveTime: null, // 生效时间
          indexName: null, // 指标:NUMBER_RESTARTS/NUMBER_CHECKPOINT/DELAY
          jobId: null, // 作业ID
          notifiType: null, // 通知方式:CROSS_SOUND/SHORT_MESSAGE/TELEPHONE, 多个逗号分开
          notifyUsers: [], // 通知用户
          ruleContent: { // 规则内容
            operator: null, // 运算符
            threshold: null, // 阈值
            timeDiff: null, // 时间差
          },
          ruleDesc: null, // 规则描述
          ruleGenerateType: 'CUSTOMIZ', // 规则生成类型:SYSTEM_AUTOMATIC/CUSTOMIZE
          ruleName: null // 规则名称
        },
      }
    },
    methods: {
      // 关闭抽屉
      onClose (type) {
        this.$emit('closeAlarmDrawer', type)
      },
      // 取消
      cancelEvent () {
        this.onClose(this.type)
      },
      // 确认
      confirmEvent () {
        let formData = this.$refs['rules'].$refs['form'].formData
        let type = this.$refs['rules'].$refs['form'].type
        let res = this.formDataReg(formData)
        // // console.log('---------res: ', res)
        if (!res.flag) {
          return this.$message.error({ content: res.text, duration: 2 })
        } else {
          if (type === 'addAlarm') {
            let params = this.formatParams(formData, JSON.parse(JSON.stringify(this.params)))
            this.addRule(params, type)
          } else {
            let params = this.formatParams(formData, JSON.parse(JSON.stringify(this.$store.getters.ruleInfo)))
            this.editRule(params, type)
          }
        }
      },
      // formData校验
      formDataReg (formData) {
        console.log('formData: ', formData)
        let result = {
          flag: true,
          text: null
        }
        if (formData.ruleName === null || formData.ruleName === '') {
          result.flag = false
          result.text = '规则名称不能为空'
          return result
        }
        if (formData.ruleDesc === null || formData.ruleDesc === '') {
          result.flag = false
          result.text = '规则描述不能为空'
          return result
        }
        if (formData.indexName !== 'DELAY' && formData.indexName !== 'WAIT_RECORDS' && (formData.ruleContent.timeDiff === null || formData.ruleContent.timeDiff === '')) {
          result.flag = false
          result.text = '规则内容时间差不能为空'
          return result
        }
        if (formData.indexName !== 'INTERRUPT_OPERATION' && formData.indexName !== 'BACKPRESSURED' && (formData.ruleContent.threshold === null || formData.ruleContent.threshold === '')) {
          result.flag = false
          result.text = '规则内容阈值不能为空'
          return result
        }
        if (formData.firstTime === null || formData.firstTime === '' || formData.secondTime === null || formData.secondTime === '') {
          result.flag = false
          result.text = '规则生效时间不能为空'
          return result
        }
        if (formData.indexName !== 'INTERRUPT_OPERATION' && (formData.alertRate === null || formData.alertRate === '')) {
          result.flag = false
          result.text = '规则告警频率不能为空'
          return result
        }
        // if (formData.notifiType.length === 0) {
        //   result.flag = false
        //   result.text = '未选择告警通知方式'
        //   return result
        // }
        if (formData.notifyUsers.length === 0) {
          result.flag = false
          result.text = '未指定告警联系人'
          return result
        }
        return result
      },
      // 添加规则
      async addRule (params, type) {
        params.notifyUsers = JSON.stringify(params.notifyUsers)
        let res = await this.$http.post('/alert/rule/add', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.onClose(type)
          this.$message.success({ content: '添加成功', duration: 2 })
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
        return res
      },
      // 修改规则
      async editRule (params, type) {
        params.notifyUsers = JSON.stringify(params.notifyUsers)
        let res = await this.$http.post('/alert/rule/update', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.onClose(type)
          this.$message.success({ content: '修改成功', duration: 2 })
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
        return res
      },
      // 格式化参数
      formatParams (formData, params) {
        // // console.log('------formData:', formData)
        // // console.log('------params:', params)
        for (let key in params) {
          if (key === 'effectiveTime') {
            params[key] = formData.firstTime + '-' + formData.secondTime
          } else if (key === 'jobId') {
            params[key] = this.$store.getters.jobInfo.id
          } else if (key === 'notifyUsers') {
            params[key] = []
            if (formData[key].length > 0) {
              formData[key].forEach(item => {
                let userArr = item.split(',')
                params[key].push({
                  employee_number: userArr[0],
                  employee_name: userArr[1]
                })
              })
            }
          } else if (key === 'notifiType') {
            params[key] = formData[key].join(',')
          } else {
            if (formData[key]) params[key] = formData[key]
          }
        }
        if (params.indexName === 'DELAY') {
          params.ruleContent.timeDiff = null
        }
        // // console.log('-self-params:', params)
        // // console.log('-this-params:', this.params)
        return params
      },
      goback () {
        this.$emit('closeAlarmDrawer', this.type)
      }
    }
  }
</script>
<style lang="scss" scoped>
  .alarm-drawer {
    /deep/ .ant-breadcrumb {
      font-size: 12px;
      margin-bottom: 15px;
    }
    /deep/ .ant-drawer-content-wrapper {
      width: 560px !important;
      .ant-drawer-header {
        height: 50px;
        padding: 16px;
        .ant-drawer-title {
          color: #333;
          font-weight: 700;
        }
        .ant-drawer-close {
          width: 54px;
          height: 54px;
        }
      }
      .ant-drawer-body {
        height: calc(100% - 54px);
        padding: 12px 16px 0 16px;
        .content {
          height: 100%;
          .rules-detail {
            height: calc(100% - 44px);
          }
          .footer {
            height: 44px;
            display: flex;
            justify-content: flex-end;
          }
        }
      }
    }
  }
</style>
