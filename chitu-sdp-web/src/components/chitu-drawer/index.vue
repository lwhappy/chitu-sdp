<!--
 * @description: 封装抽屉组件
 * @Author: lijianguo19
 * @Date: 2022-08-17 16:39:12
 * @FilePath: \src\dataHub\components\chitu-drawer\index.vue
-->
<template>
  <a-drawer :closable="false"
            class="chitu-drawer-container"
            :class="{'is-max':isMax || isMaxWith}"
            @close="closeDrawer"
            :maskClosable="maskClosable"
            :visible="drawerVisible">
    <!-- 头部信息 -->
    <template #title>
      <div class="drawer-title">
        <template v-if="showExpand">
          <a-icon v-if="!isMax"
                  class="close-drawer"
                  type="double-left"
                  @click="isMax=!isMax" />
          <a-icon v-if="isMax"
                  class="close-drawer"
                  type="double-right"
                  @click="isMax=!isMax" />
        </template>
        <span>{{title}}</span>
        <i class="chitutree-h5 chitutreeguanbi remove"
           @click="closeDrawer"></i>
      </div>
    </template>
    <!-- 主体内容 -->
    <slot name="content" />
    <!-- 底部 -->
    <div :style="{
          position: 'absolute',
          right: 0,
          bottom: 0,
          width: '100%',
          borderTop: '1px solid #e9e9e9',
          padding: '12px 16px',
          background: '#fff',
          textAlign: 'right',
          zIndex: 1,
        }">
      <!-- 自定义底部插槽 -->
      <template v-if="$slots.footer">
        <slot name="footer" />
      </template>
      <!-- 默认底部 -->
      <template v-else>
        <a-button size="small"
                  @click="closeDrawer">{{cancelText}}</a-button>
        <a-button type="primary"
                  size="small"
                  style="margin-left:8px"
                  @click="save">{{saveText}}</a-button>
      </template>
    </div>
  </a-drawer>
</template>

<script>

  export default {
    name: 'data-hub-drawer',
    components: {},
    props: {
      drawerVisible: {
        type: Boolean,
        default: false
      },
      title: {
        type: String,
        default: ''
      },
      // 是否展示伸缩按钮
      showExpand: {
        type: Boolean,
        default: true
      },
      maskClosable: {
        type: Boolean,
        default: true
      },
      cancelText: {
        type: String,
        default: '取消'
      },
      saveText: {
        type: String,
        default: '保存'
      },
      isMaxWith: {
        type: Boolean,
        default: false
      }
    },
    data () {
      return {
        isMax: false
      };
    },
    computed: {},
    watch: {
      // 抽屉伸缩状态改变通知
      isMax (value) {
        if (this.drawerVisible) {
          this.$emit('isMaxChange', value)
        }
      }
    },
    created () { },
    mounted () { },
    methods: {
      closeDrawer () {
        this.$emit('close')
      },
      save () {
        this.$emit('save')
      }
    },
  }
</script>
<style lang='scss' scoped>
  .chitu-drawer-container {
    &.is-max {
      /deep/ .ant-drawer-content-wrapper {
        width: calc(100vw - 178px) !important;
      }
    }
    /deep/ .ant-drawer-content-wrapper {
      top: 72px;
      height: calc(100% - 72px);
      // width: calc(100vw - 420px) !important;
      width: 905px !important;
      transition: all 0.3s cubic-bezier(0.7, 0.3, 0.1, 1);
      .ant-drawer-header {
        padding: 9px 16px 8px 16px;
        .drawer-title {
          font-size: 16px;
          display: flex;
          align-items: center;
          position: relative;
          .close-drawer {
            cursor: pointer;
            color: #2c2f37;
          }
          .remove {
            position: absolute;
            right: 0;
            cursor: pointer;
          }
          i {
            margin-right: 8px;
          }
          span {
            font-weight: 600;
          }
        }
      }
      .ant-drawer-body {
        // height: calc(100% - 40px - 60px);
        height: calc(100% - 77px);
        box-sizing: border-box;
        overflow-y: auto;
      }
    }
  }
</style>