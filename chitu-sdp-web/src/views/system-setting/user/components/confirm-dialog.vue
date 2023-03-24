<template>
  <!-- 一定要用v-if="isShowAddProject"摧毁dom重新渲染，不然会有莫名其妙的问题 -->
  <a-modal v-model="isVisible"
           v-if="isVisible"
           :closable="closable"
           wrapClassName="confirm-dialog"
           :mask-closable="false"
           :footer="null"
           v-drag>
    <template #title>
      <div class="confirm-header justify-start">
        <i v-if="type==='normal'"
           class="chitutree-h5 chitutreeicon_tips blue"></i>
        <i v-else-if="type==='warning'"
           class="chitutree-h5 chitutreeicon_warning_tips yellow"></i>
        <i v-else
           class="chitutree-h5 chitutreeicon_tips blue"></i>
        <span>{{title}}</span>
      </div>
    </template>

    <div class="confirm-main">
      <slot />
    </div>
    <div class="confirm-footer justify-end">
      <a-button class="button-restyle button-cancel"
                @click="cancelEvent">{{cancelText}}</a-button>
      <a-button class="button-restyle button-confirm"
                :disabled="confirmDisabled"
                @click="confirmEvent">{{confirmText}}</a-button>

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
      closable: {
        type: Boolean,
        default: false
      },
      visible: {
        type: Boolean,
        default: false
      },
      type: {
        type: String,
        default: 'normal'
      },
      title: {
        type: String,
        default: '提示'
      },
      confirmText: {
        type: String,
        default: '确认'
      },
      cancelText: {
        type: String,
        default: '取消'
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
        this.$emit('close')
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
  .confirm-dialog {
    .ant-modal {
      width: 400px !important;
      height: auto;
      background: #ffffff;
      box-shadow: 0px 10px 20px 0px rgba(0, 0, 0, 0.15);
      padding: 0;
      border-radius: 6px;
      .yellow {
        color: #ff9300;
      }
      .confirm-header {
        color: #333;
        font-size: 16px;
        font-weight: 600;
        i {
          font-size: 24px !important;
          margin-right: 6px;
        }
      }
      .ant-modal-body {
        box-shadow: none;
        width: 400px;
        padding: 5px 0 0;
        box-sizing: border-box;
        position: relative;
        min-height: 228px;
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        align-items: stretch;
        .confirm-main {
          margin-top: 40px;
          padding: 0 18px;
          flex: 1;
          height: 0;
        }
        .confirm-footer {
          border-top: 1px solid #e2e2ea;
          width: 100%;
          padding: 12px;
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