/*
 * @Author: your name
 * @Date: 2022-01-21 16:20:10
 * @LastEditTime: 2022-10-11 15:05:32
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \src\utils\index.js
 */
import { api } from "@/api/http.js";
import common from "@/utils/common";
import { getCookieToken } from '@/utils/cookie'
export const processDates = (date) => {
  return date > 9 ? date : `0${date}`;
};
import store from '@/store'
import router, { resetRouter } from '@/router'
export const logout = () => {
  sessionStorage.clear();
  // localStorage.removeItem("TOKEN_COMMON");
  // localStorage.removeItem("erpticket");
  localStorage.removeItem("userInfo");
  localStorage.removeItem("sdpToken");
  localStorage.removeItem("sdpEmployeeNumber");
  localStorage.removeItem("projectInfo");
  store.dispatch("user/setUserId", null);
  store.dispatch("user/setUserInfo", null);
  router.push({
    name: "login",
  });
}
export const time = (d) => {
  let date = d;
  let Year = date.getFullYear();
  let month = processDates(date.getMonth() + 1);
  let day = processDates(date.getDate());
  let hour = processDates(date.getHours());
  let minute = processDates(date.getMinutes());
  let second = processDates(date.getSeconds());
  return `${Year}-${month}-${day} ${hour}:${minute}:${second}`;
};
export const createWatermarkStyle = (user, alpha) => {
  let map = {
    1: 0.12,
    1.5: 0.04,
    2: 0.03,
    2.5: 0.02,
    3: 0.015,
  };
  alpha = map[alpha];
  let { username, employeeNumber } = user;
  let conf = { w: 4.2, h: 4, tw: 1.6, th: 3.6 };
  let text = username || employeeNumber ? `${username || ""} ${employeeNumber || ""}` : "";
  text = `${text} ${time(new Date())}`;
  let rotate = (18 * Math.PI) / 180;
  let canvas = document.createElement("canvas");
  let ctx = canvas.getContext("2d");
  ctx.font = "bold normal 16px Microsoft YaHei";
  let width = Math.floor(Math.cos(rotate) * (ctx.measureText(text).width || 94));
  let height = Math.floor(Math.sin(rotate) * width);
  canvas.width = conf.w * width;
  canvas.height = conf.h * height;
  ctx.font = "bold normal 16px Microsoft YaHei";
  ctx.fillStyle = `rgba(0,110,235,${alpha})`;
  ctx.textBaseline = "middle";
  ctx.translate(16, 1.3 * height);
  ctx.rotate(-rotate);
  ctx.fillText(text, 0, 0);
  ctx.fillText(text, conf.tw * width, conf.th * height);
  let encode = canvas.toDataURL("image/gif");
  let style = document.createElement("style");
  style.type = "text/css";
  style.innerHTML = `.layout-watermark, .el-dialog{background-image:url(${encode})}`;
  document.querySelector("head").appendChild(style);
};



export const getInfo = async () => {
  // const token = common.getToken();
  const token = common.getToken()
  const employeeNumber = common.getEmployeeNumber();
  if (token) {
    let res = await api.get(`/auth/getUserInfo?token=${token}&employeeNumber=${employeeNumber}`);
    if (res.code === 0 && res.data) {
      sessionStorage.setItem("userId", res.data.id);
      localStorage.setItem("userInfo", JSON.stringify(res.data));
    }
    return res;
  }
};
export const getUserInfo = async () => {
  // const token = common.getToken();
  const token = getCookieToken().c_token
  const employeeNumber = common.getEmployeeNumber();
  if (token) {
    let res = await api.get(`/auth/getUserInfo?token=${token}&employeeNumber=${employeeNumber}`);
    if (res.code === 0 && res.data) {
      store.dispatch('user/setUserId', res.data.id)
      store.dispatch('user/setUserInfo', res.data)
      sessionStorage.setItem("userId", res.data.id);
      localStorage.setItem("userInfo", JSON.stringify(res.data));
      // 重置权限路由
      const roles = store.getters.userInfo.isAdmin === 1 ? ['admin'] : []
      const accessRoutes = await store.dispatch('permission/generateRoutes', roles)
      resetRouter()
      router.addRoutes(accessRoutes)
    }
    return res;
  }
};

//根据ticket更新用户信息
export const getTokenByTicket = async () => {
  const ticket = getCookieToken().e_token
  if (ticket) {
    let res = await api.post(`/auth/getTokenByTicket`, { ticket });
    if (res.code === 0 && res.data) {
      sessionStorage.setItem('employeeNumber', res.data.userInfo.employeeNumber)
      localStorage.setItem('employeeNumber', res.data.userInfo.employeeNumber)
      store.dispatch('user/setEmployeeNumber', res.data.userInfo.employeeNumber)
    }
    return res;
  }
};

/**
 * 组件子目录通过index.vue导出,通过name选项，在components目录的才自动注入全局组件
 * @param {*} Vue
 */
export const initGlobalComponents = (Vue) => {
  const requireComponent = require.context(
    // 其组件目录的相对路径
    "../components",
    // 是否查询其子目录
    true,
    // 匹配基础组件文件名的正则表达式
    /index.vue$/
  );
  requireComponent.keys().forEach((fileName) => {
    // 获取组件配置
    const componentConfig = requireComponent(fileName);
    let componentName = componentConfig.default.name;
    if (componentName) {
      // console.log('componentName', componentName)
      // 全局注册组件
      Vue.component(
        componentName,
        // 如果这个组件选项是通过 `export default` 导出的，
        // 那么就会优先使用 `.default`，
        // 否则回退到使用模块的根。
        componentConfig.default || componentConfig
      );
    }
  });
};

export function downloadByData(data, filename, mime, bom) {
  const blobData = typeof bom !== "undefined" ? [bom, data] : [data];
  const blob = new Blob(blobData, { type: mime || "application/octet-stream" });

  const blobURL = window.URL.createObjectURL(blob);
  const tempLink = document.createElement("a");
  tempLink.style.display = "none";
  tempLink.href = blobURL;
  tempLink.setAttribute("download", filename);
  if (typeof tempLink.download === "undefined") {
    tempLink.setAttribute("target", "_blank");
  }
  document.body.appendChild(tempLink);
  tempLink.click();
  document.body.removeChild(tempLink);
  window.URL.revokeObjectURL(blobURL);
}


