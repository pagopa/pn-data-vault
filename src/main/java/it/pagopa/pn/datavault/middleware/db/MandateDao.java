package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;

import java.util.List;

@Repository
@Slf4j
public class MandateDao extends BaseDao {

    DynamoDbAsyncTable<MandateEntity> mandateTable;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;


    public MandateDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
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
    public Flux<MandateEntity> listMandatesByIds(@NonNull List<String> ids) {
        log.debug("quering mandates list-by-ids ids size:{}", ids.size());

        var rbb = ReadBatch.builder(MandateEntity.class)
            .mappedTableResource(mandateTable);

        ids.stream().distinct().forEach(id -> rbb.addGetItem(new MandateEntity(id)));


        BatchGetItemEnhancedRequest qeRequest = BatchGetItemEnhancedRequest.builder()
                .addReadBatch(rbb.build())
                .build();

        // TODO: viene volutamente ignorata la gestione della paginazione, che per ora non serve.
        // si suppone infatti che la lista delle deleghe non sia troppo lunga e quindi non vada a sforare il limite di 1MB di paginazione
        return Flux.from(dynamoDbEnhancedAsyncClient.batchGetItem(qeRequest)
                .flatMapIterable(x -> x.resultsForTable(mandateTable)));
    }

    public Mono<MandateEntity> updateMandate(MandateEntity entity)
    {
        log.info("updating mandate mandateid:{}",entity.getMandateId());

        return Mono.fromFuture(mandateTable.updateItem(entity));
    }

    public Mono<MandateEntity> deleteMandateId(String mandateId) {
        log.info("deleting mandate mandateid:{}",mandateId);

        MandateEntity me = new MandateEntity(mandateId);
        return Mono.fromFuture(mandateTable.deleteItem(me));
    }
}
