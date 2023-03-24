
<template>
  <a-drawer :closable="false"
            class="drawer"
            @close="closeDrawer"
            :visible="drawerVisible">
    <!-- 头部信息 -->
    <div class="title">
      <span>名词解释</span>

      <i class="chitutree-h5 chitutreeguanbi remove"
         @click="closeDrawer"></i>
    </div>
    <div class="content">
      <h3 class="justify-start align-center"><span></span>项目等级/作业等级</h3>
      <div class="description">
        <dl>
          <dt>项目等级</dt>
          <dd>项目等级是指项目权重等级，分为P1/P2/P3/P4共4级，项目等级从P1开始权重逐级递减，用户根据实际项目权重设置项目等级。</dd>
        </dl>
        <dl>
          <dt>作业等级</dt>
          <dd>作业等级是指作业权重等级，分为P1/P2/P3/P4共4级，作业等级默认和项目等级一致，用户根据实际作业权重设置作业等级。</dd>
        </dl>
      </div>
      <table border="0"
             cellpadding="0"
             cellspacing="0"
             border-collapse="collapse">
        <thead>
          <tr>
            <th>权重等级</th>
            <th>等级描述</th>
            <th>参考项目范围</th>
            <th>判断标准</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>P1</td>
            <td>特别重要</td>
            <td>
              <p><b>【对外】</b>涉及一票货主流程类 销售营收成本类客户信息类</p>
            </td>
            <td>公司核心业务，公司级项目，公司主要决策人为主要用户受众面广，使用频次超高（客户和销售使用，每天使用）</td>
          </tr>
          <tr>
            <td>P2</td>
            <td>重要</td>
            <td>
              <p><b>【公司内】</b>公司信息安全类项目，绩效考核类项目，员工信息类项目，采购类项目、公关营销类项目、人效成本类项目</p>
            </td>
            <td>公司重要业务，中心级项目，公司主要决策人会使用（每周1次）受众面广，使用频次高（客户和销售使用，每周使用2次及以上）</td>
          </tr>
          <tr>
            <td>P3</td>
            <td>一般</td>
            <td>
              <p><b>【部门内】</b>职能部门作为唯一受众群体，一般不涉及跨部门合作</p>
            </td>
            <td>职能部门业务，部门级项目，职能部门为主要用户，使用频次高（每周使用2次及以上）</td>
          </tr>
          <tr>
            <td>P4</td>
            <td>不影响</td>
            <td>
              <p><b>【其他】</b>周期性项目、周边信息收集类项目，产研测试类项目</p>
            </td>
            <td>职能部门业务，部门级项目，职能部门活指定人员使用，使用频次一般或研发团队自建的测试类项目</td>
          </tr>
        </tbody>
      </table>
    </div>
  </a-drawer>
</template>
<script>

  export default {
    props: {
      drawerVisible: {
        type: Boolean,
        default: false
      }
    },
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
      // 关闭抽屉
      closeDrawer () {
        this.$emit('closeDrawer')
      }
    }
  }
</script>
<style lang="scss" scoped>
  .drawer {
    /deep/ .ant-drawer-content-wrapper {
      top: 70px;
      height: calc(100% - 50px);
      width: 905px !important;
      .ant-drawer-body {
        padding: 0;
        height: 100%;
        .title {
          height: 40px;
          line-height: 40px;
          padding-left: 16px;
          border-bottom: 1px solid #d9d9d9;
          font-size: 16px;
          display: flex;
          align-items: center;
          position: relative;
          .close-drawer {
            cursor: pointer;
            color: #2c2f37;
          }
          .remove {
            position: absolute;
            right: 0;
            cursor: pointer;
          }
          i {
            margin-right: 8px;
          }
          span {
            font-weight: 600;
          }
          div {
            margin-left: 8px;
            width: 72px;
            height: 20px;
            line-height: 20px;
            color: #fff;
            text-align: center;
          }
        }

        .content {
          height: calc(100% - 40px - 40px);
          padding: 0 16px;
          h3 {
            height: 42px;
            span {
              display: block;
              width: 4px;
              height: 14px;
              background: #006eff;
              margin-right: 9px;
            }
          }
          .description {
            background: #eff4ff;
            border-radius: 4px;
            padding: 8px 12px;
            dl {
              dt {
                font-weight: 600;
                text-align: left;
                color: #006eff;
                line-height: 24px;
              }
              dd {
                color: #2c2f37;
                line-height: 24px;
                font-size: 14px;
              }
            }
          }
          table {
            margin-top: 13px;
            width: 100%;
            border-right: 1px solid #dee2ea;
            border-bottom: 1px solid #dee2ea;
            thead,
            tbody {
              width: 100%;
            }
            th,
            td {
              font-size: 12px;
              text-overflow: initial;
              padding: 10px 10px;
              white-space: pre-wrap !important;
              border-top: 1px solid #dee2ea;
              border-left: 1px solid #dee2ea;
              &:nth-of-type(1),
              &:nth-of-type(2) {
                width: 10%;
                text-align: center;
              }
              &:nth-of-type(3) {
                width: 40%;
              }
              &:nth-of-type(4) {
                width: 40%;
              }
              p {
                line-height: normal;
              }
            }
          }
        }
      }
    }
  }
</style>
