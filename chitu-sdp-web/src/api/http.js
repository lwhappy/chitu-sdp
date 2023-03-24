import axios from 'axios'
// import cas from '@/api/cas.js'
import { logout } from '@/utils/'
import { config } from '@/utils/config'
import { message } from 'ant-design-vue';
import store from '@/store'
import common from '@/utils/common'
message.config({
  maxCount: 1,
});

// const CryptoJS = require("crypto-js");
// const Qs = require('qs');

// 本地打开这个
const baseDomain = config.domain + config.apiRoot
// const baseDomain = '/'


// 接口调用
const api = axios.create({
  baseURL: baseDomain + '/',
  method: 'POST',
  headers: {

  }
})

export const requestResolve = async (config) => {
  const userId = sessionStorage.getItem('userId')
  let token = common.getToken()
 
  const employeeNumber = common.getEmployeeNumber()
  config.headers['x-platform'] = 'sdp'
  config.headers['token'] = token
  // config.headers['env'] = getUserEnv()
  if (userId) {
    config.headers['X-uid'] = userId
  }
  if (employeeNumber) {
    config.headers['X-employee-code'] = employeeNumber
  }
  
  if (store.getters.env) {
    config.headers['env'] = store.getters.env
  }
  
  return config
}

export const reqReject = error => {
  console.error(error)
  return Promise.reject(error)
}
const key = 'updatable';
export const responseResolve = response => {
  // 401代表没权限、402token过期失效。

  if (response.config.responseType !== "blob") {
    if (response.data.code === 401) { // 没权限
      console.log(response.data.msg)
    } else if (response.data.code === 503 || response.data.code === 504) { // 503过期,504 token不存在
      message.error({ content: response.data.msg, key })
      if (config.isCheckToken) {
        setTimeout(() => {
          logout()
        }, 1000)
      }

    } else if (response.data.code !== 0) {
      if (response.data) {
        console.log(response.data.msg)
      }
    }
    return response.data ? response.data : response
  } else {
    //文件流
    return response
  }
}

export const resReject = error => {

  try {
    error = JSON.stringify(error)
  } catch (e) {
    console.log(e)
  }
  if (error.indexOf('502') >= 0 || error.indexOf('503') >= 0) {
    message.error({ content: '网络或服务器故障', key })
  }
  console.log('error', error)
  return Promise.reject(error)
}


api.interceptors.request.use(requestResolve, reqReject)
api.interceptors.response.use(responseResolve, resReject)


export { api }
