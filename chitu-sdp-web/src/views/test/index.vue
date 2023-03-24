<template>
  <section class="test">
    <router-view />
  </section>
</template>

<script>
  export default {
    name: 'Test',
    computed: {

    },
    watch: {

    },
    methods: {
      getRandom () {
        return Math.random()
      }
    }
  }
</script>

<style lang="scss" scoped>
</style>
