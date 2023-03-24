<template>
  <div class="apply-container">
    <div class="search-wrapper justify-start">
      <div class="date-wrapper justify-start">
        <p class="label">申请时间：</p>
        <a-range-picker v-model="pickDate"
                        @change="onChangeDate" />
      </div>
      <div class="status-wrapper justify-start">
        <p class="label">审批状态：</p>
        <a-select placeholder="全部"
                  mode="multiple"
                  v-model="status"
                  class="select-status">
          <a-select-option v-for="item in statusList"
                           :key="item.key"
                           :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </div>
      <a-button @click="getList"
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
    <div class="divider-line"></div>
    <div class="data-list">
      <div class="sub-list">
        <chitu-table v-loading="isLoading"
                   :columns="columns"
                   :dataSource="dataList"
                   rowKey="id"
                   @change="handleChange"
                   :tableOpt="{customRow:customRow}"
                   :pagination="pagination"
                   @pageChange="pageChange"
                   @pageSizeChange="pageSizeChange">
          <template #projectName="{record}">
            <span class="name">
              {{record.projectName}}
            </span>
          </template>
          <template #jobName="{record}">
            <a-popover v-if="record.jobName">
              <template slot="content">
                <div style="word-break: break-all;">
                  {{record.jobName}}
                </div>
              </template>
              <span class="jobName blue"
                    @click.stop="gotoJob(record)">
                {{record.jobName}}
              </span>
            </a-popover>
          </template>
          <template #description="{record}">
            <span class="description">
              <a-popover v-if="record.description">
                <template slot="content">
                  <div v-html="record.description"
                       style="word-break: break-all;">
                  </div>
                </template>
                <span>{{record.description}}</span>
              </a-popover>
              <template v-else>{{$common.empty(record.description)}}</template>
            </span>
          </template>
          <template #status="{record}">
            <span class="status"
                  :class="record.status">
              <a-popover v-if="record.opinion">
                <template slot="content">
                  <div v-html="record.opinion"
                       style="max-width:80%;word-break: break-all;">
                  </div>
                </template>
                <span>
                  {{statusText(record)}}
                </span>
              </a-popover>
              <span v-else>
                {{statusText(record)}}
              </span>
            </span>
          </template>
          <template #creationDate="{record}">
            <span class="date">
              {{record.creationDate}}
            </span>
          </template>
          <template #approver="{record}">
            <span class="approver">
              <span v-if="approver(record)">
                <a-popover placement="topLeft">
                  <template slot="content">
                    <div v-html="approver(record)">
                    </div>
                  </template>
                  <span>
                    {{approver(record)}}
                  </span>
                </a-popover>
              </span>

            </span>
          </template>
          <template #updationDate="{record}">
            <span class="date">
              {{approveDate(record)}}

            </span>
          </template>
          <template #operate="{record}">
            <div class="common-action-container ">
              <!-- 待审批和审批中可以撤销 -->
              <a-button type="link"
                        :disabled="record.status === 'PENDING' || record.status === 'APPROVING'?false:true"
                        @click.stop="recall(record)"><i class="chitutree-h5 chitutreechexiao1"></i>撤销</a-button>
            </div>
          </template>
        </chitu-table>
      </div>
    </div>
  </div>
