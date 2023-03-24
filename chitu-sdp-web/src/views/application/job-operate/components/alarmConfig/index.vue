<!--
 * @Author: hjg
 * @Date: 2021-11-08 20:38:31
 * @LastEditTime: 2022-07-19 15:55:59
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\views\application\job-operate\components\alarmConfig\index.vue
-->
<template>
  <div class="alarm-config">
    <!-- 上层切换和新增告警配置 -->
    <template v-if="!drawerConfigVisible">
      <div class="top">
        <div class="left">
          <div :class="{'tab-checked':isTableActive === 'alarmEvent'}"
               @click="tabChangeEvent('alarmEvent')">告警事件</div>
          <div :class="{'tab-checked':isTableActive === 'alarmRule'}"
               @click="tabChangeEvent('alarmRule')">告警规则</div>
        </div>
        <div class="right"
             @click="openAlarmModal('addAlarm')">
          <!-- <i class="chitutree-h5 chitutreexinzeng"></i> -->
          <a-button type="primary"
                    size="small"
                    icon="plus">
            告警配置
          </a-button>
        </div>
      </div>
      <!-- table表内容 -->
      <div class="table-content">
        <component :is="isTableActive"
                   :active-name="isTableActive"
                   ref="alarmTable"
                   @editEvent="editEvent" />
      </div>
    </template>

    <!-- 告警配置抽屉 -->
    <alarmDrawer v-if="drawerConfigVisible"
                 :projectUsers="projectUsers"
                 :type="alarmDrawerType"
                 :active-name="isTableActive"
                 :drawerConfigVisible="drawerConfigVisible"
                 @closeAlarmDrawer="closeAlarmDrawer" />
  </div>
</template>
<script>
  import alarmEvent from './components/alarmEvent.vue'
  import alarmRule from './components/alarmRule.vue'
  import alarmDrawer from './components/alarmDrawer.vue'
  export default {
    name: 'alarmConfig',
    components: {
      alarmEvent,
      alarmRule,
      alarmDrawer
    },
    props: {},
    data () {
      return {
        alarmDrawerType: 'addAlarm',
        isTableActive: 'alarmEvent',
        drawerConfigVisible: false,
        projectUsers: []
      }
    },
    created () {
      let isTableActive = this.$store.getters.alarmConfigActive
      this.isTableActive = isTableActive || 'alarmEvent'
      this.getUserList()
    },
    methods: {
      // 获取项目成员
      async getUserList () {
        const params = {
          id: this.$store.getters.jobInfo.projectId
        }
        let res = await this.$http.post('/project/projectManagement/getProjectUser', params, {
          headers: {
            projectId: params.id
          }
        })
        if (res.code === 0) {
          this.projectUsers = res.data
        }
      },
      // tab选中
      tabChangeEvent (type) {
        this.isTableActive = type
        this.$store.dispatch('job/setAlarmConfigActive', type)
      },
      // 打开新增告警模态框
      openAlarmModal (type) {
        this.alarmDrawerType = type
        this.drawerConfigVisible = true
      },
      // 关闭告警配置抽屉
      closeAlarmDrawer (type) {
        this.drawerConfigVisible = false
        this.$nextTick(() => {
          if (this.isTableActive === 'alarmRule') {
            let ref = this.$refs.alarmTable
            if (type === 'addAlarm') {
              ref.params.page = 1
            }
            ref.getRulesList(ref.params)
          }
        })

      },
      // 编辑事件传递
      editEvent () {
        this.openAlarmModal('editAlarm')
      }
    }
  }
</script>
<style lang="scss" scoped>
  .alarm-config {
    width: 100%;
    height: 100%;
    .top {
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 24px;
      margin-bottom: 12px;
      .left {
        display: flex;
        > :first-child {
          border-left: 1px solid #ddd;
          border-radius: 4px 0px 0px 4px;
        }
        > :last-child {
          border-radius: 0 4px 4px 0;
        }
        div {
          cursor: pointer;
          border: 1px solid #ddd;
          width: 80px;
          padding: 1px 0;
          text-align: center;
          line-height: 24px;
          font-size: 12px;
          color: #999;
        }
        .tab-checked {
          background-color: #006eff;
          border: 1px solid #006eff;
          color: #fff;
          font-weight: 500;
        }
      }
      .right {
        cursor: pointer;
        font-size: 14px;
        color: #0066ff;
        i {
          margin-right: 8px;
        }
      }
    }
    .table-content {
      width: 100%;
      height: calc(100% - 36px);
    }
  }
</style>
