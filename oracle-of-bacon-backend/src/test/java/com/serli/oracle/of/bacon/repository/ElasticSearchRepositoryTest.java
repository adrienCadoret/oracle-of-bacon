package com.serli.oracle.of.bacon.repository;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class ElasticSearchRepositoryTest {
    private static ElasticSearchRepository repository;

    @BeforeClass
    public static void setUp() {
        repository = new ElasticSearchRepository();
    }

    @Test
    public void createClientTest() {
        try (RestHighLevelClient client = ElasticSearchRepository.createClient()) {

            assertNotNull("Client should not be null", client);
            assertTrue("Client should be pingable", client.ping());
        } catch (Exception e) {
            fail("An exception occurred : " + e.getMessage());
        }
    }

    @Test
    public void getActorsSuggestTest() {
        List<String> oracle_actors = Arrays.asList("Niro, Vicente",
                "Senanayake, Niro",
                "Niro, Armando",
                "Niro, Pablo",
                "Niro, Chel");

        try {
            List<String> actors = repository.getActorsSuggests("niro");


            assertNotNull("Result should not be null", actors);
            assertEquals("Number of result should be the same", oracle_actors.size(), actors.size());
            assertEquals("Lists of actors should be the same", oracle_actors, actors);
        } catch (Exception e) {
            fail("An exception occurred : " + e.getMessage());
        }
    }
}
