<!--
 * @Author: your name
 * @Date: 2021-10-17 14:21:32
 * @LastEditTime: 2022-09-13 14:44:27
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\components\search-autocomplete\commonIndex.vue
-->
<template>
  <a-auto-complete class="global-search"
                   size="large"
                   v-model="defaultValue"
                   :placeholder="autoMsg"
                   option-label-prop="title"
                   :allowClear="allowClear"
                   :defaultActiveFirstOption="false"
                   @change="onChange"
                   @select="onSelect"
                   @search="handleSearch">
    <template slot="dataSource">
      <a-select-option v-for="(item, index) in dataSource"
                       :key="'auto-search' + index"
                       :title="item.label">
        {{ item.text }}
      </a-select-option>
    </template>
    <a-input @pressEnter="searchBtn">
    </a-input>
  </a-auto-complete>
</template>

<script>

  export default {
    name: "Top",
    components: {
    },
    mixins: [],
    props: {
      autoMsg: {
        type: String,
        default: null
      },
      dataSource: { // 传入时，数组中的每一项都有一个text，用来展示信息
        type: Array,
        default: () => { [] }
      },
      isShowBtn: {
        type: Boolean,
        default: true
      },
      allowClear: {
        type: Boolean,
        default: false
      }
    },
    data () {
      return {
        defaultValue: null
      }
    },
    computed: {

    },
    mounted () {

    },
    methods: {
      onChange (value) {
        // // console.log('onChange', value)
        this.$emit('onChange', value)
      },
      onSelect (value) {
        // 截取auto-search后的内容，为dataSource数组的索引
        let index = parseInt(value.match(/auto-search(\S*)/)[1])
        this.$emit('onSelect', this.dataSource[index])
        this.defaultValue = this.dataSource[index].label
      },
      handleSearch () {
        // this.dataSource = value ? this.searchResult(value) : [];
      },
      searchBtn () {
        // 字符串为null时其search方法会异常报错
        // if (this.defaultValue !== null && (this.defaultValue.length > 0 && this.defaultValue.search('auto-search') === -1))
        this.$emit('searchBtn', this.defaultValue)
      }
    },
  }
</script>
<style lang="scss" scoped>
  .global-search {
    //自动补齐输入框
    /deep/ .ant-input {
      height: 28px !important;
      width: 184px;
    }
    /deep/ .search-btn {
      width: 28px;
      height: 28px;
      text-align: center;
      cursor: pointer;
      color: #fff;
      i {
        font-size: 14px;
      }
    }
  }
</style>

