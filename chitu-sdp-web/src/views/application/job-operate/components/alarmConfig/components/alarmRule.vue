<!--
 * @Author: hjg
 * @Date: 2021-11-08 22:14:15
 * @LastEditTime: 2022-07-20 15:38:14
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\alarmConfig\components\alarmRule.vue
-->
<template>
  <div class="alarm-rule">
    <a-table class="tale-data"
             v-loading="isLoading"
             bordered
             v-defaultPage="!tableData || (tableData && tableData.length === 0)"
             row-key="id"
             ref="alarmRuleTable"
             :columns="columns"
             :data-source="tableData"
             :loading="loading"
             :pagination="false"
             :scroll="{y: 'calc(100% - 55px)'}"
             @change="handleTableChange">
      <!-- 规则名称 -->
      <div slot="ruleName"
           class="rule-name"
           :title="record.ruleName"
           slot-scope="text,record">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span>{{record.ruleName}}</span>
          </template>
          <span>{{ record.ruleName }}</span>
        </a-tooltip>
      </div>
      <!-- 指标 -->
      <div slot="indexName"
           class="index-name"
           :title="record.indexName | filterIndex"
           slot-scope="text,record">{{ record.indexName | filterIndex }}</div>
      <!-- 规则状态 -->
      <div slot="effectiveState"
           :class="[record.effectiveState=='STOP'?'':'running']"
           slot-scope="text,record">{{ record.effectiveState | filterRuleState}}</div>
      <!-- 规则描述 -->
      <div slot="ruleDesc"
           class="rule-desc"
           :title="record.ruleDesc"
           slot-scope="text,record">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <span>{{record.ruleDesc}}</span>
          </template>
          <span>{{ record.ruleDesc }}</span>
        </a-tooltip>
      </div>
      <!-- 规则类型 -->
      <div slot="ruleGenerateType"
           slot-scope="text,record">{{ record.ruleGenerateType | filterRuleType }}</div>
      <!-- 操作 -->
      <div class="operation"
           slot="operation"
           slot-scope="text,record">
        <!-- 启动 -->
        <div v-if="record.effectiveState === 'STOP'"
             class="operate-item"
             @click="operateItem('run', record)">
          <i class="chitutree-h5 chitutreeqidong"></i>
          <span>启动</span>
        </div>
        <!-- 停止 -->
        <div v-if="record.effectiveState === 'START'"
             class="operate-item">
          <a-popconfirm @confirm="() => operateItem('stop', record)">
            <template slot="title">
              <h3>注意：确定要停止规则？</h3>
            </template>
            <i class="chitutree-h5 chitutreetingzhi"></i>
            <span>停止</span>
          </a-popconfirm>
        </div>
        <!-- 编辑 -->
        <div class="operate-item"
             @click="operateItem('edit', record)">
          <i class="chitutree-h5 chitutreebianji"></i>
          <span>编辑</span>
        </div>
        <!-- 删除 -->
        <div v-if="record.effectiveState === 'STOP' && record.ruleGenerateType !== 'SYSTEM_AUTOMATIC'"
             class="operate-item">
          <a-popconfirm @confirm="() => operateItem('delete', record)">
            <template slot="title">
              <h3>注意：确定要删除规则？</h3>
            </template>
            <i class="chitutree-h5 chitutreeshanchu"></i>
            <span>删除</span>
          </a-popconfirm>
        </div>
      </div>
    </a-table>
    <!-- 分页 -->
    <div class="footer-page">
      <Pagination :pagination="pagination"
                  @pageChange="pageChange"
                  @pageSizeChange="pageSizeChange" />
    </div>
  </div>
