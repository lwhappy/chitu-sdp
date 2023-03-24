<template>
  <div class="info">
    <template>
      <HealthSnippet :fetchHealthInfo="fetchInfo" />
    </template>

  </div>
</template>

<script>

  import axios from 'axios'

  export default {
    name: 'TestInfo',
    mixins: [],
    components: {

    },
    directives: {
    },
    data () {
      return {

      }
    },
    created () {
      console.log('test/info')
    },
    watch: {

    },
    methods: {
      async fetchInfo () {
        const res = await axios.get("/bigdata-sdp/info")
        return res.data
      },
    },
    computed: {

    }
  }
</script>
<style lang="less" scoped>
</style>
