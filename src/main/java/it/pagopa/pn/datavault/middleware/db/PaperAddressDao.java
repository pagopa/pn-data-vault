package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.entities.PaperAddressEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@Repository
@Slf4j
public class PaperAddressDao extends BaseDao {

    DynamoDbAsyncTable<PaperAddressEntity> paperAddressTable;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    public PaperAddressDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                           PnDatavaultConfig pnDatavaultConfig) {
        this.paperAddressTable = dynamoDbEnhancedAsyncClient.table(pnDatavaultConfig.getDynamodbTableName(), TableSchema.fromBean(PaperAddressEntity.class));
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
    }

    /**
     * Recupera tutti gli indirizzi analogici per un dato paperRequestId.
     *
     * @param paperRequestId id della richiesta analogica
     * @return {@link Flux} contenente tutti gli indirizzi trovati
     */
    public Flux<PaperAddressEntity> getPaperAddressesByPaperRequestId(@NonNull String paperRequestId) {
        log.debug("querying paperAddresses by paperRequestId={}", paperRequestId);

        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                builder -> builder.partitionValue(PaperAddressEntity.buildPk(paperRequestId))
        );

        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .scanIndexForward(true)
                .build();

        return Flux.from(paperAddressTable.query(queryRequest))
                .flatMap(page -> Flux.fromIterable(page.items()));
    }

    /**
     * Recupera un indirizzo analogico specifico per paperRequestId e addressId.
     *
     * @param paperRequestId id della richiesta analogica
     * @param addressId id univoco dell'indirizzo
     * @return {@link Mono} contenente l'indirizzo trovato oppure vuoto se non esiste
     */
    public Mono<PaperAddressEntity> getPaperAddressByIds(@NonNull String paperRequestId, @NonNull String addressId) {
        log.debug("querying paperAddress by paperRequestId={} and addressId={}", paperRequestId, addressId);

        PaperAddressEntity keyEntity = new PaperAddressEntity();
        keyEntity.setPaperRequestId(paperRequestId);
        keyEntity.setAddressId(addressId);

        return Mono.fromFuture(paperAddressTable.getItem(keyEntity));
    }

    /**
     * Salva o aggiorna un indirizzo analogico.
     *
     * @param paperRequestId id della richiesta paper
     * @param addressId id dell'indirizzo
     * @param entity l'entità da salvare/aggiornare
     * @return {@link Mono} che emette l'entità salvata/aggiornata
     */
    public Mono<PaperAddressEntity> updatePaperAddress(@NonNull String paperRequestId,
                                                     @NonNull String addressId,
                                                     @NonNull PaperAddressEntity entity) {
        log.info("saving/updating paperAddress paperRequestId:{} addressId:{}", paperRequestId, addressId);

        entity.setPaperRequestId(paperRequestId);
        entity.setAddressId(addressId);

        return Mono.fromFuture(paperAddressTable.putItem(entity))
                .thenReturn(entity);
    }

    /**
     * Elimina un indirizzo analogico per paperRequestId e addressId.
     *
     * @param paperRequestId id della richiesta paper
     * @param addressId id dell'indirizzo da eliminare
     * @return {@link Mono} che emette l'entità eliminata
     */
    public Mono<PaperAddressEntity> deletePaperAddress(@NonNull String paperRequestId, @NonNull String addressId) {
        log.info("deleting paperAddress paperRequestId:{} addressId:{}", paperRequestId, addressId);

        PaperAddressEntity keyEntity = new PaperAddressEntity();
        keyEntity.setPaperRequestId(paperRequestId);
        keyEntity.setAddressId(addressId);

        return Mono.fromFuture(paperAddressTable.deleteItem(keyEntity));
    }
}
