<template>
  <a-modal wrapClassName="online-progress-dialog"
           :mask-closable="false"
           v-model="isShow"
           :footer="null"
           :title="title">
    <template v-if="isInWhite===false">
      <div class="tip">
        <p>* 本次发布审批通过后在【作业运维】中启动后生效</p>
        <p v-if="progressData.isCheckSuccess && !progressData.isApplySuccess">* 项目管理员负责作业代码和配置审批, 系统管理员负责查询和写入数据源权限审批。请审批人用心审批！</p>
      </div>
    </template>

    <div class="online-progress-wrapper">
      <div v-if="isInWhite===false"
           class="step justify-start">
        <div class="step-item justify-start active">
          <i>1</i>
          <span class="text">发布确认</span>
        </div>
        <!-- <div class="line"
               :class="isVerifying?'active':'gray'"></div>
          <div class="step-item justify-start"
               :class="isVerifying?'active':'gray'">
            <i>2</i>
            <span class="text">最终检查</span>
          </div> -->
        <div class="line"
             :class="progressData.isCheckSuccess?'active':'gray'"></div>
        <div class="step-item justify-start"
             :class="progressData.isCheckSuccess?'active':'gray'">
          <i>2</i>
          <span class="text">审批</span>
        </div>
      </div>
      <a-progress v-if="!progressData.isCheckSuccess"
                  :percent="progress"
                  :stroke-color="strokeColor"
                  status="active" />
      <template v-if="!progressData.isCheckSuccess">
        <a-textarea class="textarea"
                    v-model="remark"
                    :maxLength="1000"
                    placeholder="请输入发布备注（可选）" />
        <p class="input-length">{{remark.length}}/1000</p>
      </template>
    </div>
    <div v-if="progressData.isCheckSuccess && !progressData.isApplySuccess"
         class="apply-tip">确定要申请发布吗？</div>
    <div v-if="progressData.isApplySuccess"
         class="apply-success">申请成功，请在<span class="blue"
            @click="gotoApply">实时作业流程-我的申请</span>页面查看进度</div>
    <div class="footer justify-end">
      <!-- 检验成功,显示审批 -->
      <template v-if="isShowApply">
        <!-- 审批成功 -->
        <template v-if="progressData.isApplySuccess">
          <a-button class="button-restyle button-confirm"
                    @click="close">
            知道啦
          </a-button>
        </template>
        <!-- 开始审批 -->
        <template v-else-if="!progressData.isApplySuccess">
          <a-button class="button-restyle button-cancel"
                    @click="cancelEvent">取消</a-button>
          <a-button class="button-restyle button-confirm"
                    :disabled="isApplying || progressData.isApplySuccess ? true : false"
                    @click="handleApply">
            <p v-if="isApplying"
               class="loading">
              <a-spin>
                <a-icon slot="indicator"
                        type="loading"
                        style="font-size: 24px"
                        spin />
              </a-spin>
            </p>确定申请
          </a-button>

        </template>
      </template>
      <!-- end -->
      <!-- 开始检验 -->
      <template v-else>
        <a-button class="button-restyle button-cancel"
                  @click="cancelEvent">取消</a-button>
      </template>
      <a-button v-if="isInWhite"
                class="button-restyle button-confirm"
                :disabled="isVerifying ? true : false"
                @click="confirmEvent()">
        <p v-if="isVerifying"
           class="loading">
          <a-spin>
            <a-icon slot="indicator"
                    type="loading"
                    style="font-size: 24px"
                    spin />
          </a-spin>
        </p>
        <template>确定</template>

      </a-button>
      <a-button v-if="!isInWhite && progress!=100"
                class="button-restyle button-confirm"
                :disabled="isVerifying ? true : false"
                @click="confirmEvent">
        <p v-if="isVerifying"
           class="loading">
          <a-spin>
            <a-icon slot="indicator"
                    type="loading"
                    style="font-size: 24px"
                    spin />
          </a-spin>
        </p>
        <template v-if="progressData.isCheckSuccess === false">重试</template>
        <template v-if="progressData.isCheckSuccess === ''">下一步</template>
      </a-button>

      <!-- end -->
    </div>

  </a-modal>
</template>

