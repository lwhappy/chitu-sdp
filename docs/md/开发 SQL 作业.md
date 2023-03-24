本文为您介绍 Flink SQL 作业开发的**操作步骤**。

## 前置条件

- 系统管理员创建 flink 计算引擎，并添加引擎使用者。
- 系统管理员创建项目，并添加项目成员。

## 操作步骤

### 1.登录赤兔实时计算平台，新建作业

![开发SQL作业-1](D:\chitu-sdp\docs\md\image\开发SQL作业-1.png)

​    a.登录赤兔实时计算平台。

​    b.在项目管理，找到目标项目点击**进入项目。**

​    c.点击**新建。**

​    d.在新建作业对话框，填写作业配置信息。

|   作业   |                             说明                             | 是否必填 | 备注 |
| :------: | :----------------------------------------------------------: | -------- | ---- |
| 文件名称 | 作业的名称 同一个项目内作业名唯一。 作业命名规则：字母，数字或下划线。 | 是       |      |
| 作业等级 | 作业等级是指作业权重等级，分为P1/P2/P3/P4共4级，作业等级默认和项目等级一致，用户根据实际作业权重设置作业等级。<br />![项目管理-1](D:\chitu-sdp\docs\md\image\项目管理-1.png) | 是       |      |
| 文件类型 |              文件类型需要选择为SQL/DS，选择SQL               | 是       |      |
| 存储位置 | 指定该作业的代码文件所属的文件夹。 您还可以在现有文件夹右侧，单击![新建文件夹](https://help-static-aliyun-doc.aliyuncs.com/assets/img/zh-CN/7214291261/p277156.png)图标，新建子文件夹。 | 是       |      |

​    e.点击**保存。**

### 2.在作业开发页面右侧，点击**元表配置**，填写配置信息

![img](https://bg-prd-cos-bdp-1257092428.cos.ap-guangzhou.myqcloud.com/rdp-metadata/portal/2023/2/3/141677825853721.png)

**参数解析如下表所示**

|  作业参数  |                       说明                       |
| :--------: | :----------------------------------------------: |
|  引用名称  |                  元表引用的名称                  |
| 数据源实例 |          文件类型需要选择为kafka/mysql           |
|   数据库   |       若有数据库，选择数据源实例后自动带出       |
| 数据库类型 |       若有数据库，选择数据源实例后自动带出       |
|   Topic    |       数据源实例为kafka时显示，Topic的名称       |
|    表名    |        数据源实例为mysql时显示，表的名称         |
|  元表类型  |         元表类型选择为source、sink、dim          |
|    DDL     | 元表配置信息填写完成后点击生成ddl，会生成ddl语句 |

### 3.编写 sql 逻辑

![img](https://bg-prd-cos-bdp-1257092428.cos.ap-guangzhou.myqcloud.com/rdp-metadata/portal/2023/2/7/171678181335850.png)

和离线 sql 开发一样，基于创建好的表，做过滤、清洗、转换、去重、join、分组统计、窗口计算等

示例代码如下

```
-- 使用内置函数解析内容, 根据条件过滤数据, 过滤结果写入视图
CREATE VIEW db_write_num_view AS
SELECT
JSON_VALUE(message,'$.flinkJobId') AS flink_job_id -- flink任务id
, JSON_VALUE(message,'$.sourceType') AS source_type -- 数据源类型
, JSON_VALUE(message,'$.databaseTableName') AS database_table_name -- 数据库表名
, cast(JSON_VALUE(message,'$.type') AS TINYINT) AS type -- 写入成功/失败类型
, cast(JSON_VALUE(message,'$.num') AS bigint) AS num -- 批次写入数量
FROM flink_log_demo
WHERE level = 'INFO'
AND JSON_EXISTS(message,'$.sdpRunningResultLogFlag')
AND JSON_VALUE(message,'$.flinkJobId') IS NOT NULL
AND JSON_VALUE(message,'$.databaseTableName') IS NOT NULL; -- 按照维度(flink任务id、数据源类型、数据库表、写入成功/失败类型) 分组统计
INSERT INTO ads_db_write_num_result_demo(flink_job_id,source_type,database_table_name,type,num)
SELECT flink_job_id -- flink任务id
, source_type -- 数据源类型
, database_table_name -- 数据库表名
, type -- 写入成功/失败类型 , sum(num) -- 批次写入数量
FROM db_write_num_view
GROUP BY flink_job_id, type, source_type, database_table_name;
```

**按钮说明如下表所示**

| 按钮名称  |                             说明                             |
| :-------: | :----------------------------------------------------------: |
|   保存    |                         作业本地保存                         |
|   撤销    |                编辑器内容上一步操作内容的回退                |
|   恢复    |                编辑器内容上一步操作内容的恢复                |
|   查找    | 按照输入内容进行模糊匹配。查找功能包括：模糊匹配，精确匹配，结果统计，向上查找结果，向下查找结果，结果高亮显示 |
|  格式化   |            编辑器内容按照标准编码规范进行美化代码            |
|  转生产   |                    UAT代码同步至生产环境                     |
|   验证    | 通过调用flink接口验证脚本编辑器内容是否符合flink 语义和语法的规范 |
| 发布至UAT |                     作业发布到配置的集群                     |