<!--
 * @description: 
 * @Author: lijianguo19
 * @Date: 2022-09-15 16:39:11
 * @FilePath: \src\views\application\job-develop\components\topic-auth-apply-dialog.vue
-->
<template>
  <div>
    <a-modal class="subscribe-dialog"
             v-model="modalVisible"
             v-if="modalVisible"
             :mask-closable="false"
             :title="title"
             width="600px"
             @cancel="handleCancel"
             v-drag>
      <p class="tip">以下Topic开启了鉴权，当前项目目前还没有访问权限，如需访问请申请订阅</p>
      <a-form-model layout="vertical"
                    ref="ruleForm"
                    :model="form"
                    :rules="rules">
        <a-form-model-item>
          <chitu-table :columns="columns"
                     :dataSource="dataSource"
                     :autoHight="false"
                     :scroll="{ y: '300px' }"
                     rowKey="clusterToken"></chitu-table>
        </a-form-model-item>
        <div class="applyer justify-start">
          <p class="label">申请人</p>
          <p>{{form.applicantName}}</p>
        </div>
        <a-form-model-item label="订阅原因"
                           prop="reason">
          <div class="description-content">
            <a-textarea class="description"
                        v-model="form.reason"
                        :max-length="1000"
                        placeholder="请输入订阅原因"
                        :rows="2" />
            <span class="max-length">{{form.reason.length}}/1000</span>
          </div>
        </a-form-model-item>
      </a-form-model>
      <template slot="footer">
        <a-button size="small"
                  @click="handleCancel">
          取消
        </a-button>

        <a-button type="primary"
                  size="small"
                  :loading="isLoading"
                  @click="onSubmit">
          提交申请
        </a-button>
      </template>

    </a-modal>
    <ApplySubscribeDialog ref="applySubscribeDialogRef" />
  </div>
</template>
<script>
  import ApplySubscribeDialog from './apply-subscribe-dialog.vue'
  export default {
    name: '',
    components: { ApplySubscribeDialog },
    props: {},
    data () {
      return {
        modalVisible: false,
        title: '权限申请',
        confirmVisible: false,
        isLoading: false,
        applicationList: [],
        memberData: [],
        isFetchingMember: false,
        columns: [
          {
            dataIndex: 'topic',
            title: 'Topic',
            width: 150
          },
          {
            dataIndex: 'clusterName',
            title: '所属集群',
            width: 150
          },
        ],
        dataSource: [],
        form: {
          applicant: [],//申请人id
          reason: ''
        },
        rules: {
          reason: [
            { required: true, message: '请输入订阅原因', trigger: 'blur' },
            { min: 1, max: 1000, message: '订阅原因长度为1-1000个字符', trigger: 'blur' },
          ],
        }
      };
    },
    computed: {},
    watch: {},
    created () { },
    mounted () { },
    methods: {
      open (data) {
        this.dataSource = data
        this.form.applicant = []
        this.modalVisible = true
        const { employeeNumber, userName: username } = this.$store.getters.userInfo
        this.form.applicantName = username
        this.form.applicant.push({
          employeeNumber,
          username
        })
      },
      handleCancel () {
        this.resetForm()
        this.modalVisible = false
      },
      resetForm () {
        this.form = {
          applicant: [],//申请人id
          reason: ''
        }
        this.$refs.ruleForm.resetFields()
      },
      onSubmit () {
        this.$refs.ruleForm.validate(valid => {
          if (valid) {
            this.onSave()
          } else {
            return false;
          }
        });
      },
      async onSave () {
        const { reason, applicant } = this.form
        const topicPairs = this.dataSource.map(item => {
          return {
            clusterToken: item.clusterToken,
            topic: item.topic
          }
        })
        const appKey = this.dataSource[0].appKey
        const params = {
          topicPairs,
          appKey,
          applicant,
          reason,
        }
        this.isLoading = true
        let res = await this.$http.post('/file/topicSubscribe', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          this.$message.success('申请成功')
          if (res.data.passOa) {
            const data = res.data.oas
            this.$refs.applySubscribeDialogRef.open(data)
          }
          this.resetForm()
          this.modalVisible = false
        } else {
          this.$message.error(res.msg)
        }
      },
    },
  }
</script>
<style lang='scss' scoped>
  .subscribe-dialog {
    .tip {
      padding-bottom: 12px;
      color: #2b2f37;
    }
    /deep/ .ant-modal {
      top: 80px;
    }
    /deep/ .ant-modal-body {
      max-height: calc(100vh - 200px);
      overflow: auto;
    }
    /deep/ .ant-form label {
      font-size: 12px !important;
    }
    /deep/ .ant-form-item {
      margin-bottom: 8px !important;
      padding-bottom: 0 !important;
    }
    .form-title {
      font-size: 14px;
      color: #333;
      font-weight: 600;
      margin-bottom: 12px;
      .line {
        display: block;
        width: 4px;
        height: 14px;
        background: #006eff;
        margin-right: 8px;
      }
    }

    .applyer {
      font-size: 12px;
      margin: 12px 0;
      .label {
        margin-right: 8px;
        color: #333;
      }
    }
    .description-content {
      position: relative;
      border: 1px solid #d9d9d9;
      width: 100%;
      border-radius: 6px;
      .description {
        height: 80px;
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
    .ant-btn-success {
      background-color: #33cc22;
      border-color: #33cc22;
      color: #fff;
    }
    .ant-btn-success[disabled] {
      background-color: #c1efbc;
      border-color: #c1efbc;
    }
    .ant-btn-primary[disabled] {
      background-color: #b2d3ff;
      border-color: #b2d3ff;
      color: #fff;
    }
  }
</style>