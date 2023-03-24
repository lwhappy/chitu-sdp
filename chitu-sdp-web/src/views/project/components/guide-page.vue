<!--
 * @description: 引导页
 * @Author: lijianguo19
 * @Date: 2022-05-24 21:21:40
 * @FilePath: \src\views\project\components\guide-page.vue
-->
<template>
  <div class="guide-container">
    <div class="guide-main-container">
      <div class="header-container">
        <div class="header-left">
          <div class="title justify-start">
            <img src="@/assets/icons/guide-logo.png"
                 alt="">
            <span class="name">赤兔</span>
            <span class="line"></span>
            <span>实时计算平台</span>
          </div>
          <p class="descr">
            一站式流计算大数据开发平台，提供端到端亚秒级实时数据分析能力； 并通过标准SQL降低开发门槛，提高数开人员开发能力； 提高数据分析及处理效率。
          </p>
        </div>
        <div class="header-right">
          <div class="line-bg"> </div>
        </div>
      </div>
      <div class="content-container justify-start">
        <div class="content-left">
          <div class="project-container justify-center">
            <div class="icon pointer"
                 @click="goToProject">
              <img src="@/assets/icons/guide-project-img.png"
                   alt="">
              <p>项目管理</p>
            </div>
            <div class="list">
              <ul>
                <li class="pointer"
                    @click="goToApplication(item.path)"
                    v-for="item in applicationList"
                    :key="item.path">{{item.name}}</li>
              </ul>
            </div>
          </div>
          <div class="approve-container justify-center">
            <div class="icon pointer"
                 @click="goToApprove('approve')">
              <img src="@/assets/icons/guide-approve-img.png"
                   alt="">
              <p>实时作业流程</p>
            </div>
            <div class="list">
              <ul>
                <li class="pointer"
                    @click="goToApprove('apply')">我申请的</li>
                <li class="pointer"
                    @click="goToApprove('approve')">我审批的</li>
              </ul>
            </div>
          </div>
        </div>
        <div class="content-right">
          <div v-if="false"
               class="add-btn">
            <h3>开始</h3>
            <div class="add-item justify-start">
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>建设中...</span>
                </template>
                <img src="@/assets/icons/guide-add-icon.png"
                     alt="">
                <a-button type="link"
                          disabled>新增项目</a-button>
              </a-tooltip>
            </div>
          </div>
          <div class="approve-list"
               v-loading="loading">
            <h3>最近作业</h3>
            <ul>
              <li>
                <span>作业名称</span>
                <span>项目名称</span>
                <!-- <span>更新人</span> -->
                <span>更新时间</span>
              </li>
              <div class="approve-list_item">
                <li v-for="(item, index) in jobList"
                    :key="index"
                    @click="gotoJob(item)">
                  <!-- <span>{{ item.fileName }}</span> -->
                  <a-tooltip placement="topLeft">
                    <template slot="title">
                      <span>{{ item.fileName }}</span>
                    </template>
                    <span>
                      <div class="file-name">
                        {{ item.fileName }}
                      </div>
                    </span>
                  </a-tooltip>
                  <span class="icon_click"><img src="@/assets/icons/fold-new.png" />{{ item.projectName }}</span>
                  <span>{{ item.updationDate }}</span>
                </li>
                <div class="empty-text"
                     v-if="jobList.length === 0">
                  <img src="/svg/default-img.png" />
                  <p>
                    暂无数据
                  </p>
                </div>
              </div>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: '',
    components: {},
    props: {},
    data () {
      return {
        jobList: [],
        loading: false,
        applicationList: [
          {
            name: '作业开发',
            path: "/application/job-develop"
          },
          {
            name: '作业运维',
            path: "/application/job-operate",
          },
          {
            name: '资源管理',
            path: "/application/source-manage",
          },
          {
            name: '数据源管理',
            path: "/application/data-source-manage",
          }
        ],
      };
    },
    computed: {},
    watch: {},
    created () {
      this.getHistoryJobList()
    },
    mounted () {
      // this.setZoom()
      // window.onresize = () => {
      //   this.setZoom()
      // }
    },
    destroyed () {
      document.body.style.zoom = 1
    },
    methods: {
      setZoom () {
        let browserRatio = this.detectZoom()
        if (browserRatio !== 1) {
          // this.zoom = -0.6 * this.browserRatio + 1.6
          let zoom = 1 / browserRatio * 1.25
          document.body.style.zoom = zoom
        }
      },
      detectZoom () {
        let ratio = 0
        let screen = window.screen
        let ua = navigator.userAgent.toLowerCase()
        if (window.devicePixelRatio !== undefined) {
          ratio = window.devicePixelRatio
        } else if (~ua.indexOf('msie')) {
          if (screen.deviceXDPI && screen.logicalXDPI) {
            ratio = screen.deviceXDPI / screen.logicalXDPI
          }
        } else if (window.outerWidth !== undefined && window.innerWidth !== undefined) {
          ratio = window.outerWidth / window.innerWidth
        }
        return ratio
      },
      // 获取用户更新作业列表
      async getHistoryJobList () {
        this.loading = true
        let res = await this.$http.post('/file/fileHistory')
        if (res.code === 0) {
          this.jobList = res.data
        }
        this.loading = false
      },
      // 项目管理
      goToProject () {
        this.$router.push({
          name: 'Project',
          query: {//预留query
            // version: 1
          }
        })
      },
      // 作业开发
      goToApplication (path) {
        //读取缓存项目
        const projectInfo = this.$store.getters.currentProject
        if (projectInfo) {
          let projectId, projectCode, projectName = ''
          projectId = projectInfo.id
          projectCode = projectInfo.code
          projectName = projectInfo.name
          if (!projectId || !projectName) {
            this.$message.error({ content: '您还没有参与的实时项目，请前往【项目管理】参与项目或创建项目', duration: 2 })
            return
          }
          this.$router.push({
            path,
            query: {
              projectId,
              projectName,
              projectCode
            }
          })
        } else {
          this.$message.error({ content: '您还没有参与的实时项目，请前往【项目管理】参与项目或创建项目', duration: 2 })

        }


      },
      // 实时作业流程
      goToApprove (key) {
        this.$router.push({
          name: 'ApproveList',
          query: {//预留query
            active: key
          }
        })
      },
      gotoJob (item) {
        this.$router.push({
          name: 'JobDevelop',
          query: {//预留query
            projectId: item.projectId,
            projectName: encodeURIComponent(item.projectName),
            fileId: item.id,
            folderId: item.folderId
          }
        })
      }
    },
  }
