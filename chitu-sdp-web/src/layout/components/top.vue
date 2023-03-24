<template>
  <div class="header-container">
    <div class="left">
      <!-- 环境切换 -->
      <div class="env-container">
        <EnvSnippet :value="publicEnv" :envList="envList" @change="changeEnv" />
      </div>
      <!-- 项目列表 -->
      <div v-if="showProject" class="project-container justify-start">
        <p class="name justify-center">项目名称:</p>
        <el-select
          :popper-append-to-body="false"
          v-model="projectName"
          filterable
          placeholder="请输入项目名称"
          @blur="handleSelectBlur"
          class="project-select-wrap"
          @change="handleSelectChange"
        >
          <el-option
            v-for="item in projectData"
            :key="item.id"
            :label="item.projectName"
            :value="item.id"
          >
          </el-option>
        </el-select>
      </div>
    </div>
    <div class="right">
      <!-- 下拉菜单 -->
      <div class="action-container">
        <el-dropdown
          trigger="click"
          @command="handleCommand"
          @visible-change="visibleChange"
        >
          <div class="action-wrap">
            <div class="info">
              <img src="@/assets/icons/user.png" />
              <p v-if="$store.getters.userInfo">
                {{ $store.getters.userInfo.userName }}
              </p>
            </div>
            <i :class="iconClass" class="login-user-dropdown-icon"></i>
          </div>
          <el-dropdown-menu slot="dropdown" class="menu-wrap">
            <el-dropdown-item command="logout">
              <div class="logout_btn logout">退出登录</div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
import EnvSnippet from "./env-snippet.vue";
import {logout} from '@/utils/index.js'
export default {
  name: "Top",
  components: {
    EnvSnippet,
  },
  props: {
    //是否展示项目栏
    showProject: {
      type: Boolean,
      default: false,
    },
  },
  mixins: [],
  watch: {
    showProject(val) {
      val && this.getProjectList();
    },
    $route: {
      handler(to) {
        if (to.meta.isDataHub) {
          this.isDisabled = true;
          this.publicEnv = "prod";
        } else {
          this.isDisabled = false;
          this.publicEnv = this.$store.getters.env;
        }
        //缓存当前页面环境变量(因为dataHub是物理环境，跳转平台需要携带当前页面的环境变量)
        this.$store.dispatch("global/setCurrentEnv", this.publicEnv);
      },
      immediate: true,
    },
  },
  data() {
    return {
      iconClass: "el-icon-arrow-down",
      approveNum: 0,
      envList: [
        // { label: '开发环境', value: 'dev', color: '#FFB318' },
        { label: "UAT环境", value: "uat", color: "#25CBCE" },
        { label: "生产环境", value: "prod", color: "#1790FF" },
      ],
      envText: "",
      publicEnv: "dev",
      projectName: "",
      httpData: [],
      fetching: false,
      projectData: [],
      isDisabled: false,
    };
  },
  created() {
    if (this.showProject) {
      this.getProjectList();
    }
  },
  computed: {
    isUatEnv() {
      return this.$store.getters.env === "uat";
    },
  },
  mounted() {},
  methods: {
    handleCommand(val) {
      if (val === "logout") {
        this.logout();
      }
    },
    visibleChange(val) {
      this.iconClass = val ? "el-icon-arrow-up" : "el-icon-arrow-down";
    },
    //切换环境
    changeEnv(value) {
      if (value === this.publicEnv) return;
      this.publicEnv = value;
      this.$store.dispatch("global/setEnv", value);
      sessionStorage.setItem("env", value);
      // 清除作业开发缓存数据
      localStorage.removeItem("openFiles");
      setTimeout(() => {
        // 详情页跳转回列表页
        const detailBack = this.$route.meta.detailBack;
        if (detailBack) {
          let query = {};
          if (detailBack === "SourceManage") {
            const { projectId, projectName, projectCode } = this.$route.query;
            query = {
              projectId,
              projectName,
              projectCode,
            };
          }
          this.$router
            .push({
              name: detailBack,
              query,
            })
            .then(() => {
              // 需要页面跳转完成后刷新当前页
              window.__intercept__ = 0;
              window.location.reload();
            });
        } else {
          //  window.__isUserClosed__ = true;
          // __intercept__为0时，不会触发beforeunload事件所以在这里设置__isUserClosed__是没效果的
          window.__intercept__ = 0;
          window.location.reload();
        }
      }, 20);
    },
    // 获取项目列表
    async getProjectList() {
      this.projectName = decodeURIComponent(
        decodeURIComponent(this.$route.query.projectName)
      );
      let res = await this.$http.post(
        "/project/projectManagement/getProjects",
        {}
      );
      if (res.code === 0) {
        this.projectData = res.data;
        this.httpData = JSON.parse(JSON.stringify(res.data));
      }
    },
    // 选中
    handleSelectChange(value) {
      const findItem = this.projectData.filter((item) => {
        return String(value) === String(item.id);
      });
      if (findItem && findItem.length) {
        const query = {
          //预留query
          // version: 1
          projectId: findItem[0].id,
          projectName: encodeURIComponent(findItem[0].projectName),
          projectCode: findItem[0].projectCode,
        };
        let name = this.$route.name;
        if (this.$route.name === "SourceManage_historyVersion") {
          name = "SourceManage";
        }
        this.$router.push({
          name: name,
          query: query,
        });
        this.$bus.$emit("changeProject", query);
      }
    },
    fetchProject(value) {
      let projectList = this.httpData.filter(
        (item) => item.projectName.search(value) != -1
      );
      this.projectData = projectList;
    },
    // 失去焦点获取全量列表
    handleSelectBlur() {
      this.fetchProject("");
    },
    async logout() {
      await this.$http.post("/auth/open/logout");
      logout()
    },
  },
};
</script>

