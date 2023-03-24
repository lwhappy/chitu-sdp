/*
 * @Author: your name
 * @Date: 2021-10-20 10:00:51
 * @LastEditTime: 2022-01-04 10:53:36
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \bigdata-sdp-frontend2\src\utils\config.js
 */
export const config = {
  isOpenLog: false,
  isCheckToken: true,//是否要登录获取token,主要用于dev和uat调试时不需要登录调用接口
  isOpenMock: true,//是否要使用mockjs
  domain: '',//正式
  apiRoot: '/sdp'
}