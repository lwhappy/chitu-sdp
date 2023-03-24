<template>
  <div :class="{'tab-content-container':true, 'tab-content-container-SQL':fileDetail.fileType === 'SQL', 'tab-content-container-DS':fileDetail.fileType === 'DS',}">
    <div class="tab-content-wrap">
      <div v-if="fileDetail.fileType === 'SQL'"
           class="code-editor-container"
           :id="'monacoEditor-' + fileDetail.id"
           ref="monacoEditor">
        <!-- 固定区不可编辑 -->
        <div class="fixed-editor-div"
             :id="'fixed-' + fileDetail.id">
          <div class="fixed-editor"
               :id="'fixedEditor-' + fileDetail.id"
               ref="fixedEditor"></div>
          <div v-if="foldFlag"
               class="click-guide">
            <p class="text">该区域只展示元表配置内容，<button @click="foldEvent">点击查看</button></p>
          </div>
        </div>
        <div class="move-drag"
             :id="'moveDrag-' +fileDetail.id">
          <div class="move-div"
               :id="'moveDiv-' +fileDetail.id"
               ref="moveDiv"
               v-moveMonaco>
            <!-- <img v-show="!foldFlag"
                 @click="foldEvent"
                 class="up-img"
                 src="@/assets/icons/fold-up-arrow-blue.png"
                 alt=""> -->

            <div class="move"
                 :id="'move-' + fileDetail.id">
            </div>
            <div v-show="!foldFlag"
                 @click="foldEvent"
                 class="up-img"></div>
            <div v-show="foldFlag"
                 @click="foldEvent"
                 class="down-img">
            </div>
          </div>
        </div>

        <!-- 非固定区可编辑 -->
        <div class="auto-editor"
             ref="autoEditor"
             :id="'autoEditor-' + fileDetail.id"></div>
      </div>
      <DS ref="dsView"
          v-else-if="fileDetail.fileType === 'DS'"
          :fileDetail="fileDetail" />
      <div v-show="fileDetail.lockSign === 0"
           ref="lockJob"
           class="lock justify-between">
        <p class="text">{{fileDetail.lockedBy}} 在 {{fileDetail.updationDate}} 锁定了当前作业</p>
        <a-button type="primary"
                  size="small"
                  @click="unlock">
          点击解锁
        </a-button>
        <!-- <a-button class="btn"
                  @click="unlock">点击解锁</a-button> -->
      </div>
      <!-- 右侧配置 -->
      <div class="config-container">
        <div class="senior-config config"
             :class="showConfig === 'senior'?'active':''">
          <div class="title"
               @click="changeConfig('senior')">
            <!-- <i class="chitutree-h5 chitutreegaojipeizhi"></i> -->
            <p class="name">高级配置</p>
          </div>
        </div>
        <div class="resource-config config"
             :class="showConfig === 'source'?'active':''">
          <div class="title"
               @click="changeConfig('source')">
            <!-- <i class="chitutree-h5 chitutreeziyuanpeizhi"></i> -->
            <p class="name">资源配置</p>
          </div>
        </div>
        <div v-if="fileDetail.fileType === 'SQL'"
             class="resource-config config"
             :class="showConfig === 'metaTable'?'active':''">
          <div class="title"
               @click="changeConfig('metaTable')">
            <!-- <i class="chitutree-h5 chitutreeicon_yuanbiaopeizhi"></i> -->
            <p class="name">元表配置</p>
          </div>
        </div>
        <div v-if="fileDetail.fileType === 'DS'"
             class="resource-config config"
             :class="showConfig === 'DS'?'active':''">
          <div class="title"
               @click="changeConfig('DS')">
            <!-- <i class="chitutree-h5 chitutreeicon_yuanbiaopeizhi"></i> -->
            <p class="name">DS配置</p>
          </div>
        </div>
        <div class="version-info-config config"
             :class="showConfig === 'versionInfo'?'active':''">
          <div class="title"
               @click="changeConfig('versionInfo')">
            <!-- <i class="chitutree-h5 chitutreebanbenxinxi"></i> -->
            <p class="name">版本信息</p>
          </div>
        </div>
        <div class="version-info-config config"
             :class="showConfig === 'basicInformation'?'active':''">
          <div class="title"
               @click="changeConfig('basicInformation')">
            <!-- <i class="chitutree-h5 chitutreejibenxinxiicon"></i> -->
            <p class="name">基础信息</p>
          </div>
        </div>

      </div>
      <div v-show="showConfig"
           class="config-board"
           :class="{'min':!isMaxWidth}">
        <div v-show="showConfig === 'senior'"
             class="config-item">
          <h2>
            <a-icon v-if="isMaxWidth"
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-right" />
            <a-icon v-else
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-left" />
            <span>高级配置</span>
            <i class="chitutree-h5 chitutreeguanbi remove"
               @click.stop.prevent="showConfig=''"></i>
          </h2>
          <senior-config ref="seniorConfig"
                         :isShow="showConfig === 'senior'"
                         :isMaxWidth="isMaxWidth"
                         :fileType="fileDetail.fileType"
                         :config="fileDetail.configContent" />
        </div>
        <div v-show="showConfig === 'source'"
             class="config-item">
          <h2>
            <a-icon v-if="isMaxWidth"
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-right" />
            <a-icon v-else
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-left" />
            <span>资源配置</span>
            <i class="chitutree-h5 chitutreeguanbi remove"
               @click.stop.prevent="showConfig=''"></i>
          </h2>
          <source-config ref="sourceConfig"
                         :isMaxWidth="isMaxWidth"
                         :config="fileDetail.sourceContent" />
        </div>
        <!-- 元表配置 -->
        <div v-if="showConfig === 'metaTable'"
             class="config-item">
          <meta-table ref="metaTableConfig"
                      @change="changeMetaTable"
                      :isMaxWidth="isMaxWidth"
                      :dataSourceList="dataSourceList"
                      :fileDetail="fileDetail">
            <template slot="expandIcon">
              <a-icon v-if="isMaxWidth"
                      class="close-drawer"
                      @click="isMaxWidth = !isMaxWidth"
                      type="double-right" />
              <a-icon v-else
                      class="close-drawer"
                      @click="isMaxWidth = !isMaxWidth"
                      type="double-left" />
            </template>
            <i class="chitutree-h5 chitutreeguanbi remove"
               slot="close"
               @click.stop.prevent="showConfig=''"></i>
          </meta-table>
        </div>
        <div v-show="showConfig === 'DS'"
             class="config-item">
          <h2>
            <a-icon v-if="isMaxWidth"
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-right" />
            <a-icon v-else
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-left" />
            <span>DS配置</span>
            <i class="chitutree-h5 chitutreeguanbi remove"
               @click.stop.prevent="showConfig=''"></i>
          </h2>
          <DS-config ref="DSConfig"
                     :isShow="showConfig === 'DS'"
                     :isMaxWidth="isMaxWidth"
                     :fileId="fileDetail.id"
                     :config="fileDetail.dataStreamConfig" />
        </div>
        <div v-show="showConfig === 'versionInfo'"
             class="config-item">
          <h2>
            <a-icon v-if="isMaxWidth"
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-right" />
            <a-icon v-else
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-left" />
            <span>版本信息</span>
            <i class="chitutree-h5 chitutreeguanbi remove"
               @click.stop.prevent="showConfig=''"></i>
          </h2>
          <version-info ref="versionInfoConfig"
                        :isMaxWidth="isMaxWidth"
                        :isShow="showConfig === 'versionInfo'"
                        :detail="fileDetail" />
        </div>
        <div v-show="showConfig === 'basicInformation'"
             class="config-item">
          <h2>
            <a-icon v-if="isMaxWidth"
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-right" />
            <a-icon v-else
                    class="close-drawer"
                    @click="isMaxWidth = !isMaxWidth"
                    type="double-left" />
            <span>基础信息</span>
            <i class="chitutree-h5 chitutreeguanbi remove"
               @click.stop.prevent="showConfig=''"></i>
          </h2>
          <basic-information ref="basicInformation"
                             :isMaxWidth="isMaxWidth"
                             :isShow="showConfig === 'basicInformation'"
                             :detail="fileDetail"
                             type="job"
                             @change="changeMetaTable" />
        </div>
      </div>
      <!-- end -->
    </div>
  </div>
