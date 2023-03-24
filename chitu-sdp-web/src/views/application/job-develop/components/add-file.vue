<template>
  <a-modal v-if="isShow"
           wrapClassName="add-file-dialog"
           :mask-closable="false"
           v-model="isShow"
           :footer="null"
           :title="title"
           v-drag
           width="600px">
    <!-- <span slot="closeIcon">x</span> -->
    <div class="add-file">
      <a-form-model :model="form">
        <div class="form-body">
          <a-form-model-item>
            <p class="label"><span>*</span>文件名称</p>
            <a-input v-model="form.fileName"
                     v-decorator="[
          'fileName',
          { rules: [{ required: true, message: '请输入文件名称' }] },
        ]"
                     placeholder="请输入文件名称" />
          </a-form-model-item>
          <a-form-model-item>
            <p class="label">
              <span>*</span>作业等级
              <img class="question"
                   width="14"
                   height="14"
                   src="@/assets/icons/ask.png"
                   alt=""
                   @click="openDrawer" />
            </p>
            <a-select v-model="defaultPriority"
                      label-in-value
                      @select="handerLevelChange">
              <a-select-option v-for="item in priorityList"
                               :value="JSON.stringify(item)"
                               :key="item.label">{{ priorityPrefix }}{{ item.label }}</a-select-option>

            </a-select>
          </a-form-model-item>
          <a-form-model-item>
            <p class="label">文件类型</p>
            <a-select v-model="fileType"
                      @change="handleTypeChange">
              <a-select-option v-for="item in fileTypeList"
                               :key="item.value"
                               :value="item.value">
                {{item.value}}
              </a-select-option>
            </a-select>
          </a-form-model-item>
          <template v-if="fileType==='DS'">
            <a-form-model-item>
              <p class="label"><span>*</span>选择jar包资源</p>
              <a-select placeholder="请选择jar包名称"
                        class="select-source"
                        dropdownClassName="select-source-option"
                        show-search
                        label-in-value
                        :allow-clear="true"
                        v-model="source"
                        :filter-option="false"
                        :not-found-content="isFetchingSource ? undefined : null"
                        @search="handleSourceSearch"
                        @change="handleChangeSource">
                <a-spin v-if="isFetchingSource"
                        slot="notFoundContent"
                        size="small" />
                <a-select-option v-for="(item,index) in sourceData"
                                 :value="JSON.stringify(item)"
                                 :key="String(item.id) + index">
                  <a-tooltip placement="topLeft">
                    <template slot="title">
                      <div>
                        <p class="name"> {{ item.name }}</p>
                        <p class="description">{{item.description}}</p>
                      </div>
                    </template>
                    <div>
                      <div>
                        <p class="name"> {{ item.name }}</p>
                        <p class="description">{{item.description}}</p>
                      </div>
                    </div>
                  </a-tooltip>
                </a-select-option>
              </a-select>

            </a-form-model-item>
            <a-form-model-item>
              <p class="label"><span>*</span>选择jar包版本</p>
              <a-select placeholder="请选择jar包版本"
                        class="select-source"
                        dropdownClassName="select-source-option"
                        show-search
                        label-in-value
                        :allow-clear="true"
                        v-model="jarVersion"
                        :filter-option="false"
                        :not-found-content="isFetchingJarVersion ? undefined : null"
                        @search="handleJarVersionSearch"
                        @change="handleChangeJarVersion">
                <a-spin v-if="isFetchingJarVersion"
                        slot="notFoundContent"
                        size="small" />
                <a-select-option v-for="(item,index) in jarVersionData"
                                 :value="JSON.stringify(item)"
                                 :key="String(item.id) + index"
                                 @mouseenter="jarVersionMouseenter(item)">
                  <a-tooltip placement="topLeft">
                    <template slot="title">
                      <div>
                        <p class="name"> {{ item.version }}</p>
                        <p class="description">{{item.description}}</p>
                      </div>
                    </template>
                    <div>
                      <div>
                        <p class="name"> {{ item.version }}</p>
                        <p class="description">{{item.description}}</p>
                      </div>
                    </div>
                  </a-tooltip>
                </a-select-option>
              </a-select>
            </a-form-model-item>
            <a-form-model-item>
              <p class="label"><span>*</span>Main-Class</p>
              <a-input v-model="dataStreamConfig.mainClass"
                       placeholder="请输入jar包的Main-Class，示例：com.chitu.Application" />
            </a-form-model-item>
          </template>
          <a-form-model-item class="save-folder_item">
            <p class="label">存储位置</p>
            <save-folder ref="saveFolder"
                         class="save-folder-tree"
                         :expanded-keys="expandedKeys"
                         :default-selected-keys="defaultSelectedKeys"
                         @select="select" />
          </a-form-model-item>

        </div>
        <div class="footer justify-end">
          <a-button @click="cancelEvent"
                    size="small">取消</a-button>
          <a-button style="margin-left:8px"
                    @click="confirmEvent"
                    size="small"
                    type="primary">保存</a-button>
        </div>
      </a-form-model>
    </div>
    <drawer ref="drawer"
            :drawerVisible="drawerVisible"
            @closeDrawer="drawerVisible = false" />
  </a-modal>
