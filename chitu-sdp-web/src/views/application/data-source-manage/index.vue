<!--
 * @Author: hjg
 * @Date: 2021-12-10 09:46:51
 * @LastEditTime: 2022-09-29 16:45:25
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\application\data-source-manage\index.vue
-->
<template>
  <div class="data-source-manage">
    <!-- 头部 -->
    <div class="search-container">
      <div class="search-main justify-between">
        <div class="right-content justify-end">
          <div class="justify-start product-line">
            <div class="search-item">
              <p>实例名称</p>
              <a-input
                placeholder="请输入实例名称"
                v-model="params.dataSourceName"
              ></a-input>
              <p>类型</p>
            </div>
            <div class="search-item">
              <a-select
                placeholder="选择类型"
                style="width: 184px; height: 28px"
                :allow-clear="true"
                v-model="paramsType"
              >
                <a-select-option
                  :key="index"
                  :value="item.value"
                  v-for="(item, index) in typeArr"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </div>
            <div class="search-item">
              <p>作业名称</p>
              <a-input
                placeholder="请输入作业名称"
                v-model="params.jobName"
              ></a-input>
            </div>

            <div class="search-item">
              <p>表名称</p>
              <a-input
                placeholder="请输入表名称"
                v-model="params.tableName"
              ></a-input>
            </div>
            <div class="search-item">
              <p>元表名称</p>
              <a-input
                placeholder="请输入元表名称"
                v-model="params.metaTableName"
              ></a-input>
            </div>
            <div class="search-item">
              <a-button
                @click="queryDataSource"
                style="margin-left: 8px"
                type="primary"
                size="small"
                icon="search"
              >
                查询
              </a-button>
              <a-button
                @click="reset"
                style="margin-left: 8px"
                size="small"
                icon="undo"
              >
                重置
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="group-button">
      <a-button
        @click="showDataForm(`新建${titleText}数据源`, 'add')"
        type="primary"
        size="small"
        icon="plus"
      >
        添加数据源
      </a-button>
    </div>
    <!-- 内容列表 -->
    <div class="content">
      <chitu-table
        v-loading="isLoading"
        row-key="id"
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @pageChange="pageChange"
        @pageSizeChange="pageSizeChange"
        :tableOpt="{
          rowClassName: (record) => {
            return record.id === rowId ? 'clickRowStyle' : 'rowStyleNone';
          },
        }"
        @change="handleTableChange"
      >
        <!-- 数据源名称 -->
        <template #dataSourceName="{ record }">
          <div class="dataSourceName" @click="openDataSourceDetail(record)">
            <a-tooltip placement="topLeft">
              <template slot="title">
                <span>{{ record.dataSourceName }}</span>
              </template>
              <div class="title">{{ record.dataSourceName }}</div>
            </a-tooltip>
          </div>
        </template>
        <!-- 描述 -->
        <template #remark="{ record }">
          <div>
            <a-tooltip
              class="reset-toolip"
              overlayClassName="reset-toolip"
              placement="topLeft"
            >
              <template slot="title">
                <span>{{ record.remark }}</span>
              </template>
              <div>{{ record.remark }}</div>
            </a-tooltip>
          </div>
        </template>
        <!-- 引用作业数 -->
        <template #countJob="{ record }">
          <div
            class="countJob"
            :class="[record.countJob == 0 ? 'disabled' : 'blue']"
            @click="popCountJob(record)"
          >
            {{ record.countJob }}
          </div>
        </template>
        <!-- 引用元表数 -->
        <template #countMetaTable="{ record }">
          <div
            class="countMetaTable blue"
            :class="[record.countMetaTable == 0 ? 'disabled' : 'blue']"
            @click="popMetaTableTable(record)"
          >
            {{ record.countMetaTable }}
          </div>
        </template>
        <!-- 操作 -->
        <template #operation="{ record }">
          <div class="common-action-container">
            <!-- 编辑权限控制 -->
            <a-button
              type="link"
              :disabled="
                !(
                  userInfo.isAdmin === 1 ||
                  userInfo.isLeader === 1 ||
                  userInfo.id === record.owner
                )
              "
              @click="editDataSource(record)"
            >
              <i class="chitutree-h5 chitutreebianji"></i>修改
            </a-button>
            <!-- 删除权限控制 -->
            <a-divider type="vertical" />
            <a-button
              type="link"
              class="btn-danger"
              @click="handleDelete(record)"
              :disabled="
                !(
                  (userInfo.isAdmin === 1 ||
                    userInfo.isLeader === 1 ||
                    userInfo.id === record.owner) &&
                  record.isUsed === 0
                )
              "
            >
              <i class="chitutree-h5 chitutreeshanchu"></i>删除
            </a-button>
          </div>
        </template>
      </chitu-table>
    </div>
    <confirm-dialog
      :visible="deleteVisible"
      type="warning"
      @close="deleteVisible = false"
      @confirm="confirmDelete(deleteItem)"
    >
      <template>
        <p class="word-break">
          删除数据后不可恢复，确定要<span class="warn-message"
            >&nbsp;删除&nbsp;</span
          >吗？
        </p>
      </template>
    </confirm-dialog>
    <!-- 数据源 -->
    <dataForm
      v-if="formVisible"
      class="data-form"
      :formVisible="formVisible"
      :title="title"
      :dataForm="dataForm"
      :typeArr="typeArr"
      :projectUsers="projectUsers"
      :type="type"
      @closeDataFormtModal="closeDataFormtModal"
    />
    <!-- 引用作业清单 -->
    <count-job ref="countJob" />
    <!-- 引用元表清单 -->
    <count-meta-table ref="countMetaTable" />
  </div>