</template>
<script>
  import $ from 'jquery'
  import * as monaco from "monaco-editor"
  import SeniorConfig from './senior-config'
  import SourceConfig from './source-config'
  import DSConfig from './DS-config'
  import VersionInfo from './version-info'
  import MetaTable from './meta-table.vue'
  import DS from './DS.vue'
  import BasicInformation from './basic-information.vue'

  import { format } from 'sql-formatter';
  export default {
    name: "TabContent",
    directives: {
      moveMonaco (el) {
        el.onmousedown = function (e) {
          let id = e.target.id.split('-')[1]
          let init = e.clientY
          let parent = document.getElementById('fixed-' + id)
          let autoEditor = document.getElementById('autoEditor-' + id)
          let drag = document.getElementById('moveDrag-' + id)
          if (!document.getElementById('monacoEditor-' + id)) return
          let heightScale = document.getElementById('monacoEditor-' + id).offsetHeight
          let initHeight = parent.offsetHeight
          document.onmousemove = function (e) {
            let endHight = e.clientY
            let fixHeight = endHight - init + initHeight
            if (heightScale - fixHeight < 30) return
            parent.style.height = fixHeight + 'px'
            if (this.foldFlag) {
              this.fixHeight = fixHeight
            }
            autoEditor.style.height = heightScale - fixHeight - drag.clientHeight - 20 + 'px'
          }
          document.onmouseup = function () {
            document.onmousemove = document.onmouseup = null
          }
        }
      }
    },
    props: {
      contentData: {
        type: Object
      }
    },
    components: {
      SeniorConfig, SourceConfig, DSConfig, VersionInfo, MetaTable, DS, BasicInformation
    },
    mixins: [],
    data () {
      return {
        isMaxWidth: true,
        showConfig: '',//senior,source,metaTable,DS,versionInfo
        monacoInstance: null,
        isShowSenior: false,
        isShowSource: false,
        isShowMetaTable: false,
        isShowVersionInfo: false,
        foldFlag: false,
        fileDetail: {
          id: ''
        },
        fixedEditor: null,
        moveFlag: false,
        fixHeight: 266,
        heightScale: null,
        dataSourceList: [],
        isMove: false,
        test: '',
      }
    },
    computed: {
      isProdEnv () {
        return this.$store.getters.env === 'prod'
      }
    },
    watch: {
      contentData: {
        async handler (val) {
          let isDiffrent = false
          if (val == this.fileDetail) {
            return//避免不断的执行渲染
          }
          if (val.etlContent !== this.fileDetail.etlContent) {
            isDiffrent = true
          }
          const fileDetail = val//这里不能克隆数据，需要跟父页面的数据保持联动，如保存按钮的高亮显示依赖的都是这个对象，实时监听content

          if (fileDetail.configContent && typeof fileDetail.configContent === 'string') {
            fileDetail.configContent = JSON.parse(fileDetail.configContent)
          }
          if (fileDetail.sourceContent && typeof fileDetail.sourceContent === 'string') {
            fileDetail.sourceContent = JSON.parse(fileDetail.sourceContent)
          }
          if (fileDetail.dataStreamConfig && typeof fileDetail.dataStreamConfig === 'string') {
            fileDetail.dataStreamConfig = JSON.parse(fileDetail.dataStreamConfig)
          }
          this.fileDetail = fileDetail
          if (this.monacoInstance) {
            let style = {
              readOnly: false,
              bgColor: '#FFFFFF'
            }
            const flag = await this.isAllowEditFile()
            if (this.fileDetail.lockSign === 0 || !flag) {
              style.readOnly = true
              style.bgColor = '#F6F6F6'
            }
            this.monacoInstance.updateOptions({
              readOnly: style.readOnly
            })
            this.$refs['autoEditor'].children[0].children[0].children[0].style.backgroundColor = style.bgColor
            this.$refs['autoEditor'].children[0].children[0].children[1].children[0].style.backgroundColor = style.bgColor
            if (isDiffrent) {//点回滚时会重新拉取文件内容
              let value = this.contentData.etlContent
              this.monacoInstance.setValue(value)
            }

          }
          // metaTable sql保持最新
          if (this.fixedEditor) {
            let value = this.contentData.metaTableContent || ''
            if (value === null || value.length === 0) {
              value = '-- 该区域不可编辑，只展示元表配置内容'
            }
            this.fixedEditor.setValue(value)
            this.monacoInstance.updateOptions({
              lineNumbers: num => num + this.fixedEditor.getModel().getLineCount()
            })
          }
        },
        immediate: true,
        deep: true
      }
    },
    created () {
      this.fileDetail = this.contentData
      this.getSourceList()
    },
    mounted () {
      this.fileDetail = this.contentData
      if (this.fileDetail.fileType === 'SQL') {
        this.initSql()
        this.heightScale = this.$refs.monacoEditor.clientHeight
      }
      this.move()
      this.foldEvent()
    },
    methods: {
      async isAllowEditFile () {
        if (this.isProdEnv) {
          let res = await this.$http.post('/file/allowEditFile', {
            id: this.fileDetail.id
          }, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            return res.data.allowEdit
          } else {
            return true
          }
        } else {
          return true
        }
      },
      focusLineInEditor (lineNumber) {//在错误栏，点击错误信息跳到编缉器对应的行，在父组件调用
        const line1 = this.fixedEditor.getModel().getLineCount()
        if (lineNumber <= line1) {
          if (this.foldFlag) {
            this.foldEvent()
          }
          this.fixedEditor.revealLineInCenter(lineNumber)
          this.fixedEditor.focus()
          this.fixedEditor.setPosition(new monaco.Position(lineNumber, 0))
        } else {
          const positionLine = lineNumber - line1
          this.monacoInstance.revealLineInCenter(positionLine)
          this.monacoInstance.focus()
          this.monacoInstance.setPosition(new monaco.Position(positionLine, 0))
        }


        // this.monacoInstance.setSelection(new monaco.Range(4, 4, 4, 4));
        // this.monacoInstance.setPosition(new monaco.Position(4, 10));
      },
      // 编辑器折叠
      foldEvent () {
        let fixEditor = document.getElementById('fixed-' + this.fileDetail.id)
        if (!fixEditor) {
          return
        }
        let fixHeight = fixEditor.clientHeight
        let autoEditor = document.getElementById('autoEditor-' + this.fileDetail.id)
        let drag = document.getElementById('moveDrag-' + this.fileDetail.id)
        let move = document.getElementById('move-' + this.fileDetail.id)
        let monacoEditor = document.getElementById('monacoEditor-' + this.fileDetail.id)
        this.foldFlag = !this.foldFlag
        if (this.foldFlag) {
          this.fixHeight = fixHeight
          autoEditor.style.height = monacoEditor.clientHeight - 72 + 'px'
          fixEditor.style.height = 36 + 'px'
          move.style.marginTop = 0 + 'px'
        } else {
          autoEditor.style.height = monacoEditor.clientHeight - this.fixHeight - drag.clientHeight - 20 + 'px'
          fixEditor.style.height = this.fixHeight + 'px'
          move.style.marginTop = 0 + 'px'
        }
      },
      // 获取数据源实例列表
      async getSourceList () {
        let params = {
          dataSourceName: ''
        }
        let res = await this.$http.post('dataSource/getDataSources', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.dataSourceList = res.data
        }
      },
      initFixedEditor () {
        let value = this.contentData.metaTableContent || ''
        if (value === null || value.length === 0) {
          value = '-- 该区域不可编辑，只展示元表配置内容'
        }
        // 放置元表SQL
        this.fixedEditor = monaco.editor.create(
          this.$refs["fixedEditor"], {
          value: value,
          language: 'sql',
          formatOnType: true,
          readOnly: true,
          automaticLayout: true,
          scrollBeyondLastLine: false
        })
        // 通过修改ref子节点背景色，来达到修改moncao编辑器的颜色;原因：manco主题只认一个，一旦设置，会影响所有用到的地方
        this.$refs['fixedEditor'].children[0].children[0].children[0].style.backgroundColor = '#F6F6F6'
        this.$refs['fixedEditor'].children[0].children[0].children[1].children[0].style.backgroundColor = '#F6F6F6'
      },
      // 初始化
      initSql () {
        this.initFixedEditor()
        // 初始化编辑器实例
        let readOnly = false
        if (this.fileDetail.lockSign === 0) {
          readOnly = true
        }
        let value = this.contentData.etlContent
        this.monacoInstance = monaco.editor.create(
          this.$refs["autoEditor"], {
          value: value,
          language: 'sql',
          formatOnType: true,
          readOnly: readOnly,
          automaticLayout: true,
          lineNumbers: num => num + this.fixedEditor.getModel().getLineCount(),
          scrollBeyondLastLine: false
        });

        this.monacoInstance.onDidChangeModelContent(
          () => {
            this.fileDetail.etlContent = this.monacoInstance.getValue()
          });
        this.monacoInstance.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S, () => {
          this.$emit('save')
        })


        // define a document formatting provider
        // then you contextmenu will add an "Format Document" action
        monaco.languages.registerDocumentFormattingEditProvider('sql', {
          provideDocumentFormattingEdits (model, options) {
            var formatted = format(model.getValue(), {
              language: 'plsql',//plsql - Oracle PL/SQL,使用sql，内容中有status这种关键字的时候格式化失败，选项查看SQL Formatter文档
              indent: ' '.repeat(options.tabSize)
            });
            return [
              {
                range: model.getFullModelRange(),
                text: formatted
              }
            ];
          }
        });

        // define a range formatting provider
        // select some codes and right click those codes 
        // you contextmenu will have an "Format Selection" action
        monaco.languages.registerDocumentRangeFormattingEditProvider('sql', {
          provideDocumentRangeFormattingEdits (model, range, options) {
            var formatted = format(model.getValueInRange(range), {
              language: 'plsql',//plsql - Oracle PL/SQL,使用sql，内容中有status这种关键字的时候格式化失败，选项查看SQL Formatter文档
              indent: ' '.repeat(options.tabSize)
            });
            return [
              {
                range: range,
                text: formatted
              }
            ];
          }
        });
      },
      getData () {
        const jobConfig = JSON.parse(JSON.stringify(this.$refs.seniorConfig.jobConfig))
        jobConfig.flinkYaml = this.$refs.seniorConfig.monacoInstance ? this.$refs.seniorConfig.monacoInstance.getValue() : this.$refs.seniorConfig.flinkYaml
        const sourceConfig = JSON.parse(JSON.stringify(this.$refs.sourceConfig.sourceConfig))
        sourceConfig.parallelism = this.$refs.sourceConfig.parallelism
        sourceConfig.jobManagerCpu = this.$refs.sourceConfig.jobManagerCpu
        sourceConfig.jobManagerMem = this.$refs.sourceConfig.jobManagerMem
        sourceConfig.taskManagerCpu = this.$refs.sourceConfig.taskManagerCpu
        sourceConfig.taskManagerMem = this.$refs.sourceConfig.taskManagerMem

        const fileDetail = JSON.parse(JSON.stringify(this.fileDetail))
        fileDetail.jobConfig = jobConfig
        fileDetail.sourceConfig = sourceConfig
        if (fileDetail.fileType === 'DS') {
          const dataStreamConfig = JSON.parse(JSON.stringify(this.$refs.DSConfig.dataStreamConfig))
          fileDetail.dataStreamConfig = dataStreamConfig
        }
        return fileDetail
      },

      changeConfig (value) {
        if (this.showConfig === value) {
          this.showConfig = ''
        } else {
          this.showConfig = value
          this.isMaxWidth = true
        }

      },
      changeMetaTable (data) {
        // console.log('changeMetaTable-flag:', data)
        if (data.flag === 2 || data.flag === 1 || data.flag === 0) {
          this.fileDetail.metaTableContent = data.data
          this.contentData.metaTableContent = data.data
          let value = this.contentData.metaTableContent || ''
          if (value === null || value.length === 0) {
            value = '-- 该区域不可编辑，只展示元表配置内容'
          }
          this.fixedEditor.setValue(value)
          if (data.flag === 2) {
            this.$emit('save', { flag: false })
          } else {
            this.$emit('save')
          }
        }
        if (data.flag !== 0 && data.flag !== 2) {
          this.showConfig = ''
        }
      },

      undo () {
        this.monacoInstance.trigger('', 'undo')
      },
      redo () {
        this.monacoInstance.trigger('', 'redo')
      },
      find () {
        this.monacoInstance.trigger('', 'actions.find');
      },
      beauty () {
        // mannually trigger document formatting by:
        this.monacoInstance.trigger('anyString', "editor.action.formatDocument");
        // mannully tirgger selection formatting by:
        // this.monacoInstance.trigger("editor", "editor.action.formatSelection");
      },
      async unlock () {
        const params = {
          id: this.fileDetail.id
        }
        let res = await this.$http.post('/file/unlockFile', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          // this.fileDetail.lockSign = 1
          this.$message.success('解锁成功')
          this.$bus.$emit('queryFile', this.fileDetail.id, 'unclock')
          this.$bus.$emit('refreshTree')
        } else {
          this.$message.error('解锁成功')
        }
      },
      move () {
        const that = this
        that.isMove = false
        let offset = $(that.$refs.lockJob).offset()
        let left = offset.left
        let top = offset.top
        let clientX = 0
        let clientY = 0
        $(that.$refs.lockJob).off('mousedown').on('mousedown', (e) => {
          that.isMove = true
          that.test = Math.random()
          clientX = e.clientX
          clientY = e.clientY
          offset = $(that.$refs.lockJob).offset()
          left = offset.left
          top = offset.top

        })
        $(document).off('mousemove').on('mousemove', (e) => {
          if (!that.isMove) return;
          const distanceX = e.clientX - clientX
          const distanceY = e.clientY - clientY
          left += distanceX
          top += distanceY
          clientX = e.clientX
          clientY = e.clientY
          $(that.$refs.lockJob).css({ left: left + 'px', top: top + 'px' })

        }).off('mouseup').on('mouseup', () => {
          that.isMove = false;
        })
      }
    }
  }
