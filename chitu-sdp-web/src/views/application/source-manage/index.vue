<template>
  <div class="project-container">
    <div class="search-container">
      <div class="search-main justify-between">
        <div class="left-content justify-between">
          <p>jar计数<span class="sum">{{jarTotal}}</span></p>
          <a-divider type="vertical" />
          <p>引用作业总数<span class="sum">{{referenceJobsTotal}}</span></p>
        </div>
        <div class="right-content justify-end">
          <div class="justify-start product-line">
            <p>jar包名称</p>
            <search-autocomplete ref="searchRef"
                                 autoMsg="请输入jar包名称"
                                 @search="search" />
            <a-button @click="query"
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
    <div class="group-button">
      <a-button @click="add"
                type="primary"
                size="small"
                icon="plus">
        新增jar
      </a-button>
    </div>
    <!-- <div class="project-top">
      <div class="sub-project-top justify-between">
        <div class="total justify-start">
          <i class="chitutree-h5 chitutreesidenav_ziyuanguanli"></i>资源管理
        </div>
        <div class="right justify-end">
          <div class="search">
            <search-autocomplete @search="search" />
          </div>
          <p @click="add"
             class="new blue-bg"><i class="chitutree-h5 chitutreexinzeng"></i>新增jar</p>
        </div>
      </div>
    </div>
    <div class="data-cpount">
      <p class="item">Jar计数<span>{{jarTotal}}</span></p>
      <a-divider type="vertical" />
      <p class="item">引用作业总数<span>{{referenceJobsTotal}}</span></p>
    </div> -->
    <div class="project-list">
      <div class="sub-list">
        <chitu-table v-loading="isLoading"
                   :columns="columns"
                   rowKey="id"
                   :data-source="dataList"
                   @change="handleChange"
                   :pagination="pagination"
                   @pageChange="pageChange"
                   @pageSizeChange="pageSizeChange">
          <template #name="{record}">
            <span class="name">
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>{{record.name}}</span>
                </template>
                <span>{{record.name}}</span>
              </a-tooltip>
            </span>
          </template>
          <template #description="{record}">
            <span class="description">
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>{{record.description}}</span>
                </template>
                <span>{{record.description}}</span>
              </a-tooltip>
            </span>
          </template>
          <template #git="{record}">
            <span class="git">
              <a class="blue"
                 :href="record.git"
                 target="_blank"> {{gitReplace(record.git)}}</a>
            </span>
          </template>
          <template #creationDate="{record}">
            <span class="date">
              {{record.creationDate}}
            </span>
          </template>
          <template #createdBy="{record}">
            <span class="createdBy">
              {{record.createdBy}}
            </span>
          </template>
          <template #jobs="{record}">
            <span class="jobs blue"
                  @click="viewJobs(record)">
              {{record.jobs}}
            </span>
          </template>
          <template #operate="{record}">
            <div class="common-action-container">

              <a-button type="link"
                        @click="edit(record)"><i class="chitutree-h5 chitutreebianji"></i>上传新版本</a-button>
              <a-button type="link"
                        @click="gotoHistory(record)">版本</a-button>
              <a-button type="link"
                        @click="download(record)">下载</a-button>
              <a-divider type="vertical" />
              <a-button type="link"
                        class=" delete-color delete-btn"
                        @click="remove(record)"><i class="chitutree-h5 chitutreeshanchu"></i>删除</a-button>
            </div>
          </template>

        </chitu-table>
      </div>
    </div>
    <add-jar ref="addJar"
             @addSuccess="addSuccess"
             @confirm="jarConfirm"
             @cancel="jarCancel" />
    <reference-jobs ref="referenceJobs" />
  </div>
</template>

