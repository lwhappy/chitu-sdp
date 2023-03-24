<template>
  <!-- 一定要用v-if="isShow"摧毁dom重新渲染，不然会有莫名其妙的问题 -->
  <a-modal v-if="isShow"
           wrapClassName="add-jar-dialog"
           v-model="isShow"
           :mask-closable="false"
           :footer="null"
           :title="title">
    <!-- <span slot="closeIcon">x</span> -->
    <div class="add-jar">
      <a-form-model>
        <div class="form-body">
          <div class="justify-start item">
            <p class="label">上传jar包</p>
            <div style="width:400px">
              <a-upload-dragger :file-list="fileList"
                                :remove="handleRemove"
                                :disabled="progress > 0"
                                :before-upload="beforeUpload"
                                accept=".jar">
                <p class="ant-upload-drag-icon">
                  <img src="@/assets/icons/icon_upload.png"
                       alt="">
                </p>
                <p class="ant-upload-text">
                  将文件拖拽到此处，或<a-button type="link">点击上传jar包</a-button>
                </p>
                <p class="ant-upload-hint">
                  文件最大{{fileLimit}}MB，支持 .jar格式
                </p>
              </a-upload-dragger>
              <!-- <a-upload :file-list="fileList"
                        :remove="handleRemove"
                        :before-upload="beforeUpload"
                        accept=".jar">
                <a-button>
                  <a-icon type="upload" />选择文件
                </a-button>
              </a-upload> -->

              <!-- <div class="upload-status">
                <p v-if="isUploading">
                  <a-spin>
                    <a-icon slot="indicator"
                            type="loading"
                            style="font-size: 24px"
                            spin />
                  </a-spin>
                </p>
                <p v-else>{{uploadStatus}}</p>
              </div> -->
            </div>
          </div>
          <div class="justify-start item">
            <p class="label">jar包名称</p>
            <a-input :disabled="type==='add'?false:true"
                     v-model="form.name"
                     placeholder="请输入jar包名称" />
          </div>
          <div class="justify-start item">
            <p class="label">git_url</p>
            <a-input v-model="form.git"
                     placeholder="请输入git_url" />
          </div>
          <div class="item justify-start">
            <p class="label">版本描述<template v-if="descriptionVersion">{{descriptionVersion}}</template></p>
            <div class="description-content">
              <a-textarea class="description"
                          :max-length="250"
                          v-model="form.description"
                          placeholder="请输入版本描述" />
              <span class="max-length">{{form.description.length}}/250</span>
            </div>
          </div>
        </div>
        <div class="pocess-load"
             v-show="isLoading">
          <div class="title">进度</div>
          <div v-if="isShowProcess"
               class="progress">
            <a-progress :percent="progress"
                        :show-info="true"
                        status="active" />
          </div>
        </div>
        <div class="footer justify-end">
          <a-button @click="cancelEvent"
                    size="small">取消</a-button>
          <a-button style="margin-left:8px"
                    @click="confirmEvent"
                    :loading="loading"
                    size="small"
                    type="primary">
            <template v-if="isLoading || isLoading === ''">保存</template>
            <template v-else-if="isLoading === false">重试</template>
          </a-button>
          <!-- <a-button class="button-restyle button-confirm"
                    :disabled="isLoading === true?true:false"
                    @click="clickSubmit">
            <template v-if="isLoading || isLoading === ''">确认</template>
            <template v-else-if="isLoading === false">重试</template>
          </a-button>
          <a-button class="button-restyle button-cancel"
                    @click="cancelEvent">取消</a-button>  -->
        </div>
      </a-form-model>

    </div>
  </a-modal>
</template>

