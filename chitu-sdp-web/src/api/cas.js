/*
 * @Author: your name
 * @Date: 2022-01-21 16:20:10
 * @LastEditTime: 2022-10-21 17:16:06
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\api\cas.js
 */
import router, { resetRouter } from '@/router'
// import { api } from '@/api/http.js'
import { getInfo } from '@/utils/'
import store from '@/store'
// import common from '@/utils/common'


class CAS {
  //登录校验
  async router_verify_cas(to, from, next) {
    if ((!from.name || from.name === 'login') && to.name !== 'login') {//刷新页面获取用户信息
      //保存环境变量
      const localEnv = sessionStorage.getItem('env') || 'prod'
      store.dispatch('global/setEnv', localEnv)
      sessionStorage.setItem('env', localEnv)
      if (store.getters.userInfo) {
        next()
      } else {
        const userInfo = await this.generateRoutes()

        if (userInfo && userInfo.code === 0) {
          next(to)
        } else {
          next('/login')
        }
      }
    } else {
      next()
    }
  }
  async generateRoutes() {
    const info = await getInfo()
    if (info && info.code === 0 && info.data) {
      store.dispatch('user/setUserId', info.data.id)
      store.dispatch('user/setUserInfo', info.data)
      const roles = store.getters.userInfo.isAdmin === 1 ? ['admin'] : []
      const accessRoutes = await store.dispatch('permission/generateRoutes', roles)
      resetRouter()
      router.addRoutes(accessRoutes)
    }
    return info
  }
}
export default new CAS();
