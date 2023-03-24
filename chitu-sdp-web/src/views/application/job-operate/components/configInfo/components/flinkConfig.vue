<!--
 * @Author: hjg
 * @Date: 2021-11-09 16:01:38
 * @LastEditTime: 2022-07-15 10:12:58
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\configInfo\components\flinkConfig.vue
-->
<template>
  <div class="flink-config"
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
        dataInfo: {
          availabilty: 'vvp-kubemetes',
          checkpointion: 'RETAIN_ON_CANCELL ATION',
          port: '999',
          class: 'org.apache.flink.metrics.prometheus.Promethus REporter'
        },
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
          if (val === 'flinkConfig') {
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
        this.getJobConfigInfo()
      },
      // 获取flink配置信息
      async getJobConfigInfo () {
        // console.log('jobInfo', this.$store.getters.jobInfo)
        let params = {
          confType: 'flinkConfig',
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
          let obj = {
            text: key,
            key: key,
            type: 'text'
          }
          this.keyArrs.push(obj)
        }
        // console.log('keyArrs:', this.keyArrs)
      }
    }
  }
</script>
<style lang="scss" scoped>
  .flink-config {
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