<script>
  import _ from 'lodash'

  export default {
    components: {},
    data () {
      return {
        fileLimit: 500,//文件最大500M
        fileList: [],
        isShow: false,
        isUploading: false,
        uploadStatus: '',
        headers: {},
        isLoading: '',
        form: {
          name: '',
          git: '',
          description: '',
          version: ''
        },
        type: '',
        id: '',
        descriptionVersion: '',
        isShowProcess: true,
        progress: 0
      }
    },
    props: {

    },
    computed: {
      loading () {
        return this.isLoading == true
      }
      // descriptionLength () {
      //   return this.form.description.length
      // }
    },
    watch: {
      isShow: {
        handler () {

        }
      }
    },
    created () {
      const userId = sessionStorage.getItem('userId')
      const token = sessionStorage.getItem('token')
      this.headers.projectId = Number(this.$route.query.projectId)
      this.headers['X-uid'] = userId
      this.headers['token'] = token
    },
    methods: {
      handleRemove (file) {
        const index = this.fileList.indexOf(file);
        const newFileList = this.fileList.slice();
        newFileList.splice(index, 1);
        this.fileList = newFileList;
      },
      beforeUpload (file) {
        if (this.type === 'add') {
          this.form.name = file.name
        }
        this.fileList = [file]
        return false
      },
      open (data) {
        if (data) {
          this.isShowProcess = true
          this.isLoading = ''
          this.progress = 0
          this.fileList = []
          this.descriptionVersion = ''
          // this.headers = {}
          this.isUploading = false
          this.uploadStatus = ''
          this.title = data.title
          this.type = data.type || ''
          if (this.type === 'add') {//点击新增jar
            this.form.name = ''
            this.form.git = 'http://xxxxxx'
            this.form.description = ''
            this.form.version = ''
          } else {
            if (data.data) {
              this.form.name = data.data.name
              this.form.git = data.data.git
              this.form.description = data.data.description
              this.form.version = data.data.version
              this.id = Number(data.data.id) || ''
              if (this.type === 'editMain') {//点击上传新版本
                this.descriptionVersion = data.data.newVersion
              } else if (this.type === 'history') {//点击更新
                this.descriptionVersion = data.data.version
              }

            }
          }
        }
        this.isShow = true
      },
      close () {
        this.isShow = false
      },
      clickSubmit:
        _.debounce(function () {
          console.log('debounce')
          this.confirmEvent()
        }, 500),
      async confirmEvent () {
        this.form.name = this.form.name.trim()
        this.form.git = this.form.git.trim()
        this.form.description = this.form.description.trim()
        if ((this.type === 'add' || this.type === 'editMain') && this.fileList.length === 0) {
          this.$message.warning('请上传jar包')
          return
        }
        if (this.form.name === '') {
          this.$message.warning('jar包名称不能为空')
          return
        }
        //限制文件大小
        const isLimit = this.fileList[0].size / 1024 / 1024 <= this.fileLimit
        if (!isLimit) {
          this.$message.warning(`jar包最大不能超过${this.fileLimit}MB`)
          return
        }
        if (this.form.description === '') {
          this.$message.warning('版本描述不能为空')
          return
        }
        const url = '/jar/addJar'
        const params = JSON.parse(JSON.stringify(this.form))
        params.projectId = Number(this.$route.query.projectId)
        params.name = encodeURIComponent(params.name)
        params.git = encodeURIComponent(params.git)
        params.description = encodeURIComponent(params.description)
        const formData = new FormData()
        formData.append('file', this.fileList[0])
        if (this.type === 'add') {//新增
          params.type = 'ADD'
          params.version = 'ADD'
        }
        else if (this.type === 'history') {//更新历史版本
          params.type = 'EDIT'
          params.id = this.id
        } else if (this.type === 'editMain') {//更新主版本
          params.type = 'UPDATE'
          params.version = 'UPDATE'
        }
        this.isShowProcess = true
        this.isLoading = true
        this.progress = 0
        let res = await this.$http.post(url, formData, {
          headers: params,
          onUploadProgress: progressEvent => {
            let progress = (progressEvent.loaded / progressEvent.total * 100 | 0)
            progress = parseInt(progress)
            if (progress === 100) {
              progress = 99.9
            }
            this.progress = progress
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          this.progress = 100
          if (this.type === 'editMain' || this.type === 'history') {
            this.$message.success('更新成功')
          } else if (this.type === 'add') {//新增
            this.$message.success('添加成功')
          }
          this.isShow = false
          this.$emit('addSuccess')
        } else {
          this.progress = 0
          this.$message.error(res.msg)
        }


      },
      cancelEvent () {
        this.isShow = false
      },
      handleChange (info) {
        this.isUploading = true
        const file = info.file
        if (file && file.response) {
          if (file.response.code === 0) {
            this.isUploading = false
            this.uploadStatus = '上传成功'
            this.$message.success('上传成功')
            const url = file.response.data
            const lastIndex = file.name.lastIndexOf('/')
            const name = file.name.substr(lastIndex + 1)
            const versionLastIndex = url.lastIndexOf('/')
            const descriptionVersion = url.substr(versionLastIndex + 1)
            this.descriptionVersion = descriptionVersion
            if (this.type === 'add') {
              this.form.name = name
            }
            this.form.url = url
          } else {
            this.isUploading = false
            this.uploadStatus = '上传失败'
            this.$message.error(file.response.msg)
          }
        }
        // if (info.file.status !== 'uploading') {
        //   // console.log(info.file, info.fileList);
        // }
        // if (info.file.status === 'done') {
        //   this.$message.success(`${info.file.name} file uploaded successfully`);
        // } else if (info.file.status === 'error') {
        //   this.$message.error(`${info.file.name} file upload failed.`);
        // }
      },
    },
    mounted () {

    }
  }
</script>
<style lang="scss">
  .add-jar-dialog {
    .ant-modal {
      width: 600px !important;
    }
  }
</style>
<style lang="scss" scoped>
  /deep/ .ant-modal-body {
    padding: 0;
    .add-jar {
      position: relative;
      .form-body {
        padding: 12px 16px 0;
        .ant-upload {
          font-size: 12px;
          .ant-upload-text {
            font-weight: 600;
          }
          .ant-upload-text,
          .ant-upload-hint {
            font-size: 12px;
          }
          button {
            font-size: 12px;
          }
        }
        .ant-upload-list {
          .ant-upload-list-item-card-actions {
            right: -20px;
          }
          .ant-upload-list-item-info {
            display: flex;
            align-items: center;
            .anticon-paper-clip {
              width: 20px;
              height: 20px;
              margin-top: -5px;
              background: url(~@/assets/icons/icon_jar.png) no-repeat;
              background-size: 100%;
              svg {
                display: none !important;
              }
            }
            .ant-upload-list-item-name {
              font-size: 12px;
              color: #2c2f37;
            }
          }
        }
        .upload-status {
          margin-left: 10px;
        }
        .item {
          margin-bottom: 0;
          margin-top: 12px;
          font-size: 12px;
          // .max-length {
          //   text-align: right;
          // }
          &:first-of-type {
            margin-top: 0;
          }
          input {
            font-size: 12px;
          }
          .label {
            line-height: normal;
            width: 90px;
            padding-right: 10px;
            text-align: right;
            flex-shrink: 0;
          }
          .description-content {
            position: relative;
            border: 1px solid #d9d9d9;
            width: 100%;
            border-radius: 6px;
            .description {
              height: 120px;
              resize: none;
              overflow-y: auto;
              border: none;
              outline: none;
              box-shadow: none;
              margin-bottom: 20px;
            }
            .max-length {
              position: absolute;
              bottom: 0;
              right: 12px;
              color: #999;
            }
          }

          .red {
            color: red;
          }
        }
      }
      .pocess-load {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 16px;
        height: 32px;
        border-bottom: 1px solid #d9d9d9;
        .progress {
          width: calc(100% - 32px);
        }
      }
      .footer {
        height: 44px;
        padding-right: 16px;
        margin-top: 5px;
      }

      // .progress {
      //   position: absolute;
      //   bottom: 35px;
      //   width: calc(100% - 32px);
      //   left: 16px;
      //   z-index: 10;
      //   display: flex;
      //   justify-content: space-between;
      //   .title {
      //     width: auto;
      //   }
      //   .ant-progress-circle .ant-progress-text {
      //     color: #0066FF;
      //   }
      // }
    }
  }
</style>