<script>
  export default {
    name: "OnlineProgress",
    data () {
      return {
        isShow: false,
        baseTitle: '发布新版本',
        title: '',
        progress: 0,
        strokeColor: '#32DB7E',
        remark: '',
        isVerifying: false,
        isShowApply: false,
        isApplying: false,
        isInWhite: ''
      }
    },
    props: {
      progressData: {
        type: Object
      }
    },
    computed: {

    },
    components: {
    },
    watch: {
      progressData: {
        handler (val) {
          if (val) {
            this.title = this.baseTitle + val.version
            this.progress = val.progress
            if (val.isCheckSuccess) {//isCheckSuccess : 表示调用验证接口成功
              this.isShowApply = true
            }
            if (val.isCheckSuccess === false) {
              this.strokeColor = '#FF5555'
            } else {
              this.strokeColor = '#32DB7E'
            }

          }
        },
        deep: true,
        immediate: true
      }
    },
    created () {
      this.title = this.baseTitle
    },
    methods: {
      open () {
        this.isShow = true
        this.remark = ''
        this.isVerifying = false
        this.isShowApply = false
        this.progress = 0
        this.isInWhiteList()
      },
      close () {
        this.isShow = false
      },
      async isInWhiteList () {
        let res = await this.$http.get('/sysconfig/isWhiteList', {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          if (res.data) {
            this.isInWhite = true
          } else {
            this.isInWhite = false
          }
        }
      },
      resetData () {
        this.isVerifying = false
        this.isApplying = false
      },
      confirmEvent () {
        this.isVerifying = true
        this.$emit('confirm', this.isInWhite, this.remark)//触发检验和发布
      },
      handleApply () {
        this.$emit('apply', this.remark)//触发申请审批
      },
      cancelEvent () {
        this.isShow = false
      },

      handleTypeChange () {

      },
      select (selectedKeys) {
        this.selectedKeys = selectedKeys
      },
      gotoApply () {
        let active = ''
        this.$router.push({
          name: 'Approve',
          query: {//预留query
            active: active
          }
        })
      },

    },
    mounted () {

    }
  }
</script>
<style lang="scss">
  .online-progress-dialog {
    .ant-modal {
      width: 500px !important;
      .ant-modal-close-x {
        width: 44px;
        height: 44px;
        line-height: 44px;
      }
      .ant-modal-header {
        //弹窗头部
        height: 44px;
        padding: 0;
        .ant-modal-title {
          line-height: 44px;
          padding-left: 16px;
          font-size: 16px;
          font-weight: 600;
        }
      }
    }
  }
</style>
<style lang="scss" scoped>
  /deep/ .ant-modal-body {
    padding: 0;
    .tip {
      background: #fff8ed;
      color: #ff9300;
      font-size: 12px;
      padding-left: 16px;
      padding-right: 16px;
      min-height: 24px;
      line-height: normal;
      p {
        padding: 4px 0;
        &:nth-of-type(2) {
          padding-top: 0;
        }
      }
    }
    .apply-tip {
      text-align: center;
      font-size: 12px;
      span {
        color: #ff5555;
        margin-right: 6px;
      }
    }
    .apply-success {
      text-align: center;
      font-size: 12px;
      span {
        cursor: pointer;
      }
    }
    .online-progress-wrapper {
      padding: 0 16px;
      .input-length {
        text-align: right;
        font-size: 12px;
      }
      .step {
        margin: 12px 0;
        .text {
          font-size: 12px;
          margin-left: 4px;
        }

        .line {
          height: 1px;
          flex: 1;

          margin: 0 8px;
          &.gray {
            background: rgba(232, 232, 237, 0.8);
          }
          &.active {
            background: #006eff;
          }
        }
        .step-item {
          i {
            width: 24px;
            height: 24px;
            display: flex;
            justify-content: center;
            align-items: center;
            text-align: center;
            line-height: 24px;
            border-radius: 24px;
            font-style: normal;
            font-size: 12px;
          }
          &.gray {
            i {
              background: rgba(255, 255, 255, 1);
              border: 1px solid rgba(0, 0, 0, 0.25);
            }
          }
          &.active {
            i {
              background: #0066ff;
              color: #fff;
            }
          }
        }
      }
      .textarea {
        margin: 10px 0 4px;
      }
      .tip {
        color: #ff586c;
        font-size: 12px;
      }
    }

    .footer {
      height: 44px;
      padding-right: 16px;
      border: solid 1px #ddd;
      margin-top: 12px;
      .button-confirm {
        display: flex;
        justify-content: center;
        align-items: center;
      }
      .loading {
        .anticon-loading {
          font-size: 15px !important;
          margin-right: 5px;
        }
      }
    }
  }
</style>