</script>
<style lang='scss' scoped>
  .guide-container {
    width: 100%;
    height: 100%;
    background: #f5f7fa;
    .guide-main-container {
      height: 100%;
      position: relative;
      padding: 16px;
      .header-container {
        height: 264px;
        width: 100%;
        border-radius: 4px;
        display: flex;
        flex-direction: row;
        background: url(~@/assets/icons/guide-bg.png) no-repeat;
        background-size: 100% 100%;
        .header-left {
          margin-left: 32px;
          padding-top: 42px;
          color: #ffffff;
          width: 600px;
          .title {
            margin-top: 30px;
            font-size: 30px;
            img {
              margin-top: -20px;
            }
            .name {
              margin: 0 16px;
              font-weight: 600;
            }
            .line {
              height: 30px;
              width: 1px;
              background: #7c7c8c;
              margin-right: 16px;
            }
          }
          .descr {
            width: 465px;
            font-size: 14px;
            margin-top: 20px;
            letter-spacing: 1px;
          }
        }
        .header-right {
          width: 100%;
          position: relative;
          .line-bg {
            width: 839px;
            height: 240px;
            position: absolute;
            top: 20px;
            left: 0;
            background: url(~@/assets/icons/guide-line-bg.png) no-repeat;
            background-size: 90%;
          }
        }
      }
      .content-container {
        padding-top: 16px;
        height: calc(100% - 262px);
        width: 100%;
        .content-left {
          width: 359px;
          height: 100%;
          padding: 16px;
          box-sizing: border-box;
          background: linear-gradient(180deg, #ffffff, #ffffff);
          border-radius: 4px;
          box-shadow: 0px 15px 20px 0px rgba(147, 161, 187, 0.15);
          .project-container {
            width: 100%;
            height: 48%;
            background: linear-gradient(360deg, #e0f0ff 0%, #f5fcff 98%);
            border-radius: 4px;
            li:hover {
              color: #0066ff;
            }
          }
          .approve-container {
            width: 100%;
            height: 48%;
            margin-top: 16px;
            background: linear-gradient(360deg, #dbddf2, #f9f8ff 98%);
            border-radius: 4px;
            li:hover {
              color: #0066ff;
            }
          }
          .icon {
            width: 50%;
            text-align: center;
            &:hover p {
              color: #0066ff;
            }
            img {
              width: 108px;
            }
            p {
              font-size: 14px;
              font-weight: 700;
              margin-top: 10px;
              color: #2c2f37;
            }
          }
          .list {
            width: 50%;
            box-sizing: border-box;
            padding-left: 30px;
            li {
              font-size: 14px;
              margin: 18px 0;
              color: #2c2f37;
            }
          }
        }
        .content-right {
          margin-left: 16px;
          flex: 1;
          height: 100%;
          padding: 16px 24px;
          box-sizing: border-box;
          border-radius: 4px;
          background: #ffffff;
          box-shadow: 0px 15px 20px 0px rgba(147, 161, 187, 0.15);
          .add-btn {
            h3 {
              font-size: 14px;
              font-weight: 700;
              text-align: left;
              color: #2c2f37;
              line-height: 19px;
            }
            .add-item {
              margin: 21px 0 0 76px;
              font-size: 14px;
              color: #b3bdcf;
              .ant-btn {
                padding: 0 8px;
              }
              img {
                width: 16px;
              }
            }
          }
          .approve-list {
            margin-top: 32px;
            height: calc(100% - 104px);
            h3 {
              font-size: 14px;
              font-weight: 700;
              text-align: left;
              color: #2c2f37;
              line-height: 19px;
            }
            ul {
              padding: 0px 54px;
              font-size: 12px;
              height: calc(100% - 20px);
            }
            .approve-list_item {
              height: calc(100% - 40px);
              overflow-y: auto;
              position: relative;
              li {
                cursor: pointer;
              }
              li:hover {
                background: #ffffff;
                border-radius: 4px;
                box-shadow: 0px 0px 20px 0px rgba(147, 161, 187, 0.2);
              }
              .empty-text {
                padding: 30px 27% 0 0;
                text-align: center;
                img {
                  width: 90px;
                }
                p {
                  font-size: 12px;
                  color: #667082;
                }
              }
            }
            ul li {
              height: 40px;
              line-height: 40px;
              display: table;
              table-layout: fixed;
              width: 100%;
              padding-left: 23px;
              color: #2c2f37;

              span {
                display: table-cell;
              }
              .file-name {
                width: 90%;
                text-overflow: ellipsis;
                white-space: nowrap;
                overflow: hidden;
              }
              .icon_click {
                img {
                  display: inline-block;
                  padding-right: 8px;
                  height: 16px;
                  vertical-align: middle;
                }
                span {
                  line-height: 16px;
                }
                color: #006fff;
              }
            }
          }
        }
      }
    }
  }
</style>