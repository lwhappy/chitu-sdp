<!--
 * @Author: your name
 * @Date: 2022-01-14 10:48:07
 * @LastEditTime: 2022-06-28 16:00:26
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\application\job-develop\components\meta-kafka-auth.vue
-->
<template>
  <a-modal class="meta-kafka-auth"
           v-model="isShowKafkaAuthDialog"
           :mask-closable="false"
           title="提示"
           :footer="null"
           width="360px">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <!-- 内容提示 -->
    <div class="content-msg">
      由于kafka鉴权问题，请点击
      <span @click="helpLink">运维值班wiki文档</span>
      联系对应运维人员处理
    </div>
    <!-- 底部 -->
    <div class="footer justify-end">
      <a-button class="button-restyle button-confirm ant-btn"
                type="primary"
                @click="closeDialog">
        知道啦
      </a-button>
    </div>
  </a-modal>
</template>
<script>
  export default {
    data () {
      return {
        isShowKafkaAuthDialog: false
      }
    },
    methods: {
      helpLink () {
        console.log('helpLink')
      },
      closeDialog () {
        this.isShowKafkaAuthDialog = false
      }
    }
  }
</script>
<style lang="scss" scoped>
  .meta-kafka-auth {
    /deep/ .ant-modal {
      margin-top: 200px;
      margin-right: 150px;
      .ant-modal-content {
        border-radius: 0;
      }
      .ant-modal-header {
        padding: 11px 16px;
        border-radius: 0;
        .ant-modal-title {
          font-weight: 700;
        }
      }
      .ant-modal-close-x {
        width: 44px;
        height: 44px;
        line-height: 44px;
      }
      .ant-modal-body {
        padding: 0 0 12px 0;
        height: auto;
        .content-msg {
          margin: 12px 16px;
          height: 40px;
          span {
            color: #0066ff;
            cursor: pointer;
          }
        }
        .footer {
          margin: 12px 16px 0 16px;
        }
      }
    }
  }
</style>
