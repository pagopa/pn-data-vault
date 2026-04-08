package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.entities.MessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;
import java.util.Objects;

@Repository
@Slf4j
public class MessageDao extends BaseDao {

    private final DynamoDbAsyncTable<MessageEntity> messageTable;
    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final Long messageExpirationSeconds;

    public MessageDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                      PnDatavaultConfig pnDatavaultConfig) {
        this.messageTable = dynamoDbEnhancedAsyncClient.table(
                pnDatavaultConfig.getDynamodbTableName(),
                TableSchema.fromBean(MessageEntity.class)
        );
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.messageExpirationSeconds = pnDatavaultConfig.getMessageExpiration();
    }

    public Mono<MessageEntity> writeMessage(MessageEntity entity) {
        MessageEntity preparedEntity = enrichExpiration(entity);
        log.debug("writeMessage prepared entity messageId:{} sk:{} expiration:{}",
                preparedEntity.getMessageId(),
                preparedEntity.getSk(),
                preparedEntity.getExpiration());

        return Mono.error(new UnsupportedOperationException("writeMessage not implemented yet"));
    }

    public Mono<MessageEntity> readMessage(String messageId, String senderId) {
        log.debug("readMessage messageId:{} sk:{}", messageId, senderId);
        return Mono.error(new UnsupportedOperationException("readMessage not implemented yet"));
    }

    private MessageEntity enrichExpiration(MessageEntity entity) {
        Objects.requireNonNull(entity, "entity is required");

        if (entity.getExpiration() == null && messageExpirationSeconds != null) {
            entity.setExpiration(Instant.now().getEpochSecond() + messageExpirationSeconds);
        }
        return entity;
    }
}
