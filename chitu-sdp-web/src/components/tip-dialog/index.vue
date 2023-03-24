<template>
  <!-- 一定要用v-if="isShowAddProject"摧毁dom重新渲染，不然会有莫名其妙的问题 -->
  <a-modal v-model="isVisible"
           :closable="false"
           wrapClassName="tip-dialog"
           :mask-closable="false"
           :footer="null"
           title="">
    <div class="confirm-header justify-start">
      <i v-if="type==='normal'"
         class="chitutree-h5 chitutreeicon_tips blue"></i>
      <i v-else-if="type==='warning'"
         class="chitutree-h5 chitutreeicon_warning_tips yellow"></i>
      <i v-else
         class="chitutree-h5 chitutreeicon_tips blue"></i>
      <span>{{title}}</span>
    </div>
    <div class="confirm-main">
      <slot />
    </div>
    <div class="confirm-footer justify-end">
      <!-- <a-button class="button-restyle button-confirm"
                @click="confirmEvent">确认</a-button> -->
      <a-button class="button-restyle button-confirm"
                @click="cancelEvent">关闭</a-button>
    </div>
  </a-modal>
</template>

<script>
  export default {
    components: {

    },
    data () {
      return {
        isVisible: false
      }
    },
    props: {
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
      }
    },
    computed: {

    },
    watch: {
      visible: {
        handler (value) {
          this.isVisible = value
        },
        immediate: true
      }
    },
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
  .tip-dialog {
    z-index: 2000 !important;
    .ant-modal {
      width: 500px !important;
      // height: 500px;
      background: #ffffff;
      box-shadow: 0px 10px 20px 0px rgba(0, 0, 0, 0.15);
      padding: 0;
      .yellow {
        color: #ff9300;
      }
      .ant-modal-content {
        box-shadow: none;
        padding: 14px 18px 12px;
        position: relative;
        height: 100%;
        .ant-modal-body {
          height: 100%;
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
        .confirm-main {
          margin-top: 20px;
          overflow: auto;
          min-height: 100px;
          max-height: 500px;
          margin-bottom: 40px;
        }
        .confirm-footer {
          position: absolute;
          right: 12px;
          bottom: 12px;
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