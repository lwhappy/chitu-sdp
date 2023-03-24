<!--
 * @Author: your name
 * @Date: 2022-01-21 16:20:10
 * @LastEditTime: 2022-07-15 16:39:27
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\application\job-develop\index.vue
-->
<template>
  <div class="develop-container">
    <div class="develop-wrap justify-start">
      <!-- <left-nav /> -->
      <chitu-collapse ref="collapseRef"
                    showBtn="BtnArrow"
                    :btnStyle="btnStyle"
                    @collapsed="onCollapsed"
                    collapsedStyle="width: 0;"
                    :collapsedWidth="0"
                    width="244px"
                    :pull-able-setting="{
            minWidth: 40,
            maxWidth: 500
          }">
        <tree />
      </chitu-collapse>

      <main-board ref="mainBoard" />
    </div>

  </div>
</template>
<script>
  // import LeftNav from './components/left-nav'
  import chituCollapse from '@/components/chitu-collapse'
  import Tree from './components/tree'
  import MainBoard from './components/main-board'
  export default {
    name: "JobDevelop",
    components: {
      Tree, MainBoard, chituCollapse
    },
    mixins: [],
    data () {
      return {
        isShowSaveIcon: false,
        isAnimate: false,
        form: {
          name: '',
          // id: '',
          ownerId: 0,
        },
        pagination: {
          current: 1,
          size: 10,
          total: 0
        },
        updateTime: '',
        btnStyle: { top: '50%', right: '0px', 'z-index': 10 },
      }
    },
    computed: {

    },
    watch: {
      '$store.getters.isRemoveTag': {//监听关闭页签，关闭页签后清除缓存
        handler (val, oldVal) {
          if (val === oldVal) {
            return
          }
          if (this.$store.getters.removeRouteName.includes('JobDevelop')) {
            this.$common.toClearCache(this);
          }
        }
      }
    },

    mounted () {
    },
    // beforeRouteLeave (to, from, next) {
    //   let openFiles = this.$refs.mainBoard.panes
    //   openFiles = openFiles.map(item => {
    //     return item.id
    //   })
    //   const activeFile = this.$refs.mainBoard.activeKey
    //   let obj = {
    //     activeFile: activeFile,
    //     files: openFiles
    //   }
    //   try {
    //     obj = JSON.stringify(obj)
    //     localStorage.setItem('openFiles', obj)
    //   } catch (e) {
    //     // console.log(e)
    //   }

    //   next()
    // },
    methods: {
      onCollapsed (n) {
        this.btnStyle.right = n ? 0 : '0px'
        this.$refs.accessStatistics && this.$refs.accessStatistics.resize()
        this.$refs.blood && this.$refs.blood.renderBlood()
      }
    },
  }
</script>
<style lang="scss" scoped>
  .develop-container {
    height: 100%;
    .develop-wrap {
      // height: calc(100% - 32px);
      height: 100%;
    }
  }
</style>
