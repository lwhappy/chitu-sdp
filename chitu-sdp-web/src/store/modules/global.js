/*
 * @Author: hjg
 * @Date: 2021-10-26 09:41:43
 * @LastEditTime: 2022-10-12 15:17:19
 * @LastEditors: Please set LastEditors
 * @Description: 项目信息状态管理文件
 * @FilePath: \src\store\modules\global.js
 */
const state = {
  env: null,
  currentEnv: null,
  isFolder: false,
  currentProject: {
    id: '',
    name: '',
    code: ''
  }
}

const mutations = {
  SET_ENV: (state, data) => {
    state.env = data
  },
  SET_CURRENT_ENV: (state, data) => {
    state.currentEnv = data
  },
  SET_FOLDER: (state) => {
    state.isFolder = !state.isFolder
  },
  saveCurrentProject: (state, data) => {
    state.currentProject = data
  }

}

const actions = {
  setEnv ({ commit }, data) {
    commit('SET_ENV', data)
  },
  setCurrentEnv ({ commit }, data) {
    commit('SET_CURRENT_ENV', data)
  },
  setFolder ({ commit }) {
    commit('SET_FOLDER')
  },
  saveCurrentProject ({ commit }, data) {
    commit('saveCurrentProject', data)
  },

}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
