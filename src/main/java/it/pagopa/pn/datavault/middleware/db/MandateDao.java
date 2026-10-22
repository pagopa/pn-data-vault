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
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.util.List;

@Repository
@Slf4j
public class MandateDao extends BaseDao {

    DynamoDbAsyncTable<MandateEntity> mandateTable;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final Long mandateExpirationSeconds;


    public MandateDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                      PnDatavaultConfig pnDatavaultConfig) {
        this.mandateTable = dynamoDbEnhancedAsyncClient.table(pnDatavaultConfig.getDynamodbTableName(), TableSchema.fromBean(MandateEntity.class));
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.mandateExpirationSeconds = pnDatavaultConfig.getMandateExpiration();
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

        // viene volutamente ignorata la gestione della paginazione, che per ora non serve.
        // si suppone infatti che la lista delle deleghe non sia troppo lunga e quindi non vada a sforare il limite di 1MB di paginazione
        return Flux.from(dynamoDbEnhancedAsyncClient.batchGetItem(qeRequest)
                .flatMapIterable(x -> x.resultsForTable(mandateTable)));
    }

    public Mono<MandateEntity> updateMandate(MandateEntity entity)
    {
        log.info("updating mandate mandateid:{}",entity.getMandateId());

        return Mono.fromFuture(mandateTable.updateItem(entity));
    }

    public Mono<MandateEntity> setMandateExpiration(String mandateId) {
        log.info("setting ttl on mandate mandateid:{}", mandateId);

        MandateEntity entity = new MandateEntity(mandateId);
        entity.setExpiration(Instant.now().getEpochSecond() + mandateExpirationSeconds);

        Expression conditionExpression = Expression.builder()
                .expression("attribute_exists(#pk)")
                .putExpressionName("#pk", MandateEntity.COL_PK)
                .build();

        UpdateItemEnhancedRequest<MandateEntity> request = UpdateItemEnhancedRequest.builder(MandateEntity.class)
                .item(entity)
                .ignoreNulls(true)
                .conditionExpression(conditionExpression)
                .build();

        return Mono.fromFuture(mandateTable.updateItem(request))
                .onErrorResume(ConditionalCheckFailedException.class, ex -> {
                    log.warn("mandate {} non trovato, nessun aggiornamento eseguito", mandateId);
                    return Mono.empty();
                });
    }
}
