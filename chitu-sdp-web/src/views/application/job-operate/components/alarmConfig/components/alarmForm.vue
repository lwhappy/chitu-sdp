<!--
 * @Author: hjg
 * @Date: 2021-11-10 09:34:51
 * @LastEditTime: 2022-07-12 21:12:16
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\application\job-operate\components\alarmConfig\components\alarmForm.vue
-->
<template>
  <div ref="alarmForm"
       class="alarm-form">
    <!-- 具体规则 -->
    <div class="form-title">具体规则</div>
    <!-- 规则名称 -->
    <div class="form-item">
      <span class="label">
        <span class="flag">*</span>规则名称
      </span>
      <a-input class="form-value"
               v-model="formData.ruleName"
               allow-clear
               placeholder="请输入名称"></a-input>
    </div>
    <!-- 描述 -->
    <div class="form-item">
      <span class="label">
        <span class="flag">*</span>描述
      </span>
      <a-input class="form-value"
               v-model="formData.ruleDesc"
               allow-clear
               placeholder="请输入描述"></a-input>
    </div>
    <!-- 内容 -->
    <div class="description">
      <span class="target">指标</span>
      <span class="time"
            v-show="formData.indexName !== 'DELAY' && formData.indexName !== 'INTERRUPT_OPERATION' && formData.indexName!=='WAIT_RECORDS' ">时间差(m)</span>
      <span class="operator"
            v-show="formData.indexName !== 'INTERRUPT_OPERATION' && formData.indexName !=='BACKPRESSURED'">运算符</span>
      <span class="threshold"
            v-show="formData.indexName !== 'INTERRUPT_OPERATION' &&  formData.indexName!=='BACKPRESSURED'">阈值</span>
    </div>
    <div class="form-item">
      <span class="label">
        <span class="flag">*</span>内容
      </span>
      <!-- 指标 -->
      <a-select v-model="formData.indexName"
                class="target">
        <a-select-option :value="item.value"
                         v-for="(item, index) in targets"
                         :key="index">
          {{ item.label }}
        </a-select-option>
      </a-select>
      <!-- 时间差(m) -->
      <a-input-number v-model="formData.ruleContent.timeDiff"
                      class="time"
                      v-show="formData.indexName !== 'DELAY' && formData.indexName !== 'INTERRUPT_OPERATION' && formData.indexName!=='WAIT_RECORDS' "
                      :min="1"></a-input-number>
      <!-- 运算符 -->
      <a-select v-model="formData.ruleContent.operator"
                v-show="formData.indexName !== 'INTERRUPT_OPERATION' && formData.indexName !=='BACKPRESSURED'"
                class="operator">
        <a-select-option :value="item.value"
                         v-for="(item, index) in operators"
                         :key="index">
          {{ item.label }}
        </a-select-option>
      </a-select>
      <!-- 阈值 -->
      <a-input v-model="formData.ruleContent.threshold"
               v-show="formData.indexName !== 'INTERRUPT_OPERATION' && formData.indexName!=='BACKPRESSURED'"
               class="threshold"
               @keyup.native="number($event)"
               placeholder="阈值"></a-input>
    </div>
    <!-- 生效时间 -->
    <div class="form-item">
      <span class="label">
        <span class="flag">*</span>生效时间
      </span>
      <a-time-picker v-model="formData.firstTime"
                     class="time-picker"
                     value-format="HH:mm"
                     format="HH:mm"
                     placeholder="时间"></a-time-picker>
      -
      <a-time-picker v-model="formData.secondTime"
                     class="time-picker"
                     value-format="HH:mm"
                     format="HH:mm"
                     placeholder="时间"></a-time-picker>
    </div>
    <!-- 告警频率 -->
    <div class="form-item">
      <span class="label">
        <span class="flag">*</span>告警频率
      </span>
      <a-input-number v-model="formData.alertRate"
                      class="rate"
                      :min="1"></a-input-number>
    </div>
    <div class="rate-description">连续N分钟内只发一次警告</div>
    <!-- 通知方式 -->
    <div class="form-title">通知方式</div>
    <div class="form-item">
      <span class="label">
        通知方式
        <!-- <span class="flag">*</span> -->
      </span>
      <a-checkbox-group v-model="formData.notifiType"
                        :options="noticeMethods"></a-checkbox-group>
    </div>
    <!-- 联系人 -->
    <div class="form-item">
      <span class="label">
        <span class="flag">*</span>联系人
      </span>
      <a-select class="auto-complete"
                mode="multiple"
                placeholder="请输入姓名/工号"
                v-model="formData.notifyUsers"
                :auto-clear-search-value="false"
                option-label-prop="label">
        <a-select-option v-for="(item, index) in dataSource"
                         :value="item.employeeNumber + ',' + item.userName"
                         :key="'employee-' + index"
                         :label="item.userName">
          {{ item.employeeNumber }}, {{ item.userName }}
        </a-select-option>
      </a-select>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'alarmForm',
    props: {
      params: {
        type: Object,
        default: () => {
          return {}
        }
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
      projectUsers: {
        type: Array,
        default: () => {
          return []
        }
      }
    },
    data () {
      return {
        formData: {
          alertRate: 5, // 告警频率
          effectiveState: 'STOP',
          indexName: 'NUMBER_RESTARTS', // 指标:NUMBER_RESTARTS/NUMBER_CHECKPOINT/DELAY
          notifiType: [], // 通知方式:CROSS_SOUND/SHORT_MESSAGE/TELEPHONE, 多个逗号分开
          notifyUsers: [], // 通知用户
          firstTime: '00:00',
          secondTime: '23:59',
          ruleContent: { // 规则内容
            operator: 'GREATER_THAN_EQUAL', // 运算符
            threshold: null, // 阈值
            timeDiff: 1, // 时间差
          },
          ruleDesc: null, // 规则描述
          ruleGenerateType: 'CUSTOMIZE', // 规则生成类型:SYSTEM_AUTOMATIC/CUSTOMIZE
          ruleName: null // 规则名称
        },
        targets: [{
          label: '重启次数',
          value: 'NUMBER_RESTARTS'
        }, {
          label: 'checkpoint次数',
          value: 'NUMBER_CHECKPOINT'
        }, {
          label: '延迟',
          value: 'DELAY'
        }, {
          label: '作业运行中断',
          value: 'INTERRUPT_OPERATION'
        }, {
          label: 'topic数据解析失败',
          value: 'KAFKA_FAIL_MSG'
        }, {
          label: '待消费数',
          value: 'WAIT_RECORDS'
        }, {
          label: '背压',
          value: 'BACKPRESSURED'
        }, {
          label: '最近N小时消费数',
          value: 'CONSUME_RECORDS'
        }],
        operators: [{
          label: '>=',
          value: 'GREATER_THAN_EQUAL'
        }, {
          label: '<=',
          value: 'LESS_THAN_EQUAL'
        }],
        noticeMethods: [{
          label: '邮件',
          value: 'EMAIL'
        }],
        dataSource: []
      }
    },
    created () {
      this.initFormData()
    },
    methods: {
      number (e) {
        let flag = new RegExp("^[0-9]*$").test(e.target.value);
        if (!flag) {
          this.formData.ruleContent.threshold = ''
        }
      },
      // 初始化表单数据
      initFormData () {
        // // console.log('formData-type:', this.type)
        // // console.log('ruleInfo:', this.$store.getters.ruleInfo)
        // // console.log('initFormData-projectUsers:', this.projectUsers)
        this.dataSource = this.projectUsers
        if (this.type === 'editAlarm') {
          this.formData['notifiType'] = []
          let params = JSON.parse(JSON.stringify(this.$store.getters.ruleInfo))
          for (let key in this.formData) {
            if (key === 'firstTime') {
              this.formData.firstTime = params['effectiveTime'].split('-')[0]
            } else if (key === 'secondTime') {
              this.formData.secondTime = params['effectiveTime'].split('-')[1]
            } else if (key === 'notifyUsers') {
              let userArr = JSON.parse(params[key])
              userArr.forEach(item => {
                this.formData[key].push(item.employee_number + ',' + item.employee_name)
              })
            } else if (key === 'ruleContent') {
              this.formData[key] = JSON.parse(params[key])
            } else if (key === 'notifiType') {
              if (params[key]) {
                this.formData[key] = params[key].split(',')
              }

            } else {
              if (params[key]) this.formData[key] = params[key]
            }
          }
        }
        // // console.log('initFormData-formData:', this.formData)
      }
    }
  }
