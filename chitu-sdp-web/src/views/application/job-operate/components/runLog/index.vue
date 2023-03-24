<!--
 * @Author: hjg
 * @Date: 2021-11-08 21:30:17
 * @LastEditTime: 2022-10-20 10:53:47
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\runLog\index.vue
-->
<template>
  <div class="run-log"
       v-loading="isLoading"
       v-defaultPage="!logContent">
    <div ref="logContent"
         class="log-content"
         :class="{'black':logContent}">
      {{ filterContent(logContent) }}
    </div>
  </div>
</template>
<script>
  import $ from 'jquery'
  export default {
    data () {
      return {
        isLoading: false,
        logContent: '', // 日志内容
        timer: null,
        height: null,
        flag: true,
        time: 5000,
        latestTime: null // 最后更新时间,用于给后台判断是否有新的日志需要返回给前端显示于页面
      }
    },
    props: {
      activeName: {
        type: String,
        value: ''
      }
    },
    mounted () {
    },
    watch: {
      activeName: {
        async handler (val) {
          if (val === 'runLog') {
            this.clear()
            this.init()
          } else {
            this.clear()
          }
        },
        immediate: true
      }
    },
    methods: {
      init () {
        this.getRunLog('init')
        this.timerFunc()
      },
      clear () {
        clearInterval(this.timer)
        this.timer = null
      },
      // 内容过滤超链接
      filterContent (text) {
        if (text === null) return
        let bIndexArr = []
        let eIndexArr = []
        let bStrIndexArr = []
        let eStrIndexArr = []
        let urlArr = []
        let bIndex = text.indexOf('【')
        let eIndex = text.indexOf('】')
        let url = '【' + text.substring(bIndex + 1, eIndex) + '】'
        urlArr.push(url)
        bIndexArr.push(bIndex)
        eIndexArr.push(eIndex)

        let strUrlArr = []
        let bStrIndex = text.indexOf('《')
        let eStrIndex = text.indexOf('》')
        // // console.log('bStrIndex: ', bStrIndex, ' eStrIndex: ', eStrIndex)
        let strUrl = '《' + text.substring(bStrIndex + 1, eStrIndex) + '》'
        // // console.log('url:', url)
        // // console.log('bIndex: ', bIndex, ' eIndex: ', eIndex, ' url: ', url)
        strUrlArr.push(strUrl)
        bStrIndexArr.push(bStrIndex)
        eStrIndexArr.push(eStrIndex)
        while (bIndex != -1) {
          bIndex = text.indexOf('【', bIndex + 1)
          bIndexArr.push(bIndex)
          eIndex = text.indexOf('】', eIndex + 1)
          eIndexArr.push(eIndex)
          url = '【' + text.substring(bIndex + 1, eIndex) + '】'
          urlArr.push(url)

          bStrIndex = text.indexOf('《', bStrIndex + 1)
          eStrIndex = text.indexOf('》', eStrIndex + 1)
          strUrl = '《' + text.substring(bStrIndex + 1, eStrIndex) + '》'
          strUrlArr.push(strUrl)
          bStrIndexArr.push(bStrIndex)
          eStrIndexArr.push(eStrIndex)
        }
        // // console.log('------urlArr: ', urlArr)
        bIndexArr = bIndexArr.filter(x => x > 0)
        eIndexArr = eIndexArr.filter(x => x > 0)
        // let indexArr = bIndexArr.concat(eIndexArr).sort((a, b) => a - b)
        let strArr = []
        let index = 0
        bStrIndexArr = bStrIndexArr.filter(x => x > 0)
        eStrIndexArr = eStrIndexArr.filter(x => x > 0)
        let strIndexArr = bStrIndexArr.concat(eStrIndexArr).sort((a, b) => a - b)
        let urlArrIndex = 0
        for (let i = 0; i < strIndexArr.length; i++) {
          let str = text.substr(index, strIndexArr[i] - index + 1)
          if (strUrlArr.filter(item => item === str).length > 0) {
            let content = str.substr(1, str.length - 2)
            // // console.log('urlArr:', urlArrIndex, urlArr[urlArrIndex])
            let linkUrl = urlArr[urlArrIndex].substr(1, urlArr[urlArrIndex].length - 1)
            linkUrl = linkUrl.replace('【', '')
            linkUrl = linkUrl.replace('】', '')
            str = `<a class="link" attribute-href="${linkUrl}" >${content}</a>`
            urlArrIndex++
          }
          strArr.push(str)
          index = strIndexArr[i]
        }
        strArr.push(text.substr(strIndexArr[strIndexArr.length - 1]))
        // // console.log('strArr: ', strArr)
        text = strArr.join('')
        text = text.replace(/【.*?】/g, '').replace(/《/g, '').replace(/》/g, '')
        this.$nextTick(() => { // 保证ref挂载
          // // console.log('refs: ', this.$refs)
          this.$refs['logContent'].innerHTML = text
          if (this.flag) this.$refs['logContent'].scrollTop = this.$refs['logContent'].scrollHeight - this.height - 10
          this.linkEvent()
        })
      },
      timerFunc () {
        this.timer = setInterval(() => {
          this.getRunLog()
        }, this.time)
      },
      // 获取运行日志
      async getRunLog (init) {
        let params = {
          jobId: this.$store.getters.jobInfo.id,
          latestTime: this.latestTime
        }
        if (init) {
          this.isLoading = true
        }
        let res = await this.$http.post('/runtimeLog/queryRuntimeLog', params)
        // // console.log('--------res: ', res)
        this.isLoading = false
        if (res.code === 0) {
          this.logContent = res.data.logs
          this.latestTime = res.data.latestTime
          this.$nextTick(() => {
            if (this.flag) this.$refs['logContent'].scrollTop = this.$refs['logContent'].scrollHeight - this.height - 10

          })


        } // 运行日志无更新
      },
      linkEvent () {
        $('.log-content .link').unbind('click').bind('click', (e) => {
          e.stopPropagation()
          const content = $(e.target).attr('attribute-href')
          //生产域名下生产逻辑环境：支持复制功能
          if (window.location.hostname === 'xxx') {
            if (['prod'].includes(this.$store.getters.env)) {
              this.$common.copyContent(content)
              this.$message.success({ content: '复制成功，请前往堡垒机查看详情', duration: 2 })
            } else {
              window.open(content, '_blank')
            }
          }
          // dev域名或uat域名:只有状态转移测试、引擎引用测试这两个项目支持复制功能
          else {
            const projectName = decodeURIComponent(decodeURIComponent(this.$route.query.projectName))
            if (['状态转移测试', '引擎引用测试'].includes(projectName)) {
              this.$common.copyContent(content)
              this.$message.success({ content: '复制成功，请前往堡垒机查看详情', duration: 2 })
            } else {
              window.open(content, '_blank')
            }
          }
        })
      }
    },
    beforeDestroy () {
      // window.stop()
      this.clear()
    },

  }
</script>
<style lang="scss" scoped>
  .run-log {
    width: 100%;
    height: 100%;
    .log-content {
      width: 100%;
      height: 100%;
      white-space: pre-wrap;
      padding: 4px;
      line-height: 28px;
      overflow-y: scroll;
      &.black {
        background-color: #282923;
        color: #fff;
        border: 1px solid #333;
      }
      .time {
        width: 50px;
      }
      /deep/ a {
        color: #0066ff;
        text-decoration: underline;
      }
    }
  }
</style>
