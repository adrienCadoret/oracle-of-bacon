package com.serli.oracle.of.bacon.loader.elasticsearch;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletionLoader {
    private static AtomicInteger count = new AtomicInteger(0);
    private static AtomicInteger initialNumberOfLine = new AtomicInteger(0);

    /**
     * Nombre maximum du lot de requête exécuté en bulk sur elastic
     */
    private static final int NUMBER_OF_INDEX_PER_BULK = 200000;

    /**
     * Permet d'envoyer les requêtes bulk dès qu'on a atteint le nombre max de requêtes ci-dessus : car java 8 et dans le forEach(line -> ..) on ne peut pas modifier un bulkRequest externe la var doit être finale.
     * On utilise donc un seul élément de la stack à chaque fois
     */
    private final static Stack<BulkRequest> bulkbulkrequest = new Stack<>();

    /**
     * Si erreur entity too large (trop de données à bulker), il faut décrémenter la variable NUMBER_OF_INDEX_PER_BULK. Ce sera bien sûr plus long à éxécuter
     *
     * Entre chaque essai (si erreur) executer DELETE /oracle-of-bacon/actors
     *
     * @see CompletionLoader#NUMBER_OF_INDEX_PER_BULK
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        RestHighLevelClient client = ElasticSearchRepository.createClient();

        if (args.length != 1) {
            System.err.println("Expecting 1 arguments, actual : " + args.length);
            System.err.println("Usage : completion-loader <actors file path>");
            System.exit(-1);
        }

        bulkbulkrequest.push(new BulkRequest());

        String inputFilePath = args[0];
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFilePath))) {
            bufferedReader
                    .lines()
                    .forEach(line -> {
                        initialNumberOfLine.incrementAndGet();

                        try {
                            // Créé l'index à ajouter
                            XContentBuilder builder = XContentFactory.jsonBuilder();

                            builder.startObject();
                            builder.field("name", line);
                            builder.endObject();

                            // On sotcke l'index dans la BulkRequest au 1er element de la stack
                            bulkbulkrequest.peek().add(new IndexRequest(ElasticSearchRepository.INDEX, ElasticSearchRepository.TYPE, line).source(builder));

                            int current_count = count.incrementAndGet();

                            // Si on atteint les NUMBER_OF_INDEX_PER_BULK index préparés, on envoie le tout à elastic
                            if (current_count % NUMBER_OF_INDEX_PER_BULK == 0) {
                                sendBulk(client);
                            }
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    });
        }

        // On envoie ce qui reste
        sendBulk(client);

        client.close();
    }

    /**
     * Permet d'envoyer un lot de bulkrequest auprès d'elastic
     *
     * @param client le client elastic
     */
    private static void sendBulk(RestHighLevelClient client) throws IOException {
        System.out.println("Prepared total of " + count.get() + " of " + initialNumberOfLine.get() + " actors");

        if (bulkbulkrequest.peek().numberOfActions() != 0) {
            BulkResponse response = client.bulk(bulkbulkrequest.pop());

            if (response.hasFailures()) {
                for (BulkItemResponse itemResponse : response) {
                    if (itemResponse.isFailed()) {
                        BulkItemResponse.Failure failure = itemResponse.getFailure();
                        System.err.println(failure.getMessage());
                        count.decrementAndGet();
                    }
                }
            }

            System.out.println("Indexed total of " + count.get() + " of " + initialNumberOfLine.get() + " actors");

            bulkbulkrequest.push(new BulkRequest());
        }
    }
}
