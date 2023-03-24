/*
 * @Author: hjg
 * @Date: 2021-10-26 09:41:43
 * @LastEditTime: 2021-11-12 18:05:30
 * @LastEditors: Please set LastEditors
 * @Description: 项目信息状态管理文件
 * @FilePath: \bigdata-sdp-frontend2\src\store\modules\rule.js
 */
const state = {
  ruleInfo: null
}

const mutations = {
  SET_RULE_INFO: (state, data) => {
    state.ruleInfo = data
  }
}

const actions = {
  setRuleInfo ({ commit }, data) {
    commit('SET_RULE_INFO', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
