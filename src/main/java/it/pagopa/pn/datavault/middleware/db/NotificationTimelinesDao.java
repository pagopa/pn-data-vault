package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.commons.db.BaseDAO;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementId;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import static it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity.ADDRESS_PREFIX;

@Repository
@Slf4j
public class NotificationTimelinesDao extends BaseDAO<NotificationTimelineEntity> {


    protected NotificationTimelinesDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                       DynamoDbAsyncClient dynamoDbAsyncClient,
                                       PnDatavaultConfig pnDatavaultConfig
                                       ) {
        super(dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient, pnDatavaultConfig.getDynamodbTableName(), NotificationTimelineEntity.class);
    }

    public Flux<NotificationTimelineEntity> getNotificationTimelines(Flux<ConfidentialTimelineElementId> confidentialTimelineElementId) {
        return confidentialTimelineElementId
                .map(id -> Tuples.of(ADDRESS_PREFIX + id.getIun(), id.getTimelineElementId()))
                .collectList()
                .flatMapMany(this::batchGetItem);
    }
}
