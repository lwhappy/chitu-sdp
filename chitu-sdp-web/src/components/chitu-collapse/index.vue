<template>
  <div class="collapse"
       :style="
      (isCollapsed
        ? `width: ${finalWidth}; background-color: white; ${collapsedStyle}`
        : `width: ${finalWidth};`) +
      (hidden
        ? `width: 0; overflow: hidden; border: none; ${hiddenStyle}`
        : '') +
      collapsingStyleFinal
    "
       v-pullable.right="
      !!pullAbleSetting ? { ...pullAbleSetting, onChange: onPullChange } : false
    ">
    <div class="collapse-slot-wrapper"
         :hidden="isCollapsed">
      <slot />
    </div>
    <div class="collapse-btn"
         :style="btnStyle"
         @click="onSwitch"
         @mouseenter="onHover(true)"
         @mouseleave="onHover(false)">
      <!-- <btn-icon :isCollapsed="isCollapsed" :isHover="isHover" /> -->
      <component :is="showBtnCom"
                 :isCollapsed="isCollapsed"
                 :isHover="isHover" />
    </div>
  </div>
</template>

<script>
  import BtnIcon from './btn-icon.vue'
  import BtnArrow from './btn-arrow.vue'
  export default {
    // components: {  },
    name: 'chitu-collapse',
    props: {
      width: {
        type: String,
        default: ''
      },
      collapsedStyle: {
        type: String,
        default: ''
      },
      hiddenStyle: {
        type: String,
        default: ''
      },
      btnStyle: {
        type: Object,
        default: () => ({})
      },
      pullAbleSetting: {
        type: Object,
        default: null
      },
      showBtn: {
        type: String,
        default: 'btnIcon'
      },
      collapsingStyle: {
        type: String,
        default: 'transition: all 300ms;'
      },
      // 收缩后的宽度
      collapsedWidth: {
        type: [String, Number],
        default: '20px'
      }
    },
    data () {
      return {
        finalWidth: '',
        isCollapsed: false,
        isHover: false,
        hidden: false, // 完全隐藏该组件，包括折叠按钮
        collapsingStyleFinal: 'transition: all 300ms;'
      }
    },
    computed: {
      showBtnCom () {
        const name = this.showBtn.toLowerCase()
        let comp = BtnIcon
        switch (name) {
          case 'btnarrow':
            comp = BtnArrow
            break
          default:
            break
        }
        return comp
      }
    },
    watch: {
      width: {
        handler (width) {
          this.finalWidth = width
        },
        immediate: true
      },
      isCollapsed (n) {
        this.$emit('collapsed', n)
      }
    },
    mounted () {
      this.mountedToParent()
    },
    destroyed () {
      this.unMountedToParent()
    },
    methods: {
      /**
       * 尝试注册到父组件 key-collapse-group 中，便于实现多 key-collapse 之间的互动
       */
      mountedToParent () {
        if (this.$parent.$options.name === 'chitu-collapse-group') {
          this.$parent.onChildMounted(this)
        }
      },
      /**
       * 尝试从父组件 key-collapse-group 中卸载
       */
      unMountedToParent () {
        if (this.$parent.$options.name === 'chitu-collapse-group') {
          this.$parent.onChildDestroyed(this)
        }
      },
      /**
       * 完全隐藏该组件，包括折叠按钮，目前仅用于父组件 chitu-collapse-group 调度
       */
      hide (hidden) {
        this.hidden = hidden
      },
      onSwitch (value, width = this.width) {
        if (typeof value === 'boolean') {
          this.isCollapsed = value
        } else {
          this.isCollapsed = !this.isCollapsed
        }
        if (!this.isCollapsed) {
          this.finalWidth = width
        } else {
          this.finalWidth = this.collapsedWidth
        }
        // 尝试通知父组件 key-collapse-group 当前子组件的折叠状态
        if (this.$parent.$options.name === 'chitu-collapse-group') {
          this.$parent.onChildSwitch(this.isCollapsed)
        }
      },
      onHover (isHover) {
        this.isHover = isHover
      },
      onPullChange (detail) {
        // 拖拉期间移除过渡动画，否则拖拽会感觉卡顿
        this.collapsingStyleFinal = ''
        clearTimeout(this.collapsingStyleTimer)
        this.collapsingStyleTimer = setTimeout(() => {
          this.collapsingStyleFinal = this.collapsingStyle
        }, 100)

        this.pullAbleSetting.onChange && this.pullAbleSetting.onChange(detail)
        if (this.isCollapsed) {
          if (
            !this.pullAbleSetting.minWidth ||
            detail.width > this.pullAbleSetting.minWidth
          ) {
            // 拖拉时如果是折叠状态，并且宽度超过了最小宽度，自动切换为展开
            this.onSwitch(false, detail.width + 'px')
          }
        } else {
          if (
            this.pullAbleSetting.minWidth &&
            detail.width <= this.pullAbleSetting.minWidth
          ) {
            // 拖拉时如果是展开状态，并且宽度小于最小宽度，自动切换为折叠
            this.onSwitch(true)
          }
        }
        this.$emit('moveEnd', detail)
        if (this.isCollapsed) {
          return { width: this.finalWidth }
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
  $btn-size: 44px;

  .collapse {
    height: 100%;
    width: 100%;
    position: relative;

    &-slot-wrapper {
      height: 100%;
      width: 100%;
      overflow: hidden;
    }

    &-btn {
      position: absolute;
      z-index: 9;
      top: 50%;
      right: 0;
      transform: translate(100%, -50%);
      cursor: pointer;
      font-size: 10px;

      .svg-border {
        fill: #ffffff;
        stroke: #d9d9d9;
        box-shadow: 1px 0 5px black;
      }

      .svg-triangle {
        fill: #b8bcbf;
        stroke: #b8bcbf;
      }

      &:hover {
        .svg-border {
          fill: #f1f1f1;
        }
      }
    }
  }
</style>
