package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
import it.pagopa.pn.datavault.middleware.db.entities.DiscoveredAddressEntity;
import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.List;

@Repository
@Slf4j
public class DiscoveredAddressDao extends BaseDao {

    DynamoDbAsyncTable<DiscoveredAddressEntity> discoveredAddressTable;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;


    public DiscoveredAddressDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                PnDatavaultConfig pnDatavaultConfig) {
        this.discoveredAddressTable = dynamoDbEnhancedAsyncClient.table(pnDatavaultConfig.getDynamodbTableName(), TableSchema.fromBean(DiscoveredAddressEntity.class));
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
    }


    /**
     * Recupera un {@link DiscoveredAddressEntity}.
     *
     * @param addressId id univoco dell'indirizzo
     * @return {@link Mono} contenente l'indirizzo trovato oppure vuoto se non esiste
     */
    public Mono<DiscoveredAddressEntity> getDiscoveredAddressById(@NonNull String addressId) {
        log.debug("querying discoveredAddress by addressId={}", addressId);

        DiscoveredAddressEntity keyEntity = new DiscoveredAddressEntity(addressId);

        return Mono.fromFuture(discoveredAddressTable.getItem(keyEntity));
    }


    /**
     * Aggiorna un'entità {@link DiscoveredAddressEntity} esistente nella tabella DynamoDB.
     *
     * @param entity l'entità da aggiornare
     * @return {@link Mono} che emette l'entità aggiornata
     */
    public Mono<DiscoveredAddressEntity> updateMandate(DiscoveredAddressEntity entity)
    {
        log.info("updating discoveredAddress addressUid:{}",entity.getAddressId());

        return Mono.fromFuture(discoveredAddressTable.updateItem(entity));
    }

    /**
     * Elimina un'entità {@link DiscoveredAddressEntity} dalla tabella DynamoDB in base all'ID fornito.
     *
     * @param addressId ID dell'indirizzo da eliminare
     * @return {@link Mono} che emette l'entità eliminata
     */
    public Mono<DiscoveredAddressEntity> deleteMandateId(String addressId) {
        log.info("deleting mandate addressUid:{}", addressId);

        DiscoveredAddressEntity entity = new DiscoveredAddressEntity(addressId);
        return Mono.fromFuture(discoveredAddressTable.deleteItem(entity));
    }
}
