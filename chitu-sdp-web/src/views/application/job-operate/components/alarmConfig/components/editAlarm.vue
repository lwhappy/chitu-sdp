<!--
 * @Author: hjg
 * @Date: 2021-11-10 15:00:40
 * @LastEditTime: 2021-11-12 17:39:49
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\views\application\job-operate\components\alarmConfig\components\editAlarm.vue
-->
<template>
  <div class="edit-alarm">
    <edit-alarm-form ref="form"
                     :params="params"
                     :type="'editAlarm'"
                     :jobInfo="jobInfo"
                     :projectUsers="projectUsers" />
  </div>
</template>
<script>
  import editAlarmForm from './alarmForm.vue'
  export default {
    name: 'editAlarm',
    components: {
      editAlarmForm
    },
    props: {
      params: {
        type: Object,
        default: () => {
          return {}
        }
      },
      jobInfo: {
        type: Object,
        default: () => {
          return {}
        }
      },
      projectUsers: {
        type: Array,
        default: () => {
          return []
        }
      }
    },
    data () {
      return {

      }
    }
  }
</script>
<style lang="scss" scoped>
  .edit-alarm {
    width: 100%;
  }
</style>
