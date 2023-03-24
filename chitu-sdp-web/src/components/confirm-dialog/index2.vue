<template>
  <!-- 一定要用v-if="isShowAddProject"摧毁dom重新渲染，不然会有莫名其妙的问题 -->
  <a-modal v-model="isVisible"
           :closable="false"
           :width="width"
           wrapClassName="confirm-dialog2"
           :mask-closable="false"
           :footer="null"
           title="">
    <div class="confirm-wrapper justify-start align-stretch">
      <div class="confirm-header justify-between">

        <span>{{title}}</span>
        <i class="chitutree-h5 chitutreeguanbi"
           @click="cancelEvent"></i>
      </div>
      <div class="confirm-main">
        <slot />
      </div>
      <div v-if="isShowFooter"
           class="confirm-footer justify-end">
        <div v-if="$slots.footer"
             class="footer-slot">
          <slot name="footer" />
        </div>
        <div class="footer-btns justify-end">
          <a-button @click="cancelEvent"
                    size="small">取消</a-button>
          <a-button style="margin-left:8px"
                    @click="confirmEvent"
                    :disabled="confirmDisabled"
                    size="small"
                    type="primary">确定</a-button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script>
  export default {
    components: {

    },
    data () {
      return {
        // isVisible: false
      }
    },
    props: {
      visible: {
        type: Boolean,
        default: false
      },

      title: {
        type: String,
        default: '提示'
      },
      width: {
        type: Number,
        default: 520
      },
      isShowFooter: {
        type: Boolean,
        default: true
      },
      confirmDisabled: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      isVisible: {
        get () {
          return this.visible
        },
        set () {
          this.$emit('close')
        }
      }
    },
    // watch: {
    //   visible: {
    //     handler (value) {
    //       this.isVisible = value
    //     },
    //     immediate: true
    //   }
    // },
    methods: {
      confirmEvent () {
        this.$emit('confirm')
        // this.$emit('close')
      },
      cancelEvent () {
        this.$emit('cancel')
        this.$emit('close')
      }
    },
    mounted () {

    }
  }
</script>
<style lang="scss">
  .confirm-dialog2 {
    .ant-modal {
      min-width: 400px !important;
      min-height: 222px;
      background: #ffffff;
      border-radius: 6px;
      box-shadow: 0px 10px 20px 0px rgba(0, 0, 0, 0.15);
      padding: 0;
      .yellow {
        color: #ff9300;
      }
      .ant-modal-content {
        box-shadow: none;
        position: relative;
        height: 100%;
        min-height: 222px;
        .confirm-wrapper {
          flex-direction: column;
          min-height: 222px;
        }
        .confirm-header {
          color: #333;
          font-size: 16px;
          font-weight: 600;
          height: 44px;
          border-bottom: 1px solid #d7d7db;
          padding: 0 16px;
          i {
            font-size: 12px !important;
            font-weight: normal;
            color: #666;
            margin-right: 0;
            cursor: pointer;
          }
        }
        .confirm-main {
          margin-top: 40px;
          padding: 0 18px;
          flex: 1;
        }
        .confirm-footer {
          // position: absolute;
          width: 100%;
          height: 48px;
          // right: 0;
          // bottom: 0;
          border-top: 1px solid #d7d7db;
          .footer-slot {
            width: 60%;
          }
          .footer-btns {
            height: 100%;
            width: 40%;
            padding-right: 16px;
          }
        }
      }
    }
  }
</style>
<style lang="scss" scoped>
  /deep/ .ant-modal-body {
    padding: 0;
    .add-project {
      .form-body {
        padding: 0 16px;
        .ant-form-item {
          margin-bottom: 0;
          margin-top: 12px;
          font-size: 12px;
          input {
            font-size: 12px;
          }
          .label {
            line-height: normal;
          }
          .red {
            color: red;
          }
        }
      }
      .footer {
        height: 44px;
        padding-right: 16px;
      }
    }
  }
</style>