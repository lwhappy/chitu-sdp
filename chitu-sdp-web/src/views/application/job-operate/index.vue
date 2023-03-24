<!--
 * @Author: hjg
 * @Date: 2021-10-21 10:16:02
 * @LastEditTime: 2022-09-27 10:51:14
 * @LastEditors: Please set LastEditors
 * @Description: 系统设置包含系统管理员和引擎管理
 * @FilePath: \src\views\application\job-operate\index.vue
-->
<template>
  <div v-if="isShow"
       class="job-operate">
    <div class="search-container">
      <div class="search-main justify-between">
        <div class="right-content justify-start">
          <div class="justify-start product-line">
            <p>更新人</p>
            <a-select placeholder="选择更新人"
                      style="width: 200px;"
                      v-model="updatedBy"
                      :allow-clear="true">
              <a-select-option v-for="(item, index) in updatedByList"
                               :value="item.id"
                               :key="'updatedBy-' + index">
                {{ item.updatedBy }}
              </a-select-option>
            </a-select>
            <p>作业目录</p>
            <folders-autocomplete ref="foldersAutocomplete"
                                  @searchSelect="searchSelect"
                                  @clearFolder="clearFolder"
                                  @onChange="changeFolder" />
            <p>作业名称</p>
            <search-autocomplete ref="searchAuto"
                                 :isShowBtn="false"
                                 :autoMsg='autoMsg'
                                 :dataSource="dataSource"
                                 @searchBtn="searchBtn"
                                 @onChange="changeName"
                                 @onSelect="onSelect" />
            <a-button @click="search"
                      style="margin-left:8px;"
                      type="primary"
                      size="small"
                      icon="search">
              查询
            </a-button>
            <a-button @click="reset"
                      style="margin-left:8px;"
                      size="small"
                      icon="undo">
              重置
            </a-button>
          </div>
        </div>
      </div>

    </div>
    <div class="divider-line"></div>
    <!-- 数据表格 -->
    <div class="job-operate-content">
      <chitu-table class="table-data"
                 v-loading="isLoading"
                 :columns="columns"
                 :dataSource="tableData"
                 :loading="loading"
                 row-key="id"
                 :tableOpt="{customRow:customRow,rowClassName:(record) => { return record.id === rowId ? 'clickRowStyle': 'rowStyleNone' }}"
                 @change="handleTableChange"
                 :pagination="pagination"
                 @pageChange="pageChange"
                 @pageSizeChange="pageSizeChange">
        <!-- 作业目录 -->
        <template #fullPath="{record}">
          <div class="fullPath">
            <a-tooltip placement="topLeft">
              <template slot="title">
                <span>{{record.fullPath}}</span>
              </template>
              <div class="title">{{ shortPath(record.fullPath) }}</div>
            </a-tooltip>
          </div>
        </template>

        <!-- 作业名称 -->
        <template #jobName="{record}">
          <div class="jobName"
               @click.stop="openDrawer(record)"
               @dblclick.stop="gotoDevelop(record)">
            <div class="sql">{{ record.fileType }}</div>
            <a-tooltip placement="topLeft">
              <template slot="title">
                <span>{{record.jobName}}</span>
              </template>
              <div class="title">{{ record.jobName }}</div>
            </a-tooltip>
          </div>
        </template>
        <!-- 作业版本 -->
        <template #runningVersion="{record}">
          <div class="version">
            <span class="runningVersion">{{ record.runningVersion }}</span>
            <div @click="openJobVersion(record.id, record.fileType)"
                 v-show="record.isNewVersion === 1"
                 class="topic">
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>有新版本{{ record.latestVersion }}</span>
                </template>
                <img src="@/assets/icons/new.png"
                     alt="">
              </a-tooltip>

              <!-- <div class="info">有新版本{{ record.latestVersion }}</div> -->
            </div>
          </div>
        </template>

        <!-- 作业优先级 -->
        <!-- <div class="level"
             slot="priority"
             slot-scope="text, record">
          <a-select class="level-select"
                    v-model="record.priority"
                    @select="(value, option) => jobLevelUpdate(value, option, record)">
            <a-select-option v-for="item in jobLevelData"
                             :key="item.id"
                             :value="item.value">
              {{ item.text }}
            </a-select-option>
          </a-select>
        </div> -->
        <!-- 作业状态 -->
        <template #jobStatus="{record}">
          <div class="jobStatus">
            <!-- 当前态 -->
            <div :class="record.jobStatus">{{ record.jobStatus | filterStatus}}</div>
            <!-- 结合当前态与目标态生成的过程态 -->
            <div v-show="record.jobStatus != record.expectStatus"
                 class="process">
              <a-icon type="double-right"
                      style="color:#B3BDCF" />
              <div :class="record.expectStatus">{{ record.jobStatus | filterProcess(record.expectStatus)}} ...</div>
            </div>
          </div>
        </template>
        <!-- 作业等级 -->
        <template #priorityTitle>
          <span>
            作业等级
            <img class="pointer"
                 width="14"
                 height="14"
                 src="@/assets/icons/ask.png"
                 alt=""
                 @click="openPriortyDrawer">
          </span>

        </template>
        <template #priority="{record}">
          <div class="priority">
            <a-select ref="select"
                      v-model="record.priority"
                      style="width: 80px"
                      size="small"
                      @select="handerLevelChange(record,arguments)">
              <a-select-option v-for="item in priorityList"
                               :key="item.value">{{priorityPrefix}}{{item.label}}</a-select-option>

            </a-select>
          </div>
        </template>
        <!-- cpu -->
        <template #cpuCore="{record}">
          <div>{{ record.cpuCore || 0}}</div>
        </template>
        <!-- 内存 -->
        <template #memoryGb="{record}">
          <div class="memoryGb">
            <span v-if="record.memoryGb">{{ record.memoryGb | filterMemoryGb}}</span>
            <span v-else>0</span>
          </div>
        </template>

        <!-- 更新时间 -->
        <template #updationDate="{record}">
          <div>{{ record.updationDate }}</div>
        </template>
        <!-- 操作 -->
        <template #operation="{record}">
          <div class="operation">
            <!-- 启动 -->
            <div v-show="operationSetOne.includes(record.jobStatus)"
                 class="operation-item"
                 @click="clickButton('run', record)">
              <div :class="{'operation-disabled' : record.jobStatus !== record.expectStatus || record.disabled}">
                <i class="chitutree-h5 chitutreeqidong"></i>
                <span>启动</span>
              </div>
            </div>
            <!-- 下线 -->
            <div v-show="operationSetOne.includes(record.jobStatus) && record.jobStatus === record.expectStatus && !record.disabled"
                 class="operation-item">
              <!-- <a-popconfirm @confirm="() => operateItem('下线', 'delete', record)">
              <template slot="title">
                <p>确定要<span class="warn-message">下线</span>吗？</p>
              </template>
              <i class="chitutree-h5 chitutreeicon_xiaxian"></i>
              <span>下线</span>
            </a-popconfirm> -->
              <p @click="clickButton('delete',record)">
                <i class="chitutree-h5 chitutreeicon_xiaxian"></i>
                <span>下线</span>
              </p>
            </div>
            <!-- 恢复 -->
            <div v-show="operationSetThree.includes(record.jobStatus)"
                 class="operation-item">
              <div class="operation-disabled"
                   v-if="record.jobStatus !== record.expectStatus || record.disabled">
                <i class="chitutree-h5 chitutreehuifu"></i>
                <span>恢复</span>
              </div>
              <!-- 无新版本作业 -->
              <!-- <div v-else-if="record.jobStatus === record.expectStatus && record.isNewVersion === 0"
                 @click="operateItem('恢复', 'recovery', record, false)">
              <i class="chitutree-h5 chitutreehuifu"></i>
              <span>恢复</span>
            </div> -->
              <div v-else-if="record.jobStatus === record.expectStatus && record.isNewVersion === 0"
                   @click="clickButton('resourceValidate', record, false)">
                <i class="chitutree-h5 chitutreehuifu"></i>
                <span>恢复</span>
              </div>
              <!-- 有新版本作业 -->
              <!-- <a-popconfirm v-else-if="record.jobStatus === record.expectStatus && record.isNewVersion === 1"
                          @confirm="() => operateItem('恢复', 'recovery', record, true)">
              <template slot="title">
                <h3>是否使用最新版本</h3>
                <a-radio-group v-model="useNewVersion">
                  <a-radio :value="true">是</a-radio>
                  <a-radio :value="false">否</a-radio>
                </a-radio-group>
              </template>
              <i class="chitutree-h5 chitutreehuifu"></i>
              <span>恢复</span>
            </a-popconfirm> -->
              <p v-else-if="record.jobStatus === record.expectStatus && record.isNewVersion === 1"
                 @click="clickButton('resourceValidate', record, true)">

                <i class="chitutree-h5 chitutreehuifu"></i>
                <span>恢复</span>
              </p>
            </div>
            <!-- 暂停 -->
            <div v-show="operationSetTwo.includes(record.jobStatus)"
                 class="operation-item">
              <div class="operation-disabled"
                   v-if="record.jobStatus !== record.expectStatus || record.disabled">
                <i class="chitutree-h5 chitutreehuifu"></i>
                <span>暂停</span>
              </div>
              <!-- <a-popconfirm v-else
                          @confirm="() => operateItem('暂停', 'templateStop', record)">
              <template slot="title">
                <p>确定要<span class="warn-message">暂停</span>作业吗？</p>
              </template>
              <i class="chitutree-h5 chitutreezanting"></i>
              <span>暂停 </span>
            </a-popconfirm> -->
              <p v-else
                 @click="clickButton('templateStop',record)">
                <i class="chitutree-h5 chitutreezanting"></i>
                <span>暂停 </span>
              </p>
            </div>
            <!-- 停止 -->
            <div v-show="operationSetFour.includes(record.jobStatus)"
                 class="operation-item stop-btn">
              <div class="operation-disabled"
                   v-if="record.jobStatus !== record.expectStatus || record.disabled">
                <i class="chitutree-h5 chitutreetingzhi"></i>
                <span>停止</span>
              </div>
              <!-- <a-popconfirm v-else
                          @confirm="() => operateItem('停止', 'stop', record)">
              <template slot="title">
                <h3>注意：确定要停止作业？</h3>
                <p>停止该作业会自动清除掉相关的</p>
                <p>Checkpoints</p>
              </template>
              <i class="chitutree-h5 chitutreetingzhi"></i>
              <span>停止</span>
            </a-popconfirm> -->
              <p v-else
                 @click="clickButton('stop',record)">
                <i class="chitutree-h5 chitutreetingzhi"></i>
                <span>停止</span>
              </p>

            </div>
            <!-- flink_ui -->
            <div v-if="record.jobStatus === record.expectStatus && record.jobStatus === 'RUNNING' && !record.disabled"
                 class="operation-item"
                 @click="operateItem('flink_ui', 'flink_ui', record)">
              <i class="chitutree-h5 chitutreeicon_flinkui"></i>flink_ui
            </div>
            <div v-else
                 class="operation-item operation-disabled">
              <i class="chitutree-h5 chitutreeicon_flinkui"></i>flink_ui
            </div>
            <div class="more operation-item">
              <a-popover trigger="click"
                         placement="bottom">
                <template slot="content">
                  <ul class="more-list">
                    <li v-if="record.jobStatus === record.expectStatus && record.jobStatus === 'RUNNING' && !record.disabled"
                        @click="operateItem('flink_job', 'flink_job', record)">flink job 监控</li>
                    <li v-else
                        class="operation-disabled">flink job 监控</li>
                    <li v-if="record.jobStatus==='RUNNING'"
                        @click="popSavePointConfirm(record)">添加保存点</li>
                    <li v-else
                        class="operation-disabled">添加保存点</li>
                  </ul>
                </template>
                <p @click.stop="showMore(record)"><i class="chitutree-h5 chitutreeicon_more"></i>更多</p>
              </a-popover>
            </div>

          </div>
        </template>
      </chitu-table>
    </div>
    <!-- 作业抽屉 -->
    <drawer v-if="drawerVisible"
            ref="jobDetail"
            :drawerVisible="drawerVisible"
            @closeDrawer="closeDrawer" />
    <!-- 分页 -->
    <div v-if="false"
         class="job-operate-footer">
      <Pagination :pagination="pagination"
                  @pageChange="pageChange"
                  @pageSizeChange="pageSizeChange" />
    </div>
    <!-- 作业版本对比 -->
    <job-version ref="jobVersion">
    </job-version>
    <!-- 二次确认框 -->
    <confirm-dialog :visible="popConfirm"
                    :type="confirmType"
                    :confirmDisabled="confirmDisabled"
                    :closable="getClosable()"
                    :confirm-text="getConfirmText()"
                    :cancel-text="getCancelText()"
                    @close="dialogClose()"
                    @cancel="dialogCancel()"
                    @confirm="dialogConfirm()">
      <template v-if="clickType === 'delete'">
        <p class="word-break">确定要<span class="warn-message">&nbsp;下线&nbsp;</span>{{selectRecord.jobName}}吗？</p>
      </template>
      <template v-else-if="clickType === 'run'">
        <p class="word-break">确定要<span class="warn-message">&nbsp;启动&nbsp;</span>{{selectRecord.jobName}}吗？</p>
      </template>
      <template v-else-if="clickType === 'templateStop'">
        <p class="word-break">确定要<span class="warn-message">&nbsp;暂停&nbsp;</span>{{selectRecord.jobName}}吗？</p>
      </template>
      <template v-else-if="clickType === 'recovery'">
        <div v-if="useLatest">
          <h3>请选择<span class="warn-message">&nbsp;恢复&nbsp;</span>{{selectRecord.jobName}}作业的版本</h3>
          <a-radio-group v-model="useNewVersion">
            <a-radio :value="true">线上最新版本：{{selectRecord.latestVersion}}</a-radio>
            <a-radio :value="false">运行中的版本：{{selectRecord.runningVersion}}</a-radio>
          </a-radio-group>
        </div>
        <p v-else
           class="word-break">确定要<span class="warn-message">&nbsp;恢复&nbsp;</span>{{selectRecord.jobName}}吗？</p>
      </template>
      <template v-else-if="clickType === 'stop'">
        <p class="word-break">注意：确定要<span class="warn-message">&nbsp;停止&nbsp;</span>{{selectRecord.jobName}}作业吗？</p>
        <!-- <p>停止该作业会自动清除掉相关的Checkpoints</p> -->
      </template>
      <template v-else-if="clickType === 'priority'">
        <p class="word-break">已将{{selectRecord.jobName}}作业等级从{{priorityPrefix}}{{selectRecord.oldPriority}}修改为{{priorityPrefix}}{{selectRecord.priority}}，是否需要以{{priorityPrefix}}{{selectRecord.priority}}作业等级同步更新作业告警规则？
        </p>
      </template>
      <div v-else-if="clickType === 'resourceValidate'"
           v-loading="isResourceValidating"
           style="margin-bottom:10px">
        <resourceValidateInfo :record="selectRecord"
                              :info="resourceData" />
      </div>
    </confirm-dialog>
    <!-- 二次确认框end -->
    <!-- 添加保存点弹窗 -->
    <confirm-dialog2 :visible="savePointConfirm"
                     title="添加保存点"
                     @close="savePointConfirm=false"
                     @confirm="savePoint()">
      <div class="save-point-input">
        <p>保存点名称：</p>
        <a-input v-model="savePointValue" />
      </div>

    </confirm-dialog2>
    <!-- 添加保存点弹窗end -->
    <!-- 错误提示框 -->
    <tip-dialog :visible="isShowDialogTip"
                type="warning"
                title="失败"
                @close="isShowDialogTip=false"
                @confirm="isShowDialogTip=false">
      <div>{{dialogTipMessage}}</div>
    </tip-dialog>
    <!-- 错误提示框end -->
    <start-dialog ref="startDialog"
                  :run-callback="runCallback"></start-dialog>
    <!-- 作业等级说明 -->
    <priority-drawer ref="drawer"
                     :drawerVisible="priorityDrawerVisible"
                     @closeDrawer="priorityDrawerVisible = false" />
  </div>