</template>
<script>
  const columns = [
    {
      dataIndex: 'index',
      key: 'index',
      title: '序号',
      width: 50,
    },
    {
      dataIndex: 'projectName',
      key: 'projectName',
      title: '项目名称',
      scopedSlots: { customRender: 'projectName' },
      width: 150,
      // defaultSortOrder: 'descend',
      sortDirections: ['ascend', 'descend', 'ascend'],
      sorter: (a, b) => a.projectName - b.projectName
    },
    {
      dataIndex: 'jobName',
      key: 'jobName',
      title: '作业名称',
      scopedSlots: { customRender: 'jobName' },
      width: 150,
      // defaultSortOrder: 'descend',
      sortDirections: ['ascend', 'descend', 'ascend'],
      sorter: (a, b) => a.jobName - b.jobName
    },
    {
      dataIndex: 'description',
      key: 'description',
      title: '描述',
      scopedSlots: { customRender: 'description' },
      width: 150,
      // defaultSortOrder: 'descend',
      // sorter: (a, b) => a.description - b.description
    },
    {
      dataIndex: 'status',
      key: 'status',
      title: '审批状态',
      scopedSlots: { customRender: 'status' },
      width: 100,
      // defaultSortOrder: 'descend',
      sortDirections: ['ascend', 'descend', 'ascend'],
      sorter: (a, b) => a.status - b.status
    },
    {
      title: '申请时间',
      dataIndex: 'creationDate',
      key: 'creationDate',
      scopedSlots: { customRender: 'creationDate' },
      width: 150,
      defaultSortOrder: 'descend',
      sortDirections: ['ascend', 'descend', 'ascend'],
      sorter: (a, b) => a.creationDate - b.creationDate
    },
    {
      title: '审批人',
      key: 'approver',
      dataIndex: 'approver',
      scopedSlots: { customRender: 'approver' },
      width: 200,
      // defaultSortOrder: 'descend',
      sortDirections: ['ascend', 'descend', 'ascend'],
      sorter: (a, b) => a.approver - b.approver
    },
    {
      title: '审批时间',
      dataIndex: 'updationDate',
      key: 'updationDate',
      scopedSlots: { customRender: 'updationDate' },
      width: 150,
      // defaultSortOrder: 'descend',
      sortDirections: ['ascend', 'descend', 'ascend'],
      sorter: (a, b) => a.updationDate - b.updationDate
    },
    {
      title: '操作',
      key: 'operate',
      dataIndex: 'operate',
      fixed: 'right',
      scopedSlots: { customRender: 'operate' },
      width: 100
    },
  ];



  export default {
    name: 'Apply',
    data () {
      return {
        pickDate: null,
        isLoading: false,
        status: [],
        statusList: [
          // { key: 'all', value: '', label: '全部' },
          { key: 'AGREE', value: 'AGREE', label: '同意' },
          { key: 'DISAGREE', value: 'DISAGREE', label: '不同意' },
          { key: 'PENDING', value: 'PENDING', label: '待审批' },
          { key: 'CANCEL', value: 'CANCEL', label: '已撤销' },
          { key: 'APPROVING', value: 'APPROVING', label: '审批中' },

        ],
        dataList: [],
        columns,
        order: {
          field: 'creation_date',
          value: 1
        },
        page: 1,
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0
        },
        date: {
          startTime: '',
          endTime: ''
        }
      };
    },
    components: {

    },
    created () {
    },
    mounted () {
      this.getList()
      // setTimeout(() => {
      //   this.getList()
      // }, 5 * 1000)
    },
    methods: {
      onChangeDate (date, dateString) {
        // console.log(date, dateString);
        this.date.startTime = dateString[0]
        this.date.endTime = dateString[1]
      },
      reset () {
        this.pickDate = null
        this.date.startTime = ''
        this.date.endTime = ''
        this.status = []
        this.pagination.current = 1
        this.getList()
      },
      async getList () {
        let userId = sessionStorage.getItem('userId')
        userId = Number(userId)
        const params = {
          orderByClauses: [{
            field: this.order.field, //排序键名
            orderByMode: this.order.value //排序模式（1：正序，0：倒序）
          }],
          page: this.page,
          pageSize: this.pagination.defaultPageSize,
          vo: {
            currentUser: userId,
            type: "apply",
            startTime: this.date.startTime,
            endTime: this.date.endTime,
            statuss: this.status.length ? this.status : null
          }
        }
        this.dataList = []
        this.pagination.total = 0
        this.isLoading = true
        let res = await this.$http.post('/approve/queryApply', params)
        this.isLoading = false
        if (res.code === 0) {
          if (res.data) {
            let dataList = res.data.rows
            dataList = dataList.map((item, index) => {
              item.index = index + 1
              item.description = '申请发布，请审批'
              // try {
              //   item.approver = JSON.parse(item.approver)
              //   item.approver = item.approver.join(',')
              // } catch (e) {
              //   // console.log(e)
              // }
              return item
            })
            this.dataList = dataList
            this.pagination.total = res.data.rowTotal
          }
        } else {
          this.$message.error(res.msg);
        }
      },
      // statusSelect (value, option) {
      //   console.log(value, option)
      //   this.status = value
      // },
      recall (item) {
        var onOk = async function () {
          const params = {
            id: item.id
          }
          let res = await this.$http.post('/approve/cancelApply', params)
          if (res.code === 0) {
            this.$message.success('撤销成功')
            this.getList()
          } else {
            this.$message.error(res.msg);
          }
        }.bind(this)

        this.$confirm({
          title: '确定要撤销吗?',
          content: '',
          okText: '确认',
          cancelText: '取消',
          onOk: onOk
        });
      },
      gotoJob (item) {
        this.$router.push({
          name: 'JobDevelop',
          query: {//预留query
            projectId: item.projectId,
            projectName: encodeURIComponent(item.projectName),
            fileId: item.fileId,
            folderId: item.folderId
          }
        })
      },
      gotoApprove (item) {

        this.$router.push({
          name: 'JobDevelop',
          query: {//预留query
            projectId: item.id,
            projectName: encodeURIComponent(item.projectName)
          }
        })
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.page = pageInfo.page
        this.getList()
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        this.pagination.defaultPageSize = pageSizeInfo.size
        this.page = 1
        this.pagination.current = 1
        this.getList()
      },
      addSuccess () {
        this.page = 1
        this.getList()
      },
      handleChange (pagination, filters, sorter) {
        // console.log(pagination, filters, sorter)
        // // console.log('sorter.order', sorter.order)
        this.order.field = this.$common.toLine(sorter.field)
        if (sorter.order === 'ascend') {
          this.order.value = 0
          this.getList()
        } else if (sorter.order === 'descend') {
          this.order.value = 1
          this.getList()
        }

      },
      statusText (record) {
        let text = ''
        if (record.status === 'AGREE') {
          text = '同意'
        }
        else if (record.status === 'DISAGREE') {
          text = '不同意'
        }
        else if (record.status === 'PENDING') {
          text = '待审批'
        }
        else if (record.status === 'CANCEL') {
          text = '已撤销'
        } else if (record.status === 'APPROVING') {
          text = '审批中'
        }
        return text
      },
      approver (record) {//审批人
        let text = ''
        if (record.status === 'AGREE') {//二级审批完了
          text = record.updatedBy2//显示二级审批人
        }
        else if (record.status === 'DISAGREE') {//不同意有可能是一级审批触发，也有可能二级审批触发
          if (record.updatedBy2) {//有二级审批者
            text = record.updatedBy2//显示二级审批人
          }
          else {
            text = record.updatedBy//显示一级审批人
          }
        }
        else if (record.status === 'PENDING') {//待审批
          text = record.approver//显示一级审批的所有人
        }
        else if (record.status === 'CANCEL') {
          if (record.updatedBy) {//项目管理员提交的审批，一级自动审批，然后点撤销，显示二级审批人，这是的updatedBy为执行撤销的那个人
            text = record.approver2
          } else {
            text = record.approver//显示一级审批的所有人
          }

        } else if (record.status === 'APPROVING') {
          text = record.approver2//显示二级审批的所有人
        }
        return text
      },
      approveDate (record) {//审批时间
        let text = ''
        if (record.status === 'AGREE') {//二级审批完了
          text = record.updationDate2//显示二级审批的时间
        }
        else if (record.status === 'DISAGREE') {
          if (record.updatedBy2) {//有二级审批者
            text = record.updationDate2//显示二级审批的时间
          }
          else {
            text = record.updationDate//显示一级审批的时间
          }
        }
        else if (record.status === 'PENDING') {//待审批
          text = this.$common.empty('')//不显示
        }
        else if (record.status === 'CANCEL') {//已撤销
          text = this.$common.empty('')//不显示
        } else if (record.status === 'APPROVING') {//审批中
          text = record.updationDate//显示一级审批的时间
        }
        return text
      },
      customRow (item) {
        return {
          props: {
          },
          on: { // 事件
            // click: (event) => { }, // 点击行
            click: () => {
              this.$router.push({
                name: 'ApproveDetail',
                query: {//预留query
                  id: item.id,
                  fileId: item.fileId,
                  projectId: item.projectId,
                  type: 'view',
                  fromTab: 'apply'
                }
              })
            },
            // contextmenu: (event) => { },
            // mouseenter: (event) => { }, // 鼠标移入行
            // mouseleave: (event) => { }
          },

        };
      }
    }
  }
