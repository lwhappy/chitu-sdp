<!--
 * @Author: hjg
 * @Date: 2021-12-24 15:12:54
 * @LastEditTime: 2022-07-13 14:22:58
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\application\job-develop\components\DS.vue
-->
<template>
  <div class="ds-container">
    <template v-if="!errorData">
      <div class="ds-default"
           v-if="!dagViewVisible">
        <img v-show="!loading"
             class="sdp-failure"
             src="@/assets/icons/sdp-failure.png" />
        <img v-show="loading"
             class="sdp-loading"
             src="@/assets/icons/sdp-loading.gif" />
        <p v-show="loading">当前作业DAG图，拼命加载中...</p>
        <p v-show="!loading">DAG加载失败了，
          <span class="re-send create"
                @click="getDagInfo">请点击重试</span>
        </p>

      </div>
      <div class="ds-dag-view">
        <dag-view v-if="dagViewVisible"
                  :nodes="nodes"
                  :fileDetail="fileDetail"
                  @change="reGenerate" />

      </div>
    </template>
    <template v-else>
      <!-- 重新生成dag图 -->
      <div class="re-generatedag"
           @click="reGenerate">
        <i class="chitutree-h5 chitutreeshuaxin"></i>
        重新生成DAG图
      </div>
      <div class="error">{{errorData}}</div>
    </template>
  </div>
</template>

<script>
  import dagView from './dag-view.vue'
  export default {
    name: "DS",
    props: {
      fileDetail: {
        type: Object,
        default: () => {
          return {}
        }
      }
    },
    data () {
      return {
        loading: true,
        dagViewVisible: false,
        nodes: null,
        erroMsg: null,
        errorData: ''
      }
    },
    computed: {

    },
    components: {
      dagView
    },
    created () { },
    methods: {
      create () {
        this.dagViewVisible = true
        this.$forceUpdate()
      },
      // 获取dag信息
      async getDagInfo () {
        this.errorData = ''
        this.loading = true
        const params = {
          id: this.fileDetail.id,
          dataStreamConfig: this.fileDetail.dataStreamConfig
        }
        let res = await this.$http.post('/file/getJobDAG', params, {
          headers: {
            projectId: this.fileDetail.projectId
          }
        })
        this.loading = false
        if (res.code === 0) {
          if (res.data !== '') {
            this.dagViewVisible = true
            try {
              this.nodes = JSON.parse(res.data)
            } catch (e) {
              this.dagViewVisible = false
              this.errorData = res.data
              this.nodes = null
            }

          }
        } else {

          this.dagViewVisible = false
          return res.msg && this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      reGenerate () {
        this.dagViewVisible = false
        this.getDagInfo()
      }
    },
    mounted () {
      this.getDagInfo()
    }
  }
</script>
<style lang="scss" scoped>
  .ds-container {
    width: 100%;
    height: 100%;
    overflow: hidden;
    .error {
      padding: 10px 50px 0 10px;
      position: relative;
    }
    .re-generatedag {
      position: absolute;
      top: 2px;
      right: 38px;
      color: #0066ff;
      z-index: 9;
      cursor: pointer;
      text-align: center;
      width: 130px;
      height: 40px;
      line-height: 40px;
      background: rgba(255, 255, 255, 0.8);
      border-radius: 2px;
      box-shadow: 0 2px 6px 0 rgba(165, 161, 176, 0.2);
    }
    .ds-default {
      width: 400px;
      text-align: center;
      margin: 0 auto;
      padding-top: 200px;
      height: 300px;
      .sdp-failure {
        width: 148px;
        height: 104px;
      }
      .sdp-loading {
        width: 80px;
        height: 80px;
      }
      .create {
        cursor: pointer;
      }
      .re-send {
        color: #0066ff;
      }
      img {
        margin-bottom: 10px;
      }
    }
    .ds-dag-view {
      width: 100%;
      height: 100%;
      overflow: hidden;
    }
  }
</style>