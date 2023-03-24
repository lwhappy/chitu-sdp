/*
 * @Author: hjg
 * @Date: 2022-01-12 16:39:55
 * @LastEditTime: 2022-01-13 17:24:49
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\mixins\table-sort.js
 * @useMethods:
 * 1.通过mixins混入，并配入以下数据即可
 *  tableSortData: {
      columnsName: 'columns', // a-table 列配置信息名称
      ref: 'sysManageTables' // a-table 绑定的ref名称
    }
  2. change方法中加两行代码：
  this.resetSortMethods(sorter)
  sorter = this.sortedInfo
  3. a-table的columns一定要放在computed就算方法中不然会报异常错误
 */
export default {
  data () {
    return {
      saveSortedInfo: null,
      sortedInfo: {
        columnKey: 'creationDate',
        order: 'descend'
      }
    }
  },
  created () {
    // console.log('columns-hhh:', this.columns)
    this.initSortedInfo()
  },
  mounted () {
    this.resetColumnWidth()
  },
  methods: {
    // 初始化：获取默认排序的列
    initSortedInfo () {
      let columns = this[this.tableSortData.columnsName]
      let index = columns.findIndex(column => column.defaultSortOrder)
      if (index !== -1) {
        this.sortedInfo.columnKey = columns[index].dataIndex
        this.sortedInfo.order = columns[index].defaultSortOrder
        this.saveSortedInfo = this.sortedInfo
      }
    },
    resetSortMethods (sorter) {
      // console.log('sorter: ', sorter)
      if (sorter.order) {
        this.sortedInfo = sorter
        this.saveSortedInfo = sorter
      } else {
        this.sortedInfo.columnKey = this.saveSortedInfo.columnKey
        this.sortedInfo.order = this.saveSortedInfo.order === 'ascend' ? 'descend' : 'ascend'
      }
      this.resetColumnWidth()
    },
    // 初始化列宽
    resetColumnWidth () {
      // 保证一开始不出现水平滚动条
      let totalWidth = this.$refs[this.tableSortData.ref].$el.clientWidth - 10
      let totalScale = 0
      this[this.tableSortData.columnsName].forEach(item => {
        totalScale += item.width
      })
      this[this.tableSortData.columnsName].forEach(item => {
        item.width = totalWidth * item.width / totalScale
        item.width = Math.max(item.width, 1)
      })
    },
  }
}
