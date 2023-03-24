<template>
  <div ref="monacoAn"
       class="monaco"></div>
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
          autoIndent: true, // 自动缩进
          readOnly: true, // 是否只读
          language: 'sql', // 语言
          automaticLayout: true
        },
        value: null
      }
    },
    methods: {
      // 设置编辑器
      setEditor (language, value) {
        console.log('hhhhhhhhhhhhhhh')
        // setEditor() {
        // 初始化container的内容，销毁之前生成的编辑器
        this.$refs.monacoAn.innerHTML = ''
        this.defaultOpts.language = language
        // 初始化编辑器实例
        this.monacoDiffInstance = monaco.editor.create(
          this.$refs["monacoAn"], {
          value: value,
          language: language,
          formatOnType: true,
          readOnly: true,
          automaticLayout: true
        })
      },
      clearEditor () {
        this.$refs.monacoAn.innerHTML = ''
      }
    }
  }
</script>
<style lang="scss" scoped>
  .container {
    width: 100%;
    height: 100%;
    overflow: hidden;
    .monaco {
      height: 100%;
    }
  }
</style>
