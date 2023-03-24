<!--
 * @Author: hjg
 * @Date: 2021-11-09 16:03:47
 * @LastEditTime: 2022-07-15 10:14:17
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\configInfo\components\jarConfig.vue
-->
<template>
  <div class="jar-config"
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
          if (val === 'jarConfig') {
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
      // 获取jar配置信息
      async getConfigInfo () {
        // console.log('jobInfo', this.$store.getters.jobInfo)
        let params = {
          confType: 'jarConfig',
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
          let type = 'text'
          if (key === 'name') {
            text = 'jar名称'
            flag = true
          } else if (key === 'version') {
            text = 'jar版本'
            flag = true
          } else if (key === 'description') {
            text = 'jar描述'
            flag = true
          } else if (key === 'git') {
            text = 'git地址'
            type = 'url'
            flag = true
          } else if (key === 'url') {
            text = '存储地址'
            flag = true
          } else if (key === 'jobs') {
            text = '作业数'
            flag = true
          } else {
            flag = false
          }
          if (flag) {
            let obj = {
              text: text,
              key: key,
              type: type
            }
            this.keyArrs.push(obj)
          }
        }
      }
    }
  }
</script>
<style lang="scss" scoped>
  .jar-config {
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
