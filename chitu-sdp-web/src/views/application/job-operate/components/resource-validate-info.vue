
<template>
  <!-- 资源利用情况 -->
  <div v-if="info"
       class="resource-wrapper">
    <div class="info-title">
      <p v-if="info.success"
         class="word-break">确定要<span class="warn-message">&nbsp;恢复&nbsp;</span>{{record.jobName}}吗？</p>
      <p v-else
         class="word-break">作业{{record.jobName}}所需资源不足，不可恢复作业</p>
    </div>
    <div class="resource">
      <div class="justify-start">
        <div class="resource-item justify-start">
          <p class="label">当前作业预计所需资源：</p>
          <p>
            <em>CPU<span :style="{color:computeCpuColor(info)}">{{info.task.vCores}}</span>&nbsp;VCore</em>
            <em>内存<span :style="{color:computeMemoryColor(info)}">{{info.task.memory}}</span>&nbsp;GB</em>
          </p>
        </div>

      </div>
      <div class="justify-start">
        <div class="resource-item justify-start">
          <p class="label">当前队列参考剩余资源：</p>
          <p>
            <em>CPU<span>{{info.queue.vCores}}</span>&nbsp;VCore</em>
            <em>内存<span>{{info.queue.memory}}</span>&nbsp;GB</em>
          </p>
        </div>
      </div>
      <div class="justify-start">
        <div class="resource-item justify-start">
          <p class="label">总资源剩余可用：</p>
          <p>
            <em>CPU<span>{{info.cluster.vCores}}</span>&nbsp;VCore</em>
            <em>内存<span>{{info.cluster.memory}}</span>&nbsp;GB</em>
          </p>
        </div>

      </div>
    </div>
    <div v-if="info.notice"
         class="tip"><img src="@/assets/icons/warn-yellow.png">{{info.notice}}</div>
  </div>
</template>
<script>
  import resourceColor from '../mixins/resource-color.js'

  export default {
    props: {
      info: {
        type: Object,
        default: () => {
          return {}
        }
      },
      record: {
        type: Object,
        default: () => {
          return {}
        }
      }
    },
    mixins: [resourceColor],
    components: {

    },
    filters: {

    },
    watch: {

    },
    data () {
      return {

      }
    },
    methods: {

    }
  }
</script>
<style lang="scss" scoped>
  .resource-wrapper {
    .word-break {
      word-break: break-all;
    }
    .warn-message {
      color: #faad14;
    }
    .info-title {
      font-size: 14px;
    }
    .resource {
      height: 116px;
      background: #f6f8fa;
      border: 1px solid #c9d0dd;
      border-radius: 4px 4px 0px 0px;
      padding: 0 16px;
      margin-top: 25px;
      .resource-item {
        font-size: 12px;
        margin-top: 16px;
        .label {
          width: 140px;
          color: #2b2f37;
          font-weight: 600;
        }
        em {
          font-style: normal;
          margin-left: 12px;
          span {
            margin-left: 6px;
            font-weight: 600;
          }
        }
      }
    }
    .tip {
      background: #fff4e7;
      border-radius: 0px 0px 4px 4px;
      height: 26px;
      line-height: 26px;
      font-size: 12px;
      text-indent: 16px;
      margin-top: 1px;
      margin-bottom: 16px;
      img {
        width: 13px;
        height: 13px;
        margin-right: 2px;
      }
    }
  }
</style>
