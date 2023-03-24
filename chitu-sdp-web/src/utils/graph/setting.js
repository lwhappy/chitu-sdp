/*
 * @Author: hjg
 * @Date: 2022-02-12 10:37:46
 * @LastEditTime: 2022-02-16 15:58:55
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \x6\example\src\utils\graph\setting.js
 */
export const defaultSetting = {
  // 锚点配置
  anchorPointScale: 0, // 节点边上锚点大小， 0 即不显示
  anchorPointStroke: '#31d0c6', // 锚点颜色
  anchorPointFill: '#fff', // 锚点填充色
  anchorPointStrokeWidth: 2, // 锚点边大小
  // 节点配置
  nodeWidth: 200, // 节点宽
  nodeHeight: 300, // 节点高
  nodeStartX: 100, // 节点始点水平坐标
  nodeStartY: 300, // 节点始点纵向(竖直)坐标
  nodeSpaceX: 300, // 节点水平间距
  nodeSpaceY: 100  // 节点纵向(竖直)间距
}