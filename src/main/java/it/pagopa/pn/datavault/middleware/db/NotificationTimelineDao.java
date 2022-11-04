package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
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

@Repository
@Slf4j
public class NotificationTimelineDao extends BaseDao {

    DynamoDbAsyncTable<NotificationTimelineEntity> timelineTable;
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;


    public NotificationTimelineDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                   PnDatavaultConfig pnDatavaultConfig) {
        this.timelineTable = dynamoDbEnhancedAsyncClient.table(pnDatavaultConfig.getDynamodbTableName(), TableSchema.fromBean(NotificationTimelineEntity.class));
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
    }

    /**
     * Aggiorna o crea una un indirizzo nella timeline
     * @param entity oggetto da creare/aggiornare
     * @return ritorna l'oggetto creato
     */
    public Mono<Object> updateNotification(NotificationTimelineEntity entity)
    {
        log.debug("updateNotification timeline internalid:{} timelineelementid:{}",entity.getInternalId(), entity.getTimelineElementId());

        return Mono.fromFuture(timelineTable.updateItem(entity));
    }

    public Mono<NotificationTimelineEntity> getNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId)
    {
        log.debug("getNotificationTimelineByIunAndTimelineElementId timeline internalid:{} timelineelementid:{}", iun, timelineElementId);

        return Mono.fromFuture(timelineTable.getItem(new NotificationTimelineEntity(iun, timelineElementId)));
    }

    public Flux<NotificationTimelineEntity> getNotificationTimelineByIun(String iun) {
        log.debug("getNotificationTimelineByIun timelines list-by-id internalid:{}", iun);

        NotificationTimelineEntity ne = new NotificationTimelineEntity(iun, "");
        QueryConditional queryConditional = QueryConditional.keyEqualTo(getKeyBuild(ne.getPk()));

        QueryEnhancedRequest qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(queryConditional)
                .scanIndexForward(true)
                .build();

        // viene volutamente ignorata la gestione della paginazione, che per ora non serve.
        // si suppone infatti che la lista degli indirizzi non sia troppo lunga e quindi non vada a sforare il limite di 1MB di paginazione
        return Flux.from(timelineTable.query(qeRequest)
                .flatMapIterable(Page::items));
    }
}
