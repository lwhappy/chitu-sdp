<!--
 * @Author: hjg
 * @Date: 2021-11-09 16:03:47
 * @LastEditTime: 2022-07-15 10:14:33
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\configInfo\components\runConfig.vue
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
          if (val === 'runConfig') {
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
        this.isLoading = true
        let res = await this.$http.get(`/job/queryRunningConfig?jobId=${this.$store.getters.jobInfo.id}`, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
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
          if (key === 'env') {
            text = '运行环境'
            flag = true
          } else if (key === 'runTimeSetting') {
            text = '运行时长设置'
            flag = true
          } else if (key === 'executeDuration') {
            text = '已运行时长'
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
