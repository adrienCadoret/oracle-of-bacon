package com.serli.oracle.of.bacon.loader.elasticsearch;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletionLoader {
    private static AtomicInteger count = new AtomicInteger(0);
    private static AtomicInteger initialNumberOfLine = new AtomicInteger(0);
    private static AtomicInteger bulkIteration = new AtomicInteger(0);

    private static final int NUMBER_OF_INDEX_PER_BULK = 200000;

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

        List<BulkRequest> bulkbulkrequest = new ArrayList<>();

        bulkbulkrequest.add(new BulkRequest());

        String inputFilePath = args[0];
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFilePath))) {
            bufferedReader
                    .lines()
                    .forEach(line -> {
                        initialNumberOfLine.incrementAndGet();

                        try {
                            XContentBuilder builder = XContentFactory.jsonBuilder();

                            builder.startObject();
                            builder.field("name", line);
                            builder.endObject();

                            bulkbulkrequest.get(bulkIteration.get()).add(new IndexRequest(ElasticSearchRepository.INDEX, ElasticSearchRepository.TYPE, line).source(builder));

                            int current_count = count.incrementAndGet();

                            if (current_count % NUMBER_OF_INDEX_PER_BULK == 0) {
                                bulkIteration.incrementAndGet();
                                bulkbulkrequest.add(new BulkRequest());
                            }
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    });
        }

        System.out.println("Prepared total of " + count.get() + " of " + initialNumberOfLine.get() + " actors");

        for (BulkRequest request : bulkbulkrequest) {
            BulkResponse response = client.bulk(request);

            if (response.hasFailures()) {
                for (BulkItemResponse itemResponse : response) {
                    if (itemResponse.isFailed()) {
                        BulkItemResponse.Failure failure = itemResponse.getFailure();
                        count.decrementAndGet();
                    }
                }
            }
        }

        System.out.println("Indexed total of " + count.get() + " of " + initialNumberOfLine.get() + " actors");
        client.close();
    }
}
