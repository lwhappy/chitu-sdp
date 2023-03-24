const common = {
  toLine: (value) => { // 驼峰转下划线
    return value.replace(/([A-Z])/g, "_$1").toLowerCase()
  },
  objectToObject: (objectOne, objectTwo) => { // 两对象遍历键值赋值
    Object.keys(objectOne).forEach(key => {
      objectOne[key] = objectTwo[key]
    })
    return objectOne
  },
  empty: (value) => {//空字符替换成'-'
    if (value === '' || typeof value === 'undefined') {
      return '-'
    } else {
      return value
    }
  },
  timeToDate: (d) => {
    var processDates = (date) => {
      return date > 9 ? date : `0${date}`
    }
    const date = new Date(d)
    let Year = date.getFullYear()
    let month = processDates(date.getMonth() + 1)
    let day = processDates(date.getDate())
    let hour = processDates(date.getHours())
    let minute = processDates(date.getMinutes())
    let second = processDates(date.getSeconds())
    return `${Year}-${month}-${day} ${hour}:${minute}:${second}`
  },
  toClearCache: (_this) => {
    if (_this.$vnode && _this.$vnode.data.keepAlive) {
      // _this.$route.meta.keepAlive = false
      if (_this.$vnode.parent && _this.$vnode.parent.componentInstance && _this.$vnode.parent.componentInstance.cache) {
        if (_this.$vnode.componentOptions) {
          var key = _this.$vnode.key == null ? _this.$vnode.componentOptions.Ctor.cid + (_this.$vnode.componentOptions.tag ? `::${_this.$vnode.componentOptions.tag}` : '') : _this.$vnode.key
          var cache = _this.$vnode.parent.componentInstance.cache
          var keys = _this.$vnode.parent.componentInstance.keys

          if (cache[key]) {
            if (keys.length) {
              var index = keys.indexOf(key)
              if (index > -1) {
                keys.splice(index, 1)
              }
            }
            delete cache[key]
          }
          _this.$forceUpdate()
        }
      }
      // _this.$route.meta.keepAlive = true
    }
  },
  getToken () {
    return localStorage.getItem('sdpToken')
  },
  getEmployeeNumber () {
    return localStorage.getItem('sdpEmployeeNumber')
  },
  getPriority () {
    const list = [
      { value: 1, label: '1', isDefault: false },
      { value: 2, label: '2', isDefault: false },
      { value: 3, label: '3', isDefault: false },
      { value: 4, label: '4', isDefault: false },
    ]
    const labelPrefix = 'P'
    return {
      list: list,
      labelPrefix: labelPrefix
    }
  },
  copyContent (content) {
    let oInput = document.createElement('input')
    oInput.value = content
    oInput.display = 'none'
    document.body.appendChild(oInput)
    oInput.select() // 选择对象;
    document.execCommand('Copy') // 执行浏览器复制命令
    oInput.remove()
  }

}
export default common