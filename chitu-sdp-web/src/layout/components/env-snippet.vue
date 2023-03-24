<template>
  <el-dropdown class="env-selector"
               trigger="click"
               :disabled="isDisabled"
               @command="onSelect"
               @visible-change="visibleEnvChange">
    <div class="env-selector__title"
         :style="selectedEnv ? `color: ${envColorMap[selectedEnv.value]}` : ''">
      <span>
        <i v-if="selectedEnv"
           class="env-selector__dot env-selector__dot--processing" />
        <span v-if="selectedEnv">{{ selectedEnv.label }}</span>
        <span v-else
              style="font-style: oblique">未选择</span>
        <i class="icon-arrow"
           :class="iconEnvClass" />
      </span>
    </div>
    <el-dropdown-menu slot="dropdown"
                      class="env-selector__body">
      <el-dropdown-item v-for="item in envList"
                        :key="item.value"
                        :command="item.value"
                        class="env-item"
                        :class="{ 'env-item--selected': value == item.value }">
        <i class="env-selector__dot"
           :class="{ 'env-selector__dot--processing': value == item.value }"
           :style="`color: ${envColorMap[item.value]}`" />
        <span> {{ item.label }}</span>
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
  const defaultEnvColorMap = {
    /** uat */
    1: '#25CBCE',
    /** 灰度 */
    3: '#738EA6',
    /** 生产 */
    4: '#1790FF'
  }

  export default {
    name: 'EnvSnippet',
    props: {
      value: String,
      envList: Array,
      isDisabled: {
        type: Boolean,
        default: false
      }
    },
    data () {
      return {
        iconEnvClass: 'el-icon-arrow-down'
      }
    },
    computed: {
      selectedEnv () {
        return this.envList.find((item) => item.value === this.value)
      },
      envColorMap () {
        if (this.envList && this.envList.some(item => !!item.color)) {
          const envColorMap = {}
          this.envList.forEach(item => {
            envColorMap[item.value] = item.color
          })
          return envColorMap
        }
        return defaultEnvColorMap
      }
    },
    methods: {
      visibleEnvChange (visible) {
        this.iconEnvClass = visible ? 'el-icon-arrow-up' : 'el-icon-arrow-down'
      },
      onSelect (value) {
        this.$emit('change', value)
        this.$emit('update:value', value)
        this.$emit(
          'select',
          this.envList.find((item) => item.value === value)
        )
      }
    }
  }
</script>

<style lang="scss" scope>
  .env-selector {
    &__title {
      font-size: 12px;
      user-select: none;
      position: relative;
      cursor: pointer;
      min-width: 98px;
      line-height: 22px;
      height: 24px;
      padding: 0 8px;
      color: #333333;
      border: 1px solid currentColor;
      border-radius: 4px;

      /** 半透明背景，颜色自动跟随当前字体颜色 */
      &::before {
        content: "";
        display: block;
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: currentColor;
        border: 1px solid transparent;
        opacity: 0.05;
      }
    }

    .icon-arrow {
      position: absolute;
      right: 6px;
      font-size: 12px;
      height: 12px;
      line-height: 12px;
      top: 0;
      bottom: 0;
      margin: auto 0;
    }
  }

  // ant design 的一个呼吸效果
  .env-selector__dot {
    margin-right: 6px;
    position: relative;
    top: -1px;
    display: inline-block;
    width: 5px;
    height: 5px;
    background-color: currentColor;
    vertical-align: middle;
    border-radius: 50%;

    &--processing {
      &::after {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border: 1px solid currentColor;
        border-radius: 50%;
        animation: antStatusProcessing 1.2s ease-in-out infinite;
        content: "";
      }
    }
  }

  @keyframes antStatusProcessing {
    0% {
      transform: scale(0.8);
      opacity: 0.5;
    }

    to {
      transform: scale(2.4);
      opacity: 0;
    }
  }
</style>

<style lang="scss">
  .env-selector.el-dropdown [disabled] {
    cursor: default;
  }
  .env-selector__body {
    .env-item.el-dropdown-menu__item {
      font-size: 12px;
      cursor: pointer;
      min-width: 98px;
      line-height: 32px;
      height: 32px;
      padding: 0 8px;
      color: #333333;
      background-color: white;

      &--selected {
        background: #ebf4ff;
      }
    }
  }
</style>
