<template>
  <!-- 一定要用v-if="isShowAddProject"摧毁dom重新渲染，不然会有莫名其妙的问题 -->
  <a-modal
    v-if="isShowAddProject"
    wrapClassName="add-project-dialog"
    v-model="isShowAddProject"
    :mask-closable="false"
    :footer="null"
    :title="title"
    v-drag
    width="600px"
  >
    <!-- <span slot="closeIcon">x</span> -->
    <div class="add-project" v-loading="isLoading">
      <a-form-model :model="form">
        <div class="form-body">
          <!-- 编辑 -->
          <template v-if="!isNew">
            <a-row>
              <a-col :span="12">
                <div class="edit-row">
                  <p class="label"><span class="red">*</span>项目名称</p>
                  <span class="item">{{ form.projectName }}</span>
                </div>
              </a-col>
              <a-col :span="12">
                <div class="edit-row">
                  <p class="label"><span class="red">*</span>项目编码</p>
                  <span class="item">{{ form.projectCode }}</span>
                </div>
              </a-col>
            </a-row>
          </template>
          <template v-else>
            <a-form-model-item>
              <p class="label"><span class="red">*</span>项目名称</p>
              <a-input
                v-model="form.projectName"
                :disabled="!isNew"
                v-decorator="[
                  'projectName',
                  { rules: [{ required: true, message: '请输入项目名称' }] },
                ]"
                placeholder="请输入项目名称"
              />
            </a-form-model-item>
            <a-form-model-item>
              <p class="label"><span class="red">*</span>项目编码</p>
              <a-input
                v-model="form.projectCode"
                :disabled="!isNew"
                v-decorator="[
                  'projectCode',
                  { rules: [{ required: true, message: '请输入项目编码' }] },
                ]"
                placeholder="请输入项目编码"
              />
            </a-form-model-item>
          </template>

          <a-form-model-item>
            <p class="label"><span class="red">*</span>项目负责人</p>
            <a-select
              placeholder="请输入姓名/工号"
              show-search
              label-in-value
              :default-value="leader"
              :filter-option="false"
              :not-found-content="isFetchingMember ? undefined : null"
              @search="handleLeaderSearch"
              @change="handleChangeLeader"
            >
              <a-spin
                v-if="isFetchingMember"
                slot="notFoundContent"
                size="small"
              />
              <a-select-option
                v-for="(item, index) in leaderData"
                :value="JSON.stringify(item)"
                :key="String(item.employeeNumber) + index"
              >
                {{ item.name }}
              </a-select-option>
            </a-select>
            <!-- <a-auto-complete placeholder="请输入姓名/工号"
                             v-model="leader"
                             @select="onSelectLeader"
                             @search="handleLeaderSearch">
              <template slot="dataSource">
                <a-select-option v-for="(item,index) in leaderData"
                                 :originalData="item"
                                 :key="String(item.employeeNumber) + index">
                  {{ item.name }}
                </a-select-option>
              </template>
            </a-auto-complete> -->
          </a-form-model-item>
          <a-form-model-item>
            <p class="label"><span class="red">*</span>添加计算引擎</p>
            <a-select
              mode="multiple"
              ref="selectEngine"
              placeholder="请选择计算引擎"
              label-in-value
              v-model="engine"
              @search="searchEngine"
              :disabled="!isAdmin"
              @change="handleChangeEngine"
            >
              <div slot="dropdownRender" slot-scope="menu">
                <v-nodes :vnodes="menu" />
                <a-divider style="margin: 4px 0" />
                <div
                  style="
                    padding: 4px 8px 8px;
                    display: flex;
                    justify-content: flex-end;
                  "
                >
                  <a-button
                    type="primary"
                    size="small"
                    @click.stop.prevent="selectEngineConfirm"
                    >确认</a-button
                  >
                </div>
              </div>
              <a-select-option
                v-for="(item, index) in engineData"
                :key="String(item.id) + index"
                :value="JSON.stringify(item)"
              >
                {{ item.engineName }}
              </a-select-option>
            </a-select>
          </a-form-model-item>
          <a-form-model-item>
            <p class="label">
              <span class="red">*</span> 项目等级
              <img
                class="question"
                width="14"
                height="14"
                src="@/assets/icons/ask.png"
                alt=""
                @click="openDrawer"
              />
            </p>
            <a-select
              v-model="defaultPriority"
              label-in-value
              @select="handerLevelChange"
            >
              <a-select-option
                v-for="item in priorityList"
                :value="JSON.stringify(item)"
                :key="item.label"
                >{{ priorityPrefix }}{{ item.label }}</a-select-option
              >
            </a-select>
          </a-form-model-item>
          <a-form-model-item>
            <p class="label"><span class="red">*</span>选择业务线</p>
            <a-select
              v-model="form.productLineName"
              @focus="getProductLineNameList"
              @select="handerProductChange"
            >
              <a-select-option
                v-for="item in productLineNameList"
                :key="item.code"
              >
                {{ item.name }}
              </a-select-option>
            </a-select>
          </a-form-model-item>

          <a-form-model-item>
            <p class="label">添加成员</p>
            <a-select
              mode="multiple"
              placeholder="请输入姓名或工号"
              label-in-value
              :default-value="member"
              :filter-option="false"
              :not-found-content="isFetchingMember ? undefined : null"
              @search="searchMember"
              @change="handleChangeMember"
            >
              <a-spin
                v-if="isFetchingMember"
                slot="notFoundContent"
                size="small"
              />
              <a-select-option
                v-for="(item, index) in memberData"
                :value="JSON.stringify(item)"
                :key="String(item.employeeNumber) + index"
              >
                {{ item.name }}
              </a-select-option>
            </a-select>
          </a-form-model-item>
          <a-form-model-item>
            <a-row style="line-height: normal">
              <a-col :span="14">
                <a-checkbox
                  v-model="form.forbidUdxUpdation"
                  :disabled="!isAdmin"
                >
                  禁止UDX资源同一版本内更新
                </a-checkbox>
              </a-col>
              <a-col :span="10">
                <i class="chitutree-h5 chitutreeicon_warning_tips"></i>
                更新同一版本内的jar包会影响引用此jar包的所有作业，建议禁用此功能
              </a-col>
            </a-row>
          </a-form-model-item>
          <a-form-model-item style="line-height: normal">
            <a-row style="line-height: normal">
              <a-col :span="24">
                <a-checkbox v-model="form.allowJobAddEdit" :disabled="!isAdmin">
                  允许生产环境新增作业与编辑作业代码
                </a-checkbox>
              </a-col>
            </a-row>
          </a-form-model-item>
        </div>
        <div class="footer justify-end">
          <a-button @click="cancelEvent">取消</a-button>
          <a-button
            style="margin-left: 8px"
            @click="confirmEvent"
            type="primary"
            >保存</a-button
          >
        </div>
      </a-form-model>
    </div>
    <drawer
      ref="drawer"
      :drawerVisible="drawerVisible"
      @closeDrawer="drawerVisible = false"
    />
  </a-modal>
