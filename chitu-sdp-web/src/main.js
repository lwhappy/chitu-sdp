/*
 * @Author: your name
 * @Date: 2021-10-15 11:08:00
 * @LastEditTime: 2022-10-10 10:09:00
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\main.js
 */
console.log('process.env.NODE_ENV', process.env.NODE_ENV)
import Vue from 'vue'
import lodash from 'lodash'//引入lodash防抖函数
import App from './App.vue'
import router from './router'
import store from './store'
import { api } from '@/api/http.js'
import bus from './utils/bus'
import pattern from './utils/pattern'
import common from './utils/common'
import './utils/element' //按需引入element-ui
import 'ant-design-vue/dist/antd.less'; // 引入官方提供的 less 样式入口文件
import Antd from 'ant-design-vue';
import '@/styles/ant.less' // 全局css
import '@/styles/index.scss' // 全局css
import "@/assets/iconfont/iconfont.css";//引入本地iconfont文件

import log from '@/utils/log'
// 注册全局指令
import pullableDirective from '@/directives/chitu-pullable'
import '@/directives/chitu-drag'
// 注册 components下chitu开头的所有的组件
import { initGlobalComponents } from '@/utils/index.js'
// 注册dataHub components下chitu开头的所有的组件
import '@/directives/chitu-pullable.scss'

Vue.directive('pullable', pullableDirective)
initGlobalComponents(Vue)
import * as filters from '@/plugin/filters.js'
import Contextmenu from '@/plugin/contextmenu/index.js' //https://github.com/GitHub-Laziji/menujs 在基础上改的 右键菜单
import VueDraggableResizable from 'vue-draggable-resizable'
import '@/mock/index'
Vue.component('vue-draggable-resizable', VueDraggableResizable)
Vue.use(Contextmenu);
Vue.use(Antd);
Vue.config.productionTip = false

Object.keys(filters).forEach(key => {
  Vue.filter(key, filters[key])
})
Vue.prototype.$lodash = lodash
Vue.prototype.$log = log
Vue.prototype.$bus = bus
Vue.prototype.$http = api
Vue.prototype.$reg = pattern
Vue.prototype.$common = common

// initGlobalComponents(Vue) //注入components下chitu开头的所有的组件 
// 注册一个全局自定义指令 `v-loading`

// 引入loading
import loading from '@/directives/loading'
Vue.use(loading);

// 注册一个全局自定义指令 `v-defaultPage` 缺省页
Vue.directive('defaultPage', {
  bind: function (el, binding) {
    if (!['relative', 'absolute', 'fixed'].includes(el.style.position)) {
      el.style.position = 'relative'
    }
    if (el.querySelector('.global-default-page')) {
      if (binding.value) {
        el.querySelector('.global-default-page').style.display = 'block'
      } else {
        el.querySelector('.global-default-page').style.display = 'none'
      }
      return
    }

    const display = binding.value ? 'block' : 'none'
    const wrapper = document.createElement('div')
    wrapper.setAttribute('class', 'global-default-page')
    wrapper.style.display = display
    let html = ''
    html += `<div class="default-page-spinner">`
    html += `<div class="default-img" ></div><p class="text">暂无数据</p>`
    html += `</div>`
    wrapper.innerHTML = html
    el.appendChild(wrapper)
  },
  update (el, binding) {
    if (binding.value) {
      el.querySelector('.global-default-page').style.display = 'block'
    } else {
      el.querySelector('.global-default-page').style.display = 'none'
    }
  }
})

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')

window.onbeforeunload = function () {
  if (window.__intercept__ === 0) {
    return null
  }
  return false
}
