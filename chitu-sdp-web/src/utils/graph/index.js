import { Graph } from '@antv/x6'
import './shape'
import './edge'
// import { graphDatas } from './data'
import { defaultSetting } from './setting'
import { DagreLayout } from '@antv/layout'
export default class FlowGraph {

  static init (container, graphData, minMap) {
    this.graph = new Graph({
      container: container,
      autoResize: true, // 自动更新画布大小
      keyboard: { // 键盘快捷键
        enabled: true,
        global: true
      },
      // selecting: { //点选/框选
      //   enabled: true,
      //   multiple: true,
      //   rubberband: true,
      //   movable: true,
      //   showNodeSelectionBox: true
      // },
      snapline: false, // 对齐线
      panning: { // 支持拖拽
        enabled: true
        // eventTypes: ['leftMouseDown', 'rightMouseDown', 'mouseWheel'],
      },
      minimap: {
        enabled: true,
        container: minMap,
        width: 236,
        height: 110
      },
      // grid: { // 网格
      //   size: 10,
      //   visible: true
      // },
      // scroller: { // 画布调整
      //   enabled: true,
      //   autoResize: true,
      //   pageVisible: false,
      //   // pageVisible: true,
      //   // pageBreak: true,
      //   pannable: true
      // },
      mousewheel: { // 鼠标缩放
        enabled: true,
        modifiers: ['ctrl', 'meta'],
        // minScale: 0.5,
        // maxScale: 2
      },
      highlighting: { // 指定触发某种交互时的高亮样式,当链接桩可以被链接时
        magnetAvailable: { // 在链接桩外围渲染一个 2px 宽的红色矩形框
          name: 'stroke',
          args: {
            padding: 4,
            attrs: {
              strokeWidth: 4,
              stroke: 'skyblue'
            }
          }
        },
        magnetAdsorbed: { // 连线过程中，自动吸附到链接桩时被使用
          name: 'stroke',
          args: {
            padding: 4,
            attrs: {
              'stroke-width': 8,
              stroke: 'skyblue',
            },
          },
        }
      },
      history: true, // 撤销/重做
      clipboard: { // 剪切板
        enabled: true
      },
      embedding: { // 通过embedding可以将一个节点拖动到另一个节点中，使其成为另一节点的子节点
        enabled: true,
        findParent ({ node }) {
          const bbox = node.getBBox()
          return this.getNodes().filter((node) => {
            // 只有 data.parent 为 true 的节点才是父节点
            const data = node.getData()
            if (data && data.parent) {
              const targetBBox = node.getBBox()
              return bbox.isIntersectWithRect(targetBBox)
            }
            return false
          })
        }
      }
    })
    // graphData = graphDatas
    const dagreLayout = new DagreLayout({
      type: 'dagre',
      rankdir: 'LR',
      align: 'UL',
      begin: [0, 0], // 画布初始渲染位置
      ranksep: 100, // 节点间距
      nodesep: 100, // 层间距
      controlPoints: true // 是否保留布局连线的控制点
    })
    this.model = this.initData(graphData)
    const newModel = dagreLayout.layout(this.model)
    // console.log('newModel: ', newModel)
    this.graph.fromJSON(newModel)
    this.initEvent()
    return this.graph
  }

  // 节点和边数据组合
  static initData (paramsData) {
    // console.log('paramsData: ', paramsData.nodes)
    const model = {
      nodes: [],
      edges: []
    }
    // 不使用数组长度的原因：ID可能不是连续递增的
    let nodeCount = Math.max.apply(Math, paramsData.nodes.map(item => { return item.id }))
    paramsData.nodes.forEach((item) => {
      // 组合节点
      let node = {
        id: String(item.id),
        title: item.pact + '(ID = ' + item.id + ')',
        content: item.contents,
        footer: 'Parallelism: ' + item.parallelism,
        pact: item.pact
      }
      model.nodes.push(this.nodeInfo(node))

      // 组合边
      if (item.predecessors) {
        item.predecessors.forEach(predecessor => {
          let edge = {
            id: String(nodeCount + model.edges.length + 1),
            shape: 'lane-edge',
            source: String(predecessor.id),
            target: String(item.id),
            label: predecessor.ship_strategy,
            // attrs: {
            //   line: {
            //     stroke: '#fd6d6f',
            //     strokeWidth: 1,
            //   },
            // }
          }
          model.edges.push(edge)
        })
      }
    })
    return model
  }

