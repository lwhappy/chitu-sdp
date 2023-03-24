<!--
 * @Author: hjg
 * @Date: 2021-10-15 15:36:10
 * @LastEditTime: 2022-06-20 15:51:35
 * @LastEditors: Please set LastEditors
 * @Description: 输入框搜索组件
 * @FilePath: \src\components\input-search\index.vue
-->
<template>
  <div class="input-search">
    <!-- 下拉列表 -->
    <div class="label-info">
      <a-select v-if="labelInfos.length > 1"
                class="select-restyle"
                :default-value="activeItem"
                @change="activeLabelEvent">
        <a-select-option v-for="(item, index) in labelInfos"
                         :value="item.value"
                         :key="'inputLabel' + index">
          {{ item.name }}
        </a-select-option>
      </a-select>
    </div>
    <div class="input-info">
      <a-auto-complete class="auto-complete-restyle"
                       ref="autoComplete"
                       v-model="defaultValue"
                       :data-source="dataSource"
                       style="width: 200px"
                       :placeholder="inputMsg"
                       @select="onSelect"
                       @search="onSearch"
                       @change="onChange">
        <template slot="dataSource">
          <a-select-option v-for="(item, index) in dataSource"
                           :key="'auto-search' + index"
                           :originData="item"
                           :title="item.text">
            {{ item.text }}
            <!-- <span className="global-search-item-count">{{ item.count }} results</span> -->
          </a-select-option>
        </template>
      </a-auto-complete>
    </div>
  </div>
</template>
<script>
  export default {
    data () {
      return {
        activeItem: null,
        defaultValue: null,
        searchValue: null
      }
    },
    computed: {

    },
    watch: {

    },
    methods: {
      onSearch (searchText) {
        // // console.log('onSearch', searchText)
        this.$emit('onSearch', { type: this.activeItem, value: searchText })
      },
      onSelect (searchText, item) {
        // // console.log('onSelect', {type: this.activeItem, value: searchText}, item)
        this.$emit('onSelect', { type: this.activeItem, value: item.data.attrs.originData })
      },
      onChange () {
        // console.log('onChange', value);
      },
      // 初始化选中位
      initActiveItem () {
        this.activeItem = this.labelInfos[0].value
      },
      // 点击选中
      activeLabelEvent (item) {
        // console.log('----------item: ', item)
        this.searchValue = null
        this.activeItem = item
        this.$emit('initDataSource', { type: this.activeItem, flag: true })
      }
    },
    created () {
      this.initActiveItem()
    },
    mounted () {

    },
    destroyed () {
      this.serchDataVisible = false
    },
    props: {
      labelInfos: {
        type: Array,
        default: () => ([])
      },
      inputMsg: {
        type: String,
        default: ''
      },
      serchDataVisible: {
        type: Boolean,
        default: false
      },
      dataSource: {
        type: Array,
        default: () => {
          return []
        }
      }
    }
  }
</script>
<style lang="scss" scoped>
  .input-search {
    display: flex;
    height: 28px;
    // line-height: 28px;
    .label-info {
      height: 28px;
      width: auto;
      padding-top: 1px;
      .select-restyle {
        height: 100%;
        line-height: 28px;
        /deep/ .ant-select-selection--single {
          height: 28px;
        }
        /deep/ .ant-select-selection__rendered {
          height: 28px;
          line-height: 28px;
        }
        /deep/.ant-select-selection {
          height: 28px;
          border-radius: 0;
          // border: 1px solid #000;
          border: 0;
          color: #000;
        }
        /deep/svg {
          color: #000;
        }
      }
    }
    .input-info {
      margin-left: 8px;
      width: calc(80% - 8px);
      height: 100%;
      .auto-complete-restyle {
        width: 100% !important;
        /deep/ .ant-input {
          border-radius: 0;
          height: 100%;
          border-color: #d9d9d9;
          margin-right: 16px;
          &:focus,
          &:hover {
            border-color: #d9d9d9 !important;
            box-shadow: none;
          }
        }
      }
    }
  }
</style>
