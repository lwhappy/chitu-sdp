<template>
  <!-- hasError: { // 1是404 2是没权限访问 -->
  <layout :hasError="1"></layout>
</template>

<script>
  import layout from '@/layout/'
  export default {
    components: {
      layout
    }
  }
</script>

<style>
</style>