  // 初始节点信息
  static nodeInfo (nodeData) {
    // console.log('nodeData: ', nodeData)
    return {
      id: nodeData.id,
      width: defaultSetting.nodeWidth,
      height: defaultSetting.nodeHeight,
      shape: 'vue-shape',
      component: 'node',
      data: {
        title: nodeData.title,
        content: nodeData.content,
        footer: nodeData.footer,
        pact: nodeData.pact
      },
      ports: {
        groups: {
          top: {
            attrs: {
              circle: {
                r: defaultSetting.anchorPointScale,
                magnet: true,
                stroke: defaultSetting.anchorPointStroke,
                strokeWidth: defaultSetting.anchorPointStrokeWidth,
                fill: defaultSetting.anchorPointFill
              },
            },
            position: 'top',
          },
          bottom: {
            attrs: {
              circle: {
                r: defaultSetting.anchorPointScale,
                magnet: true,
                stroke: defaultSetting.anchorPointStroke,
                strokeWidth: defaultSetting.anchorPointStrokeWidth,
                fill: defaultSetting.anchorPointFill
              },
            },
            position: 'bottom',
          },
          left: {
            attrs: {
              circle: {
                r: defaultSetting.anchorPointScale,
                magnet: true,
                stroke: defaultSetting.anchorPointStroke,
                strokeWidth: defaultSetting.anchorPointStrokeWidth,
                fill: defaultSetting.anchorPointFill
              },
            },
            position: 'left',
          },
          right: {
            attrs: {
              circle: {
                r: defaultSetting.anchorPointScale,
                magnet: true,
                stroke: defaultSetting.anchorPointStroke,
                strokeWidth: defaultSetting.anchorPointStrokeWidth,
                fill: defaultSetting.anchorPointFill
              },
            },
            position: 'right',
          }
        },
        items: [
          {
            id: 'port1',
            group: 'top',
          },
          {
            id: 'port2',
            group: 'bottom',
          },
          {
            id: 'port3',
            group: 'left',
          },
          {
            id: 'port4',
            group: 'right',
          }
        ]
      }
    }
  }
  // 添加节点
  static createNode (nodeData) {
    return this.graph.addNode({
      id: nodeData.id,
      width: defaultSetting.nodeWidth,
      height: defaultSetting.nodeHeight,
      shape: 'vue-shape',
      component: 'node',
      data: {
        title: nodeData.title,
        content: nodeData.content,
        footer: nodeData.footer
      },
      position: nodeData.position,
      ports: {
        groups: {
          top: {
            attrs: {
              circle: {
                r: defaultSetting.anchorPointScale,
                magnet: true,
                stroke: defaultSetting.anchorPointStroke,
                strokeWidth: defaultSetting.anchorPointStrokeWidth,
                fill: defaultSetting.anchorPointFill
              },
            },
            position: 'top',
          },
          bottom: {
            attrs: {
              circle: {
                r: defaultSetting.anchorPointScale,
                magnet: true,
                stroke: defaultSetting.anchorPointStroke,
                strokeWidth: defaultSetting.anchorPointStrokeWidth,
                fill: defaultSetting.anchorPointFill
              },
            },
            position: 'bottom',
          },
          left: {
            attrs: {
              circle: {
                r: defaultSetting.anchorPointScale,
                magnet: true,
                stroke: defaultSetting.anchorPointStroke,
                strokeWidth: defaultSetting.anchorPointStrokeWidth,
                fill: defaultSetting.anchorPointFill
              },
            },
            position: 'left',
          },
          right: {
            attrs: {
              circle: {
                r: defaultSetting.anchorPointScale,
                magnet: true,
                stroke: defaultSetting.anchorPointStroke,
                strokeWidth: defaultSetting.anchorPointStrokeWidth,
                fill: defaultSetting.anchorPointFill
              },
            },
            position: 'right',
          }
        },
        items: [
          {
            id: 'port1',
            group: 'top',
          },
          {
            id: 'port2',
            group: 'bottom',
          },
          {
            id: 'port3',
            group: 'left',
          },
          {
            id: 'port4',
            group: 'right',
          }
        ]
      }
    })
  }

  // 添加边
  static createEdge (data) {
    return this.graph.addEdge(data)
  }

  // 事件
  static initEvent () {
    this.graph.on('node:mouseenter', ({ node }) => {
      // console.log('node:mouseenter', { node })
      if (node) {
        let animateEdges = this.model.edges.filter(item => item.source === node.id)
        // console.log('animateEdges: ', animateEdges)
        animateEdges.forEach(item => {
          this.graph.removeEdge(item.id)
          this.createEdge({
            id: item.id,
            shape: 'animate-edge',
            source: item.source,
            target: item.target,
            label: item.label
          })
        })
      }
    })
    this.graph.on('node:mouseleave', ({ node }) => {
      if (node) {
        let laneEdges = this.model.edges.filter(item => item.source === node.id)
        // console.log('laneEdges: ', laneEdges)
        laneEdges.forEach(item => {
          this.graph.removeEdge(item.id)
          this.createEdge({
            id: item.id,
            shape: 'lane-edge',
            source: item.source,
            target: item.target,
            label: item.label
          })
        })
      }
    })
  }
  // 销毁
  static destroy () {
    this.graph.dispose()
  }
}
