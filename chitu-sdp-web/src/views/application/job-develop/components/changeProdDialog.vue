<template>
  <!-- 一定要用v-if="isShowAddProject"摧毁dom重新渲染，不然会有莫名其妙的问题 -->
  <a-modal v-model="isVisible"
           closable
           wrapClassName="confirm-dialog"
           :mask-closable="false"
           @cancel="cancelEvent"
           :footer="null"
           v-drag>
    <template #title>
      <div class="confirm-header justify-start">
        <i class="chitutree-h5 chitutreeicon_warning_tips yellow"></i>
        <span>提示</span>
      </div>
    </template>

    <div class="confirm-main">
      <!-- <p class="word-break">当前作业的UDX资源版本为 <span class="delete-color ">V2</span>，转生产后生产环境也将同步此版本，是否确认转环境？</p> -->
      <p class="word-break">当前作业的UAT环境UDX资源版本为 <span class="delete-color ">V2</span>，生产环境UDX资源版本为 <span class="delete-color ">V3</span>，转生产后生产环境也将同步此版本，是否确认转环境？</p>
    </div>
    <div class="confirm-footer justify-end">
      <a-space>
        <a-button size="small"
                  @click="cancelEvent">
          取消
        </a-button>
        <a-button type="primary"
                  size="small"
                  @click="confirmEvent(false)">
          确定(不自动转环境)
        </a-button>
        <a-button type="primary"
                  size="small"
                  @click="confirmEvent(true)">
          确定
        </a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script>
  export default {
    components: {

    },
    data () {
      return {

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
    methods: {
      confirmEvent (flag) {
        console.log(flag);
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
