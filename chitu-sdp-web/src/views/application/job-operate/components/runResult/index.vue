<!--
 * @Author: lw
 * @Date: 2021-11-08 21:32:02
 * @LastEditTime: 2022-07-12 19:25:08
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\runResult\index.vue
-->
<template>
  <div class="run-result"
       v-loading="isLoading">
    <div v-show="!isShowDetail"
         class="run-list">
      <div class="rource ">
        <a-collapse class="collapse-container"
                    :default-active-key="[1,2]"
                    :bordered="false">
          <template #expandIcon="props">
            <a-icon type="caret-right"
                    :rotate="props.isActive ? 90 : 0" />
          </template>

          <a-collapse-panel key="1"
                            header="来源表"
                            :style="customStyle">
            <a-table class="table-data"
                     bordered
                     row-key="id"
                     v-defaultPage="!sourceData || (sourceData && sourceData.length === 0)"
                     :columns="sourceColumns"
                     :data-source="sourceData"
                     :loading="loading"
                     :pagination="false"
                     :scroll="{y: 'calc(100% - 55px)'}">
              <span slot="consumedTimeTitle">
                当前消费位置
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>当前消费位置后台每10分钟更新一次, 仅供参考, 实际的消费位置可能已更新</span>
                  </template>
                  <!-- <span><img width="14"
                     height="14"
                     src="@/assets/icons/ask.png"
                     alt=""></span> -->
                  <a-icon type="question-circle"
                          style="font-size:14px; color:#0066FF ;" />
                </a-tooltip>
              </span>
              <span slot="pendingRecordsTitle">
                待消费数
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>待消费数后台每1分钟更新一次。</span>
                  </template>
                  <!-- <span><img width="14"
                     height="14"
                     src="@/assets/icons/ask.png"
                     alt=""></span> -->
                  <a-icon type="question-circle"
                          style="font-size:14px; color:#0066FF ;" />
                </a-tooltip>
              </span>
              <span slot="consumedTotalTitle">
                消费总数
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>消费总数后台每1分钟更新一次，从任务最近一次启动开始累计。</span>
                  </template>
                  <!-- <span><img width="14"
                     height="14"
                     src="@/assets/icons/ask.png"
                     alt=""></span> -->
                  <a-icon type="question-circle"
                          style="font-size:14px; color:#0066FF ;" />
                </a-tooltip>
              </span>
              <span slot="deserializeFailNumTitle">
                消费失败数
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>消费失败数后台每1分钟更新一次，从任务最近一次启动开始累计。</span>
                  </template>
                  <!-- <span><img width="14"
                     height="14"
                     src="@/assets/icons/ask.png"
                     alt=""></span> -->
                  <a-icon type="question-circle"
                          style="font-size:14px; color:#0066FF ;" />
                </a-tooltip>
              </span>
              <span slot="dataSourceName"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.dataSourceName}}</span>
                  </template>
                  <span>{{ record.dataSourceName }}</span>
                </a-tooltip>
              </span>
              <span slot="metatableName"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.metatableName}}</span>
                  </template>
                  <span>{{ record.metatableName }}</span>
                </a-tooltip>
              </span>
              <span slot="pendingRecords"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.pendingRecords}}</span>
                  </template>
                  <span>{{ record.pendingRecords }}</span>
                </a-tooltip>
              </span>
              <span slot="consumedTotal"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.consumedTotal}}</span>
                  </template>
                  <span>{{ record.consumedTotal }}</span>
                </a-tooltip>
              </span>
              <span slot="consumedTotal"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.consumedTotal}}</span>
                  </template>
                  <span>{{ record.consumedTotal }}</span>
                </a-tooltip>
              </span>
              <span slot="deserializeFailNum"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.deserializeFailNum}}</span>
                  </template>
                  <span class="deserializeFailNum blue"
                        @click="goDetail(record)">{{ record.deserializeFailNum }}</span>
                </a-tooltip>
              </span>
            </a-table>
            <span slot="extra"
                  class="refresh blue"
                  @click="handleRefresh">
              <i class="chitutree-h5 chitutreeshuaxin blue"
                 :style="{transform:'rotate(' + rotate +'deg)'}"></i>刷新
            </span>
          </a-collapse-panel>
          <a-collapse-panel key="2"
                            header="目标表"
                            :style="customStyle">
            <a-table class="table-data"
                     bordered
                     row-key="id"
                     v-defaultPage="!sinkData || (sinkData && sinkData.length === 0)"
                     :columns="sinkColumns"
                     :data-source="sinkData"
                     :loading="loading"
                     :pagination="false"
                     :scroll="{y: 'calc(100% - 55px)'}">
              <span slot="dataSourceName"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.dataSourceName}}</span>
                  </template>
                  <span>{{ record.dataSourceName }}</span>
                </a-tooltip>
              </span>
              <span slot="databaseName"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.databaseName}}</span>
                  </template>
                  <span>{{ record.databaseName }}</span>
                </a-tooltip>
              </span>
              <span slot="metatableName"
                    slot-scope="text,record">
                <a-tooltip placement="topLeft">
                  <template slot="title">
                    <span>{{record.metatableName}}</span>
                  </template>
                  <span>{{ record.metatableName }}</span>
                </a-tooltip>
              </span>
              <span class="sinkErrorRecords"
                    slot="sinkErrorRecords"
                    slot-scope="text, record">
                <span v-if="['doris','kafka'].includes(record.dataSourceType)"
                      class="blue"
                      @click="goDetail(record)">{{ record.sinkErrorRecords }}</span>
                <span v-else>暂不支持查看</span>
              </span>
              <span class="sinkSuccessRecords"
                    slot="sinkSuccessRecords"
                    slot-scope="text, record">
                <span v-if="['doris','kafka'].includes(record.dataSourceType)">{{ record.sinkSuccessRecords }}</span>
                <span v-else>暂不支持查看</span>
              </span>
            </a-table>
          </a-collapse-panel>

        </a-collapse>
      </div>
    </div>
    <fail-detail ref="detail"
                 v-show="isShowDetail"
                 :show-list="showList" />

  </div>
