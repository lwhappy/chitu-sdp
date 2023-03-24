<!--
 * @Author: hjg
 * @Date: 2021-10-18 14:12:22
 * @LastEditTime: 2022-01-25 10:03:40
 * @LastEditors: Please set LastEditors
 * @Description: 分页组件
 * @FilePath: \bigdata-sdp-frontend2\src\components\table-pignation\index.vue
-->
<template>
  <div class="component-pagination justify-end">
    <a-pagination v-model="pagination.current"
                  :showSizeChanger="pagination.showSizeChanger"
                  :showQuickJumper="pagination.showQuickJumper"
                  :defaultPageSize="pagination.defaultPageSize"
                  :pageSizeOptions="pagination.pageSizeOptions || ['10', '20', '40', '60']"
                  :total="pagination.total"
                  :show-total="total => `共 ${total} 条`"
                  @change="changePage"
                  @showSizeChange="changePageSize"></a-pagination>
  </div>
</template>

<script>
  export default {
    props: {
      pagination: {
        type: Object,
        default: () => ({})
      }
    },
    data () {
      return {}
    },
    methods: {
      // 页码改变的回调，参数是改变后的页码及每页条数
      changePage (page, pageSize) {
        this.$emit('pageChange', { page, pageSize })
      },
      // pageSize 变化回调
      changePageSize (current, size) {
        this.$emit('pageSizeChange', { current, size })
      }
    }
  }
</script>
