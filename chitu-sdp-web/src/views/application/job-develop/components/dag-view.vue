<!--
 * @Author: hjg
 * @Date: 2022-01-20 17:41:17
 * @LastEditTime: 2022-07-29 10:56:50
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\application\job-develop\components\dag-view.vue
-->
<template>
  <div class="dag-view">
    <!-- 重新生成dag图 -->
    <div class="re-generatedag"
         @click="reGenerateDag">
      <i class="chitutree-h5 chitutreeshuaxin"></i>
      重新生成DAG图
    </div>
    <!-- 缩放 -->
    <div class="slider">
      <a-slider vertical
                :default-value="50"
                :tooltipVisible="false"
                @change="zoomEvent" />
    </div>
    <!-- 边界缩放方式 -->
    <div class="fast-scale">
      便捷缩放方式: Ctrl + 滚轮
    </div>
    <!-- dag图 -->
    <div id="mountNode"
         class="mountNode"
         ref="dag"></div>
    <!-- 小地图 -->
    <div class="min-map"
         ref="minMap"></div>
  </div>
</template>
<script>
  import DagGraph from '@/utils/graph'
  export default {
    name: 'dagView',
    props: {
      nodes: {
        type: Object,
        default: () => {
          return {}
        }
      },
      fileDetail: {
        type: Object,
        default: () => {
          return {}
        }
      }
    },
    data () {
      return {
        graph: null
      }
    },
    computed: {
      // 获取尺寸
      getContainerSize () {
        return {
          width: this.$refs.dag.offsetWidth,
          height: this.$refs.dag.offsetHeight
        }
      },
    },
    created () { },
    mounted () {
      this.$nextTick(() => {
        this.initGraph(this.nodes)
      })
    },
    activated () {
      // 监听宽高变化，重新渲染
      const resizeFn = () => {
        const { width, height } = this.getContainerSize
        this.graph.resize(width, height)
      }
      resizeFn()
    },
    methods: {
      // 重新获取dag数据
      async reGenerateDag () {
        this.$emit('change')
      },
      // 初始化dag
      initGraph (nodes) {
        if (JSON.stringify(nodes) === {} || nodes === null) {
          return this.$message.error({ content: '未获取到DAG图信息', duration: 2 })
        }
        this.graph = DagGraph.init(this.$refs.dag, nodes, this.$refs.minMap)

        // 画布缩放-用的到
        this.graph.zoomTo(0.8)
        this.isReady = true
        const resizeFn = () => {
          const { width, height } = this.getContainerSize
          this.graph.resize(width, height)
        }
        resizeFn()
        window.addEventListener('resize', resizeFn)
        return () => {
          window.removeEventListener('resize', resizeFn)
        }
      },
      // 滑块缩放事件
      zoomEvent (value) {
        this.graph.zoomTo(value / 100 * 1.6)
      }
    }
  }
</script>
<style lang="scss" scoped>
  .dag-view {
    width: calc(100% - 36px);
    height: 100%;
    overflow: hidden;
    border-top: 1px solid #d9d9d9;
    .re-generatedag {
      position: absolute;
      top: 2px;
      right: 38px;
      color: #0066ff;
      z-index: 9;
      cursor: pointer;
      text-align: center;
      width: 130px;
      height: 40px;
      line-height: 40px;
      background: rgba(255, 255, 255, 0.8);
      border-radius: 2px;
      box-shadow: 0 2px 6px 0 rgba(165, 161, 176, 0.2);
    }
    .slider {
      position: absolute;
      top: 200px;
      right: 50px;
      z-index: 9;
      height: 200px;
    }
    .fast-scale {
      position: absolute;
      bottom: 5px;
      right: 50px;
      z-index: 9;
      color: #ccc;
      user-select: none;
    }
    .min-map {
      position: absolute;
      width: 240px;
      height: 120px;
      z-index: 9;
      right: 50px;
      bottom: 30px;
      border: 1px solid #9f86e2;
    }
    .mountNode {
      width: 100%;
      height: 100%;
      // overflow: scroll;
      // &::-webkit-scrollbar {
      //   //整体样式
      //   height: 6px;
      //   width: 6px;
      // }
      // &::-webkit-scrollbar-thumb {
      //   //滑动滑块条样式
      //   border-radius: 6px;
      //   background: #ab90e8;
      //   height: 20px;
      // }
      // &::-webkit-scrollbar-track {
      //   //轨道的样式
      //   background-color: rgba(255, 254, 255, 0.75);
      // }
    }
  }
</style>
