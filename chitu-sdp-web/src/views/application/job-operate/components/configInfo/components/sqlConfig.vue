<!--
 * @Author: hjg
 * @Date: 2021-11-09 15:58:38
 * @LastEditTime: 2021-11-15 14:38:19
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \bigdata-sdp-frontend2\src\views\application\job-operate\components\configInfo\components\sqlConfig.vue
-->
<template>
  <div class="sql-config"
       v-loading="isLoading"
       ref="sqlConfig"
       v-defaultPage="!sqlText"></div>
</template>
<script>
  import * as monaco from 'monaco-editor'
  export default {
    data () {
      return {
        isLoading: false,
        sqlText: null
      }
    },
    props: {
      activeName: {
        type: String,
        value: ''
      }
    },
    created () {

    },
    watch: {
      activeName: {
        async handler (val) {
          if (val === 'sqlConfig') {
            this.init()
          }
        },
        immediate: true
      }
    },
    methods: {
      init () {
        this.getConfigInfo()
      },
      // 获取SQL配置信息
      async getConfigInfo () {
        // console.log('jobInfo', this.$store.getters.jobInfo)
        let params = {
          confType: 'sqlConfig',
          vo: {
            id: this.$store.getters.jobInfo.id
          }
        }
        this.isLoading = true
        let res = await this.$http.post('/job/jobConf', params)
        this.isLoading = false
        if (res.code === 0) {
          this.sqlText = res.data.cont
          this.initEditor()
        }
      },
      initEditor () {
        this.editor = monaco.editor.create(this.$refs['sqlConfig'], {
          value: this.sqlText, // 编辑器初始显示文字
          language: 'sql', // 语言支持自行查阅demo
          automaticLayout: true, // 自动布局
          readOnly: true, // 只读
          theme: 'vs' // 官方自带三种主题vs, hc-black, or vs-dark
        })
      }
    }
  }
</script>
<style scoped>
  .sql-config {
    width: 99%;
    height: 99%;
  }
</style>
