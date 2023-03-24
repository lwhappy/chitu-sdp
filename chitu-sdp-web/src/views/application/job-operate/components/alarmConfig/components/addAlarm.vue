<!--
 * @Author: hjg
 * @Date: 2021-11-10 09:39:32
 * @LastEditTime: 2021-11-13 10:24:46
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\views\application\job-operate\components\configInfo\components\addAlarm.vue
-->
<template>
  <div class="add-alarm">
    <add-alarm-form ref="form"
                    :params="params"
                    :type="'addAlarm'"
                    :jobInfo="jobInfo"
                    :projectUsers="projectUsers" />
  </div>
</template>
<script>
  import addAlarmForm from './alarmForm.vue'
  export default {
    name: 'addAlarm',
    components: {
      addAlarmForm
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
  .add-alarm {
    width: 100%;
  }
</style>
