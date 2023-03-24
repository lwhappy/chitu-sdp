<!--
 * @description: 抽离table组件
 * @Author: lijianguo19
 * @Date: 2022-06-20 10:16:15
 * @FilePath: \src\components\chitu-table\index.vue
-->
<template>
  <div class="chitu-table-container"
       :class="[fixedHeight?'chitu-table-fixed-height':'',`chitu-table-container-${tableKey}`]"
       :ref="`chituTable-${tableKey}`">
    <a-table v-bind="newtableOpt"
             :locale="tableLocale"
             @change="onTableChange">
      <template v-for="(index,name) in tableSlot"
                :slot="name"
                slot-scope="text,record">
        <slot :name="name"
              :text="text"
              :record="record" />
      </template>
    </a-table>
    <div class="pagination-container"
         :class="{'fixed-pagination':autoHight, 'is-max':autoHight&&isSidbarpagination&&!isFolder ,'is-min':autoHight&&isSidbarpagination&&isFolder}"
         v-if="pagination !== null">
      <a-pagination v-model="pagination.current"
                    :showSizeChanger="pagination.showSizeChanger || true"
                    :showQuickJumper="pagination.showQuickJumper || true"
                    :defaultPageSize="pagination.defaultPageSize || 20"
                    :pageSizeOptions="pagination.pageSizeOptions || ['10', '20', '40', '60']"
                    :total="pagination.total"
                    :show-total="total => `共 ${total} 条`"
                    @change="changePage"
                    :size="pagination.size || ''"
                    @showSizeChange="changePageSize"></a-pagination>
    </div>

  </div>
</template>

