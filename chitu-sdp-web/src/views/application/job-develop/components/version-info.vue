<template>
  <div class="version-info">

    <div class="list">
      <chitu-table v-if="isShow"
                 :columns="columns"
                 :data-source="dataList"
                 rowKey="id"
                 :drag="false"
                 :scroll="{y: '400px'}"
                 :autoHight="false"
                 @change="handleChange"
                 :pagination="pagination"
                 @pageChange="pageChange"
                 @pageSizeChange="pageSizeChange">
        <template #remark="{text,record}">
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>{{record.remark}}</span>
            </template>
            <span>{{record.remark}}</span>
          </a-tooltip>
        </template>
        <template #operate="{text,record}">
          <div class="common-action-container">
            <a-button type="link"
                      @click="compare(record)">对比</a-button>
            <a-button type="link"
                      @click="scrollBack(record)">回滚</a-button>
            <!-- <a-button class="delete justify-start btn blue"
                    @click="removeVersion(record)"
                    :disabled="record.enabledDel === 1?false:true">删除</a-button> -->
          </div>
        </template>

      </chitu-table>
    </div>
    <job-version ref="jobVersion">
      <slot>
        <a-button class="button-restyle button-confirm">
          <div class="job-version-slot"
               @click="scrollBack(selectItem)">回滚</div>
        </a-button>
      </slot>
    </job-version>
  </div>
</template>
<script>
  import jobVersion from '@/components/job-version/index'
  import chituTable from '@/components/chitu-table/index.vue';

  const columns = [
    {
      dataIndex: 'fileVersion',
      key: 'fileVersion',
      title: '版本号',
      scopedSlots: { customRender: 'fileVersion' },
      width: 80
    },
    {
      title: '提交时间',
      dataIndex: 'creationDate',
      key: 'creationDate',
      scopedSlots: { customRender: 'creationDate' },
      width: 100,
      defaultSortOrder: 'descend',
      sortDirections: ['ascend', 'descend', 'ascend'],
      sorter: (a, b) => a.creationDate - b.creationDate
    },
    {
      title: '提交人',
      key: 'createdBy',
      dataIndex: 'createdBy',
      scopedSlots: { customRender: 'createdBy' },
      width: 80
    },
    {
      title: '备注',
      key: 'remark',
      dataIndex: 'remark',
      scopedSlots: { customRender: 'remark' },
      width: 80
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
    name: "VersionInfo",
    data () {
      return {
        isLoading: false,
        columns,
        order: 1,
        dataList: [],
        page: 1,
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0
        },
        selectItem: {}
      }
    },
    props: {
      detail: {
        type: Object,
        default: () => {
          return {}
        }
      },
      isShow: {
        type: Boolean,
        default: false
      }
    },
    computed: {

    },
    components: {
      jobVersion,
      chituTable
    },
    watch: {
      config: {
        handler () {

        },
        immediate: true
      },
      isShow: {
        handler (val) {
          if (val) {
            this.getList()
          }

        }
      }
    },
    created () {
    },
    methods: {

      // 分页数据变化
      pageChange (pageInfo) {
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
      async getList () {

        const params = {
          orderByClauses: [{
            field: "creation_date", //排序键名
            orderByMode: this.order //排序模式（1：正序，0：倒序）
          }],
          page: this.page,
          pageSize: this.pagination.defaultPageSize,
          vo: {
            fileId: this.detail.id || ""
          }
        }
        this.isLoading = true
        let res = await this.$http.post('/file/version/getVersions', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          if (res.data) {
            this.pagination.total = res.data.rowTotal
            this.dataList = res.data.rows
          }

        }
      },
      handleChange (pagination, filters, sorter) {
        if (sorter.order === 'ascend') {
          this.order = 0
          this.getList()
        } else if (sorter.order === 'descend') {
          this.order = 1
          this.getList()
        }

      },
      async scrollBack (item) {

        let onOk = async function () {

          const params = {
            id: item.id
          }

          let res = await this.$http.post('/file/version/rollback', params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            this.$message.success('回滚成功');
            this.$refs['jobVersion'].getCompareInfo()
            this.$bus.$emit('queryFile', item.fileId, 'scrollBack')
          } else {
            this.$message.error(res.msg);
          }
        }.bind(this)
        this.$confirm({
          title: `点击确认后将使用【版本号: ${item.fileVersion}, 提交时间: ${item.creationDate}】的历史版本覆盖当前编辑版本, 您确认要继续吗?`,
          content: '',
          okText: '确认',
          cancelText: '取消',
          onOk: onOk
        });
      },
      async removeVersion (item) {
        let onOk = async function () {
          const params = {
            id: item.id
          }

          let res = await this.$http.post('/file/version/deleteVersion', params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            this.$message.success('删除成功');
            this.getList()
          } else {
            this.$message.error(res.msg);
          }
        }.bind(this)
        this.$confirm({
          title: '确定要删除吗？',
          content: '',
          okText: '确认',
          cancelText: '取消',
          onOk: onOk
        });
      },
      async compare (item) {
        this.selectItem = item
        this.$refs['jobVersion'].open({ jobId: item.id, type: 'file', fileType: this.detail.fileType })

      },
    },
    mounted () {

    }
  }
</script>
<style lang="scss" scoped>
  .version-info {
    width: 100%;
    font-size: 12px;
    .row {
      margin-top: 12px;
    }
    .list {
      padding: 16px;
    }
  }
  .job-version-slot {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
</style>