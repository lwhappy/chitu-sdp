<!--
 * @Author: hjg
 * @Date: 2021-10-19 11:08:45
 * @LastEditTime: 2022-06-24 10:42:57
 * @LastEditors: Please set LastEditors
 * @Description: 新建引擎
 * @FilePath: \src\views\system-setting\components\addEngineDialog.vue
-->
<template>
  <a-modal class="add-engine-dialog"
           v-model="isShowDialog"
           title="新增引擎"
           width="600px"
           :footer="null">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <div class="form-info"
         v-loading="isLoading">
      <a-form>
        <a-form-item class="name-url">
          <p>引擎名称<span>*</span></p>
          <a-input v-model="form.engineName"
                   class="name"
                   placeholder="请输入名称"></a-input>
        </a-form-item>
        <!-- 集群部署类型 -->
        <a-form-item class="form-item">
          <p>集群部署类型<span>*</span></p>
          <a-select v-model="form.engineType"
                    placeholder="请选择集群部署类型"
                    @change="handleClusterQueue">
            <a-select-option v-for="(item, index) in engineTypeList"
                             :value="item.value"
                             :key="index">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <!--UAT环境集群  -->
        <a-form-item class="form-item">
          <p>UAT环境集群<span>*</span></p>
          <a-select v-model="form.uatEngineCluster"
                    placeholder="请选择集群"
                    @change=" value => handleEngineCluster(value, 'uat')">
            <a-select-option v-for="(item, index) in uatEngineClusterList"
                             :value="item.clusterCode"
                             :key="index">{{ item.clusterName }}</a-select-option>
          </a-select>
        </a-form-item>
        <!-- UAT环境队列 -->
        <a-form-item class="form-item">
          <p>{{form.engineType=='yarn'?'UAT环境队列':'UAT环境namespace'}} <span>*</span></p>
          <a-select v-model="form.uatEngineQueue"
                    :placeholder="form.engineType=='yarn'?'请选择队列':'请选择namespace'">
            <a-select-option v-for="(item, index) in uatEngineQueueList"
                             :value="item.queueName"
                             :key="index">{{ item.queueName }}</a-select-option>
          </a-select>
        </a-form-item>
        <!-- 生产环境集群 -->
        <a-form-item class="form-item">
          <p>生产环境集群<span>*</span></p>
          <a-select v-model="form.engineCluster"
                    placeholder="请选择集群"
                    @change=" value => handleEngineCluster(value, 'prod')">
            <a-select-option v-for="(item, index) in engineClusterList"
                             :value="item.clusterCode"
                             :key="index">{{ item.clusterName }}</a-select-option>
          </a-select>
        </a-form-item>
        <!-- 生产环境队列 -->
        <a-form-item class="form-item">
          <p>{{form.engineType=='yarn'?'生产环境队列':'生产环境namespace'}} <span>*</span></p>
          <a-select v-model="form.engineQueue"
                    :placeholder="form.engineType=='yarn'?'请选择队列':'请选择namespace'">
            <a-select-option v-for="(item, index) in engineQueueList"
                             :value="item.queueName"
                             :key="index">{{ item.queueName }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>
    <div class="footer justify-end">
      <div class="confirm-footer justify-end">
        <a-button @click="cancelEvent"
                  size="small">取消</a-button>
        <a-button style="margin-left:8px"
                  @click="confirmEvent"
                  size="small"
                  type="primary">确定</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script>
  export default {
    data () {
      return {
        isLoading: false,
        isShowDialog: false,
        form: {
          engineType: 'yarn',
          engineName: null,
          uatEngineCluster: '',
          engineCluster: '',
          uatEngineQueue: '',
          engineQueue: '',
        },
        engineTypeList: [
          {
            label: "Yarn",
            value: "yarn",
          },
          {
            label: "Kubernetes",
            value: "kubernetes",
          }
        ],
        uatEngineClusterList: [],
        engineClusterList: [],
        uatEngineQueueList: [],
        engineQueueList: [],
      }
    },
    watch: {
      isShowDialog: {
        async handler (val) {
          if (val) {
            this.initForm()
            this.isLoading = true
            await this.getEngineClusterList('uat')
            await this.getEngineClusterList('prod')
            await this.getEngineQueueList('uat')
            await this.getEngineQueueList('prod')
            this.isLoading = false
          }
        }
      }
    },
    methods: {
      // 初始化表单
      initForm () {
        this.isLoading = false
        this.form = {
          engineName: null,
          engineType: 'yarn',
          uatEngineCluster: '',
          engineCluster: '',
          uatEngineQueue: '',
          engineQueue: '',
        }
        this.uatEngineQueueList = []
        this.engineQueueList = []
        this.uatEngineClusterList = []
        this.engineClusterList = []
      },
      // 获取集群列表
      async getEngineClusterList (env) {
        let res = await this.$http.get(`/setting/engineSetting/queryCluster?env=${env}&engineType=${this.form.engineType}`)
        if (res.code === 0) {
          if (env == 'uat') {
            this.uatEngineClusterList = res.data
            if (this.uatEngineClusterList && this.uatEngineClusterList.length) {
              this.form.uatEngineCluster = this.uatEngineClusterList[0].clusterCode
            } else {
              this.form.uatEngineCluster = ''
            }
          } else {
            this.engineClusterList = res.data
            if (this.engineClusterList && this.engineClusterList.length) {
              this.form.engineCluster = this.engineClusterList[0].clusterCode
            } else {
              this.form.engineCluster = ''
            }
          }
        } else {
          if (env == 'uat') {
            this.uatEngineClusterList = []
            this.form.uatEngineCluster = ''
          } else {
            this.engineClusterList = []
            this.form.engineCluster = ''
          }
        }
        return res
      },
      // 获取队列列表
      async getEngineQueueList (env) {
        const params = {
          clusterCode: env == 'uat' ? this.form.uatEngineCluster : this.form.engineCluster,
          env,
          engineType: this.form.engineType,
        }
        let res = await this.$http.post('/setting/engineSetting/engineQueues', params)
        if (res.code === 0) {
          if (env == 'uat') {
            this.uatEngineQueueList = res.data
            if (this.uatEngineQueueList && this.uatEngineQueueList.length) {
              this.form.uatEngineQueue = this.uatEngineQueueList[0].queueName
            } else {
              this.form.uatEngineQueue = ''
            }
          } else {
            this.engineQueueList = res.data
            if (this.engineQueueList && this.engineQueueList.length) {
              this.form.engineQueue = this.engineQueueList[0].queueName
            } else {
              this.form.engineQueue = ''
            }
          }
        } else {
          if (env == 'uat') {
            this.uatEngineQueueList = []
            this.form.uatEngineQueue = ''
          } else {
            this.engineQueueList = []
            this.form.engineQueue = ''
          }
        }
        return res
      },

      // 确认点击事件
      async confirmEvent () {
        if (this.form.engineName === null || this.form.engineName === '' || this.form.engineName.length === 0) {
          return this.$message.warning({ content: '引擎名称不能为空', duration: 2 })
        }
        if (this.form.engineType === null || this.form.engineType === '' || this.form.engineType.length === 0) {
          return this.$message.warning({ content: '未选择集群部署类型', duration: 2 })
        }
        if (this.form.uatEngineCluster === null || this.form.uatEngineCluster === '' || this.form.uatEngineCluster.length === 0) {
          return this.$message.warning({ content: '未选择UAT环境集群', duration: 2 })
        }
        if (this.form.engineCluster === null || this.form.engineCluster === '' || this.form.engineCluster.length === 0) {
          return this.$message.warning({ content: '未选择生产环境集群', duration: 2 })
        }
        if (this.form.uatEngineQueue === null || this.form.uatEngineQueue === '' || this.form.uatEngineQueue.length === 0) {
          return this.$message.warning({ content: this.form.engineType == 'yarn' ? '未选择UAT环境队列' : '未选择UAT环境namespace', duration: 2 })
        }
        if (this.form.engineQueue === null || this.form.engineQueue === '' || this.form.engineQueue.length === 0) {
          return this.$message.warning({ content: this.form.engineType == 'yarn' ? '未选择生产环境队列' : '未选择生产环境namespace', duration: 2 })
        }
        const params = {
          engineName: this.form.engineName,
          engineType: this.form.engineType,
          uatEngineCluster: this.form.uatEngineCluster,
          engineCluster: this.form.engineCluster,
        }
        if (this.form.engineType == 'yarn') {
          params.uatEngineQueue = this.form.uatEngineQueue
          params.engineQueue = this.form.engineQueue
        } else {
          params.uatNamespace = this.form.uatEngineQueue
          params.namespace = this.form.engineQueue
        }
        let res = await this.$http.post('/setting/engineSetting/add', params)
        if (res.code === 0) {
          this.$message.success({ content: '添加成功', duration: 2 })
          this.isShowDialog = false
          this.$emit('confirmEvent', true)
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 取消点击事件
      cancelEvent () {
        this.isShowDialog = false
        this.$emit('cancelEvent', false)
      },
      // 集群部署类型改变，重新获取集群和队列数据
      async handleClusterQueue (value) {
        if (value) {
          this.isLoading = true
          await this.getEngineClusterList('uat')
          await this.getEngineClusterList('prod')
          await this.getEngineQueueList('uat')
          await this.getEngineQueueList('prod')
          this.isLoading = false
        }
      },
      async handleEngineCluster (value, env) {
        if (value) {
          this.isLoading = true
          await this.getEngineQueueList(env)
          this.isLoading = false
        }
      }
    },
    created () {

    }
  }
</script>

<style lang="scss" scoped>
  .add-engine-dialog {
    /deep/ .ant-modal-body {
      padding: 0;
    }
    .form-info {
      height: 412px;
      padding: 12px 16px 0;
      overflow-y: auto;
      /deep/ .ant-form-item {
        margin-bottom: 12px;
        height: 48px;
      }
      p {
        height: 16px;
        font-size: 12px;
        line-height: 16px;
        color: #333;
        font-weight: 600;
        span {
          color: red;
        }
      }
      .form-item {
        /deep/ .ant-form-item-children .ant-input {
          height: 28px;
        }
        .user-select {
          /deep/ .ant-select-selection--multiple {
            height: auto;
            min-height: 28px;
          }
          /deep/ .ant-select-selection__rendered {
            height: 28px;
            ul > li {
              margin-top: 1px;
            }
          }
        }
      }
      .name-url {
        /deep/ .ant-form-item-children .ant-input {
          height: 28px;
        }
        .name {
          width: 100%;
        }
        .url {
          width: calc(100% - 184px);
          border-left: 0;
        }
      }
    }
    .footer {
      height: 44px;
      border-top: 1px solid #ddd;
      padding-right: 16px;
    }
  }
</style>
