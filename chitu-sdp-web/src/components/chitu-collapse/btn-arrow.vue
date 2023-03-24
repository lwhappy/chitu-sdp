
<template>
  <div class="container"
       :class="{
      'container--hover': isHover,
      collapsed: isCollapsed,
      'un-collapsed': !isCollapsed
    }">
    <div class="arrow"
         :style="
        isCollapsed ? 'transform: rotate(0deg) translate(-50%, -50%)' : ''
      ">
      <div>
        <a-icon type="caret-right" />
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'btn-arrow',
    props: {
      isCollapsed: Boolean,
      isHover: Boolean
    },
    watch: {
      isHover (n) {
        this.setLineHover(n)
      }
    },
    methods: {
      setLineHover (flag) {
        const el = document.querySelector('.pullable-line')
        if (el) {
          flag
            ? el.classList.add('line-hover')
            : el.classList.remove('line-hover')
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    position: relative;
    width: 10px;
    height: 60px;
    background: #fff;
    border: 1px solid #dee2ea;
    border-radius: 0px 4px 4px 0px;
    box-shadow: 3px 3px 10px 0px rgba(213, 220, 227, 0.6);
    &:hover {
      background-color: #006eff;
      .arrow {
        color: #fff;
      }
      .border {
        background-color: #006eff;
        &::after {
          display: none;
        }
      }
    }
    &.un-collapsed {
      border-right: none;
    }
    &.collapsed {
      border-radius: 0 4px 4px 0;
    }
    .arrow {
      position: absolute;
      left: 50%;
      top: 50%;
      transform: rotate(180deg) translate(50%, 50%);
    }
  }
</style>
<style>
  .pullable-line.line-hover {
    background-color: #006eff;
  }
</style>
