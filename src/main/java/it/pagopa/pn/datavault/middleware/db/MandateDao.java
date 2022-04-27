package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.conf.AwsConfigs;
import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.util.List;

@Repository
@Slf4j
public class MandateDao extends BaseDao {

    DynamoDbAsyncTable<MandateEntity> mandateTable;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;


    public MandateDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                      DynamoDbAsyncClient dynamoDbAsyncClient,
                      PnDatavaultConfig pnDatavaultConfig) {
        this.mandateTable = dynamoDbEnhancedAsyncClient.table(pnDatavaultConfig.getDynamodbTableName(), TableSchema.fromBean(MandateEntity.class));
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
    }

    /**
     * Ritorna la lista delle deleghe per ids
     *
     * @param ids lista degli id delle delghe
     * @return lista delle deleghe
     */
    public Flux<MandateEntity> listMandatesByIds(List<String> ids) {
        if (log.isInfoEnabled())
            log.info("quering list-by-ids ids size:{}", ids==null?0:ids.size());

        var rbb = ReadBatch.builder(MandateEntity.class)
            .mappedTableResource(mandateTable);

        ids.stream().distinct().forEach(id -> {
            rbb.addGetItem(new MandateEntity(id));
        });


        BatchGetItemEnhancedRequest qeRequest = BatchGetItemEnhancedRequest.builder()
                .addReadBatch(rbb.build())
                .build();

        // viene volutamente ignorata la gestione della paginazione, che per ora non serve.
        // si suppone infatti che la lista delle deleghe non sia troppo lunga e quindi non vada a sforare il limite di 1MB di paginazione
        return Flux.from(dynamoDbEnhancedAsyncClient.batchGetItem(qeRequest)
                .flatMapIterable(x -> x.resultsForTable(mandateTable)));
    }

    public Mono<MandateEntity> updateMandate(MandateEntity entity)
    {
        return Mono.fromFuture(mandateTable.updateItem(entity));
    }

    public Mono<MandateEntity> deleteMandateId(String mandateId) {
        MandateEntity me = new MandateEntity(mandateId);
        return Mono.fromFuture(mandateTable.deleteItem(me));
    }
}
