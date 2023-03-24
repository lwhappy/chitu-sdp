<!--
 * @Author: hjg
 * @Date: 2021-12-24 10:35:19
 * @LastEditTime: 2021-12-29 09:55:28
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\views\application\job-operate\components\configInfo\components\dsConfig.vue
-->
<template>
  <div class="ds-config"
       v-loading="isLoading">
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
          dsJarName: '',
          dsJarVersion: '',
          mainClass: '',
          git: '',
          url: '',
          description: ''
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
          if (val === 'dsConfig') {
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
        this.getDsConfigInfo()
      },
      // 获取DS配置信息
      async getDsConfigInfo () {
        // console.log('dsConfig', this.$store.getters.jobInfo)
        let params = {
          confType: 'dsConfig',
          vo: {
            id: this.$store.getters.jobInfo.id
          }
        }
        this.isLoading = true
        let res = await this.$http.post('/job/jobConf', params)
        this.isLoading = false
        if (res.code === 0) {
          this.dataInfo = res.data
          // console.log('ds info: ', this.dataInfo)
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
          if (key === 'jarName') {
            obj.text = '资源文件名称'
          } else if (key === 'jarVersion') {
            obj.text = '资源文件版本'
          } else if (key === 'mainClass') {
            obj.text = 'Main-Class'
          } else if (key === 'git') {
            obj.text = 'git地址'
            obj.type = 'url'
          } else if (key === 'url') {
            obj.text = '存储地址'
          } else if (key === 'description') {
            obj.text = '资源文件描述'
            obj.type = 'bigText'
          }
          if (key !== 'jarId') {
            this.keyArrs.push(obj)
          }
        }
        // console.log('keyArrs:', this.keyArrs)
      }
    }
  }
</script>
<style lang="scss" scoped>
  .ds-config {
    width: 100%;
    height: 100%;
    /deep/ .table-item {
      width: 600px;
      .left {
        width: 150px;
      }
      .right {
        width: 450px;
      }
    }
  }
</style>
