<!--
 * @description: 
 * @Author: lijianguo19
 * @Date: 2022-06-30 15:04:11
 * @FilePath: \src\components\chitu-table\README.md
-->
## chitu-table 表格组件使用文档

### 参数说明
#### 1、 columns：表格列的配置描述
基本使用参考a-table

```js
let vm =this
columns =[
    {
        dataIndex: 'projectName',
        title: '项目名称',
        width: 150,//宽度必须填写且为number类型
        customRender: (text, record) => {
            return {
            children: vm.$createElement('a-button', {
                attrs: { type: 'link' },
                on: {
                click: () => {
                    vm.gotoJob(record)
                }
                }
            }, text)
            }
        }
    }
]


```
customRender支持render函数渲染
```js
customRender: (text, record) => {
    return {
    children: vm.$createElement('a-button', {
        attrs: { type: 'link' },
        on: {
        click: () => {
            vm.gotoJob(record)
        }
        }
    }, text)
    }
}
```
也支持slot
```js
columns =[
   {
      dataIndex: 'jobName',
      key: 'jobName',
      title: '作业名称',
      scopedSlots: { customRender: 'jobName' },
      width: 200
    }
]

//表格
 <chitu-table>
    <template #jobName="{text,record}">
        <div>
            <a-tooltip placement="topLeft">
            <template slot="title">
                <span>{{record.jobName}}</span>
            </template>
            <div class="jobName blue"
                    @click="gotoDevelop(record)">{{ record.jobName }}</div>
            </a-tooltip>
        </div>
    </template>
</chitu-table>
```
#### 2、 scroll：表格滚动
默认设置{ x: '100%' },自适应宽度；操作列设置fixed:'right'可实现固定列

高度暂不支持{y:'100%'} 和{y:'calc(...)'}配置；如需设置固定高度可传{y:300}或{y:'300px'}(后续完善)

如需自适应屏幕高度，可设置autoHight

#### 3、 autoHight：自适应高度(默认为true)
注意：为true的时候scroll设置失效

#### 4、 extraHeight：自适应高度添加而外的高度

#### 5、 bordered 是否边框(默认为true)

#### 6、 drag 表头 th 是否可以拖动改变列宽(默认为true;且bordered需要为true才可生效)

#### 7、pagination 开启分页；无需分页不需要设置pagination；当autoHight为true的时候，分页器默认固定底部
```js
 pagination: {
    current: 1,
    showSizeChanger: true,
    showQuickJumper: true,
    defaultPageSize: 20,
    total: 0
},

//分页器监听事件
@pageChange="pageChange"
@pageSizeChange="pageSizeChange"
```
#### 8、isSidbarpagination 分页器宽度是否随侧边菜单栏宽度改变(只有设置了autoHight时才生效；使用场景为表格自适应高度且左侧有侧边菜单栏)

#### 9、tableOpt 第三组件的属性相当于v-bind="$attrs"，可自行扩展添加table属性
```js
 <chitu-table v-loading="isLoading"
                 row-key="id"
                 :columns="columns"
                 :data-source="tableData"
                 :loading="loading"
                 :pagination="pagination"
                 @pageChange="pageChange"
                 @pageSizeChange="pageSizeChange"
                 :tableOpt="{rowClassName:(record) => { return record.id === rowId ? 'clickRowStyle': 'rowStyleNone' }}"
                 @change="handleTableChange">
</chitu-table
```

