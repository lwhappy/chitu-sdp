<template>
  <div class="project-container">
    <div class="search-container">
      <div class="search-main justify-start">
        <!-- <div class="create-time justify-start">
          <p class="label">创建时间</p>
          <a-range-picker v-model="pickDate"
                          showTime
                          @change="onChangeDate">
            <img src="@/assets/icons/date.png"
                 slot="suffixIcon"
                 type="smile" />
          </a-range-picker>
        </div> -->

        <div class="user-name justify-start">
          <p class="label">用户名</p>
          <search-user
            ref="searchRef"
            autoMsg="请输入用户名"
            @search="search"
          />
        </div>
        <div class="employee-number justify-start">
          <p class="label">账号</p>
          <a-input type="text" v-model="employeeNumber" />
        </div>

        <a-button
          @click="query"
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
    <div class="group-button">
      <a-button @click="openDrawer" type="primary" size="small" icon="plus">
        新增用户
      </a-button>
    </div>
    <div class="project-list">
      <div class="sub-list">
        <chitu-table
          v-loading="isLoading"
          :columns="columns"
          :dataSource="dataList"
          rowKey="id"
          @change="handleChange"
          :pagination="pagination"
          @pageChange="pageChange"
          @pageSizeChange="pageSizeChange"
        >
        </chitu-table>
      </div>
    </div>
    <add-user ref="addUser" @success="addUserSuccess" />

    <confirm-dialog
      :visible="deleteVisible"
      type="warning"
      @close="deleteVisible = false"
      @confirm="confirmDelete(deleteItem)"
    >
      <template>
        <p>确定要删除吗？</p>
      </template>
    </confirm-dialog>
  </div>
</template>
<script>
import AddUser from "./add-user-dialog.vue";

import SearchUser from "./components/search-user.vue";
import ConfirmDialog from "./components/confirm-dialog.vue";

