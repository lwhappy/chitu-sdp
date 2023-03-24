<!--
 * @Author: hjg
 * @Date: 2021-11-13 16:13:45
 * @LastEditTime: 2022-06-24 11:36:21
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\views\system-setting\components\engineDetail.vue
-->
<template>
  <a-modal class="engine-detail"
           v-model="isShowEngineDetailDialog"
           :mask-closable="false"
           title="引擎详情"
           :footer="null"
           width="600px">
    <!-- 修改弹框右上角关闭图标 -->
    <template slot="closeIcon"><i class="chitutree-h5 chitutreeguanbi"></i></template>
    <div class="content">
      <div class="item">
        <span>引擎名称:</span>
        <p>{{ engineInfo.engineName }}</p>
      </div>
      <div class="item">
        <span>集群部署类型:</span>
        <p>{{ engineInfo.engineType }}</p>
      </div>
      <chitu-table :columns="columns"
                 row-key="id"
                 :autoHight="false"
                 :data-source="tableData"
                 :scroll="{ y: '200px' }">
      </chitu-table>
    </div>
  </a-modal>
</template>
<script>
  const columns = (vm) => {
    if (vm.engineInfo.engineType == 'yarn') {
      return [
        {
          title: '系统环境',
          width: 180,
          dataIndex: 'env'
        },
        {
          title: '引擎集群',
          width: 180,
          dataIndex: 'clusterName',
          customRender: (text, record) => {
            return {
              children: vm.$createElement('span', {

              }, record.env == 'UAT环境' ? record.uatClusterName : record.clusterName)
            }
          }
        },
        {
          title: '引擎队列',
          width: 180,
          dataIndex: 'engineQueue',
          customRender: (text, record) => {
            return {
              children: vm.$createElement('span', {

              }, record.env == 'UAT环境' ? record.uatEngineQueue : record.engineQueue)
            }
          }
        },
      ]
    } else {
      return [
        {
          title: '系统环境',
          width: 180,
          dataIndex: 'env'
        },
        {
          title: '引擎集群',
          width: 180,
          dataIndex: 'clusterName',
          customRender: (text, record) => {
            return {
              children: vm.$createElement('span', {

              }, record.env == 'UAT环境' ? record.uatClusterName : record.clusterName)
            }
          }
        },
        {
          title: 'namespace',
          width: 180,
          dataIndex: 'namespace',
          customRender: (text, record) => {
            return {
              children: vm.$createElement('span', {

              }, record.env == 'UAT环境' ? record.uatNamespace : record.namespace)
            }
          }
        },
      ]
    }

  }
  export default {
    data () {
      return {
        engineInfo: {
          engineName: null,
          engineUrl: null,
          engineVersion: null
        },
        columns: [],
        tableData: [], // table数据
        isShowEngineDetailDialog: false
      }
    },
    methods: {
      // 打开引擎详情
      open (engineInfo) {
        this.isShowEngineDetailDialog = true
        this.engineInfo = engineInfo
        this.columns = columns(this)
        this.tableData = [
          {
            id: 1,
            env: 'UAT环境',
            uatClusterName: engineInfo.uatClusterName,
            clusterName: engineInfo.clusterName,
            uatEngineQueue: engineInfo.uatEngineQueue,
            engineQueue: engineInfo.engineQueue,
            uatNamespace: engineInfo.uatNamespace,
            namespace: engineInfo.namespace,
          },
          {
            id: 2,
            env: '生产环境',
            uatClusterName: engineInfo.uatClusterName,
            clusterName: engineInfo.clusterName,
            uatEngineQueue: engineInfo.uatEngineQueue,
            engineQueue: engineInfo.engineQueue,
            uatNamespace: engineInfo.uatNamespace,
            namespace: engineInfo.namespace,
          }
        ]
      }
    }
  }
</script>
<style lang="scss" scoped>
  .engine-detail {
    width: 100%;
    height: 100%;
    /deep/ .ant-select {
      font-size: 12px;
    }
    /deep/ .ant-modal-body {
      padding: 0;
    }
    .content {
      width: 100%;
      min-height: 155px;
      max-height: 400px;
      padding: 12px 16px;
      .item {
        width: 100%;
        font-size: 12px;
        margin-bottom: 8px;
        span {
          font-weight: 700;
          display: inline-block;
          width: 100px;
          position: absolute;
        }
        p {
          display: inline-block;
          width: calc(100% - 100px);
          margin-left: 100px;
        }
        .engine-url {
          max-height: 52px;
          overflow-y: auto;
        }
      }
    }
  }
</style>
