/*
 * @Author: your name
 * @Date: 2022-01-21 16:20:10
 * @LastEditTime: 2022-08-02 20:03:46
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \vue.config.js
 */
// const PROXY_ENV = 'kaifa'
const PROXY_ENV = 'dev'
// const PROXY_ENV = 'uat'
// const PROXY_ENV = 'prod'
const targets = {
    dev: 'http://127.0.0.1:12222',
    // dev:'http://10.121.18.2:12281',
    uat: 'http://127.0.0.1:12222',
    prod: 'http://127.0.0.1:12222',
}

console.log(`代理到${PROXY_ENV}环境：${targets[PROXY_ENV]}`)
const webpack = require('webpack')
module.exports = {
    plugins: [
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
            'window.jQuery': 'jquery'
        })
    ]
}
module.exports = {
    pages: {
        index: {
            entry: 'src/main.js',
            title: '赤兔实时计算',//放要改的title名
        },
    },
    productionSourceMap: false,   // 关闭sourcemap
    // 开启compiler模板支持，原因：vue有两种形式的代码 compiler（模板）模式和runtime模式（运行时），vue模块的package.json的main字段默认为runtime模式， 指向了"dist/vue.runtime.common.js"位置
    runtimeCompiler: true,
    devServer: {
        host: '127.0.0.1',
        port: 9527,
        /*不检测host*/
        disableHostCheck: true,
        proxy: {
            '/sdp': {
                target: targets[PROXY_ENV],
                changeOrigin: true,
                secure: false,
                pathRewrite: {
                    '^/sdp': '/'
                }
            }
        }
    }
}
