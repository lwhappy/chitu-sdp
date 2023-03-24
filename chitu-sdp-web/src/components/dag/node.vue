<!--
 * @Author: your name
 * @Date: 2022-02-11 16:59:32
 * @LastEditTime: 2022-07-04 20:18:49
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\components\dag\node.vue
-->
<template>
  <div :class="{'dag-node': true, 'source-node-bg': pact === 'Data Source', 'operator-node-bg': pact === 'Operator', 'sink-node-bg': pact === 'Data Sink'}">
    <div class="node-icon">
      <img v-if="pact === 'Data Source'"
           src="@/assets/icons/node-source-icon.png"
           alt="">
      <img v-else-if="pact === 'Operator'"
           src="@/assets/icons/node-operator-icon.png"
           alt="">
      <img v-else-if="pact === 'Data Sink'"
           src="@/assets/icons/node-sink-icon.png"
           alt="">
    </div>
    <!-- 标题 -->
    <div :class="{'title': true, 'source-title': pact === 'Data Source', 'operator-title': pact === 'Operator', 'sink-title': pact === 'Data Sink'}">{{ title }}</div>
    <!-- 内容 -->
    <div :class="{'content': true, 'source-content': pact === 'Data Source', 'operator-content': pact === 'Operator', 'sink-content': pact === 'Data Sink'}">{{ content }}</div>
    <!-- 底部 -->
    <div :class="{'footer': true, 'source-footer': pact === 'Data Source', 'operator-footer': pact === 'Operator', 'sink-footer': pact === 'Data Sink'}">{{ footer }}</div>
  </div>
</template>
<script>
  import '@antv/x6-vue-shape'
  export default {
    name: 'Node',
    inject: ['getGraph', 'getNode'],
    data () {
      return {
        title: '',
        content: '',
        footer: '',
        pact: ''
      }
    },
    mounted () {
      this.initNode()
    },
    methods: {
      initNode () {
        const self = this
        const node = this.getNode()
        // console.log('node: ', node)
        self.title = node.store.data.data.title
        self.content = node.store.data.data.content
        self.footer = node.store.data.data.footer
        self.pact = node.store.data.data.pact
      }
    }
  }
</script>
<style lang="scss" scoped>
  .source-node-bg {
    // 起始节点
    background: rgba(241, 231, 255, 1);
    border: 1px solid rgba(208, 134, 226, 1);
  }
  .operator-node-bg {
    // 中间节点
    background: rgba(233, 231, 255, 1);
    border: 1px solid rgba(159, 134, 226, 1);
  }
  .sink-node-bg {
    // 结束节点
    background: rgba(227, 236, 255, 1);
    border: 1px solid rgba(95, 149, 255, 1);
  }
  .dag-node {
    width: 200px;
    height: auto;
    // background-color: #5f95ff;
    margin-bottom: 100px;
    // color: #fff;
    overflow: hidden;
    .node-icon {
      position: absolute;
      top: -16px;
      height: 32px;
      left: calc(50% - 16px);
      img {
        width: 32px;
        height: 32px;
      }
    }
    .source-icon {
      width: 32px !important;
      font-size: 32px !important;
      color: #d086e2;
    }
    .title {
      height: 60px;
      line-height: 60px;
      text-align: center;
      font-size: 16px;
      font-weight: 600;
    }
    .source-title {
      color: #d086e2;
    }
    .operator-title {
      color: #9f86e2;
    }
    .sink-title {
      color: #5f95ff;
    }
    .content {
      // min-height: 200px;
      // height: auto;
      height: 200px;
      padding: 0 2px 5px 4px;
      text-align: left;
      line-height: 18px;
      word-wrap: break-word;
      font-size: 12px;
      overflow-y: auto;
      // &::-webkit-scrollbar {
      //   //整体样式
      //   height: 6px;
      //   width: 2px;
      // }
      // &::-webkit-scrollbar-thumb {
      //   //滑动滑块条样式
      //   border-radius: 2px;
      //   background: #ab90e8;
      //   height: 20px;
      // }
    }
    .source-content {
      color: #d086e2;
      // &::-webkit-scrollbar-track {
      //   //轨道的样式
      //   background-color: rgba(241, 231, 255, 1);
      // }
    }
    .operator-content {
      color: #9f86e2;
      // &::-webkit-scrollbar-track {
      //   //轨道的样式
      //   background-color: rgba(233, 231, 255, 1);
      // }
    }
    .sink-content {
      color: #5f95ff;
      // &::-webkit-scrollbar-track {
      //   //轨道的样式
      //   background-color: rgba(227, 236, 255, 1);
      // }
    }
    .footer {
      height: 40px;
      color: #fff;
      line-height: 40px;
      padding-left: 10px;
    }
    .source-footer {
      background: #d086e2;
      border: 1px solid rgba(208, 134, 226, 1);
    }
    .operator-footer {
      background: #9f86e2;
      border: 1px solid rgba(159, 134, 226, 1);
    }
    .sink-footer {
      background: #5f95ff;
      border: 1px solid rgba(95, 149, 255, 1);
    }
  }
</style>