</template>
<script>
// import tableHeaderDrag from '../../../mixins/table-header-drag'
import dataForm from "./components/dataForm.vue";
// import tableSort from '../../../mixins/table-sort'
import CountJob from "./components/count-job/index";
import CountMetaTable from "./components/count-meta-table/index";
import ConfirmDialog from "@/components/confirm-dialog";
export default {
  // mixins: [tableHeaderDrag, tableSort],
  mixins: [],
  components: {
    dataForm,
    CountJob,
    CountMetaTable,
    ConfirmDialog,
  },
  computed: {
    columns() {
      return [
        {
          title: "实例名称",
          key: "dataSourceName",
          dataIndex: "dataSourceName",
          width: 170,
          scopedSlots: { customRender: "dataSourceName" },
          defaultSortOrder: "descend",
          sortDirections: ["ascend", "descend", "ascend"],
          sorter: (a, b) => a.dataSourceName - b.dataSourceName,
        },
        {
          title: "类型",
          key: "dataSourceType",
          dataIndex: "dataSourceType",
          width: 100,
          scopedSlots: { customRender: "dataSourceType" },
          defaultSortOrder: "descend",
          sortDirections: ["ascend", "descend", "ascend"],
          sorter: (a, b) => a.dataSourceType - b.dataSourceType,
        },
        {
          title: "描述",
          key: "remark",
          dataIndex: "remark",
          width: 170,
          scopedSlots: { customRender: "remark" },
        },
        {
          title: "引用作业数",
          key: "countJob",
          dataIndex: "countJob",
          width: 80,
          scopedSlots: { customRender: "countJob" },
        },
        {
          title: "引用元表数",
          key: "countMetaTable",
          dataIndex: "countMetaTable",
          width: 80,
          scopedSlots: { customRender: "countMetaTable" },
        },
        {
          title: "责任人",
          key: "ownerName",
          dataIndex: "ownerName",
          width: 80,
          scopedSlots: { customRender: "ownerName" },
          defaultSortOrder: "descend",
          sortDirections: ["ascend", "descend", "ascend"],
          sorter: (a, b) => a.ownerName - b.ownerName,
        },
        {
          title: "创建时间",
          key: "creationDate",
          dataIndex: "creationDate",
          width: 150,
          scopedSlots: { customRender: "creationDate" },
          defaultSortOrder: "descend",
          sortDirections: ["ascend", "descend", "ascend"],
          sorter: (a, b) => a.creationDate - b.creationDate,
        },
        {
          title: "更新人",
          key: "updatedBy",
          dataIndex: "updatedBy",
          width: 80,
          scopedSlots: { customRender: "updatedBy" },
          defaultSortOrder: "descend",
          sortDirections: ["ascend", "descend", "ascend"],
          sorter: (a, b) => a.updatedBy - b.updatedBy,
        },
        {
          title: "更新时间",
          key: "updationDate",
          dataIndex: "updationDate",
          width: 150,
          scopedSlots: { customRender: "updationDate" },
          defaultSortOrder: "descend",
          sortDirections: ["ascend", "descend", "ascend"],
          sorter: (a, b) => a.updationDate - b.updationDate,
        },
        {
          title: "操作",
          key: "operation",
          dataIndex: "operation",
          width: 150,
          fixed: "right",
          scopedSlots: { customRender: "operation" },
        },
      ];
    },
    isUatEnv() {
      return this.$store.getters.env === "uat";
    },
    titleText() {
      if (this.isUatEnv) {
        return "UAT环境";
      } else {
        return "生产环境";
      }
    },
  },
  data() {
    return {
      oldProjectId: "",
      isLoading: false,
      deleteVisible: false,
      deleteItem: null,
      type: "add",
      formVisible: false,
      title: "新建数据源",
      pagination: {
        current: 1,
        showSizeChanger: true,
        showQuickJumper: true,
        defaultPageSize: 20,
        total: 0,
        pageSizeOptions: ["10", "20", "40", "60"],
      },
      tableSortData: {
        columnsName: "columns",
        ref: "dataSourceTable",
      },
      headerDragData: {
        columnsName: "columns",
        ref: "dataSourceTable",
      },
      // typeArr: ['kafka', 'mysql', 'elasticsearch', 'doris', 'hbase', 'kudu', 'tidb', 'hive', 'print', 'datagen'],
      typeArr: [
        { value: "kafka", label: "kafka" },
        { value: "mysql", label: "mysql" },
        { value: "elasticsearch", label: "elasticsearch" },
        // { value: 'elasticsearch7', label: 'elasticsearch7' },
        // { value: 'doris', label: 'doris' },
        { value: "hbase", label: "hbase" },
        // { value: 'kudu', label: 'kudu' },
        // { value: "tidb", label: "tidb" },
        { value: "hive", label: "hive" },
        { value: "print", label: "print" },
        { value: "datagen", label: "datagen" },
        // { value: 'hudi', label: 'hudi' }
      ],
      tableData: [],
      loading: false,
      userInfo: {
        isAdmin: null,
        isLeader: null,
        employeeNumber: null,
      },
      paramsType: undefined,
      params: {
        orderByClauses: [
          {
            field: "creation_date", //排序键名
            orderByMode: 1, //排序模式（0：正序，1：倒序）
          },
        ],
        page: 1,
        pageSize: 20,
        dataSourceName: "",
        dataSourceType: "",
        jobName: "",
        tableName: "",
        metaTableName: "",
      },
      dataForm: {
        id: "",
        dataSourceName: "",
        dataSourceType: null,
        streamLoadUrl: "",
        remark: "",
        owner: "",
        dataSourceUrl: "",
        databaseName: "",
        userName: "",
        password: "",
        certifyType: "default",
        hbaseZnode: "",
        hadoopConfDir: "",
        hiveCluster: "",
        clusterName: "",
        enabledDatahub: 0,
        authKafkaClusterAddr: "",
        clusterToken: "",
        enabledFlag: 0,
        supportDatahub: 0,
      },
      rowId: null,
      projectUsers: [],
    };
  },
  created() {
    this.init();
    this.oldProjectId = this.$route.query.projectId;
  },
  watch: {
    $route: {
      //需要同时监听切换项目和切换页签时的路由变化，beforeRouteEnter和activated只能监听到切换页签，不能监听到项目切换
      handler() {
        if (
          this.$route.name === "DataSourceManage" &&
          this.$route.query.projectId !== this.oldProjectId
        ) {
          Object.assign(this.$data, this.$options.data(this));
          this.init();
          this.oldProjectId = this.$route.query.projectId;
        }
      },
      deep: true,
      // immediate: true
    },
    "$store.getters.isRemoveTag": {
      //监听关闭页签，关闭页签后清除缓存
      handler(val, oldVal) {
        if (val === oldVal) {
          return;
        }
        if (this.$store.getters.removeRouteName.includes("DataSourceManage")) {
          this.$common.toClearCache(this);
        }
      },
    },
  },
  mounted() {},
  // beforeRouteEnter: (to, from, next) => {
  //   next((vm) => {
  //     if (vm.$route.query.projectId !== vm.oldProjectId) {
  //       Object.assign(vm.$data, vm.$options.data(vm))

  //       vm.init()
  //       vm.oldProjectId = vm.$route.query.projectId
  //     }
  //   })
  // },
  methods: {
    init() {
      this.getUserRole();
      this.getDataSourceList(this.params);
      this.getUserList();
    },
    popCountJob(record) {
      if (!record.countJob) return;
      this.$refs.countJob.open(record, this.params);
    },
    popMetaTableTable(record) {
      if (!record.countMetaTable) return;
      this.$refs.countMetaTable.open(record, this.params);
    },
    // 获取项目成员
    async getUserList() {
      const params = {
        id: Number(this.$route.query.projectId),
      };
      let res = await this.$http.post(
        "/project/projectManagement/getProjectUser",
        params
      );
      if (res.code === 0) {
        this.projectUsers = res.data;
      }
    },
    handleDelete(item) {
      this.deleteItem = item;
      this.deleteVisible = true;
    },
    // 删除数据
    async confirmDelete(dataSourceInfo) {
      // console.log('deleteItem-dataSourceInfo: ', dataSourceInfo)
      let params = {
        id: dataSourceInfo.id,
      };
      let res = await this.$http.post("/dataSource/delete", params, {
        headers: {
          projectId: Number(this.$route.query.projectId),
        },
      });
      if (res.code === 0) {
        this.getDataSourceList(this.params);
        this.$message.success({ content: "删除成功", duration: 2 });
      } else {
        this.$message.error({ content: res.msg, duration: 2 });
      }
    },
    // 点击查询
    queryDataSource() {
      this.params.page = 1;
      this.pagination.current = 1;
      this.getDataSourceList(this.params);
    },
    resetData() {
      this.params = {
        orderByClauses: [
          {
            field: "creation_date", //排序键名
            orderByMode: 1, //排序模式（0：正序，1：倒序）
          },
        ],
        page: 1,
        pageSize: 20,
        dataSourceName: "",
        dataSourceType: "",
        jobName: "",
        tableName: "",
        metaTableName: "",
      };
      this.paramsType = "";
      this.pagination.current = 1;
    },
    reset() {
      this.resetData();
      this.getDataSourceList(this.params);
    },
    // 显示数据源详情
    openDataSourceDetail(dataSourceInfo) {
      // console.log('userInfo', this.userInfo)
      this.rowId = dataSourceInfo.id;
      this.dataForm = this.$common.objectToObject(
        this.dataForm,
        dataSourceInfo
      );
      this.dataForm.password = this.dataForm.password || "******";
      this.showDataForm(`${this.titleText}数据源详情`, "detail");
    },
    // 修改数据源
    editDataSource(dataSourceInfo) {
      this.dataForm.id = "";
      this.dataForm.streamLoadUrl = "";
      this.rowId = dataSourceInfo.id;
      this.dataForm = this.$common.objectToObject(
        this.dataForm,
        dataSourceInfo
      );
      // console.log('dataForm: ', this.dataForm)
      this.dataForm.password = this.dataForm.password || "******";
      this.showDataForm(`编辑${this.titleText}数据源`, "edit");
    },
    // 显示数据源窗口
    showDataForm(title, type) {
      this.title = title;
      this.type = type;
      if (type === "add") {
        // console.log('add')
        this.dataForm = {
          dataSourceName: "",
          dataSourceType: "",
          remark: "",
          owner: this.userInfo.id,
          dataSourceUrl: "",
          databaseName: "",
          userName: "",
          password: "",
          certifyType: "default",
          streamLoadUrl: "",
          hbaseZnode: "",
          hadoopConfDir: "",
          hiveCluster: "",
          clusterName: "",
          enabledDatahub: 0,
          authKafkaClusterAddr: "",
          clusterToken: "",
          enabledFlag: 0,
          supportDatahub: 0,
        };
      }
      this.formVisible = true;
    },
    // 关闭数据源窗口
    closeDataFormtModal(data) {
      this.formVisible = false;
      if (data) {
        this.params.orderByClauses.field = "creation_date";
        this.params.orderByClauses.orderByMode = 1;
        this.params.page = 1;
        this.getDataSourceList(this.params);
      }
    },
    // 排序，筛选变化时触发
    handleTableChange(pagination, filters, sorter) {
      // console.log('------handleTableChange:', pagination, filters, sorter)
      // this.resetSortMethods(sorter)
      // sorter = this.sortedInfo
      if (sorter.order) {
        if (sorter.order === "ascend") {
          this.params.orderByClauses[0].orderByMode = 0;
        } else {
          this.params.orderByClauses[0].orderByMode = 1;
        }
      }
      this.params.orderByClauses[0].field = this.$common.toLine(sorter.field);
      // console.log('handleTableChange-params: ', this.params)
      this.getDataSourceList(this.params);
    },
    // 分页数据变化
    pageChange(pageInfo) {
      // // console.log('---------pageChange: ', pageInfo.page, pageInfo.pageSize)
      this.params.page = pageInfo.page;
      this.getDataSourceList(this.params);
    },
    // pageSize变化回调
    pageSizeChange(pageSizeInfo) {
      // // console.log('---------pageSizeChange: ', pageSizeInfo.current, pageSizeInfo.size)
      this.params.page = pageSizeInfo.current;
      this.params.pageSize = pageSizeInfo.size;
      this.getDataSourceList(this.params);
    },
    // 重置分页信息
    resetPagination(pageInfo) {
      this.params.page = pageInfo.page;
      this.pagination.current = pageInfo.page;
      this.pagination.total = pageInfo.rowTotal;
    },
    // 获取数据源列表
    async getDataSourceList(params) {
      if (params.jobName && params.jobName.length < 3) {
        this.$message.warning({
          content: "作业名称请至少输入3个字符",
          duration: 2,
        });
        return;
      }
      if (params.tableName && params.tableName.length < 3) {
        this.$message.warning({
          content: "表名称请至少输入3个字符",
          duration: 2,
        });
        return;
      }
      if (params.metaTableName && params.metaTableName.length < 3) {
        this.$message.warning({
          content: "元表名称请至少输入3个字符",
          duration: 2,
        });
        return;
      }
      if (this.paramsType != undefined) {
        params.dataSourceType = this.paramsType;
      } else {
        params.dataSourceType = "";
      }
      this.isLoading = true;
      let res = await this.$http.post("dataSource/list", params, {
        headers: {
          projectId: Number(this.$route.query.projectId),
        },
      });
      this.isLoading = false;
      if (res.code === 0) {
        this.tableData = res.data.rows;
        // console.log('tableData ', this.tableData)
        // console.log('log', this.$log)
        this.$log("tableData ", this.tableData, "1");
        this.resetPagination(res.data);
      } else {
        this.$message.error({ content: res.msg, duration: 2 });
      }
    },
    // 获取数据源列表
    async getUserRole() {
      let params = {
        id: Number(this.$route.query.projectId),
      };
      let res = await this.$http.post(
        "project/projectManagement/getUserRole",
        params,
        {
          headers: {
            projectId: Number(this.$route.query.projectId),
          },
        }
      );
      if (res.code === 0) {
        this.userInfo = res.data;
        // console.log('userInfo ', this.userInfo)
      } else {
        this.$message.error({ content: res.msg, duration: 2 });
      }
    },
  },
};
</script>
<style lang="scss" scoped>
/deep/ .clickRowStyle {
  background-color: #f7f0ff;
}
.warn-message {
  color: #ff9300;
}
.rowStyleNone {
  background-color: #fff;
}
.data-source-manage {
  width: 100%;
  height: 100%;
  font-size: 12px;
  color: #333;
  .search-container {
    padding-bottom: 8px;
    background: #eff1f6;
    .search-main {
      padding: 8px 16px;
      border-bottom: 1px solid #dee2ea;
      box-sizing: border-box;
      background: #ffffff;
      .right-content {
        .product-line {
          margin-right: 16px;
          flex-flow: wrap;
          .search-item {
            margin: 8px 0;
            display: flex;
            align-items: center;
            .ant-input {
              width: 184px;
              height: 28px !important;
              line-height: 28px !important;
            }
            p {
              margin: 0 8px 0 20px;
            }
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
  .group-button {
    padding: 12px 16px;
  }
  .content {
    padding: 0 16px;
    .countJob,
    .countMetaTable {
      cursor: pointer;
      &.disabled {
        cursor: default;
        color: #333;
      }
    }

    .dataSourceName {
      .title {
        color: #0066ff;
        cursor: pointer;
      }
    }
    .operation {
      display: flex;
      .operation-btn {
        user-select: none;
        margin-right: 10px;
        i {
          margin-right: 5px;
        }
      }
      .operation-btn-active {
        color: #0066ff;
        cursor: pointer;
      }
      .operation-btn-disabled {
        color: #999;
        cursor: not-allowed;
      }
      button {
        border: none;
        padding: 0;
        margin-right: 24px;
        color: #0066ff;
        i {
          margin-right: 5px;
        }
        &:hover,
        &:focus {
          background-color: #f7f0ff;
        }
        &:disabled {
          background-color: #f7f0ff;
          color: #999;
        }
      }
      /deep/ .ant-btn[disabled] {
        background-color: #fff;
        color: #999;
        &:hover {
          background-color: #f7f0ff;
        }
      }
    }
  }
}
</style>
