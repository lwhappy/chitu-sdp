
<template>
  <div v-if="isPop"
       class="count-job-dialog">
    <confirm-dialog2 :visible="isPop"
                     :isShowFooter="false"
                     :title="title"
                     :width="900"
                     @close="isPop=false">
      <div class="content">
        <chitu-table v-loading="isLoading"
                   :columns="columns"
                   :autoHight="false"
                   :pagination="pagination"
                   @pageChange="pageChange"
                   @pageSizeChange="pageSizeChange"
                   :data-source="dataList"
                   rowKey="id"
                   :scroll="{y: 300}">
          <template #jobName="{text,record}">
            <div>
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>{{record.jobName}}</span>
                </template>
                <div class="jobName blue"
                     @click="gotoDevelop(record)">{{ record.jobName }}</div>
              </a-tooltip>
            </div>
          </template>
        </chitu-table>
      </div>
    </confirm-dialog2>
  </div>
</template>
<script>
  import ConfirmDialog2 from '@/components/confirm-dialog/index2'

  const columns = [
    {
      dataIndex: 'projectName',
      key: 'projectName',
      title: '项目名称',
      scopedSlots: { customRender: 'projectName' },
      width: 150
    },
    {
      dataIndex: 'jobName',
      key: 'jobName',
      title: '作业名称',
      scopedSlots: { customRender: 'jobName' },
      width: 200
    },
    {
      dataIndex: 'updatedBy',
      key: 'updatedBy',
      title: '更新人',
      scopedSlots: { customRender: 'updatedBy' },
      width: 150
    },
    {
      dataIndex: 'updationDate',
      key: 'updationDate',
      title: '更新时间',
      scopedSlots: { customRender: 'updationDate' },
      width: 150
    },
  ]
  export default {
    name: 'countJob',
    components: {
      ConfirmDialog2,
    },
    props: {

    },
    data () {
      return {
        title: '',
        id: '',
        columns,
        isLoading: false,
        isPop: false,
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 10,
          total: 0
        },
        dataList: [],
        params: {}
      }
    },
    created () {

    },
    watch: {

    },
    methods: {
      gotoDevelop (item) {
        this.isPop = false
        this.$router.push({
          name: 'JobDevelop',
          query: {//预留query
            projectId: this.$route.query.projectId,
            projectName: this.$route.query.projectName,
            fileId: item.fileId,
            folderId: item.folderId
          }
        })
      },
      // 分页数据变化
      pageChange (pageInfo) {
        // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
        this.pagination.current = pageInfo.page
        this.getData()
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        this.pagination.defaultPageSize = pageSizeInfo.size
        this.pagination.current = 1
        this.getData()
      },
      open (record, params) {
        this.isPop = true
        this.id = record.id
        this.params = params
        this.title = record.dataSourceName + '的引用作业清单'
        this.pagination.current = 1
        this.pagination.total = 0
        this.dataList = []
        this.getData()
      },
      async getData () {
        const params = {
          id: this.id,
          page: this.pagination.current,
          pageSize: this.pagination.defaultPageSize,
          jobName: this.params.jobName,
          tableName: this.params.tableName,
          metaTableName: this.params.metaTableName
        }
        this.isLoading = true
        let res = await this.$http.post(`/job/select`, params, {
          headers: {
            projectId: this.$route.query.projectId
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          this.dataList = res.data.rows
          this.pagination.total = res.data.rowTotal
        }
      },
    }
  }
</script>
<style lang="scss" scoped>
  .content {
    margin-top: -28px;
    .jobName {
      cursor: pointer;
    }
  }
</style>
