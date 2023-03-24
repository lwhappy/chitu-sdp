<template>
  <div class="basic-information"
       v-loading="isLoading">
    <a-collapse class="collapse-container"
                :default-active-key="[1,2]"
                :bordered="false">
      <template #expandIcon="props">
        <a-icon type="caret-right"
                :rotate="props.isActive ? 90 : 0" />
      </template>
      <a-collapse-panel key="1"
                        header="基础信息"
                        :style="customStyle">
        <div class="inner"
             :class="{'is-max':isMaxWidth}">
          <div class="item">
            <span>作业名称</span>

            <a-tooltip placement="topLeft">
              <template slot="title">
                <span>{{infoDetail.fileName}}</span>
              </template>
              <span class="job-name">{{infoDetail.fileName}}</span>
            </a-tooltip>
            <span class="status"
                  :class="infoDetail.jobStatus=='INITIALIZE'?'INITIALIZE':infoDetail.jobStatus=='RUNNING'?'RUNNING':infoDetail.jobStatus=='PAUSED'?'PAUSED':infoDetail.jobStatus=='SFAILED'?'SFAILED':infoDetail.jobStatus=='RFAILED'?'RFAILED':infoDetail.jobStatus=='FINISHED'?'FINISHED':infoDetail.jobStatus=='TERMINATED'?'TERMINATED':''">{{filterStatus(infoDetail.jobStatus)}}</span>
          </div>
          <div class="item">
            <span>作业类型</span>
            <span>{{infoDetail.fileType}}</span>
          </div>
          <div class="item">
            <span>所属项目</span>
            <span>{{infoDetail.projectName}}</span>
          </div>
          <div class="item">
            <span>存储位置</span>
            <span>{{infoDetail.filePath}}</span>
          </div>
          <div class="item">
            <span>创建人</span>
            <span>{{infoDetail.createdBy}}</span>
          </div>
          <div class="item">
            <span>创建时间</span>
            <span>{{infoDetail.creationDate}}</span>
          </div>
          <div class="item">
            <span>更新人</span>
            <span>{{infoDetail.updatedBy}}</span>
          </div>
          <div class="item">
            <span>更新时间</span>
            <span>{{infoDetail.updationDate}}</span>
          </div>
        </div>
      </a-collapse-panel>
      <a-collapse-panel key="2"
                        header="作业说明"
                        :style="customStyle">
        <div class="item">
          <span>业务价值</span>
          <span v-if="type==='approve'">{{infoDetail.businessValue}}</span>
          <a-textarea v-model="form.businessValue"
                      placeholder="请输入业务价值描述让更多人看见，示例：实时更新客户拜访数据，使得工作指导报表可以实时查看任务完成情况"
                      :auto-size="{ minRows:3, maxRows: 8 }"
                      v-if="type==='job'" />
        </div>
        <div class="item">
          <span>技术说明</span>
          <span v-if="type==='approve'">{{infoDetail.techSpecifications}}</span>
          <a-textarea v-if="type==='job'"
                      v-model="form.techSpecifications"
                      placeholder="请分享作业应用的关键技术，示例：依赖于update_visit_status_merge任务写到kafka的结果数据，实时消费dsp_v_o_customer_book_car_visit_his主题的数据"
                      :auto-size="{ minRows:3, maxRows: 8 }" />
        </div>
      </a-collapse-panel>
    </a-collapse>

    <div class="footer justify-end"
         v-if="type==='job'">
      <a-popconfirm placement="topRight"
                    @confirm="() => closeForm()">
        <template slot="title">
          <p>确定要退出详情吗？</p>
        </template>
        <a-button style="margin-right:8px"
                  size="small">取消</a-button>
      </a-popconfirm>
      <a-button type="primary"
                size="small"
                @click="submitForm">保存</a-button>

    </div>
  </div>
</template>

