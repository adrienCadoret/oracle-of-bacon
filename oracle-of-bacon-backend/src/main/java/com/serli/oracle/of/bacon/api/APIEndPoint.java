package com.serli.oracle.of.bacon.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import com.serli.oracle.of.bacon.repository.MongoDbRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository;
import com.serli.oracle.of.bacon.repository.RedisRepository;
import net.codestory.http.annotations.Get;
import org.json.JSONObject;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class APIEndPoint {
    private final Neo4JRepository neo4JRepository;
    private final ElasticSearchRepository elasticSearchRepository;
    private final RedisRepository redisRepository;
    private final MongoDbRepository mongoDbRepository;

    public APIEndPoint() {
        neo4JRepository = new Neo4JRepository();
        elasticSearchRepository = new ElasticSearchRepository();
        redisRepository = new RedisRepository();
        mongoDbRepository = new MongoDbRepository();
    }

    @Get("bacon-to?actor=:actorName")
    public String getConnectionsToKevinBacon(String actorName) {

        List<Object> elements = (List<Object>) neo4JRepository.getConnectionsToKevinBacon(actorName);


        JsonArray jsonArray = new JsonArray();
        for(Object element : elements){
            if(element instanceof Node) {
                Node node = (Node) element;
                JsonObject dataObject = new JsonObject();
                JsonObject nodeObject = new JsonObject();

                nodeObject.addProperty("id", node.id());
                for (String label : node.labels()) {
                    nodeObject.addProperty("type", label);
                }

                for (Value value : node.values()) {
                    nodeObject.addProperty("value", value.toString());
                }
                dataObject.add("data", nodeObject);
                jsonArray.add(dataObject);
            }
            if(element instanceof Relationship){
                Relationship relationship = (Relationship) element;
                JsonObject dataObject = new JsonObject();
                JsonObject relationshipObject = new JsonObject();
                relationshipObject.addProperty("id", relationship.id());
                relationshipObject.addProperty("source", relationship.startNodeId());
                relationshipObject.addProperty("target", relationship.endNodeId());
                relationshipObject.addProperty("value", relationship.type());

                dataObject.add("data", relationshipObject);
                jsonArray.add(dataObject);
            }
        }

        return jsonArray.toString();
    }

    @Get("suggest?q=:searchQuery")
    public List<String> getActorSuggestion(String searchQuery) throws IOException {
        return Arrays.asList("Niro, Chel",
                "Senanayake, Niro",
                "Niro, Juan Carlos",
                "de la Rua, Niro",
                "Niro, Simão");
    }

    @Get("last-searches")
    public List<String> last10Searches() {
        return Arrays.asList("Peckinpah, Sam",
                "Robbins, Tim (I)",
                "Freeman, Morgan (I)",
                "De Niro, Robert",
                "Pacino, Al (I)");
    }

    @Get("actor?name=:actorName")
    public String getActorByName(String actorName) {
        return "{\n" +
                "\"_id\": {\n" +
                "\"$oid\": \"587bd993da2444c943a25161\"\n" +
                "},\n" +
                "\"imdb_id\": \"nm0000134\",\n" +
                "\"name\": \"Robert De Niro\",\n" +
                "\"birth_date\": \"1943-08-17\",\n" +
                "\"description\": \"Robert De Niro, thought of as one of the greatest actors of all time, was born in Greenwich Village, Manhattan, New York City, to artists Virginia (Admiral) and Robert De Niro Sr. His paternal grandfather was of Italian descent, and his other ancestry is Irish, German, Dutch, English, and French. He was trained at the Stella Adler Conservatory and...\",\n" +
                "\"image\": \"https://images-na.ssl-images-amazon.com/images/M/MV5BMjAwNDU3MzcyOV5BMl5BanBnXkFtZTcwMjc0MTIxMw@@._V1_UY317_CR13,0,214,317_AL_.jpg\",\n" +
                "\"occupation\": [\n" +
                "\"actor\",\n" +
                "\"producer\",\n" +
                "\"soundtrack\"\n" +
                "]\n" +
                "}";
    }
}
