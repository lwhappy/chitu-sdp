
<template>
  <a-modal
    class="add-topic-dialog"
    v-model="modalVisible"
    :mask-closable="false"
    :title="title"
    width="824px"
    @cancel="handleCancel"
  >
    <div class="add-project" v-loading="isLoading">
      <a-form-model :model="form">
        <div class="form-body">
          <a-form-model-item>
            <p class="label"><span class="red">*</span>角色</p>
            <div class="value">
              <a-radio-group name="radioGroup" v-model="form.isAdmin">
                <a-radio :value="0"> 普通开发 </a-radio>
                <a-radio :value="1"> 超级管理员 </a-radio>
              </a-radio-group>
            </div>
          </a-form-model-item>
          <a-form-model-item>
            <p class="label"><span class="red">*</span>用户名</p>
            <a-input
              class="value"
              v-model="form.userName"
              placeholder="请输入用户名"
            />
          </a-form-model-item>
          <a-form-model-item>
            <p class="label"><span class="red">*</span>账号</p>
            <a-input
              class="value"
              v-model="form.employeeNumber"
              :disabled="!isNew"
              placeholder="请输入账号"
            />
          </a-form-model-item>
          <a-form-model-item>
            <p class="label">手机号</p>
            <a-input
              class="value"
              v-model="form.privateMobile"
              placeholder="请输入手机号"
            />
          </a-form-model-item>
          <a-form-model-item>
            <p class="label">邮箱</p>
            <a-input
              class="value"
              v-model="form.email"
              placeholder="请输入邮箱"
            />
          </a-form-model-item>
          <a-form-model-item has-feedback>
            <p class="label"><span class="red">*</span>密码</p>
            <a-input
              class="value"
              type="password"
              v-model="form.password"
              placeholder="请输入密码"
            />
          </a-form-model-item>
          <a-form-model-item>
            <p class="label"><span class="red">*</span>确认密码</p>
            <a-input
              class="value"
              type="password"
              v-model="form.passwordConfirm"
              placeholder="请输入确认密码"
            />
          </a-form-model-item>
        </div>
      </a-form-model>
    </div>
    <template slot="footer">
      <a-button size="small" @click="handleCancel"> 取消 </a-button>

      <a-button type="primary" size="small" :loading="isAdding" @click="save">
        确定
      </a-button>
    </template>
  </a-modal>
</template>
<script>
import _ from "lodash";

export default {
  props: {},
  components: {},
  filters: {},
  created() {},
  watch: {},
  data() {
    return {
      formModel: {
        userName: "",
        employeeNumber: "",
        password: "",
        passwordConfirm: "",
        privateMobile: "",
        email: "",
        isAdmin: 0,
      },
      form: {},
      isNew: true,
      modalVisible: false,
      title: "新增用户",
      isLoading: false,
      isAdding: false,
    };
  },
  methods: {
    open(form) {
      this.modalVisible = true;
      if (form) {
        this.form = _.cloneDeep(form);
        this.isNew = false;
        this.title = "编辑用户";
      } else {
        this.form = _.cloneDeep(this.formModel);
        this.isNew = true;
        this.title = "新增用户";
      }
    },
    async addUser() {
      const params = {
        userName: this.form.userName,
        employeeNumber: this.form.employeeNumber,
        privateMobile: this.form.privateMobile,
        email: this.form.email,
        isAdmin: this.form.isAdmin,
        password: this.form.password,
      };
      const url = "/setting/userSetting/addUser";
      this.isAdding = true;

      let res = await this.$http.post(url, params);
      this.isAdding = false;
      if (res.code === 0) {
        this.modalVisible = false;
        this.$message.success({ content: "添加成功", duration: 2 });
        this.$emit("success");
      } else {
        this.$message.error({ content: res.msg, duration: 2 });
      }
    },
    async editUser() {
      const params = {
        id: this.form.id,
        userName: this.form.userName,
        employeeNumber: this.form.employeeNumber,
        privateMobile: this.form.privateMobile,
        email: this.form.email,
        isAdmin: this.form.isAdmin,
        password: this.form.password,
      };
      const url = "/setting/userSetting/updateUser";
      this.isAdding = true;

      let res = await this.$http.post(url, params);
      this.isAdding = false;
      if (res.code === 0) {
        this.modalVisible = false;
        this.$message.success({ content: "修改成功", duration: 2 });
        this.$emit("success");
      } else {
        this.$message.error({ content: res.msg, duration: 2 });
      }
    },
    // 关闭抽屉
    close() {
      this.$emit("close");
    },

    save() {
      if (!["0", "1"].includes(this.form.isAdmin.toString())) {
        this.$message.warning({ content: "请选择角色", duration: 2 });
        return;
      }
      if (!this.form.userName) {
        this.$message.warning({ content: "用户名不能为空", duration: 2 });
        return;
      }
      if (!this.form.employeeNumber) {
        this.$message.warning({ content: "账号不能为空", duration: 2 });
        return;
      }
      if (!this.form.password) {
        this.$message.warning({ content: "密码不能为空", duration: 2 });
        return;
      }
      if (this.form.password.length < 6) {
        this.$message.warning({ content: "密码至少6位", duration: 2 });
        return;
      }
      if (this.form.password !== this.form.passwordConfirm) {
        this.$message.warning({ content: "两次输入密码不一致", duration: 2 });
        return;
      }
      if (this.isNew) {
        this.addUser();
      } else {
        this.editUser();
      }
    },
    handleCancel() {
      this.modalVisible = false;
    },
  },
};
</script>
<style lang="scss" scoped>
.add-project {
  /deep/ .form-body {
    padding: 16px;
    // height: 70vh;
    // overflow-y: auto;
    .chitutreeicon_warning_tips {
      color: #ff5555;
      font-size: 16px !important;
    }
    .ant-form-item {
      margin-bottom: 0;
      margin-top: 12px;
      font-size: 12px;
      .ant-form-item-children {
        display: flex;
        align-items: center;
        .value {
          flex: 1;
          width: auto;
          label {
            font-size: 12px;
          }
        }
      }
      .ant-checkbox-wrapper {
        font-size: 12px;
        margin-left: 80px;
      }
      input {
        font-size: 12px;
      }
      .label {
        line-height: normal;
        width: 120px;
        text-align: right;
        margin-right: 8px;
        .question {
          cursor: pointer;
          margin-left: 3px;
        }
      }
      .ant-select-selection--multiple {
        max-height: 200px !important;
        overflow-y: auto !important;
      }
      .red {
        color: red;
      }
    }
    .edit-row {
      display: flex;
      align-items: center;
      .label {
        width: 97px;
        font-size: 12px;
        text-align: right;
        margin-right: 8px;
      }
      .red {
        color: red;
      }
      .item {
        width: calc(100% - 105px);
        font-size: 12px;
        word-wrap: break-word;
        word-break: normal;
        padding-right: 4px;
      }
    }
  }
}
.footer {
  height: 44px;
  border-top: 1px solid #ddd;
  padding-right: 16px;
}
</style>
