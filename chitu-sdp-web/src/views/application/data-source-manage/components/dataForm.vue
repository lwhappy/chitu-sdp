<!--
 * @Author: hjg
 * @Date: 2021-12-10 15:01:02
 * @LastEditTime: 2022-09-30 10:09:19
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\application\data-source-manage\components\dataForm.vue
-->
<template>
  <a-modal class="data-source-form"
           ref="dataSourceForm"
           v-model="visible"
           :mask-closable="false"
           :title="title"
           :width="800"
           @cancel="closeDataFormtModal(false)"
           :footer="null">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <!-- 表单 -->
    <div class="form-content">
      <div class="form-item">
        <div class="form-item-label">
          实例名称 <span>*</span>
          <a-tooltip placement="topLeft">
            <template slot="title">
              <p>为了便于数据源环境间映射，建议规范实例名称，不同环境有映射关系的数据源实例名称使用同一个</p>
            </template>
            <img class="ask"
                 width="14"
                 height="14"
                 style="margin:0 0 2px 4px"
                 src="@/assets/icons/ask.png"
                 alt="">
          </a-tooltip>
        </div>
        <div class="form-item-value">
          <a-input v-model="formData.dataSourceName"
                   :disabled="type === 'detail'"
                   allow-clear
                   placeholder="支持英文、下划线，建议以 dsn_ 作前缀。示例：dsn_crm_a"></a-input>
        </div>
      </div>
      <div class="form-item">
        <div class="form-item-label">
          类型 <span>*</span>
        </div>
        <div class="form-item-value">
          <a-select placeholder="选择类型"
                    style="width: 100%;"
                    v-model="paramsType"
                    :disabled="type === 'edit' || type === 'detail'">
            <a-select-option :key="index"
                             :value="item.value"
                             v-for="(item, index) in typeArr">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </div>
      </div>

      <div class="form-item"
           v-if="paramsType === 'kafka'">
        <div class="form-item-label">
          认证方式 <span>*</span>
        </div>
        <div class="form-item-value">
          <a-select placeholder="选择类型"
                    style="width: 100%;"
                    v-model="formData.certifyType"
                    :disabled="type === 'edit' || type === 'detail'">
            <a-select-option :key="index"
                             :value="item.value"
                             v-for="(item, index) in certifyTypeArr">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </div>
      </div>
      <template v-if="paramsType === 'hbase'">
        <div class="form-item">
          <div class="form-item-label">
            zookeeper地址 <span>*</span>
          </div>
          <div class="form-item-value">
            <a-input v-model="formData.dataSourceUrl"
                     :disabled="type === 'detail'"
                     allow-clear
                     :placeholder="dataSourceUrlPlaceholder(paramsType)"></a-input>
          </div>
        </div>
        <div class="form-item">
          <div class="form-item-label">
            HBase根目录 <span>*</span>
          </div>
          <div class="form-item-value">
            <a-input v-model="formData.hbaseZnode"
                     :disabled="type === 'detail'"
                     allow-clear
                     placeholder="必填，示例：'/hbase-sdp'"></a-input>
          </div>
        </div>
        <div class="form-item">
          <div class="form-item-label">
            namespace <span>*</span>
          </div>
          <div class="form-item-value">
            <a-input v-model="formData.databaseName"
                     :disabled="type === 'edit' || type === 'detail'"
                     allow-clear>
            </a-input>
          </div>
        </div>
      </template>
      <template v-else-if="paramsType === 'hive'">
        <div class="form-item">
          <div class="form-item-label">
            hive集群 <span>*</span>
          </div>
          <div class="form-item-value">
            <a-select class="select"
                      style="width: 100%;"
                      :disabled="type === 'detail'"
                      v-model="formData.hiveCluster"
                      @change="handlerChangeCluster">
              <a-select-option v-for="(item,index) in clusterList"
                               :key="index"
                               :value="item.hiveCluster">
                {{item.hiveCluster}}
              </a-select-option>
            </a-select>

          </div>
        </div>
        <div class="form-item">
          <div class="form-item-label">
            hive配置目录
          </div>
          <div class="form-item-value">
            <a-input v-model="formData.dataSourceUrl"
                     disabled></a-input>
          </div>
        </div>
        <div class="form-item">
          <div class="form-item-label">
            hadoop配置目录
          </div>
          <div class="form-item-value">
            <a-input v-model="formData.hadoopConfDir"
                     disabled
                     placeholder="">
            </a-input>
          </div>
        </div>

      </template>
      <template v-else-if="paramsType === 'hudi'">
        <div class="form-item">
          <div class="form-item-label">
            hudi集群 <span>*</span>
          </div>
          <div class="form-item-value">
            <a-select class="select"
                      style="width: 100%;"
                      :disabled="type === 'detail'"
                      v-model="formData.hiveCluster"
                      @change="handlerChangeHudiCluster">
              <a-select-option v-for="(item,index) in hudiClusterList"
                               :key="index"
                               :value="item.hiveCluster">
                {{item.hiveCluster}}
              </a-select-option>
            </a-select>

          </div>
        </div>
        <div class="form-item">
          <div class="form-item-label">
            hive配置目录
          </div>
          <div class="form-item-value">
            <a-input v-model="formData.dataSourceUrl"
                     disabled></a-input>
          </div>
        </div>
        <div class="form-item">
          <div class="form-item-label">
            hadoop配置目录
          </div>
          <div class="form-item-value">
            <a-input v-model="formData.hadoopConfDir"
                     disabled
                     placeholder="">
            </a-input>
          </div>
        </div>

      </template>
      <template v-else-if="paramsType === 'print' || paramsType === 'datagen'"></template>
      <div v-else
           class="form-item">
        <div class="form-item-label">
          数据源地址 <span>*</span>
        </div>
        <div class="form-item-value">
          <a-input-group compact
                         v-if="paramsType === 'mysql' || paramsType === 'doris' || paramsType === 'tidb'">
            <a-input style="width: 30%"
                     v-model="urlPrefix"
                     :disabled="type === 'detail'"
                     @change="urlPrefixChange" />
            <a-input v-model="formData.dataSourceUrl"
                     style="width: 70%"
                     :disabled="type === 'detail'"
                     allow-clear
                     :placeholder="dataSourceUrlPlaceholder(paramsType)"></a-input>
          </a-input-group>
          <a-input v-model="formData.dataSourceUrl"
                   v-else
                   :disabled="type === 'detail'"
                   allow-clear
                   :placeholder="dataSourceUrlPlaceholder(paramsType)"></a-input>
        </div>
      </div>
      <div class="form-item"
           v-if="isShowDB && paramsType === 'kafka'">
        <div class="form-item-label">
          DataHub集群名称<span>*</span>
        </div>
        <div class="form-item-value">
          <a-input v-model="formData.clusterName"
                   disabled
                   allow-clear></a-input>
        </div>
      </div>
      <div class="form-item"
           v-if="isShowDB && paramsType === 'kafka'">
        <div class="form-item-label">
          是否启用DataHub<span>*</span>
        </div>
        <div class="form-item-value">
          <a-select v-model="formData.enabledDatahub"
                    disabled
                    style="width: 100%;">
            <a-select-option :value="item.value"
                             :key="index"
                             v-for="(item, index) in enabledDatahubList">
              {{item.label}}
            </a-select-option>
          </a-select>
        </div>
      </div>
      <div class="form-item"
           v-if="formData.streamLoadUrl_show">
        <div class="form-item-label">
          stream load地址<span>*</span>
        </div>
        <div class="form-item-value">
          <a-input v-model="formData.streamLoadUrl"
                   :disabled="type === 'detail' || paramsType !== 'doris'"
                   allow-clear
                   placeholder="示例：10.83.192.4:3306;10.83.192.5:3306;10.83.192.6:3306"></a-input>
        </div>
      </div>
      <div class="form-item"
           v-if="formData.databaseName_show">
        <div class="form-item-label">
          数据库<span>*</span>
        </div>
        <div class="form-item-value">
          <a-input v-model="formData.databaseName"
                   :disabled="type === 'edit' || type === 'detail' || paramsType === 'kafka' || paramsType === 'elasticsearch' || paramsType === 'elasticsearch7'"
                   allow-clear
                   placeholder="支持英文、下划线，示例：crm_activity"></a-input>
        </div>
      </div>
      <template v-if="!((paramsType === 'kafka' && formData.certifyType === 'default') || paramsType === 'hbase' || paramsType === 'kudu' || paramsType === 'hive' || paramsType === 'hudi'  || paramsType === 'print' || paramsType === 'datagen')">
        <div class="form-item">
          <div class="form-item-label">
            用户名<span v-show="paramsType !== 'elasticsearch'">*</span>
          </div>
          <div class="form-item-value">
            <a-input v-model="formData.userName"
                     :disabled="type === 'detail'"
                     allow-clear
                     placeholder="请输入用户名">
            </a-input>
          </div>
        </div>

        <div class="form-item">
          <div class="form-item-label">
            登录密码<span v-show="paramsType !== 'elasticsearch' && paramsType !== 'elasticsearch7'">*</span>
          </div>
          <div class="
                form-item-value">
            <a-input v-if="type === 'detail'"
                     v-model="formData.password"
                     :disabled="type === 'detail'"
                     placeholder="请输入"></a-input>
            <!-- type="password有些浏览器会将保存的密码自动填充进来，需要使用autocomplete="new-password"禁止这个功能" -->
            <a-input v-else
                     v-model="formData.password"
                     type="password"
                     autocomplete="new-password"
                     allow-clear
                     placeholder="请输入"></a-input>
          </div>
        </div>
      </template>
      <div class="form-item">
        <div class="form-item-label">
          责任人 <span>*</span>
        </div>
        <div class="form-item-value">
          <a-select v-model="formData.owner"
                    :disabled="type === 'detail'"
                    placeholder="选择责任人"
                    style="width: 100%;">
            <a-select-option :value="item.id"
                             :key="'employee-' + index"
                             v-for="(item, index) in projectUsers">
              {{ item.employeeNumber }}, {{ item.userName }}
            </a-select-option>
          </a-select>
        </div>
      </div>

      <!-- 描述 -->
      <div class="form-text">
        <div class="form-text-label">
          描述 <span>*</span>
        </div>
        <div class="form-text-value count-message">
          <a-textarea placeholder="必填，请描述数据源表的基础信息和简介，不可输入用户登录密码敏感信息"
                      :max-length="1000"
                      v-model="formData.remark"
                      :disabled="type === 'detail'"
                      allow-clear></a-textarea>
          <span class="count-info">{{ countLength }} / 1000</span>
        </div>
      </div>
    </div>
    <!-- 操作 -->
    <div class="footer justify-end">
      <a-button style="padding:0 20px;"
                type="small"
                @click="closeDataFormtModal(false)">取消</a-button>
      <template v-if="paramsType !== 'print' && paramsType !== 'datagen'">
        <a-button v-if="type !== 'detail' && submitFlag === -1"
                  @click="checkConnect"
                  type
                  class="button-restyle button-confirm">{{ submitFlag | verifyText }}</a-button>
        <a-button v-if="type !== 'detail' && submitFlag === 0"
                  class="button-restyle verify-ing">{{ submitFlag |
            verifyText
        }}</a-button>
        <a-button v-if="type !== 'detail' && submitFlag === 1"
                  @click="checkConnect"
                  class="button-restyle verify-success">
          <a-icon type="check" />{{ submitFlag | verifyText }}
        </a-button>
      </template>
      <a-button v-if="type !== 'detail'"
                :disabled="submitFlag !== 1"
                @click="clickSubmit"
                class="button-restyle button-confirm">确认</a-button>

    </div>
  </a-modal>
