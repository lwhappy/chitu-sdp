<template>
  <div id="tags-view-container"
       class="tags-view-container">
    <scroll-pane ref="scrollPane"
                 class="tags-view-wrapper"
                 @scroll="handleScroll">
      <!--https://next.router.vuejs.org/guide/migration/#removal-of-append-prop-in-router-link -->
      <div v-for="tag in visitedViews"
           :id="tag.firstRouteName"
           ref="tag"
           :key="tag.path"
           @click="link(tag,$event)">
        <span :class="tag.isActive?'active':''"
              class="tags-view-item"
              @click.middle="closeSelectedTag(tag)"
              @contextmenu.prevent="openMenu(tag,$event)"
              role="link">
          <span class="u-title">{{ tag.title }}</span>
          <span v-if="isShowClose(tag)"
                class="el-icon-close"
                @click.prevent.stop="closeSelectedTag(tag)" />
        </span>
      </div>
    </scroll-pane>
    <div style="position:relative;">
      <ul v-show="visible"
          :style="{left:left+'px',top:'0px'}"
          class="context-menu">
        <li v-show="showClose"
            @click="closeSelectedTag(selectedTag)">关闭</li>
        <li v-show="showClose"
            @click="closeOthersTags">关闭其它</li>
      </ul>
      <a ref="href"
         hidden
         target="_blank"
         :href="`/#${selectedTag.path || '/'}`"></a>
    </div>
    <!-- <EnvModal /> -->
  </div>
</template>

<script>
  import ScrollPane from './ScrollPane'
  // import EnvModal from './env-modal.vue'
  export default {
    // components: { ScrollPane, EnvModal },
    components: { ScrollPane },
    data () {
      return {
        visible: false,
        left: 0,
        selectedTag: {},
        affixTags: []
      }
    },
    computed: {
      visitedViews () {
        return this.$store.state.tagsView.visitedViews
      },
      showClose () {
        return this.selectedTag.tag !== '/'
      }
    },
    watch: {
      $route: {
        handler () {
          this.init()

        },
        deep: true
      },
      visible (value) {
        if (value) {
          document.body.addEventListener('click', this.closeMenu)
        } else {
          document.body.removeEventListener('click', this.closeMenu)
        }
      }
    },
    mounted () {
      // this.initTags()
      this.init()
    },
    methods: {
      init () {
        this.addTags()
        const query = this.$route.query
        if (query.projectId) {//存储当前项目信息
          //只监听作业开发，作业运维，资源管理，数据源管理页面的projectId
          const applicationNames = ['JobDevelop', 'JobOperate', 'SourceManage', 'DataSourceManage']
          if (applicationNames.includes(this.$route.name) && (query.projectId !== this.$store.getters.currentProject.id || query.projectName !== this.$store.getters.currentProject.name || query.projectCode !== this.$store.getters.currentProject.code)) {
            const currentProject = {
              id: query.projectId,
              name: query.projectName,
              code: query.projectCode
            }
            this.$store.dispatch('global/saveCurrentProject', currentProject)

          }
        }
        this.moveToCurrentTag()
      },
      link (item) {
        //跳转到作业开发，作业运维，资源管理，数据源管理需要带上项目信息
        const applicationNames = ['JobDevelop', 'JobOperate', 'SourceManage', 'DataSourceManage']
        if (applicationNames.includes(item.firstRouteName) && this.$store.getters.currentProject.id) {
          this.$router.push({
            name: item.firstRouteName,
            query: {
              projectId: this.$store.getters.currentProject.id,
              projectName: this.$store.getters.currentProject.name,
              projectCode: this.$store.getters.currentProject.code
            }
          })
        } else {
          this.$router.push({
            path: item.path
          })
        }

      },
      isActive (tag) {
        let path1 = this.$route.fullPath
        let path2 = tag.path
        if (this.$route.meta && this.$route.meta.tag && !this.$route.meta.isEveryNewTag) {
          path1 = this.$route.meta.tag
        }
        if (tag.tag) {
          path2 = tag.tag
        }
        return path2 === path1
      },
      isAffix (tag) {
        return tag.meta && tag.meta.affix
      },
      isShowClose (tag) {//如果只剩下一个报表配置页签，不显示关闭按钮
        if (tag.tag === '/project' && this.visitedViews.length === 1) {
          return false
        }
        return true
      },
      addTags () {
        const { name } = this.$route
        if (name) {
          if (this.$route.query.from == 'login') {
            this.$router.push({ query: {} })
          }
          this.$store.dispatch('tagsView/addView', this.$route)
        }
        return false
      },
      moveToCurrentTag () {
        this.$nextTick(() => {
          for (const tag of this.visitedViews) {
            if (tag.path === this.$route.fullPath) {
              this.$refs.scrollPane.moveToTarget(document.getElementById(tag.firstRouteName))
              // when query is different then update
              if (tag.fullPath !== this.$route.fullPath) {
                this.$store.dispatch('tagsView/updateVisitedView', this.$route)
              }
              break
            }
          }
        })
      },
      refreshSelectedTag (view) {
        this.$store.dispatch('tagsView/delCachedView', view).then(() => {
          const { fullPath } = view
          this.$nextTick(() => {
            this.$router.replace({
              path: '/redirect' + fullPath
            })
          })
        })
      },
      closeSelectedTag (tag) {
        console.log('跳到最后1个')

        this.closeSelectedTagEvent(tag)
      },
      closeSelectedTagEvent (view) {
        console.log('closeSelectedTagEvent', view)
        this.$store.dispatch('tagsView/delView', view).then(({ visitedViews }) => {
          if (view.isActive) {
            this.toLastView(visitedViews, view)
          }
        })
      },
      closeOthersTags () {
        this.$router.push(this.selectedTag)
        this.$store.dispatch('tagsView/delOthersViews', this.selectedTag).then(() => {
          this.moveToCurrentTag()
        })
      },
      closeAllTags (view) {
        this.$store.dispatch('tagsView/delAllViews').then(({ visitedViews }) => {
          if (this.affixTags.some(tag => tag.path === view.path)) {
            return
          }
          this.toLastView(visitedViews, view)
        })
      },
      toLastView (visitedViews, view) {
        const latestView = visitedViews.slice(-1)[0]
        if (latestView) {
          const path = latestView.fullPath || latestView.path
          this.$router.push(path)
        } else {
          // now the default is to redirect to the home page if there is no tags-view,
          // you can adjust it according to your needs.
          if (view.name === 'Dashboard') {
            // to reload home page
            const path = view.fullPath || view.path
            this.$router.replace({ path: '/redirect' + path })
          } else {
            this.$router.push('/')
          }
        }
      },
      openMenu (tag, e) {
        //如果只剩下一个报表配置页签，不显示关闭按钮
        if (tag.meta && tag.meta.tag === '/project' && this.visitedViews.length === 1) {
          return
        }
        const menuMinWidth = 105
        const offsetLeft = this.$el.getBoundingClientRect().left // container margin left
        const offsetWidth = this.$el.offsetWidth // container width
        const maxLeft = offsetWidth - menuMinWidth // left boundary
        const left = e.clientX - offsetLeft + 5 // 15: margin right

        if (left > maxLeft) {
          this.left = maxLeft
        } else {
          this.left = left
        }

        // this.top = e.clientY
        this.visible = true
        this.selectedTag = tag
      },
      closeMenu () {
        this.visible = false
      },
      handleScroll () {
        this.closeMenu()
      }
    }
  }
