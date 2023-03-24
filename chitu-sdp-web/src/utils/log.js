/*
 * @Author: hjg
 * @Date: 2022-01-04 09:55:51
 * @LastEditTime: 2022-01-04 10:50:52
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\utils\log.js
 */
import { config } from '@/utils/config'
const log = function () {
  if (config.isOpenLog) {
    console.log(arguments)
  }
}
export default log
