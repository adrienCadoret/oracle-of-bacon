package com.serli.oracle.of.bacon.repository;


import org.neo4j.driver.internal.InternalPath;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        this.driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "coucou"));
    }

    public List<?> getConnectionsToKevinBacon(String actorName) {

        List<Object> toReturn = new ArrayList<>();
        try ( Session session = driver.session() )
        {
            StatementResult rs = session.run( "MATCH p=shortestPath((bacon:Actor {name:\"Bacon, Kevin (I)\"})-[*]-(actor:Actor {name:\""+actorName+"\"}))  RETURN DISTINCT p");
            List<Record> list = rs.list();

            if(list.size() > 0) {
                Path path = ((PathValue) list.get(0).values().get(0)).asPath();
                Iterable<Node> listNodes = path.nodes();
                for(Node n: listNodes){
                    toReturn.add(n);
                }
                Iterable<Relationship> listRelationships = path.relationships();
                for(Relationship r: listRelationships){
                    toReturn.add(r);
                }

            }
        }

        return toReturn;
    }

    public static abstract class GraphItem {
        public final long id;

        private GraphItem(long id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphItem graphItem = (GraphItem) o;

            return id == graphItem.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    private static class GraphNode extends GraphItem {
        public final String type;
        public final String value;

        public GraphNode(long id, String value, String type) {
            super(id);
            this.value = value;
            this.type = type;
        }
    }

    private static class GraphEdge extends GraphItem {
        public final long source;
        public final long target;
        public final String value;

        public GraphEdge(long id, long source, long target, String value) {
            super(id);
            this.source = source;
            this.target = target;
            this.value = value;
        }
    }
}
