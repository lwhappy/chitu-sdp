/*
 * @Author: your name
 * @Date: 2022-01-21 16:20:10
 * @LastEditTime: 2022-07-07 20:27:57
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\store\modules\user.js
 */
// import variables from '@/styles/element-variables.scss'
// import defaultSettings from '@/settings'

// const { showSettings, tagsView, fixedHeader, sidebarLogo } = defaultSettings
const state = {
  userInfo: null,
  dict: null,
  token: '',
  erpToken: '',
  employeeNumber: null,
  userId: null
}

const mutations = {
  SET_USER_INFO: (state, data) => {
    state.userInfo = data
  },
  SET_DICT: (state, data) => {
    state.dict = data
  },
  SET_TOKEN: (state, data) => {
    state.token = data
  },
  SET_ERP_TOKEN: (state, data) => {
    state.erpToken = data
  },
  SET_EMPLOYEE_NUMBER: (state, data) => {
    state.employeeNumber = data
  },
  SET_USER_ID: (state, data) => {
    state.userId = data
  }
}

const actions = {
  setUserInfo({ commit }, data) {
    commit('SET_USER_INFO', data)
  },
  setDict({ commit }, data) {
    commit('SET_DICT', Object.fromEntries(data.map(item => [item.name, item.items])))
  },
  setToken({ commit }, data) {
    commit('SET_TOKEN', data)
  },
  setErpToken({ commit }, data) {
    commit('SET_ERP_TOKEN', data)
  },
  setEmployeeNumber({ commit }, data) {
    commit('SET_EMPLOYEE_NUMBER', data)
  },
  setUserId({ commit }, data) {
    commit('SET_USER_ID', data)
  }
}


export default {
  namespaced: true,
  state,
  mutations,
  actions
}

