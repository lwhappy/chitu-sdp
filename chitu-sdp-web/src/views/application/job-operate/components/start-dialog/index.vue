
<template>
  <div v-if="isPop"
       class="start-dialog">
    <confirm-dialog2 :visible="isPop"
                     :confirmDisabled="resourceData && resourceData.success === false"
                     title="启动作业"
                     :width="950"
                     @close="isPop=false"
                     @confirm="start()">
      <div class="start-main"
           v-loading="isLoading">
        <p class="tip">请选择&nbsp;{{record.jobName}}&nbsp;作业启动位置：</p>
        <a-radio-group class="radios"
                       v-model="selectValue"
                       :options="options" />
        <div v-show="selectValue === 'default'"
             class="default">
          <div class="default-tip">
            <i class="chitutree-h5 chitutreedengpaotishi"></i>温馨提示：直接启动是不带状态的，如果不是首次启动，建议从检查点/保存点启动
          </div>
          <div v-if="fileType!='DS'"
               class="checkbox-wrapper">
            <a-checkbox v-model="isEditConsumedPosition">修改消费位置后启动</a-checkbox>
          </div>
          <div v-if="isEditConsumedPosition"
               class="position-list">
            <template v-if="positionList.length">
              <div class="header justify-start">
                <p></p>
                <p>元表名称</p>
                <p>topic / topic-pattern</p>
                <p class="justify-start">
                  <span>消费位置</span>
                  <a-popover>
                    <template slot="content">
                      <p>最早位置(earliest-offset)：从topic分区最早偏移量位置开始消费</p>
                      <p>最新位置(latest-offset)：从topic分区最新偏移量位置开始消费</p>
                      <p>指定时间戳：从topic分区指定的时间戳位置开始消费</p>
                      <p>继续从上次位置消费(group-offsets)：从topic消费者组已提交偏移量位置开始消费</p>
                    </template>
                    <img style="width:14px;margin-left:5px"
                         src="@/assets/icons/ask.png"
                         alt="" />
                  </a-popover>

                </p>
              </div>
              <div class="body">
                <div v-for="(item,index) in positionList"
                     :key="item.orderNum"
                     class="justify-start">
                  <p>{{index + 1}}</p>
                  <p>
                    <a-tooltip placement="topLeft">
                      <template slot="title">
                        <span>{{item.flinkTableName}}</span>
                      </template>
                      <span>{{item.flinkTableName}}</span>
                    </a-tooltip>
                  </p>
                  <p>
                    <a-tooltip placement="topLeft">
                      <template slot="title">
                        <span>{{item.topic}}</span>
                      </template>
                      <span>{{item.topic}}</span>
                    </a-tooltip>
                  </p>
                  <p>
                    <a-select :style="{width:'240px'}"
                              v-model="item.startupMode"
                              @change="handleChangeStartupMode(item)">
                      <a-select-option v-for="consumedItem in consumedList"
                                       :value="consumedItem.value"
                                       :key="consumedItem.value">
                        {{ consumedItem.name }}
                      </a-select-option>
                    </a-select>

                    <a-date-picker v-if="item.startupMode === 'timestamp'"
                                   class="timestamp-input"
                                   type="text"
                                   :allowClear="false"
                                   v-model="item.dateStr"
                                   show-time
                                   @change="onDateChange(arguments,item)"
                                   @openChange="openDateChange(arguments,item)"
                                   @ok="onDateOk(item)" />
                    <span v-if="item.isShowWaitConsumedNum"
                          class="consumedNum">待消费数: {{item.waitConsumedNum}}</span>
                  </p>
                </div>
              </div>
            </template>
            <template v-else>
              <p class="no-data">暂无数据</p>
            </template>
          </div>
        </div>
        <!-- 检查点列表 -->
        <div v-show="selectValue === 'check'"
             v-defaultPage="checkList.length === 0"
             class="check-list">
          <div class="header justify-start">
            <p></p>
            <p>编号</p>
            <p>检查点路径</p>
            <p>触发时间</p>
          </div>
          <div class="body">
            <div v-for="(item,index) in checkList"
                 :key="item.orderNum"
                 class="justify-start"
                 @click="clickCheck(item)">
              <p>
                <a-radio v-model="item.checked"></a-radio>
              </p>
              <p>{{index + 1}}</p>
              <p>
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{item.filePath}}</span>
                  </template>
                  <span>{{item.filePath}}</span>
                </a-tooltip>
              </p>
              <p>{{item.triggerTime}}</p>
            </div>
          </div>
        </div>
        <!-- 保存点列表 -->
        <div v-show="selectValue === 'save'"
             class="save-wrapper">
          <div class="filter justify-start">
            <div class="project">
              <span>项目名称</span>
              <a-select class="select-project"
                        placeholder="请输入项目名称"
                        show-search
                        v-model="defaultProject"
                        label-in-value
                        :filter-option="false"
                        :not-found-content="isFetchingProject ? undefined : null"
                        @search="handleProjectSearch"
                        @change="handleChangeProject">
                <a-spin v-if="isFetchingProject"
                        slot="notFoundContent"
                        size="small" />
                <a-select-option v-for="(item,index) in projectData"
                                 :value="JSON.stringify(item)"
                                 :key="String(item.id) + index">
                  {{ item.projectName }}
                </a-select-option>
              </a-select>
            </div>
            <div class="job">
              <span>作业名称</span>
              <a-select class="select-job"
                        placeholder="请输入作业名称"
                        show-search
                        v-model="defaultJob"
                        label-in-value
                        :filter-option="false"
                        :not-found-content="isFetchingJob ? undefined : null"
                        @search="handleJobSearch"
                        @change="handleChangeJob">
                <a-spin v-if="isFetchingJob"
                        slot="notFoundContent"
                        size="small" />
                <a-select-option v-for="(item,index) in jobData"
                                 :value="JSON.stringify(item)"
                                 :key="String(item.id) + index">
                  {{ item.jobName }}
                </a-select-option>
              </a-select>
            </div>
          </div>
          <div class="save-list"
               v-defaultPage="saveList.length === 0">
            <div class="header justify-start">
              <p></p>
              <p>编号</p>
              <p>保存点名称</p>
              <p>保存点路径</p>
              <p>触发时间</p>
            </div>
            <div class="body">
              <div v-for="(item,index) in saveList"
                   :key="item.orderNum"
                   class="justify-start"
                   @click="clickSave(item)">
                <p>
                  <a-radio v-model="item.checked"></a-radio>
                </p>
                <p>{{index + 1}}</p>
                <p>
                  <a-tooltip placement="topLeft">
                    <template slot="title">
                      <span>{{item.savepointName}}</span>
                    </template>
                    <span>{{item.savepointName}}</span>
                  </a-tooltip>
                </p>
                <p>
                  <a-tooltip placement="topLeft">
                    <template slot="title">
                      <span>{{item.filePath}}</span>
                    </template>
                    <span>{{item.filePath}}</span>
                  </a-tooltip>
                </p>
                <p>{{item.triggerTime}}</p>
              </div>
            </div>
          </div>
        </div>
        <!-- 资源利用情况 -->
        <div v-if="resourceData"
             class="resource-wrapper">
          <div class="resource">
            <div class="justify-between">
              <div class="resource-item justify-start">
                <p class="label">当前作业预计所需资源：</p>
                <p>
                  <em>CPU<span :style="{color:computeCpuColor(resourceData)}">{{resourceData.task.vCores}}</span>&nbsp;VCore</em>
                  <em>内存<span :style="{color:computeMemoryColor(resourceData)}">{{resourceData.task.memory}}</span>&nbsp;GB</em>
                </p>
              </div>
              <div class="resource-item justify-start">
                <p class="label">当前队列参考剩余资源：</p>
                <p>
                  <em>CPU<span>{{resourceData.queue.vCores}}</span>&nbsp;VCore</em>
                  <em>内存<span>{{resourceData.queue.memory}}</span>&nbsp;GB</em>
                </p>
              </div>
            </div>
            <div class="justify-start">
              <div class="resource-item justify-start">
                <p class="label">总资源剩余可用：</p>
                <p>
                  <em>CPU<span>{{resourceData.cluster.vCores}}</span>&nbsp;VCore</em>
                  <em>内存<span>{{resourceData.cluster.memory}}</span>&nbsp;GB</em>
                </p>
              </div>

            </div>
          </div>
          <div v-if="resourceData.notice"
               class="img-tip"><img src="@/assets/icons/warn-yellow.png">{{resourceData.notice}}</div>
        </div>
      </div>
      <div v-if="['dev','uat'].includes($store.getters.env)"
           class="footer-left"
           slot="footer">
        UAT环境作业运行
        <a-select style="width: 80px;"
                  v-model="runDays">
          <a-select-option v-for="item in runDaysList"
                           :value="item.value"
                           :key="item.value">
            {{ item.name }}
          </a-select-option>
        </a-select>
        后自动下线
      </div>
      <div v-else
           slot="footer"></div>
    </confirm-dialog2>
  </div>
