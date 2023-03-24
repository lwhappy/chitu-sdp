/*
 * @Author: liaowei
 * @Date: 2021-12-08 17:57:49
 * @LastEditTime: 2021-12-16 10:19:24
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\mock\projectInfo.js
 */
const postOne = [
  { // 获取项目列表信息
    url: 'project/projectManagement/projectInfo',
    enable: false,
    retunData: {
      'code': 0,
      'msg': 'OK',
      'data': {
        'page': 1,
        'pageSize': 10,
        'rows': [
          { 'id': 158, 'createdBy': '廖伟04', 'creationDate': '2021-12-06 17:07:03', 'enabledFlag': 1, 'projectName': 'test1206', 'projectCode': 'qwe', 'projectOwner': '[{\'employeeNumber\':\'632687\',\'enabledFlag\':1,\'id\':1,\'isAdmin\':1,\'isLeader\':2,\'projectId\':158,\'updationDate\':1638781727000,\'userName\':\'廖伟04\'}]', 'projectUsers': '[{\'employeeNumber\':\'632687\',\'enabledFlag\':1,\'id\':1,\'isAdmin\':1,\'isLeader\':1,\'projectId\':158,\'updationDate\':1638781727000,\'userName\':\'廖伟04\'}]', 'projectEngines': '[{\'enabledFlag\':1,\'engineName\':\'flink\',\'engineVersion\':\'flink - 1.14\',\'id\':107},{\'enabledFlag\':1,\'engineName\':\'yarn_queue_storage\',\'engineVersion\':\'1.14\',\'id\':153}]', 'projectUserRole': { 'id': 1, 'enabledFlag': 1, 'projectId': 158, 'userName': '廖伟04', 'employeeNumber': '632687', 'isAdmin': 1, 'isLeader': 1 } }
        ]
      }
    }
  },
  // { // 搜索目录或文件
  //   url: 'folder/searchFolder',
  //   retunData: {
  //     "code": 0,

  //     "msg": "OK",

  //     "data": [
  //       {
  //         "id": 250,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "folder",

  //         "name": "test",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 283,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "folder",

  //         "name": "CHENYUN_TEST",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 147,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "lb_test1",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 178,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_start",

  //         "folderId": 47

  //       },
  //       {
  //         "id": 204,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_datagen",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 208,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "UDF_TEST",

  //         "folderId": 47

  //       },
  //       {
  //         "id": 209,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_kafka1",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 215,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_udf1",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 217,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_udf2",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 227,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "flink_monitor_test",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 228,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_lock",

  //         "folderId": 93

  //       },
  //       {
  //         "id": 229,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_lock1",

  //         "folderId": 93

  //       },
  //       {
  //         "id": 240,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "flink_monitor_test01",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 241,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "flink_monitor_test02",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 242,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "flink_monitor_test03",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 263,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_udf3",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 270,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_kafka2",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 290,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "TEST_1_14",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 311,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test001",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 316,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_kafka3",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 317,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_udf4",

  //         "folderId": 103

  //       },
  //       {
  //         "id": 334,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "yzx_test",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 338,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "sjb_test",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 349,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "new_test_kafka1",

  //         "folderId": 219

  //       },
  //       {
  //         "id": 376,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 384,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "test_02",

  //         "folderId": 0

  //       },
  //       {
  //         "id": 407,

  //         "enabledFlag": 1,

  //         "parentId": 0,

  //         "isLeaf": true,

  //         "type": "file",

  //         "name": "CHENYUN_TEST",

  //         "folderId": 283

  //       }
  //     ],

  //     "success": true

  //   }
  // }
]
export default postOne

