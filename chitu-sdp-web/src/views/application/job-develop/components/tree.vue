<template>
  <div class="tree-container">
    <div class="tree-top justify-between">
      <div class="search">
        <SearchAutocomplete @searchSelect="searchSelect" />
      </div>
      <p class="top-btns">
        <a-button class="btn"
                  ref="setPosition"
                  @click="setPosition('','clickPosition')"><i class="chitutree-h5 chitutreedingwei"></i></a-button>
        <!-- <a-button :disabled="selectItem && selectItem.id!=='0'?false:true"
                  class="btn"
                  @click="deleteItem"><i class="chitutree-h5 chitutreeshanchu"></i></a-button> -->
      </p>
    </div>
    <div class="tree-wrapper"
         v-loading="isLoading">
      <a-tree v-if="isRender"
              :tree-data="treeData"
              :expanded-keys.sync="defaultExpandedKeys"
              :block-node="true"
              :selectedKeys="selectedKeys"
              :draggable="isDraggable"
              @dragenter="onDragEnter"
              @drop="onDrop"
              @select="select"
              :expand-action="expandAction"
              @expand="expand">
        <template slot="switcherIcon"></template>
        <template v-slot:title="item">
          <span v-if="item.isFile"
                :style="{paddingLeft:item.level * 10 + 'px'}"
                class="name file-name"
                :id="item.key"
                @dragover="fileDragover"
                @contextmenu.stop.prevent="rightClick(item,$event)"
                @dblclick.stop.prevent="openFile(item)">
            <template v-if="item.fileType === 'SQL'">
              <i v-if="item.isOnLine === 0"
                 class="chitutree-h5 chitutreeSQL"></i>
              <i v-if="item.isOnLine === 1"
                 class="chitutree-h5 chitutreeSQL green"></i>
            </template>
            <template v-else-if="item.fileType === 'DS'">
              <i v-if="item.isOnLine === 0"
                 class="chitutree-h5 chitutreeDS"></i>
              <i v-if="item.isOnLine === 1"
                 class="chitutree-h5 chitutreeDS cyanine"></i>
            </template>
            {{item.fileName}}<em class="lock"
                v-if="item.lockSign === 0">{{item.lockedBy}} 锁定</em>
          </span>
          <span v-else
                class="name"
                :id="'tree-folder-' + item.key"
                :level="item.level"
                :style="{paddingLeft:item.level * 10 + 'px'}"
                @mouseenter="titleHover(item,$event)"
                @mouseleave="titleLeave(item,$event)"
                @contextmenu.prevent="rightClick(item,$event)"
                @dblclick="clickFolder(item)">

            <span class="sub-name"
                  :class="!item.isFile && item.isExpand?'expand':'fold'">
              <input v-if="item.contenteditable"
                     class="folder-name  input"
                     :id="'folder-' + item.id"
                     type="text"
                     :value="item.folderName"
                     @keyup="keyup($event,item)"
                     @keydown="keydown($event)"
                     @blur="nameBlur($event,item)" />
              <!-- dom是if else,不会存在两个相同的id -->
              <span v-else
                    class="folder-name"
                    :id="'folder-' + item.id">{{ item.folderName }}</span>
            </span>
            <span class="operate">
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>新建作业</span>
                </template>
                <span class="item"
                      @click.stop="addFile(item)"><i class="chitutree-h5 chitutreexinjian"></i></span>
              </a-tooltip>
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>新建目录</span>
                </template>
                <span class="item"
                      @click.stop="addFolder(item)"><i class="chitutree-h5 chitutreexinjianwenjianjia"></i></span>
              </a-tooltip>
              <a-tooltip placement="topLeft">
                <template slot="title">
                  <span>目录重命名</span>
                </template>
                <span v-if="String(item.id) !== '0'"
                      class="item"
                      @click.stop="editFolder($event,item)"><i class="chitutree-h5 chitutreebianji"></i></span>
              </a-tooltip>

            </span>
          </span>

        </template>
      </a-tree>
    </div>
    <!-- <div class="resize"></div> -->
    <add-file ref="addFile" />
    <div v-if="rightClickStyle.isShow"
         class="right-click-wrapper"
         @click="rightClickStyle.isShow=false">
      <div class="sub"
           @click.stop
           :style="{left:rightClickStyle.left,top:rightClickStyle.top}">
        <a-button :disabled="selectItem && selectItem.id!=='0'?false:true"
                  class="item"
                  @click="deleteItem">删除</a-button>
        <a-button v-if="selectItem.isFile"
                  class="item"
                  @click="copyFile">复制</a-button>

      </div>
    </div>
    <a-modal wrapClassName="copy-name-dialog"
             :mask-closable="false"
             :closable="false"
             v-model="isShowCopyFileName"
             :width="360"
             :footer="null">
      <div class="content">
        <div class="input-wrapper">
          <p v-if="selectItem"
             class="label">
            作业名称
          </p>
          <a-input placeholder="请输入作业名称"
                   v-model="copyName" />
        </div>
        <p class="input-tip">注：作业名称不支持修改，请认真填写</p>
      </div>
      <div class="footer justify-end">
        <a-button class="button-restyle button-confirm"
                  @click="copyConfirmEvent">
          确认
        </a-button>
        <a-button class="button-restyle button-cancel"
                  @click="isShowCopyFileName=false;rightClickStyle.isShow = false">取消</a-button>
      </div>

    </a-modal>
  </div>
