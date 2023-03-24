<template>
  <div class="main-board-container">
    <!-- 头部 -->
    <div class="board-top justify-between">
      <div class="left justify-start">
        <a-button type="link"
                  @click="addFile"><i class="chitutree-h5 chitutreexinjian"></i>新建</a-button>

        <a-button type="link"
                  :disabled="activePane.contentData && activePane.contentData.lockSign!==0?false:true"
                  @click="clickSave()"><i class="chitutree-h5 chitutreebaocun"></i>保存</a-button>
        <template v-if="activePane.contentData && activePane.contentData.fileType === 'SQL'">
          <a-button v-if="activeKey"
                    type="link"
                    :disabled="activePane.contentData &&  activePane.contentData.lockSign!==0?false:true"
                    @click="handleUndo()"><i class="chitutree-h5 chitutreechexiao"></i>撤销</a-button>
          <a-button v-if="activeKey"
                    type="link"
                    :disabled="activePane.contentData &&  activePane.contentData.lockSign!==0?false:true"
                    @click="handleRedo()"><i class="chitutree-h5 chitutreezhongzhi"></i>恢复</a-button>
          <a-button v-if="activeKey"
                    type="link"
                    :disabled="activePane.contentData &&  activePane.contentData.lockSign!==0?false:true"
                    @click="handleFind()"><i class="chitutree-h5 chitutreechaxun"></i>查找</a-button>
          <a-button v-if="activeKey"
                    type="link"
                    :disabled="activePane.contentData &&  activePane.contentData.lockSign!==0?false:true"
                    @click="handleBeauty()"><i class="chitutree-h5 chitutreegeshihua"></i>格式化</a-button>
        </template>
      </div>
      <div class="right justify-end">
        <a-button type="link"
                  class="change-env-container"
                  v-show="isUatEnv"
                  :loading="isChangeEnv"
                  :disabled="activePane.contentData && activePane.contentData.lockSign!==0?false:true"
                  @click="changeEnv"><i class="chitutree-h5 chitutreeicon_zhuanshengchan"></i>转生产</a-button>
        <template v-if="activePane.contentData && activePane.contentData.fileType === 'SQL'">
          <a-button type="link"
                    :loading="isVerifying"
                    :disabled="activePane.contentData && activePane.contentData.lockSign!==0?false:true"
                    @click="handleVerify(false)"><i class="chitutree-h5 chitutreeyanzheng"></i>验证</a-button>
        </template>

        <a-button type="link"
                  :loading="isOnLineing"
                  :disabled="activePane.contentData && activePane.contentData.lockSign!==0?false:true"
                  @click="handlePublishVerify"><i class="chitutree-h5 chitutreejuxing"></i>{{isUatEnv?'发布至UAT':'发布至生产'}} </a-button>
      </div>
    </div>
    <!-- end -->
    <!-- 中部 -->
    <div ref="boardMain"
         id="boardMain"
         class="board-main"
         v-loading="isLoading">
      <!-- :style="{height: tabHeight + 'px'}" -->
      <div class="board-tab"
           v-show="panes.length > 0">
        <a-tabs v-model="activeKey"
                :animated="false"
                @change="changeTab">
          <a-tab-pane v-for="(item,index) in panes"
                      :key="item.id">
            <span slot="tab">
              <div class="tab-content justify-start"
                   @contextmenu.prevent="openMenu(item.id,index,$event)">
                <template v-if="item.contentData && item.contentData.fileType === 'SQL'">
                  <i v-if="item.contentData.isOnLine === 0"
                     class="chitutree-h5 chitutreeSQL"></i>
                  <i v-if="item.contentData.isOnLine === 1"
                     class="chitutree-h5 chitutreeSQL green"></i>
                </template>
                <template v-else-if="item.contentData && item.contentData.fileType === 'DS'">
                  <i v-if="item.contentData.isOnLine === 0"
                     class="chitutree-h5 chitutreeDS"></i>
                  <i v-if="item.contentData.isOnLine === 1"
                     class="chitutree-h5 chitutreeDS cyanine"></i>
                </template>
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{item.title}}</span>
                  </template>
                  <span class="tab-title">{{item.title}}</span>
                </a-tooltip>
                <i class="chitutree-h5 chitutreeguanbi remove"
                   @click.stop.prevent="removeTab(item.id,index)"></i>
              </div>
            </span>
            <div style="position:relative;">
              <ul v-show="tabVisible"
                  :style="{left:left+'px',top:'0px'}"
                  class="context-menu">
                <li @click="removeTab(selectedId,selectedIndex)">关闭</li>
                <li @click="removeLeftTab(selectedIndex)">关闭左侧</li>
                <li @click="removeRightTab(selectedIndex)">关闭右侧</li>
                <li @click="closeOthersTab(selectedId,selectedIndex)">关闭其它</li>
              </ul>
            </div>
            <tab-content :ref="'tabContent'+item.id"
                         :contentData="item.contentData"
                         @save="handleSave" />
          </a-tab-pane>
        </a-tabs>
      </div>
      <div class="board-msg"
           v-show="panes.length === 0">
        <div class="borad-msg-content">
          <img src="@/assets/icons/default-img.png"
               alt="暂无数据">
          <div class="text">
            暂无数据，请双击左侧目录树作业名称进行编辑
          </div>
        </div>
      </div>
    </div>
    <!-- 暂时关闭提示 -->
    <!-- <div class="history-alert"
         v-if="isHistoryFiles">
      <a-alert show-icon
               description="直接在生产环境开发存在一定风险，建议下线此任务,在UAT环境创建作业进行开发后转至生产环境~"
               type="warning"
               closable
               @close="isHistoryFiles = false" />
    </div> -->
    <!-- end -->
    <div v-if="activePane.contentData && (activePane.contentData.fileType === 'SQL' || activePane.contentData.fileType === 'DS')"
         ref="boardBot"
         class="board-bot justify-start">
      <div class="question"
           @click="showQuestion">
        <p class="justify-between">
          <span class="name"
                :class="{'light':question && question.length}"> <i class="chitutree-h5 chitutreewenti"></i>问题<template v-if="question && question.length">&nbsp;({{question.length}})</template>
          </span>
          <span class="arrow"><i class="chitutree-h5 chitutreejiantouzhankai"></i>展开</span>
        </p>
      </div>
      <div v-if="isShowQuestion"
           class="question-board"
           id="question-board">
        <h3 class="justify-between">
          <span class="name"
                :class="{'light':question && question.length}"
                @click.stop="isShowQuestion=false"><i class="chitutree-h5 chitutreewenti"></i>问题<template v-if="question && question.length">&nbsp;({{question.length}})</template></span>
          <span class="fold"
                @click.stop="isShowQuestion=false">
            <i class="chitutree-h5 chitutreejiantoushouqi"></i>收起
          </span>
        </h3>
        <div class="question-tag"
             v-moveQuestion></div>
        <div v-if="question"
             class="msg">
          <div class="question-tab">
            <p :class="{'active':questionType === 'error'}"
               @click="getQuestion('error')">错误&nbsp;({{questionNum('error')}})</p>
            <p :class="{'active':questionType === 'warn'}"
               @click="getQuestion('warn')">警告&nbsp;({{questionNum('warn')}})</p>
          </div>
          <div class="msg-rows">
            <template v-if="typeof question === 'string'">
              {{question}}
            </template>
            <template v-else>
              <div v-for="(item,index) in getQuestion(questionType)"
                   :key="index"
                   class="msg-item">
                <template v-if="questionType === 'error'">
                  <div class="left-text"
                       :class="{'overflow':!item.showMore}">
                    <img src="@/assets/icons/icon_error@2x.png" />

                    <em>{{item.explainTime}}</em>
                    <span v-if="item.line > 0"
                          class="error jump"
                          @click="jumpToLine(item)">验证失败&nbsp;{{item.error}}</span>
                    <span v-else
                          class="error">验证失败&nbsp;{{item.error}}</span>

                    <span class="sql">{{item.sql}}</span>
                  </div>
                  <p v-if="!item.showMore"
                     class="view-more"
                     @click="item.showMore = true">点击查看详情</p>
                </template>
                <template v-if="questionType === 'warn'">
                  <div class="left-text"
                       :class="{'overflow':!item.showMore}">
                    <img src="@/assets/icons/icon_warning@2x.png" />
                    <em>{{item.explainTime}}</em>
                    <span v-if="item.line > 0"
                          class="warn jump"
                          @click="jumpToLine(item)">{{item.error}}</span>
                    <span v-else
                          class="warn">{{item.error}}</span>

                    <span class="sql">{{item.sql}}</span>
                  </div>
                  <p v-if="!item.showMore"
                     class="view-more"
                     @click="item.showMore = true">点击查看详情</p>
                </template>

              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
    <div v-if="activePane.contentData && activePane.contentData.fileType === 'SQL'"
         class="develop-bot justify-start">

      最后一次保存时间：{{saveJobTime}}
      <template v-if="isShowSaveIcon">
        <svg width="24"
             height="24"
             :class="{'animate':isAnimate}">
          <circle fill="none"
                  stroke="#333"
                  stroke-width="2"
                  cx="12"
                  cy="12"
                  r="10"
                  class="circle"
                  stroke-linecap="round" />
          <polyline fill=" none"
                    stroke="#333"
                    stroke-width="2"
                    points="7,13 12,17 18,9"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="tick" />
        </svg>
        <span>{{saveText}}</span>
      </template>
    </div>
    <!-- end -->
    <add-file ref="addFile" />
    <online-progress ref="onlineProgress"
                     :progress-data="progressData"
                     @confirm="triggerPublish"
                     @apply="triggerApply" />
    <TopicAuthApplyDialog ref="topicAuthApplyDialogRef" />
    <!-- 转环境页面 -->
    <ChangeEnv :visible="changeProdVisible"
               :fileType="fileType"
               :fileData="fileData"
               @close="changeProdVisible =false" />
    <ChangeProdDialog :visible="changeProdDialogVisible"
                      @close="changeProdDialogVisible = false" />
  </div>