<script>

  export default {
    name: 'BigdataSdpFrontendBasicInformation',
    props: {
      detail: {
        type: Object,
        default: () => {
          return {}
        }
      },
      isShow: {
        type: Boolean,
        default: false
      },
      type: {
        type: String,
      },
      isMaxWidth: {
        type: Boolean,
        default: true
      }
    },
    watch: {
      isShow: {
        handler (val) {
          if (val) {
            let { id } = this.detail
            this.getDetail(id) // 获取详情信息
          }

        }
      }
    },
    created () {
    },
    data () {
      return {
        customStyle: 'border-radius: 4px;margin-bottom: 0;border: 0;overflow: hidden; background: #ffffff;',
        isLoading: false,
        form: {
          businessValue: "",
          techSpecifications: ""
        },
        infoDetail: [{
          "createdBy": "刘城02",
          "updatedBy": "刘城02",
          "enabledFlag": 1,
          "fileName": "xinzengJAR",
          "fileType": "DS",
          "folderId": 70,
          "projectName": "测试薛",
          "filePath": "/作业开发/测试一级目录/测试二级/我们"
        }]
      };
    },
    mounted () {

    },

    methods: {
      filterStatus (status) { // 当前状态过滤映射
        let text = null
        if (status === 'INITIALIZE') {
          text = '初始状态'
        } else if (status === 'RUNNING') {
          text = '运行中'
        } else if (status === 'PAUSED') {
          text = '暂停状态'
        } else if (status === 'SFAILED') {
          text = '启动失败'
        } else if (status === 'RFAILED') {
          text = '恢复失败'
        } else if (status === 'FINISHED') {
          text = '成功'
        } else if (status === 'TERMINATED') {
          text = '停止状态'
        }
        return text
      },
      statusClass (status) { // 
        return ['RUNNING', 'FINISHED'].includes(status)
      },
      async submitForm () {
        let { businessValue, techSpecifications } = this.form
        let params = {
          id: this.infoDetail.id || '',
          fileId: this.detail.id || '',
          projectId: Number(this.$route.query.projectId),
          businessValue,
          techSpecifications
        }
        let res = await this.$http.post('/file/updateBaseInfo', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.$message.success({ content: '修改成功', duration: 2 })
          this.$emit('change', { flag: -1 })
        }
      },
      async getDetail (id) {
        this.isLoading = true
        let res = await this.$http.post('/file/queryBaseInfo', { id }, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          if (res.data) {
            this.infoDetail = res.data
            this.form.techSpecifications = res.data.techSpecifications || ''
            this.form.businessValue = res.data.businessValue || ''
          }

        }
      },
      closeForm () {
        this.$emit('change', { flag: -1 })
      },
    },
  };
</script>

<style lang="scss" scoped>
  .common-title {
    padding: 10px 0;
    i {
      color: #006fff;
      margin-right: 8px;
    }
    .name {
      font-size: 14px;
      font-weight: 900;
    }
  }
  .basic-information {
    font-size: 12px;
    height: calc(100% - 32px);
    overflow-x: hidden;
    overflow-y: auto;
    padding: 10px 16px;
    background: #ffffff;
    .collapse-container {
      /deep/ .ant-collapse-header {
        font-size: 14px !important;
        font-weight: 900;
        i {
          color: #006fff !important;
        }
      }
      /deep/ .ant-collapse-item {
        font-size: 12px;
      }
      .inner {
        width: 100%;
        display: flex;
        flex-wrap: wrap;

        div {
          width: 50%;
        }
        &.is-max {
          display: flex;
          flex-wrap: wrap;
          div {
            width: 33%;
          }
        }
      }
      .item {
        margin-bottom: 10px;
        display: flex;
        span {
          margin-left: 10px;
          &:nth-of-type(1) {
            flex-basis: 80px;
          }
        }
        .job-name {
          width: 41%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      .status {
        margin-left: 30px;
        padding: 0px 10px;
        text-align: center;
      }
      .INITIALIZE {
        background: #efe3ff;
        border-radius: 2px;
        color: #2c2f37;
        display: flex;
        justify-content: center;
        align-items: center;
      }
      .TERMINATED {
        background-color: #ddd;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #93a1bb;
        border-radius: 2px;
      }
      .RUNNING {
        background-color: #dfecff;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #0066ff;
        border-radius: 2px;
      }
      .PAUSED {
        background-color: #eef0f4;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #2c2f37;
        border-radius: 2px;
      }
      .SFAILED {
        background: #ffe0e0;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #f95353;
        border-radius: 2px;
      }
      .RFAILED {
        background-color: #fff4e7;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #ff9118;
        border-radius: 2px;
      }
      .FINISHED {
        background-color: #e2f6de;
        display: flex;
        justify-content: center;
        align-items: center;
        color: #33cc22;
        border-radius: 2px;
      }
      .success {
        background-color: #47cb89;
      }
      .warning {
        background-color: #ffad33;
      }
      .error {
        background-color: #f16543;
      }
      .info-title {
        border-left: 2px solid #006eff;
        padding-left: 8px;
      }
    }
  }
</style>