<!--
 * @Author: hjg
 * @Date: 2021-11-11 18:49:04
 * @LastEditTime: 2022-01-06 12:12:18
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\components\job-version\compareEditor.vue
-->
<template>
  <div ref="compareEditor"
       class="compareEditor"></div>
</template>
<script>
  import * as monaco from 'monaco-editor'
  export default {
    props: {
      height: {
        type: Number,
        default: 600
      }
    },
    data () {
      return {
        defaultOpts: { // 主要配置
          value: '', // 编辑器的值
          theme: 'vs', // 编辑器主题：vs, hc-black, or vs-dark，更多选择详见官网
          roundedSelection: true, // 右侧不显示编辑器预览框
          autoIndent: true, // 自动缩进
          readOnly: true, // 是否只读
          language: 'sql', // 语言
          automaticLayout: true
        },
        oldValue: null,
        newValue: null
      }
    },
    methods: {
      // 设置编辑器
      setEditor (language, oldValue, newValue) {
        // setEditor() {
        // 初始化container的内容，销毁之前生成的编辑器
        this.$refs.compareEditor.innerHTML = ''
        this.defaultOpts.language = language
        // 初始化编辑器实例
        this.monacoDiffInstance = monaco.editor.createDiffEditor(this.$refs['compareEditor'], this.defaultOpts)
        this.oldValue = oldValue
        this.newValue = newValue
        this.monacoDiffInstance.setModel({
          // oldValue为以前的值
          original: monaco.editor.createModel(this.oldValue, this.defaultOpts.language),
          // oldValue为新的值
          modified: monaco.editor.createModel(this.newValue, this.defaultOpts.language)
        })
      },
      clearEditor () {
        this.$refs.compareEditor.innerHTML = ''
      }
    }
  }
</script>
<style lang="scss" scoped>
  .container {
    width: 100%;
    height: 100%;
    overflow: hidden;
    .compareEditor {
      height: 100%;
      /deep/ .monaco-diff-editor .diffViewport {
        background: none;
      }
      // /deep/ .diffOverview {
      //   background: #fff;
      //   display: none;
      //   .diffViewport {
      //     background: #fff;
      //     canvas {
      //       display: none;
      //     }
      //   }
      // }
      // /deep/ .vs {
      //   width: calc(100% + 10px);
      // }
    }
  }
</style>
