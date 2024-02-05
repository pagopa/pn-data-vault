package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.commons.db.BaseDAO;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementId;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import static it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity.ADDRESS_PREFIX;

@Repository
@Slf4j
public class NotificationTimelineDao extends BaseDAO<NotificationTimelineEntity> {


    protected NotificationTimelineDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                      DynamoDbAsyncClient dynamoDbAsyncClient,
                                      PnDatavaultConfig pnDatavaultConfig
                                       ) {
        super(dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient, pnDatavaultConfig.getDynamodbTableName(), NotificationTimelineEntity.class);
    }

    /**
     * Aggiorna o crea una un indirizzo nella timeline
     * @param entity oggetto da creare/aggiornare
     * @return ritorna l'oggetto creato
     */
    public Mono<Object> updateNotification(NotificationTimelineEntity entity)
    {
        log.debug("updateNotification timeline internalid:{} timelineelementid:{}",entity.getInternalId(), entity.getTimelineElementId());
        return Mono.fromFuture(update(entity));
    }

    public Mono<NotificationTimelineEntity> getNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId)
    {
        log.debug("getNotificationTimelineByIunAndTimelineElementId timeline internalid:{} timelineelementid:{}", iun, timelineElementId);
        NotificationTimelineEntity entity = new NotificationTimelineEntity(iun, timelineElementId);
        return Mono.fromFuture(get(entity.getPk(), entity.getTimelineElementId()));
    }

    public Flux<NotificationTimelineEntity> getNotificationTimelineByIun(String iun) {
        log.debug("getNotificationTimelineByIun timelines list-by-id internalid:{}", iun);
        NotificationTimelineEntity entity = new NotificationTimelineEntity(iun, null);
        QueryConditional queryConditional = QueryConditional.keyEqualTo(keyBuild(entity.getPk(), entity.getTimelineElementId()));

        // viene volutamente ignorata la gestione della paginazione, che per ora non serve.
        // si suppone infatti che la lista degli indirizzi non sia troppo lunga e quindi non vada a sforare il limite di 1MB di paginazione
        return Flux.from(getByFilter(queryConditional, null, null, null));
    }

    public Flux<NotificationTimelineEntity> getNotificationTimelines(Flux<ConfidentialTimelineElementId> confidentialTimelineElementId) {

        log.debug("getNotification timelines confidentialTimelineElementId:{}", confidentialTimelineElementId);

        return confidentialTimelineElementId
                .map(id -> Tuples.of(getIunWhitPrefix(id.getIun()), id.getTimelineElementId()))
                .collectList()
                .flatMapMany(this::batchGetItem);
    }

    private String getIunWhitPrefix(String iun){
        return StringUtils.contains(ADDRESS_PREFIX, iun) ? iun : ADDRESS_PREFIX + iun;
    }
}
