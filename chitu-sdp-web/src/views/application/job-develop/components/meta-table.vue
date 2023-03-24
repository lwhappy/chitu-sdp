<!--
 * @Author: hjg
 * @Date: 2021-12-14 22:32:55
 * @LastEditTime: 2022-09-30 10:06:52
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\application\job-develop\components\meta-table.vue
-->
<template>
  <div class="meta-table"
       ref="metaTable">
    <!-- 上部 -->
    <div class="meta-table-top">
      <div class="top-left">
        <div v-show="unorderedVisible"
             class="unordered-list">
          <div class="unordered-item"
               :class="{'active':key == activeKey,'is-max':isMaxWidth}"
               @click.stop="unorderedChange(key)"
               v-for="(item,key) in metaTableList"
               :key="key">
            <a-tooltip placement="topLeft">
              <template slot="title">
                {{item.flinkTableName}}
              </template>
              <img v-if="item.metaTableType == 'source'"
                   src="@/assets/icons/node-source-icon.png"
                   alt="">
              <img v-else-if="item.metaTableType == 'dim'"
                   src="@/assets/icons/node-operator-icon.png"
                   alt="">
              <img v-else-if="item.metaTableType == 'sink'"
                   src="@/assets/icons/node-sink-icon.png"
                   alt="">
              <span> {{item.flinkTableName}}</span>
            </a-tooltip>
          </div>
        </div>
        <div v-if="$slots['expandIcon']"
             class="expand-icon">
          <slot name="expandIcon" />
        </div>
        <div class="left-wrapper">
          <a-tabs v-model="activeKey"
                  @change="switchMetaTable"
                  type="card">
            <a-tab-pane v-for="(item, index) in metaTableList"
                        :key="index">
              <!-- 重置tab样式 -->
              <span slot="tab">
                <span :class="{'warn-tab' : !metaTableList[index].subFormVerfiy || (metaTableList[index].flinkDdl_msg !== null && metaTableList[index].flinkDdl_msg.length > 0)}">{{ metaTableList[index].flinkTableName || 'table_name' }}</span>
                <span v-if="metaTableList[index].id !== ''">
                  <a-popconfirm placement="topRight"
                                @confirm="() => removeTab(index)">
                    <template slot="title">
                      <p>删除数据后不可恢复，确定要<span class="warn-message">删除</span>吗？</p>
                    </template>
                    <i class="chitutree-h5 chitutreeguanbi remove meta-tab-style"></i>
                  </a-popconfirm>
                </span>
                <i v-else
                   @click="removeTab(index)"
                   class="chitutree-h5 chitutreeguanbi remove meta-tab-style"></i>
              </span>
              <a-collapse class="collapse-container"
                          :default-active-key="[1,2]"
                          @change="collapseChange"
                          :bordered="false">
                <template #expandIcon="props">
                  <a-icon type="caret-right"
                          :rotate="props.isActive ? 90 : 0" />
                </template>
                <a-collapse-panel key="1"
                                  header="元表配置"
                                  :style="customStyle">
                  <!-- 元表表单 -->
                  <div class="meta-form"
                       :class="{'is-max':isMaxWidth}">

                    <div class="form-line">
                      <div class="form-item"
                           :class="{'justify-start':!isMaxWidth}">
                        <div class="form-item-label">
                          <template v-if="metaTableList[index].dataSourceType === 'tidb' || metaTableList[index].dataSourceType === 'hive' || metaTableList[index].dataSourceType === 'hudi'">catalog名称</template>
                          <template v-else><span>*</span>引用名称

                            <a-tooltip placement="topLeft">
                              <template slot="title">
                                <span>示例：引用名称为foo，该元表生成的DDL必须是create table foo (...)，两者需要保持一致</span>
                              </template>
                              <img style="margin-top:-3px"
                                   width="14"
                                   height="14"
                                   src="@/assets/icons/ask.png"
                                   alt="">
                            </a-tooltip>
                          </template>
                        </div>
                        <div class="form-item-value">
                          <a-input style="width: 100%;"
                                   v-model="metaTableList[index].flinkTableName"
                                   @change="metaTableChange(index, 'flinkTableName')"
                                   placeholder="必填，支持英文、数字、下划线。"
                                   :disabled="!isAllowEdit"></a-input>
                          <a-tooltip placement="topLeft">
                            <template slot="title">
                              <span>{{ metaTableList[index].flinkTableName_msg }}</span>
                            </template>
                            <span class="warn-msg"
                                  v-show="metaTableList[index].flinkTableName_msg && metaTableList[index].flinkTableName_msg !== null && metaTableList[index].flinkTableName_msg.length > 0">{{ metaTableList[index].flinkTableName_msg }}</span>
                          </a-tooltip>
                        </div>

                      </div>

                      <div class="form-item"
                           :class="{'justify-start':!isMaxWidth}">
                        <div class="form-item-label">
                          <span>*</span>数据源实例
                        </div>
                        <div class="form-item-value">
                          <a-select placeholder="请选择"
                                    style="width: 100%;"
                                    option-label-prop="label"
                                    show-search
                                    @search="(value) => handleDataSourceSearch(value, index)"
                                    @select="(value) => handleDataSourceSelect(value, index)"
                                    v-model="metaTableList[index].dataSourceName"
                                    :disabled="!isAllowEdit">
                            <a-select-option :key="'dataSource-' + activeKey + '-' + dataSourceItem.id"
                                             :value="dataSourceItem.dataSourceName"
                                             :label="dataSourceItem.dataSourceName"
                                             v-for="dataSourceItem in metaTableList[index].dataSourceList">
                              <span style="font-size: 12px">{{ dataSourceItem.dataSourceName }}</span>
                              &nbsp;
                              <span role="img"
                                    style="font-size: 8px; color: #ccc; font-weight: 300;"
                                    :aria-label="dataSourceItem.dataSourceName">{{ transformDataSourceType(dataSourceItem.dataSourceType) }}</span>
                            </a-select-option>
                          </a-select>
                          <a-tooltip placement="topLeft">
                            <template slot="title">
                              <span>{{ metaTableList[index].dataSourceId_msg }}</span>
                            </template>
                            <span class="warn-msg"
                                  v-show="metaTableList[index].dataSourceId_msg && metaTableList[index].dataSourceId_msg !== null && metaTableList[index].dataSourceId_msg.length > 0">{{ metaTableList[index].dataSourceId_msg }}</span>
                          </a-tooltip>
                        </div>

                      </div>
                    </div>
                    <div v-if="metaTableList[index].dataSourceType !== 'print' && metaTableList[index].dataSourceType !== 'datagen'"
                         class="form-line">
                      <div v-if="metaTableList[index].dataSourceType !== 'hive' && metaTableList[index].dataSourceType !== 'hudi'"
                           class="form-item"
                           :class="{'justify-start':!isMaxWidth}">
                        <div class="form-item-label">
                          {{metaTableList[index].dataSourceType === 'hbase'?'namespace':'数据库'}}
                        </div>
                        <div class="form-item-value">
                          <a-input placeholder="若有,选择数据源实例后,自动带入。"
                                   v-model="metaTableList[index].databaseName"
                                   disabled></a-input>
                        </div>
                      </div>

                      <div class="form-item"
                           :class="{'justify-start':!isMaxWidth}">
                        <div class="form-item-label">
                          数据库类型
                        </div>
                        <div class="form-item-value">
                          <a-input placeholder="选择数据源实例后,自动带入。"
                                   v-model="metaTableList[index].dataSourceType"
                                   disabled></a-input>
                        </div>
                      </div>
                    </div>
                    <template v-if="metaTableList[index].dataSourceType !== 'tidb' && metaTableList[index].dataSourceType !== 'hive' && metaTableList[index].dataSourceType !== 'hudi' && metaTableList[index].dataSourceType !== 'print' && metaTableList[index].dataSourceType !== 'datagen'">
                      <div class="form-line justify-start">
                        <div class="form-item"
                             :class="{'justify-start':!isMaxWidth}">
                          <div class="form-item-label">
                            <span>*</span>{{metaTableList[index].dataSourceType === 'kafka'?'Topic':'表名'}}
                          </div>
                          <div class="form-item-value">
                            <a-input v-model="metaTableList[index].metaTableName"
                                     @change="metaTableChange(index, 'metaTableName')"
                                     placeholder="必填，支持英文、数字、下划线。"
                                     :disabled="!isAllowEdit"></a-input>
                            <a-tooltip placement="topLeft">
                              <template slot="title">
                                <span>{{ metaTableList[index].metaTableName_msg }}</span>
                              </template>
                              <span class="warn-msg"
                                    v-show="metaTableList[index].metaTableName_msg && metaTableList[index].metaTableName_msg !== null && metaTableList[index].metaTableName_msg.length > 0">{{ metaTableList[index].metaTableName_msg }}</span>
                            </a-tooltip>
                          </div>
                        </div>
                        <div class="form-btn-one">
                          <a-button v-if="metaTableList[index].metaTableExist === -1"
                                    class="button-restyle button-confirm"
                                    @click="dataSourceVerify(index)"
                                    :disabled="!isAllowEdit">{{metaTableList[index].metaTableExist | verifyText}}</a-button>
                          <a-button v-else-if="metaTableList[index].metaTableExist === 0"
                                    class="button-restyle button-confirm"
                                    :disabled="!isAllowEdit">{{metaTableList[index].metaTableExist | verifyText}}</a-button>
                          <a-button v-else
                                    class="button-restyle button-confirm btn-success"
                                    :disabled="!isAllowEdit">
                            <a-icon type="check" />{{metaTableList[index].metaTableExist | verifyText}}
                          </a-button>
                        </div>
                      </div>

                      <div class="form-line table-type">
                        <div class="form-item"
                             :class="{'justify-start':!isMaxWidth}">
                          <div class="form-item-label">
                            <span>*</span>元表类型
                            <a-tooltip placement="topLeft">
                              <template slot="title">
                                <span>元表类型在生成ddl时对应不同的with参数模板</span>
                              </template>
                              <img style="margin-top:-3px;margin-left:3px;"
                                   width="14"
                                   height="14"
                                   src="@/assets/icons/ask.png"
                                   alt="">
                            </a-tooltip>
                          </div>
                          <div class="form-item-value">
                            <a-select placeholder="选择类型"
                                      style="width: 100%;"
                                      v-model="metaTableList[index].metaTableType"
                                      @select="handleMetaTableSelect(index)"
                                      :disabled="!isAllowEdit">
                              <a-select-option :key="'metaTable-' + activeKey + '-' + metaTableTypeItem"
                                               :value="metaTableTypeItem"
                                               v-for="metaTableTypeItem in metaTableTypeArr">
                                {{ metaTableTypeItem }}
                              </a-select-option>
                            </a-select>
                          </div>
                          <a-tooltip placement="topLeft">
                            <template slot="title">
                              <span>{{ metaTableList[index].metaTableType_msg }}</span>
                            </template>
                            <span class="warn-msg"
                                  v-show="metaTableList[index].metaTableType_msg && metaTableList[index].metaTableType_msg !== null && metaTableList[index].metaTableType_msg.length > 0">{{ metaTableList[index].metaTableType_msg }}</span>
                          </a-tooltip>
                        </div>
                      </div>
                    </template>
                  </div>
                </a-collapse-panel>
                <a-collapse-panel key="2"
                                  header="DDL"
                                  :style="customStyle">
                  <!-- DDL -->
                  <div class="form-line ddl-wrapper ">
                    <div class="form-btn-two ">
                      <a-button type="primary"
                                size="small"
                                :disabled="(metaTableList.length > 0 && metaTableList[Number(activeKey)] && metaTableList[Number(activeKey)].metaTableExist !== 1 )|| !isAllowEdit"
                                @click="clickGenerate(index);generateDebounce()">生成DDL</a-button>
                      <p class="expand"
                         @click="expandEditor"><i class="chitutree-h5 chitutreeicon_zhankai"></i>展开</p>
                      <span v-show="metaTableList.length > 0 && metaTableList[Number(activeKey)] && metaTableList[Number(activeKey)].productDDlFlag">
                        <span v-if="metaTableList.length > 0 && metaTableList[Number(activeKey)] && metaTableList[Number(activeKey)].metaTableExist !== 1 ">请先验证通过，再重新生成DDL</span>
                        <span v-else>请重新生成DDL</span>
                      </span>
                    </div>
                  </div>
                </a-collapse-panel>
              </a-collapse>

            </a-tab-pane>
          </a-tabs>

          <!-- 编辑器内容 -->
          <div v-show="collapseActive"
               class="editor-ddl-wrapper"
               v-loading="isGenerateDdling">
            <div class="editor-ddl-div expand-editor"
                 :class="{'ddl1':metaTableList[Number(activeKey)] && (metaTableList[Number(activeKey)].dataSourceType === 'tidb' || metaTableList[Number(activeKey)].dataSourceType === 'hive' || metaTableList[Number(activeKey)].dataSourceType === 'hudi'),
             'ddl2':metaTableList[Number(activeKey)] && (metaTableList[Number(activeKey)].dataSourceType === 'print' || metaTableList[Number(activeKey)].dataSourceType === 'datagen')}">
              <div v-if="isExpand"
                   class="expand-top">
                <p class="expand-top-btn"
                   @click="recoverEditor">
                  <i class="chitutree-h5 chitutreeicon_shouqi1"></i>
                  收起
                </p>
              </div>
              <div class="editor-ddl"
                   ref="editorDdl"></div>
            </div>
          </div>
        </div>
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span v-if="metaTableList[Number(activeKey)] && metaTableList[Number(activeKey)].flinkDdl_msg && metaTableList[Number(activeKey)].flinkDdl_msg !== null && metaTableList[Number(activeKey)].flinkDdl_msg.length > 0">{{ metaTableList[Number(activeKey)].flinkDdl_msg }}</span>
          </template>
          <span class="editor-ddl-msg"
                v-if="metaTableList[Number(activeKey)] && metaTableList[Number(activeKey)].flinkDdl_msg  && metaTableList[Number(activeKey)].flinkDdl_msg !== null && metaTableList[Number(activeKey)].flinkDdl_msg.length > 0">{{ metaTableList[Number(activeKey)].flinkDdl_msg }}</span>
        </a-tooltip>
      </div>
      <!-- 新增元表tab -->
      <div class="top-right">
        <div class="sub">
          <div class="unordered-container"
               @click="handleUnordered">
            <a-icon :type="unorderedVisible?'up':'down'" />
          </div>
          <div class="meta-add"
               @click="addMetaTable">
            <a-icon type="plus" />
          </div>
          <div v-if="$slots['close']"
               class="close">
            <slot name="close" />
          </div>
        </div>
      </div>
    </div>
    <!-- 下部 -->
    <div class="footer justify-end">
      <a-popconfirm placement="topRight"
                    @confirm="() => closeForm()">
        <template slot="title">
          <p>确定要关闭元表配置吗？</p>
        </template>
        <a-button style="margin-right:8px">取消</a-button>
      </a-popconfirm>
      <a-button type="primary"
                size="small"
                :disabled="metaTableList.filter(item => item.metaTableExist !== 1 || !item.flinkDdl).length > 0 || submitFormFlag ||!isAllowEdit"
                @click="clickSubmit">保存</a-button>

    </div>
    <metaKafkaAuth ref="metaKafkaAuth" />
  </div>
