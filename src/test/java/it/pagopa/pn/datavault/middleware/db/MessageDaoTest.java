package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.middleware.db.entities.MessageEntity;
import it.pagopa.pn.datavault.middleware.db.entities.MessageObjEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageDaoTest {

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Mock
    private DynamoDbAsyncTable<MessageEntity> messageTable;

    @Mock
    private PnDatavaultConfig pnDatavaultConfig;

    private MessageDao messageDao;

    @BeforeEach
    void setup() {
        when(pnDatavaultConfig.getDynamodbTableName()).thenReturn("table-name");
        when(pnDatavaultConfig.getMessageExpiration()).thenReturn(3600L);
        when(dynamoDbEnhancedAsyncClient.table(anyString(), MockitoHelper.anyMessageTableSchema())).thenReturn(messageTable);

        messageDao = new MessageDao(dynamoDbEnhancedAsyncClient, pnDatavaultConfig);
    }

    @Test
    void readMessage_shouldReturnEntity() {
        MessageEntity entity = new MessageEntity(UUID.randomUUID().toString());
        entity.setPrimaryMessage(buildMessageObjEntity("IT", "Oggetto principale", "Corpo principale", "Abstract principale"));
        entity.setAdditionalMessage(buildMessageObjEntity("FR", "Sujet secondaire", "Corps secondaire", "Resume secondaire"));
        entity.setCreatedAt(Instant.now().toString());

        UUID messageId = UUID.fromString(entity.getMessageId());
        UUID senderId = UUID.fromString(entity.getSenderId());
        when(messageTable.getItem(any(MessageEntity.class))).thenReturn(CompletableFuture.completedFuture(entity));

        MessageEntity read = messageDao.readMessage(messageId, senderId).block();
        ArgumentCaptor<MessageEntity> keyCaptor = ArgumentCaptor.forClass(MessageEntity.class);

        verify(messageTable).getItem(keyCaptor.capture());
        MessageEntity key = keyCaptor.getValue();

        Assertions.assertNotNull(read);
        Assertions.assertNotNull(read.getAdditionalMessage());
        Assertions.assertEquals(messageId.toString(), read.getMessageId());
        Assertions.assertEquals(senderId.toString(), read.getSenderId());
        Assertions.assertEquals("Oggetto principale", read.getPrimaryMessage().getSubject());
        Assertions.assertEquals("Abstract principale", read.getPrimaryMessage().getShortBody());
        Assertions.assertEquals("Corps secondaire", read.getAdditionalMessage().getLongBody());
        Assertions.assertEquals(MessageEntity.buildPk(messageId.toString()), key.getPk());
        Assertions.assertEquals(senderId.toString(), key.getSenderId());
    }

    @Test
    void readMessage_whenItemMissing_shouldReturnEmpty() {
        when(messageTable.getItem(any(MessageEntity.class))).thenReturn(CompletableFuture.completedFuture(null));

        MessageEntity read = messageDao.readMessage(UUID.randomUUID(), UUID.randomUUID()).block();
        Assertions.assertNull(read);
    }

    private MessageObjEntity buildMessageObjEntity(String language,
                                                   String subject,
                                                   String longBody,
                                                   String shortBody) {
        MessageObjEntity entity = new MessageObjEntity();
        entity.setSubject(subject);
        entity.setLongBody(longBody);
        entity.setShortBody(shortBody);
        entity.setLanguage(language);
        return entity;
    }

    private static final class MockitoHelper {
        @SuppressWarnings("unchecked")
        private static TableSchema<MessageEntity> anyMessageTableSchema() {
            return any(TableSchema.class);
        }
    }
}



