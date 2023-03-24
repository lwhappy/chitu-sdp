import Vue from 'vue'

// v-drag-dragDom: 弹窗拖拽
Vue.directive('drag', (el, bindings, vnode) => {
    Vue.nextTick(() => {
        let { visible } = vnode.componentInstance
        if (!visible) return
        let dragDom = el.getElementsByClassName('ant-modal')[0]
        let dialogHeaderEl = el.getElementsByClassName('ant-modal-header')[0]
        dialogHeaderEl.style.cssText += ';cursor:move;';
        dragDom.style.cssText += ';padding-bottom:0px;';
        const sty = (function () {
            if (window.document.currentStyle) {
                return (dom, attr) => dom.currentStyle[attr];
            } else {
                return (dom, attr) => getComputedStyle(dom, false)[attr];
            }
        })();
        dialogHeaderEl.onmousedown = e => {
            // 鼠标按下，计算当前元素距离可视区的距离
            const disX = e.clientX - dialogHeaderEl.offsetLeft;
            const disY = e.clientY - dialogHeaderEl.offsetTop;
            const screenWidth = document.body.clientWidth; // body当前宽度
            const screenHeight = document.documentElement.clientHeight; // 可见区域高度(应为body高度，可某些环境下无法获取)
            const dragDomWidth = dragDom.offsetWidth; // 对话框宽度
            const dragDomheight = dragDom.offsetHeight; // 对话框高度
            const minDragDomLeft = dragDom.offsetLeft;
            const maxDragDomLeft = screenWidth - dragDom.offsetLeft - dragDomWidth;
            const minDragDomTop = dragDom.offsetTop;
            const maxDragDomTop = screenHeight - dragDom.offsetTop - dragDomheight;
            // 获取到的值带px 正则匹配替换
            let styL = sty(dragDom, 'left');
            let styT = sty(dragDom, 'top');
            // 注意在ie中 第一次获取到的值为组件自带50% 移动之后赋值为px
            if (styL.includes('%')) {
                styL = +document.body.clientWidth * (+styL.replace(/%/g, '') / 100);
                styT = +document.body.clientHeight * (+styT.replace(/%/g, '') / 100);
            } else {
                styL = +styL.replace(/\px/g, '');
                styT = +styT.replace(/\px/g, '');
            }

            document.onmousemove = function (e) {
                // 通过事件委托，计算移动的距离
                let left = e.clientX - disX;
                let top = e.clientY - disY;

                // 边界处理
                if (-left > minDragDomLeft) {
                    left = -minDragDomLeft;
                } else if (left > maxDragDomLeft) {
                    left = maxDragDomLeft - 4;
                }
                if (-top > minDragDomTop) {
                    top = -minDragDomTop;
                } else if (top > maxDragDomTop) {
                    top = maxDragDomTop - 4;
                }
                // 移动当前元素
                dragDom.style.cssText += `;left:${left + styL}px;top:${top + styT}px;`;
            };

            document.onmouseup = function () {
                document.onmousemove = null;
                document.onmouseup = null;
            };
            return false;
        };
    })
})