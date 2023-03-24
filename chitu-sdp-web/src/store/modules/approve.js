
const state = {
  updateApproveData: 0

}

const mutations = {
  SET_APPROVE: (state) => {
    state.updateApproveData += 1
  }
}

const actions = {
  SET_APPROVE ({ commit }) {
    commit('SET_APPROVE')
  }
}


export default {
  namespaced: true,
  state,
  mutations,
  actions
}