</template>
<script>
  import _ from 'lodash'

  export default {
    props: {
      type: {
        type: String,
        default: 'add'
      },

      formVisible: {
        type: Boolean,
        default: false
      },
      title: {
        type: String,
        default: ''
      },
      dataForm: {
        type: Object,
        default: () => {
          return {}
        }
      },
      typeArr: {
        type: Array,
        default: () => {
          return []
        }
      },
      projectUsers: {
        type: Array,
        default: () => {
          return []
        }
      }
    },
    data () {
      return {
        clusterList: [],
        hudiClusterList: [],
        // hiveDir: '',
        // hadoopConfDir: '',
        visible: false,
        submitFlag: -1,
        urlPrefix: 'jdbc:mysql://',
        formData: {
          id: null,
          dataSourceName: '',
          dataSourceType: null,
          remark: '',
          owner: '',
          dataSourceUrl: '',
          streamLoadUrl: '',
          streamLoadUrl_show: false,
          databaseName: '',
          databaseName_show: false,
          userName: '',
          password: '',
          certifyType: 'default',
          hbaseZnode: '',
          hadoopConfDir: '',
          hiveCluster: '',
          clusterName: '',
          enabledDatahub: 0,
          authKafkaClusterAddr: "",
          clusterToken: "",
          enabledFlag: 0,
          supportDatahub: 0
        },
        isShowDB: false,
        paramsType: 'kafka',
        certifyTypeArr: [{
          value: 'default',
          label: '无'
        }, {
          value: 'sasl',
          label: 'SASL'
        }],
        enabledDatahubList: [
          {
            value: 1,
            label: '是'
          },
          {
            value: 0,
            label: '否'
          }]
      }
    },
    filters: {
      verifyText (value) {
        // // console.log('value: ', value, value === 0, value === 1)
        let text = '点击验证'
        if (value === 0) {
          text = '验证中...'
        } else if (value === 1) {
          text = '验证成功'
        }
        return text
      }
    },
    computed: {
      countLength () {
        return this.formData.remark.toString().length
      },
      newFormData () {
        return JSON.stringify(this.formData)
      }
    },
    created () {
      this.init()
    },
    watch: {
      newFormData: {
        handler (value, oldValue) {
          let newValue = JSON.parse(value)
          let originValue = JSON.parse(oldValue)
          // // console.log(333, newValue, originValue)
          if (originValue.dataSourceUrl === '') return
          if (newValue.databaseName !== originValue.databaseName || newValue.dataSourceUrl !== originValue.dataSourceUrl || newValue.userName !== originValue.userName || newValue.password !== originValue.password) {
            this.submitFlag = -1
          }
        },
        deep: true
      },
      paramsType: {
        handler (value) {
          this.formData.clusterName = ''
          this.formData.enabledDatahub = 0
          this.formData.authKafkaClusterAddr = ''
          this.formData.clusterToken = ''
          this.formData.enabledFlag = 0
          this.formData.supportDatahub = 0
          // console.log('paramsType: ', value)
          this.formData.certifyType = 'default'
          if (this.type === 'add') {
            this.submitFlag = -1
          }
          if (value === 'kafka') {
            this.formData.databaseName_show = false
            this.formData.streamLoadUrl_show = false
          } else if (value === 'mysql') {
            this.formData.databaseName_show = false
            this.formData.streamLoadUrl_show = false
          } else if (value === 'tidb') {
            this.formData.databaseName_show = false
            this.formData.streamLoadUrl_show = false

          } else if (value === 'elasticsearch' || value === 'elasticsearch7') {
            this.formData.databaseName_show = false
            this.formData.streamLoadUrl_show = false
          } else if (value === 'doris') {
            this.formData.databaseName_show = false
            this.formData.streamLoadUrl_show = true
          } else if (value === 'hive') {
            this.formData.databaseName_show = false
            this.formData.streamLoadUrl_show = false
            this.setHiveConf()
            // if (this.type === 'add') {
            //   this.formData.dataSourceUrl = this.hiveDir
            //   this.formData.hadoopConfDir = this.hadoopConfDir
            // }
          } else if (value === 'hudi') {
            this.formData.databaseName_show = false
            this.formData.streamLoadUrl_show = false
            this.setHudiConf()
          } else if (value === 'hbase') {
            if (this.type === 'add') {
              this.formData.databaseName = 'default'
            }
          } else if (value === 'kudu') {
            this.formData.databaseName_show = true
            this.formData.streamLoadUrl_show = false
            if (this.type === 'add') {
              this.formData.databaseName = 'default'
            }
          } else if (value === 'print') {
            this.submitFlag = 1
          } else if (value === 'datagen') {
            this.submitFlag = 1
          }

        }
      }
    },
    methods: {
      urlPrefixChange () {
        this.urlPrefix = 'jdbc:mysql://'
      },
      dataSourceUrlPlaceholder (type) {
        let text = 'kafka1.com:9092,kafka2.com:9092,kafka3.com:9092'
        if (type === 'mysql' || type === 'tidb') {
          text = 'ip:port/database/#/?a=a&b=b'
        } else if (type === 'doris') {
          text = 'ip:port/database/#/?a=a&b=b,ip:port/database/#/?a=a&b=b'
        } else if (type === 'elasticsearch' || type === 'elasticsearch7' || type === 'hive' || type === 'kudu') {
          text = 'ip:port,ip:port,ip:port'
        } else if (type === 'hbase') {
          text = '必填，格式ip:port,ip:port,ip:port示例：10.83.192.6:2181,10.83.192.7:2181,10.83.192.8:2181'
        }
        return text
      },
      dataItemShow (value) {
        if (value === 'kafka') {
          this.formData.databaseName_show = false
          this.formData.streamLoadUrl_show = false
        } else if (value === 'mysql') {
          this.formData.databaseName_show = false
          this.formData.streamLoadUrl_show = false
        } else if (value === 'tidb') {
          this.formData.databaseName_show = false
          this.formData.streamLoadUrl_show = false
        } else if (value === 'elasticsearch' || value === 'elasticsearch7') {
          this.formData.databaseName_show = false
          this.formData.streamLoadUrl_show = false
        } else if (value === 'doris') {
          this.formData.databaseName_show = false
          this.formData.streamLoadUrl_show = true
        }
        else if (value === 'hive') {
          this.formData.databaseName_show = false
          this.formData.streamLoadUrl_show = false
        } else if (value === 'hbase') {
          if (this.type === 'add') {
            this.formData.databaseName = 'default'
          }
        } else if (value === 'kudu') {
          this.formData.databaseName_show = true
          this.formData.streamLoadUrl_show = false
          if (this.type === 'add') {
            this.formData.databaseName = 'default'
          }
        }
      },
      // 初始化
      async init () {
        // console.log('dataForm: ', this.dataForm)
        // if (this.$store.getters.env === 'prod') {
        //   this.hiveDir = '/opt/apache/hive-0125/conf'
        //   this.hadoopConfDir = '/opt/apache/hadoop-3.1.1-new/etc/hadoop/'
        // } else {
        //   this.hiveDir = '/opt/apache/hive/conf'
        //   this.hadoopConfDir = ''
        // }
        this.visible = this.formVisible
        this.formData = JSON.parse(JSON.stringify(this.dataForm))
        // console.log('formData: ', this.formData)
        if (this.type === 'edit') {
          this.submitFlag = 1
          if (this.formData.clusterName) {
            this.isShowDB = true
          }
          this.paramsType = this.formData.dataSourceType
        } else if (this.type === 'detail') {
          if (this.formData.clusterName) {
            this.isShowDB = true
          }
          this.paramsType = this.formData.dataSourceType
        }
        this.dataItemShow(this.paramsType)
        const clusterList = await this.getHiveClusterList()
        this.clusterList = clusterList || []
        const hudiClusterList = await this.getHudiClusterList()
        this.hudiClusterList = hudiClusterList || []
        if (this.paramsType === 'hive') {
          this.setHiveConf()
        }
        if (this.paramsType === 'hudi') {
          this.setHudiConf()
        }
      },
      handlerChangeCluster (value) {
        const findItem = this.clusterList.find(item => item.hiveCluster === value)
        this.formData.dataSourceUrl = findItem.dataSourceUrl
        this.formData.hadoopConfDir = findItem.hadoopConfDir
      },
      handlerChangeHudiCluster (value) {
        const findItem = this.hudiClusterList.find(item => item.hiveCluster === value)
        this.formData.dataSourceUrl = findItem.dataSourceUrl
        this.formData.hadoopConfDir = findItem.hadoopConfDir
      },
      setHiveConf () {
        if (this.clusterList && this.clusterList.length) {
          if (this.type === 'add') {
            this.formData.dataSourceUrl = this.clusterList[0].dataSourceUrl
            this.formData.hadoopConfDir = this.clusterList[0].hadoopConfDir
            this.formData.hiveCluster = this.clusterList[0].hiveCluster
          } else if (this.type === 'edit') {
            if (!this.formData.hiveCluster) {
              this.formData.dataSourceUrl = this.clusterList[0].dataSourceUrl
              this.formData.hadoopConfDir = this.clusterList[0].hadoopConfDir
              this.formData.hiveCluster = this.clusterList[0].hiveCluster
            }

          }

        }
      },
      setHudiConf () {
        if (this.hudiClusterList && this.hudiClusterList.length) {
          if (this.type === 'add') {
            this.formData.dataSourceUrl = this.hudiClusterList[0].dataSourceUrl
            this.formData.hadoopConfDir = this.hudiClusterList[0].hadoopConfDir
            this.formData.hiveCluster = this.hudiClusterList[0].hiveCluster
          } else if (this.type === 'edit') {
            if (!this.formData.hiveCluster) {
              this.formData.dataSourceUrl = this.hudiClusterList[0].dataSourceUrl
              this.formData.hadoopConfDir = this.hudiClusterList[0].hadoopConfDir
              this.formData.hiveCluster = this.hudiClusterList[0].hiveCluster
            }

          }

        }
      },
      clickSubmit:
        _.debounce(function () {
          console.log('debounce')
          this.submitForm()
        }, 500),
      // 提交表单
      submitForm () {
        let formData = JSON.parse(this.newFormData)
        //判断是否有集群地址和启用DataHub
        if (!this.isShowDB) {
          delete formData.clusterName
          delete formData.enabledDatahub
          delete formData.authKafkaClusterAddr
          delete formData.clusterToken
          delete formData.enabledFlag
          delete formData.supportDatahub
        }
        formData.dataSourceType = this.paramsType
        if (formData.dataSourceType === 'hudi') {
          const findItem = this.hudiClusterList.find(item => item.hiveCluster === formData.hiveCluster)
          formData.hudiCatalogPath = findItem.hudiCatalogPath
        }
        for (let key in formData) {
          // console.log('key: ', formData[key])
          if (typeof formData[key] === 'string') // 去掉前后空格
            formData[key] = formData[key].trim()
        }
        let res = this.formDataVertify(formData)
        if (res.code === 0) {
          if (this.submitFlag === 1) {
            formData = this.removeDataOfNull(formData)
            if (this.type === 'add') {
              this.addDataSource(formData)
            } else {
              formData.id = this.dataForm.id
              this.editDataSource(formData)
            }
          } else {
            this.$message.warning({ content: '请先进行连通性验证', duration: 2 })
          }
        } else {
          this.$message.warning({ content: res.msg, duration: 2 })
        }
      },
      removeDataOfNull (data) {
        Object.keys(data).forEach(key => {
          if (data[key] === null || data[key].length === 0 || typeof data[key] === 'boolean') {
            delete data[key]
          }
        })
        // console.log('data: ', data)
        return data
      },
      async getHiveClusterList () {
        let res = await this.$http.post('/dataSource/hiveClusterList', {}, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          return res.data
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      async getHudiClusterList () {
        const params = {
          dataSourceType: 'hudi'
        }
        let res = await this.$http.post('/dataSource/hiveClusterList', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          return res.data
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 添加数据源
      async addDataSource (params) {
        let res = await this.$http.post('/dataSource/add', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.closeDataFormtModal(true)
          this.$message.success({ content: '添加成功', duration: 2 })
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 修改数据源
      async editDataSource (params) {
        params = this.removeDataOfNull(params)
        let res = await this.$http.post('/dataSource/update', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.closeDataFormtModal(true)
          this.$message.success({ content: '修改成功', duration: 2 })
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 连通性验证
      checkConnect () {
        let formData = JSON.parse(this.newFormData)
        formData.dataSourceType = this.paramsType
        if (formData.dataSourceType === 'hudi') {
          const findItem = this.hudiClusterList.find(item => item.hiveCluster === formData.hiveCluster)
          formData.hudiCatalogPath = findItem.hudiCatalogPath
        }
        for (let key in formData) {
          // console.log('key: ', formData[key])
          if (typeof formData[key] === 'string') // 去掉前后空格
            formData[key] = formData[key].trim()
        }
        let res = this.formDataVertify(formData)
        if (res.code === 0) {
          this.submitFlag = 0
          this.checkConnectHttp(formData)
        } else {
          this.$message.warning({ content: res.msg, duration: 2 })
        }
      },
      // 连通性验证请求
      async checkConnectHttp (params) {
        this.isShowDB = false
        params = this.removeDataOfNull(params)
        let res = await this.$http.post('/dataSource/checkConnect', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.submitFlag = 1
          if (res.data) {
            this.isShowDB = true
            this.formData.clusterName = res.data.clusterName
            this.formData.enabledDatahub = res.data.enabledDatahub
            this.formData.authKafkaClusterAddr = res.data.authKafkaClusterAddr
            this.formData.clusterToken = res.data.clusterToken
            this.formData.enabledFlag = res.data.enabledFlag
            this.formData.supportDatahub = res.data.supportDatahub
          } else {
            this.isShowDB = false
          }
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
          this.submitFlag = -1
          this.isShowDB = false
        }
      },
      // 字段校验
      formDataVertify (data) {
        let res = {
          msg: '',
          code: 0
        }
        let reg = /^[0-9a-zA-Z]([0-9a-zA-Z_]{0,50})$/
        // 实例名称
        if (!reg.test(data.dataSourceName)) {
          res.msg = '实例名称不合法，1-50个字符，支持英文和下划线，不支持以下划线开头'
          if (!data.dataSourceName) {
            res.msg = '实例名称不能为空'
          }
          res.code = -1
          return res
        }
        // 数据源类型
        if (data.dataSourceType === undefined) {
          res.msg = '未选择数据源类型'
          res.code = -1
          return res
        }
        if (data.dataSourceType === 'hbase') {
          // zookeeper地址
          reg = /^[a-zA-Z0-9]([-_a-zA-Z0-9.,;：:；，?&=\\/:]{0,1000})$/
          if (!reg.test(data.dataSourceUrl)) {
            res.msg = 'zookeeper地址不合法，1-1000个字符，以英文开头，支持英文、数字、双斜杆//、单斜杆/、英文点.、下划线_、冒号:'
            if (!data.dataSourceUrl) {
              res.msg = 'zookeeper地址不能为空'
            }
            res.code = -1
            return res
          }
          if (!this.formData.hbaseZnode) {
            res.msg = 'HBase根目录不能为空'
            res.code = -1
            return res
          }
          if (!this.formData.databaseName) {
            res.msg = 'namespace不能为空'
            res.code = -1
            return res
          }

        } if (data.dataSourceType === 'hive' || data.dataSourceType === 'hudi') {

          if (!this.formData.dataSourceUrl) {
            res.msg = 'hive配置目录不能为空'
            res.code = -1
            return res
          }

        } else if (data.dataSourceType !== 'print' && data.dataSourceType !== 'datagen') {
          // 数据源地址
          reg = /^[a-zA-Z0-9]([-_a-zA-Z0-9.,;：:；，?&=\\/:]{0,1000})$/
          if (!reg.test(data.dataSourceUrl)) {
            res.msg = '数据源地址不合法，1-1000个字符，以英文开头，支持英文、数字、双斜杆//、单斜杆/、英文点.、下划线_、冒号:'
            if (!data.dataSourceUrl) {
              res.msg = '数据源地址不能为空'
            }
            res.code = -1
            return res
          }
        }

        // stream load地址
        if (data.dataSourceType === 'doris') {
          reg = /^[a-zA-Z0-9]([-_a-zA-Z0-9.;:,，?&=\\/]{0,500})$/
          if (!reg.test(data.streamLoadUrl) || data.streamLoadUrl === undefined) {
            res.msg = 'stream load地址不合法，1-500个字符，以英文开头，支持英文、分号、数字、双斜杆//、单斜杆/、英文点.、下划线_、冒号:'
            if (!data.streamLoadUrl) {
              res.msg = 'stream load地址不能为空'
            }
            res.code = -1
            return res
          }
        }
        if (!(data.dataSourceType === 'doris' || data.dataSourceType === 'mysql' || data.dataSourceType === 'tidb' || data.dataSourceType === 'kafka' || data.dataSourceType === 'elasticsearch' || data.dataSourceType === 'elasticsearch7' || data.dataSourceType === 'hive' || data.dataSourceType === 'hudi' || data.dataSourceType === 'print' || data.dataSourceType === 'datagen')) {
          // 数据库
          reg = /^[0-9a-zA-Z_]{1,50}$/
          if (!reg.test(data.databaseName)) {
            res.msg = '数据库不合法，1-50个字符，支持英文、数字、下划线'
            if (!data.databaseName) {
              res.msg = '数据库不能为空'
            }
            res.code = -1
            return res
          }
        }
        if (!((data.dataSourceType === 'kafka' && data.certifyType === 'default') || data.dataSourceType === 'hbase' || data.dataSourceType === 'kudu' || data.dataSourceType === 'hive' || data.dataSourceType === 'hudi' || data.dataSourceType === 'print' || data.dataSourceType === 'datagen')) {
          if (!data.userName) {
            res.msg = '用户名不能为空'
            res.code = -1
            return res
          }
          if (!data.password) {
            res.msg = '密码不能为空'
            res.code = -1
            return res
          }




          // // 用户名
          // reg = /^[0-9a-zA-Z_-]{1,50}$/
          // if (!reg.test(data.userName)) {
          //   res.msg = '用户名不合法，1-50个字符，支持英文、数字、下划线'
          //   if (data.userName === null || data.userName.length === 0) {
          //     res.msg = '用户名不能为空'
          //   }
          //   res.code = -1
          //   return res
          // }
          // // 密码
          // reg = /^[0-9a-zA-Z_~!@#$%^&*-]{1,50}$/
          // if (!reg.test(data.password)) {
          //   res.msg = '密码不合法，1-50个字符，支持英文、数字、特殊字符'
          //   if (data.password === null || data.password.length === 0) {
          //     res.msg = '密码不能为空'
          //   }
          //   res.code = -1
          //   return res
          // }
        }
        if (!data.remark) {
          res.msg = '数据源描述不能为空'
          res.code = -1
          return res
        }
        // 描述
        // reg = /^[0-9a-zA-Z_ .。（）()【】[\]|{}~!@#$%^&*，：—、/(\r|\n|\r\n)\u4e00-\u9fa5]{1,1000}$/
        // if (!reg.test(data.remark)) {
        //   res.msg = '数据源描述不合法，1-1000个字符，支持英文、数字、特殊字符'
        //   if (data.remark === null || data.remark.length === 0) {
        //     res.msg = '数据源描述不能为空'
        //   }
        //   res.code = -1
        //   return res
        // }
        return res
      },
      // 关闭modal
      closeDataFormtModal (val) {
        this.$emit('closeDataFormtModal', val)
      }
    }
  }
</script>
<style lang="scss" scoped>
  .data-source-form {
    width: 100%;
    font-size: 12px;
    color: #333;

    /deep/ .ant-select {
      font-size: 12px;
    }

    /deep/ .ant-modal-body {
      padding: 12px 0 0 0;
    }

    .form-content {
      min-height: 400px;
      max-height: 550px;
      padding: 10px 16px;

      .form-item {
        // width: 368px;
        width: calc(50% - 8px);
        display: inline-table;
        margin-right: 8px;
        margin-bottom: 12px;

        .form-item-label {
          color: #333;
          margin-bottom: 8px;

          span {
            color: red;
          }
        }

        .form-item-value {
          height: 28px;

          /deep/ .ant-input {
            height: 100%;
          }

          input {
            height: 100%;
          }

          /deep/ .ant-select-selection--single {
            height: 28px;
          }

          /deep/ .ant-select-selection__rendered {
            line-height: 25px;
          }
        }
      }

      .form-text {
        width: 100%;

        .form-text-label {
          color: #333;
          margin-bottom: 8px;

          span {
            color: red;
          }
        }

        .count-message {
          position: relative;
          /deep/ .ant-input-affix-wrapper {
            border: 1px solid #d9d9d9;
          }
          /deep/ textarea {
            height: 200px;
            resize: none;
            overflow-y: auto;
            border: none;
            outline: none;
            box-shadow: none;
            margin-bottom: 20px;
          }

          .count-info {
            position: absolute;
            bottom: 0;
            right: 12px;
            color: #999;
          }
        }
      }

      // }
    }

    .footer {
      border-top: 1px solid #ddd;
      height: 44px;
      line-height: 44px;
      padding-right: 16px;

      .verify-ing {
        background-color: #4c9aff;
        color: #fff;
        border: none;
        padding: 0;
      }

      .verify-success {
        background-color: #52c41a;
        color: #fff;
        border: none;
        padding: 0;
      }

      i {
        margin-right: 2px;
      }

      /deep/ .ant-btn > .anticon + span {
        margin-left: 0;
      }
    }
  }
</style>