</template>
<script>
  // import Pagination from '@/components/pagination/index'
  import FailDetail from './fail-detail'
  export default {
    name: 'RunResult',
    components: {
      FailDetail
    },
    mixins: [],
    props: {
      activeName: {
        type: String,
        value: ''
      }
    },
    computed: {
      sourceColumns () {
        return [{
          title: '数据源类型',
          key: 'dataSourceType',
          dataIndex: 'dataSourceType',
          scopedSlots: { customRender: 'dataSourceType' },
          width: 100,
        },
        {
          title: '数据源',
          key: 'dataSourceName',
          dataIndex: 'dataSourceName',
          scopedSlots: { customRender: 'dataSourceName' },
          width: 130,
        },
        // {
        //   title: '数据库',
        //   dataIndex: 'databaseName',
        //   scopedSlots: { customRender: 'databaseName' },
        //   width: '80'
        // },
        {
          title: 'topic/表',
          key: 'metatableName',
          dataIndex: 'metatableName',
          scopedSlots: { customRender: 'metatableName' },
          width: 120,
        },
        {
          // title: '当前消费位置',
          key: 'consumedTime',
          dataIndex: 'consumedTime',
          slots: { title: 'consumedTimeTitle' },
          scopedSlots: { customRender: 'consumedTime' },
          width: 140,
        },
        {
          // title: '待消费数',
          key: 'pendingRecords',
          dataIndex: 'pendingRecords',
          slots: { title: 'pendingRecordsTitle' },
          scopedSlots: { customRender: 'pendingRecords' },
          width: 80,
        },
        {
          // title: '消费总数',
          key: 'consumedTotal',
          dataIndex: 'consumedTotal',
          slots: { title: 'consumedTotalTitle' },
          scopedSlots: { customRender: 'consumedTotal' },
          width: 80,
        },
        {
          // title: '消费失败数',
          key: 'deserializeFailNum',
          dataIndex: 'deserializeFailNum',
          slots: { title: 'deserializeFailNumTitle' },
          scopedSlots: { customRender: 'deserializeFailNum' },
          width: 90,
        }]
      },
      sinkColumns () {
        return [{
          title: '数据源类型',
          key: 'dataSourceType',
          dataIndex: 'dataSourceType',
          scopedSlots: { customRender: 'dataSourceType' },
          width: 120,
        },
        {
          title: '数据源',
          key: 'dataSourceName',
          dataIndex: 'dataSourceName',
          scopedSlots: { customRender: 'dataSourceName' },
          width: 150,
        },
        {
          title: '数据库',
          key: 'databaseName',
          dataIndex: 'databaseName',
          scopedSlots: { customRender: 'databaseName' },
          width: 120,
        },
        {
          title: 'topic/表',
          key: 'metatableName',
          dataIndex: 'metatableName',
          scopedSlots: { customRender: 'metatableName' },
          width: 120,
        },
        {
          title: '写入成功数',
          key: 'sinkSuccessRecords',
          dataIndex: 'sinkSuccessRecords',
          scopedSlots: { customRender: 'sinkSuccessRecords' },
          width: 80,
        },
        {
          title: '写入失败数',
          key: 'sinkErrorRecords',
          dataIndex: 'sinkErrorRecords',
          scopedSlots: { customRender: 'sinkErrorRecords' },
          width: 80,
        }
        ]
      }
    },
    data () {
      return {
        customStyle: 'border-radius: 4px;margin-bottom: 0;border: 0;overflow: hidden;background:#FFF',
        isLoading: false,
        timeGap: 60 * 1000,
        timeOut: null,
        rotate: 0,
        isClick: false,
        isShowDetail: false,
        detailData: null,
        sourceData: [],
        sinkData: [],
        loading: false,
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0,
          pageSizeOptions: ['10', '20', '40', '60']
        }
      }
    },
    created () {

    },
    beforeDestroy () {
      // window.stop()
      this.clear()
    },
    watch: {
      activeName: {
        async handler (val) {
          if (val === 'runResult') {
            this.clear()
            this.init()
          } else {
            this.clear()
          }
        },
        immediate: true
      }
    },
    methods: {
      init () {
        this.getData('init')
        this.loopData()
      },
      clear () {
        clearTimeout(this.timeOut)
        this.timeOut = null
      },
      loopData () {
        this.timeOut = setTimeout(() => {
          this.getData()
          this.loopData()
        }, this.timeGap)
      },
      handleRefresh (event) {
        event.stopPropagation();
        this.getData()
      },
      async getData (init) {
        this.rotate += 360
        // setTimeout(() => {
        //   this.isClick = false
        // }, 3000)
        const jobId = this.$store.getters.jobInfo.id
        const url = `/runResult/queryRunningResult?jobId=${jobId}`
        if (init) {
          this.isLoading = true
        }
        let res = await this.$http.get(url)
        this.isLoading = false
        if (res.code === 0) {
          this.sourceData = res.data.source
          this.sinkData = res.data.sink
        }
      },
      goDetail (record) {
        this.isShowDetail = true
        this.$refs.detail.init(record)
      },
      showList () {
        this.isShowDetail = false
      }

    }
  }
