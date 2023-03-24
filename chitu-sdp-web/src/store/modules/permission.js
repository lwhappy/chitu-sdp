import { baseRoutes, constantRoutes } from '@/router'

/**
 * Use meta.role to determine if the current user has permission
 * @param roles
 * @param route
 */
function hasPermission (roles, route) {
  if (route.meta && route.meta.roles) {
    return roles.some(role => route.meta.roles.includes(role))
  } else {
    return true
  }
}
/**
 * Filter asynchronous routing tables by recursion
 * @param routes asyncRoutes
 * @param roles
 */
export function filterAsyncRoutes (routes, roles) {
  const res = []

  routes.forEach(route => {
    const tmp = { ...route }
    if (hasPermission(roles, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, roles)
      }
      res.push(tmp)
    }
  })

  return res
}

const state = {
  userRoutes: [],
  buttonPermission: null,
  userPermissionCode: '',
  permissionRoutes: [],
}

const recursionRoute = (routes, buttonPermission) => {
  routes.forEach(item => {
    if (item.type === 2 && item.perms) {
      // 多个perms时用逗号分割
      item.perms.split(',').forEach(perms => {
        buttonPermission[perms] = {
          perms: perms,
          id: item.id,
          name: item.parentName + '-' + item.name
        }
      })
    }
    if (item.list) {
      recursionRoute(item.list, buttonPermission)
    }
  })
}

const mutations = {
  SET_USER_ROUTES: (state, routes) => {
    state.userRoutes = routes

    const _buttonPermission = {}
    recursionRoute(routes, _buttonPermission)

    setTimeout(() => {
      state.buttonPermission = _buttonPermission
    }, 100)
  },
  SET_PERMISSION_CODE: (state, data) => {
    state.userPermissionCode = data
  },
  SET_ROUTES: (state, routes) => {
    state.permissionRoutes = baseRoutes.concat(routes)
  }
}

const actions = {
  setUserRoutes ({ commit }, data) {
    commit('SET_USER_ROUTES', data)
  },
  setPermissionCode ({ commit }, data) {
    commit('SET_PERMISSION_CODE', data)
  },
  generateRoutes ({ commit }, roles) {
    return new Promise(resolve => {
      let accessedRoutes
      if (roles.includes('admin')) {
        accessedRoutes = constantRoutes || []
      } else {
        accessedRoutes = filterAsyncRoutes(constantRoutes, roles)
      }
      commit('SET_ROUTES', accessedRoutes)
      resolve(accessedRoutes)
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
