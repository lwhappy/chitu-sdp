<template>
  <div class="login-container">
    <img class="login-logo" src="@/assets/icons/login-logo.png" />
    <div class="text-wrapper">
      <h1>赤兔实时计算平台</h1>
      <p>
        一站式流计算大数据开发平台
        提供端到端亚秒级实时数据分析能力，并通过标准SQL降低开发门槛，
        提高数开人员开发能力，提高数据分析及处理效率。
      </p>
    </div>
    <div class="login-wrapper">
      <a-form class="login-form" :form="form" @submit="handleSubmit">
        <h3 class="title">用户登录</h3>
        <a-form-item has-feedback>
          <a-input
            class="login-input"
            placeholder="账号"
            v-decorator="[
              'account',
              {
                rules: [
                  {
                    required: true,
                    message: '账号不能为空',
                  },
                ],
                initialValue:'admin'
              },
            ]"
          />
        </a-form-item>
        <a-form-item has-feedback>
          <a-input
            class="login-input"
            placeholder="密码"
            v-decorator="[
              'password',
              {
                rules: [
                  {
                    required: true,
                    message: '密码不能为空',
                  },
                  {},
                ],
                initialValue:'123456'
              },
            ]"
            type="password"
          />
        </a-form-item>
        <div class="btn-wrapper">
          <a-button class="btn" type="primary" html-type="submit">
            登录
          </a-button>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      confirmDirty: false,
    };
  },
  created() {
    // this.getMenu()
  },
  beforeCreate() {
    this.form = this.$form.createForm(this, { name: "login" });
  },
  methods: {
    async login(params) {
      let res = await this.$http.post("/auth/open/login", params);
      if (res.code === 0) {
        localStorage.setItem("sdpToken", res.data);
        localStorage.setItem("sdpEmployeeNumber", params.employeeNumber);
        this.$router.push({
          name: "Guide",
        });
      }else{
        this.$message.error(res.msg)
      }
    },

    validateToNextPassword(rule, value, callback) {
      const form = this.form;
      if (value && this.confirmDirty) {
        form.validateFields(["confirm"], { force: true });
      }
      callback();
    },
    checkMobile(rule, value, callback) {
      if (!isNaN(value)) {
        callback();
        return true;
      } else {
        callback();
        return false;
      }
    },
    handleSubmit(e) {
      e.preventDefault();
      this.form.validateFieldsAndScroll((err, values) => {
        console.log("err", err);
        if (!err) {
          const params = {
            employeeNumber: values.account,
            password: values.password,
          };
          this.login(params);
        }
      });
      return false
    },
  },
};
</script>

<style scoped lang="scss">
.login-container {
  background: url("~@/assets/icons/login-bg.png") no-repeat;
  background-size: cover;
  height: 100%;
  position: relative;
  .login-logo {
    margin: 30px 0 0 120px;
  }
  .text-wrapper {
    margin: 0 0 0 220px;
    position: absolute;
    top: 20%;
    width: 620px;
    color: #fff;
    h1 {
      font-size: 48px;
      color: #fff;
      margin-bottom: 30px;
    }
    p {
      font-size: 20px;
    }
  }
  .login-wrapper {
    width: 450px;
    margin: 0 auto;
    height: 428px;
    position: absolute;
    top: 20%;
    right: 10%;
    background: #ffffff;
    border-radius: 4px;
    box-shadow: 2px 8px 30px 0px rgba(157, 174, 192, 0.3);
    padding-top: 60px;
  }
  .login-form {
    margin: 0 40px;
    .title {
      font-size: 24px;
      text-align: left;
      color: #282b33;
      margin-bottom: 30px;
    }
    /deep/ .ant-form-item {
      margin-bottom: 20px;
    }
    .login-input {
      width: 370px;
      height: 48px;
      font-size: 14px;
    }
    .btn-wrapper {
      margin: 0 auto;
      display: block;
      text-align: center;
      .btn {
        margin: 20px auto;
        width: 370px;
        height: 48px;
        background: #006fff;
        border-radius: 4px;
        color: #fff;
        font-size: 16px !important;
      }
    }
    .forget-password {
      text-align: center;
      button {
        border: none;
        background: none;
        box-shadow: none;
        cursor: pointer;
      }
    }
  }
}
</style>