function createDragableLine(direction, id) {
  const lineEle = document.createElement('div')
  lineEle.className = `pullable-line pullable-line--${direction}`
  lineEle.id = id
  return lineEle
}

/** 根据当前鼠标坐标、初始鼠标坐标、初始容器尺寸来确定当前容器的尺寸 */
function computeFinalSize(
  x,
  y,
  dragStartX,
  dragStartY,
  startWidth,
  startHeight,
  direction
) {
  // 根据当前鼠标坐标和拖拉初始时的鼠标坐标计算出x轴、y轴拖拉距离
  const offsetX = x - dragStartX
  const offsetY = y - dragStartY
  // 再根据拖拉距离和拖拉初始时的容器宽高计算出最终的容器宽高
  let finalWidth = startWidth,
    finalHeight = startHeight
  switch (direction) {
    case 'top': {
      finalHeight = startHeight - offsetY
      break
    }
    case 'bottom': {
      finalHeight = startHeight + offsetY
      break
    }
    case 'left': {
      finalWidth = startWidth - offsetX
      break
    }
    case 'right': {
      finalWidth = startWidth + offsetX
      break
    }
  }
  return { finalWidth, finalHeight }
}

function updateSize(
  container,
  x,
  y,
  dragStartX,
  dragStartY,
  startWidth,
  startHeight,
  direction,
  sizeLimit,
  onChange,
  stopDrag
) {
  let { finalWidth, finalHeight } = computeFinalSize(
    x,
    y,
    dragStartX,
    dragStartY,
    startWidth,
    startHeight,
    direction
  )
  // 如果设置了拖拉限制，并且计算结果超出了限制值，则需要修正最终结果为限制值
  if (sizeLimit.minWidth && finalWidth < sizeLimit.minWidth) {
    finalWidth = sizeLimit.minWidth
  }
  if (sizeLimit.maxWidth && finalWidth > sizeLimit.maxWidth) {
    finalWidth = sizeLimit.maxWidth
  }
  if (sizeLimit.minHeight && finalHeight < sizeLimit.minHeight) {
    finalHeight = sizeLimit.minHeight
  }
  if (sizeLimit.maxHeight && finalHeight > sizeLimit.maxHeight) {
    finalHeight = sizeLimit.maxHeight
  }

  let finalWidthWithUnit = finalWidth + 'px'
  let finalHeightWithUnit = finalHeight + 'px'

  // 通过onChange回调业务层逻辑
  // 并优先取业务计算得到的尺寸
  if (onChange) {
    const { width: returnedWidth, height: returnedHeight } =
      onChange({ width: finalWidth, height: finalHeight, stopDrag }) || {}
    if (typeof returnedWidth === 'string') {
      finalWidthWithUnit = returnedWidth
    }
    if (typeof returnedWidth === 'number') {
      finalWidthWithUnit = returnedWidth + 'px'
    }
    if (typeof returnedHeight === 'string') {
      finalHeightWithUnit = returnedHeight
    }
    if (typeof returnedHeight === 'number') {
      finalHeightWithUnit = returnedHeight + 'px'
    }
  }

  if (direction === 'top' || direction === 'bottom') {
    // 拖拽上边/下边时只更新高度
    container.style.height = finalHeightWithUnit
  } else {
    // 拖拽左边/右边时只更新宽度
    container.style.width = finalWidthWithUnit
  }
}

function bindDragEvent(container, direction, id, sizeLimit, onChange) {
  let startWidth, startHeight // 拖拉起始时的容器宽高
  let dragStartX, dragStartY // 拖拉起始时的鼠标坐标
  let x, y // 当前鼠标坐标
  let draging = false
  const stopDrag = function () {
    draging = false
  }
  const updateFunc = function () {
    updateSize(
      container,
      x,
      y,
      dragStartX,
      dragStartY,
      startWidth,
      startHeight,
      direction,
      sizeLimit,
      onChange,
      stopDrag
    )
    if (draging) {
      requestAnimationFrame(updateFunc)
    }
  }
  document.addEventListener('mousedown', function (e) {
    if (e.target.id !== id) return
    e.preventDefault()
    startWidth = container.offsetWidth
    startHeight = container.offsetHeight
    dragStartX = e.x
    dragStartY = e.y
    x = e.x
    y = e.y
    draging = true
    requestAnimationFrame(updateFunc)
  })
  document.addEventListener('mousemove', function (e) {
    if (draging) {
      e.preventDefault()
      x = e.x
      y = e.y
    }
  })
  document.addEventListener('mouseup', function (e) {
    if (draging) {
      stopDrag()
      e.preventDefault()
      x = e.x
      y = e.y
    }
  })
}

function initDragableLine(container, direction, id, sizeLimit, onChange) {
  const lineEle = createDragableLine(direction, id)
  bindDragEvent(container, direction, id, sizeLimit, onChange)
  container.appendChild(lineEle)
}

function init(el, binding) {
  const {
    minWidth,
    minHeight,
    maxWidth,
    maxHeight,
    onChange,
    pullPos = '',
    disablePull = false
  } = typeof binding.value === 'object' ? binding.value : {}
  // 禁止拖动、只可以通过点击收缩
  if (disablePull) return
  let {
    top = pullPos === 'top',
    right = pullPos === 'right',
    bottom = pullPos === 'bottom',
    left = pullPos === 'left'
  } = binding.modifiers
  if (!top && !right && !bottom && !left) {
    // 未指定可拖拽边相当于四个边都可以拖拽
    top = right = bottom = left = true
  }
  const sizeLimit = { minWidth, minHeight, maxWidth, maxHeight }
  const idPrefix = Math.random().toString(36).slice(-8)
  top && initDragableLine(el, 'top', idPrefix + '-top', sizeLimit, onChange)
  right &&
    initDragableLine(el, 'right', idPrefix + '-right', sizeLimit, onChange)
  bottom &&
    initDragableLine(el, 'bottom', idPrefix + '-bottom', sizeLimit, onChange)
  left && initDragableLine(el, 'left', idPrefix + '-left', sizeLimit, onChange)
}

export default {
  bind(el, binding) {
    if (binding.value === false) {
      return
    }

    setTimeout(() => {
      // setTimeout中才能拿到position
      const elPosition = getComputedStyle(el).position
      if (!elPosition || elPosition === 'static') {
        // 必须将目标元素设为非static
        el.style.position = 'relative'
      }
      el.style.flex = 'none' // 父容器flex布局的情况下，需将flex设为none，否则可能width/height值不生效
      init(el, binding)
    }, 0)
  }
}
