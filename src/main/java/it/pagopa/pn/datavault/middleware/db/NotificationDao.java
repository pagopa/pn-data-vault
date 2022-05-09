package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
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
public class NotificationDao extends BaseDao {

    DynamoDbAsyncTable<NotificationEntity> notificationTable;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;


    public NotificationDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                           PnDatavaultConfig pnDatavaultConfig) {
        this.notificationTable = dynamoDbEnhancedAsyncClient.table(pnDatavaultConfig.getDynamodbTableName(), TableSchema.fromBean(NotificationEntity.class));
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
    }

    /**
     * Aggiorna o crea una lista di indirizzi
     * @param entities lista oggetto da creare/aggiornare
     * @return ritorna l'oggetto creato
     */
    public Mono<Object> updateNotifications(List<NotificationEntity> entities)
    {
        if (entities.size() == 1)
            log.info("updating notification internalid:{}", entities.get(0).getInternalId());
        else
            log.info("updating notification entities size:{}", entities.size());



        var updRequestBuilder = TransactWriteItemsEnhancedRequest.builder();
        entities.forEach(n -> updRequestBuilder.addUpdateItem(notificationTable, TransactUpdateItemEnhancedRequest.builder(NotificationEntity.class)
                        .item(n).build()));
        return Mono.fromFuture(dynamoDbEnhancedAsyncClient.transactWriteItems(updRequestBuilder.build())
                .thenApply(x -> ""));
    }

    /**
     * Elimina gli indrizzi associati alla notifica
     *
     * @param iun idnotifica
     * @return l'entity eliminata
     */
    public Mono<Object> deleteNotificationByIun(String iun) {
        log.info("deleting notification internalid:{}",iun);

        // dato che devo cancellare TUTTI gli indirizzi per un certo iun
        // devo necessariamente chiedere quali sono con una query, e poi
        // ricavarmi dai risultati le chiavi per poter eseguire la cancellazione
        return  listNotificationRecipientAddressesDtoById(iun)
                .collectList()
                .map(list -> {
                    log.debug("deleting {} notifications", list.size());
                    var delRequestBuilder = TransactWriteItemsEnhancedRequest.builder();
                    list.forEach(n -> delRequestBuilder.addDeleteItem(notificationTable, TransactDeleteItemEnhancedRequest.builder()
                            .key(getKeyBuild(n.getPk(), n.getRecipientIndex()))
                            .build()));
                    return dynamoDbEnhancedAsyncClient.transactWriteItems(delRequestBuilder.build());
                });
    }

    public Flux<NotificationEntity> listNotificationRecipientAddressesDtoById(String iun) {
        log.debug("quering notifications list-by-id internalid:{}", iun);

        NotificationEntity ne = new NotificationEntity(iun, "");
        QueryConditional queryConditional = QueryConditional.keyEqualTo(getKeyBuild(ne.getPk()));

        QueryEnhancedRequest qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(queryConditional)
                .scanIndexForward(true)
                .build();

        // TODO: viene volutamente ignorata la gestione della paginazione, che per ora non serve.
        // si suppone infatti che la lista degli indirizzi non sia troppo lunga e quindi non vada a sforare il limite di 1MB di paginazione
        return Flux.from(notificationTable.query(qeRequest)
                .flatMapIterable(Page::items));
    }
}