</template>

<script>
  import jobVersion from '@/components/job-version/index'
  import searchAutocomplete from '@/components/search-autocomplete/commonIndex'
  import Pagination from '@/components/pagination/index'
  import drawer from './components/drawer.vue'
  import { mapActions } from 'vuex'
  // import tableHeaderDrag from '../../../mixins/table-header-drag'
  // import tableSort from '../../../mixins/table-sort'
  import FoldersAutocomplete from '@/components/folders-autocomplete'
  import ConfirmDialog from '@/components/confirm-dialog'
  import ConfirmDialog2 from '@/components/confirm-dialog/index2'

  import TipDialog from '@/components/tip-dialog'
  import startDialog from './components/start-dialog'
  import resourceValidateInfo from './components/resource-validate-info'

  import priorityDrawer from '@/components/priority-drawer.vue'
  const pagination = {
    current: 1,
    showSizeChanger: true,
    showQuickJumper: true,
    defaultPageSize: 20,
    total: 0,
    pageSizeOptions: ['10', '20', '40', '60']
  }
  const params = {
    orderByClauses: [{
      field: "updation_date", //排序键名
      orderByMode: 1 //排序模式（0：正序，1：倒序）
    }],
    page: 1,
    pageSize: 20,
    vo: {
      jobName: null,
      searchParams: [],
      updatedBy: ''
    }
  }
  export default {
    // mixins: [tableHeaderDrag, tableSort],
    mixins: [],
    components: {
      searchAutocomplete,
      Pagination,
      drawer,
      jobVersion,
      FoldersAutocomplete,
      ConfirmDialog,
      TipDialog,
      ConfirmDialog2,
      startDialog,
      priorityDrawer,
      resourceValidateInfo
    },
    computed: {
      columns () {
        return [{
          title: '作业目录',
          dataIndex: 'fullPath',
          width: 150,
          // sortDirections: ['descend', 'ascend'],
          // sorter: () => this.handleTableChange,
          scopedSlots: { customRender: 'fullPath' },
          // sortOrder: this.sortedInfo.columnKey === 'full_path' && this.sortedInfo.order
        }, {
          title: '作业名称',
          dataIndex: 'job_name',
          width: 250,
          defaultSortOrder: 'descend',
          sortDirections: ['ascend', 'descend', 'ascend'],
          sorter: (a, b) => a.jobName - b.jobName,
          scopedSlots: { customRender: 'jobName' },
        },
        {
          title: '作业版本',
          dataIndex: 'runningVersion',
          width: 100,
          scopedSlots: { customRender: 'runningVersion' }
        },
        // {
        //   title: '优先级',
        //   dataIndex: 'priority',
        //   width: 120,
        //   sortDirections: ['descend', 'ascend'],
        //   sorter: () => this.handleTableChange,
        //   scopedSlots: { customRender: 'priority' },
        //   sortOrder: this.sortedInfo.columnKey === 'priority' && this.sortedInfo.order
        // },
        {
          title: '状态',
          dataIndex: 'jobStatus',
          scopedSlots: { customRender: 'jobStatus' },
          width: 170,
          filteredValue: this.filteredValue,//已筛选的 value 数组
          filters: [{
            text: '初始状态',
            value: 'INITIALIZE'
          }, {
            text: '运行中',
            value: 'RUNNING'
          }, {
            text: '已停止',
            value: 'TERMINATED'
          }, {
            text: '已暂停',
            value: 'PAUSED'
          }, {
            text: '成功',
            value: 'FINISHED'
          }, {
            text: '启动失败',
            value: 'SFAILED'
          }, {
            text: '恢复失败',
            value: 'RFAILED'
          }]
        },
        // {
        //   title: '作业等级',
        //   dataIndex: 'priority',
        //   scopedSlots: { customRender: 'priority' },
        //   width: 150,
        //   filteredValue: this.filteredLevel,//已筛选的 value 数组
        //   filters: this.filterPriorityList
        // },
        {
          dataIndex: 'priority',
          // scopedSlots: { customRender: 'priority' },
          scopedSlots: { title: 'priorityTitle', customRender: 'priority' },
          // slots: { title: 'priorityTitle' },
          width: 120,
          filteredValue: this.filteredLevel,//已筛选的 value 数组
          filters: this.filterPriorityList
        },
        // {
        //   title: 'CPU',
        //   dataIndex: 'cpu_core',
        //   sorter: () => this.handleTableChange,
        //   sortDirections: ['descend', 'ascend'],
        //   width: 120,
        //   scopedSlots: { customRender: 'cpuCore' },
        //   sortOrder: this.sortedInfo.columnKey === 'cpu_core' && this.sortedInfo.order
        // },
        {
          title: '并行度',
          dataIndex: 'parallelism',
          defaultSortOrder: 'descend',
          sortDirections: ['ascend', 'descend', 'ascend'],
          sorter: (a, b) => a.parallelism - b.parallelism,
          width: 70,
          scopedSlots: { customRender: 'parallelism' },
        },
        {
          title: 'slots',
          dataIndex: 'slots',
          // sorter: () => this.handleTableChange,
          // sortDirections: ['descend', 'ascend'],
          width: 70,
          scopedSlots: { customRender: 'slots' },
          // sortOrder: this.sortedInfo.columnKey === 'slots' && this.sortedInfo.order
        },
        {
          title: '内存',
          dataIndex: 'memory_gb',
          // defaultSortOrder: 'descend',
          // sortDirections: ['ascend', 'descend', 'ascend'],
          // sorter: (a, b) => a.memoryGb - b.memoryGb,
          width: 70,
          scopedSlots: { customRender: 'memoryGb' },
        },
        {
          title: '更新人',
          dataIndex: 'updatedBy',
          defaultSortOrder: 'descend',
          sortDirections: ['ascend', 'descend', 'ascend'],
          sorter: (a, b) => a.updatedBy - b.updatedBy,
          width: 100,
          scopedSlots: { customRender: 'updatedBy' },
        },
        {
          title: '更新时间',
          dataIndex: 'updation_date',
          width: 150,
          defaultSortOrder: 'descend',
          sortDirections: ['ascend', 'descend', 'ascend'],
          sorter: (a, b) => a.updationDate - b.updationDate,
          scopedSlots: { customRender: 'updationDate' },
        },
        {
          title: '操作',
          dataIndex: 'operation',
          width: 300,
          fixed: 'right',
          scopedSlots: { customRender: 'operation' }
        }]
      }
    },
    data () {
      return {
        oldProjectId: '',
        isShouldClose: true,
        confirmDisabled: false,
        isResourceValidating: false,
        resourceData: null,
        priorityDrawerVisible: false,
        updateRule: false,//是否点了更新
        priorityPrefix: '',
        priorityList: [],//作业等级列表
        filterPriorityList: [],//筛选作业等级列表
        isShow: true,
        filteredValue: [],
        filteredLevel: [],
        isLoading: false,
        isShowDialogTip: false,
        dialogTipMessage: '',
        confirmType: 'normal',
        useLatest: false,
        selectRecord: {},
        popConfirm: false,
        savePointConfirm: false,
        savePointValue: '',
        clickType: '',
        callback: function () { },
        updatedBy: undefined,
        updatedByList: [],
        drawerVisible: false,
        useNewVersion: false,
        jobLevelData: [{
          id: 'level-1',
          value: '高',
          text: '高'
        }, {
          id: 'level-2',
          value: '中',
          text: '中'
        }, {
          id: 'level-3',
          value: '低',
          text: '低'
        }],
        tableSortData: {
          columnsName: 'columns',
          ref: 'jobOperateTable'
        },
        timer: null,
        time: 10 * 1000,
        operationSetOne: [ // 当前四种状态下，可有启动，删除按钮
          'INITIALIZE',
          'TERMINATED',
          'FINISHED',
          'SFAILED'
        ],
        operationSetTwo: [ // 当前两种状态下，可有暂停，停止按钮
          'RUNNING'
        ],
        operationSetThree: [ // 当前状态下，可有恢复按钮
          'PAUSED',
          'RFAILED'
        ],
        operationSetFour: [ // 当前两种状态下，可有停止按钮
          'RUNNING',
          'PAUSED',
          'RFAILED'
        ],
        autoMsg: '搜索名称...',
        dataSource: [],
        headerDragData: {
          columnsName: 'columns',
          ref: 'jobOperateTable'
        },
        tableData: [],
        loading: false,
        selectedDataSource: [],
        rowSelection: {
          onChange: (selectedRowKeys, selectedRows) => {
            console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows)
          },
          onSelect: (record, selected, selectedRows) => {
            console.log('------onSelect: ', record, selected, selectedRows)
            this.selectedDataSource = selectedRows
          },
          onSelectAll: (selected, selectedRows, changeRows) => {
            console.log('------onSelectAll: ', selected, selectedRows, changeRows)
            this.selectedDataSource = selectedRows
          }
        },
        pagination: JSON.parse(JSON.stringify(pagination)),
        params: JSON.parse(JSON.stringify(params)),
        rowId: null
      }
    },
    filters: {
      filterMemoryGb (value) { // 内存过滤映射
        return value.replace(/"/g, "")
      },
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
      filterProcess (status, expectStatus) { // 过程态过滤映射
        let text = null
        if (status === 'RUNNING') {
          text = '停止中'
          if (expectStatus === 'PAUSED') {
            text = '暂停中'
          }
        } else if (status === 'PAUSED') {
          text = '恢复中'
          if (expectStatus === 'TERMINATED') {
            text = '停止中'
          }
        } else if (status === 'SFAILED') {
          text = '启动中'
          if (expectStatus === 'TERMINATED') {
            text = '停止中'
          }
        } else if (status === 'RFAILED') {
          text = '恢复中'
          if (expectStatus === 'TERMINATED') {
            text = '停止中'
          }
        } else {
          text = '启动中'
        }
        return text
      }
    },
    created () {
      this.init()
      this.oldProjectId = this.$route.query.projectId

    },
    mounted () {
      this.$bus.$off('jobOperateUpdate').$on('jobOperateUpdate', (data) => {
        this.$refs.searchAuto.defaultValue = data
        this.search()
      })
    },
    activated () {

    },
    watch: {
      $route: {//需要同时监听切换项目和切换页签时的路由变化，beforeRouteEnter和activated只能监听到切换页签，不能监听到项目切换
        handler () {
          if (this.$route.name === 'JobOperate') {
            if (this.$route.query.projectId !== this.oldProjectId) {
              this.resetData()
              this.init()
              this.oldProjectId = this.$route.query.projectId
            } else {
              this.getJobList()
              this.loopHttp()
            }
          } else {
            this.clearTimer()
            this.closeDrawer()
          }
        },
        deep: true,
        // immediate: true
      },

      '$store.getters.isRemoveTag': {//监听关闭页签，关闭页签后清除缓存
        handler (val, oldVal) {
          if (val === oldVal) {
            return
          }
          if (this.$store.getters.removeRouteName.includes('JobOperate')) {
            this.$common.toClearCache(this);
          }
        }
      }
    },
    // beforeRouteEnter: (to, from, next) => {
    //   next((vm) => {
    //     if (vm.$route.query.projectId !== vm.oldProjectId) {
    //       vm.resetData()
    //       vm.init()
    //       vm.oldProjectId = vm.$route.query.projectId
    //     }
    //   })
    // },
    // beforeRouteLeave (to, from, next) {
    //   this.clearTimer()
    //   this.closeDrawer()
    //   next();

    // },
    methods: {
      ...mapActions('job', {
        'jobInfo': 'jobInfo'
      }),
      clearPageCache () {
        //重置为初始数据
        // if (this.$data && this.$options) {
        //   Object.assign(this.$data, this.$options.data())
        // }
        //重置为初始数据 end
        //摧毁缓存dom
        this.isShow = false
        this.$nextTick(() => {
          this.isShow = true
        })
        //摧毁缓存dom end
      },
      clearTimer () {
        clearTimeout(this.timer)
        this.timer = null
      },
      reset () {
        this.resetData()
        this.init()
      },
      resetData () {
        // Object.assign(this.$data, this.$options.data(this))
        this.tableData = []
        this.drawerVisible = false
        this.filteredValue = []
        this.filteredLevel = []
        this.updatedBy = undefined
        this.$refs.searchAuto.defaultValue = ''
        this.$refs.foldersAutocomplete.keyword = ''
        this.pagination = JSON.parse(JSON.stringify(pagination))
        this.params = JSON.parse(JSON.stringify(params))
      },
      init () {
        this.clearTimer()
        this.isShouldClose = true
        this.filterPriorityList = []
        const globalPriority = this.$common.getPriority()
        this.priorityPrefix = globalPriority.labelPrefix
        this.priorityList = globalPriority.list
        this.priorityList.forEach(item => {
          const obj = {
            text: this.priorityPrefix + item.label,
            value: item.value
          }
          this.filterPriorityList.push(obj)
        })

        this.getJobList('init')
        this.loopHttp()
        this.getUpdatedByList()
      },
      // 获取项目下更新人列表
      async getUpdatedByList () {
        let res = await this.$http.post('/job/selectUpdatedBy', {}, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          let data = res.data
          this.updatedByList = Object.keys(data).filter((key) => data[key] !== null && data[key] !== undefined).reduce((acc, key) => ({ ...acc, [key]: data[key] }), {}); // 过滤掉后台返回的空数据
        }
      },
      // 打开作业版本弹框
      openJobVersion (jobId, fileType) {
        let data = {
          jobId: jobId,
          type: 'job',
          fileType: fileType
        }
        this.$refs['jobVersion'].open(data)
      },
      // 打开抽屉
      openDrawer (jobInfo) {
        if (this.$store.getters.jobInfo === null) { // state 有数据时
          this.drawerVisible = true
        } else { // state 无数据时
          if (this.$store.getters.jobInfo.id === jobInfo.id) {
            this.drawerVisible = !this.drawerVisible
          } else {
            this.drawerVisible = true
          }
        }
        if (this.drawerVisible) {
          this.rowId = jobInfo.id
        } else {
          this.rowId = null
        }
        this.$store.dispatch('job/setJobInfo', jobInfo)
      },
      // 关闭抽屉
      closeDrawer () {
        this.drawerVisible = false
        this.rowId = null
      },
      // 作业优先级变更
      jobLevelUpdate (value, option, record) {
        let params = {
          vo: {
            id: record.id,
            priority: value
          }
        }
        this.updateJobLevel(params)
      },
      async updateJobLevel (params) {
        let res = await this.$http.post('/job/setPriorityLevel', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.getJobList()
          this.$message.success({ content: '修改优先级成功', duration: 2 })
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        // console.log('------handleTableChange:', pagination, filters, sorter)
        // this.resetSortMethods(sorter)
        // sorter = this.sortedInfo
        if (sorter.order) {
          if (sorter.order === 'ascend') {
            this.params.orderByClauses[0].orderByMode = 0
          } else {
            this.params.orderByClauses[0].orderByMode = 1
          }
        }
        if (filters && filters.jobStatus) {
          this.params.vo.searchParams = filters.jobStatus
          this.filteredValue = filters.jobStatus
        }
        console.log('filters', filters)
        if (filters && filters.priority && filters.priority.length) {
          this.params.vo.prioritys = filters.priority.join(',')
          this.filteredLevel = filters.priority
        } else {
          this.params.vo.prioritys = null
          this.filteredLevel = []
        }

        this.params.orderByClauses[0].field = sorter.field
        if (this.params.orderByClauses[0].field === 'updatedBy') {
          this.params.orderByClauses[0].field = 'updated_by'
        }
        this.getJobList(true)
      },
      changeFolder () {
        console.log('changeFolder')
        this.params.vo.folderId = ''
      },
      // 输入值变化时搜索补全
      async changeName (value) {
        console.log('公共-onChange: ', value)
        if (value === '') {
          this.params.vo.jobName = null
          this.getJobList()
        } else {
          // this.params.vo.jobName = value
          const params = {
            vo: {
              jobName: value
            }
          }
          let res = await this.$http.post('/job/searchJob', params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          // // console.log('-----------res: ', res)
          if (res.code === 0) {
            this.dataSource = []
            res.data.map(item => {
              this.dataSource.push({
                text: item,
                title: item,
                label: item
              })
            })
          }
        }
      },
      // 选中后进行搜索
      onSelect (value) {
        console.log('公共-onSelect: ', value)
        this.params.vo.jobName = value.text
        this.params.page = 1
        this.getJobList()
      },
      clearFolder () {
        this.params.vo.folderId = ''
      },
      // 搜索按钮事件
      searchBtn (value) {

        this.params.vo.jobName = value || ''
        this.params.page = 1
        this.getJobList()
      },
      search () {
        this.params.vo.jobName = this.$refs.searchAuto.defaultValue || ''
        this.params.page = 1
        this.getJobList('init')
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.params.page = pageInfo.page
        this.getJobList()
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        // // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
        this.params.page = pageSizeInfo.current
        this.params.pageSize = pageSizeInfo.size
        this.getJobList()
      },
      runCallback (res) {
        if (res.code === 0) {
          this.delayHttp()
        } else if (res.code === 9510) {
          this.isShowDialogTip = true
          this.dialogTipMessage = res.msg
          this.loopHttp()
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
          this.loopHttp()
        }
      },
      /* 操作作业
      * (删除和跳转操作flinkui操作除外)各个操作结束立马重新发其请求获取当前页面的数据，不必再等待请求的返回结果成功或失败
      * 原因：前端不在些状态扭转逻辑，有后端进行状态逻辑扭转
      * 目的：保证前后端状态一致，且减少后期业务增添造成的维护
      */
      operateItem (text, type, jobInfo, useLatest) {
        if (jobInfo.jobStatus !== jobInfo.expectStatus) return
        // 删除和跳转flinkui不在发送请求
        if (type !== 'delete' && type !== 'flink_ui' && type !== 'flink_job') {
          this.clearTimer()
          this.tableData.forEach(item => {
            if (item.id === jobInfo.id) {
              item.disabled = true
            }
          })
          this.$forceUpdate()
        }
        let params = [
          {
            vo: {
              id: jobInfo.id
            }
          }
        ]
        if (type === 'delete') {
          params = [
            {
              id: jobInfo.id
            }
          ]
          this.deleteJobApi(params)
        } else if (type === 'run') {
          params = [{
            vo: {
              id: jobInfo.id
            },
            useLatest: true
          }]
          this.runJobApi(params)
        } else if (type === 'recovery') {
          params = {
            vo: {
              id: jobInfo.id
            },
            useLatest: this.useNewVersion
          }
          if (!useLatest) {
            params.useLatest = useLatest
          }
          this.recoveryJobApi(params)
          this.useNewVersion = true
        } else if (type === 'templateStop') {
          params = {
            vo: {
              id: jobInfo.id
            }
          }
          this.templateStopJobApi(params)
        } else if (type === 'stop') {
          // console.log('stop')
          this.stopJobApi(params)
        } else if (type === 'flink_ui') {
          window.open(jobInfo.flinkUrl)
        } else if (type === 'flink_job') {
          window.open(jobInfo.grafanaUrl)
        }
      },
      // 启动作业
      async runJobApi (params) {
        let res = await this.$http.post('/job/startJob', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.delayHttp()
        } else if (res.code === 9510) {
          this.isShowDialogTip = true
          this.dialogTipMessage = res.msg
          this.loopHttp()
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
          this.loopHttp()
        }
      },
      // 删除作业
      async deleteJobApi (params) {
        let res = await this.$http.post('/job/deleteJob', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.params.page = 1
          this.getJobList()
          this.$message.success({ content: '下线成功', duration: 2 })
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 暂停作业
      async templateStopJobApi (params) {
        let res = await this.$http.post('/job/pauseJob', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.delayHttp()
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
          this.loopHttp()
        }
      },
      // 停止作业
      async stopJobApi (params) {
        // console.log('stop')
        let res = await this.$http.post('/job/stopJob', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.delayHttp()
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
          this.loopHttp()
        }
      },
      // 恢复作业
      async recoveryJobApi (params) {
        let res = await this.$http.post('/job/recoverJob', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.delayHttp()
        } else if (res.code === 9510) {
          this.isShowDialogTip = true
          this.dialogTipMessage = res.msg
          this.loopHttp()
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
          this.loopHttp()
        }
      },
      delayHttp () {
        this.clearTimer()
        this.getJobList()
        this.loopHttp()
      },
      // 批量操作
      batchOperate (type) {
        console.log('----------batchOperate: ', type)
        // console.log('------handleTableChange-selectedDataSource:', this.selectedDataSource)
      },
      // 重置分页信息
      resetPagination (pageInfo) {
        this.pagination.current = pageInfo.page
        this.pagination.total = pageInfo.rowTotal
      },
      // 获取作业（查询）列表
      async getJobList (isInit) {
        const params = JSON.parse(JSON.stringify(this.params))
        if (this.updatedBy !== undefined) {
          params.vo.updatedBy = this.updatedBy
        } else {
          params.vo.updatedBy = ''
        }
        if (isInit) {
          this.isLoading = true
          this.tableData = []
        }
        let res = await this.$http.post('/job/queryJob', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isLoading = false
        // // console.log('-----------res: ', res)
        if (res.code === 0) {
          const tableData = [...res.data.rows]
          tableData.forEach(item => {
            item.disabled = false
            item.isShow = false
            item.oldPriority = item.priority
          })
          this.tableData = tableData
          this.resetPagination(res.data)
          this.$forceUpdate()
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      loopHttp () {
        this.timer = setTimeout(() => {
          if (this.$route.name === 'JobOperate') {//防止有时定时器没清掉一直执行
            this.getJobList()
          }
          this.loopHttp()
        }, this.time)
      },
      shortPath (path) {
        const lastIndex = path.lastIndexOf('/')
        const newPath = path.substr(lastIndex + 1)
        return newPath
      },

      customRow (item) {
        return {
          props: {
          },
          on: { // 事件
            // click: (event) => { }, // 点击行
            dblclick: () => {
              this.gotoDevelop(item)

            },
            // contextmenu: (event) => { },
            // mouseenter: (event) => { }, // 鼠标移入行
            // mouseleave: (event) => { }
          },
        };
      },
      gotoDevelop (item) {
        this.closeDrawer()
        this.$router.push({
          name: 'JobDevelop',
          query: {//预留query
            projectId: Number(this.$route.query.projectId),
            projectName: this.$route.query.projectName,
            fileId: item.fileId,
            folderId: item.folderId
          }
        })
      },
      searchSelect (item) {
        this.params.vo.folderId = item.id
      },
      async clickButton (value, record, useLatest) {
        this.selectRecord = record
        this.clickType = value
        if (this.clickType !== 'run') {
          this.popConfirm = true
        }

        this.useLatest = useLatest
        switch (value) {
          case 'delete':
            this.confirmType = 'warning'
            this.callback = () => {
              this.operateItem('下线', 'delete', record)
            }
            break
          case 'run':
            // this.confirmType = 'normal'
            this.$refs.startDialog.open(this.selectRecord)
            // this.callback = () => {
            //   this.operateItem('启动', 'run', record)
            // }
            break
          case 'templateStop':
            this.confirmType = 'warning'
            this.callback = () => {
              this.operateItem('暂停', 'templateStop', record)
            }
            break
          case 'recovery':
            this.callback = async () => {
              this.isShouldClose = true
              this.operateItem('恢复', 'recovery', record, useLatest)
            }
            break
          case 'resourceValidate':
            this.confirmType = 'normal'
            this.isResourceValidating = true
            var res = await this.isResourceGood()//点恢复后先判断资源足不足
            this.isResourceValidating = false
            if (res === 1) {//开关未打开
              this.clickButton('recovery', record, useLatest)
            }
            else if (res === 2) {//开关打开了且资源足
              if (useLatest) {//有新版本

                this.callback = async () => {
                  this.isShouldClose = false//不需要关闭弹窗，切换窗口内容
                  this.clickButton('recovery', record, useLatest)
                  setTimeout(() => {
                    this.isShouldClose = true
                  }, 200)

                }
              }
              else {//没有新版本
                this.confirmDisabled = false
                this.callback = async () => {

                  this.operateItem('恢复', 'recovery', record, false)
                }
              }
            }
            else if (res === 3) {//资源不足
              this.confirmDisabled = true
              this.callback = () => {
              }
              return
            }



            break
          case 'stop':
            this.confirmType = 'warning'
            this.callback = () => {
              this.operateItem('停止', 'stop', record)
            }
        }


      },
      async isResourceGood () {//先获取开关配置，再判断资源是否不足,返回值1:开关未打开，2:开关打开了并且资源足，3:开关打开了资源不足
        var setting = await this.getSetting()//获取设置，资源验证开关是否打开
        if (setting.code === 0 && setting.data) {
          if (setting.data.resourceValidate) {//需要验证
            var res = await this.resourceValidate()//资源验证
            if (res.code === 0) {
              this.resourceData = res.data
              if (this.resourceData) {
                if (!this.resourceData.success) {//资源不足
                  return 3
                } else {
                  return 2//资源足
                }
              }
            } else {
              res.msg && this.$message.error({ content: res.msg, duration: 2 })
            }
          } else {//开关未打开
            return 1
          }

        } else if (setting.code !== 0) {
          setting.msg && this.$message.error({ content: setting.msg, duration: 2 })
        }

      },
      async resourceValidate () {//判断资源是否不足
        this.resourceData = null
        const params = {
          id: this.selectRecord.id
        }
        let res = await this.$http.post(`/setting/engineSetting/resourceValidate`, params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })

        return res
      },
      async getSetting () {
        let res = await this.$http.get('/sysconfig/getSysoper')
        return res

      },
      showMore (record) {
        this.tableData = this.tableData.map(item => {
          item.isShow = false
          return item
        })
        record.isShow = true
        var hide = function () {
          record.isShow = false
        }
        document.removeEventListener('click', hide)
        document.addEventListener('click', hide)
      },
      popSavePointConfirm (record) {
        this.selectRecord = record
        this.savePointConfirm = true
        var timeToDate = () => {
          var processDates = (date) => {
            return date > 9 ? date : `0${date}`
          }
          const date = new Date()
          let Year = date.getFullYear()
          let month = processDates(date.getMonth() + 1)
          let day = processDates(date.getDate())
          let hour = processDates(date.getHours())
          let minute = processDates(date.getMinutes())
          let second = processDates(date.getSeconds())
          return `${Year}${month}${day}${hour}${minute}${second}`
        }
        this.savePointValue = 'savepoint_' + timeToDate()
        this.$nextTick(() => {
          document.querySelector('.save-point-input .input').focus()
        })
      },
      async savePoint () {
        const params = [{
          vo: {
            id: this.selectRecord.id
          },
          savepointName: this.savePointValue
        }]
        let res = await this.$http.post('/job/triggerSavepoint', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.$message.success({ content: '添加成功', duration: 2 })
          this.savePointConfirm = false
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      async handerLevelChange () {
        this.popConfirm = true
        this.clickType = 'priority'
        const priorityArguments = arguments
        this.selectRecord = arguments[0]
        this.callback = async function () {

          const params = {
            id: priorityArguments[0].id,
            priority: priorityArguments[1][0],
            updateRule: this.updateRule
          }
          let res = await this.$http.post('/job/setPriorityLevel', params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res && res.code === 0) {
            this.selectRecord.oldPriority = this.selectRecord.priority
            this.$message.success('修改成功')
          } else {
            this.selectRecord.priority = this.selectRecord.oldPriority
          }
        }.bind(this)

      },
      getConfirmText () {
        if (this.clickType === 'priority') {
          return '更新'
        } else {
          return '确认'
        }
      },
      getCancelText () {
        if (this.clickType === 'priority') {
          return '不更新'
        } else {
          return '取消'
        }
      },
      dialogCancel () {
        if (this.clickType === 'priority') {
          this.updateRule = false
          this.callback()
        }
      },
      dialogConfirm () {
        this.updateRule = true
        this.callback()
      },
      dialogClose () {
        if (this.isShouldClose) {
          this.popConfirm = false
          this.clickType = ''
          this.confirmDisabled = false
        }


      },
      getClosable () {
        if (this.clickType === 'priority') {
          return true
        }
        return false
      },
      openPriortyDrawer () {
        this.priorityDrawerVisible = true
      }
    },
    beforeDestroy () {
      this.clearTimer()
    }
  }
</script>
<style lang="scss" scoped>
  .operation-disabled {
    color: #999 !important;
    cursor: not-allowed !important;
  }
  .word-break {
    word-break: break-all;
  }
  .warn-message {
    color: #faad14;
  }
  .save-point-input {
    p {
      font-size: 12px;
      margin-bottom: 4px;
      color: #333;
    }
    input {
      height: 28px;
      background: #ffffff;
      border: 1px solid #d7d7db;
      border-radius: 2px;
    }
  }
  /deep/ .clickRowStyle {
    background-color: #f7f0ff;
  }
  .rowStyleNone {
    background-color: #fff;
  }
  .job-operate {
    width: 100%;
    height: 100%;
    color: #333;
    .search-container {
      padding-bottom: 8px;
      background: #eff1f6;
      .search-main {
        height: 56px;
        padding: 0 16px;
        border-bottom: 1px solid #dee2ea;
        box-sizing: border-box;
        background: #ffffff;
        .right-content {
          .product-line {
            margin-right: 16px;
            p {
              margin: 0 8px 0 20px;
            }
          }
          .product-line {
            /deep/ .ant-select-selection--single {
              height: 28px !important;
              .ant-select-selection__rendered {
                line-height: 28px !important;
              }
            }
          }
        }
      }
    }
    .job-operate-top {
      height: 40px;
      line-height: 40px;
      border-bottom: 1px solid #d9d9d9;
      padding-left: 16px;
      border-bottom: 1px solid #d9d9d9;
      overflow: hidden;
      display: flex;
      justify-content: space-between;
      .job-operate-top-left {
        display: inline-block;
        font-size: 16px;
        font-weight: bold;
        i {
          margin-right: 8px;
          font-size: 16px !important;
        }
      }
      .job-operate-top-content {
        display: inline-block;
        margin-left: 136px;
        span {
          font-weight: 600;
          display: inline-block;
          margin-right: 16px;
          font-size: 12px;
          cursor: pointer;
          i {
            margin-right: 6px;
            font-size: 11px;
          }
        }
        span:hover {
          color: #0066ff;
        }
      }
      .updated-by {
        span {
          margin-right: 4px;
        }
        /deep/ .ant-select {
          height: 28px;
          margin-right: 20px;
          .ant-select-selection--single {
            height: 100%;
            .ant-select-selection__rendered {
              line-height: 26px;
            }
          }
        }
      }
      .job-operate-top-right {
        display: flex;
        justify-content: space-between;
        margin-right: 20px;
        .button-restyle {
          margin-left: 8px;
          border-radius: 2px;
          height: 28px;
          width: 72px;
          min-width: 72px;
          font-size: 12px;
        }
      }
    }
    .job-operate-content {
      // height: calc(100% - 40px - 72px);
      .table-data {
        padding: 16px;
        // height: 100%;
        /deep/ .ant-spin-nested-loading {
          height: 100%;
          .ant-spin-container {
            height: 100%;
            .ant-table {
              height: 100%;
              .ant-table-content {
                height: 100%;
                .ant-table-scroll {
                  overflow: hidden;
                  height: 100%;
                  .ant-table-body {
                    height: 100%;
                    tr td {
                      padding: 10px 16px !important;
                      font-size: 14px;
                      overflow: hidden;
                      &:last-of-type {
                        overflow: visible;
                      }
                    }
                  }
                }
                //固定列情况下显示更多
                .ant-table-fixed-right
                  > .ant-table-body-outer
                  > .ant-table-body-inner
                  > table
                  > .ant-table-tbody {
                  tr td {
                    overflow: visible;
                  }
                }
              }
            }
          }
        }
        .jobName {
          color: #0066ff;
          cursor: pointer;
          display: flex;
          align-items: center;

          .sql {
            width: 36px;
            height: 16px;
            padding: 0 5px;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 12px;
            border: 1px solid #3586ff;
            background-color: #e3eeff;
            border-radius: 2px;
          }
          .title {
            margin-left: 8px;
            font-size: 12px;
            width: calc(100% - 48px);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        .version {
          display: flex;
          align-items: center;
          span {
            width: 25px;
          }
          .topic {
            cursor: pointer;
            color: #0066ff;
            margin-left: 5px;
            width: 28px;
            min-width: 28px;
            height: 14px;
            min-height: 14px;
            font-size: 12px;
            display: flex;
            align-items: center;
            img {
              width: 100%;
              height: 100%;
            }
            .arrow {
              background: #e8dffc;
              width: 6px;
              height: 6px;
              margin-left: -3px;
              transform: rotate(45deg);
            }
            .info {
              width: 75px;
              text-align: center;
            }
          }
        }
        .level {
          color: red;
          width: 60px;
          .level-select {
            width: 60px;
            height: 28px;
            /deep/ .ant-select-selection--single {
              height: 28px;
              .ant-select-selection__rendered {
                line-height: 24px;
              }
            }
          }
        }
        .jobStatus {
          display: flex;
          div {
            width: 72px;
            height: 20px;
            font-size: 12px;
            text-align: center;
            color: #fff;
          }
          .process {
            display: flex;
            align-items: center;
            text-align: left;
            width: 84px;
            i {
              color: #333;
            }
            div {
              width: 72px;
              // background-color: #52C41A;
            }
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
            background-color: #eef0f4;
            display: flex;
            justify-content: center;
            align-items: center;
            color: #93a1bb;
            border-radius: 2px;
          }
          // .RUNNING {
          //   background-color: #dfecff;
          //   display: flex;
          //   justify-content: center;
          //   align-items: center;
          //   color: #0066ff;
          //   border-radius: 2px;
          // }
          .RUNNING {
            background-color: #e2f6de;
            display: flex;
            justify-content: center;
            align-items: center;
            color: #2c2f37;
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
        }
        .operation-item {
          display: inline-block;
          color: #0066ff;
          cursor: pointer;
          margin-right: 12px;
          i {
            font-size: 14px;
            margin-right: 7px;
          }
          span {
            font-size: 12px;
          }
          /deep/ .ant-popover-inner-content {
            width: 90px;
            text-align: center;
            padding: 5px;
          }
        }
        .stop-btn {
          color: #f95353;
        }

        .more {
          position: relative;
          font-size: 12px;
        }

        /deep/ .ant-table-thead > tr > th {
          padding: 10.5px 16px;
          font-weight: 700;
          font-size: 12px;
          .anticon-filter {
            left: 0 !important;
          }
          &:nth-child(4),
          &:nth-child(5) {
            span {
              margin-left: 10px;
            }
          }
        }
        /deep/ .ant-table-tbody > tr > td {
          padding: 0 16px;
        }
      }
    }
    .job-operate-footer {
      height: 72px;
      padding: 20px 16px;
    }
  }
  .job-version-slot {
    color: #0066ff;
    cursor: pointer;
  }
  .more-list {
    li {
      height: 28px;
      line-height: 28px;
      font-size: 12px;
      color: #333333;
      cursor: pointer;
      &:hover {
        color: #0066ff;
      }
    }
  }
</style>
