<!--
 * @Author: hjg
 * @Date: 2021-11-09 16:14:41
 * @LastEditTime: 2022-06-27 14:53:56
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\approve\components\objectTable.vue
-->
<template>
  <div class="table">
    <div class="table-item"
         v-for="(item, index) in keyArrs"
         :key="index">
      <div class="left">{{ item.text }}</div>
      <div class="right">
        <span>{{ dataInfo[item.key] }}</span>

      </div>
    </div>
  </div>
</template>
<script>
  export default {
    props: {
      keyArrs: {
        type: Array,
        default: () => {
          return []
        }
      },
      dataInfo: {
        type: Object,
        default: () => {
          return {}
        }
      }
    },
    data () {
      return {}
    }
  }
</script>
<style lang="scss" scoped>
  .table {
    width: 100%;
    height: 100%;
    // &::-webkit-scrollbar {
    //   //整体样式
    //   height: 12px;
    //   width: 6px;
    // }
    // &::-webkit-scrollbar-thumb {
    //   //滑动滑块条样式
    //   border-radius: 6px;
    //   background: #ab90e8;
    //   height: 20px;
    // }
    // &::-webkit-scrollbar-track {
    //   //轨道的样式
    //   background-color: #fff;
    // }
    .table-item {
      width: 100%;
      height: 30px;
      display: flex;
      border: 1px solid #ddd;
      border-top: 0;
      div {
        display: flex;
        align-items: center;
        font-size: 12px;
        color: #333;
        padding-left: 8px;
        white-space: nowrap;
      }
      .left {
        width: 140px;
        background-color: #f9f9f9;
        border-right: 1px solid #ddd;
      }
      .right {
        overflow: hidden;
        a {
          color: #0066ff;
          text-decoration: underline;
          width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .bigText {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
    > :first-child {
      height: 34px;
      border-top: 1px solid #ddd;
    }
  }
</style>
