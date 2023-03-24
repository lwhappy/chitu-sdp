<!--
 * @description: 
 * @Author: lijianguo19
 * @Date: 2022-08-11 15:39:35
 * @FilePath: \src\directives\Loading\loading.vue
-->

<template>
  <div v-show="visible"
       class="global-loading-mask">
    <div class="loading-spinner">
      <div class="loading-horse"></div>
    </div>
  </div>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
      };
    },
  };
</script>
<style lang="scss" scoped>
  .global-loading-mask {
    position: absolute;
    z-index: 20;
    background-color: rgba(255, 255, 255, 0.9);
    margin: 0;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    transition: opacity 0.3s;

    .loading-spinner {
      top: 50%;
      margin-top: -21px;
      width: 100%;
      text-align: center;
      position: absolute;

      .loading-horse {
        background: url("~@/assets/icons/sdp-loading.gif") no-repeat;
        background-size: 100%;
        width: 80px;
        height: 80px;
        margin: 0 auto;
      }

      .circular {
        height: 42px;
        width: 42px;
        -webkit-animation: loading-rotate 2s linear infinite;
        animation: loading-rotate 2s linear infinite;

        .path {
          -webkit-animation: loading-dash 1.5s ease-in-out infinite;
          animation: loading-dash 1.5s ease-in-out infinite;
          stroke-dasharray: 90, 150;
          stroke-dashoffset: 0;
          stroke-width: 2;
          stroke: #0066ff;
          stroke-linecap: round;
        }
      }
    }
  }
</style>