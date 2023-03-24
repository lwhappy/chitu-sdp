import _ from 'lodash'
const state = {
  visitedViews: [], // 缓存窗口数组
  cachedViews: [],
  removeRouteName: [],
  isRemoveTag: 0
}

const mutations = {
  /* meta设置了isEveryNewTag，目前只有报表详情，以name+id区分，每个id对应一个页签
      没有设置isEveryNewTag的，以meta设置的tag区分，每个tag为一个页签
  */
  ADD_VISITED_VIEW: (state, route) => {
    // if (state.visitedViews.some(v => v.path === route.path)) return
    // console.log(!route.name || !route.meta.tag || state.visitedViews.some(v => v.path === route.fullPath))
    if (!route.name) {
      return
    }
    let view = null
    // let groovyTag = ''
    // if (route.name === 'editorGroovyeditor' || route.name === 'Groovyeditor') {
    //   groovyTag = 'EditReportConfig' + route.query.listId//groovy脚本的新增/编辑页挂在当前报表详情页签下
    // }
    state.visitedViews.map(item => {
      item.isActive = false
      if (route.meta.isEveryNewTag) {
        let tag = route.name + route.query.id
        if (route.name === 'Groovyeditor') {
          tag = route.name + route.query.code
        }
        if (item.tag === tag) {
          item.isActive = true
          view = item
        }
      } else {
        if (item.tag === route.meta.tag) {
          item.isActive = true
          view = item
        }
      }
    })
    if (route.meta.isEveryNewTag) {//报表配置详情页每个不同的id都生成一个页签
      // let { title } = route.meta
      if (view) {
        view.path = route.fullPath
        return
      } else {
        // let tag = route.name + route.query.id
        let tag = route.name + route.query.id
        if (route.name === 'Groovyeditor') {
          tag = route.name + route.query.code
        }
        let title = ''
        state.visitedViews.push({
          path: route.fullPath,
          names: [route.name],
          firstRouteName: route.name,
          tag,
          title,
          id: route.query.id,
          isActive: true
        })
      }

    } else {
      if (view) {
        view.path = route.fullPath
        return
      } else {

        let { title, tag } = route.meta
        state.visitedViews.push({
          path: route.fullPath,
          names: [route.name],
          firstRouteName: route.name,
          tag,
          title,
          isActive: true
        })
      }

    }

  },
  ADD_CACHED_VIEW: (state, view) => {
    if (state.cachedViews.includes(view.name)) return
    if (!view.meta.noCache) {
      state.cachedViews.push(view.name)
    }
  },

  DEL_VISITED_VIEW: (state, view) => {
    for (const [i, v] of state.visitedViews.entries()) {
      if (v.path === view.path) {
        state.visitedViews.splice(i, 1)
        state.isRemoveTag++
        state.removeRouteName = [view.firstRouteName]
        break
      }
    }
  },
  DEL_CACHED_VIEW: (state, view) => {
    const index = state.cachedViews.indexOf(view.name)
    index > -1 && state.cachedViews.splice(index, 1)
  },

  DEL_OTHERS_VISITED_VIEWS: (state, view) => {
    const otherViews = state.visitedViews.filter(v => {

      return v.path !== view.path
    })
    const removeRouteNames = otherViews.map(item => {
      return item.firstRouteName
    })
    state.visitedViews = state.visitedViews.filter(v => {
      const isHasAffix = _.has(v, 'meta.affix')
      if (isHasAffix) {
        const { meta: { affix = '' } } = v
        return affix
      }
      return v.path === view.path
    })

    state.isRemoveTag++
    state.removeRouteName = removeRouteNames
  },
  DEL_OTHERS_CACHED_VIEWS: (state, view) => {
    const index = state.cachedViews.indexOf(view.name)
    if (index > -1) {
      state.cachedViews = state.cachedViews.slice(index, index + 1)
    } else {
      // if index = -1, there is no cached tags
      state.cachedViews = []
    }
  },

  DEL_ALL_VISITED_VIEWS: state => {
    // keep affix tags
    const affixTags = state.visitedViews.filter(tag => tag.meta.affix)
    state.visitedViews = affixTags
  },
  DEL_ALL_CACHED_VIEWS: state => {
    state.cachedViews = []
  },

  UPDATE_VISITED_VIEW: (state, view) => {
    for (let v of state.visitedViews) {
      if (v.path === view.path) {
        v = Object.assign(v, view)
        break
      }
    }
  }
}

const actions = {
  addView ({ dispatch }, view) {
    dispatch('addVisitedView', view)
    dispatch('addCachedView', view)
  },
  addVisitedView ({ commit }, view) {
    commit('ADD_VISITED_VIEW', view)
  },
  addCachedView ({ commit }, view) {
    commit('ADD_CACHED_VIEW', view)
  },

  delView ({ dispatch, state }, view) {
    return new Promise(resolve => {
      dispatch('delVisitedView', view)
      dispatch('delCachedView', view)
      resolve({
        visitedViews: [...state.visitedViews],
        cachedViews: [...state.cachedViews]
      })
    })
  },
  delVisitedView ({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_VISITED_VIEW', view)
      resolve([...state.visitedViews])
    })
  },
  delCachedView ({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_CACHED_VIEW', view)
      resolve([...state.cachedViews])
    })
  },

  delOthersViews ({ dispatch, state }, view) {
    return new Promise(resolve => {
      dispatch('delOthersVisitedViews', view)
      dispatch('delOthersCachedViews', view)
      resolve({
        visitedViews: [...state.visitedViews],
        cachedViews: [...state.cachedViews]
      })
    })
  },
  delOthersVisitedViews ({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_OTHERS_VISITED_VIEWS', view)
      resolve([...state.visitedViews])
    })
  },
  delOthersCachedViews ({ commit, state }, view) {
    return new Promise(resolve => {
      commit('DEL_OTHERS_CACHED_VIEWS', view)
      resolve([...state.cachedViews])
    })
  },

  delAllViews ({ dispatch, state }, view) {
    return new Promise(resolve => {
      dispatch('delAllVisitedViews', view)
      dispatch('delAllCachedViews', view)
      resolve({
        visitedViews: [...state.visitedViews],
        cachedViews: [...state.cachedViews]
      })
    })
  },
  delAllVisitedViews ({ commit, state }) {
    return new Promise(resolve => {
      commit('DEL_ALL_VISITED_VIEWS')
      resolve([...state.visitedViews])
    })
  },
  delAllCachedViews ({ commit, state }) {
    return new Promise(resolve => {
      commit('DEL_ALL_CACHED_VIEWS')
      resolve([...state.cachedViews])
    })
  },

  updateVisitedView ({ commit }, view) {
    commit('UPDATE_VISITED_VIEW', view)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