<script>
  import _ from 'lodash'
  import VueDraggableResizable from 'vue-draggable-resizable'
  import { mapState } from 'vuex'
  export default {
    name: 'chitu-table',
    components: {

    },
    props: {
      columns: {
        type: Array,
        default: () => []
      },
      dataSource: {
        type: Array,
        default: () => []
      },
      /**
       * 同 a-table 的 scroll，内置 x 的值
       */
      scroll: {
        type: Object,
        default: () => ({ x: '100%' })
      },
      rowKey: {
        type: String,
        default: 'id'
      },
      /**
       * 是否边框
       */
      bordered: {
        type: Boolean,
        default: true
      },
      /**
       * 表头 th 是否可以拖动改变列宽(bordered需要为true才可生效)
       */
      drag: {
        type: Boolean,
        default: true
      },
      /**
       * 是否自适应高度
       * 为true的时候scroll设置失效
       */
      autoHight: {
        type: Boolean,
        default: true
      },
      /**
       * 是否固定高度
       * 为true则表格高度固定
       */
      fixedHeight: {
        type: Boolean,
        default: false
      },
      /**
       * 自适应高度添加而外的高度
       */
      extraHeight: {
        type: [Number, String],
        default: 65
      },
      //开启分页
      pagination: {
        type: [Object, null],
        default: null
      },
      /**
       * 分页器宽度是否随侧边栏宽度改变
       * 只有设置了autoHight时才生效
       */
      isSidbarpagination: {
        type: Boolean,
        default: true
      },
      size: {
        type: String,
        default: 'small'
      },
      /**
       * 第三组件的属性相当于v-bind="$attrs"
       * 可自行扩展添加table属性
       */
      tableOpt: {
        type: Object,
        default: () => { }
      },
    },
    data () {
      return {
        tableKey: new Date().getTime(),
        scrollHight: 0,
        tableLocale: {
          emptyText: () => (
            <div><div><img src="/svg/default-img.png" width="120px;" /></div><div style="font-size:14px;color:#667082">暂无数据</div></div>
          )
        },
        renderColumns: [],
        minColWidth: 90
      };
    },
    computed: {
      ...mapState('global', {
        'isFolder': 'isFolder'
      }),
      columnsWidth () {
        let columnsWidth = 0
        columnsWidth = this.renderColumns.reduce((total, item) => {
          return total + (item.width || 200)
        }, 0)
        //勾选框
        if (this.tableOpt?.rowSelection) {
          columnsWidth += 60
        }
        return columnsWidth
      },
      newtableOpt () {
        let renderScroll = {}
        const { renderColumns: columns, dataSource, scroll, rowKey, bordered, drag, size, tableOpt, components } = this
        // 高度自适应
        if (this.autoHight) {
          renderScroll = {
            x: '100%',
            y: this.scrollHight
          }
        } else {
          renderScroll = {
            x: scroll.x || '100%',
            y: scroll.y
          }
        }
        return { ...tableOpt, columns, dataSource, scroll: renderScroll, rowKey, bordered, drag, size, tableOpt, components, pagination: false }
      },
      tableSlot () { // 支持antd table作用域插槽
        return Object.keys(this.$scopedSlots).reduce((acc, name) => {
          acc[name] = this.$scopedSlots[name]
          return acc
        }, {})
      },
      // 设置表格列拖动
      components () {
        if (this.drag && this.bordered) {
          return {
            header: {
              cell: (h, props, children) => {
                const { key, ...restProps } = props
                // 由于drag事件结束后必然会出发click事件，所以我们需要一个参数去判断当前操作是click点击事件还是drag拖拽事件
                let isDrag = false
                const col = this.renderColumns.find(col => {
                  const k = col.dataIndex || col.key
                  return k === key
                })

                if (!col || !col.width) {
                  return h('th', { ...restProps }, [...children])
                }

                const dragProps = {
                  key: col.dataIndex || col.key,
                  class: 'table-draggable-handle',
                  attrs: {
                    w: 10,
                    x: col.width,
                    z: 1,
                    axis: 'x',
                    draggable: true,
                    transform: 'none',
                    resizable: false
                  },
                  on: {
                    // 拖动时把isdrag参数设置为true
                    // eslint-disable-next-line no-unused-vars
                    dragging: (x, y) => {
                      isDrag = true
                      col.width = Math.max(x, col.minWidth ? col.minWidth : this.minColWidth)
                    },
                    // 拖动结束后把isdrag参数设置为false
                    dragstop: () => {
                      isDrag = true
                      // eslint-disable-next-line vue/no-async-in-computed-properties
                      setTimeout(() => {
                        isDrag = false
                      }, 300)
                    }
                  }
                }
                // 取出column的click事件，对事件进行判断，如果现在isDrag参数为true，则截胡，防止拖动后触发click事件
                if (restProps.on && restProps.on.click) {
                  let clickFunc = restProps.on.click
                  restProps.on.click = (event) => {
                    if (isDrag) {
                      return
                    }
                    clickFunc(event)
                  }
                }
                const drag = h(VueDraggableResizable, { ...dragProps })
                return h('th', { ...restProps, class: 'resize-table-th' }, [...children, drag])
              }
            }
          }
        } else {
          return {}
        }
      }
    },
    watch: {
      columns: {
        handler (newColumn) {
          let renderColumns = _.cloneDeep(newColumn)
          this.renderColumns = renderColumns
        },
        immediate: true
      },
      dataSource: {
        handler () {
          if (this.fixedHeight) {
            this.handleFixedHight()
          }
        },
      },
      columnsWidth () {
        this.debounceAutoColumn()
      },
      extraHeight () {
        this.getTableScroll({ extraHeight: this.extraHeight, ref: this.$refs[`chituTable-${this.tableKey}`] })
      }
    },
    created () { },
    mounted () {
      //监听浏览器视口变化
      this.addResizeListeners()
      this.$nextTick(() => {
        this.debounceAutoColumn()
        if (this.autoHight) {
          this.getTableScroll({ extraHeight: this.extraHeight, ref: this.$refs[`chituTable-${this.tableKey}`] })
        }
        if (this.fixedHeight) {
          this.handleFixedHight()
        }
      })
    },
    beforeDestroy () {
      this.removeResizeListeners()
    },
    activated () {
      this.addResizeListeners()
      this.$nextTick(() => {
        this.debounceAutoColumn()
        if (this.autoHight) {
          this.getTableScroll({ extraHeight: this.extraHeight, ref: this.$refs[`chituTable-${this.tableKey}`] })
        }
        if (this.fixedHeight) {
          this.handleFixedHight()
        }
      })
    },
    deactivated () {
      this.removeResizeListeners()
    },
    methods: {
      addResizeListeners () {
        this.autoHight && window.addEventListener('resize', () => {
          this.throttleAutoHeight()
        })
        window.addEventListener('resize', () => {
          this.debounceAutoColumn()
        })
      },
      removeResizeListeners () {
        window.removeEventListener('resize', this.throttleAutoHeight())
        window.removeEventListener('resize', this.debounceAutoColumn())
      },
      // 动态计算宽度
      debounceAutoColumn () {
        this.$nextTick(() => {
          const chituTable = document.getElementsByClassName(`chitu-table-container-${this.tableKey}`)
          if (!chituTable.length) return
          let chituTableWidth = chituTable[0].offsetWidth
          const col = this.columns.find(col => {
            return col.fixed
          })
          if (col) {
            if (this.columnsWidth >= chituTableWidth) {
              this.columns.map((item, key) => {
                if (item.fixed) {
                  this.renderColumns[key].fixed = item.fixed
                }
              })
            } else {
              this.renderColumns.map((item) => {
                if (item.fixed) {
                  item.fixed = ''
                }
              })
            }
          }
        })
      },
      throttleAutoHeight: _.debounce(function () {
        this.getTableScroll({ extraHeight: this.extraHeight, ref: this.$refs[`chituTable-${this.tableKey}`] })
      }, 100),
      /**
       * 获取第一个表格的可视化高度
       * @param {number} extraHeight 额外的高度(表格底部的内容高度 Number类型,默认为74) 
       * @param {reactRef} ref Table所在的组件的ref
       */
      getTableScroll ({ extraHeight, ref } = {}) {
        let tHeader = null
        if (ref && ref.current) {
          tHeader = ref.current.getElementsByClassName("ant-table-thead")[0]
        } else {
          const chituTable = document.getElementsByClassName(`chitu-table-container-${this.tableKey}`)[0]
          if (chituTable) [
            tHeader = chituTable.getElementsByClassName("ant-table-thead")[0]
          ]
        }
        //表格内容距离顶部的距离
        let tHeaderBottom = 0
        if (tHeader) {
          tHeaderBottom = tHeader.getBoundingClientRect().bottom
        }
        // 窗体高度-表格内容顶部的高度-表格内容底部的高度
        // let height = document.body.clientHeight - tHeaderBottom - extraHeight
        let height = `calc(100vh - ${tHeaderBottom + extraHeight}px)`
        // 空数据的时候表格高度保持不变,暂无数据提示文本图片居中
        if (ref && ref.current) {
          let placeholder = ref.current.getElementsByClassName('ant-table-placeholder')[0]
          if (placeholder) {
            placeholder.style.height = height
            placeholder.style.display = "flex"
            placeholder.style.alignItems = "center"
            placeholder.style.justifyContent = "center"
          }
        }
        this.scrollHight = height
        //表格重新滚动到顶部
        const tableDom = document.querySelector('.ant-table-body')
        if (tableDom) {
          tableDom.scrollTop = 0
        }
      },
      /**
       * 设置表格固定高度
       * 场景：Topic订阅左右表格高度保持一致
       */
      handleFixedHight () {
        this.$nextTick(() => {
          const Table = document.getElementsByClassName(`chitu-table-container-${this.tableKey}`)[0]
          const TableScroll = Table.getElementsByClassName(`ant-table-scroll`)[0]
          TableScroll.style.position = 'relative'
          const TableScrollPlaceholder = Table.getElementsByClassName(`ant-table-placeholder`)[0]
          if (TableScrollPlaceholder) {
            TableScrollPlaceholder.style.cssText = 'position:absolute;top:50px;border:0;width:100%;'
          }
          const TableBody = Table.getElementsByClassName(`ant-table-body`)[0]
          TableBody.style.height = this.scroll.y
        })
      },
      onTableChange (pagination, filters, sorter) {
        this.$emit('change', pagination, filters, sorter)
      },
      // 页码改变的回调，参数是改变后的页码及每页条数
      changePage (page, pageSize) {
        this.$emit('pageChange', { page, pageSize })
      },
      // pageSize 变化回调
      changePageSize (current, size) {
        this.$emit('pageSizeChange', { current, size })
      }
    },
  }
