package com.serli.oracle.of.bacon.repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.print.Doc;
import java.util.Optional;

public class MongoDbRepository {
    private final MongoCollection<Document> actorCollection;

    public MongoDbRepository() {
        this.actorCollection= new MongoClient("localhost", 27017).getDatabase("workshop").getCollection("actors");
    }

    public Optional<Document> getActorByName(String name) {
        // TODO implement actor fetch
        Optional<Document> optionalActor = Optional.empty();
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("name", name);
        FindIterable<Document> list = actorCollection.find(searchQuery);

        for (Document doc : list) {
            optionalActor =Optional.of(doc);
        }
        return optionalActor;
    }
}