<style lang="scss" scoped>
/deep/ .el-dropdown-menu__item {
  font-size: 12px !important;
}
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 40px;
  line-height: 40px;
  padding: 0 20px;
  color: #2e2c37;
  background: #fff;
  .left {
    display: flex;
    align-items: center;
    .project-container {
      margin-left: 8px;
      height: 100%;
      font-size: 12px;
      .name {
        width: 60px;
        height: 100%;
        text-align: center;
      }
      .project-select-wrap {
        /deep/ .el-input,
        /deep/ .el-select-dropdown__item {
          font-size: 12px !important;
        }
        /deep/ .el-input__inner {
          border: 0;
          width: 200px !important;
          padding-left: 10px !important;
          font-size: 12px !important;
        }
      }
    }
  }
  .right {
    display: flex;
    align-items: center;
    .action-container {
      display: flex;
      align-items: center;
      margin-left: 15px;
    }
  }
}

.action-wrap {
  display: flex;
  align-items: center;
  cursor: pointer;
  .info {
    color: #2e2c37;
    display: flex;
    align-items: center;
    img {
      width: 24px;
      height: 24px;
      margin-right: 4px;
    }
    p {
      font-weight: 400;
      font-size: 14px;
      line-height: 14px;
    }
  }
  .login-user-dropdown-icon {
    color: #2e2c37;
    margin-left: 6px;
  }
  .login-user-dropdown-icon {
    color: #2e2c37;
    margin-left: 6px;
  }
}
.menu-wrap {
  /deep/ .el-dropdown-menu__item {
    padding: 0 !important;
    width: 100px;
    &:hover {
      background: #fff !important;
    }
  }
  /deep/ .el-dropdown-menu__item--divided:before {
    margin: 0;
    height: 0;
  }
  .logout_btn {
    height: 34px;
    line-height: 34px;
    font-size: 12px;
    width: 100%;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    .dropdown-text {
      padding: 0 10px;
      border-radius: 4px;
      &:hover {
        background: #ecf5ff;
      }
    }
    &.logout {
      &:hover {
        background: #ecf5ff;
      }
    }
  }
  /deep/ .ant-scroll-number {
    transform: scale(0.7);
    // transform-origin: 100% -150%;
  }
}
</style>
