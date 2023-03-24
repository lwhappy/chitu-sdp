/*
 * @Author: hjg
 * @Date: 2021-12-08 17:57:49
 * @LastEditTime: 2021-12-22 16:25:48
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \bigdata-sdp-frontend2\src\mock\projectInfo.js
 */
const postTwo = [
  { // 获取系统管理员列表
    url: 'setting/userSetting/getInfo',
    enable: false,
    retunData: {
      'code': 0,
      'msg': 'OK',
      'data': {
        'page': 1,
        'pageSize': 10,
        'pageTotal': 3,
        'rowTotal': 25,
        'rows': [
          {
            'id': 1085,
            'creationDate': '2021-12-05 18:54:33',
            'enabledFlag': 1,
            'employeeNumber': '633631',
            'userName': '商渭清',
            'isAdmin': 1
          }
        ]
      }
    }
  },
  { // 获取数据源列表
    url: 'dataSource/list',
    enable: false,
    retunData: {
      'code': 0, //code
      'msg': 'OK', //返回文字描述
      'data': {
        'page': 3,
        'pageSize': 5,
        'rows': [
          {
            'id': '36', //元表id
            'dataSourceName': 'mysql_test', //数据源名称
            'creationDate': '2021-11-10 18:59:27', //创建时间
            'enabledFlag': 1, //是否可用
            'dataSourceType': 'mysql', //数据源类型
            'remark': '成功', //描述
            'owner': '632687', //责任人工号
            'ownerName': '李三', //责任人名称
            'dataSourceUrl': 'http://baidu.com', //数据源url
            'databaseName': 'crm', //数据库名称
            'userName': 'root', //用户名
            'password': '*****', //密码
            'updatedBy': '李三', //更新人
            'updationDate': '2021-11-10 18:59:27', //更新时间
            'createdBy': '李三', //创建人（提交人）
            'isUsed': 0
          }
        ], //行对象
        'rowTotal': 17,
        'pageTotal': 4
      }, //返回数据
      'success': true //成功响应
    }
  },
  { // 获取项目下的用户角色
    url: 'project/projectManagement/getUserRole',
    enable: false,
    retunData: {
      'code': 0, //code
      'msg': 'OK', //返回文字描述
      'data': {
        'id': '460', //用户id
        'enabledFlag': 1, //是否有效
        'projectId': 53, //项目id
        'userName': '廖伟', //用户名李慧杰
        'employeeNumber': '632687', //员工编号632743
        'isAdmin': 1, //是否系统管理人
        'isLeader': 0 //是否是项目管理人}, //返回数据
      },
      'success': true //成功响应
    }
  },
  { // 数据源连通检验
    url: 'dataSource/checkConnect',
    enable: false,
    retunData: {
      'code': 0, //code
      'msg': 'OK', //返回文字描述
      'data': {}, //返回数据
      'success': true //成功响应
    }
  },
  { // 新增数据源
    url: 'dataSource/add',
    enable: false,
    retunData: {
      'code': 0, //code
      'msg': 'OK', //返回文字描述
      'data': {}, //返回数据
      'success': true //成功响应
    }
  },
  { // 修改数据源
    url: 'dataSource/update',
    enable: false,
    retunData: {
      'code': 0, //code
      'msg': 'OK', //返回文字描述
      'data': {}, //返回数据
      'success': true //成功响应
    }
  },
  { // 查询元表列表
    url: 'meta/table/query',
    enable: false,
    retunData: {
      'code': 0, //code
      'msg': 'OK', //返回文字描述
      'data': {
        'rows': []
        // 'rows': [{
        //   'id': 1,
        //   'flinkTableName': 'test1', // 引用名称
        //   'metaTableName': 'test1', // 元表名称
        //   'metaTableType': 'sink', // 元表类型
        //   'dataSourceId': 1, // 数据源ID
        //   'flinkDdl': '-- 创建kafka source表\r\nCREATE TABLE order_info (\r\n\t' // ddl
        // },
        // {
        //   'id': 2,
        //   'flinkTableName': 'test2', // 引用名称
        //   'metaTableName': 'test2', // 元表名称
        //   'metaTableType': 'dim', // 元表类型
        //   'dataSourceId': 1, // 数据源ID
        //   'flinkDdl': '-- 创建kafka source表\r\nCREATE TABLE order_info (\r\n\t' // ddl
        // },
        // {
        //   'id': 3,
        //   'flinkTableName': 'test3', // 引用名称
        //   'metaTableName': 'test3', // 元表名称
        //   'metaTableType': 'source', // 元表类型
        //   'dataSourceId': 1, // 数据源ID
        //   'flinkDdl': '-- 创建kafka source表\r\nCREATE TABLE order_info (\r\n\t' // ddl
        // },
        // {
        //   'id': 4,
        //   'flinkTableName': 'test4', // 引用名称
        //   'metaTableName': 'test4', // 元表名称
        //   'metaTableType': 'dim', // 元表类型
        //   'dataSourceId': 1, // 数据源ID
        //   'flinkDdl': '-- 创建kafka source表\r\nCREATE TABLE order_info (\r\n\t' // ddl
        // },
        // {
        //   'id': 5,
        //   'flinkTableName': 'tesaaaaaaaaaaaaaaaaaaaaaaaaaaaat5', // 引用名称
        //   'metaTableName': 'test5', // 元表名称
        //   'metaTableType': 'source', // 元表类型
        //   'dataSourceId': 1, // 数据源ID
        //   'flinkDdl': '-- 创建kafka source表\r\nCREATE TABLE order_info (\r\n\t' // ddl
        // }]
      }, //返回数据
      'success': true //成功响应
    }
  },
  { // 查询元表列表
    url: 'project/projectManagement/projectInfo',
    enable: false,
    retunData: {
      'code': 0, //code
      'msg': 'OK', //返回文字描述
      'data': {
        'rows': []
      }, //返回数据
      'success': true //成功响应
    }
  }
]
export default postTwo

