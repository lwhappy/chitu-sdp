<template>
  <div class="main-sidebar-wrapper"
       @dblclick="changeFolder">
    <div class="main-sidebar"
         :class="{'min':isFolder,'max':isFolder === false}">
      <a-menu :defaultSelectedKeys="defaultSelectedKeys"
              :selectedKeys="defaultSelectedKeys"
              :openKeys="openKeys"
              mode="inline"
              theme="dark"
              :inline-collapsed="collapsed"
              @openChange="onOpenChange"
              @click="menuClick">
        <template v-for="item in permissionRoutes">
          <template v-if="!item.hidden">
            <template v-if="hasOneShowingChild(item.children,item) && (!onlyOneChild.children||onlyOneChild.noShowingChildren)&&!item.alwaysShow">
              <!-- eslint-disable-next-line vue/valid-v-for -->
              <a-menu-item :key="onlyOneChild.path">
                <img :src="onlyOneChild.path==defaultSelectedKeys? onlyOneChild.meta.activeIcon:onlyOneChild.meta.icon"
                     class="icon-logo anticon"
                     alt="">
                <span>{{ onlyOneChild.meta.title}}</span>
              </a-menu-item>
            </template>
            <template v-else>
              <sub-menu :key="item.path"
                        :menu-info="item"
                        :active-chitu="defaultSelectedKeys" />
            </template>
          </template>

        </template>
      </a-menu>
    </div>
    <div class="switch-button">
      <button @click="changeFolder"><i class="chitutree-h5 chitutreeicon_shouqi"
           :class="{'active':isFolder}"></i></button>
    </div>
  </div>
</template>
<script>
  import { mapState, mapGetters } from 'vuex'
  import { Menu } from 'ant-design-vue';
  // 定义函数式组件
  const SubMenu = {
    template: `
      <a-sub-menu :key="menuInfo.path" v-bind="$props" v-on="$listeners">
        <span slot="title">
        <img :src="activeChitu[0].split('/')[1]== menuInfo.path.split('/')[1] ? menuInfo.meta.activeIcon:menuInfo.meta.icon"
              class="anticon"
              style="width: 16px;margin-right: 4px;"
              alt="">
       <span>{{ menuInfo.meta.title }}</span>
        </span>
        <template v-for="item in menuInfo.children">
        <template v-if="!item.hidden">
         <a-menu-item  v-if="!item.children" :key="item.path">
                <span>{{ item.meta.title }}</span>
              </a-menu-item>
          <sub-menu v-else :key="item.path" :menu-info="item" />
        </template>
        </template>
         
      </a-sub-menu>
    `,
    name: 'SubMenu',
    // must add isSubMenu: true 此项必须被定义
    isSubMenu: true,
    props: {
      // 解构a-sub-menu的属性，也就是文章开头提到的为什么使用函数式组件
      ...Menu.SubMenu.props,
      // 接收父级传递过来的菜单信息
      menuInfo: {
        type: Object,
        default: () => ({}),
      },
      activeChitu: {
        type: Array,
        default: () => [],
      }
    },
  };
  export default {
    components: {
      'sub-menu': SubMenu,
    },
    data () {
      this.onlyOneChild = null
      return {
        collapsed: false,
        // 全部顶级父节点,用来控制所有父级菜单只展开其中的一项，可用遍历菜单信息进行赋值
        rootSubmenuKeys: ['/application', '/approve', '/system-setting'],
        // 展开的父菜单项
        openKeys: [],
        // 选中的子菜单项
        defaultSelectedKeys: [this.$route.meta.activeMenu || this.$route.path],
      }
    },
    computed: {
      ...mapState('global', {
        'isFolder': 'isFolder'
      }),
      ...mapGetters([
        'userInfo',
        'permissionRoutes'
      ]),
      isAdmin () {
        return this.userInfo.isAdmin
      }
    },
    watch: {
      // 监听路由变化，设置菜单选中
      $route: {
        handler (value) {
          this.defaultSelectedKeys = [value.meta.activeMenu || value.path]
          if (this.collapsed) {
            this.openKeys = []
          } else {
            this.setOpenKeys(value)
          }
        },
        deep: true
      },
      //折叠菜单关闭父级菜单
      collapsed (value) {
        if (value) {
          this.openKeys = []
        } else {
          this.setOpenKeys(this.$route)
        }
      }
    },
    created () {
      this.setOpenKeys(this.$route)
    },
    mounted () { },
    methods: {
      changeFolder () {
        this.collapsed = !this.collapsed
        this.$store.dispatch('global/setFolder')
      },
      async getProjectHistory () {//获取最近打开的项目
        // this.isLoading = true
        let res = await this.$http.post('/project/projectManagement/getProjectHistory')
        // this.isLoading = false
        return res
      },
      async menuClick ({ key }) {
        // 实时开发菜单传递路由参数
        const applicationPathList = ['/application/job-develop', '/application/job-operate', '/application/source-manage', '/application/data-source-manage']
        if (applicationPathList.includes(key)) {
          const projectInfo = this.$store.getters.currentProject
          //读取缓存项目
          let projectId, projectCode, projectName = ''
          if (projectInfo) {
            projectId = projectInfo.id
            projectCode = projectInfo.code
            projectName = projectInfo.name
          } else {
            projectId = this.$route.query.projectId || ''
            projectCode = this.$route.query.projectCode || ''
            projectName = decodeURIComponent(this.$route.query.projectName) || ''
          }
          if (!projectId || !projectName) {
            const res = await this.getProjectHistory()
            if (res.code === 0 && res.data && res.data.projectId && res.data.projectName) {
              projectId = res.data.projectId
              projectName = encodeURIComponent(res.data.projectName)
              projectCode = res.data.projectCode
              const currentProject = {
                id: projectId,
                name: projectName,
                code: projectCode
              }
              this.$store.dispatch('global/saveCurrentProject', currentProject)

            } else {
              this.$message.error({ content: '您还没有参与的实时项目，请前往【项目管理】参与项目或创建项目', duration: 2 })

            }
            return
          }
          this.$router.push({
            path: key,
            query: {
              projectId,
              projectName,
              projectCode
            }
          })
        } else {
          this.$router.push({
            path: key
          })
        }
      },
      setOpenKeys (path) {
        const { matched } = path
        if (matched.length > 1) {
          this.openKeys = [matched[0].path]
        }
      },
      onOpenChange (openKeys) {
        let openKeysList = [];
        if (openKeys.length) {
          openKeysList.push(openKeys[openKeys.length - 1])
        }
        this.openKeys = openKeysList;
      },
      hasOneShowingChild (children = [], parent) {
        const showingChildren = children.filter(item => {
          if (item.hidden) {
            return false
          } else {
            // Temp set(will be used if only has one showing child)
            this.onlyOneChild = item
            return true
          }
        })
        // When there is only one child router, the child router is displayed by default
        if (showingChildren.length === 1) {
          return true
        }

        // Show parent if there are no child router to display
        if (showingChildren.length === 0) {
          this.onlyOneChild = { ...parent, path: '', noShowingChildren: true }
          return true
        }

        return false
      },
    }
  };
