/*
 * @Author: hjg
 * @Date: 2022-02-11 19:24:14
 * @LastEditTime: 2022-02-16 15:59:03
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \x6\example\src\utils\graph\edge.js
 */
import { Graph } from '@antv/x6'
import insertCss from 'insert-css'

export const DagNode = Graph.registerEdge('lane-edge',
  {
    inherit: 'edge',
    connector: { name: 'smooth' },
    attrs: {
      line: {
        stroke: '#A2B1C3',
        strokeWidth: 2,
      },
    },
    label: {
      attrs: {
        label: {
          fill: '#A2B1C3',
          fontSize: 12,
        },
      },
    },
  },
  true,
)

export const DagNodeAnimate = Graph.registerEdge('animate-edge',
  {
    inherit: 'edge',
    connector: { name: 'smooth' },
    attrs: {
      line: {
        stroke: '#1890ff',
        strokeDasharray: 5,
        targetMarker: 'classic',
        style: {
          animation: 'ant-line 30s infinite linear',
        },
      },
    },
    label: {
      attrs: {
        label: {
          fill: '#A2B1C3',
          fontSize: 12,
        },
      },
    },
  },
  true,
)
insertCss(`
  @keyframes ant-line {
    to {  
        stroke-dashoffset: -1000
    }
  }
`)
