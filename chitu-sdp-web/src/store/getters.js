/*
 * @Author: your name
 * @Date: 2021-10-15 11:08:00
 * @LastEditTime: 2022-10-13 21:25:50
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \src\store\getters.js
 */
const getters = {
  // sidebar: state => state.app.sidebar,
  // size: state => state.app.size,
  // device: state => state.app.device,
  visitedViews: state => state.tagsView.visitedViews,
  cachedViews: state => state.tagsView.cachedViews,
  removeRouteName: state => state.tagsView.removeRouteName,
  isRemoveTag: state => state.tagsView.isRemoveTag,
  lookUpOptions: state => state.user.dict,
  userInfo: state => state.user.userInfo,
  buttonPermission: state => state.permission.buttonPermission,
  // token: state => state.user.token,
  // avatar: state => state.user.avatar,
  // name: state => state.user.name,
  // introduction: state => state.user.introduction,
  // roles: state => state.user.roles,
  // permission_routes: state => state.permission.routes,
  // errorLogs: state => state.errorLog.logs
  dict: state => state.user.dict,
  userPermissionCode: state => state.permission.userPermissionCode,
  projectId: state => state.project.projectId,
  jobInfo: state => state.job.jobInfo,
  ruleInfo: state => state.rule.ruleInfo,
  token: state => state.user.token,
  employeeNumber: state => state.user.employeeNumber,
  userId: state => state.user.userId,
  saveJobTime: state => state.project.saveJobTime,
  updateApproveData: state => state.approve.updateApproveData,
  env: state => state.global.env,
  currentEnv: state => state.global.currentEnv,
  currentProject: state => state.global.currentProject,
  configInfoActive: state => state.job.configInfoActive,
  alarmConfigActive: state => state.job.alarmConfigActive,
  permissionRoutes: state => state.permission.permissionRoutes,
}
export default getters
