package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Repository
@Slf4j
public class AddressDao extends BaseDao {

    DynamoDbAsyncTable<AddressEntity> addressTable;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;


    public AddressDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                      DynamoDbAsyncClient dynamoDbAsyncClient,
                      PnDatavaultConfig pnDatavaultConfig) {
        this.addressTable = dynamoDbEnhancedAsyncClient.table(pnDatavaultConfig.getDynamodbTableName(), TableSchema.fromBean(AddressEntity.class));
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
    }

    /**
     * Ritorna la lista degli indirizziper ids
     *
     * @param internalId id del destinatario
     * @return lista delle deleghe
     */
    public Flux<AddressEntity> listAddressesById(String internalId) {
        if (log.isInfoEnabled())
            log.info("quering list-by-ids id:{}", internalId);

        AddressEntity ae = new AddressEntity(internalId, "");
        QueryConditional queryConditional = QueryConditional.sortBeginsWith(getKeyBuild(ae.getPk(), ""));

        QueryEnhancedRequest qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(queryConditional)
                .scanIndexForward(true)
                .build();

        // viene volutamente ignorata la gestione della paginazione, che per ora non serve.
        // si suppone infatti che la lista degli indirizzi non sia troppo lunga e quindi non vada a sforare il limite di 1MB di paginazione
        return Flux.from(addressTable.query(qeRequest)
                .flatMapIterable(Page::items));
    }

    /**
     * Aggiorna o crea un indirizzo
     * @param entity oggetto da creare
     * @return ritorna l'oggetto creato
     */
    public Mono<AddressEntity> updateAddress(AddressEntity entity)
    {
        return Mono.fromFuture(addressTable.updateItem(entity));
    }

    /**
     * Elimina un indirizzo
     *
     * @param internalId internalid
     * @param addressId addressid da eliminare
     * @return l'entity eliminata
     */
    public Mono<AddressEntity> deleteAddressId(String internalId, String addressId) {
        AddressEntity me = new AddressEntity(internalId, addressId);
        return Mono.fromFuture(addressTable.deleteItem(me));
    }
}
