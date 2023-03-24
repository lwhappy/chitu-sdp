<template>
  <div class="senior"
       :class="{ 'is-max': isMaxWidth }">
    <div class="row">
      <!-- <a-select class="select"
                v-model="mode"
                @change="change">
        <a-select-option v-for="item in modeList"
                         :key="item.name">
          {{ item.name }}
        </a-select-option>
      </a-select> -->

    </div>
    <div v-if="mode === '基础模式'"
         class="basic">
      <div class="row justify-between align-start">
        <!-- <div class="col">
          <h3>Job Manager CPUs</h3>
          <a-input type="number"
                   v-model="jobManagerCpu"
                   placeholder="请输入Job Manager CPUs">
            <a-tooltip slot="suffix"
                       title="任何大于 0 的数,例如：1，10.5">
              <a-icon type="info-circle"
                      style="color: rgba(0,0,0,.45)" />
            </a-tooltip>
          </a-input>
        </div> -->
        <div class="col">
          <h3>Job Manager Memory</h3>
          <div class="justify-start">
            <a-input type="number"
                     v-model="jobManagerMem"
                     placeholder="请输入Job Manager Memory">
              <a-tooltip slot="suffix"
                         title="以G为单位，可输入任意数，例如1，或者0.5等">
                <a-icon type="info-circle"
                        style="color: #7FB6FF" />
              </a-tooltip>
            </a-input>
            <span class="unit">G</span>
          </div>
        </div>
      </div>
      <div class="row justify-between align-start">
        <!-- <div class="col">
          <h3>Task Manager CPUs</h3>
          <a-input type="number"
                   v-model="taskManagerCpu"
                   placeholder="请输入Task Manager CPUs">
            <a-tooltip slot="suffix"
                       title="任何大于 0 的数,例如：1，10.5">
              <a-icon type="info-circle"
                      style="color: rgba(0,0,0,.45)" />
            </a-tooltip>
          </a-input>
        </div> -->
        <div class="col">
          <h3>Task Manager Memory</h3>
          <div class="justify-start">
            <a-input type="number"
                     v-model="taskManagerMem"
                     placeholder="请输入Task Manager Memory">
              <a-tooltip slot="suffix"
                         title="以G为单位，可输入任意数，例如1，或者0.5等">
                <a-icon type="info-circle"
                        style="color: #7FB6FF" />
              </a-tooltip>
            </a-input>
            <span class="unit">G</span>
          </div>
        </div>
      </div>
      <div class="row justify-between">
        <div class="col">
          <h3>并发度</h3>
          <a-input type="number"
                   v-model="parallelism"
                   placeholder="请输入并发度">
            <a-tooltip slot="suffix"
                       title="任何大于 0 的整数">
              <a-icon type="info-circle"
                      style="color: #7FB6FF" />
            </a-tooltip>
          </a-input>
        </div>
        <!-- <div class="col">
          <h3>Task Managers 数量</h3>
          <a-input ref="userNameInput"
                   v-model="userName"
                   placeholder="Basic usage">
            <a-tooltip slot="suffix"
                       title="Extra information">
              <a-icon type="info-circle"
                      style="color: rgba(0,0,0,.45)" />
            </a-tooltip>
          </a-input>
        </div> -->
      </div>

    </div>
  </div>
</template>

<script>
  export default {
    name: "ResourceConfig",
    data () {
      return {
        modeList: [{ name: '基础模式' }],
        selectId: '',
        sourceConfig: {},
        mode: '',
        jobManagerCpu: '',
        jobManagerMem: '',
        taskManagerCpu: '',
        taskManagerMem: '',
        parallelism: ''
      }
    },
    props: {
      config: {
        type: Object,
        default: () => {
          return {}
        }
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
        handler (val) {
          if (val) {
            this.sourceConfig = val
            this.sourceConfig.mode = this.sourceConfig.mode || '基础模式'
            this.mode = this.sourceConfig.mode
            this.parallelism = this.sourceConfig.parallelism || 1//并发度
            this.jobManagerCpu = this.sourceConfig.jobManagerCpu || 1
            this.jobManagerMem = this.sourceConfig.jobManagerMem || 1
            this.taskManagerCpu = this.sourceConfig.taskManagerCpu || 1
            this.taskManagerMem = this.sourceConfig.taskManagerMem || 1
          }
        },
        immediate: true
      }
    },
    created () {
    },
    methods: {
      change (value) {
        this.selectId = value
        this.sourceConfig.mode = value
      }

    },
    mounted () {

    }
  }
</script>
<style lang="scss" scoped>
  .senior {
    width: 100%;
    font-size: 12px;
    padding: 0 16px;

    .row {
      margin-top: 12px;

      .title {
        font-weight: 500;
        color: #2c2f37;
        line-height: 20px;
        font-size: 14px;
        letter-spacing: 0.23px;
      }

      .unit {
        margin-left: 2px;
      }

      h3 {
        color: #333;
        margin-bottom: 8px;
        font-size: 12px;
      }

      .select {
        width: 100%;
        font-size: 12px;
      }

      .col {
        width: calc(100% - 6px);

        .ant-input-affix-wrapper {
          width: 95%;
        }
      }

      /deep/ input {
        font-size: 12px;
      }

      .tip {
        color: #999;
      }
    }

    .basic {
      h3 {
        padding-left: 0;
      }
    }

    &.is-max {
      .basic {
        display: block;

        .row {
          width: 60%;
        }
      }
    }
  }
</style>