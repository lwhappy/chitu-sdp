<template>
  <div class="tip-container justify-start"
       v-if="visible && isUAT">
    <span>已支持生产域名的UAT环境，后续会在新环境迭代转环境功能进行作业同步，若需在UAT环境进行作业开发，请前往新的UAT环境,</span>
    <span class="to-btn"
          @click="toProd">
      立即前往
      <a-icon type="double-right" />
    </span>
    <a-icon class="close-btn"
            type="close"
            @click="visible=false" />
  </div>
</template>

<script>

  export default {
    name: '',
    components: {},
    props: {},
    data () {
      return {
        visible: true
      };
    },
    computed: {
      isUAT () {
        return window.location.hostname === 'localhost'
      }
    },
    watch: {},
    created () { },
    mounted () { },
    methods: {
      toProd () {
        const url = window.location.href
        this.openWindow(url)
      },
      openWindow (url) {
        const a = document.createElement('a');
        a.href = url;
        a.target = '_blank';
        a.style.display = 'none';

        const interceptTemp = window.__intercept__
        if (!interceptTemp) {
          window.__intercept__ = 0
        }

        a.click()
        window.__intercept__ = interceptTemp
      }
    },
  }
</script>
<style lang='scss' scoped>
  .tip-container {
    position: absolute;
    top: 0;
    left: 50%;
    transform: translate(-50%, 0);
    height: 32px;
    line-height: 32px;
    padding: 0 6px;
    color: #2b2f37;
    background: rgba(240, 246, 255, 0.8);
    border: 1px solid #8dbaff;
    border-radius: 2px;
    box-shadow: 0px 6px 10px 0px rgba(145, 168, 191, 0.2);
    .to-btn {
      margin-left: 5px;
      color: #0066ff;
      cursor: pointer;
    }
    .close-btn {
      margin-left: 5px;
      font-size: 15px !important;
      cursor: pointer;
    }
  }
</style>