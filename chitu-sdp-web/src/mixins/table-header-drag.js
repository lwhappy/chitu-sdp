/*
 * @Author: hjg
 * @Date: 2021-11-30 15:02:52
 * @LastEditTime: 2022-01-19 10:26:16
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\mixins\table-header-drag.js
 * @useMethods:
 * 1:a-table 的列配置信息必须配置width,默认值不能为0(也可以按比列设置保证列宽，如：10, 20, 30, 10);
 * 2: a-table 属性components绑定值components;
 * 3: 通过mixins混入，并配入以下数据即可
 *  headerDragData: {
    columnsName: 'columns', // a-table 列配置信息名称
    ref: 'sysManageTables' // a-table 绑定的ref名称
  }
 */
export default {
  data () {
    return {
      resetColumnWidthFlag: true,
      components: this.initTableHeader()// a-table 属性覆盖默认的 table 元素
    }
  },
  mounted () {
    this.resetColumnWidth()
    window.addEventListener('scroll', this.handleScroll, true)
  },
  methods: {
    // 初始化components
    initTableHeader () {
      // this.resetColumnWidth()
      return {
        header: {
          cell: (h, props, children) => {
            const { key, ...restProps } = props
            const col = this[this.headerDragData.columnsName].find((col) => {
              const k = col.dataIndex || col.key
              return k === key
            })
            // if (!col || !col.width) {
            //   return h('th', { ...restProps }, [...children])
            // }
            const dragProps = {
              key: col.dataIndex || col.key,
              class: 'table-draggable-handle',
              attrs: {
                w: 10,
                x: col.width,
                z: 1,
                axis: 'x',
                draggable: true,
                resizable: false,
              },
              on: {
                dragging: (x) => {
                  console.log('drag-x:', x)
                  console.log('width-0: ', col.width)
                  if (x < 60) {
                    x = 60
                  }
                  col.width = Math.max(x, 1)
                  console.log('width-1: ', col.width)
                },
              },
            }
            dragProps.attrs = {
              w: 10,
              x: col.width,
              z: 1,
              axis: 'x',
              draggable: true,
              resizable: false,
            }
            const drag = h('vue-draggable-resizable', { ...dragProps })
            return h('th', { ...restProps, class: 'resize-table-th' }, [...children, drag])
          }
        }
      }
    },
    // 初始化列宽
    resetColumnWidth () {
      // 保证一开始不出现水平滚动条
      let totalWidth = this.$refs[this.headerDragData.ref].$el.clientWidth - 10
      let totalScale = 0
      this[this.headerDragData.columnsName].forEach(item => {
        totalScale += item.width
      })
      this[this.headerDragData.columnsName].forEach(item => {
        item.width = totalWidth * item.width / totalScale
        item.width = Math.max(item.width, 1)
      })
    },
    handleScroll () { // 监听滚动条的变化
      // 目的：让a-table的header与body体的位置保持一致
      if (!document.getElementsByClassName('ant-table-body')[0]) return
      let scrollLeft = document.getElementsByClassName('ant-table-body')[0].scrollLeft
      document.getElementsByClassName('ant-table-header')[0].style.transform = `translateX(-${scrollLeft}px)`
      document.getElementsByClassName('ant-table-header')[0].style.width = `calc(100% + ${scrollLeft}px)`
    }
  }
}

