module.exports = {
  root: true,
  env: {
    node: true
  },
  'extends': [
    'plugin:vue/essential',
    'eslint:recommended'
    // '@vue/standard'
  ],
  rules: {
    'camelcase': 0,
    'comma-dangle': [2, 'only-multiline'],
    'indent': 0,
    'no-extend-native': 2,
    'no-multiple-empty-lines': 0,
    'no-return-assign': 0,
    'object-curly-spacing': 0,
    'space-before-function-paren': [0, 'always'],
    'no-debugger': 0
  },
  parserOptions: {
    parser: 'babel-eslint'
  }
}
