package com.serli.oracle.of.bacon.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import com.serli.oracle.of.bacon.repository.MongoDbRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository;
import com.serli.oracle.of.bacon.repository.RedisRepository;
import net.codestory.http.annotations.Get;
import org.bson.Document;
import org.json.JSONObject;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import javax.print.Doc;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        return elasticSearchRepository.getActorsSuggests(searchQuery);
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
        Optional<Document> actor = mongoDbRepository.getActorByName(actorName);
        return actor.get().toJson();
    }
}