</script>

<style lang='less' scoped>
  .tags-view-container {
    position: relative;
    white-space: nowrap;
    // overflow: hidden;
    width: 100%;
    height: 28px;
    background-color: #fff;
    margin-left: -1px;
    border-top: 1px solid #e3e8ed;
    /deep/ .el-scrollbar__wrap {
      position: relative;
      white-space: nowrap;
      overflow: hidden;
      width: 100%;
      height: 29px;
      background: #eff1f6;
      margin-left: -1px !important;
      margin-bottom: 0 !important;
    }
    /deep/ .el-scrollbar__view {
      position: absolute;
      display: flex;
      align-items: center;
      height: 100%;
      .tags-view-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
        color: #93a1bb;
        padding-left: 12px;
        padding-right: 10px;
        // border-left: 1px solid #dbdae6;
        height: 29px;
        cursor: pointer;
        border: 1px solid #e2e2ea;
        border-bottom: none;
        box-shadow: 0 15px 20px 0 rgba(206, 205, 219, 0.3);
        background-color: #f8f8fa;
        margin-right: 4px;
        border-radius: 4px 4px 0 0;
        margin-top: 1px;
        &:last-of-type {
          border-right: 1px solid #dbdae6;
        }
        &.active {
          background-color: #fff;
          color: #2b2f37;
          // font-weight: 600;
          .iconfont {
            color: #bcb9cc;
          }
        }
        &:not(.active):hover {
          // background-color: #dbdae6;
          color: #0066ff;
          .iconfont {
            color: #0066ff;
          }
        }
        .u-title {
          padding-right: 16px;
        }
        .icon-dangrishibairenwushu {
          font-size: 12px;
          height: 12px;
          width: 12px;
          cursor: pointer;
          -webkit-text-stroke-width: 0.1px;
          &:hover {
            border-radius: 50%;
            background-color: #d8d8d8;
            // background: red;
          }
        }
      }
    }
    .context-menu {
      position: absolute;
      top: -10px;
      margin: 0;
      z-index: 100;
      padding: 5px 0;
      color: #333;
      font-size: 12px;
      font-weight: 400;
      background: #fff;
      border-radius: 4px;
      list-style-type: none;
      box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, 0.3);
      li {
        margin: 0;
        padding: 7px 16px;
        cursor: pointer;
        &:hover {
          color: #66b1ff;
          background: #ecf5ff;
        }
      }
    }
  }
</style>