<script>
  import SearchAutocomplete from "./components/search-autocomplete";
  import AddJar from "./components/add-jar";
  import ReferenceJobs from "./components/referenceJobs";
  import { downloadByData } from "../../../utils";

  const columns = [
    {
      dataIndex: "name",
      key: "name",
      title: "名称",
      scopedSlots: { customRender: "name" },
      width: 180,
    },
    {
      dataIndex: "description",
      key: "description",
      title: "描述",
      scopedSlots: { customRender: "description" },
      width: 150,
    },
    {
      dataIndex: "git",
      key: "git",
      title: "git_url",
      scopedSlots: { customRender: "git" },
      width: 220,
    },
    {
      title: "创建时间",
      dataIndex: "creationDate",
      key: "creationDate",
      scopedSlots: { customRender: "creationDate" },
      width: 150,
      defaultSortOrder: "descend",
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.creationDate - b.creationDate,
    },
    {
      title: "创建人",
      key: "createdBy",
      dataIndex: "createdBy",
      scopedSlots: { customRender: "createdBy" },
      width: 100,
    },
    // {
    //   title: '引用作业',
    //   key: 'jobs',
    //   dataIndex: 'jobs',
    //   scopedSlots: { customRender: 'jobs' },
    //   width: '10%'
    // },
    {
      title: "操作",
      key: "operate",
      fixed: "right",
      dataIndex: "operate",
      scopedSlots: { customRender: "operate" },
      width: 200,
    },
  ];

  export default {
    data () {
      return {
        oldProjectId: '',
        sourceName: "",
        isLoading: false,
        dataList: [],
        columns,
        isShowMemberDialog: false,

        isNew: true,
        projectDialog: {
          title: "",
        },
        page: 1,
        pagination: {
          current: 1,
          showSizeChanger: true,
          showQuickJumper: true,
          defaultPageSize: 20,
          total: 0,
        },
        referenceJobsTotal: 0,
        jarTotal: 0,
        order: 1,
      };
    },
    components: {
      SearchAutocomplete,
      AddJar,
      ReferenceJobs,
    },
    created () {
      this.init()
      this.oldProjectId = this.$route.query.projectId
    },
    watch: {
      $route: {//需要同时监听切换项目和切换页签时的路由变化，beforeRouteEnter和activated只能监听到切换页签，不能监听到项目切换
        handler () {
          if (this.$route.name === 'SourceManage' && this.$route.query.projectId !== this.oldProjectId) {
            Object.assign(this.$data, this.$options.data(this))
            this.init()
            this.oldProjectId = this.$route.query.projectId
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
          if (this.$store.getters.removeRouteName.includes('SourceManage')) {
            this.$common.toClearCache(this);
          }
        }
      }
    },

    mounted () {
    },
    methods: {
      resetData () {
        this.pagination.defaultPageSize = 20
        this.page = 1;
        this.pagination.current = 1;
        this.sourceName = ''
        this.order = 1
        this.referenceJobsTotal = 0
        this.jarTotal = 0
        this.dataList = []
        this.$refs.searchRef.keyword = ''
      },
      init () {
        this.getList();
        this.referenceJobs();
      },
      search (value) {
        this.sourceName = value;
      },
      query () {
        this.pagination.current = 1;
        this.getList();
      },
      reset () {
        this.sourceName = "";
        this.$refs.searchRef.keyword = "";
        this.pagination.current = 1;
        this.getList();
      },
      async getList () {
        const params = {
          orderByClauses: [
            {
              field: "creation_date",
              orderByMode: this.order,
            },
          ],
          page: this.page,
          pageSize: this.pagination.defaultPageSize,
          vo: {
            name: this.sourceName,
            projectId: Number(this.$route.query.projectId),
            type: "first",
          },
        };
        this.dataList = [];
        this.pagination.total = 0;
        this.isLoading = true;
        let res = await this.$http.post("/jar/queryJar", params);
        this.isLoading = false;
        if (res.code === 0) {
          if (res.data) {
            this.pagination.total = res.data.rowTotal;
            if (!this.sourceName) {
              this.jarTotal = res.data.rowTotal;
            }
            if (res.data.rows) {
              let rows = res.data.rows;
              // rows = rows.map((item, index) => {
              //   item.key = index
              //   return item
              // })
              this.dataList = rows;
            } else {
              this.dataList = [];
            }
          }
        } else {
          this.$message.error(res.msg);
        }
      },
      async referenceJobs () {
        const params = {
          name: "",
          projectId: Number(this.$route.query.projectId),
          // version: "v1"
        };
        let res = await this.$http.post("/jar/referenceJobs", params);
        if (res.code === 0) {
          if (res.data) {
            this.referenceJobsTotal = res.data.total;
          }
        } else {
          this.$message.error(res.msg);
        }
      },
      popMemberDialog (item) {
        this.$refs.memberDialog.open(item);
      },
      add () {
        const obj = {
          type: "add",
          data: null,
          title: "新增jar包资源",
        };
        this.$refs.addJar.open(obj);
      },
      edit (item) {
        const obj = {
          type: "editMain", //编辑主版本
          data: item,
          title: "编辑：" + item.name,
        };
        this.$refs.addJar.open(obj);
      },
      viewJobs (data) {
        this.$refs.referenceJobs.open(data);
      },
      remove (item) {
        var onOk = async function () {
          const params = {
            id: item.id,
            name: item.name,
          };
          let res = await this.$http.post("/jar/deleteJar", params);
          if (res.code === 0) {
            this.$message.success("删除成功");
            this.getList();
          } else {
            this.$message.error(res.msg);
          }
        }.bind(this);
        this.$confirm({
          title: "确定要删除吗?",
          content: "",
          okText: "确认",
          cancelText: "取消",
          onOk: onOk,
        });
      },

      jarConfirm () { },
      jarCancel () { },

      // 分页数据变化
      pageChange (pageInfo) {
        this.page = pageInfo.page;
        this.getList();
      },
      // pageSize变化回调
      pageSizeChange (pageSizeInfo) {
        this.pagination.defaultPageSize = pageSizeInfo.size;
        this.page = 1;
        this.pagination.current = 1;
        this.getList();
      },
      addSuccess () {
        this.page = 1;
        this.pagination.current = 1;
        this.getList();
      },
      handleChange (pagination, filters, sorter) {
        if (sorter.order === "ascend") {
          this.order = 0;
          this.getList();
        } else if (sorter.order === "descend") {
          this.order = 1;
          this.getList();
        }
      },
      gotoHistory (item) {
        this.$router.push({
          name: "SourceManage_historyVersion",
          query: {
            //预留query
            projectId: Number(this.$route.query.projectId),
            projectName: this.$route.query.projectName,
            projectCode: this.$route.query.projectCode,
            name: encodeURIComponent(item.name),
          },
        });
      },
      gitReplace (url) {
        url = url.replace("xxx", "...");
        return url;
      },
      async download (record) {
        const params = {
          name: record.name,
          projectId: Number(this.$route.query.projectId),
          // version: "v1"
        };
        this.isLoading = true;
        //blob方式
        let res = await this.$http.post("/jar/download", params, {
          headers: {
            projectId: Number(this.$route.query.projectId),
          },
          responseType: "blob",
        });
        // console.log('res', res)
        if (res) {
          if (res.msg) {//有msg说明文件有问题，不能下载
            this.$message.error(res.msg);
            this.isLoading = false;
            return;
          }
          downloadByData(res.data, record.name);
          this.isLoading = false;
        }
      },
    },
  };
</script>

<style lang="scss" scoped>
  .project-container {
    height: 100%;
    .guide-component {
      width: 100%;
      height: 100%;
    }
    .search-container {
      padding-bottom: 8px;
      background: #eff1f6;
      .search-main {
        height: 56px;
        padding: 0 16px;
        border-bottom: 1px solid #dee2ea;
        box-sizing: border-box;
        background: #ffffff;
        .left-content {
          font-size: 12px;
          .sum {
            margin-left: 6px;
            font-size: 16px;
            font-weight: 900;
            color: #0066ff;
          }
        }
        .right-content {
          .product-line {
            margin-right: 16px;
            p {
              margin: 0 8px 0 20px;
            }
          }
          .productLine {
            /deep/ .ant-select-selection--single {
              height: 28px;
              .ant-select-selection__rendered {
                line-height: 28px;
              }
            }
          }
        }
      }
    }
    .group-button {
      padding: 12px 16px;
    }
    // .project-top {
    //   height: 41px;
    //   font-size: 14px;
    //   border-bottom: 1px solid #d9d9d9;
    //   .sub-project-top {
    //     height: 100%;
    //     padding: 0 16px;
    //     margin: 0 auto;
    //     .total {
    //       font-size: 16px;
    //       font-weight: bold;
    //       /deep/ i {
    //         font-size: 16px !important;
    //         margin-right: 10px;
    //       }
    //       .item {
    //         margin-right: 20px;
    //         color: rgb(71, 66, 66);
    //         font-size: 16px;
    //         &.member {
    //           i {
    //             margin-right: 8px;
    //           }
    //         }
    //       }
    //     }
    //     .right {
    //       height: 38px;
    //       overflow: hidden;
    //       .new {
    //         width: 92px;
    //         height: 28px;
    //         text-align: center;
    //         line-height: 28px;
    //         color: #fff;
    //         font-size: 12px;
    //         cursor: pointer;
    //         margin-top: -2px;
    //         i {
    //           font-size: 12px;
    //           margin-right: 7px;
    //         }
    //       }
    //     }
    //   }
    // }
    // .data-cpount {
    //   height: 40px;
    //   display: flex;
    //   align-items: center;
    //   padding-left: 40px;
    //   .item {
    //     font-size: 12px;
    //     color: #666;
    //     span {
    //       margin: 0 10px;
    //       font-weight: bold;
    //     }
    //   }
    // }
    .project-list {
      // height: calc(100% - 41px - 40px);
      padding: 0 16px;
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
      .sub-list {
        height: 100%;

        .jobs {
          cursor: pointer;
        }
        // /deep/ .ant-table-thead > tr > th {
        //   background: #f9f9f9;
        //   padding: 18px 16px;
        //   color: #333;
        //   font-size: 12px;
        //   border-bottom: 1px solid #f9f9f9;
        // }
        // /deep/ .ant-table-tbody > tr > td {
        //   border-bottom: 1px solid #f9f9f9;
        //   color: #333;
        //   font-size: 14px;
        // }
        /deep/ .name {
          cursor: pointer;
        }

        /deep/ .member {
          cursor: pointer;
        }
        /deep/ .date {
          color: #999;
        }
        /deep/ .operate {
          i {
            margin-right: 3px;
          }
          .ant-btn-link {
            padding-left: 4px;
            padding-right: 4px;
          }
          .delete-btn {
            padding-left: 0 !important;
            &:hover {
              color: #f95353 !important;
            }
          }
        }
        .footer-page {
          height: 72px;
          padding: 20px 16px;
        }
      }
    }
  }
</style>
