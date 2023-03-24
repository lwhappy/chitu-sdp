/*
 * @Author: your name
 * @Date: 2021-12-08 17:55:17
 * @LastEditTime: 2022-01-04 10:56:25
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\mock\index.js
 */
import Mock from 'mockjs'
import { config } from '@/utils/config'
import log from '@/utils/log'
import postOne from './postOne'
import postTwo from './postTwo'

let _postOne = JSON.parse(JSON.stringify(postOne))
let _postTwo = JSON.parse(JSON.stringify(postTwo))
let postArr = _postOne.concat(_postTwo)
postArr = postArr.filter(item => item.enable)
log('mock postArr: ', postArr)
if (process.env.NODE_ENV === 'development' && config.isOpenMock) {
  const baseDomain = config.domain + config.apiRoot
  const baseURL = baseDomain + '/'
  postArr.forEach(item => {
    Mock.mock(baseURL + item.url, item.retunData)
  })
}

export default Mock