</template>
<script>
  import ConfirmDialog2 from '@/components/confirm-dialog/index2'
  import resourceColor from '../../mixins/resource-color.js'
  export default {
    name: 'startDialog',
    mixins: [resourceColor],
    components: {
      ConfirmDialog2
    },
    props: {
      runCallback: {
        type: Function,
        value: () => { }
      }
    },
    data () {
      return {
        fileType: '',
        confirmDisabled: false,
        resourceData: null,
        isLoading: false,
        defaultProject: '',
        defaultJob: '',
        projectData: [],
        selectProject: {},
        isFetchingProject: false,
        jobData: [],
        selectJob: {},
        isFetchingJob: false,
        selectCheckPoint: {},
        selectSavePoint: {},
        selectProjectId: '',
        record: {},
        jobId: '',
        selectJobId: '',
        initSelectJobId: '',
        saveSelectJobId: '',
        isPop: false,
        options: [{
          label: '直接启动',
          value: 'default',
        }, {
          label: '从检查点启动',
          value: 'check',
        }, {
          label: '从保存点启动',
          value: 'save',
        }],
        selectValue: 'default',
        checkList: [],
        saveList: [],
        savepointPath: '',
        runDays: 3,
        runDaysList: [
          { name: '3天', value: 3 },
          { name: '7天', value: 7 },
          { name: '15天', value: 15 },
          { name: '永久', value: -1 },
        ],
        isEditConsumedPosition: false,
        positionList: [],
        originalPositionList: [],
        consumedList: [
          {
            name: '最早位置(earliest-offset)', value: 'earliest-offset'
          },
          {
            name: '最新位置(latest-offset)', value: 'latest-offset'
          },
          {
            name: '指定时间戳', value: 'timestamp'
          },
          {
            name: '继续从上次位置消费(group-offsets)', value: 'group-offsets'
          },

        ]
      }
    },
    created () {

    },
    watch: {
      selectValue: {
        async handler (val) {
          this.savepointPath = ''
          if (val === 'check') {
            this.selectJobId = this.initSelectJobId
          } else if (val === 'save') {
            this.selectJobId = this.saveSelectJobId
          }
          if (val === 'check' || val === 'save') {
            this.getCheckAndSavePoints()
          }
          // if (val === 'save') {

          // }
        }
      }
    },
    methods: {

      onDateChange () {
        arguments[1].selectDate = arguments[0][1]
      },
      openDateChange () {
        if (arguments[0][0]) {//弹出面板
          arguments[1].oldDateStr = arguments[1].dateStr
          arguments[1].isOk = false
        } else if (!arguments[1].isOk) {//关闭面板又没点ok,重置dateStr
          //判断是否选择此刻
          if (arguments[1].selectDate == this.$common.timeToDate(new Date())) {
            this.queryWaitConsumedNum(arguments[1])
          } else {
            arguments[1].dateStr = arguments[1].oldDateStr
          }

        } else {
          this.queryWaitConsumedNum(arguments[1])
        }
      },
      onDateOk (item) {
        item.isOk = true
      },
      async handleChangeStartupMode (item) {
        if (item.startupMode === 'timestamp') {
          if (!item.dateStr) {
            item.dateStr = this.$common.timeToDate(new Date())
          }
        }
        this.queryWaitConsumedNum(item)

      },
      async queryWaitConsumedNum (item) {
        const params = {
          flinkTableName: item.flinkTableName,
          topic: item.topic,
          startupMode: item.startupMode,
          jobId: this.jobId,
          dateStr: item.selectDate ? item.selectDate : item.dateStr
        }
        let res = await this.$http.post(`/job/queryWaitConsumedNum`, params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          item.isShowWaitConsumedNum = true
          item.waitConsumedNum = res.data
        }
      },
      async queryDirectStartData () {

        let res = await this.$http.get(`/job/queryDirectStartData?jobId=${this.jobId}`, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          if (res.data.uatJobRunningValidDays) {
            this.runDays = res.data.uatJobRunningValidDays
          }
          if (res.data.sourceKafkaList) {
            let positionList = res.data.sourceKafkaList
            positionList = positionList.map(item => {
              item.isShowWaitConsumedNum = false
              item.waitConsumedNum = 0
              if (!item.dateStr) {
                item.dateStr = ''
              }
              return item
            })
            this.positionList = positionList
            this.originalPositionList = JSON.parse(JSON.stringify(this.positionList))
          }

        }
        return res
      },
      handleChangeProject (value) {
        this.selectProject = JSON.parse(value.key)
        this.selectProjectId = this.selectProject.id
        this.defaultJob = ''
        this.saveList = []
        this.saveSelectJobId = ''
        this.getJob()
      },
      async getProject (value) {
        const params = {
          "projectName": value || '' //引擎名称
        }
        let res = await this.$http.post('/project/projectManagement/getProjects', params)
        if (res.code === 0) {
          this.projectData = res.data
        }
        return res
      },
      handleProjectSearch (value) {
        // console.log('handleEmployeeSearch', value)
        this.getProject(value)
      },
      handleChangeJob (value) {
        this.selectJob = JSON.parse(value.key)
        this.saveSelectJobId = this.selectJob.id
        this.selectJobId = this.saveSelectJobId
        this.getCheckAndSavePoints()
      },
      async getJob (value) {
        const params = {
          "vo": {
            "jobName": value || '' //作业名称
          }
        }
        let res = await this.$http.post('/job/selectJob', params, {
          headers: {
            projectId: this.selectProjectId
          }
        })
        if (res.code === 0) {
          this.jobData = res.data
        }
        return res
      },
      handleJobSearch (value) {
        // console.log('handleEmployeeSearch', value)
        this.getJob(value)
      },
      clickCheck (record) {
        this.selectCheckPoint = record
        this.checkList = this.checkList.map(item => {
          item.checked = false
          return item
        })
        this.selectCheckPoint.checked = true
        this.savepointPath = this.selectCheckPoint.filePath
      },
      clickSave (record) {
        this.selectSavePoint = record
        this.saveList = this.saveList.map(item => {
          item.checked = false
          return item
        })
        this.selectSavePoint.checked = true
        this.savepointPath = this.selectSavePoint.filePath
      },
      start () {
        if (this.selectValue === 'check') {
          if (!this.savepointPath) {
            this.$message.warning({ content: '未选择检查点', duration: 2 })
            return
          }
        } else if (this.selectValue === 'save') {
          if (!this.savepointPath) {
            this.$message.warning({ content: '未选择保存点', duration: 2 })
            return
          }
        }
        this.runJobApi()
      },
      async open (record) {
        this.isPop = true
        this.record = record
        this.fileType = record.fileType
        this.jobId = record.id
        this.selectJobId = record.id
        this.initSelectJobId = record.id
        this.saveSelectJobId = record.id
        this.selectValue = 'default'
        this.selectProjectId = Number(this.$route.query.projectId)
        this.defaultProject = ''
        this.defaultJob = ''
        this.isEditConsumedPosition = false
        this.positionList = []
        this.resourceData = null
        this.isLoading = true
        const setting = await this.getSetting()//获取设置，资源验证开关是否打开
        if (setting.code === 0 && setting.data && setting.data.resourceValidate) {
          await this.resourceValidate()//资源验证
        } else if (setting.code !== 0) {
          setting.msg && this.$message.error({ content: setting.msg, duration: 2 })
        }
        await this.queryDirectStartData()

        this.isLoading = false
        const res = await this.getProject('')
        if (res.code === 0) {
          const selectProject = res.data.find(item => {
            return item.id.toString() === this.$route.query.projectId.toString()
          })
          this.selectProject = selectProject
        }
        this.defaultProject = { key: JSON.stringify(this.selectProject), label: this.selectProject.projectName }
        const jobs = await this.getJob()
        if (jobs.code === 0) {
          const selectJob = this.jobData.find(item => {
            return item.id.toString() === this.jobId.toString()
          })
          this.defaultJob = { key: JSON.stringify(selectJob), label: selectJob.jobName }
        }
      },
      // 启动作业
      async runJobApi () {
        const params = [{
          vo: {
            id: this.record.id
          },
          useLatest: true,
          savepointPath: this.savepointPath,
          uatJobRunningValidDays: this.runDays
        }]
        if (this.isEditConsumedPosition && this.selectValue === 'default') {
          if (this.positionList.length) {
            const positionList = []
            for (let i = 0; i < this.positionList.length; i++) {
              if ((this.positionList[i].startupMode !== this.originalPositionList[i].startupMode) || (this.positionList[i].startupMode === 'timestamp' && this.positionList[i].dateStr !== this.originalPositionList[i].dateStr)) {

                if (this.positionList[i].selectDate) {
                  this.positionList[i].dateStr = this.positionList[i].selectDate
                }
                //删除额外添加的属性
                delete this.positionList[i].oldDateStr
                delete this.positionList[i].selectDate
                delete this.positionList[i].isOk
                positionList.push(this.positionList[i])//修改了消费位置才提交
              }
            }
            if (positionList.length > 0) {
              params[0].sourceKafkaList = positionList
            }

          }

        }
        let res = await this.$http.post('/job/startJob', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.isPop = false
        }
        this.runCallback(res)
      },
      async getCheckAndSavePoints () {
        this.isLoading = true
        let res = await this.$http.get(`/savepoint/queryCheckpointAndSavepoint?jobId=${this.jobId}&selectJobId=${this.selectJobId}`, {
          headers: {
            projectId: this.selectValue === 'save' ? this.selectProjectId : Number(this.$route.query.projectId)
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          let checkList = res.data.checkpoints
          checkList = checkList.map(item => {
            item.checked = false
            return item
          })
          this.checkList = checkList

          let saveList = res.data.savepoints
          saveList = saveList.map(item => {
            item.checked = false
            return item
          })
          this.saveList = saveList
        }
      },
      async resourceValidate () {//判断资源是否不足
        this.resourceData = null
        const params = {
          id: this.jobId
        }
        let res = await this.$http.post(`/setting/engineSetting/resourceValidate`, params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.resourceData = res.data
        } else {
          res.msg && this.$message.error({ content: res.msg, duration: 2 })
        }
        return res
      },
      async getSetting () {
        let res = await this.$http.get('/sysconfig/getSysoper')
        return res

      }
    }
  }
</script>
<style lang="scss" scoped>
  .start-main {
    min-height: 200px;
    .tip {
      color: #2e2c37;
      margin-bottom: 16px;
      margin-top: -10px;
      font-size: 12px;
    }
    .default {
      .default-tip {
        margin-top: 16px;
        height: 26px;
        line-height: 26px;
        background: #fff7e6;
        border: 1px solid #ffcc88;
        padding: 0 8px;
        color: #ff9118;
        font-size: 12px;
        i {
          margin-right: 6px;
        }
      }
      .checkbox-wrapper {
        margin: 8px 0;
        font-weight: 600;
        font-size: 14px;
      }
      .timestamp-input {
        width: 152px;
        min-width: 152px !important;
        height: 28px;
        line-height: 28px;
        margin-left: 8px;
        /deep/ input {
          font-size: 12px;
          height: 28px;
        }
        /deep/ .ant-calendar-picker-icon {
          display: none;
        }
        /deep/ .ant-calendar-picker-clear {
          right: 6px;
        }
      }
    }

    .radios {
      font-weight: 600;
      margin-bottom: 10px;
      /deep/ label {
        margin-right: 24px;
      }
    }
    .header,
    .body {
      padding: 0 16px;
      p {
        height: 32px;
        line-height: 32px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        margin-left: 6px;
        /deep/ .ant-select-selection--single {
          height: 28px;
          margin-top: 2px;
          .ant-select-selection__rendered {
            line-height: 28px;
          }
        }
      }
    }
    .header {
      height: 32px;
      background: #f9f9f9;
      color: #333;
      font-size: 12px;
      font-weight: 600;
    }
    .body {
      height: 300px;
      overflow: auto;
      font-size: 12px;
      p {
        cursor: pointer;
      }
    }
    .position-list {
      .no-data {
        text-align: center;
        font-size: 12px;
      }
      .header,
      .body {
        p:nth-of-type(1) {
          width: 30px;
        }
        p:nth-of-type(2) {
          width: 150px;
        }
        p:nth-of-type(3) {
          width: 150px;
        }
        p:nth-of-type(4) {
          width: 550px;
          .consumedNum {
            margin-left: 10px;
          }
        }
      }
    }
    .check-list {
      .header,
      .body {
        p:nth-of-type(1) {
          width: 30px;
        }
        p:nth-of-type(2) {
          width: 50px;
        }
        p:nth-of-type(3) {
          width: 450px;
        }
        p:nth-of-type(4) {
          width: 120px;
        }
      }
    }
    .save-list {
      .header,
      .body {
        p:nth-of-type(1) {
          width: 30px;
        }
        p:nth-of-type(2) {
          width: 50px;
        }
        p:nth-of-type(3) {
          width: 200px;
        }
        p:nth-of-type(4) {
          width: 370px;
        }
        p:nth-of-type(5) {
          width: 150px;
        }
      }
    }
    .save-wrapper {
      .filter {
        margin: 12px 0;
        font-size: 12px;
        .job {
          margin-left: 16px;
        }
        .select-project,
        .select-job {
          width: 180px;
          height: 28px;
          margin-left: 8px;
        }
      }
    }
    .resource-wrapper {
      margin-top: 12px;
      margin-bottom: 16px;
      .resource {
        height: 82px;
        background: #f6f8fa;
        border: 1px solid #c9d0dd;
        border-radius: 4px 4px 0px 0px;
        padding: 0 16px;
        .resource-item {
          font-size: 12px;
          margin-top: 16px;
          .label {
            width: 140px;
            color: #2b2f37;
            font-weight: 600;
          }
          em {
            font-style: normal;
            margin-right: 22px;
            span {
              margin-left: 6px;
              font-weight: 600;
            }
          }
        }
      }
      .img-tip {
        background: #fff4e7;
        border-radius: 0px 0px 4px 4px;
        height: 26px;
        line-height: 26px;
        font-size: 12px;
        text-indent: 16px;
        margin-top: 1px;
        img {
          width: 13px;
          height: 13px;
          margin-right: 2px;
        }
      }
    }
  }
  .footer-left {
    padding-left: 16px;
    font-size: 12px;
    font-weight: 600;
  }
</style>