</template>
<script>
  import Pagination from '@/components/pagination/index'
  import { mapActions } from 'vuex'
  // import tableHeaderDrag from '../../../../../../mixins/table-header-drag'
  import tableSort from '../../../../../../mixins/table-sort'
  export default {
    name: 'alarmRule',
    mixins: [tableSort],
    components: {
      Pagination
    },
    props: {
      activeName: {
        type: String,
        value: ''
      }
    },
    filters: {
      filterIndex (value) { // 指标过滤
        let text = '重启次数'
        if (value === 'NUMBER_CHECKPOINT') {
          text = 'checkpoint次数'
        } else if (value === 'DELAY') {
          text = '延迟'
        } else if (value === 'INTERRUPT_OPERATION') {
          text = '作业运行中断'
        } else if (value === 'KAFKA_FAIL_MSG') {
          text = 'topic数据解析失败'
        } else if (value === 'WAIT_RECORDS') {
          text = '待消费数'
        } else if (value === 'BACKPRESSURED') {
          text = '背压'
        } else if (value === 'CONSUME_RECORDS') {
          text = '最近N小时消费数'
        }
        return text
      },
      filterRuleState (value) { // 规则状态过滤
        let text = '已启动'
        if (value === 'STOP') {
          text = '已停止'
        }
        return text
      },
      filterRuleType (value) { // 规则类型过滤
        let text = '系统自动'
        if (value === 'CUSTOMIZE') {
          text = '自定义'
        }
        return text
      }
    },
    computed: {
      columns () {
        return [{
          title: '规则名称',
          dataIndex: 'ruleName',
          scopedSlots: { customRender: 'ruleName' },
          width: 86
        },
        {
          title: '指标',
          dataIndex: 'indexName',
          scopedSlots: { customRender: 'indexName' },
          width: 172
        },
        {
          title: '规则状态',
          dataIndex: 'effectiveState ',
          scopedSlots: { customRender: 'effectiveState' },
          width: 86
        },
        {
          title: '规则描述',
          dataIndex: 'ruleDesc',
          scopedSlots: { customRender: 'ruleDesc' },
          width: 86
        },
        {
          title: '规则类型',
          dataIndex: 'ruleGenerateType',
          scopedSlots: { customRender: 'ruleGenerateType' },
          width: 86
        },
        {
          title: '创建时间',
          dataIndex: 'creationDate',
          defaultSortOrder: 'descend',
          sorter: () => this.handleTableChange,
          scopedSlots: { customRender: 'creationDate' },
          width: 172,
          sortOrder: this.sortedInfo.columnKey === 'creationDate' && this.sortedInfo.order
        },
        {
          title: '操作',
          dataIndex: 'operation',
          scopedSlots: { customRender: 'operation' },
          width: 168
        }]
      }
    },
    data () {
      return {
        isLoading: false,
        headerDragData: {
          columnsName: 'columns',
          ref: 'alarmRuleTable'
        },
        tableSortData: {
          columnsName: 'columns',
          ref: 'alarmRuleTable'
        },
        tableData: [],
        loading: false,
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0,
          pageSizeOptions: ['10', '20', '40', '60']
        },
        params: {
          jobId: null,
          orderByClauses: [{
            field: "creation_date", //排序键名
            orderByMode: 1 //排序模式（0：正序，1：倒序）
          }],
          page: 1,
          pageSize: 20
        },
      }
    },
    watch: {
      activeName: {
        async handler (val) {
          if (val === 'alarmRule') {
            this.init()
          }
        },
        immediate: true
      }
    },
    created () {

    },
    methods: {
      ...mapActions('rule', {
        'ruleInfo': 'ruleInfo'
      }),
      init () {
        this.getRulesList(this.params)
      },
      // 排序，筛选变化时触发
      handleTableChange (pagination, filters, sorter) {
        // console.log('------handleTableChange:', pagination, filters, sorter)
        this.resetSortMethods(sorter)
        sorter = this.sortedInfo
        if (sorter.order) {
          if (sorter.order === 'ascend') {
            this.params.orderByClauses[0].orderByMode = 0
          } else {
            this.params.orderByClauses[0].orderByMode = 1
          }
        }
        this.getRulesList(this.params)
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.params.page = pageInfo.page
        this.getRulesList(this.params)
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
        this.params.page = pageSizeInfo.current
        this.params.pageSize = pageSizeInfo.size
        this.getRulesList(this.params)
      },
      // 操作
      operateItem (type, ruleInfo) {
        if (type === 'run') {
          // console.log(type, ruleInfo)
          this.startRule(ruleInfo)
        } else if (type === 'edit') {
          // // console.log(this.$store)
          this.$store.dispatch('rule/setRuleInfo', ruleInfo)
          // this.$store.dispatch('job/setJobInfo', jobInfo)
          this.$emit('editEvent')
        } else if (type === 'stop') {
          // console.log(type, ruleInfo)
          this.stopRule(ruleInfo)
        } else if (type === 'delete') {
          // console.log(type, ruleInfo)
          this.deleteRule(ruleInfo)
        }
      },
      // 启动规则
      async startRule (ruleInfo) {
        let params = ruleInfo
        params.ruleContent = JSON.parse(params.ruleContent)
        params.effectiveState = 'START'
        let res = await this.$http.post('alert/rule/update', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.$message.success({ content: '启动成功', duration: 2 })
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
        this.getRulesList(this.params)
      },
      // 停止规则
      async stopRule (ruleInfo) {
        let params = ruleInfo
        params.ruleContent = JSON.parse(params.ruleContent)
        params.effectiveState = 'STOP'
        let res = await this.$http.post('alert/rule/update', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.$message.success({ content: '停止成功', duration: 2 })
        } else {
          this.$message.success({ content: res.msg, duration: 2 })
        }
        this.getRulesList(this.params)
      },
      // 删除规则
      async deleteRule (ruleInfo) {
        let params = ruleInfo
        params.ruleContent = JSON.parse(params.ruleContent)
        let res = await this.$http.post('alert/rule/delete', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.$message.success({ content: '删除成功', duration: 2 })
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
        this.getRulesList(this.params)
      },
      // 获取规则列表
      async getRulesList (params) {
        params.jobId = this.$store.getters.jobInfo.id
        this.isLoading = true
        let res = await this.$http.post('alert/rule/list', params, {
          headers: {
            projectId: this.$store.getters.jobInfo.projectId
          }
        })
        // console.log('getRulesList-res: ', res)
        this.isLoading = false
        if (res.code === 0) {
          this.resetPagination(res.data)
          this.tableData = res.data.list
        } else {
          this.$message.error({ content: res.msg, duration: 2 })
        }
      },
      // 重置分页信息
      resetPagination (pageInfo) {
        this.pagination.current = pageInfo.pageNum
        this.pagination.total = pageInfo.total
      }
    }
  }
</script>
<style lang="scss" scoped>
  .alarm-rule {
    width: 100%;
    height: 100%;
    .tale-data {
      height: calc(100% - 72px);
      /deep/ .ant-table-thead > tr > th {
        padding: 12px 16px;
        font-weight: 700;
        font-size: 12px;
        border-right: 1px solid #e8e8e8 !important;
      }
      /deep/ .ant-table-tbody > tr > td {
        padding: 9px 7px;
      }
      /deep/ .ant-table-placeholder {
        visibility: hidden;
      }
      .operation {
        display: flex;
        .operate-item {
          color: #0066ff;
          font-size: 12px;
          cursor: pointer;
          margin-right: 12px;
          i {
            margin-right: 4px;
          }
        }
      }
    }
    /deep/ .ant-table-wrapper {
      height: calc(100% - 72px);
      .ant-spin-nested-loading {
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
                }
              }
            }
          }
        }
      }
    }
    .rule-name,
    .rule-desc {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .running {
      color: #33cc22;
    }
    .footer-page {
      height: 72px;
      padding: 20px 16px;
    }
  }
</style>
