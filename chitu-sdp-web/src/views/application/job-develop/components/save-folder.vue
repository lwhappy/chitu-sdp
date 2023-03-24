<template>
  <div class="tree-container"
       v-loading="isLoading">
    <a-tree v-if="isRender"
            :tree-data="treeData"
            :expanded-keys.sync="expanded"
            :default-selected-keys="defaultSelectedKeys"
            :replace-fields="replaceFields"
            :block-node="true"
            @select="select">
      <template #title="item">
        <p>
          <img class="fold-img"
               src="@/assets/icons/fold-new.png"
               alt="">
          {{item.folderName}}
        </p>
      </template>
    </a-tree>
    <div class="resize"></div>

  </div>
</template>

<script>
  export default {
    name: "SaveFolder",
    props: {
      expandedKeys: {
        type: Array,
        default: () => { return [] }
      },
      defaultSelectedKeys: {
        type: Array,
        default: () => { return [] }
      }
    },
    components: {
    },
    mixins: [],
    data () {
      return {
        isLoading: false,
        treeData: [],
        replaceFields: {
          key: 'id'
        },
        isRender: false,
        expanded: [],
        lastSelectKey: ''

      }
    },
    computed: {

    },
    watch: {
      expandedKeys: {
        handler (val) {
          this.expanded = val
        },
        deep: true,
        immediate: true
      },
      // defaultSelectedKeys: {
      //   handler () {
      //     setTimeout(() => {
      //       this.selectedKeys =
      //         this.isRender = true
      //     }, 20)


      //   },
      //   deep: true
      // }
    },
    created () {
      this.getTree()
    },
    mounted () {

    },
    methods: {
      async getTree () {
        this.isRender = false
        this.isLoading = true
        let res = await this.$http.post('/folder/queryFolder', {}, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          const data = res.data
          let rebuildData = function (item) {
            const isLeaf = item.isLeaf
            delete item.isLeaf//ant design源码里估计有isReaf字段，会影响dom的渲染，删掉换成isLeafNode
            item.isLeafNode = isLeaf
            item.operateType = ''
            item.contenteditable = false

            if (!item.children) {
              item.children = []
            } else {
              for (let i = 0; i < item.children.length; i++) {
                rebuildData(item.children[i])
              }
            }
          }
          for (let i = 0; i < data.length; i++) {
            rebuildData(data[i])//递归添加该有的字段，避免不能渲染
          }
          this.treeData = [
            {
              folderName: '作业开发',
              id: '0',
              isLeafNode: false,
              isShowOperate: false,
              children: data
            }
          ]
          this.isRender = true
        }
      },
      select (selectedKeys, { selectedNodes }) {
        if (selectedKeys && selectedKeys.length) {
          this.lastSelectKey = selectedKeys[0]
          const findKey = this.expanded.filter(item => { return String(item) === String(selectedKeys[0]) })
          if (findKey.length === 0) {
            this.expanded.push(selectedKeys[0])
          }
        } else if (selectedKeys.length === 0) {
          this.expanded = this.expanded.filter(item => {
            return String(item) !== String(this.lastSelectKey)
          })
        }
        this.selectedKeys = [selectedNodes[0].data.props.dataRef.id]
        this.$emit('select', this.selectedKeys)
      },


    },
  }
</script>
<style lang="scss" scoped>
  .tree-container {
    height: 168px;
    overflow: auto;
    /deep/ .ant-tree-node-content-wrapper {
      font-size: 12px;
    }
    .fold-img {
      width: 16px;
      height: 16px;
    }
  }
</style>
