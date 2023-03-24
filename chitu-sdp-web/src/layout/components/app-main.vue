<!--
 * @Author: your name
 * @Date: 2021-10-15 11:19:56
 * @LastEditTime: 2022-10-08 09:41:36
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\layout\components\app-main.vue
-->
<template>
  <section class="app-main">
    <div style="text-align: center;height: 300px;line-height: 300px;font-size: 24px;color: #A6ACB5;"
         v-if="hasError">
      <template v-if="hasError === 1">
        您访问的地址可能有误, 找不到对应页面
      </template>
      <template v-else>
        您暂无权限访问页面
      </template>
    </div>
    <template v-else>
      <router-view v-if="!$route.meta.keepAlive"
                   :key="getRandom()" />
      <keep-alive>
        <router-view v-if="$route.meta.keepAlive" />
      </keep-alive>
    </template>
  </section>
</template>

<script>
  export default {
    name: 'AppMain',
    props: {
      hasError: { // 1是404 2是没权限访问
        type: Number
      }
    },
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
  .app-main {
    height: calc(100% - 30px);
  }
</style>