</template>

<script>
  // import $ from 'jquery'
  import TabContent from './tab-content.vue'
  import addFile from './add-file'
  import OnlineProgress from './online-progress'
  import TopicAuthApplyDialog from './topic-auth-apply-dialog.vue'
  import ChangeEnv from './changeEnv'
  import ChangeProdDialog from './changeProdDialog.vue'
  import intro from 'intro.js'
  import 'intro.js/minified/introjs.min.css'
  export default {
    name: "MainBoard",
    directives: {
      moveQuestion (el) {
        el.onmousedown = function (e) {
          let init = e.clientY
          let parent = document.getElementById('question-board')
          let initHeight = parent.offsetHeight
          document.onmousemove = function (e) {
            let endHight = e.clientY
            let newHight = endHight - init + initHeight
            if (endHight > init) {
              newHight = initHeight - (endHight - init)
            } else {
              newHight = initHeight + init - endHight
            }
            parent.style.height = newHight + 'px'
          }
          document.onmouseup = function () {
            document.onmousemove = document.onmouseup = null
          }
        }
      }
    },
    components: {
      TabContent, addFile, OnlineProgress, TopicAuthApplyDialog, ChangeEnv, ChangeProdDialog
    },
    mixins: [],
    data () {
      return {
        isHistoryFiles: false,
        fileType: '',
        fileData: {},
        oldProjectId: '',
        questionType: 'error',
        tabVisible: false,
        left: 0,
        selectedId: 0,
        selectedIndex: 0,
        isLoading: false,
        isAnimate: false,
        isShowSaveIcon: false,
        saveJobTime: '',
        activeKey: '',
        panes: [],
        activePane: {},
        newTabIndex: 0,
        isShowQuestion: false,
        question: '',
        isVerifying: false,
        isChangeEnv: false,
        isOnLineing: false,
        isChangeEnvVerify: false,
        timer: null,
        progressData: {
          version: '',
          progress: 0,
          isCheckSuccess: '',
          isApplySuccess: ''
        },
        userRole: null,
        projectName: '',
        fileId: '',
        timeGap: 10 * 1000,//10秒自动保存一次
        verifyResult: null,//错误信息
        saveResult: null,//保存储存结果
        version: '',
        changeProdVisible: false,
        changeProdDialogVisible: false,
      }
    },
    computed: {
      isUatEnv () {
        return this.$store.getters.env === 'uat'
      },
      isProdEnv () {
        return this.$store.getters.env === 'prod'
      }
    },
    watch: {
      // question: {
      //   handler (val) {
      //     debugger
      //     if (val) {
      //       this.questionType = 'error'
      //       this.questionList = this.getQuestion('error')
      //     } else {
      //       this.questionType = 'error'
      //       this.questionList = []
      //     }
      //   },
      //   deep: true
      // },
      $route: {
        handler (val) {
          if (val.name === 'JobDevelop') {
            this.autoSave()
            if (this.$route.query.projectId !== this.oldProjectId) {
              Object.assign(this.$data, this.$options.data(this))
              this.init()
              this.oldProjectId = this.$route.query.projectId
            }
          } else {
            this.cancelAutoSave()
          }
        },
        deep: true
      },
      "saveJobTime": {
        handler (value, oldValue) {
          if (value && value !== oldValue) {
            this.isAnimate = true
            this.isShowSaveIcon = true
            this.saveText = '保存中'
            setTimeout(() => {
              this.isAnimate = false
              this.saveText = '保存成功'
            }, 1000)
            setTimeout(() => {
              this.isShowSaveIcon = false
            }, 3000)
          }
        }
      },
      activeKey: {
        async handler (val) {
          if (val !== '') {
            this.isShowQuestion = false
            this.question = []
            for (let i = 0; i < this.panes.length; i++) {
              if (String(this.panes[i].id) === String(this.activeKey)) {
                this.activePane = this.panes[i]
                this.$bus.$emit('tree_getActiveFile', this.activePane)//更新目录树
                break
              }
            }
            if (this.$refs['tabContent' + this.activeKey] && this.$refs['tabContent' + this.activeKey][0]) {
              this.$refs['tabContent' + this.activeKey][0].move()//重新触发move,不然切换页签后，拖动解锁的弹框会有问题
            }
            this.saveFilesToLocal()
            this.queryFile(val)
            await this.isHistoryFile()
          }
        },
        immediate: true
      },
      'panes.length': {
        handler (val, oldVal) {//本地存储页签
          if (val !== oldVal) {
            this.saveFilesToLocal()
          }
        }
      },
      tabVisible (value) {
        if (value) {
          document.body.addEventListener('click', this.closeMenu)
        } else {
          document.body.removeEventListener('click', this.closeMenu)
        }
      },

      '$route.query.fileId': {
        handler (val) {
          if (val) {
            Object.assign(this.$data, this.$options.data(this))
            this.init()
          }
        }
      }
    },
    async created () {
      this.init()
    },
    mounted () {
      if (this.isUatEnv) {
        setTimeout(() => {
          this.showFeatureGuide()
        }, 500)
      }
    },
    deactivated () {
      this.$refs.onlineProgress.close()
    },
    destroyed () {
      this.cancelAutoSave()
    },
    methods: {
      showFeatureGuide () {
        const guideKey = `sdp-change-env-guide`
        if (!localStorage.getItem(guideKey)) {
          this.handleGuideStart(guideKey)
        }
      },
      handleGuideStart (guideKey) {
        const introInstance = intro().setOptions({
          showButtons: true,
          doneLabel: '我知道了',
          showBullets: false,
          showProgress: false,
          showStepNumbers: false,
          steps: [
            {
              title: '功能引导',
              element: document.querySelector(`.change-env-container`),
              intro:
                `现在支持将UAT环境代码同步到生产环境啦`
            }
          ]
        })
        introInstance.start()
        introInstance.onexit(() => {
          localStorage.setItem(guideKey, '1')
        })
      },
      async init () {
        this.fileId = Number(this.$route.query.fileId)
        this.projectName = decodeURIComponent(decodeURIComponent(this.$route.query.projectName))
        this.$bus.$off('getFile').$on('getFile', this.initActiveFile)//tree双击文件时触发
        this.$bus.$off('queryFile').$on('queryFile', this.queryFile)//触发条件：1、版本信息回滚成功2、解锁成功
        this.$bus.$off('updateFolderId').$on('updateFolderId', this.updateFolderId)//左侧目录树文件移动后更新目录id
        this.$bus.$off('deleteTreeFile').$on('deleteTreeFile', this.deleteTreeFile)
        this.$bus.$off('isLoadingFile').$on('isLoadingFile', this.isLoadingFile)
        //读取已保存的页签
        let localFiles = localStorage.getItem('openFiles')
        if (typeof localFiles === 'string') {
          try {
            localFiles = JSON.parse(localFiles)
          } catch (e) {
            console.log(e)
          }
        }
        let localActiveKey = ''

        if (localFiles && String(localFiles.projectId) === String(this.$route.query.projectId)) {
          let files = localFiles.files
          if (files) {
            files = files.map(item => {
              item.isLocal = true
              return item
            })
            this.panes = files
            const ids = this.panes.map(item => {
              return Number(item.id)
            })
            const states = await this.checkState(ids)
            if (states.code === 0 && states.data) {
              const haveRemoveItems = states.data.filter(item => {//找出已经被删除的
                return item.enabledFlag === 0
              })
              const enabledItems = states.data.filter(item => {//找出未被删除的
                return item.enabledFlag === 1
              })
              if (haveRemoveItems && haveRemoveItems.length > 0) {
                haveRemoveItems.forEach(item => {//过滤已被删除的文件
                  this.panes = this.panes.filter(tab => {
                    return String(tab.id) !== String(item.id)
                  })
                })
              }
              this.panes.forEach(item => {//同步文件状态
                for (let i = 0; i < enabledItems.length; i++) {
                  if (String(item.id) === String(enabledItems[i].id)) {
                    item.contentData.isOnLine = enabledItems[i].isOnLine
                    break
                  }
                }
              })
            }
          }
          localActiveKey = localFiles.activeFile
        }
        this.activeKey = this.fileId || localActiveKey//优先选择路由的fileId作为当前页签
        if (this.activeKey) {
          const fileContent = await this.queryFile(this.activeKey)
          if (fileContent.code === 0) {
            this.initActiveFile(fileContent.data)
          }
        }
      },
      isLoadingFile (isLoading) {
        this.isLoading = isLoading
      },
      async getProjectUserRole () {//获取当前用户在当前项目的角色
        const params = {
          id: Number(this.$route.query.projectId)
        }
        let res = await this.$http.post('/project/projectManagement/getUserRole', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.userRole = res.data
      },
      autoSave () {//自动保存
        if (this.activeKey && !this.timer) {
          this.timer = setInterval(() => {
            if (this.$route.name === 'JobDevelop') {//防止有时定时器没清掉一直执行
              this.handleAutoSave()
            }
          }, this.timeGap)

        }
      },
      cancelAutoSave () {//取消自动保存
        clearInterval(this.timer)
        this.timer = null
      },
      deleteTreeFile (id) {
        let index = ''
        for (let i = 0; i < this.panes.length; i++) {
          if (String(this.panes[i].id) === String(id)) {
            index = i
          }
        }
        if (index !== '') {
          this.removeTab(id, index)
        }
      },
      openMenu (id, index, e) {
        const menuMinWidth = 105
        const offsetLeft = this.$el.getBoundingClientRect().left // container margin left
        const offsetWidth = this.$el.offsetWidth // container width
        const maxLeft = offsetWidth - menuMinWidth // left boundary
        const left = e.clientX - offsetLeft + 5 // 15: margin right

        if (left > maxLeft) {
          this.left = maxLeft
        } else {
          this.left = left
        }

        // this.top = e.clientY
        this.tabVisible = true
        this.selectedId = id
        this.selectedIndex = index
      },
      //关闭左侧
      removeLeftTab (index) {
        this.panes = this.panes.filter((option, key) => {
          return key >= index
        })
        this.activeKey = this.panes[0].id
      },
      //关闭右侧
      removeRightTab (index) {
        this.panes = this.panes.filter((option, key) => {
          return key <= index
        })
        this.activeKey = this.panes[index].id
      },
      //关闭其他
      closeOthersTab (id) {
        this.panes = this.panes.filter(option => {
          return String(option.id) === String(id)
        })
        this.activeKey = this.panes[0].id
      },
      closeMenu () {
        this.tabVisible = false
      },
      removeTab (id, index) {//关闭页签
        if (String(this.activeKey) === String(id)) {
          if (index < this.panes.length - 1) {
            this.activeKey = this.panes[index + 1].id
          } else {
            this.activeKey = this.panes[0].id
          }
        }
        this.panes = this.panes.filter(option => {
          return String(option.id) !== String(id)
        })
        if (this.panes.length === 0) {//页签都关闭了，清除自动保存的定时器
          this.cancelAutoSave()
        }
      },
      async addFile () {//新建文件
        const flag = await this.isAllowAddFile()
        if (!flag) return
        const expandedKeys = []
        const defaultSelectedKeys = []
        this.$refs.addFile.open({ expandedKeys: expandedKeys, defaultSelectedKeys: defaultSelectedKeys })
      },
      async isAllowAddFile () {
        if (this.isUatEnv) {
          return true
        } else {
          let res = await this.$http.post('/file/allowAddFile', {}, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            if (!res.data) {
              this.$message.warning('为了规范作业流程，生产环境不允许直接创建作业，建议先在UAT环境进行作业开发后同步至生产环境~')
            }
            return res.data
          } else {
            return false
          }
        }
      },
      async isAllowEditFile () {
        if (!this.isProdEnv) {
          return true
        } else {
          let res = await this.$http.post('/file/allowEditFile', {
            id: this.activeKey
          }, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            if (!res.data.allowEdit) {
              this.$message.warning('为了规范作业流程，生产环境不允许直接编辑作业，建议先在UAT环境进行作业开发后同步至生产环境~')
            }
            return res.data.allowEdit
          } else {
            return true
          }
        }
      },
      //是否历史作业
      async isHistoryFile () {
        if (!this.isProdEnv) {
          return true
        } else {
          let res = await this.$http.post('/file/allowEditFile', {
            id: this.activeKey
          }, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            this.isHistoryFiles = res.data.historyFile
          } else {
            return true
          }
        }
      },
      initActiveFile (data) {//tree双击文件时触发
        const findItem = this.panes.filter(item => {
          return String(item.id) === String(data.id)
        })
        if (findItem.length === 0) {
          if (!data.etlContent) {
            data.etlContent = '-- 请在此处编辑内容'
          }
          data.originalContent = data.etlContent
          this.panes.push({ title: data.fileName, id: data.id, contentData: data })
        }
        this.activeKey = data.id
        const findActive = this.panes.filter(item => {
          return String(item.id) === String(this.activeKey)
        })
        findActive[0].contentData = data
        this.activePane = findActive[0]
        this.$bus.$emit('tree_getActiveFile', this.activePane)//更新目录树
        this.question = ''
        this.isShowQuestion = false
        //文件锁定时不自动保存
        if (this.activePane.contentData && this.activePane.contentData.lockSign !== 0) {
          this.autoSave()
        }
        // this.getProjectUserRole()
      },
      async handleUndo () {//撤销
        const flag = await this.isAllowEditFile()
        if (!flag) return
        this.$refs['tabContent' + this.activeKey][0].undo()
      },
      async handleRedo () {//恢复
        const flag = await this.isAllowEditFile()
        if (!flag) return
        this.$refs['tabContent' + this.activeKey][0].redo()
      },
      async handleFind () {//查找
        const flag = await this.isAllowEditFile()
        if (!flag) return
        this.$refs['tabContent' + this.activeKey][0].find()
      },
      async handleBeauty () {//格式化
        const flag = await this.isAllowEditFile()
        if (!flag) return
        this.$refs['tabContent' + this.activeKey][0].beauty()
      },
      async clickSave () {//点击保存
        //触发元表保存 元表保存后又会触发一次文件保存，暂时先注释
        if (this.$refs['tabContent' + this.activeKey][0].showConfig === 'metaTable') {
          const res = await this.$refs['tabContent' + this.activeKey][0].$refs['metaTableConfig'].submitForm()
          if (res.code !== 0 && res.code !== 8515) {//submitForm code===0和code=== 8515时会触发保存
            this.handleSave()
          }
        } else {
          this.handleSave()
        }

      },
      async handleSave (data) {

        const params = this.$refs['tabContent' + this.activeKey][0].getData()
        if (params.dataStreamConfig) {
          if (!params.dataStreamConfig.jarName) {
            this.$message.warning('DS配置中未选择UDX资源文件')
            return
          }
          if (!params.dataStreamConfig.jarVersion) {
            this.$message.warning('DS配置中未选择UDX资源版本')
            return
          }
          if (!params.dataStreamConfig.mainClass) {
            this.$message.warning('DS配置中Main-Class 不能为空')
            return
          }
        }
        // this.question = ''
        // this.isShowQuestion = false
        const saveResult = await this.save(true)
        if (data) {
          saveResult.flag = data.flag
        }
        if (!saveResult || saveResult.code !== 0) {
          this.$message.error('保存失败')
          return
        } else if (saveResult.code === 0) {
          // console.log('saveResult: ', saveResult)
          if (saveResult.flag) { // 避免kafka权限问题弹窗
            this.$message.success('保存成功')
          }
        }
      },
      async handleAutoSave () {
        if (this.$refs['tabContent' + this.activeKey] && this.$refs['tabContent' + this.activeKey][0]) {
          const params = this.$refs['tabContent' + this.activeKey][0].getData()
          if (params.originalContent === params.etlContent || params.etlContent === '') {
            return
          }
          // this.question = ''
          // this.isShowQuestion = false
          const saveResult = await this.save(true)
          if (!saveResult || saveResult.code !== 0) {
            if (saveResult.code === 7502) {
              // this.$message.error('文件已被他人锁定，自动保存失败')
              console.log('文件已被他人锁定，自动保存失败');
            } else {
              this.$message.error('自动保存失败')
            }
            return
          } else if (saveResult.code === 0) {
            // this.$message.success('自动保存成功')
          }
        }

      },
      /**
       * @param isChangeEnv {Boolean} 是否转生产环境
       */
      async handleVerify (isChangeEnv) {//点击验证
        this.question = ''
        this.isShowQuestion = false
        this.isVerifying = true
        if (isChangeEnv) {
          this.isChangeEnv = true
          this.isChangeEnvVerify = false
        }
        const saveResult = await this.save()
        // console.log('saveResult: ', saveResult)
        if (!saveResult) {
          this.isVerifying = false
          if (isChangeEnv) this.isChangeEnv = false
          // this.$message.error('接口返回数据异常')
          this.$message.error('验证失败')
          return
        } else if (saveResult.code !== 0) {
          this.isVerifying = false
          if (isChangeEnv) this.isChangeEnv = false
          // this.$message.error(saveResult.msg)
          this.$message.error('验证失败:' + saveResult.msg)
          return
        } else if (saveResult.code === 0) {
          const verifyResult = await this.verify()
          if (!verifyResult) {
            // this.$message.error('接口返回数据异常')
            this.$message.error('验证失败')
            this.isVerifying = false
            if (isChangeEnv) this.isChangeEnv = false
            return
          } else if (verifyResult.code === 0 || verifyResult.code === 11006) {
            this.isVerifying = false
            if (isChangeEnv) {
              this.isChangeEnv = false
              this.isChangeEnvVerify = true
            }
            if (verifyResult.code === 0) {
              this.$message.success('验证成功')
            }
            if (verifyResult.code === 11006) {//只有警告
              if (verifyResult.msg) {
                this.$message.warning(verifyResult.msg)
              }
              if (verifyResult.data) {
                verifyResult.data = verifyResult.data.map(item => {
                  item.showMore = false
                  return item
                })
                this.question = verifyResult.data
                this.isShowQuestion = true
                this.questionType = 'warn'
              }
            }
            // if (verifyResult.msg && verifyResult.msg.indexOf('写入的是来源表') >= 0) {//写入的是来源表给个提示，但是验证仍然是成功的
            //   this.$message.warning(verifyResult.msg)
            // } else {
            //   this.$message.success('验证成功')
            // }

          } else {
            this.isVerifying = false
            if (isChangeEnv) this.isChangeEnv = false
            // this.$message.error(verifyResult.msg)
            let tip = '验证失败'
            if (verifyResult.msg) {
              tip += (':' + verifyResult.msg)
            }
            this.$message.error(tip)
            if (verifyResult.data) {
              verifyResult.data = verifyResult.data.map(item => {
                item.showMore = false
                return item
              })
              this.question = verifyResult.data
              this.isShowQuestion = true
              this.questionType = 'error'
              // 判断kafka source是否都有权限, 如果存在没有权限的kafka source, 需要弹框提示并能一键发起OA申请
              const dataHubData = verifyResult.data[0]
              if (dataHubData.explainExt && dataHubData.explainExt.type == 1 && dataHubData.explainExt.obj.length) {
                this.$refs.topicAuthApplyDialogRef.open(dataHubData.explainExt.obj)
              }
            }
            return
          }
        }
      },
      //发布前校验
      async handlePublishVerify () {
        this.progressData.isCheckSuccess = ''
        this.progressData.version = ''
        this.progressData.progress = ''
        this.verifyResult = null
        this.saveResult = null
        this.isOnLineing = true
        this.version = ''
        const saveResult = await this.save()
        if (!saveResult) {
          this.isOnLineing = false
          this.$message.error('发布失败')
          this.$refs.onlineProgress.resetData()
          return
        } else if (saveResult.code !== 0) {
          this.isOnLineing = false
          this.$message.error('发布失败:' + saveResult.msg)
          this.$refs.onlineProgress.resetData()
          return
        } else if (saveResult.code === 0) {
          const verifyResult = await this.verify()
          if (!verifyResult) {
            this.$message.error('发布失败')
            this.$refs.onlineProgress.resetData()
            this.isOnLineing = false
            return
          } else if (verifyResult.code === 0 || verifyResult.code === 11006) {//验证成功，走发布下一步操作
            this.isOnLineing = false
            this.progressData.version = saveResult.data.version
            this.progressData.progress = 20
            this.handlePublish()
            this.saveResult = saveResult
            // 保存校验信息
            this.verifyResult = verifyResult
          } else {
            this.isOnLineing = false
            let tip = '验证失败'
            if (verifyResult.msg) {
              tip += (':' + verifyResult.msg)
            }
            this.$message.error(tip)
            if (verifyResult.data) {
              verifyResult.data = verifyResult.data.map(item => {
                item.showMore = false
                return item
              })
              this.question = verifyResult.data
              this.isShowQuestion = true
              this.questionType = 'error'
              // 判断kafka source是否都有权限, 如果存在没有权限的kafka source, 需要弹框提示并能一键发起OA申请
              const dataHubData = verifyResult.data[0]
              if (dataHubData.explainExt && dataHubData.explainExt.type == 1 && dataHubData.explainExt.obj.length) {
                this.$refs.topicAuthApplyDialogRef.open(dataHubData.explainExt.obj)
              }
            }
            return
          }
        }
      },
      async handlePublish () {//点击发布
        this.question = ''
        this.isShowQuestion = false
        this.progressData.isCheckSuccess = ''
        this.progressData.isApplySuccess = ''
        this.$refs.onlineProgress.open()

      },
      async triggerPublish (isInWhite, remark) {//点击进度条弹框的确认，点发布如果报错统一提示“发布失败”
        if (isInWhite) {//在白名单直接发布
          let res = await this.publishWithWhite(remark)
          if (res.code === 0 || res.code === 11006) {
            if (res.code === 0) {
              this.$message.success('发布成功')
              // 自动跳转到作业运维
              const query = {
                projectId: this.$route.query.projectId,
                projectName: this.$route.query.projectName,
                projectCode: this.$route.query.projectCode
              }
              this.$router.push({
                name: 'JobOperate',
                query
              }).then(() => {
                // 携带作业名称进行搜索
                const params = this.$refs['tabContent' + this.activeKey][0].getData()
                this.$bus.$emit('jobOperateUpdate', params.fileName)
              })
            }
            else if (res.code === 11006) {
              if (res.msg) {
                this.$message.warning(res.msg)
              }
            }
            this.$refs.onlineProgress.close()
          } else {
            this.$message.error(res.msg)
          }
        } else {//不在白名单
          if (this.verifyResult.code === 11006) {
            if (this.verifyResult.msg) {
              this.$message.warning(this.verifyResult.msg)
            }
            if (this.verifyResult.data) {
              this.verifyResult.data = this.verifyResult.data.map(item => {
                item.showMore = false
                return item
              })
              this.question = this.verifyResult.data
              this.isShowQuestion = true
              this.questionType = 'warn'
            }
          }
          this.progressData.progress = 100
          this.progressData.isCheckSuccess = true
        }
      },
      async triggerApply (description) {
        this.progressData.isApplySuccess = ''
        const fileDetail = JSON.parse(JSON.stringify(this.$refs['tabContent' + this.activeKey][0].fileDetail))
        const params = {
          id: fileDetail.id,
          projectName: this.projectName,
          remark: description
        }
        let res = await this.$http.post('/approve/submitApply', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)

          }
        })
        if (res.code === 0) {
          this.progressData.isApplySuccess = true
          this.$store.dispatch('approve/SET_APPROVE')//更新红点数
          // setTimeout(() => {
          //   this.$message.success('申请成功，请在作业审批-我的申请 页面查看进度')
          //   this.$refs.onlineProgress.close()
          // }, 1000)
        } else {
          this.progressData.isApplySuccess = false
          if (res.msg) {
            this.$message.error(res.msg)
          }

        }
      },
      updateFolderId (fileId, newFolderId) {//左侧目录树文件移动后更新目录id
        const findData = this.panes.filter(item => String(item.id) === String(fileId))
        if (findData.length) {
          findData[0].contentData.folderId = newFolderId
        }

      },
      async queryFile (id, type) {
        const params = {
          id: Number(id)
        }
        let res = await this.$http.post('/file/detailFile', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          const data = res.data
          const findData = this.panes.filter(item => String(item.id) === String(id))
          if (findData.length) {
            if (type === 'lock') {//这里的情况是，用户点保存发现文件已经锁定，这里不能全量覆盖contentData，只更新lock相关字段
              findData[0].contentData.lockSign = data.lockSign
              findData[0].contentData.lockedBy = data.lockedBy
              findData[0].contentData.updationDate = data.updationDate
            } else if (type === 'scrollBack' || type === 'unclock') {//回滚和解锁后都要拉取最新的数据
              if (!data.etlContent) {
                data.etlContent = '-- 请在此处编辑内容'
              }
              data.originalContent = data.etlContent
              findData[0].contentData = data
              if (type === 'unclock') {//解锁后重新触发自动保存
                this.autoSave()
              }
            } else {//其他情况为：比如：1：页签切换时，当前页签为localStorage取出的需要拉取最新数据；2：加载localStorage的页签后
              if (!data.etlContent) {
                data.etlContent = '-- 请在此处编辑内容'
              }
              data.originalContent = data.etlContent
              findData[0].contentData = data
            }
          }
        }
        return res
      },
      async save (flag) {//调用保存接口
        if (!this.activeKey) {
          return
        }
        const params = this.$refs['tabContent' + this.activeKey][0].getData()
        params.projectId = Number(this.$route.query.projectId)
        delete params.sourceContent
        delete params.configContent
        const data = {
          jobConfig: params.jobConfig,
          metaTableContent: params.metaTableContent,
          etlContent: params.etlContent,
          fileName: params.fileName,
          fileType: params.fileType,
          folderId: params.folderId,
          id: params.id,
          projectId: Number(this.$route.query.projectId),
          sourceConfig: params.sourceConfig,
          dataStreamConfig: params.dataStreamConfig
        }
        let res = await this.$http.post('/file/updateFile', data, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          if (res.data && res.data.updationDate) {
            // let updationDate = res.data.updationDate
            // updationDate = updationDate.split(' ')
            // this.$store.dispatch('project/setSaveJobTime', updationDate[1])
            this.saveJobTime = res.data.updationDate
          }
          for (let i = 0; i < this.panes.length; i++) {
            if (String(this.panes[i].id) === String(this.activeKey)) {
              this.panes[i].contentData.originalContent = params.etlContent
              this.panes[i].contentData.dataStreamConfig = params.dataStreamConfig
              break
            }
          }

          // this.$emit('saveSuccess', res.data)
        } else if (res.code === 7502) {//文件被锁定
          this.$bus.$emit('refreshTree')//更新目录树
          this.queryFile(this.activeKey, 'lock')//拉取文件详情更新锁定状态
          this.cancelAutoSave()//关闭自动保存
        }
        if (flag) {
          res.flag = flag
        }
        return res
      },
      async verify () {//调用验证接口
        if (!this.activeKey) {
          return
        }
        const fileDetail = JSON.parse(JSON.stringify(this.$refs['tabContent' + this.activeKey][0].fileDetail))
        const params = {
          id: fileDetail.id
        }
        let res = await this.$http.post('/file/validate', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        return res
      },
      async publishWithWhite (remark) {//调用验证接口
        if (!this.activeKey) {
          return
        }
        const fileDetail = JSON.parse(JSON.stringify(this.$refs['tabContent' + this.activeKey][0].fileDetail))
        const params = {
          id: fileDetail.id,
          remark: remark
        }
        let res = await this.$http.post('/file/validateOnline4WhiteList', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        return res
      },
      async publish (remark) {//调用发布接口
        if (!this.activeKey) {
          return
        }
        const fileDetail = JSON.parse(JSON.stringify(this.$refs['tabContent' + this.activeKey][0].fileDetail))
        const params = {
          id: fileDetail.id
        }
        if (remark) {
          params.remark = remark
        }
        let res = await this.$http.post('/file/online', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        return res
      },
      showQuestion () {//底部问题的显示隐藏
        this.isShowQuestion = !this.isShowQuestion
        this.botHeight = 32
        this.tabHeight = this.$refs.boardMain.clientHeight - this.botHeight
      },
      saveFilesToLocal () {//保存页签到localStorage
        let openFiles = JSON.parse(JSON.stringify(this.panes))
        openFiles = openFiles.map(item => {
          //编辑器内容和配置不保存本地，节省空间
          item.contentData.configContent = {}
          item.contentData.content = ''
          item.contentData.sourceContent = {}
          item.contentData.etlContent = ''
          item.contentData.metaTableContent = ''
          delete (item.contentData.originalContent)
          return item
        })
        let activeFile = this.activeKey
        if (openFiles.length === 0) {
          activeFile = ''
        }
        let obj = {
          activeFile: activeFile,
          files: openFiles,
          projectId: Number(this.$route.query.projectId)
        }
        try {
          obj = JSON.stringify(obj)
          localStorage.setItem('openFiles', obj)
        } catch (e) {
          // console.log(e)
        }
      },
      async changeTab (activeKey) {//切换页签，注意切换页签activeKey的监听也会触发，这里主要处理页签是locaStorage取出的时候
        const currentPane = this.panes.find(item => {
          return String(item.id) === String(activeKey)
        })
        if (currentPane && currentPane.isLocal) {
          const res = await this.queryFile(activeKey)
          if (res && res.code === 0) {
            currentPane.isLocal = false
          }
        }
      },
      async checkState (ids) {
        const params = ids
        let res = await this.$http.post('/file/checkState', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        return res
      },
      jumpToLine (item) {//跳到编缉器的那一行
        const line = item.line
        if (line <= 0) {
          return
        }
        if (this.$refs['tabContent' + this.activeKey] && this.$refs['tabContent' + this.activeKey][0]) {
          this.$refs['tabContent' + this.activeKey][0].focusLineInEditor(line)
        }
        this.isShowQuestion = false
      },
      getQuestion (type) {
        this.questionType = type
        let questionList = []
        if (this.question && this.question.length) {
          questionList = this.question.filter(item => item.type === type)
        }
        return questionList
      },
      questionNum (type) {
        let list = []
        if (this.question && this.question.length) {
          list = this.question.filter(item => item.type === type)
        }
        return list.length
      },
      //转生产按钮
      async changeEnv () {
        await this.handleVerify(true)
        if (!this.isChangeEnvVerify) return
        this.fileType = this.activePane.contentData.fileType
        this.fileData = this.activePane.contentData
        this.changeProdVisible = true
        // this.changeProdDialogVisible = true
      }

    },
  }
</script>
<style lang="scss" scoped>
  .main-board-container {
    height: 100%;
    flex: 1;
    width: 0; /*flex设置成1，width设置成0可以解决撑出父容器的问题 */
    // max-width: calc(100% - 150px);
    // width: calc(100% - 220px);
    .green {
      color: #51c313;
    }
    .cyanine {
      color: #16a8cc;
    }
    .board-top {
      height: 32px;
      border-bottom: solid 1px #d9d9d9;
      flex-shrink: 0;
      font-size: 12px;
      .item {
        i {
          margin-right: 10px;
        }
      }
      .left {
        padding-left: 0;
        i {
          font-size: 14px !important;
          margin-right: 4px;
        }
      }
      .right {
        padding-right: 17px;
        i {
          font-size: 14px !important;
          margin-right: 4px;
        }
      }
    }
    .board-main {
      height: calc(100% - 96px);
      .board-msg {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        .borad-msg-content {
          font-size: 12px;
          text-align: center;
          color: #667082;
          img {
            width: 100px;
            margin-bottom: 10px;
          }
        }
      }
      .board-tab {
        .context-menu {
          position: absolute;
          top: -10px;
          margin: 0;
          z-index: 100;
          padding: 5px 0;
          color: #333;
          font-size: 12px;
          font-weight: 400;
          background: #fff;
          border-radius: 4px;
          list-style-type: none;
          box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, 0.3);
          li {
            margin: 0;
            padding: 7px 16px;
            cursor: pointer;
            &:hover {
              color: #66b1ff;
              background: #ecf5ff;
            }
          }
        }
        height: 100% !important;
        .tab-title {
          max-width: 200px;
          display: inline-block;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        i {
          font-size: 12px;
          margin-right: 5px;
          &.remove {
            margin-left: 3px;
            display: inline-block;
            transform: scale(0.8);
            // display: none;
          }
        }
        /deep/ .ant-tabs-nav {
          font-size: 12px;
        }
        /deep/ .ant-tabs {
          height: calc(100% + 31px);
          .ant-tabs-bar {
            margin: 0;
            max-width: calc(100% - 80px);
            border-bottom: none;
          }
          .ant-tabs-content {
            height: 100%;
            border-top: solid 1px #d9d9d9;
            .ant-tabs-tabpane-active {
              height: 100%;
            }
          }
        }
        /deep/ .ant-tabs-nav {
          .ant-tabs-tab {
            height: 32px;
            line-height: 32px;
            margin: 0 10px;
            padding: 0 0 0 6px;
            // &:hover .remove {
            //   display: inline-block;
            // }
          }
          .ant-tabs-tab-active {
            color: #0066ff;
          }
        }
        .tab-close {
          width: 12px;
          height: 12px;
        }
      }
    }
    .history-alert {
      width: 400px;
      position: absolute;
      right: 37px;
      bottom: 68px;
      z-index: 99999;
    }
    .board-bot {
      height: 32px;
      width: 100%;
      border-top: solid 1px #d9d9d9;
      font-size: 12px;
      line-height: 28px;
      position: relative;
      background: #fff;
      .light {
        color: #ff9118 !important;
        i {
          color: #ff9118 !important;
        }
      }
      .result {
        margin-left: 16px;
      }
      .question {
        z-index: 999;
        user-select: none;
        height: 30px;
        margin-left: 16px;
        width: 100%;
        background: #ffffff;
        cursor: pointer;
        p {
          width: 100%;
          .name {
            i {
              margin-right: 8px;
            }
          }
          .arrow {
            margin-right: 10px;

            i {
              font-size: 16px !important;
            }
          }
        }
      }
      .question-board {
        position: absolute;
        overflow-y: hidden;
        min-height: 200px;
        max-height: 500px;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 200px;
        z-index: 999;
        border-top: solid 1px #d9d9d9;
        border-right: solid 1px #d9d9d9;
        background: #fff;
        font-size: 12px;
        .question-tag {
          cursor: row-resize;
          height: 4px;
          border-top: 1px solid #d9d9d9;
        }
        h3 {
          user-select: none;
          height: 32px;
          line-height: 32px;
          font-size: 12px;
          padding-left: 16px;
          // border-bottom: solid 1px #d9d9d9;
          span {
            cursor: pointer;
          }
          .name {
            i {
              margin-right: 8px;
            }
          }
          .fold {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 40px;
            margin-right: 10px;
            height: 100%;
            cursor: pointer;
            i {
              font-size: 16px !important;
            }
          }
        }
        .msg {
          padding: 10px 12px 12px;
          min-height: 168px;
          height: calc(100% - 32px);
          overflow: hidden;
          cursor: text;
          .question-tab {
            display: flex;
            width: 152px;
            height: 24px;
            line-height: 24px;
            justify-content: flex-start;
            align-items: center;

            margin-bottom: 8px;
            p {
              cursor: pointer;
              width: 50%;
              height: 24px;
              line-height: 24px;
              color: #2b2f37;
              text-align: center;
              align-items: center;
              display: flex;
              justify-content: center;
              overflow: hidden;
              border: 1px solid #dee2ea;
              &:nth-of-type(1) {
                border-radius: 4px 0 0 4px;
                border-right: none;
              }
              &:nth-of-type(2) {
                border-radius: 0 4px 4px 0;
                border-left: none;
              }
              &.active {
                background: #006eff;
                color: #fff;
              }
            }
          }
          .msg-rows {
            height: calc(100% - 24px);
            overflow: auto;
          }
          .msg-item {
            color: #2b2f37;
            line-height: 20px;
            display: flex;
            .left-text {
              max-width: 80%;

              &.overflow {
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
              }
              img {
                width: 14px;
                height: 14px;
                margin-right: 5px;
                margin-top: -3px;
              }
              em {
                font-style: normal;
                margin-right: 5px;
              }
              .warn {
                color: #e5890f;
              }
              .sql {
              }
              .error {
                color: #fe6b6c;
                &.jump {
                  cursor: pointer;
                  // color: #006eff;
                  text-decoration: underline;
                }
              }
            }
            .view-more {
              color: #0066ff;
              cursor: pointer;
              margin-left: 4px;
            }
          }
        }
      }
    }
    .develop-bot {
      height: 32px;
      border-top: solid 1px #d9d9d9;
      padding-left: 14px;
      color: #333;
      font-size: 12px;
      svg {
        transform: scale(0.6);
        margin-left: 2px;
      }

      svg.animate .circle {
        animation: circle 1s ease-in-out;
        animation-fill-mode: forwards;
        stroke-dasharray: 88;
        stroke-dashoffset: 100;
      }

      svg.animate .tick {
        animation: tick 1s ease-out;
        animation-fill-mode: forwards;
        animation-delay: 0.95s;
        stroke-dasharray: 20;
        stroke-dashoffset: 20;
      }

      @keyframes circle {
        from {
          stroke-dashoffset: 100;
        }

        to {
          stroke-dashoffset: 0;
        }
      }

      @keyframes tick {
        from {
          stroke-dashoffset: 20;
        }

        to {
          stroke-dashoffset: 0;
        }
      }
    }
  }
</style>
