<template>
  <div class="tab-container">
    <div class="draft justify-center">
      <p>Drafts</p>
    </div>
  </div>
</template>

<script>

  export default {
    name: "Tab",
    components: {

    },
    mixins: [],
    data () {
      return {
        form: {
          name: '',
          // id: '',
          ownerId: 0,
        },
        pagination: {
          current: 1,
          size: 10,
          total: 0
        },
      }
    },
    computed: {

    },
    mounted () {

    },
    methods: {

    },
  }
</script>
<style lang="scss" scoped>
  .tab-container {
    width: 44px;
    border-right: solid 1px #d9d9d9;
    height: 100%;
    .draft {
      width: 44px;
      height: 63px;
      background: #f0f0f0;
      border: 1px solid #dddddd;
      flex-direction: column;
      color: #333;
      font-size: 12px;
    }
  }
</style>