</script>
<style lang='less'>
  .chitu-table-container {
    position: relative;
    background: #ffffff;
    &.chitu-table-fixed-height {
      .ant-table-bordered .ant-table-tbody > tr > td {
        border-bottom: 1px solid #e8e8e8 !important;
      }
    }
    .ant-table-thead {
      height: 40px;
      font-size: 12px !important;
      background: #fafafa;
      > tr th {
        .ant-table-header-column {
          > div {
            width: 100%;
            overflow: hidden;
            color: #2b2f37;
            font-weight: 900;
            font-size: 12px;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .ant-table-column-sorters {
            cursor: pointer;
          }
        }
      }
    }
    .ant-table-tbody {
      font-size: 12px !important;
      tr {
        &:hover:not(.ant-table-expanded-row):not(.ant-table-row-selected) {
          td {
            background: #f4f7fa;
          }
        }
        td {
          color: #333;
          font-size: 12px !important;
          * {
            font-size: 12px !important;
          }
          .ant-select-selection--single {
            height: 28px !important;
            .ant-select-selection__rendered {
              line-height: 28px !important;
            }
          }
          &.ant-table-column-sort {
            background: #ffffff;
          }
        }
      }
    }
    //表格列拖动
    .resize-table-th {
      position: relative;
      .table-draggable-handle {
        position: absolute;
        z-index: 100 !important;
        height: 100% !important;
        bottom: 0;
        left: auto !important;
        right: -5px;
        cursor: col-resize;
        touch-action: none;
        transform: none !important;
      }
    }
    // 表头右侧空白问题
    .ant-table-hide-scrollbar {
      &::-webkit-scrollbar-track {
        //轨道的样式
        background-color: #fafafa !important;
      }
    }
    // 表头右侧边框问题
    .ant-table-fixed-right .ant-table-thead > tr > th:first-child {
      border-right: 0 !important;
    }
    // 列不对齐问题修复
    .ant-table {
      background: #ffffff;
      .ant-table-content {
        .ant-table-scroll {
          .ant-table-body {
            overflow-x: auto !important;
          }
        }
      }
    }
    // 勾选框条件下样式调整
    .ant-table-fixed {
      .selection-column {
        border-right: 0 !important;
      }
      .ant-table-selection-column {
        border-right: 0 !important;
      }
    }
    .ant-table-fixed-left {
      .selection-column {
        border-right: 1px solid #e8e8e8 !important;
      }
      .ant-table-selection-column {
        border-right: 1px solid #e8e8e8 !important;
      }
    }
    ::-webkit-scrollbar {
      //整体样式
      height: 8px;
      width: 8px;
    }
    ::-webkit-scrollbar-thumb {
      //滑动滑块条样式
      border-radius: 6px;
      background-color: #ccd4e0;
      height: 20px;
    }
    ::-webkit-scrollbar-track {
      //轨道的样式
      background-color: #fff;
    }
    // 分页器样式
    .pagination-container {
      text-align: left;
      padding: 10px 20px;
      background: #fff;
      border-top: 1px solid #dee2ea;
      margin-top: 10px;
      &.fixed-pagination {
        position: fixed;
        bottom: 10px;
        left: 0;
        width: 100%;
        padding-bottom: 0;
      }
      &.is-max {
        margin-left: 176px;
        transition: margin-left 0.5s;
        -webkit-transition: margin-left 0.5s; /* Safari */
      }
      &.is-min {
        margin-left: 48px;
        transition: margin-left 0.5s;
        -webkit-transition: margin-left 0.5s; /* Safari */
      }
    }
    .ant-btn {
      height: 22px !important;
      padding: 0 5px !important;
    }
  }
</style>