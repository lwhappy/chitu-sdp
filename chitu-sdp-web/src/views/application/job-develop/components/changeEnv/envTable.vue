<template>
  <div class="container">
    <chitu-table v-loading="isLoading"
               :columns="columns"
               :dataSource="tableData"
               rowKey="id"
               fixedHeight
               :scroll="{ y: '200px' }"
               :autoHight="false">
      <template #tableArrow>
        <img class="arrow-icon"
             src="@/assets/icons/env-arrow.png"
             alt="">
      </template>
      <!-- 生产环境数据源实例 -->
      <template #prodDataSourceIdTitle>
        <span class="justify-start">
          <span style="margin-right:5px;">数据源实例</span>
          <a-tooltip placement="topLeft">
            <template slot="title">
              <p>1.默认匹配同一名称的生产环境数据源实例与UAT环境数据源实例，若无法默认带出，则需手动匹配;</p>
              <p>2.若无数据源实例，则需进入生产环境的“实时开发-数据源管理”页面，新建数据源实例</p>
            </template>
            <img class="pointer"
                 width="14"
                 height="14"
                 src="@/assets/icons/ask.png"
                 alt="">
          </a-tooltip>
        </span>
      </template>
      <template #prodDataSourceId="{record}">
        <div style="width:100%;">
          <span class="red">*</span>
          <a-select v-model="record.prodDataSourceId"
                    :ref="`select${record.tableIndex}`"
                    style="width:calc(100% - 10px)"
                    placeholder="请选择"
                    @focus="handleFocus(record)"
                    @change="(a,b)=>handleChange(a,b,record)">
            <a-select-option v-for="item in record.prodDataSourceNameList"
                             :value="item.id"
                             :key="item.id">
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>{{item.dataSourceName}}</span>
                </template>
                <span>{{item.dataSourceName}}</span>
              </a-tooltip>
            </a-select-option>
          </a-select>
        </div>
      </template>
      <template #prodMetaTableName="{record}">
        <a-tooltip placement="topLeft">
          <template slot="title">
            <p>{{record.prodMetaTableName}}</p>
          </template>
          <p>{{record.prodMetaTableName}}</p>
        </a-tooltip>
        <p class="text-red"
           v-if="!record.hasProdTable">生产环境表名不存在</p>
      </template>
    </chitu-table>
  </div>
</template>

