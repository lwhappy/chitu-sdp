/*
 * @Author: hjg
 * @Date: 2022-02-10 17:25:51
 * @LastEditTime: 2022-02-16 15:58:48
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \x6\example\src\utils\graph\shape.js
 */
import { Graph } from '@antv/x6'
import node from '../../components/dag/node.vue'
import '@antv/x6-vue-shape'

export const DagNode = Graph.registerVueComponent('node', {
  width: 200,
  height: 300,
  template: `<node />`,
  components: {
    node
  }
},
  true
)

