<!--
 * @Author: hjg
 * @Date: 2021-11-13 16:11:32
 * @LastEditTime: 2022-07-06 15:42:33
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\system-setting\components\engineProjects.vue
-->
<template>
  <a-modal class="engine-projects"
           v-model="isShowProjectsDialogModal"
           :mask-closable="false"
           title="项目引用数"
           :footer="null"
           :after-close="init"
           :destroy-on-close="true"
           @cancel="closeProjectModal"
           width="600px">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <div class="content">
      <!-- 项目名称搜素 -->
      <div class="data-search">
        <a-auto-complete class="auto-complete-restyle"
                         v-model="searchValue"
                         ref="autoComplete"
                         :data-source="dataSource"
                         :placeholder="inputMsg"
                         @search="onSearch">
          <template slot="dataSource">
            <a-select-option v-for="(item, index) in dataSource"
                             :key="'auto-search' + index"
                             :originData="item"
                             :title="item.text">
              {{ item.text }}
            </a-select-option>
          </template>
        </a-auto-complete>
      </div>
      <!-- 数据展示 -->
      <chitu-table :columns="columns"
                 row-key="id"
                 :autoHight="false"
                 :data-source="tableData"
                 :loading="loading"
                 :scroll="{y: '400px'}">>
        <template #projectName="{record}">
          <div class="project-name">
            <a-tooltip placement="topLeft">
              <template slot="title">
                <span>{{record.projectName}}</span>
              </template>
              <span>{{ record.projectName }}</span>
            </a-tooltip>
          </div>
        </template>
      </chitu-table>
    </div>
  </a-modal>
</template>
<script>
  export default {
    props: {
      isShowProjectsDialog: {
        type: Boolean,
        default: false
      },
      engineInfo: {
        type: Object,
        default: () => {
          return {}
        }
      }
    },
    data () {
      return {
        isShowProjectsDialogModal: false,
        searchValue: null,
        // isShowProjectsDialog: false,
        inputMsg: '请输入项目名称',
        // engineInfo: {},
        columns: [{ // 表列名
          title: '项目名称',
          dataIndex: 'projectName',
          width: 175,
          scopedSlots: { customRender: 'projectName' }

        }, {
          title: '项目负责人',
          width: 175,
          dataIndex: 'projectOwner'
        }],
        dataSource: [],
        loading: false,
        tableData: [],
        httpData: []
      }
    },
    mounted () {
      this.open(this.engineInfo)
    },
    methods: {
      // 打开弹框
      open (engineInfo) {
        this.isShowProjectsDialogModal = JSON.parse(JSON.stringify(this.isShowProjectsDialog))
        this.engineInfo = engineInfo
        let params = {
          id: engineInfo.id
        }
        this.getEngineProjects(params)
      },
      // 获取引擎使用项目
      async getEngineProjects (params) {
        let res = await this.$http.post('/setting/engineSetting/engineProjects', params)
        if (res.code === 0) {
          this.httpData = res.data
          this.tableData = res.data
        }
      },
      // 搜索
      onSearch (searchText) {
        // // console.log('--------search:', searchText)
        this.tableData = this.httpData.filter(item => item.projectName.search(searchText) != -1)
      },
      init () {
        this.tableData = []
        this.searchValue = null
      },
      // 关闭弹框回调
      closeProjectModal () {
        this.$emit('closeProjectModal')
      }
    }
  }
</script>
<style lang="scss" scoped>
  .engine-projects {
    width: 100%;
    height: 100%;
    // /deep/ .ant-modal-content {
    //   border-radius: 0;
    // }
    /deep/ .ant-select {
      font-size: 12px;
    }
    // /deep/ .ant-modal-header {
    //   padding: 11px 16px;
    //   border-radius: 0;
    //   .ant-modal-title {
    //     font-weight: 700;
    //   }
    // }
    // /deep/ .ant-modal-close-x {
    //   width: 44px;
    //   height: 44px;
    //   line-height: 44px;
    // }
    /deep/ .ant-modal-body {
      padding: 0;
    }
    .content {
      min-height: 328px;
      max-height: 600px;
      padding: 12px 16px;
      .data-search {
        height: 28px;
        margin-bottom: 12px;
        /deep/ .ant-select {
          width: 240px;
        }
        /deep/ .ant-input {
          // border-radius: 0;
          height: 28px;
          width: 100%;
          // border-color: #d9d9d9;
          // &:focus,
          // &:hover {
          //   border-color: #d9d9d9 !important;
          //   box-shadow: none;
          // }
        }
        // /deep/ .ant-input-suffix {
        //   right: 0;
        // }
        /deep/ .btn {
          background: #006eff;
          width: 28px;
          height: 28px;
          border: none;
          cursor: pointer;
          color: #fff;
          i {
            font-size: 12px;
          }
        }
      }
      // /deep/ .ant-table-thead {
      //   height: 32px;
      //   th {
      //     padding: 0 0 0 2px;
      //     font-size: 12px;
      //     color: #000;
      //     line-height: 40px;
      //     height: 40px;
      //     overflow: hidden;
      //   }
      // }
      // /deep/ .ant-table-tbody > tr > td {
      //   padding: 0 0 0 2px;
      //   font-size: 12px;
      //   color: #000;
      //   line-height: 40px;
      //   height: 40px;
      //   overflow: hidden;
      //   .user-name {
      //     color: #ccc;
      //     margin-right: 8px;
      //   }
      // }
      // /deep/ .ant-table-wrapper {
      //   height: calc(100% - 36px);
      //   .ant-spin-nested-loading {
      //     height: 100%;
      //     .ant-spin-container {
      //       height: 100%;
      //       .ant-table {
      //         height: 100%;
      //         .ant-table-content {
      //           height: 100%;
      //           .ant-table-scroll {
      //             height: 100%;
      //             .ant-table-body {
      //               // height: 100%;
      //             }
      //           }
      //         }
      //       }
      //     }
      //   }
      // }
      .project-name {
        width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-right: 4px;
      }
    }
  }
</style>