</script>
<style lang="scss" scoped>
  .run-result {
    width: 100%;
    height: 100%;
    .collapse-container {
      /deep/ .ant-collapse-header {
        font-size: 14px !important;
        font-weight: 900;
        i {
          color: #006fff !important;
        }
      }
      /deep/ .ant-collapse-item {
        font-size: 12px;
      }
    }
    .refresh {
      border: none;
      background: none;
      cursor: pointer;
      display: flex;
      color: #006eff;
      font-weight: normal;
      font-size: 12px;
      i {
        display: block;
        transition: transform 2s;
        transform: rotateZ(0deg);
        transform-origin: center;
        margin-right: 6px;
      }
      .rotate {
        transform: rotateZ(360deg);
      }
    }
    .sink {
      margin-top: 16px;
    }
    .table-data {
      height: calc(100% - 72px);
      overflow-y: hidden;
      /deep/ .ant-table-thead > tr > th {
        padding: 12px 16px;
        font-weight: 700;
        font-size: 12px;
        border-right: 1px solid #e8e8e8 !important;
      }
      /deep/ .ant-table-tbody > tr > td {
        padding: 7px;
      }
      .deserializeFailNum,
      .sinkErrorRecords {
        cursor: pointer;
      }
      /deep/ .ant-table-placeholder {
        visibility: hidden;
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
  }
</style>