</script>
<style>
  .ant-menu-sub {
    background: #1e202d !important;
  }
  .ant-menu-item-selected {
    background: linear-gradient(270deg, #22194b, #006eff) !important;
  }
</style>
<style lang="scss" scoped>
  .main-sidebar-wrapper {
    height: 100%;
    background: #1e202d;
    position: relative;
    .icon-logo {
      width: 16px;
      margin-right: 10px;
    }
    .main-sidebar {
      width: 176px;
      // background: #2a2b38;
      color: #fff;
      font-size: 18px;
      height: calc(100% - 44px);
      overflow-y: auto;
      &::-webkit-scrollbar {
        height: 0;
        width: 0;
      }
      &.min {
        width: 48px !important;
        transition: width 0.5s;
        -webkit-transition: width 0.5s; /* Safari */
      }
      &.max {
        width: 176px !important;
        transition: width 0.5s;
        -webkit-transition: width 0.5s; /* Safari */
      }
    }
    .switch-button {
      position: absolute;
      bottom: 0;
      text-align: center;
      width: 100%;
      button {
        padding: 10px;
        line-height: normal;
        display: block;
        margin: 0 auto;
        background: none;
        border: none;
        cursor: pointer;
        text-align: center;
        i {
          color: #fff;
          display: block;
          font-size: 16px !important;
          transform: rotate(180deg) !important;
          &.active {
            transform: rotate(0deg) !important;
          }
        }
      }
    }
    /deep/ .ant-menu {
      background: #1e202d !important;

      // 菜单栏展开过渡效果修改
      // .ant-motion-collapse-legacy {
      //   overflow: hidden;
      //   &-active {
      //     transition: height 0.3s ease-in-out, opacity 0.3s ease-in-out !important;
      //   }
      // }
      .ant-motion-collapse-legacy {
        overflow: hidden;
        &-active {
          transition: height 0.3s linear, opacity 0.3s linear !important;
        }
      }
      &.ant-menu-inline-collapsed {
        width: 48px !important;
        .ant-menu-item {
          box-sizing: border-box !important;
          padding: 0 16px !important;
        }

        .ant-menu-submenu {
          // padding: 0 16px !important;
          .ant-menu-submenu-title {
            padding: 0 16px !important;
          }
        }
        .ant-menu-submenu-selected {
          .ant-menu-submenu-title {
            background: linear-gradient(270deg, #22194b, #006eff) !important;
          }
        }
      }
      .ant-menu-item {
        height: 50px;
        line-height: 50px;
        font-size: 14px;
        color: #c1c6c8 !important;
        &.ant-menu-item-selected {
          color: #fff !important;
          background: linear-gradient(270deg, #22194b, #006eff) !important;
        }
      }
    }
  }
</style>