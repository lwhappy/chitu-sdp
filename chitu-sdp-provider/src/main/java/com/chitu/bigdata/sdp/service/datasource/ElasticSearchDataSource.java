package com.chitu.bigdata.sdp.service.datasource;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;
import com.chitu.bigdata.sdp.api.domain.MetadataTableColumn;
import com.chitu.bigdata.sdp.api.domain.SourceMeta;
import com.chitu.bigdata.sdp.api.enums.DataSourceOption;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.constant.FlinkDataTypeMapping;
import com.chitu.bigdata.sdp.utils.CommonSqlParser;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.google.common.collect.Maps;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sutao
 * @create 2021-12-09 17:19
 */
@Component("elasticsearch")
@Slf4j
public class ElasticSearchDataSource extends AbstractDataSource<RestHighLevelClient> {


    private final String DEFAULT_INDEX_TYPE = "_doc";


    @Override
    public RestHighLevelClient getConnection(ConnectInfo connectInfo) throws IOException {
        RestHighLevelClient client = null;
        String[] nodes = connectInfo.getAddress().split(",");
        List<String> nodeList = Stream.of(nodes).collect(Collectors.toList());
        List<HttpHost> httpHosts = new ArrayList<>();
        nodeList.forEach(node -> {
            String[] address = node.split(":");
            HttpHost httpHost = new HttpHost(address[0], Integer.parseInt(address[1]), "http");
            httpHosts.add(httpHost);
        });

        if (!StringUtils.isEmpty(connectInfo.getUsername()) && !StringUtils.isEmpty(connectInfo.getPwd())) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(connectInfo.getUsername(), connectInfo.getPwd()));
            client = new RestHighLevelClient(
                    RestClient.builder(httpHosts.stream().toArray(HttpHost[]::new))
                            .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                                @Override
                                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                                    httpClientBuilder.disableAuthCaching();
                                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                                }
                            }));
        } else {
            client = new RestHighLevelClient(RestClient.builder(httpHosts.stream().toArray(HttpHost[]::new)));
        }
        client.cluster().health(new ClusterHealthRequest(), RequestOptions.DEFAULT);
        return client;
    }

    @Override
    public void closeConnection(RestHighLevelClient restHighLevelClient) {
        try {
            if (restHighLevelClient != null) {
                restHighLevelClient.close();
            }
        } catch (IOException e) {
            log.error("close RestHighLevelClient error:", e);
        }
    }

    @Override
    public boolean tableExists(ConnectInfo connectInfo, String tableName) throws IOException {
        RestHighLevelClient client = getConnection(connectInfo);
        try {
            if (indexExist(client, tableName)) {
                return true;
            }
            return templateExist(client, tableName);
        } finally {
            closeConnection(client);
        }
    }

    public boolean indexExist(RestHighLevelClient client, String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest();
        request.indices(indexName);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 该api模板不存在时会抛ElasticsearchStatusException异常
     *
     * @param client
     * @param templateName
     * @return
     * @throws IOException
     */
    public boolean templateExist(RestHighLevelClient client, String templateName) throws IOException {
        GetIndexTemplatesRequest templatesRequest = new GetIndexTemplatesRequest();
        templatesRequest.names(templateName);
        GetIndexTemplatesResponse template = null;
        try {
            template = client.indices().getTemplate(templatesRequest, RequestOptions.DEFAULT);
            List<IndexTemplateMetaData> indexTemplates = template.getIndexTemplates();
            if (CollectionUtil.isNotEmpty(indexTemplates)) {
                return true;
            }
        } catch (ElasticsearchStatusException e) {
            log.warn("索引模板名不存在", e);
        }
        return false;
    }

    @Override
    public List<String> getTables(ConnectInfo connectInfo) throws Exception {
        RestHighLevelClient connection = getConnection(connectInfo);
        List<String> tables = new ArrayList<>();
        try {
            RestClient restClient = connection.getLowLevelClient();
            Response templateResponse = restClient.performRequest("GET", "/_cat/templates?format=json");
            Response indiceResponse = restClient.performRequest("GET", "/_cat/indices?format=json");
            List<Map> templateMap = JSONObject.parseArray(EntityUtils.toString(templateResponse.getEntity()), Map.class);
            List<Map> indiceMap = JSONObject.parseArray(EntityUtils.toString(indiceResponse.getEntity()), Map.class);
            templateMap.forEach(item -> {
                tables.add(item.get("name").toString());
            });
            indiceMap.forEach(item -> {
                tables.add(item.get("index").toString());
            });
        } finally {
            closeConnection(connection);
        }
        return tables;
    }

    @Override
    public List<MetadataTableColumn> getTableColumns(ConnectInfo connectInfo, String tableName) throws Exception {
        RestHighLevelClient client = getConnection(connectInfo);
        List<MetadataTableColumn> tableColumns = new ArrayList<>();
        try {
            RestClient restClient = client.getLowLevelClient();
            Response response = null;
            //判断索引是否存在
            if (indexExist(client, tableName)) {
                response = restClient.performRequest("GET", "/" + tableName + "/" + "_mapping?pretty");
            } else {
                response = restClient.performRequest("GET", "_template/" + tableName);
            }

            try {
                HttpEntity entity = response.getEntity();
                String entityJson = EntityUtils.toString(entity);
                JSONObject jsonObject = JSONObject.parseObject(entityJson, Feature.OrderedField);
                JSONObject indexObject = jsonObject.getJSONObject(tableName);
                JSONObject mappingsObject = indexObject.getJSONObject("mappings");
                Set<Map.Entry<String, Object>> entries = mappingsObject.entrySet();
                for (Map.Entry<String, Object> item : entries) {
                    // 索引类型
                    String indexType = item.getKey();
                    JSONObject typeObject = JSONObject.parseObject(item.getValue().toString(), Feature.OrderedField);
                    JSONObject propertiesObject = typeObject.getJSONObject("properties");
                    Set<Map.Entry<String, Object>> propertiesEntries = propertiesObject.entrySet();
                    propertiesEntries.stream().forEach(e -> {
                        String name = e.getKey();
                        JSONObject jsonValue = JSONObject.parseObject(e.getValue().toString());
                        String type = null;
                        if (jsonValue.get("type") != null) {
                            type = jsonValue.get("type").toString();
                        } else {
                            if (jsonValue.get("properties") != null) {
                                type = "object";
                            }
                        }
                        MetadataTableColumn metaConnectTableColumn = new MetadataTableColumn();
                        metaConnectTableColumn.setIndexType(indexType);
                        metaConnectTableColumn.setColumnName(name);
                        metaConnectTableColumn.setColumnType(type);
                        tableColumns.add(metaConnectTableColumn);
                    });
                    // 只处理第一个type
                    break;
                }
            } catch (Exception e) {
                log.error("es解析异常", e);
            }

        } finally {
            closeConnection(client);
        }
        return tableColumns;
    }

    @Override
    public String generateDdl(FlinkTableGenerate flinkTableGenerate) {
        //转换成flink字段类型
        flinkTableGenerate.getMetadataTableColumnList().forEach(item -> {
            FlinkDataTypeMapping.ES_TYPE_MAP.forEach((k, v) -> {
                if (StrUtil.equalsIgnoreCase(item.getColumnType(), k)) {
                    item.setColumnType(v);
                    return;
                }
            });
            //如果没匹配到，设置默认值
            if (!FlinkDataTypeMapping.ES_TYPE_MAP.containsValue(item.getColumnType())) {
                item.setColumnType(FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE);
            }
        });
        StringBuilder sb = new StringBuilder();
        flinkTableGenerate.getMetadataTableColumnList().forEach(item -> {
            sb.append("  ").append(StrUtils.strWithBackticks(item.getColumnName())).append(" ").append(item.getColumnType()).append(",").append("\n");
        });
        String indexType = DEFAULT_INDEX_TYPE;
        if (CollectionUtil.isNotEmpty(flinkTableGenerate.getMetadataTableColumnList())) {
            indexType = flinkTableGenerate.getMetadataTableColumnList().get(0).getIndexType();
        }
        String tableDdl = String.format(
                "CREATE TABLE %s (\n" +
                        "%s" +
                        "  PRIMARY KEY (`id`) NOT ENFORCED -- 主键字段\n" +
                        ") WITH (\n" +
                        "  'connector' = 'elasticsearch-6',\n" +
                        "  'hosts' = '%s',\n" +
                        "  'username' = '%s',\n" +
                        "  'password' = '%s',\n" +
                        "  'index' = '%s',\n" +
                        "  'document-type' = '%s'\n" +
                        ");",
                StrUtils.strWithBackticks(flinkTableGenerate.getFlinkTableName()),
                sb.toString(),
                SecureUtil.md5(flinkTableGenerate.getAddress().replace(",", ";")),
                flinkTableGenerate.getUserName(),
                SecureUtil.md5(flinkTableGenerate.getPwd()),
                flinkTableGenerate.getSourceTableName(),
                indexType
        );
        return tableDdl;
    }

    @Override
    public Map<String, String> replaceConnParam(Map<String, String> options, SdpDataSource sdpDataSource) {
        for (String key : options.keySet()) {
            switch (DataSourceOption.EsOption.ofOption(key)) {
                case HOSTS:
                    options.put(key, sdpDataSource.getDataSourceUrl().replaceAll(StrUtil.COMMA, StrUtils.SEMICOLON));
                    break;
                case USER_NAME:
                    options.put(key, sdpDataSource.getUserName());
                    break;
                case PWD:
                    options.put(key, sdpDataSource.getPassword());
                    break;
                default:
                    break;
            }
        }
        return options;
    }

    @Override
    public void initTable(SourceMeta meta) {

    }


    @Override
    public String modifyOption4ChangeEnv(String ddl, SdpDataSource sdpDataSource) throws Exception {
        CommonSqlParser commonSqlParser = new CommonSqlParser(ddl);
        SqlCreateTable sqlCreateTable = commonSqlParser.getCreateTableList().get(0);
        List<SqlNode> options = sqlCreateTable.getPropertyList().getList();

        Map<String,String> optionsMap = Maps.newHashMap();
        optionsMap.put(FlinkConfigKeyConstant.USERNAME,sdpDataSource.getUserName());
        Map<String, SqlTableOption> replaceOptionsMap = SqlParserUtil.getTableOptionMap(optionsMap);

        int offset1 = removeOption(options, FlinkConfigKeyConstant.USERNAME);
        if (-1 == offset1) {
            insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.USERNAME));
        } else {
            insertOption(options, offset1, replaceOptionsMap.get(FlinkConfigKeyConstant.USERNAME));
        }

        StringBuilder modifySql = flinkSqlBuilder(ddl, options);

        return modifySql.toString();
    }
}
