<template>
  <div class="container" :class="isHover ? 'container--hover' : ''">
    <div class="border"></div>
    <div
      style="
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
      ">
      <div
        :style="
          isCollapsed
            ? 'transform: rotate(180deg); transform-origin: center;'
            : ''
        ">
        <svg
          width="1em"
          height="1em"
          viewBox="-2 -2 14 10"
          style="
            transform-origin: center;
            transform: scale(0.75) rotate(-90deg);
          ">
          <polygon
            class="svg-triangle"
            stroke-linejoin="round"
            points="5,0 0,7 10,7"
            stroke-width="2"></polygon>
        </svg>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    isCollapsed: Boolean,
    isHover: Boolean
  }
}
</script>

<style lang="scss" scoped>
.container {
  position: relative;
  width: 1.2em;
  height: 60px;
  //overflow: hidden;

  &--hover {
    .border {
      background-color: #f1f1f1;

      &::after {
        display: none;
      }
    }
  }
}

.border {
  width: 100%;
  height: 100%;
  background-color: #fff;
  border: 1px solid #d9d9d9;
  border-left-width: 0;
  border-radius: 0px 4px 4px 0px;
  box-shadow: 0.5em 0.1em 0.5em rgba(241, 241, 241, 0.87);

  &::after {
    content: '';
    display: block;
    width: 50%;
    height: 100%;
    background: linear-gradient(to right, #e7e7e7, #f4f4f4 50%, #fff);
    border-radius: 0 5em 5em 0;
  }
}

.svg-triangle {
  fill: #b8bcbf;
  stroke: #b8bcbf;
}
</style>