</script>
<style lang="scss" scoped>
  .alarm-form {
    width: 100%;
    .form-title {
      font-size: 14px;
      color: #333;
      font-weight: 700;
      height: 20px;
      line-height: 20px;
      margin-bottom: 4px;
    }
    .form-item {
      height: 28px;
      line-height: 28px;
      font-size: 12px;
      color: #333;
      margin-bottom: 12px;
      align-items: center;
      display: flex;
      .label {
        display: inline-block;
        width: 78px;
        .flag {
          color: red;
        }
      }
      .form-value {
        width: calc(100% - 78px);
        height: 28px;
        line-height: 28px;
        border-radius: 0;
        /deep/ .ant-input {
          height: 100%;
        }
      }
      .target {
        width: 213px;
        height: 28px;
        /deep/ .ant-select-selection--single {
          height: 28px;
          border-radius: 0;
          .ant-select-selection__rendered {
            line-height: 28px;
          }
        }
      }
      .time {
        width: 80px;
        height: 28px;
        border-left: 0;
        border-radius: 0;
        /deep/ .ant-input-number {
          border-radius: 0;
        }
        /deep/ .ant-input-number-handler-wrap {
          border-radius: 0;
        }
        /deep/ .ant-input-number-input {
          height: 28px;
          line-height: 28px;
          border-radius: 0;
        }
      }
      .operator {
        width: 80px;
        height: 28px;
        border-left: 0;
        /deep/ .ant-select-selection--single {
          height: 28px;
          border-radius: 0;
          border-left: 0;
          .ant-select-selection__rendered {
            line-height: 28px;
          }
        }
      }
      .threshold {
        width: 80px;
        height: 28px;
        line-height: 28px;
        width: calc(100% - 373px - 78px);
        border-radius: 0;
        border-left: 0;
      }
      .time-picker {
        width: 100px;
        height: 28px;
        /deep/ .ant-time-picker-input {
          height: 28px;
        }
      }
      .rate {
        width: calc(100% - 78px);
        height: 28px;
        /deep/ .ant-input-number-input {
          height: 28px;
          line-height: 28px;
          border-radius: 0;
        }
      }
      /deep/ .ant-checkbox-group {
        font-size: 12px;
        color: #333;
      }
      .auto-complete {
        width: calc(100% - 78px);
        height: 28px;
        /deep/ .ant-input {
          height: 28px;
        }
        /deep/ .ant-select-selection__rendered {
          line-height: 28px;
        }
        /deep/ .ant-select-selection--multiple {
          max-height: 80px;
          overflow-y: scroll;
          // &::-webkit-scrollbar {
          //   //整体样式
          //   height: 12px;
          //   width: 6px;
          // }
          // &::-webkit-scrollbar-thumb {
          //   //滑动滑块条样式
          //   border-radius: 6px;
          //   background: #ab90e8;
          //   height: 20px;
          // }
          // &::-webkit-scrollbar-track {
          //   //轨道的样式
          //   background-color: #fff;
          // }
          > ul > li {
            margin-top: 1px;
          }
        }
      }
    }
    .description {
      padding-left: 78px;
      font-size: 12px;
      color: #999;
      margin-bottom: 4px;
      height: 16px;
      line-height: 16px;
      span {
        display: inline-block;
      }
      .target {
        width: 213px;
      }
      .time {
        width: 80px;
      }
      .operator {
        width: 80px;
      }
      .threshold {
        width: calc(100% - 373px);
      }
    }
    .rate-description {
      padding-left: 78px;
      margin-top: -8px;
      color: #999;
      font-size: 12px;
      margin-bottom: 12px;
    }
  }
</style>
