<!--
 * @Author: hjg
 * @Date: 2021-11-09 16:14:41
 * @LastEditTime: 2022-07-12 17:53:07
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\configInfo\components\objectTable.vue
-->
<template>
  <div class="table">
    <div class="table-item"
         v-for="(item, index) in keyArrs"
         :key="index">
      <div class="left">{{ item.text }}</div>
      <div class="right">
        <a-tooltip placement="topLeft"
                   v-if="item.type === 'text'">
          <template slot="title">
            <span>{{ dataInfo[item.key] }}</span>
          </template>
          <span class="bigText">{{ dataInfo[item.key] }}</span>
        </a-tooltip>
        <a-tooltip placement="topLeft"
                   v-else-if="item.type === 'url'">
          <template slot="title">
            <span>{{ dataInfo[item.key] }}</span>
          </template>
          <a :href="dataInfo[item.key]"
             target="_blank">{{ dataInfo[item.key] }}
          </a>
        </a-tooltip>
        <a-tooltip placement="topLeft"
                   v-else-if="item.type === 'bigText'">
          <template slot="title">
            <span>{{ dataInfo[item.key] }}</span>
          </template>
          <span class="bigText">{{ dataInfo[item.key] }}</span>
        </a-tooltip>
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
    .table-item {
      width: 300px;
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
        width: 160px;
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
