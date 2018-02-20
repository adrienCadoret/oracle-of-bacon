package com.serli.oracle.of.bacon.repository;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchRepository {

    private final RestHighLevelClient client;
    public static final String INDEX = "oracle-of-bacon";
    public static final String TYPE = "actors";

    public ElasticSearchRepository() {
        client = createClient();

    }

    public static RestHighLevelClient createClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")
                )
        );
    }

    public List<String> getActorsSuggests(String searchQuery) throws IOException {
        List<String> result = new ArrayList<>();

        SearchRequest request = new SearchRequest(INDEX)
                .types(TYPE)
                .searchType(SearchType.DFS_QUERY_THEN_FETCH)
                .source(new SearchSourceBuilder()
                        .size(5)
                        .query(new TermQueryBuilder("name", searchQuery))
                );

        SearchResponse response = client.search(request);

        for (SearchHit searchHit : response.getHits().getHits()) {
//            result.add(searchHit.field("name").getValue());
            result.add(searchHit.getId());
        }

        return result;
    }
}