</script>
<style lang="scss" scoped>
  .tab-content-container-DS {
    // ds作业高度
    height: calc(100% - 60px);
  }
  .tab-content-container-SQL {
    // sql作业高度
    height: calc(100% - 60px);
  }
  .tab-content-container {
    /deep/.monaco-editor .margin-view-overlays .line-numbers {
      user-select: none;
    }
    .tab-content-wrap {
      height: 100%;
      position: relative;
      .code-editor-container {
        // height: calc(100% - 32px);
        height: 100%;
        width: calc(100% - 37px);
        // overflow-y: scroll;
        // &::-webkit-scrollbar {
        //   //整体样式
        //   height: 6px;
        //   width: 6px;
        // }
        // &::-webkit-scrollbar-thumb {
        //   //滑动滑块条样式
        //   border-radius: 6px;
        //   background: #ab90e8;
        //   height: 20px;
        // }
        // &::-webkit-scrollbar-track {
        //   //轨道的样式
        //   background-color: #fff;
        // }
        .fixed-editor-div {
          height: calc(30% - 21px);
          position: relative;
          .fixed-editor {
            height: 100%;
          }
          .click-guide {
            background-color: #f9fafe;
            height: 40px;
            z-index: 10;
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            .text {
              color: #667082;
              height: 100%;
              line-height: 40px;
              padding-left: 20px;
              button {
                color: #0066ff;
                background: none;
                box-shadow: none;
                border: none;
                cursor: pointer;
              }
            }
          }
        }
        .move-drag {
          height: 21px;
          cursor: row-resize;
          .move-div {
            background: #fff;
            text-align: center;
            padding: 4px 0;
            &:hover {
              .up-img {
                background-image: url("~@/assets/icons/fold-up-arrow-blue.png");
                background-size: 100% 100%;
                background-repeat: no-repeat;
              }
              .down-img {
                background-image: url("~@/assets/icons/fold-down-arrow-blue.png");
                background-size: 100% 100%;
                background-repeat: no-repeat;
              }
              .move {
                background-color: #0066ff;
              }
            }
            .move {
              margin: 0;
              padding: 0;
              height: 2px;
              width: 100%;
              background-color: #dee2ea;

              &:hover {
                background-color: #0066ff;
              }
            }
            .up-img {
              height: 10px;
              width: 60px;
              cursor: pointer;
              margin: 0 auto;
              background-image: url("~@/assets/icons/fold-up-arrow.png");
              background-size: 100% 100%;
              background-repeat: no-repeat;
              &:hover {
                background-image: url("~@/assets/icons/fold-up-arrow-blue.png");
                background-size: 100% 100%;
                background-repeat: no-repeat;
              }
            }
            .down-img {
              height: 10px;
              width: 60px;
              cursor: pointer;
              margin: 0 auto;
              background-image: url("~@/assets/icons/fold-down-arrow.png");
              background-size: 100% 100%;
              background-repeat: no-repeat;
              &:hover {
                background-image: url("~@/assets/icons/fold-down-arrow-blue.png");
                background-size: 100% 100%;
                background-repeat: no-repeat;
              }
            }
          }
        }

        .auto-editor {
          height: 88%;
        }
      }
      .lock {
        position: fixed;
        z-index: 100;
        width: 504px;
        height: 52px;
        background: rgba(255, 255, 255, 0.85);
        border-radius: 4px;
        box-shadow: 0px 10px 20px 0px rgba(0, 0, 0, 0.1);
        top: 41px;
        left: 45%;
        // left: 50%;
        // transform: translateX(-50%);
        padding: 0 17px;
        cursor: move;
        .text {
          font-size: 16px;
        }
        .btn {
          width: 92px;
          height: 28px;
          background: #006eff;
          border-radius: 2px;
          color: #fff;
          font-size: 12px;
        }
      }
      .config-board {
        position: absolute;
        right: 36px;
        top: -1px;
        bottom: 0;
        left: 0;
        border: solid 1px #d9d9d9;
        z-index: 10;
        background: #fff;
        box-shadow: -2px 0 3px 0 rgba(0, 0, 0, 0.07);
        &.min {
          left: auto;
          width: 676px;
        }
        .config-item {
          height: 100%;
        }
        h2 {
          height: 32px;
          line-height: 32px;
          padding-left: 16px;
          border-bottom: solid 1px #d9d9d9;
          font-size: 12px;
          span {
            margin-left: 3px;
          }
          .remove {
            float: right;
            margin-right: 16px;
            font-size: 12px !important;
            cursor: pointer;
          }
        }
      }
      .config-container {
        position: absolute;
        right: 0;
        top: 0;
        width: 37px;
        height: 100%;
        border-left: solid 1px #d9d9d9;
        color: #2c2f37;
        background: #fff;
        .config {
          margin-top: 32px;
          cursor: pointer;
          font-size: 12px;
          padding-left: 10px;
          margin-right: 2px;
          &.active {
            color: #0066ff;
            border-right: 2px solid #006fff;
          }
          i {
            display: block;
            margin-left: 2px;
          }
          .name {
            writing-mode: vertical-lr;
            margin-top: 4px;
            margin-left: -1px;
            letter-spacing: 3px;
          }
          p {
            user-select: none;
          }
        }
      }
    }
  }
</style>
