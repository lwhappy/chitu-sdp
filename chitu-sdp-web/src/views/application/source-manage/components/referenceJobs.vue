<template>
  <!-- 一定要用v-if="isShow"摧毁dom重新渲染，不然会有莫名其妙的问题 -->
  <a-modal v-if="isShow"
           wrapClassName="reference-jobs-dialog"
           v-model="isShow"
           :mask-closable="false"
           :footer="null"
           :title="title">
    <a-table :columns="columns"
             :pagination="false"
             :data-source="dataList"
             rowKey="id"
             :scroll="{y: '400px'}">
      <span class="jobName"
            slot="jobName"
            slot-scope="text,record">
        {{record.jobName}}
      </span>
    </a-table>
    <!-- 分页 -->
    <div class="footer-page">
      <pagination :pagination="pagination"
                  @pageChange="pageChange"
                  @pageSizeChange="pageSizeChange" />
    </div>
  </a-modal>
</template>

<script>
  import Pagination from '@/components/pagination/index'

  const columns = [
    {
      dataIndex: 'jobName',
      key: 'jobName',
      title: '作业名称',
      scopedSlots: { customRender: 'jobName' },
      width: '10%'
    }
  ]
  export default {
    components: { Pagination },
    data () {
      return {
        isShow: false,
        title: '引用的作业',
        name: '',
        columns,
        page: 1,
        pagination: {
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0
        },
        dataList: []
      }
    },
    props: {

    },
    computed: {

    },
    watch: {
      isShow: {
        handler () {

        }
      }
    },
    created () {
    },
    methods: {
      open (data) {
        this.isShow = true
        this.name = data.name
        this.version = data.version
        this.referenceJobs()
      },
      close () {
        this.isShow = false
      },
      async referenceJobs () {
        this.dataList = []
        const params = {
          name: this.name,
          projectId: Number(this.$route.query.projectId),
          version: this.version
        }
        let res = await this.$http.post('/jar/referenceJobs', params)
        if (res.code === 0) {
          if (res.data) {
            this.dataList = res.data.list
            this.pagination.total = res.data.total
          }
        } else {
          this.$message.error(res.msg);
        }
      },
      // 分页数据变化
      pageChange (pageInfo) {
        this.page = pageInfo.page
        this.referenceJobs()
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        this.pagination.defaultPageSize = pageSizeInfo.size
        this.page = 1
        this.referenceJobs()
      },
    },
    mounted () {

    }
  }
</script>
<style lang="scss">
  .reference-jobs-dialog {
    .ant-modal {
      width: 500px !important;
      .ant-modal-close-x {
        width: 44px;
        height: 44px;
        line-height: 44px;
      }
      .ant-modal-header {
        //弹窗头部
        height: 44px;
        padding: 0;
        .ant-modal-title {
          line-height: 44px;
          padding-left: 16px;
          font-size: 16px;
          font-weight: 600;
        }
      }
    }
  }
</style>
<style lang="scss" scoped>
  .footer-page {
    height: 72px;
    padding: 20px 16px;
  }
</style>