</template>

<script>
  import $ from 'jquery'
  import addFile from './add-file'
  import SearchAutocomplete from './search-autocomplete'

  export default {
    name: "Tree",
    components: {
      addFile, SearchAutocomplete
    },
    mixins: [],
    data () {
      return {
        isDraggable: true,
        oldProjectId: '',
        isLoading: false,
        treeData: [],
        defaultExpandedKeys: ['0'],
        selectKeys: [],
        isRender: false,
        replaceFields: {
          key: 'id'
        },
        selectItem: null,
        defaultExpandAll: true,
        lastSelectKey: '',
        expandAction: 'false',
        rightClickStyle: {
          isShow: false,
          left: 0,
          top: 0
        },
        selectedKeys: [],
        activeFile: null,
        isShowCopyFileName: false,
        copyName: '',
        fileId: '',
        folderId: '',
        isCreated: false


      }
    },
    computed: {
      isProdEnv () {
        return this.$store.getters.env === 'prod'
      }
    },

    created () {

      this.init()
      this.oldProjectId = this.$route.query.projectId
    },
    mounted () {
      this.$bus.$off('addFileSuccess').$on('addFileSuccess', this.addFileSuccess)
      this.$bus.$off('refreshTree').$on('refreshTree', this.init)
      this.isAllowAddFile(true)
      // let isResize = false
      // let width = $('.tree-container').width()
      // let clientX = 0
      // this.$nextTick(() => {
      //   $('.resize').on('mousedown', (e) => {
      //     isResize = true
      //     clientX = e.clientX
      //   })
      //   $('#app').on('mousemove', (e) => {
      //     if (!isResize) return;
      //     const distance = e.clientX - clientX
      //     width += distance
      //     if (width < 150) {
      //       width = 150
      //     }
      //     if (width > 500) {
      //       width = 500
      //     }
      //     clientX = e.clientX
      //     $('.tree-container').css({ 'min-width': width, 'width': width })
      //     const maxWidth = 'calc(100% - ' + width + 'px)'
      //     $('.main-board-container').css({ 'max-width': maxWidth })
      //   }).on('mouseup', () => {
      //     isResize = false;
      //   })
      // })


    },
    watch: {
      defaultExpandedKeys: {
        handler (val) {
          if (val) {
            const employeeNumber = sessionStorage.getItem('employeeNumber')
            localStorage.setItem('defaultExpandedKeys_' + employeeNumber + '_' + this.$route.query.projectId, JSON.stringify(val))
          }
        }
      },
      $route: {
        handler (val) {
          if (val) {
            if (this.$route.query.projectId && this.$route.name === 'JobDevelop') {
              if (this.$route.query.projectId !== this.oldProjectId) {
                this.init()
                this.oldProjectId = this.$route.query.projectId
              }

            }
          }
        },
        deep: true
      }
    },
    methods: {
      async init () {
        this.fileId = typeof this.$route.query.fileId !== 'undefined' ? Number(this.$route.query.fileId) : ''
        this.folderId = typeof this.$route.query.folderId !== 'undefined' ? Number(this.$route.query.folderId) : ''
        this.$bus.$off('tree_getActiveFile').$on('tree_getActiveFile', this.getActiveFile)//main-board切换页签时触发
        try {
          const employeeNumber = sessionStorage.getItem('employeeNumber')
          let defaultExpandedKeys = localStorage.getItem('defaultExpandedKeys_' + employeeNumber + '_' + this.$route.query.projectId)
          if (defaultExpandedKeys && defaultExpandedKeys !== 'null') {
            defaultExpandedKeys = JSON.parse(defaultExpandedKeys)
            this.defaultExpandedKeys = defaultExpandedKeys
          }

        } catch (e) {
          console.log(e)
          this.defaultExpandedKeys = ['0']
        }
        this.selectItem = null
        this.selectedKeys = []
        this.rightClickStyle.isShow = false
        await this.getTree()
        this.defaultExpandedKeys.forEach(item => {//获取所有展开目录下的文件
          this.getFile(Number(item))
        })
        //从作业审批点过来的，只在加载页面时触发，后续调init不触发
        if (!this.isCreated && this.folderId !== '' && this.fileId != '') {//folderId有=0的情况
          this.setPosition(this.folderId, 'jump')
        }
        this.isCreated = true
      },
      titleHover (item, e) {
        if (!item.dataRef.contenteditable) {
          $(e.target).find('.operate').show()
        }
      },
      titleLeave (item, e) {
        $(e.target).find('.operate').hide()
      },
      onDragEnter (info) {
        // console.log('onDragEnter', info);
        // expandedKeys 需要受控时设置
        // this.expandedKeys = info.expandedKeys
        this.defaultExpandedKeys = info.expandedKeys
        const findItem = this.getTreeDataById(this.treeData, info.node.dataRef.id)
        if (findItem) {
          findItem.isExpand = true
        }
      },
      onDrop (info) {
        // console.log('onDrop', info);
        if (info.node.dataRef.isFile) {//拖动的目标节点是文件
          return
        }
        let loopData = function (data, findItem, type, callback) {
          if (String(findItem.id) === String(data.id) && findItem.isFile === data.isFile) {//目标节点是根目录
            if (type === 'add') {
              return callback(data)
            }
          }
          if (data.children) {
            for (let i = 0; i < data.children.length; i++) {
              const item = data.children[i]
              if (String(findItem.id) === String(item.id) && findItem.isFile === item.isFile) {//切记isFile必须相等，文件id和目录id有存在相同的情况
                if (type === 'remove') {
                  return callback(data.children, i)
                } else if (type === 'add') {
                  return callback(item)
                }
              } else {
                loopData(item, findItem, type, callback)
              }

            }
          }
        }
        let removeItem = null
        const dragNode = info.dragNode.dataRef//拖动的节点
        const targetNode = info.node.dataRef//目标节点
        loopData(this.treeData[0], dragNode, 'remove', (arr, index) => {//递归循环树，找到拖动的节点，再删除
          const removeArr = arr.splice(index, 1)
          removeItem = removeArr[0]
        })
        loopData(this.treeData[0], targetNode, 'add', async (item) => {//添加拖动的节点
          removeItem.level = item.level + 1
          if (removeItem.isFile) {
            removeItem.folderId = targetNode.id
            await this.moveFile(removeItem)
            this.$bus.$emit('updateFolderId', removeItem.id, removeItem.folderId)//更新右侧页签文件的目录id
          }
          else {
            removeItem.parentId = targetNode.id
            await this.updateFolder(removeItem)
          }
          this.init()



        })

      },
      async updateFolder (item) {
        const params = {
          parentId: item.parentId, //父目录ID
          projectId: Number(this.$route.query.projectId), //项目ID
          id: item.id //目录ID
        }
        let res = await this.$http.post('/folder/updateFolder', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        return res
      },
      async moveFile (file) {
        const data = {
          folderId: file.folderId,
          id: file.id,
        }
        let res = await this.$http.post('/file/removeFile', data, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        return res
      },
      async getTree () {
        this.isLoading = true
        let res = await this.$http.post('/folder/queryFolder', {}, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.isLoading = false
        if (res.code === 0) {
          let data = res.data
          let rebuildData = function (item) {
            const isLeaf = item.isLeaf
            delete item.isLeaf//ant design源码里估计有isReaf字段，会影响dom的渲染，删掉换成isLeafNode
            item.isLeafNode = isLeaf
            item.operateType = ''
            item.contenteditable = false
            item.key = item.id
            if (this.defaultExpandedKeys.includes(item.key)) {
              item.isExpand = true//用于显示文件夹的图标是展开还是收缩
            }
            if (!item.children) {
              item.children = []
            } else {
              for (let i = 0; i < item.children.length; i++) {
                item.children[i].level = item.level + 1
                rebuildData(item.children[i])
              }
            }
          }.bind(this)
          for (let i = 0; i < data.length; i++) {
            data[i].level = 2
            rebuildData(data[i])//递归添加该有的字段，避免不能渲染
          }

          const treeData = [
            {
              folderName: '作业开发',
              id: 0,
              key: '0',
              isLeafNode: false,
              isFile: false,
              isExpand: true,
              parentId: '',
              projectId: Number(this.$route.query.projectId),
              level: 1,
              // isShowOperate: false,
              children: data
            }
          ]
          this.treeData = treeData
          this.isRender = true
        } else {
          this.$message.error(res.msg);
        }
      },
      async addFile (item) {
        const flag = await this.isAllowAddFile()
        if (!flag) return
        const defaultSelectedKeys = [item.dataRef.id]
        this.$refs.addFile.open({ expandedKeys: this.defaultExpandedKeys, defaultSelectedKeys: defaultSelectedKeys })
      },
      async editFolder (e, item) {
        const flag = await this.isAllowAddFile(true)
        if (!flag) {
          this.$message.warning('为了规范作业流程，生产环境不允许直接修改目录，建议先在UAT环境进行作业开发后同步至生产环境~')
          return
        }
        item.dataRef.contenteditable = true
        item.dataRef.operateType = 'edit'
        this.$nextTick(() => {
          $(e.target).parents('.operate').siblings('.sub-name').find('.folder-name').focus().select()
        })

        // $(e.target).parents('.operate').siblings('.folder-name').attr('contenteditable', true).focus()
      },
      async isAllowAddFile (flag) {
        if (!this.isProdEnv) {
          return true
        } else {
          let res = await this.$http.post('/file/allowAddFile', {}, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            if (!res.data && !flag) {
              this.$message.warning('为了规范作业流程，生产环境不允许直接创建作业，建议先在UAT环境进行作业开发后同步至生产环境~')
            }
            this.isDraggable = res.data
            return res.data
          } else {
            return false
          }
        }
      },
      async isAllowEditFile (item) {
        if (!this.isProdEnv) {
          return true
        } else {
          let res = await this.$http.post('/file/allowEditFile', {
            id: item.id
          }, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            if (!res.data.allowEdit) {
              this.$message.warning('为了规范作业流程，生产环境不允许直接编辑作业，建议先在UAT环境进行作业开发后同步至生产环境~')
            }
            return res.data.allowEdit
          } else {
            return true
          }
        }
      },
      async addFolder (item) {
        const flag = await this.isAllowAddFile()
        if (!flag) return
        let dataRef = item.dataRef
        const folderName = 'newFolder'
        const tempId = String(new Date().getTime())
        dataRef.isLeafNode = false
        const obj = {
          folderName: folderName,
          id: tempId,
          children: [],
          enabledFlag: 1,
          isLeafNode: true,
          parentId: dataRef.id,
          projectId: Number(this.$route.query.projectId),
          operateType: 'add',
          contenteditable: true,
          level: dataRef.level + 1
        }
        dataRef.children.push(obj)
        // this.$set(dataRef.children, dataRef.children.length, obj)
        // this.isRender = false
        this.defaultExpandedKeys.push(dataRef.key)
        // this.$forceUpdate()
        // this.isRender = true
        this.$nextTick(() => {
          $('#folder-' + tempId).focus().select()
        })
      },
      async deleteItem () {
        const selectItem = JSON.parse(JSON.stringify(this.selectItem))
        // const flag = await this.isAllowEditFile(selectItem)
        // if (!flag) return
        let onOk = async function () {
          let id = ''
          if (selectItem) {
            id = selectItem.id
          }
          if (!id) {
            return
          }
          const params = {
            id: id
          }
          let url = ''
          if (selectItem.isFile) {
            url = '/file/deleteFile'
          } else {
            url = '/folder/deleteFolder'
          }
          let res = await this.$http.post(url, params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            this.$message.success('删除成功')
            this.$bus.$emit('deleteTreeFile', id)
            this.init()
          } else {
            this.rightClickStyle.isShow = false
            this.$message.error(res.msg);
          }
        }.bind(this)
        this.$confirm({
          title: '确定要删除吗?',
          content: '',
          okText: '确认',
          cancelText: '取消',
          onOk: onOk,
        });
      },
      async copyFile () {
        const flag = await this.isAllowAddFile()
        if (!flag) return
        let onOk = async function () {
          this.isShowCopyFileName = true
          this.copyName = this.selectItem.fileName + '-copy'
        }.bind(this)
        this.$confirm({
          title: () => {
            return "提示"
          },
          content: '将复制该作业的代码、作业配置、资源配置、元表配置或DS配置所有内容，确定要复制吗？',
          okText: '下一步',
          cancelText: '取消',
          onOk: onOk,
          onCancel: () => {
            this.rightClickStyle.isShow = false
          },
          class: 'copy-confirm',
        });
      },
      keydown (e) {
        if (e.keyCode === 13) {
          //禁止换行
          e.cancelBubble = true;
          e.preventDefault();
          e.stopPropagation();
        }
      },
      keyup (e, item) {
        if (e.keyCode === 13) {
          this.nameBlur(e, item)
          // e.cancelBubble = true;
          // e.preventDefault();
          // e.stopPropagation();
          // e.returnValue = false;
          // item.dataRef.contenteditable = false
          return false
        }
      },
      async nameBlur (e, item) {
        if (item.dataRef.operateType === 'edit') {
          if (!item.dataRef.contenteditable) {//防止敲enter时执行两次
            return
          }
          const text = $(e.target).val().trim()
          if (!text) {
            item.dataRef.contenteditable = false
            return
          }
          if (text === item.dataRef.folderName) {
            item.dataRef.contenteditable = false
            return
          }
          item.dataRef.folderName = text
          item.folderName = text
          item.dataRef.contenteditable = false

          const params = {
            folderName: text, //目录名称
            parentId: item.dataRef.parentId, //父目录ID
            projectId: Number(this.$route.query.projectId), //项目ID
            id: item.dataRef.id //目录ID
          }
          let res = await this.$http.post('/folder/updateFolder', params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code !== 0) {
            this.$message.error(res.msg);
          }
        }
        else if (item.dataRef.operateType === 'add') {
          if (!item.dataRef.contenteditable) {//防止敲enter时执行两次
            return
          }
          const folderName = $(e.target).val().trim()
          if (!folderName) {
            item.dataRef.contenteditable = false
            const findItem = this.getTreeDataById(this.treeData, item.dataRef.id)
            const findParent = this.getTreeDataById(this.treeData, findItem.parentId)
            for (let i = 0; i < findParent.children.length; i++) {
              if (String(findParent.children[i].id) === String(item.dataRef.id)) {
                findParent.children.splice(i, 1)
                break
              }
            }
            return
          }
          item.dataRef.folderName = folderName//防止闪烁
          item.dataRef.contenteditable = false
          const params = {
            folderName: folderName, //目录名称
            parentId: item.dataRef.parentId, //父目录ID
            projectId: Number(this.$route.query.projectId), //项目ID
          }
          let res = await this.$http.post('/folder/addFolder', params, {
            headers: {
              projectId: Number(this.$route.query.projectId)
            }
          })
          if (res.code === 0) {
            // delete item.isLeaf
            // item.dataRef.id = res.data.id
            // item.dataRef.folderName = folderName
            // item.dataRef.contenteditable = false
            this.init()//重新拉一下，不然在该目录下再建目录或文件无法渲染，试过很多方法，这是最终方案

          } else {
            this.$message.error(res.msg);
            const findItem = this.getTreeDataById(this.treeData, item.dataRef.id)
            const findParent = this.getTreeDataById(this.treeData, findItem.parentId)
            for (let i = 0; i < findParent.children.length; i++) {
              if (String(findParent.children[i].id) === String(item.dataRef.id)) {
                findParent.children.splice(i, 1)
                break
              }
            }
          }
        }
      },
      projectConfirm () {

      },
      projectCancel () {

      },
      clickFolder (node) {
        const findKey = this.defaultExpandedKeys.filter(item => {
          return String(item) === String(node.key)
        })
        if (findKey && findKey.length) {
          node.dataRef.isExpand = false
          this.defaultExpandedKeys = this.defaultExpandedKeys.filter(item => {
            return String(item) !== String(node.key)
          })
        } else {
          this.defaultExpandedKeys.push(node.key)
          node.dataRef.isExpand = true
          this.getFile(node.id)
        }
      },
      rightClick (item, e) {
        if (item.key === '0') {
          return
        }
        this.selectedKeys = [item.key]
        this.selectItem = item
        // console.log('this.selectItem', this.selectItem)
        this.rightClickStyle.isShow = true
        this.rightClickStyle.left = e.clientX + 'px'
        this.rightClickStyle.top = e.clientY + 'px'
      },
      select (selectedKeys, { selectedNodes }) {
        this.selectedKeys = selectedKeys
        this.selectItem = null
        if (selectedNodes && selectedNodes.length) {
          this.selectItem = selectedNodes[0].data.props.dataRef
          if (this.selectItem.isFile) {
            return
          }
        }
      },
      expand () {
        return false
        // // console.log(expandedKeys, expanded, node)
        // if (expanded) {
        //   const id = node.eventKey
        //   this.getFile(id)
        // }

      },
      async getFile (folderId) {

        const params = {
          folderId: folderId
        }
        let res = await this.$http.post('/file/queryFile', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        const findItem = this.getTreeDataById(this.treeData, folderId)
        const fileData = res.data.map(item => {
          item.isFile = true
          item.key = 'file_' + item.id//拼file的目的是防止跟目录的key有重复
          item.level = findItem.level + 1
          return item
        })
        if (!findItem.children) {
          findItem.children = []
        }
        findItem.children = findItem.children.filter(item => {
          return !item.isFile
        })
        findItem.children = findItem.children.concat(fileData)
        if (findItem.children.length) {
          findItem.isLeafNode = false
        }
        this.$forceUpdate()

      },
      getTreeDataById (childs = [], findId, type) {//通过id获取目录节点，默认获取目录节点，type=file时获取文件节点
        let finditem = null;
        for (let i = 0, len = childs.length; i < len; i++) {
          let item = childs[i]
          if (item.id !== findId && item.children && item.children.length > 0) {
            finditem = this.getTreeDataById(item.children, findId)
          }
          if (type === 'file') {
            if (item.id == findId && item.isFile) {
              finditem = item
            }
          } else {
            if (item.id == findId && !item.isFile) {
              finditem = item
            }
          }

          if (finditem != null) {
            break
          }
        }
        return finditem
      },
      addFileSuccess (folderId) {
        this.getFile(folderId)
        const findKeys = this.defaultExpandedKeys.filter(item => {
          return String(item) === String(folderId)
        })
        if (findKeys.length === 0) {
          this.defaultExpandedKeys.push(String(folderId))
        }
      },
      async openFile (item) {
        this.selectedKeys = [item.eventKey]
        const params = {
          id: item.id
        }
        this.$bus.$emit('isLoadingFile', true)
        let res = await this.$http.post('/file/detailFile', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        this.$bus.$emit('isLoadingFile', false)
        if (res.code === 0) {
          let data = res.data
          if (data) {
            this.$bus.$emit('getFile', data)
          }
        }
      },
      getActiveFile (activeFile) {
        // console.log('activeFile: ', activeFile)
        this.activeFile = activeFile
        this.setPosition('', 'clickPosition')

      },
      getPathByKey: (curKey, data) => {
        let result = []; // 记录路径结果
        let path = []
        let traverse = (curKey, data) => {
          if (data.length === 0) {
            return;
          }
          for (let item of data) {
            path.push(item);
            if (String(item.id) === String(curKey) && !item.isFile) {
              result = JSON.parse(JSON.stringify(path));
              return
            }
            const children = Array.isArray(item.children) ? item.children : [];
            traverse(curKey, children); // 遍历
            path.pop(); // 回溯
          }
        }
        traverse(curKey, data);
        return result;
      },

      async setPosition (_folderId, type) {//有参数时是搜索点击目录时触发或从作业审批跳过来时触发，没参数时是点击定位按钮触发
        // console.log(_folderId, type)
        let folderId = ''
        if (_folderId !== '') {
          folderId = _folderId
        } else if (this.activeFile) {
          folderId = this.activeFile.contentData.folderId
        }

        const path = this.getPathByKey(folderId, this.treeData)
        // console.log('path', path)
        path.forEach(item => {
          if (!this.defaultExpandedKeys.includes(item.key)) {
            this.defaultExpandedKeys.push(item.key)
          }
        })
        if (!this.defaultExpandedKeys.includes(folderId)) {
          this.defaultExpandedKeys.push(folderId)
        }
        let key = ''
        let id = ''
        if (type === 'search') {//从搜索点击目录过来的
          key = _folderId
          id = 'tree-folder-' + key
          this.selectedKeys = [key]
          document.querySelector("#" + id) && document.querySelector("#" + id).scrollIntoView()
        } else if (type === 'clickPosition') {//点击定位
          key = 'file_' + this.activeFile.id
          id = key
          await this.getFile(folderId)
          this.selectedKeys = [key]
          document.querySelector("#" + id) && document.querySelector("#" + id).scrollIntoView()
        } else if (type === 'jump') {//作业审批跳过来的
          key = 'file_' + this.fileId
          id = key
          await this.getFile(folderId)
          this.selectedKeys = [key]
          document.querySelector("#" + id) && document.querySelector("#" + id).scrollIntoView()
        }


        // console.log('this.defaultExpandedKeys', this.defaultExpandedKeys)
      },
      async copyConfirmEvent () {

        const params = {
          id: Number(this.selectItem.id),
          fileName: this.copyName
        }
        let res = await this.$http.post('/file/copyFile', params, {
          headers: {
            projectId: Number(this.$route.query.projectId)
          }
        })
        if (res.code === 0) {
          this.isShowCopyFileName = false
          this.rightClickStyle.isShow = false
          this.$message.success('复制成功')
          this.getFile(this.selectItem.folderId)
        } else {
          this.$message.error(res.msg)
        }
      },
      searchSelect (item) {
        // console.log('item: ', item)
        if (item.type === 'file') {
          this.openFile(item)
        } else if (item.type === 'folder') {
          this.setPosition(item.id, 'search')
        }
      },
      fileDragover (e) {
        e.preventDefault()
        e.dataTransfer.dropEffect = 'none'
      }

    },
  }
</script>
<style lang="scss">
  .copy-confirm {
    .ant-modal-content {
      width: 360px;
      height: 210px;
      .ant-modal-body {
        padding: 14px 18px;
        .ant-modal-confirm-content {
          font-size: 16px;
        }
        .ant-modal-confirm-btns {
          margin-top: 50px;
        }
      }
    }
  }
  .copy-name-dialog {
    padding: 0;
    .ant-modal-close-x {
      width: 44px;
      height: 44px;
      line-height: 44px;
    }
    .ant-modal-content {
      padding: 0;
    }
    .ant-modal-body {
      padding: 0 !important;
    }
    .content {
      padding: 32px 16px 0 16px;
      font-size: 12px;
      .input-wrapper {
        .label {
          width: 70px;
          margin-bottom: 4px;
          color: #333;
        }
        .ant-input {
          height: 28px;
        }
      }
      .input-tip {
        color: #ff9300;
        margin-top: 4px;
      }
    }
    .footer {
      height: 44px;
      padding-right: 16px;
      // border-top: solid 1px #ddd;
      margin-top: 68px;
      .button-confirm {
        display: flex;
        justify-content: center;
        align-items: center;
      }
    }
  }
</style>
<style lang="scss" scoped>
  .tree-container {
    flex-basis: 220px;
    flex-shrink: 1;
    flex-grow: 0;
    height: 100%;
    border-right: solid 1px #d9d9d9;
    position: relative;
    font-size: 12px;
    overflow-x: hidden;
    .disabled {
      opacity: 0.3;
    }
    /deep/ .ant-tree-switcher-noop {
      display: none;
    }
    //关闭样式
    // /deep/ .ant-tree-switcher_close {
    //   background: url("../../../../assets/icons/fold.png") no-repeat;
    //   width: 14px;
    //   height: 14px;
    //   background-size: 100%;
    //   margin-top: 5px;
    //   i {
    //     display: none !important;
    //   }
    // }
    // //打开样式
    // /deep/ .ant-tree-switcher_open {
    //   background: url("../../../../assets/icons/expand.png") no-repeat;
    //   width: 14px;
    //   height: 14px;
    //   background-size: 100%;
    //   margin-top: 5px;
    //   i {
    //     display: none !important;
    //   }
    // }
    /deep/ .ant-tree-switcher {
      display: none;
    }
    .green {
      color: #51c313;
    }
    .cyanine {
      color: #16a8cc;
    }
    .tree-top {
      height: 32px;
      background: #ffffff;
      border-bottom: 1px solid #d9d9d9;
      padding: 0 10px;
      .search {
        flex: 1;
        /deep/ .ant-input {
          width: 100%;
          background: #fff;
        }
      }
      .top-btns {
        .btn {
          border: none;
          background: none;
          padding: 0 10px;
        }
      }
    }

    .resize {
      height: 100%;
      width: 10px;
      position: absolute;
      right: 5px;
      cursor: ew-resize;
      top: 0;
    }
    .right-click-wrapper {
      position: fixed;
      right: 0;
      top: 0;
      left: 0;
      bottom: 0;
      z-index: 1000;
      .sub {
        position: absolute;
        padding: 2px 0;
        width: 100px;
        background: #fff;
        box-shadow: 0 3px 6px -4px rgba(0, 0, 0, 0.12),
          0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 9px 28px 8px rgba(0, 0, 0, 0.05);
        .item {
          cursor: pointer;
          padding: 3px 5px;
          font-size: 12px;
          border: none;
          width: 100%;
          border-radius: 0;
          // &:hover {
          //   background: #006EFF;
          //   color: #fff;
          // }
        }
      }
    }

    .tree-wrapper {
      height: calc(100% - 32px);
      overflow-x: hidden;
      overflow-y: auto;
      .ant-tree {
        width: 100%;
        // padding-left: 10px;
        font-size: 12px;
        /deep/ li {
          position: relative;
          width: 100%;
          display: block;
          overflow: hidden;
          ul {
            padding-left: 0 !important;
          }
          .ant-tree-node-content-wrapper {
            // width: calc(100% - 24px);
            width: 100%;
            .ant-tree-title {
              width: 96%;
              display: block;

              .name {
                width: 100%;
                display: block;
                position: relative;
                i {
                  font-size: 16px !important;
                }
                .sub-name {
                  display: block;
                  width: 100%;
                  &.fold {
                    background: url("../../../../assets/icons/fold-new.png") left
                      center no-repeat;
                    background-size: 14px;
                    padding-left: 16px;
                    margin-left: -4px;
                  }
                  &.expand {
                    background: url("../../../../assets/icons/expand-new.png")
                      left center no-repeat;
                    background-size: 14px;
                    padding-left: 16px;
                    margin-left: -4px;
                  }
                }

                &.file-name {
                  margin-left: -4px;
                  .lock {
                    color: #cccccc;
                    font-style: normal;
                    margin-left: 8px;
                  }
                }
                img {
                  width: 14px;
                  height: 14px;
                }
                .folder-name {
                  width: 100%;
                  display: block;
                  overflow: hidden;
                  &.input {
                    border: none;
                    height: 80%;
                    margin-top: 3px;
                  }
                }
              }
            }
          }
          .operate {
            display: none;
            position: absolute;
            right: 0;
            top: 0;
            height: 22px;
            .item {
              margin-right: 5px;
            }
          }
          // &:first-of-type {
          //   .operate {
          //     top: 8px !important;
          //   }
          // }
          .ant-tree-treenode-switcher-close:hover {
            background: #e4f0ff !important;
            .operate {
              color: #4c9aff;
            }
          }
          .ant-tree-treenode-selected {
            background: #e4f0ff !important;
          }
          .ant-tree-node-selected {
            background: #e4f0ff !important;
          }
          .ant-tree-node-content-wrapper:hover {
            background: #e4f0ff !important;
            .operate {
              color: #4c9aff;
            }
          }
        }
      }
    }
  }
</style>