const columns = (vm) => {
  return [
    {
      dataIndex: "userName",
      title: "用户名",
      width: 150,
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.userName - b.userName,
    },
    {
      dataIndex: "employeeNumber",
      title: "账号",
      width: 150,
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.employeeNumber - b.employeeNumber,
    },
    {
      dataIndex: "privateMobile",
      title: "手机号",
      width: 150,
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.privateMobile - b.privateMobile,
    },
    {
      dataIndex: "email",
      title: "邮箱",
      width: 150,
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.email - b.email,
    },
    {
      dataIndex: "password",
      title: "密码",
      width: 150,
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.password - b.password,
      customRender: (text, record) => {
        const password = record.isShowPassword ? text : "******";
        const src = record.isShowPassword
          ? require("@/assets/icons/eye-close.png")
          : require("@/assets/icons/eye-open.png");
        return {
          children: vm.$createElement(
            "div",
            {
              style: {
                display: "flex",
              },
            },
            [
              vm.$createElement(
                "p",
                {
                  style: {
                    flex: 1,
                  },
                },
                password
              ),

              vm.$createElement(
                "img",
                {
                  attrs: {
                    src: src,
                    width: "16",
                    height: "16",
                  },
                  style: {
                    cursor: "pointer",
                    marginLeft: "10px",
                  },
                  on: {
                    click: () => {
                      record.isShowPassword = !record.isShowPassword;
                    },
                  },
                },
                ""
              ),
            ]
          ),
        };
      },
    },
    {
      dataIndex: "isAdmin",
      title: "角色",
      width: 150,
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.isAdmin - b.isAdmin,
      customRender: (text, record) => {
        let name = "";
        if (typeof record.isAdmin !== "undefined") {
          if (record.isAdmin.toString() === "0") {
            name = "普通开发";
          } else if (record.isAdmin.toString() === "1") {
            name = "超级管理员";
          }
        }

        return name;
      },
    },

    {
      dataIndex: "updatedBy",
      title: "更新人",
      width: 150,
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.updatedBy - b.updatedBy,
    },
    {
      dataIndex: "updationDate",
      title: "更新时间",
      width: 150,
      sortDirections: ["ascend", "descend", "ascend"],
      sorter: (a, b) => a.updationDate - b.updationDate,
    },

    {
      title: "操作",
      key: "operate",
      dataIndex: "operate",
      fixed: "right",
      width: 250,
      customRender: (text, record) => {
        return {
          children: vm.$createElement(
            "div",
            {
              class: "common-action-container ",
            },
            [
              vm.$createElement(
                "a-button",
                {
                  attrs: { type: "link" },
                  on: {
                    click: () => {
                      vm.edit(record);
                    },
                  },
                },
                "编缉"
              ),
              vm.$createElement("a-divider", {
                attrs: {
                  type: "vertical",
                },
              }),
              vm.$createElement(
                "a-button",
                {
                  attrs: {
                    type: "link",
                    disabled: record.id === vm.currentUserId ? true : false,
                  },
                  on: {
                    click: () => {
                      vm.handleDelete(record);
                    },
                  },
                },
                "移除"
              ),
            ]
          ),
        };
      },
    },
  ];
};
export default {
  data() {
    return {
      employeeNumber: null,
      currentUserId: "",
      editUser: {},
      editVisible: false,
      pickDate: null,
      deleteVisible: false,
      modalVisible: false,
      deleteItem: null,
      isLoading: false,
      dataList: [],
      columns: columns(this),

      pagination: {
        current: 1,
        showSizeChanger: true,
        showQuickJumper: true,
        defaultPageSize: 20,
        total: 0,
      },
      order: {
        field: "creation_date",
        value: 1,
      },
      date: {
        startTime: null,
        endTime: null,
      },
    };
  },
  components: {
    AddUser,
    SearchUser,
    ConfirmDialog,
  },
  created() {
    this.currentUserId = sessionStorage.getItem("userId");
  },
  watch: {
    "$store.getters.isRemoveTag": {
      //监听关闭页签，关闭页签后清除缓存
      handler(val, oldVal) {
        if (val === oldVal) {
          return;
        }
        if (this.$store.getters.removeRouteName.includes("SystemSettingUser")) {
          this.$common.toClearCache(this);
        }
      },
    },
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.getList();
    },
    reset() {
      this.roleIds = [];
      this.pickDate = null;
      this.date = {
        startTime: null,
        endTime: null,
      };
      this.projectName = "";
      if (this.$refs.searchRef) {
        this.$refs.searchRef.keyword = "";
      }
      if (this.$refs.roles) {
        this.$refs.roles.value = [];
      }
      this.pagination.current = 1;
      this.getList();
    },
    search(serachData) {
      this.projectName = serachData;
    },
    onChangeDate(date, dateString) {
      // console.log(date, dateString);
      this.date.startTime = dateString[0];
      this.date.endTime = dateString[1];
    },
    query() {
      this.getList();
    },
    addUserSuccess() {
      this.pagination.current = 1;
      this.getList();
    },
    async getList() {
      let userName = "";
      if (this.$refs.searchRef) {
        userName = this.$refs.searchRef.keyword;
      }

      const params = {
        orderByClauses: [
          {
            field: this.order.field,
            orderByMode: this.order.value,
          },
        ],
        page: this.pagination.current,
        pageSize: this.pagination.defaultPageSize,

        vo: {
          userName: userName,
          employeeNumber: this.employeeNumber,
          startTime: this.date.startTime,
          endTime: this.date.endTime,
        },
      };
      this.dataList = [];

      const url = "/setting/userSetting/getUserList";
      this.isLoading = true;
      let res = await this.$http.post(url, params);
      this.isLoading = false;
      if (res.code === 0) {
        if (res.data) {
          this.pagination.total = res.data.rowTotal;
          if (res.data.rows) {
            let rows = res.data.rows;
            rows = rows.map((item) => {
              item.isShowPassword = false;
              return item;
            });
            this.dataList = rows;
          }
        }
      } else {
        this.$message.error(res.msg);
      }
    },
    popMemberDialog(item) {
      this.$refs.memberDialog.open(item);
    },

    edit(item) {
      this.$refs.addUser.open(item);
    },
    handleDelete(item) {
      this.deleteItem = item;
      this.deleteVisible = true;
    },

    // 分页数据变化
    pageChange(pageInfo) {
      this.pagination.current = pageInfo.page;
      this.getList();
    },
    // pageSize变化回调
    pageSizeChange(pageSizeInfo) {
      this.pagination.defaultPageSize = pageSizeInfo.size;
      this.pagination.current = 1;
      this.getList();
    },
    addSuccess() {
      this.pagination.current = 1;
      this.getList();
    },
    handleChange(pagination, filters, sorter) {
      // console.log(sorter)
      this.order.field = this.$common.toLine(sorter.field);
      if (sorter.order === "ascend") {
        this.order.value = 0;
        this.getList();
      } else if (sorter.order === "descend") {
        this.order.value = 1;
        this.getList();
      }
    },
    openDrawer() {
      this.$refs.addUser.open();
    },

    async confirmDelete(item) {
      const params = {
        id: item.id,
      };
      let res = await this.$http.post(
        "/setting/userSetting/deleteUser",
        params
      );
      if (res.code === 0) {
        this.$message.success("删除成功");
        this.getList();
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.project-container {
  height: 100%;
  .search-container {
    padding-bottom: 8px;
    background: #eff1f6;
    .search-main {
      height: 56px;
      padding: 0;
      border-bottom: 1px solid #dee2ea;
      box-sizing: border-box;
      background: #ffffff;
      .label {
        margin-right: 8px;
        width: 50px;
        text-align: right;
      }
      .user-name {
        margin: 0;
      }
    }
  }

  .guide-component {
    width: 100%;
    height: 100%;
  }
  .group-button {
    padding: 12px 16px;
  }
  .project-list {
    // height: calc(100% - 131px);
    padding: 0 16px;
    .sub-list {
      height: 100%;
    }
  }
}
</style>