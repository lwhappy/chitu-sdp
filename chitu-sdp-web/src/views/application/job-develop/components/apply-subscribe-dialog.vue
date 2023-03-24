<!--
 * @description: 
 * @Author: lijianguo19
 * @Date: 2022-09-09 11:22:35
 * @FilePath: \src\views\application\job-develop\components\apply-subscribe-dialog.vue
-->
<template>
  <a-modal class="add-topic-dialog"
           v-model="modalVisible"
           v-if="modalVisible"
           :mask-closable="false"
           title="提示"
           width="600px"
           @cancel="handleCancel"
           v-drag>
    <p class="tip">Topic的订阅申请已经提交至相应的Topic负责人审核，请耐心等待审核</p>
    <data-hub-table :columns="columns"
                    :dataSource="dataList"
                    :scroll="{ y: '360px' }"
                    rowKey="id"
                    :autoHight="false">
    </data-hub-table>
    <template slot="footer">
      <a-button size="small"
                @click="handleCancel">
        关闭
      </a-button>

      <a-button type="primary"
                size="small"
                @click="gotoApproveTopic(null)">
        查看审核进度
      </a-button>
    </template>

  </a-modal>
</template>

<script>
  const columns = (vm) => {
    return [
      {
        dataIndex: 'oaNumber',
        title: '单号',
        width: 120,
        customRender: (text) => {
          return {
            children: vm.$createElement('span', {
              on: {
                click: () => {
                  vm.gotoApproveTopic(text)
                }
              },
              style: {
                color: '#0066ff',
                cursor: 'pointer'
              },
            }, text)
          }
        }
      },
      {
        dataIndex: 'topic',
        title: 'Topic',
        width: 120,
        customRender: (text, record) => {
          return {
            children: vm.$createElement(
              'a-tooltip',
              {
                attrs: { title: vm.resolveTopic(record), placement: 'topLeft' }
              },
              [
                vm.$createElement('span', {
                  domProps: { innerHTML: vm.resolveTopic(record) }
                })
              ]
            )
          }
        }
      },
      {
        dataIndex: 'adminNames',
        title: 'Topic负责人',
        width: 120,
        customRender: (text, record) => {
          return {
            children: vm.$createElement(
              'a-tooltip',
              {
                attrs: { title: vm.resolveAdminNames(record), placement: 'topLeft' }
              },
              [
                vm.$createElement('span', {
                  domProps: { innerHTML: vm.resolveAdminNames(record) }
                })
              ]
            )
          }
        }
      },
    ]
  }
  export default {
    name: '',
    components: {},
    data () {
      return {
        modalVisible: false,
        dataList: [],
        columns: columns(this)
      };
    },
    computed: {},
    watch: {},
    created () { },
    mounted () { },
    methods: {
      async open (data) {
        this.dataList = data
        this.modalVisible = true
      },
      handleCancel () {
        this.modalVisible = false
      },
      gotoApproveTopic (id) {
        let query = {}
        if (id) {
          query.id = id
        }
        this.$router.push({
          name: 'ApproveTopic',
          query: query
        })
        this.handleCancel()
      },
      resolveTopic (data) {
        const arr = JSON.parse(data.subscriptionInfo)
        if (!arr.length) return ''
        const result = []
        arr.forEach(item => {
          result.push(item.topic)
        });
        return result.join(',')
      },
      resolveAdminNames (data) {
        const arr = JSON.parse(data.subscriptionInfo)
        if (!arr.length) return ''
        //截取数组第一列
        const itemObj = arr[0]
        const result = []
        if (itemObj.adminNames == 'auths') {
          result.push('-')
        } else {
          result.push(itemObj.adminNames)
        }
        return result.join(',')
      }
    },
  }
</script>
<style lang='scss' scoped>
  .tip {
    margin-bottom: 10px;
  }
</style>