</template>
<script>
  import $ from 'jquery'
  import * as monaco from "monaco-editor"
  import metaKafkaAuth from "./meta-kafka-auth.vue"
  import _ from 'lodash'
  export default {
    components: {
      metaKafkaAuth
    },
    props: {
      fileDetail: {
        type: Object,
        default: () => {
          return {}
        }
      },
      dataSourceList: {
        type: Array,
        default: () => {
          return []
        }
      },
      isMaxWidth: {
        type: Boolean,
        default: true
      }
    },
    filters: {
      verifyText (value) {
        // console.log('filters: ', value)
        let text = '点击验证'
        if (value === 0) {
          text = '验证中...'
        } else if (value === 1) {
          text = '验证成功'
        }
        return text
      }
    },
    watch: {
      activeKey: {
        handler (val, oldValue) {
          // console.log('activeKey-1: ', val, oldValue, oldValue === '', this.editorDdl.getValue())
          if (oldValue !== '' && this.metaTableList[oldValue]) {
            this.$set(this.metaTableList[oldValue], 'flinkDdl', this.editorDdl.getValue())
            // console.log('activeKey-1: ', this.metaTableList)
          }
          // this.resetEditorValue(this.activeKey)
          if (this.metaTableList[val]) {
            this.resetEditorValue(val)
          } else {
            this.activeKey = oldValue
            this.resetEditorValue(oldValue)
          }
        }
      }
    },
    data () {
      return {
        collapseActive: true,
        customStyle: 'border-radius: 4px;margin-bottom: 0;border: 0;overflow: hidden;background:#FFF',
        unorderedVisible: false,
        submitFormFlag: false,
        activeKey: '',
        metaTableList: [],
        metaTableTypeArr: ['sink', 'dim', 'source'],
        editorDdl: null,
        isExpand: false,
        isGenerateDdling: false,
        isAllowEdit: true,//生产环境是否支持编辑作业
        projectId: ''
      }
    },
    computed: {
      isProdEnv () {
        return this.$store.getters.env === 'prod'
      }
    },
    created () {
      this.getTabList()
    },
    mounted () {
      this.projectId = Number(this.$route.query.projectId)
      this.initEditor()
      if (this.isProdEnv) {
        this.isAllowEditFile()
        // 监听新增/编辑项目，更新isAllowEdit
        this.$bus.$off('allowJobAddEdit').$on('allowJobAddEdit', () => this.isAllowEditFile())
      }
    },
    methods: {
      async isAllowEditFile () {
        let res = await this.$http.post('/file/allowEditFile', {
          id: this.fileDetail.id
        }, {
          headers: {
            projectId: this.projectId
          }
        })
        if (res.code === 0) {
          this.isAllowEdit = res.data.allowEdit
          // 重置编辑器
          if (this.editorDdl) {
            this.resetEditorValue(this.activeKey)
          }
        }
      },
      transformDataSourceType (dataSourceType) {
        let label = dataSourceType
        // if (dataSourceType === 'elasticsearch') {
        //   label = 'elasticsearch6'
        // }
        return label
      },
      recoverEditor () {
        this.isExpand = false
        $('.expand-editor').attr('style', '')
      },
      expandEditor () {
        this.isExpand = true
        let left = $('.main-sidebar-wrapper').width() + $('.tree-container').width()
        console.log(left)
        left -= 15
        let width = $('.main-board-container').width() - 36
        // let bottom = 80
        // let right = 36
        let top = 70
        let height = $('.board-tab').height()
        height -= 60
        let background = '#fff'
        const css = {
          left: left,
          // right: right,
          // bottom: bottom,
          top: top,
          width: width,
          height: height,
          background: background,
          position: 'fixed',
          zIndex: 10
        }

        this.$nextTick(() => {
          $('.expand-editor').css(css)
        })
      },
      metaTableChange (index, key) {
        this.metaTableList[index].flinkDdl = ''
        if (key === 'metaTableName') {
          this.metaTableList[index].metaTableExist = -1
        }
        this.$set(this.metaTableList[index], 'productDDlFlag', true)
        this.editorDdl.setValue(this.metaTableList[index].flinkDdl)
      },
      // 新增元表
      addMetaTable () {
        if (!this.isAllowEdit) {
          this.$message.warning('为了规范作业流程，生产环境不允许直接编辑作业，建议先在UAT环境进行作业开发后同步至生产环境~')
          return
        }
        this.unorderedVisible = false
        // console.log('addMetaTable')
        let metaTableInfo = {
          id: '',
          dataSourceList: JSON.parse(JSON.stringify(this.dataSourceList)),
          flinkTableName: 'table_name', // 引用名称
          metaTableName: '', // 元表名称
          metaTableType: undefined, // 元表类型
          dataSourceId: undefined, // 数据源ID
          flinkDdl: '', // ddl
          metaTableExist: -1,
          valueVerify: false,
          flinkTableName_msg: null,
          dataSourceId_msg: null,
          metaTableName_msg: null,
          metaTableType_msg: null,
          flinkDdl_msg: null,
          subFormVerfiy: true,
          productDDlFlag: false
        }
        this.metaTableList.push(metaTableInfo)
        let activeKey = this.metaTableList.length - 1
        this.activeKey = activeKey
        // this.resetEditorValue(activeKey)
        this.$forceUpdate()
      },
      clickGenerate (index) {
        this.generateIndex = index
      },
      generateDebounce: _.debounce(function () {//这种写法防抖才生效
        console.log('debounce')
        this.generateDdl(this.generateIndex)
      }, 500),
      // 生成ddl
      async generateDdl (index) {
        console.log('index', index)
        let flag = true
        let reg = /^[0-9a-zA-Z]([0-9a-zA-Z_]{0,100})$/
        if (!reg.test(this.metaTableList[index].flinkTableName)) {
          // return this.$message.warning({ content: '引用名称不能为空，1-50个字符，支持英文、数字和下划线', duration: 2 })
          this.$set(this.metaTableList[index], 'flinkTableName_msg', '1-100个字符，英文、数字和下划线，不支持以下划线开头')
          flag = false
        } else {
          this.$set(this.metaTableList[index], 'flinkTableName_msg', null)
        }
        if (this.metaTableList[index].dataSourceId === null || this.metaTableList[index].dataSourceId === undefined) {
          // return this.$message.warning({ content: '请先选择数据源实例', duration: 2 })
          this.$set(this.metaTableList[index], 'dataSourceId_msg', '请先选择数据源实例')
          flag = false
        } else {
          this.$set(this.metaTableList[index], 'dataSourceId_msg', null)
        }
        if ((this.metaTableList[index].dataSourceType !== 'tidb' && this.metaTableList[index].dataSourceType !== 'hive' && this.metaTableList[index].dataSourceType !== 'hudi' && this.metaTableList[index].dataSourceType !== 'print' && this.metaTableList[index].dataSourceType !== 'datagen') && (this.metaTableList[index].metaTableType === null || this.metaTableList[index].metaTableType === undefined)) {
          flag = false
          this.$set(this.metaTableList[index], 'metaTableType_msg', '请先选择元表类型')
          this.$set(this.metaTableList[index], 'subFormVerfiy', false)
        } else {
          this.$set(this.metaTableList[index], 'metaTableType_msg', null)
        }
        this.$forceUpdate()
        if (!flag) return
        let params = {
          dataSourceId: this.metaTableList[index].dataSourceId,
          flinkTableName: this.metaTableList[index].flinkTableName,
          metaTableName: this.metaTableList[index].metaTableName,
          fileId: this.fileDetail.id,
          metaTableType: this.metaTableList[index].metaTableType
        }
        this.isGenerateDdling = true
        let res = await this.$http.post('/meta/table/generateDdl', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isGenerateDdling = false
        // console.log('generateDdl-res: ', res)
        if (res.code === 0) {
          this.$set(this.metaTableList[index], 'flinkDdl', res.data)
          this.$set(this.metaTableList[index], 'flinkDdl_msg', '')
          this.$set(this.metaTableList[index], 'productDDlFlag', false)
          this.resetEditorValue(index)
          this.valueVerfiy()
          // // console.log('hh1: ', this.metaTableList.filter(item => item.metaTableExist !== 1))
          // // console.log('hh2: ', this.submitFormFlag)
        } else {
          this.$set(this.metaTableList[index], 'flinkDdl_msg', res.msg)
          this.$message.warning({ content: res.msg, duration: 2 })
        }
      },
      // 数据源验证
      dataSourceVerify (index) {
        let flag = true
        if (this.metaTableList[index].dataSourceId === null || this.metaTableList[index].dataSourceId === undefined) {
          // return this.$message.warning({ content: '请先选择数据源实例', duration: 2 })
          this.$set(this.metaTableList[index], 'dataSourceId_msg', '请先选择数据源实例')
          flag = false
        } else {
          this.$set(this.metaTableList[index], 'dataSourceId_msg', null)
        }
        this.$forceUpdate()
        // // console.log('flag: ', flag, reg, reg.test(this.metaTableList[index].metaTableName), this.metaTableList[index].metaTableName)
        if (!flag) return
        this.dataSourceVerifyHttp(index)
      },
      // 数据源验证请求
      async dataSourceVerifyHttp (index) {
        this.$set(this.metaTableList[index], 'metaTableExist', 0)
        this.$forceUpdate()
        let params = {
          dataSourceId: this.metaTableList[index].dataSourceId,
          metaTableName: this.metaTableList[index].metaTableName
        }
        let res = await this.$http.post('/meta/table/existVerify', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        // console.log('dataSourceVerify-res: ', res)
        if (res.code === 0) {
          if (res.data) {
            this.$set(this.metaTableList[index], 'metaTableExist', 1)
            this.$set(this.metaTableList[index], 'metaTableName_msg', null)
          } else {
            this.$set(this.metaTableList[index], 'metaTableExist', -1)
            // this.$message.warning({ content: '数据库中没有该元表，请重新输入', duration: 2 })
            this.$set(this.metaTableList[index], 'metaTableName_msg', '数据库中没有该元表，请重新输入')
          }
        } else {
          this.$set(this.metaTableList[index], 'metaTableExist', -1)
          this.$message.error({ content: res.msg, duration: 2 })
        }
        this.$forceUpdate()
      },
      // 数据源实例选中
      handleDataSourceSelect (val, index) {
        let dataSourceInfo = this.metaTableList[index].dataSourceList.filter(dataSourceItem => dataSourceItem.dataSourceName === val)[0]
        this.$set(this.metaTableList[index], 'dataSourceId', dataSourceInfo.id)
        this.$set(this.metaTableList[index], 'dataSourceName', dataSourceInfo.dataSourceName)
        this.$set(this.metaTableList[index], 'databaseName', dataSourceInfo.databaseName)
        this.$set(this.metaTableList[index], 'dataSourceType', dataSourceInfo.dataSourceType)
        this.$set(this.metaTableList[index], 'flinkDdl', '')
        if (dataSourceInfo.dataSourceType === 'hive' || dataSourceInfo.dataSourceType === 'hudi' || dataSourceInfo.dataSourceType === 'tidb' || dataSourceInfo.dataSourceType === 'print' || dataSourceInfo.dataSourceType === 'datagen') {//hive和tidb不需要验证
          this.$set(this.metaTableList[index], 'metaTableExist', 1)
        } else {
          this.$set(this.metaTableList[index], 'metaTableExist', -1)
        }

        if (this.metaTableList[index].metaTableName.length > 0) {
          this.$set(this.metaTableList[index], 'productDDlFlag', true)
        }
        this.editorDdl.setValue('')
        this.$forceUpdate()
      },
      // 数据源实例搜索
      async handleDataSourceSearch (val, index) {
        // console.log('handleDataSourceSearch: ', val, index)
        let params = {
          dataSourceName: val
        }
        let res = await this.$http.post('dataSource/getDataSources', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        // console.log('handleDataSourceSearch-res: ', res)
        if (res.code === 0) {
          this.$set(this.metaTableList[index], 'dataSourceList', res.data)
          // console.log('sss', this.metaTableList)
          this.$forceUpdate()
        }
      },
      // 初始化editor
      initEditor () {
        if (this.editorDdl !== null) return
        this.editorDdl = monaco.editor.create(
          this.$refs['editorDdl'], {
          value: '',
          language: 'sql',
          formatOnType: true,
          readOnly: true,
          minimap: {
            enabled: false
          },
          automaticLayout: true,
          scrollBeyondLastLine: false
        })
        //通过修改ref子节点背景色，来达到修改moncao编辑器的颜色;原因：manco主题只认一个，一旦设置，会影响所有用到的地方
        this.$refs['editorDdl'].children[0].children[0].children[0].style.backgroundColor = '#F6F6F6'
        this.$refs['editorDdl'].children[0].children[0].children[1].children[0].style.backgroundColor = '#F6F6F6'
      },
      resetEditStyle (index) {
        let style = {
          readOnly: false,
          bgColor: '#F6F6F6'
        }

        if (this.metaTableList[index].metaTableExist === 1 && this.isAllowEdit) {
          style.readOnly = false
          style.bgColor = '#FFFFFF'
        } else {
          style.readOnly = true
          style.bgColor = '#F6F6F6'
        }
        this.editorDdl.updateOptions({
          readOnly: style.readOnly
        })
        this.$refs['editorDdl'].children[0].children[0].children[0].style.backgroundColor = style.bgColor
        this.$refs['editorDdl'].children[0].children[0].children[1].children[0].style.backgroundColor = style.bgColor
      },
      // 重置editor 内容
      resetEditorValue (index) {
        // console.log('resetEditorValue-index: ', this.metaTableList[index].flinkDdl)
        // if (this.activeKey !== index) return
        this.resetEditStyle(index)
        this.editorDdl.setValue(this.metaTableList[index].flinkDdl)
        this.$forceUpdate()
      },
      // 关闭元表配置
      closeForm () {
        // console.log('closeForm')
        this.$emit('change', { flag: -1 })
      },
      clickSubmit:
        _.debounce(function () {
          console.log('debounce')
          this.submitForm()
        }, 500),
      // 确认保存
      async submitForm () {
        // console.log('submitForm')
        if (this.metaTableList.filter(item => item.metaTableExist !== 1).length > 0 || this.submitFormFlag) {
          return
        }
        this.submitFormFlag = true
        this.$set(this.metaTableList[this.activeKey], 'flinkDdl', this.editorDdl.getValue())
        this.valueVerfiy()
        this.$forceUpdate()
        let verfiyArr = this.metaTableList.filter(item => !item.subFormVerfiy)
        if (verfiyArr.length > 0) return
        let params = []
        this.metaTableList.forEach(item => {
          let obj = {
            fileId: this.fileDetail.id,
            flinkTableName: item.flinkTableName,
            dataSourceId: item.dataSourceId,
            metaTableName: item.metaTableName,
            metaTableType: item.metaTableType,
            flinkDdl: item.flinkDdl
          }
          if (item.id) {
            obj.id = item.id
          }
          params.push(obj)
        })
        let res = await this.$http.post('/meta/table/add', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.submitFormFlag = false
        if (res.code === 0) {
          this.$emit('change', { flag: 1, data: res.data })
          // this.$message.success({ content: '保存成功', duration: 2 })
        } else {
          if (res.code === 8502) { // sql 验证错误
            let msgArr = res.data
            msgArr.forEach(item => {
              let index = this.metaTableList.findIndex(metaTableInfo => metaTableInfo.flinkTableName === item.flinkTableName)
              if (index !== -1) {
                this.$set(this.metaTableList[index], 'subFormVerfiy', false)
                this.$set(this.metaTableList[index], 'flinkDdl_msg', item.errorMsg)
              }
            })
            this.$forceUpdate()
            this.$message.warning({ content: res.msg, duration: 2 })
          } else if (res.code === 7502) {//文件被锁定
            this.$bus.$emit('refreshTree')//更新目录树
            this.$bus.$emit('queryFile', this.fileDetail.id, 'lock')//拉取文件详情更新锁定状态
            this.$message.warning({ content: res.msg, duration: 2 })
            this.getTabList()
          } else if (res.code === 8515) { // kafka校验不通过
            this.$refs.metaKafkaAuth.isShowKafkaAuthDialog = true
            this.$emit('change', { flag: 2, data: res.data })
          } else {
            if (res.msg)
              this.$message.error({ content: res.msg, duration: 2 })
          }
        }
        return res
      },
      // 表单字段校验
      valueVerfiy () {
        this.submitFormFlag = false
        let reg = /^[0-9a-zA-Z]([0-9a-zA-Z_]{0,50})$/
        this.metaTableList.forEach((item, index) => {
          this.$set(this.metaTableList[index], 'subFormVerfiy', true)
          // this.$set(this.metaTableList[index], 'flinkDdl_msg', null)
          if (item.dataSourceType !== 'print' && item.dataSourceType !== 'datagen' && !reg.test(item.flinkTableName)) {
            this.$set(this.metaTableList[index], 'flinkTableName_msg', '1-50个字符，英文、数字和下划线，不支持以下划线开头')
            this.$set(this.metaTableList[index], 'subFormVerfiy', false)
          } else {
            this.$set(this.metaTableList[index], 'flinkTableName_msg', null)
          }
          if (item.dataSourceId === null || item.dataSourceId === undefined) {
            this.$set(this.metaTableList[index], 'dataSourceId_msg', '请先选择数据源实例')
            this.$set(this.metaTableList[index], 'subFormVerfiy', false)
          } else {
            this.$set(this.metaTableList[index], 'dataSourceId_msg', null)
          }
          reg = /^[0-9a-zA-Z_]{1,50}$/
          // if (!reg.test(item.metaTableName)) {
          //   this.$set(this.metaTableList[index], 'metaTableName_msg', '1-50个字符，英文、数字和下划线')
          //   this.$set(this.metaTableList[index], 'subFormVerfiy', false)
          // } else {
          //   this.$set(this.metaTableList[index], 'metaTableName_msg', null)
          // }
          if ((item.dataSourceType !== 'tidb' && item.dataSourceType !== 'hive' && item.dataSourceType !== 'hudi' && item.dataSourceType !== 'print' && item.dataSourceType !== 'datagen') && (item.metaTableType === null || item.metaTableType === undefined)) {
            this.$set(this.metaTableList[index], 'metaTableType_msg', '请先选择元表类型')
            this.$set(this.metaTableList[index], 'subFormVerfiy', false)
          } else {
            this.$set(this.metaTableList[index], 'metaTableType_msg', null)
          }
          if (item.flinkDdl.length === 0) {
            this.$set(this.metaTableList[index], 'flinkDdl_msg', 'ddl不能为空')
            this.$set(this.metaTableList[index], 'subFormVerfiy', false)
          }
        })
        this.$forceUpdate()
        // // console.log('valueVerfiy-metaTableList: ', this.metaTableList)
      },
      // 元表tab切换
      switchMetaTable (key) {
        console.log('switchMetaTable-key: ', key)
        // this.resetEditorValue(key)
      },
      unorderedChange (key) {
        this.activeKey = key
      },
      handleUnordered () {
        this.unorderedVisible = !this.unorderedVisible
      },

      // 删除元表
      removeTab (index) {
        if (!this.isAllowEdit) {
          this.$message.warning('为了规范作业流程，生产环境不允许直接编辑作业，建议先在UAT环境进行作业开发后同步至生产环境~')
          return
        }
        // console.log(index)
        if (this.metaTableList[index].id && this.metaTableList[index].id !== '') {
          this.deleteMetaTable(this.metaTableList[index].id, index)
        } else {
          this.removeTabOperate(index)
        }
      },
      // 删除元表
      async deleteMetaTable (id, index) {
        let params = {
          id: id
        }
        let res = await this.$http.post('/meta/table/delete', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.removeTabOperate(index)
          this.$emit('change', { flag: 0, data: res.data })
          this.$message.success({ content: '删除成功', duration: 2 })
        } else {
          if (res.code === 7502) {//文件被锁定
            this.$bus.$emit('refreshTree')//更新目录树
            this.$bus.$emit('queryFile', this.fileDetail.id, 'lock')//拉取文件详情更新锁定状态
            this.getTabList()
          }
          this.$message.warning({ content: res.msg, duration: 2 })
        }
      },
      // 删除tab操作
      removeTabOperate (index) {
        if (index === this.metaTableList.length - 1) {
          if (this.metaTableList.length === 1) {
            this.addMetaTable()
            index = 1
            this.$set(this.metaTableList[0], 'id', '')
            this.$set(this.metaTableList[0], 'dataSourceList', JSON.parse(JSON.stringify(this.dataSourceList)))
            this.$set(this.metaTableList[0], 'flinkTableName', 'table_name')
            this.$set(this.metaTableList[0], 'metaTableName', '')
            this.$set(this.metaTableList[0], 'dataSourceName', undefined)
            this.$set(this.metaTableList[0], 'metaTableType', undefined)
            this.$set(this.metaTableList[0], 'dataSourceId', undefined)
            this.$set(this.metaTableList[0], 'flinkDdl', '')
            this.$set(this.metaTableList[0], 'metaTableExist', -1)
            this.$set(this.metaTableList[0], 'databaseName', '')
            this.$set(this.metaTableList[0], 'dataSourceType', undefined)
            this.$set(this.metaTableList[0], 'valueVerify', false)
            this.$set(this.metaTableList[0], 'flinkTableName_msg', null)
            this.$set(this.metaTableList[0], 'dataSourceId_msg', null)
            this.$set(this.metaTableList[0], 'metaTableName_msg', null)
            this.$set(this.metaTableList[0], 'metaTableType_msg', null)
            this.$set(this.metaTableList[0], 'flinkDdl_msg', null)
            this.$set(this.metaTableList[0], 'subFormVerfiy', true)
            this.resetEditorValue(index)
          } else {
            this.$set(this.metaTableList[index], 'metaTableExist', this.metaTableList[index - 1].metaTableExist)
            this.$set(this.metaTableList[index], 'flinkDdl', this.metaTableList[index - 1].flinkDdl)
          }
          this.activeKey = index - 1
          // this.switchMetaTable(this.activeKey)
        } else {
          // // console.log('hhh2')
          this.$set(this.metaTableList[index], 'metaTableExist', this.metaTableList[index + 1].metaTableExist)
          this.$set(this.metaTableList[index], 'flinkDdl', this.metaTableList[index + 1].flinkDdl)
          this.resetEditorValue(index + 1)
        }
        this.metaTableList.splice(index, 1)
        this.$forceUpdate()
      },
      // 获取元表配置列表
      async getTabList () {
        let params = {
          fileId: this.fileDetail.id
        }
        let res = await this.$http.post('meta/table/query', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          if (res.data.length > 0) {
            this.metaTableList = res.data
          } else {
            let obj = {
              id: '',
              flinkTableName: 'table_name', // 引用名称
              metaTableName: '', // 元表名称
              metaTableType: undefined, // 元表类型
              dataSourceId: undefined, // 数据源ID
              flinkDdl: '' // ddl
            }
            this.metaTableList.push(obj)
          }
          this.metaTableList.forEach(item => {
            item.dataSourceList = JSON.parse(JSON.stringify(this.dataSourceList))
            if (item.dataSourceId !== undefined) {
              let dataSourceInfo = this.dataSourceList.filter(dataSourceItem => dataSourceItem.id === item.dataSourceId)[0]
              if (!dataSourceInfo) return
              item.dataSourceName = dataSourceInfo.dataSourceName || null
              item.databaseName = dataSourceInfo.databaseName || null
              item.dataSourceType = dataSourceInfo.dataSourceType || null
              item.valueVerify = true
              item.metaTableExist = 1
            } else {
              item.valueVerify = false
              item.metaTableExist = -1
            }
            item.flinkTableName_msg = null
            item.dataSourceId_msg = null
            item.metaTableName_msg = null
            item.metaTableType_msg = null
            item.flinkDdl_msg = null
            item.subFormVerfiy = true
            item.productDDlFlag = false
          })
          this.activeKey = 0
          // this.resetEditorValue(Number(this.activeKey))
          // // console.log('metaTableList: ', this.metaTableList)
        }
      },
      collapseChange (key) {
        if (key.includes('2')) {
          this.collapseActive = true
        } else {
          this.collapseActive = false
        }
      },
      handleMetaTableSelect (index) {
        this.$set(this.metaTableList[index], 'flinkDdl', '')
        this.editorDdl.setValue('')
      }
    }
  }
</script>
<style lang="scss" scoped>
  .collapse-container {
    /deep/ .ant-collapse-item {
      font-size: 12px;
      .ant-collapse-header {
        font-size: 14px !important;
        font-weight: 900;
        position: relative;
        padding: 5px 16px;
        padding-left: 40px;
        color: rgba(0, 0, 0, 0.85);
        line-height: 22px;
        cursor: pointer;
        transition: all 0.3s;
        i {
          color: #006fff !important;
        }
      }
      .ant-collapse-content-box {
        padding: 5px 16px 0 16px;
      }
    }
  }
  .common-title {
    padding: 10px;
    i {
      color: #006fff;
      margin-right: 8px;
    }
    .name {
      font-size: 14px;
      font-weight: 900;
    }
  }
  .warn-message {
    color: #faad14;
  }
  .meta-table {
    width: 100%;
    height: 100%;
    font-size: 12px;

    .meta-form {
      &.is-max {
        display: flex;
        flex-wrap: wrap;
        .form-line {
          width: calc(50% - 6px);
          margin-right: 6px;
          display: flex;
          justify-content: space-between;
          margin-bottom: 25px;
          .form-btn-one {
            padding-bottom: 0 !important;
            padding-top: 22px !important;
          }
          .form-item {
            width: calc(50% - 6px);
            padding-bottom: 0 !important;

            .form-item-value {
              width: 100% !important;
            }
          }
        }
      }
    }

    .meta-table-top {
      width: 100%;
      height: calc(100% - 50px);
      position: relative;
      .expand-icon {
        position: absolute;
        left: 0;
        top: 6px;
        z-index: 10;
      }
      .top-left {
        width: 100%;
        height: 100%;
        position: relative;
        .unordered-list {
          width: 100%;
          padding: 16px;
          min-height: 70px;
          max-height: 260px;
          overflow-y: auto;
          position: absolute;
          top: 34px;
          left: 0;
          z-index: 99;
          display: flex;
          flex-wrap: wrap;
          box-sizing: border-box;
          cursor: auto;
          background: #ffffff;
          border-radius: 0px 0px 4px 4px;
          box-shadow: 0px 10px 20px 0px rgba(179, 189, 207, 0.5);
          .unordered-item {
            width: calc(33.3% - 12px);
            padding: 0 8px;
            height: 30px;
            line-height: 30px;
            box-sizing: border-box;
            margin: 5px 12px 5px 0;
            box-sizing: border-box;
            background: #fafbfc;
            color: #667082;
            border: 1px solid #e8ebf1;
            border-radius: 4px;
            cursor: pointer;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            &.is-max {
              width: calc(20% - 12px);
            }
            &:hover {
              color: #0066ff;
              background: #e5f0ff;
              border: 1px solid #7fb7ff;
            }
            &.active {
              color: #0066ff;
              background: #e5f0ff;
              border: 1px solid #7fb7ff;
            }
            img {
              width: 20px;
              margin-right: 8px;
              margin-bottom: 4px;
            }
          }
        }
        .left-wrapper {
          height: 100%;
          display: flex;
          justify-content: flex-start;
          align-items: stretch;
          flex-direction: column;
          /deep/ .ant-tabs {
            height: auto;
          }
          .editor-ddl-wrapper {
            flex: 1;
            height: 0;
            position: relative;
            .editor-ddl-div {
              width: calc(100% - 32px);
              // margin-top: -20px;
              margin-left: 16px;
              border: 1px solid #d9d9d9;
              position: absolute;
              top: 0;
              height: 100%;
              // &.ddl1 {
              //   top: 240px;
              //   height: calc(100% - 240px - 52px);
              // }
              // &.ddl2 {
              //   top: 180px;
              //   height: calc(100% - 180px - 52px);
              // }
              .expand-top {
                padding-left: 12px;
                height: 40px;
                display: flex;
                align-items: center;
                color: #0066ff;
                border-bottom: solid 1px #d9d9d9;
                .expand-top-btn {
                  cursor: pointer;
                  width: 45px;
                }
              }
              .editor-ddl {
                width: 100%;
                // height: 380px;
                height: 100%;
              }
            }
          }
        }
        /deep/ .ant-tabs-bar {
          padding-left: 16px;
        }
        /deep/
          .ant-tabs.ant-tabs-card
          .ant-tabs-card-bar
          .ant-tabs-nav-container {
          height: 32px;
          width: calc(100% - 63px);
        }
        /deep/ .ant-tabs-tab-active {
          color: #333;
        }
        /deep/ .ant-tabs-nav .ant-tabs-tab {
          margin: 0 !important;
          padding: 0 8px !important;
          border-radius: 0;
          border-right: none;
          border-top: none;
          user-select: none;
          &:nth-last-child(1) {
            border-right: 1px solid #d9d9d9;
          }
          .warn-tab {
            color: #ff4747;
          }
          .meta-tab-style {
            display: inline-block;
            margin-left: 8px;
            transform: scale(0.8);
          }
        }
        .meta-form {
          padding: 0 16px;
          height: auto;
          .form-line {
            // display: flex;
            // justify-content: space-between;
            // margin-bottom: 12px;
            // height: 52px;
            .form-item {
              padding-bottom: 25px;
              // width: calc(50% - 6px);
              .form-item-label {
                font-size: 12px;
                width: 130px;
                span {
                  color: red;
                }
              }
              .form-item-value {
                height: 28px;
                width: 325px;
                font-size: 12px;
                // input {
                //   height: 100%;
                // }
                // /deep/ .ant-select-selection--single {
                //   height: 28px;
                // }
                // /deep/ .ant-select-selection__rendered {
                //   line-height: 25px;
                // }
              }
              .warn-msg {
                color: #ff1414;
                display: inline-block;
                width: 100%;
                text-overflow: ellipsis;
                white-space: nowrap;
                overflow: hidden;
              }
            }
            .form-text {
              width: 100%;
              .form-text-label {
                font-weight: bold;
                span {
                  color: red;
                }
              }
              .count-message {
                position: relative;
                /deep/ textarea {
                  height: 64px;
                  resize: none;
                  overflow-y: auto;
                }
                .count-info {
                  position: absolute;
                  bottom: 0;
                  right: 12px;
                  color: #999;
                }
              }
            }
            .form-btn-one {
              width: calc(50% - 6px);
              padding-bottom: 17px;
              margin-left: 20px;
              .button-restyle {
                margin-left: 0;
              }
              i {
                margin-right: 2px;
              }
              .btn-success {
                background-color: #52c41a;
              }
              .btn-success[disabled] {
                background-color: #f5f5f5;
              }
              /deep/ .ant-btn > .anticon + span {
                margin-left: 0;
              }
            }
          }
        }

        .editor-ddl-msg {
          padding-left: 16px;
          display: inline-block;
          width: 305px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          color: #ff1414;
        }
      }
      .top-right {
        width: 60px;
        padding: 0 7px;
        color: #0066ff;
        width: auto;
        height: 32px;
        line-height: 32px;
        cursor: pointer;
        border-left: 1px solid #d9d9d9;
        border-bottom: 1px solid #d9d9d9;
        position: absolute;
        top: 0;
        right: 0;
        box-shadow: -2px 0 3px 0 rgba(0, 0, 0, 0.07);
        i {
          margin-right: 10px;
        }
        .sub {
          display: flex;
          justify-content: flex-end;
          align-items: center;
          .unordered-container {
            position: relative;
            padding: 0 5px;
          }
          .close {
            margin-left: 10px;
            i {
              margin-right: 0;
              color: #333;
            }
          }
        }
      }
    }
    .footer {
      height: 30px;
      margin: 10px 0;
      padding-right: 16px;
    }
    .form-btn-two {
      // width: calc(50% - 6px);
      padding: 0 20px 10px 20px;
      width: 100%;
      box-sizing: border-box;
      display: flex;
      align-items: center;
      font-size: 12px;
      // justify-content: space-between;
      .expand {
        cursor: pointer;
        color: #0066ff;
        font-size: 12px;
        width: 45px;
        margin-left: 10px;
        i {
          margin-right: 2px;
        }
      }
      .button-restyle {
        margin-left: 0;
        margin-right: 10px;
      }
      span {
        color: red;
      }
    }
  }
</style>
