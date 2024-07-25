package com.ruoyi.kuzi.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.RequestBase;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MultiTableToElasticsearch {

    private static final Logger log = LoggerFactory.getLogger(MultiTableToElasticsearch.class);
    private static final int BATCH_SIZE = 10000;
    private static final String jdbcURL = "jdbc:mysql://localhost:3306/ry-cloud-test";
    private static final String username = "root";
    private static final String password = "yanz123";
    public static void main(String[] args) {

        String[] tables = {"ping_an_bao_xian_2020nian_10wxlsx", "quan_guo_che_zhu_76wan_2020nian_xlsx", "73wan_xlsx"}; // 需要读取的表名


        try (RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build()) {

            RestClientTransport  transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            ElasticsearchClient esClient = new ElasticsearchClient(transport);

            for (String table : tables) {
               // readAndIndexTable(jdbcURL, username, password, table, esClient);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readAndIndexTable(String jdbcURL, String username, String password, String table, ElasticsearchClient esClient) {
        String query = "SELECT * FROM " + table;

        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> jsonMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    jsonMap.put(metaData.getColumnName(i), resultSet.getObject(i));
                }

                IndexRequest<Map<String, Object>> indexRequest = new IndexRequest.Builder<Map<String, Object>>()
                        .index(table)
                        .document(jsonMap)
                        .build();
              //  bulkRequestBuilder.operations(op -> op.index(indexRequest));

                // 批量大小达到 BATCH_SIZE 时，执行批量操作
               /* if (bulkRequestBuilder.operations().size() >= BATCH_SIZE) {
                    BulkResponse bulkResponse = esClient.bulk(bulkRequestBuilder.build());
                    if (bulkResponse.errors()) {
                        // 处理失败的请求
                        System.err.println("Bulk insert had failures");
                        bulkResponse.items().forEach(item -> {
                            if (item.error() != null) {
                                System.err.println(item.error().reason());
                            }
                        });
                    }
                    bulkRequestBuilder = new BulkRequest.Builder();
                }*/
            }

            // 处理剩余的数据
            /*if (!bulkRequestBuilder.operations().isEmpty()) {
                BulkResponse bulkResponse = esClient.bulk(bulkRequestBuilder.build());
                if (bulkResponse.errors()) {
                    System.err.println("Bulk insert had failures");
                    bulkResponse.items().forEach(item -> {
                        if (item.error() != null) {
                            System.err.println(item.error().reason());
                        }
                    });
                }
            }*/

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

