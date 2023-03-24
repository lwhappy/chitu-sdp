/*
 * @description: 
 * @Author: lijianguo19
 * @Date: 2022-08-11 15:39:28
 * @FilePath: \src\directives\loading\loading.js
 */
import Vue from 'vue';
import Load from './loading.vue';

const Mask = Vue.extend(Load);

const toggleLoading = (el, binding) => {
    if (binding.value) {
        Vue.nextTick(() => {
            el.instance.visible = true// 控制loading组件显示
            insertDom(el, el, binding)// 插入到目标元素
        })
    } else {
        el.instance.visible = false
    }
}

const insertDom = (parent, el) => {
    parent.appendChild(el.mask)
}

export default {
    // bind(){}当绑定指令的时候出发
    bind: function (el, binding) {
        const mask = new Mask({
            el: document.createElement('div'),
            data () { }
        })
        el.instance = mask
        el.mask = mask.$el
        el.maskStyle = {}
        binding.value && toggleLoading(el, binding)
    },
    // update(){}当数据更新时候会触发该函数
    update: function (el, binding) {
        if (binding.oldValue !== binding.value) {
            toggleLoading(el, binding)
        }
    },
    // unbind(){}解绑的时候触发该函数
    unbind: function (el) {
        el.instance && el.instance.$destroy()
    }
}