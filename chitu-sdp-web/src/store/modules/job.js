/*
 * @Author: hjg
 * @Date: 2021-10-26 09:41:43
 * @LastEditTime: 2021-11-12 18:17:03
 * @LastEditors: Please set LastEditors
 * @Description: 项目信息状态管理文件
 * @FilePath: \bigdata-sdp-frontend2\src\store\modules\job.js
 */
const state = {
  jobInfo: null,
  configInfoActive: '',
  alarmConfigActive: ''
}

const mutations = {
  SET_JOB_INFO: (state, data) => {
    state.jobInfo = data
  },
  setConfigInfoActive: (state, data) => {
    state.configInfoActive = data
  },
  setAlarmConfigActive: (state, data) => {
    state.alarmConfigActive = data
  },
}

const actions = {
  setJobInfo ({ commit }, data) {
    commit('SET_JOB_INFO', data)
  },
  setConfigInfoActive ({ commit }, data) {
    commit('setConfigInfoActive', data)
  },
  setAlarmConfigActive ({ commit }, data) {
    commit('setAlarmConfigActive', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
