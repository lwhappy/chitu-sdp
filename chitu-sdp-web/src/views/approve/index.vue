<!--
 * @Author: your name
 * @Date: 2021-12-20 16:23:50
 * @LastEditTime: 2022-09-05 19:50:36
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\approve\index.vue
-->
<template>
  <div class="index-container">
    <div class="tab-container">
      <a-tabs v-model="activeKey">
        <a-tab-pane key="apply"
                    tab="我申请的"
                    force-render>
          <Apply ref="apply" />
        </a-tab-pane>
        <a-tab-pane key="approve"
                    tab="我审批的"
                    force-render>
          <Approve ref="approve" />
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>
<script>
  import Apply from './components/apply.vue'
  import Approve from './components/approve.vue'

  export default {
    data () {
      return {
        activeKey: 'apply'
      };
    },
    components: {
      Apply, Approve
    },
    created () {

    },
    watch: {
      '$store.getters.isRemoveTag': {//监听关闭页签，关闭页签后清除缓存
        handler (val, oldVal) {
          if (val === oldVal) {
            return
          }
          if (this.$store.getters.removeRouteName.includes('ApproveList')) {
            this.$common.toClearCache(this);
          }
        }
      }
    },
    mounted () {
      const active = this.$route.query.active
      // debugger
      if (active === 'approve') {
        this.activeKey = 'approve'
      }
    },
    activated () {
      const active = this.$route.query.active
      this.activeKey = active || this.activeKey
    },
    methods: {

      init () {
        this.$refs[this.activeKey].getList()
      },
      // changeTab (key) {
      //   // console.log(key)
      //   this.$refs[key].getList()
      // }
    }
  };
</script>

<style lang="scss" scoped>
  .index-container {
    height: 100%;
    .tab-container {
      height: 100%;
      /deep/ .ant-tabs {
        height: 100%;
      }
      h3 {
        height: 40px;
        line-height: 40px;
        margin: 0 18px;
        font-size: 16px;
        font-weight: bold;
        i {
          margin-right: 8px;
          font-size: 16px !important;
        }
      }
      /deep/ .ant-tabs-content {
        .apply-container {
          height: 100%;
        }
      }
      /deep/ .ant-tabs-bar {
        background: rgba(249, 249, 249, 1);
        border: 1px solid rgba(221, 221, 221, 1);
        padding-left: 24px;
        margin-bottom: 0;
        .ant-tabs-nav {
          .ant-tabs-tab {
            padding: 10px 0px;
            font-size: 12px;
          }
        }
      }
    }
  }
</style>