</template>

<script>
  import saveFolder from './save-folder'
  import drawer from '@/components/priority-drawer.vue'
  import _ from 'lodash'

  export default {
    name: "AddFolder",
    data () {
      return {
        drawerVisible: false,
        isShow: false,
        title: '新建作业',
        form: { fileName: '', priority: '' },
        fileTypeList: [{ value: 'SQL' }, { value: 'DS' }],
        expandedKeys: [],
        defaultSelectedKeys: [],
        selectedKeys: ['0'],
        fileType: '',
        isFetchingSource: false,
        isFetchingJarVersion: false,
        sourceData: [],
        jarVersionData: [],
        source: '',
        jarVersion: '',
        dataStreamConfig: {
          jarId: '', //jar包id
          jarName: '', //jar包名称
          jarVersion: '', //jar包版本
          mainClass: '' //主函数名称
        },
        priorityPrefix: '',
        priorityList: [],
        defaultPriority: ''
      }
    },
    props: {

    },
    computed: {

    },
    components: {
      saveFolder, drawer
    },
    watch: {
      defaultSelectedKeys: {
        handler (val) {
          if (val && val.length) {
            this.selectedKeys = val
          }

        },
        deep: true
      }
    },
    created () {

    },
    methods: {
      open (data) {
        Object.assign(this.$data, this.$options.data())
        this.defaultPriority = ''
        this.form.priority = ''
        this.expandedKeys = data.expandedKeys
        this.defaultSelectedKeys = data.defaultSelectedKeys
        this.fileType = this.fileTypeList[0].value
        this.isShow = true
        this.$nextTick(() => {
          this.$refs.saveFolder.getTree()
        })
        this.getSource()
        this.initPriority()
      },
      async initPriority () {
        //等级初始化
        const params = {
          id: Number(this.$route.query.projectId)
        }
        const res = await this.$http.post('/project/projectManagement/detail', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res && res.data) {
          this.form.priority = res.data.priority
        }
        const globalPriority = this.$common.getPriority()
        this.priorityPrefix = globalPriority.labelPrefix
        this.priorityList = globalPriority.list
        const defaultItem = this.priorityList.find(item => {
          if (this.isNew) {//新增
            return item.isDefault
          } else {//编缉
            if (this.form.priority !== '') {
              return item.value.toString() === this.form.priority.toString()
            } else {
              return item.isDefault
            }

          }
        })
        if (defaultItem && defaultItem.value) {
          let defaultPriority = { key: JSON.stringify(defaultItem), label: defaultItem.label }
          this.defaultPriority = defaultPriority
          this.form.priority = defaultItem.value
        }
        //等级初始化end
      },
      close () {
        this.isShow = false
      },
      confirmEvent: _.debounce(function () {
        console.log('debounce')
        this.addSubmit()
      }, 500),
      cancelEvent () {
        this.isShow = false
      },
      async addSubmit () {
        if (!this.form.fileName.trim()) {
          this.$message.warning('作业名称不能为空')
          return
        }
        if (this.form.priority == '') {
          this.$message.warning('作业等级不能为空')
          return
        }
        const params = {
          fileName: this.form.fileName,
          fileType: this.fileType,
          content: '',
          folderId: this.selectedKeys[0],
          projectId: Number(this.$route.query.projectId),
          fileStatus: '未验证',
          jobConfig: {},
          sourceConfig: {},
          priority: this.form.priority
        }
        if (this.fileType === 'DS') {
          if (!this.dataStreamConfig.jarId) {
            this.$message.warning('请选择jar包资源')
            return
          }
          if (!this.dataStreamConfig.jarVersion) {
            this.$message.warning('请选择jar包版本')
            return
          }
          if (!this.dataStreamConfig.mainClass) {
            this.$message.warning('Main-Class 不能为空')
            return
          }
          params.dataStreamConfig = this.dataStreamConfig
        }
        const res = await this.$http.post('/file/addFile', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.$bus.$emit('addFileSuccess', this.selectedKeys[0])
          this.isShow = false
        } else {
          this.$message.error(res.msg)
        }
      },
      handleTypeChange (value) {
        console.log('value', value)
      },
      select (selectedKeys) {
        this.selectedKeys = selectedKeys
      },
      handleSourceSearch (value) {
        this.getSource(value)
      },
      handleChangeSource (value) {
        if (value) {
          const jar = JSON.parse(value.key)
          this.dataStreamConfig.jarName = jar.name
          this.dataStreamConfig.jarId = jar.id
          // this.dataStreamConfig.jarVersion = jar.version//默认选择主版本
          this.getJarVersion(this.dataStreamConfig.jarName)
        } else {
          this.dataStreamConfig.jarVersion = ''
          this.dataStreamConfig.jarName = ''
          this.dataStreamConfig.jarId = ''
        }

      },


      handleJarVersionSearch (value) {
        this.getJarVersion(value)
      },
      handleChangeJarVersion (value) {
        if (value) {
          const selectVersion = JSON.parse(value.key)
          this.dataStreamConfig.jarVersion = selectVersion.version
          this.dataStreamConfig.jarId = selectVersion.id
        } else {
          this.dataStreamConfig.jarVersion = ''
        }

      },

      async getSource (value) {
        const params = {
          name: value || '',
          projectId: Number(this.$route.query.projectId),
          version: 'v1'
        }
        this.isFetchingSource = true
        let res = await this.$http.post('/jar/searchJar', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isFetchingSource = false
        if (res.code === 0) {
          this.sourceData = res.data
        }
      },
      async getJarVersion (jarName) {
        const params = {
          orderByClauses: [{
            field: "updation_date",
            orderByMode: 1
          }],
          page: 1,
          pageSize: 2000,
          vo: {
            name: jarName,
            projectId: Number(this.$route.query.projectId)
          }
        }
        this.jarVersionData = []
        this.isFetchingJarVersion = true
        let res = await this.$http.post('/jar/queryJar', params)
        this.isFetchingJarVersion = false
        if (res.code === 0) {
          if (res.data) {
            if (res.data.rows) {
              this.jarVersionData = res.data.rows
              this.jarVersion = { key: JSON.stringify(this.jarVersionData[0]), label: this.jarVersionData[0].version }
              this.dataStreamConfig.jarVersion = this.jarVersionData[0].version
              this.dataStreamConfig.jarId = this.jarVersionData[0].id
              // if (this.jarVersionData && this.jarVersionData.length) {
              //   const findItem = this.jarVersionData.filter(item => item.version === this.dataStreamConfig.jarVersion)
              //   if (findItem && findItem.length) {
              //     this.jarVersion = { key: JSON.stringify(findItem[0]), label: findItem[0].version }
              //   }

              // }

            } else {
              this.jarVersionData = []
            }
          }
        } else {
          this.$message.error(res.msg);
        }
      },
      sourceMouseenter (item) {
        this.sourceDescription = item.description
      },
      jarVersionMouseenter (item) {
        this.jarVersionDescription = item.description
      },
      handerLevelChange (data) {
        const key = JSON.parse(data.key)
        this.form.priority = key.value
      },
      openDrawer () {
        this.drawerVisible = true
      }
    },
    mounted () {

    }
  }
</script>
<style lang="scss">
  .add-file-dialog {
    .ant-modal {
      width: 600px !important;
      .ant-modal-close-x {
        width: 44px;
        height: 44px;
        line-height: 44px;
      }
      .ant-modal-header {
        //弹窗头部
        height: 44px;
        padding: 0;
        .ant-modal-title {
          line-height: 44px;
          padding-left: 16px;
          font-size: 16px;
          font-weight: 600;
        }
      }
    }
  }
  .select-source-option {
    // .ant-select-dropdown-content .ant-select-dropdown-menu {
    //   max-height: 220px !important;
    // }
    .name {
      color: #0066ff;
    }
    .description {
      color: #999;
    }
  }
</style>
<style lang="scss" scoped>
  /deep/ .ant-modal-body {
    padding: 0;
    .add-file {
      .form-body {
        padding: 0 16px;
        .ant-form-item {
          margin-bottom: 0;
          margin-top: 12px;
          font-size: 12px;

          .ant-form-item-control {
            line-height: normal;
            .ant-form-item-children {
              display: flex;
              align-items: center;
            }

            .select-source {
              // flex-shrink: 0;
              .description {
                display: none;
              }
            }
          }
          input {
            font-size: 12px;
          }
          .label {
            line-height: normal;
            margin-bottom: 4px;
            width: 97px;
            font-size: 12px;
            text-align: right;
            margin-right: 8px;
            span {
              color: red;
            }
            .question {
              margin-left: 3px;
              cursor: pointer;
            }
          }
          .red {
            color: red;
          }
        }
      }
      .save-folder_item {
        .ant-form-item-children {
          display: flex;
          align-items: baseline !important;
        }
        .save-folder-tree {
          width: 100%;
        }
      }
      .footer {
        height: 44px;
        padding-right: 16px;
        border-top: solid 1px #d9d9d9;
      }
    }
  }
</style>