<script>

  export default {
    name: '',
    components: {},
    props: {
      tableData: {
        type: Array,
        default: () => []
      }
    },
    data () {
      const columns = (vm) => {
        return [
          {
            title: 'UAT环境',
            children: [
              {
                dataIndex: 'uatDataSourceName',
                title: '数据源实例',
                width: 180,
                customRender: (text) => {
                  return {
                    children: vm.$createElement(
                      'a-tooltip',
                      {
                        attrs: { title: text, placement: 'topLeft' }
                      },
                      [
                        vm.$createElement('span', {
                          domProps: { innerHTML: text },
                        })
                      ]
                    )
                  }
                }
              },
              {
                dataIndex: 'uatDataSourceUrl',
                title: '数据源地址',
                width: 180,
                customRender: (text) => {
                  return {
                    children: vm.$createElement(
                      'a-tooltip',
                      {
                        attrs: { title: text, placement: 'topLeft' }
                      },
                      [
                        vm.$createElement('span', {
                          domProps: { innerHTML: text },
                        })
                      ]
                    )
                  }
                }
              },
              {
                dataIndex: 'uatDataSourceType',
                title: '数据库类型',
                width: 100,
              },
              {
                dataIndex: 'uatMetaTableType',
                title: '元表类型',
                width: 100,
              },
              {
                dataIndex: 'uatMetaTableName',
                title: '表名',
                width: 130,
                customRender: (text) => {
                  return {
                    children: vm.$createElement(
                      'a-tooltip',
                      {
                        attrs: { title: text, placement: 'topLeft' }
                      },
                      [
                        vm.$createElement('span', {
                          domProps: { innerHTML: text },
                        })
                      ]
                    )
                  }
                }
              },
            ]
          },
          {
            title: '',
            children: [
              {
                dataIndex: 'tableArrow',
                title: '',
                align: 'center',
                scopedSlots: { customRender: 'tableArrow' },
                width: 90,
              },
            ]
          },

          {
            title: '生产环境',
            children: [
              {
                dataIndex: 'prodDataSourceId',
                scopedSlots: { title: 'prodDataSourceIdTitle', customRender: 'prodDataSourceId' },
                width: 150,
              },
              {
                dataIndex: 'prodDataSourceUrl',
                title: '数据源地址',
                width: 180,
                customRender: (text) => {
                  return {
                    children: vm.$createElement(
                      'a-tooltip',
                      {
                        attrs: { title: text, placement: 'topLeft' }
                      },
                      [
                        vm.$createElement('span', {
                          domProps: { innerHTML: text },
                        })
                      ]
                    )
                  }
                }
              },
              {
                dataIndex: 'prodMetaTableName',
                title: '表名',
                scopedSlots: { customRender: 'prodMetaTableName' },
                width: 130,
              }
            ]
          },
        ]
      }
      return {
        isLoading: false,
        tableIndex: 0,
        focusIndex: 0,
        columns: columns(this),
        mouseListening: false,
      };
    },
    computed: {},
    watch: {},
    created () { },
    mounted () {
      this.initTable()
      this.addListener()
    },
    destroyed () {
      this.removeListener()
    },
    methods: {
      addListener () {
        if (!this.mouseListening) {
          window.addEventListener('mousewheel', this.mousewheelListener)
          this.mouseListening = true
        }
      },
      removeListener () {
        if (this.mouseListening) {
          window.removeEventListener('mousewheel', this.mousewheelListener)
          this.mouseListening = false
        }
      },
      mousewheelListener () {
        if (this.mouseListening) {
          if (this.$refs[`select${this.focusIndex}`]) {
            this.$refs[`select${this.focusIndex}`].blur()
          }
        } else {
          this.$destroy()
        }

      },
      initTable () {
        this.tableData.map((item, index) => {
          //判断生产环境表名
          this.$set(this.tableData[index], 'hasProdTable', true)
          this.$set(this.tableData[index], 'tableIndex', index)
          this.$set(this.tableData[index], 'prodDataSourceId', item.prodDataSourceId || '')
          this.getDataSourcesList(item, index)
        })
      },
      async getDataSourcesList (item, index) {
        let res = await this.$http.post('/dataSource/getDataSources', {
          dataSourceType: item.uatDataSourceType,
          env: 'prod'
        }, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.$set(this.tableData[index], 'prodDataSourceNameList', res.data)
        }
      },
      handleFocus (item) {
        this.focusIndex = item.tableIndex
      },
      handleChange (dataSourceId, b, item) {
        this.tableIndex = item.tableIndex
        const prodDataSourceNameList = item.prodDataSourceNameList
        const data = prodDataSourceNameList.filter((val) => {
          return val.id == dataSourceId
        })
        if (data.length) {
          this.$set(this.tableData[this.tableIndex], 'prodDataSourceUrl', data[0].dataSourceUrl)
          this.$set(this.tableData[this.tableIndex], 'prodDataSourceName', data[0].dataSourceName)
          //校验生产环境表名
          const dataSourceId = data[0].id
          const dataSourceType = data[0].dataSourceType
          const metaTableName = item.uatMetaTableName
          this.existVerifyTable(dataSourceId, dataSourceType, metaTableName)
        }

      },
      //校验生产环境表名接口
      async existVerifyTable (dataSourceId, dataSourceType, metaTableName) {

        const flag = await this.verifyDataSourceType(dataSourceType)
        if (!flag) return
        const params = {
          dataSourceId,
          metaTableName,
          env: 'prod'
        }
        let res = await this.$http.post('/meta/table/existVerify', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code == 0) {
          this.tableData[this.tableIndex].hasProdTable = res.data
        }
      },
      // 校验数据库类型:只有以下数据类型需要判断
      verifyDataSourceType (name) {
        const dataSourceTypeList = ['kafka', 'mysql', 'elasticsearch', 'elasticsearch7', 'doris', 'hbase', 'kudu']
        return dataSourceTypeList.includes(name)
      }
    },
  }
</script>
<style lang='scss' scoped>
  /deep/.ant-table-thead > tr > th[colspan]:not([colspan="1"]) {
    text-align: left;
  }
  /deep/ .ant-table-placeholder {
    top: 80px !important;
  }
  .arrow-icon {
    width: 70px;
  }
  .text-red {
    color: #ff5555;
  }
  .red {
    color: red;
    margin-right: 5px;
  }
</style>