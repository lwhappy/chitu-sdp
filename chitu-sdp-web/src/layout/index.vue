<template>
  <div class="app-wrapper">
    <top :showProject="showProject" />
    <div class="main-container">
      <sidebar />
      <div class="main-views">
        <TagsView />
        <div class="layout-watermark"
             style="display:none;"></div>
        <app-main :hasError="hasError" />
      </div>
    </div>
  </div>
</template>

<script>
  import { Sidebar, AppMain, Top, TagsView } from './components'
  // BreadCrumb
  // import ResizeMixin from './mixin/ResizeHandler'

  export default {
    name: 'Layout',
    components: {
      Sidebar,
      AppMain,
      Top,
      TagsView
    },
    props: {
      hasError: { // 1是404 2是没权限访问
        type: Number
      }
    },
    computed: {
      //只有实时开发菜单栏才展示顶部项目栏
      showProject () {
        if (this.$route.path.indexOf('application') != 1) {
          return false
        }
        return true
      },
    },
    mounted () { },
    data () {
      return {
        menuWidth: 160
      }
    },
    //   mixins: [ResizeMixin],
    // computed: {
    //   sidebar () {
    //     return this.$store.state.app.sidebar
    //   }
    // },
    methods: {
      // handleClickOutside() {
      //   this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
      // }
    }
  }
</script>

