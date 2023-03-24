
1. 下载
    node.js 版本：v14.20.1
2. 安装
    方式一.使用npm安装
npm install
    方式二.使用淘宝镜像安装
        npm install cnpm -g --registry=https://registry.npm.taobao.org
        cnpm install
    方式三.使用yarn安装
        npm install
        yarn
3. 本地运行
        npm run dev
        浏览器打开http://localhost:9527/
4. 部署到服务器
        npm run build，构建完成后将dist目录下的所有文件放到nginx服务器根目录
5. 本地环境配置
    5.1 打开vue.config.js
    5.2 修改服务端地址，PROXY_ENV设置成'dev',targets对象的dev属性设置为'http://127.0.0.1:12222'，请根据你本地服务端的地址及端口修改
    5.3 配置devServer的proxy,请求后端接口统一添加一个根路径'/sdp'（如果不用/sdp,请同步修改src/utils/config.js中confg对象的apiRoot）,用于解决前端跨域问题
    