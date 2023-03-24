<!--
 * @Author: hjg
 * @Date: 2021-11-09 16:00:05
 * @LastEditTime: 2022-07-20 15:59:38
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\configInfo\components\resourceConfig.vue
-->
<template>
  <div class="resource-config"
       v-loading="isLoading"
       v-defaultPage="!dataInfo || (dataInfo && Object.keys(dataInfo).length === 0)">
    <object-table :keyArrs="keyArrs"
                  :dataInfo="dataInfo" />
  </div>
</template>
<script>
  import objectTable from './objectTable.vue'
  export default {
    components: {
      objectTable
    },
    data () {
      return {
        isLoading: false,
        dataInfo: {},
        keyArrs: []
      }
    },
    props: {
      activeName: {
        type: String,
        value: ''
      }
    },
    watch: {
      activeName: {
        async handler (val) {
          if (val === 'resourceConfig') {
            this.init()
          }
        },
        immediate: true
      }
    },
    created () {

    },
    methods: {
      init () {
        this.getConfigInfo()
      },
      // 获取资源配置信息
      async getConfigInfo () {
        // console.log('jobInfo', this.$store.getters.jobInfo)
        let params = {
          confType: 'sourceConfig',
          vo: {
            id: this.$store.getters.jobInfo.id
          }
        }
        this.isLoading = true
        let res = await this.$http.post('/job/jobConf', params)
        this.isLoading = false
        if (res.code === 0) {
          this.dataInfo = res.data
          this.getObjectKeys()
        }
      },
      // 映射key的释义
      getObjectKeys () {
        for (let key in this.dataInfo) {
          let text = null
          let flag = true
          if (key === 'mode') {
            text = '配置模式'
            flag = true
          } else if (key === 'jobManagerCpu') {
            text = 'Job Manager CPUs'
            flag = true
          } else if (key === 'jobManagerMem') {
            text = 'Job Manager Memory'
            flag = true
          } else if (key === 'taskManagerCpu') {
            text = 'Task Manager CPUs'
            flag = true
          } else if (key === 'taskManagerMem') {
            text = 'Task Manager Memory'
            flag = true
          } else if (key === 'parallelism') {
            text = '并行度'
            flag = true
          } else {
            flag = false
          }
          if (flag) {
            let obj = {
              text: text,
              key: key,
              type: 'text'
            }
            this.keyArrs.push(obj)
          }
        }
      }
    }
  }
</script>
<style lang="scss" scoped>
  .resource-config {
    width: 100%;
    height: 100%;
    /deep/ .table-item {
      width: 870px;
      .left {
        width: 400px;
      }
      .right {
        width: 470px;
      }
    }
  }
</style>
