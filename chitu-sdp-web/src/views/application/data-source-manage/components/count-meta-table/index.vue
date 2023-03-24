
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
                   :data-source="dataList"
                   :autoHight="false"
                   rowKey="id"
                   :scroll="{y: 300}"
                   :pagination="pagination"
                   @pageChange="pageChange"
                   @pageSizeChange="pageSizeChange">
          <!-- 作业名称 -->
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

          <!-- 表名 -->
          <template #tableName="{text,record}">
            <div>
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>{{record.tableName}}</span>
                </template>
                <div class="tableName">{{ record.tableName }}</div>
              </a-tooltip>
            </div>
          </template>

          <!-- 元表名称 -->
          <template #metaTableName="{text,record}">
            <div>
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>{{record.metaTableName}}</span>
                </template>
                <div class="metaTableName">{{ record.metaTableName }}</div>
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
      dataIndex: 'jobName',
      key: 'jobName',
      title: '作业名称',
      scopedSlots: { customRender: 'jobName' },
      width: 200
    },
    {
      dataIndex: 'datasourceType',
      key: 'datasourceType',
      title: '数据源类型',
      scopedSlots: { customRender: 'datasourceType' },
      width: 120
    },
    {
      dataIndex: 'tableName',
      key: 'tableName',
      title: '表名',
      scopedSlots: { customRender: 'tableName' },
      width: 150
    },
    {
      dataIndex: 'metaTableName',
      key: 'metaTableName',
      title: '元表名称',
      scopedSlots: { customRender: 'metaTableName' },
      width: 150
    },
    {
      dataIndex: 'metaTableType',
      key: 'metaTableType',
      title: '元表类型',
      scopedSlots: { customRender: 'metaTableType' },
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
        id: '',
        title: '',
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
        this.title = record.dataSourceName + '的引用元表清单'
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
        let res = await this.$http.post(`/meta/table/select`, params, {
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