</template>

<script>
import drawer from "@/components/priority-drawer.vue";
import _ from "lodash";

export default {
  components: {
    VNodes: {
      functional: true,
      render: (h, ctx) => ctx.props.vnodes,
    },
    drawer,
  },
  data() {
    return {
      drawerVisible: false,
      isLoading: false,
      isShowAddProject: false,
      selectEngine: [],
      engine: [],
      initEngine: [],
      engineData: [],
      leaderData: [],
      leader: "",
      memberData: [],
      selectLeader: {},
      selectMember: [],
      productLineNameList: [
        {
          id: "1",
          name: "CRM-经营分析",
          code: "CRM_BA",
        },
        {
          id: "2",
          name: "CRM-经营分析2",
          code: "CRM_BA2",
        },
      ], //业务线
      member: [],
      form: {
        projectName: "",
        projectCode: "",
        productLineName: "",
        productLineCode: "",
        priority: "",
        forbidUdxUpdation: true,
        allowJobAddEdit: false,
      },
      isFetchingMember: false,
      isNew: true,
      title: "",
      projectId: "",
      priorityPrefix: "", //项目等级前缀
      priorityList: [],
      defaultPriority: "",
    };
  },
  props: {},
  computed: {
    isAdmin() {
      return this.$store.getters.userInfo.isAdmin == 1;
    },
    isProdEnv() {
      return this.$store.getters.env === "prod";
    },
  },
  watch: {
    // isShowAddProject: {
    //   handler (value) {
    //     if (value) {
    //       this.getEngine()
    //     }
    //   }
    // }
  },
  methods: {
    open(data) {
      this.drawerVisible = false;
      // Object.assign(this.$data, this.$options.data())
      this.isLoading = false;
      this.selectEngine = [];
      this.engine = [];
      this.initEngine = [];
      this.engineData = [];
      this.leaderData = [];
      this.leader = "";
      this.memberData = [];
      this.selectLeader = {};
      this.selectMember = [];
      this.member = [];
      this.form = {
        projectName: "",
        projectCode: "",
        productLineName: "",
        productLineCode: "",
        priority: "",
        forbidUdxUpdation: true,
        allowJobAddEdit: false,
      };
      this.isFetchingMember = false;
      this.isFetchingProduct = false;
      this.isShowAddProject = true;
      this.isNew = data.isNew;
      this.title = data.title;
      this.defaultPriority = "";
      if (!this.isNew) {
        //编辑
        this.projectId = data.data.id;
        this.form.projectName = data.data.projectName;
        this.form.projectCode = data.data.projectCode;
        this.form.productLineName = data.data.productLineName;
        this.form.productLineCode = data.data.productLineCode;
        this.form.priority = data.data.priority;
        this.form.forbidUdxUpdation =
          data.data.forbidUdxUpdation == 1 ? true : false;
        this.form.allowJobAddEdit =
          data.data.allowJobAddEdit == 1 ? true : false;
        if (data.data.projectOwner && data.data.projectOwner.length) {
          this.selectLeader = data.data.projectOwner[0];
          this.selectLeader.name = this.selectLeader.userName;
        }
        let projectEngines = data.data.projectEngines;
        let projectUsers = data.data.projectUsers;
        projectUsers = projectUsers.filter((item) => {
          return item.employeeNumber;
        });
        projectUsers = projectUsers.map((item) => {
          item.name = item.userName;
          return item;
        });
        if (this.selectLeader.employeeNumber) {
          this.leader = {
            key: JSON.stringify(this.selectLeader),
            label: this.selectLeader.name,
          };
        }
        // if (projectEngines && projectEngines.length) {
        //   this.selectEngine = JSON.parse(JSON.stringify(projectEngines))
        //   projectEngines = projectEngines.map(item => {
        //     if (item) {
        //       return { key: JSON.stringify(item), label: item.engineName }
        //     } else {
        //       return ''
        //     }
        //   })
        //   this.engine = projectEngines
        // }
        if (projectUsers && projectUsers.length) {
          // projectUsers = projectUsers.filter(item => {//过滤掉自己
          //   return String(item.employeeNumber) !== String(this.$store.getters.userInfo.employeeNumber)
          // })
          this.selectMember = JSON.parse(JSON.stringify(projectUsers));
          projectUsers = projectUsers.map((item) => {
            if (item) {
              return { key: JSON.stringify(item), label: item.name };
            } else {
              return "";
            }
          });
          this.member = projectUsers;
        }
        this.initEngine = projectEngines;
      } else {
        this.selectLeader = {
          // id: this.$store.getters.userInfo.id,
          name: this.$store.getters.userInfo.userName,
          employeeNumber: this.$store.getters.userInfo.employeeNumber,
          // isAdmin: 1
        };
        this.leader = {
          key: JSON.stringify(this.selectLeader),
          label: this.$store.getters.userInfo.userName,
        };
      }
      this.getEngine(); //新增和编辑都要初始化
      this.initPriority();
    },
    initPriority() {
      //等级初始化
      const globalPriority = this.$common.getPriority();
      this.priorityPrefix = globalPriority.labelPrefix;
      this.priorityList = globalPriority.list;
      const defaultItem = this.priorityList.find((item) => {
        if (this.isNew) {
          //新增
          return item.isDefault;
        } else {
          //编缉
          if (this.form.priority !== "") {
            return item.value.toString() === this.form.priority.toString();
          } else {
            return item.isDefault;
          }
        }
      });
      if (defaultItem && defaultItem.value) {
        let defaultPriority = {
          key: JSON.stringify(defaultItem),
          label: defaultItem.label,
        };
        this.defaultPriority = defaultPriority;
        this.form.priority = defaultItem.value;
      }
      //等级初始化end
    },
    close() {
      this.isShowAddProject = false;
    },
    confirmEvent: _.debounce(function () {
      console.log("debounce");
      this.addSubmit();
    }, 500),
    cancelEvent() {
      this.isShowAddProject = false;
    },
    selectEngineConfirm() {
      this.$refs.selectEngine.blur();
    },
    handleChangeEngine(value) {
      this.selectEngine = [];
      value.forEach((item) => {
        this.selectEngine.push(JSON.parse(item.key));
      });

      // nodes.forEach(item => {
      //   const engine = item.data.attrs['originalData']
      //   this.selectEngine.push(engine)
      // })
    },
    handleChangeMember(value) {
      this.selectMember = [];
      value.forEach((item) => {
        this.selectMember.push(JSON.parse(item.key));
      });
    },
    handleChangeLeader(value) {
      this.selectLeader = JSON.parse(value.key);
    },
    searchEngine(value) {
      // console.log('searchEngine', value)
      this.getEngine(value);
    },
    searchMember(value) {
      this.getMember(value);
    },
    handleLeaderSearch(value) {
      // console.log('handleEmployeeSearch', value)
      this.getLeader(value);
    },
    handerProductChange(value) {
      let selectItem = this.productLineNameList.find(
        (item) => item.code === value
      );
      this.form.productLineCode = selectItem.code;
      this.form.productLineName = selectItem.name;
    },
    // 添加业务线
    async getProductLineNameList() {
      let res = await this.$http.get(
        "/project/projectManagement/queryBusinessLine"
      );
      if (res.code === 0) {
        this.productLineNameList = res.data;
      } else {
        this.$message.error(res.msg);
      }
    },
    async getEngine(value) {
      const params = {
        engineName: value || "", //引擎名称
      };
      if (!this.isNew) {
        params.projectId = this.projectId;
      }
      this.isLoading = true;
      let res = await this.$http.post(
        "/project/projectManagement/getEngineByName",
        params
      );
      this.isLoading = false;
      if (res.code === 0) {
        this.engineData = res.data;
        if (this.initEngine.length) {
          const selectEngine = [];
          this.engineData.forEach((item) => {
            const findItem = this.initEngine.filter((innerItem) => {
              return String(item.id) === String(innerItem.id);
            });
            if (findItem.length) {
              selectEngine.push(item);
            }
          });
          if (selectEngine && selectEngine.length) {
            this.selectEngine = JSON.parse(JSON.stringify(selectEngine));
            const projectEngines = selectEngine.map((item) => {
              if (item) {
                return { key: JSON.stringify(item), label: item.engineName };
              } else {
                return "";
              }
            });
            this.engine = projectEngines;
          }
        }
      } else {
        this.$message.error(res.msg);
      }
    },
    async getLeader(value) {
      const params = {
        nameOrNumber: value || "", //引擎名称
      };
      let res = await this.$http.post(
        "/setting/userSetting/getHREmployee",
        params
      );
      if (res.code === 0) {
        this.leaderData = res.data;
      }
    },
    async getMember(value) {
      const params = {
        nameOrNumber: value || "", //引擎名称
      };
      this.isFetchingMember = true;
      let res = await this.$http.post(
        "/setting/userSetting/getHREmployee",
        params
      );
      this.isFetchingMember = false;
      if (res.code === 0) {
        this.memberData = res.data;
      }
    },
    async getProduct(value) {
      const params = {
        nameOrNumber: value || "", //引擎名称
      };
      this.isFetchingMember = true;
      let res = await this.$http.post(
        "/setting/userSetting/getHREmployee",
        params
      );
      this.isFetchingMember = false;
      if (res.code === 0) {
        this.getProduct = res.data;
      }
    },
    onSelectLeader(value, item) {
      this.selectLeader = item.data.attrs["originalData"];
    },
    async addSubmit() {
      if (this.form.projectName === "") {
        this.$message.warning("项目名称不能为空");
        return;
      }
      if (this.form.projectCode === "") {
        this.$message.warning("项目编码不能为空");
        return;
      }
      if (!this.selectLeader.name) {
        this.$message.warning("项目负责人不能为空");
        return;
      }
      if (this.selectEngine.length === 0) {
        this.$message.warning("计算引擎不能为空");
        return;
      }
      if (this.form.priority == "") {
        this.$message.warning("项目等级不能为空");
        return;
      }
      if (this.form.productLineName === "") {
        this.$message.warning("业务线不能为空");
        return;
      }
      const selectLeader = JSON.parse(JSON.stringify(this.selectLeader));
      let selectMember = JSON.parse(JSON.stringify(this.selectMember));
      selectLeader.userName = selectLeader.name;
      delete selectLeader.name;
      selectMember = selectMember.map((item) => {
        if (item) {
          item.userName = item.name;
          delete item.name;
        }
        return item;
      });
      const params = {
        projectName: this.form.projectName,
        projectCode: this.form.projectCode,
        projectEngines: this.selectEngine,
        projectLeader: [selectLeader],
        projectUsers: selectMember,
        productLineName: this.form.productLineName,
        productLineCode: this.form.productLineCode,
        priority: this.form.priority,
        forbidUdxUpdation: this.form.forbidUdxUpdation ? "1" : "0",
        allowJobAddEdit: this.form.allowJobAddEdit ? "1" : "0",
      };
      if (this.isNew) {
        let res = await this.$http.post(
          "/project/projectManagement/add",
          params
        );
        if (res.code === 0) {
          this.$message.success("添加成功");
          this.$emit("addSuccess");
          this.isShowAddProject = false;
        } else {
          this.$message.error(res.msg);
        }
      } else {
        params.id = this.projectId;
        let res = await this.$http.post(
          "/project/projectManagement/update",
          params,
          {
            headers: {
              projectId: this.projectId,
            },
          }
        );
        if (res.code === 0) {
          this.$message.success("修改成功");
          this.$emit("addSuccess");
          this.isShowAddProject = false;
        } else {
          this.$message.error(res.msg);
        }
      }
      //生产环境，同步更新允许生产环境新增作业与编辑作业代码
      if (this.isProdEnv) {
        this.$bus.$emit("allowJobAddEdit");
      }
    },
    handerLevelChange(data) {
      const key = JSON.parse(data.key);
      this.form.priority = key.value;
    },
    openDrawer() {
      this.drawerVisible = true;
    },
  },
  mounted() {},
};
</script>

<style lang="scss" scoped>
/deep/ .ant-modal-body {
  padding: 0;
  .add-project {
    .form-body {
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
    .footer {
      height: 44px;
      border-top: 1px solid #ddd;
      padding-right: 16px;
    }
  }
}
</style>