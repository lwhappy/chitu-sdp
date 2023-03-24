/*
 * @Author: hjg
 * @Date: 2021-10-26 09:41:43
 * @LastEditTime: 2021-10-26 09:48:41
 * @LastEditors: Please set LastEditors
 * @Description: 项目信息状态管理文件
 * @FilePath: \bigdata-sdp-frontend2\src\store\modules\project.js
 */
const state = {
  projectId: null,
  saveJobTime: ''
}

const mutations = {
  SET_PROJECT_ID: (state, data) => {
    state.projectId = data
  },
  SET_SAVE_JOB_TIME: (state, data) => {
    state.saveJobTime = data
  }
}

const actions = {
  setProjectId ({ commit }, data) {
    commit('SET_PROJECT_ID', data)
  },
  setSaveJobTime ({ commit }, data) {
    commit('SET_SAVE_JOB_TIME', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