</script>

<style lang="scss" scoped>
  .apply-container {
    .date-wrapper {
      margin-right: 16px;
    }
    .status-wrapper {
      margin-right: 16px;
    }
    .select-status {
      min-width: 200px;
      /deep/ .ant-select-selection--single {
        height: 28px;
        line-height: 28px;
      }
    }
    .search-wrapper {
      height: 50px;
      padding: 0 16px;
      .date-wrapper {
        .ant-calendar-picker {
          width: 200px;
          height: 28px;
          /deep/ .ant-input {
            height: 28px;
            line-height: 1;
          }
        }
      }

      .label {
        margin-right: 8px;
        font-size: 12px;
      }
      .btn {
        background: #006eff;
        width: 72px;
        height: 28px;
        border: none;
        cursor: pointer;
        color: #fff;
        font-size: 12px;
        i {
          font-size: 12px;
          margin-right: 7px;
          margin-top: 2px;
        }
      }
    }
    .divider-line {
      width: 100%;
      height: 8px;
      background: #eff1f6;
    }
    .data-list {
      padding: 12px 16px 0 16px;
      height: calc(100% - 52px - 50px);
      .sub-list {
        height: 100%;
        .goto-job {
          margin-right: 22px;
          cursor: pointer;
        }
        /deep/ .jobName {
          cursor: pointer;
        }
        /deep/ .status {
          &.AGREE {
            color: #52c41a;
          }
          &.DISAGREE {
            color: #ff1414;
          }
          &.PENDING {
            color: #333;
          }
          &.APPROVING {
            color: #bb88ff;
          }
          &.CANCEL {
            color: #999999;
          }
        }
      }
    }
  }
</style>