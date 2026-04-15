package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.LocalizedContent;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
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
    void readMessage_shouldReturnMappedResponse() {
        MessageRequestDto request = buildMessageRequestDto();
        Assertions.assertNotNull(request.getSecondaryContent());
        MessageEntity entity = new MessageEntity(request.getSenderId());
        entity.setPrimaryMessage(toMessageObj(request.getPrimaryContent()));
        entity.setAdditionalMessage(toMessageObj(request.getSecondaryContent()));
        entity.setCreatedAt(Instant.now().toString());

        UUID messageId = UUID.fromString(entity.getMessageId());
        UUID senderId = UUID.fromString(request.getSenderId());
        when(messageTable.getItem(any(MessageEntity.class))).thenReturn(CompletableFuture.completedFuture(entity));

        MessageResponseDto read = messageDao.readMessage(messageId, senderId).block();
        ArgumentCaptor<MessageEntity> keyCaptor = ArgumentCaptor.forClass(MessageEntity.class);

        verify(messageTable).getItem(keyCaptor.capture());
        MessageEntity key = keyCaptor.getValue();

        Assertions.assertNotNull(read);
        Assertions.assertNotNull(read.getSecondaryContent());
        Assertions.assertEquals(messageId, read.getMessageId());
        Assertions.assertEquals(request.getSenderId(), read.getSenderId());
        Assertions.assertEquals(request.getPrimaryContent().getSubject(), read.getPrimaryContent().getSubject());
        Assertions.assertEquals(request.getPrimaryContent().getShortBody(), read.getPrimaryContent().getShortBody());
        Assertions.assertEquals(request.getSecondaryContent().getLongBody(), read.getSecondaryContent().getLongBody());
        Assertions.assertEquals(MessageEntity.buildPk(messageId.toString()), key.getPk());
        Assertions.assertEquals(senderId.toString(), key.getSenderId());
    }

    @Test
    void readMessage_whenItemMissing_shouldReturnEmpty() {
        when(messageTable.getItem(any(MessageEntity.class))).thenReturn(CompletableFuture.completedFuture(null));

        MessageResponseDto read = messageDao.readMessage(UUID.randomUUID(), UUID.randomUUID()).block();
        Assertions.assertNull(read);
    }

    private MessageRequestDto buildMessageRequestDto() {
        MessageRequestDto dto = new MessageRequestDto();
        dto.setSenderId(UUID.randomUUID().toString());
        dto.setPrimaryContent(buildLocalizedContent(LocalizedContent.LanguageEnum.IT, "Oggetto principale", "Corpo principale", "Abstract principale"));
        dto.setSecondaryContent(buildLocalizedContent(LocalizedContent.LanguageEnum.FR, "Sujet secondaire", "Corps secondaire", "Résumé secondaire"));
        return dto;
    }

    private LocalizedContent buildLocalizedContent(LocalizedContent.LanguageEnum language,
                                                   String subject,
                                                   String longBody,
                                                   String shortBody) {
        LocalizedContent content = new LocalizedContent();
        content.setLanguage(language);
        content.setSubject(subject);
        content.setLongBody(longBody);
        content.setShortBody(shortBody);
        return content;
    }

    private MessageObjEntity toMessageObj(LocalizedContent content) {
        MessageObjEntity entity = new MessageObjEntity();
        entity.setSubject(content.getSubject());
        entity.setLongBody(content.getLongBody());
        entity.setShortBody(content.getShortBody());
        entity.setLanguage(content.getLanguage().getValue());
        return entity;
    }

    private static final class MockitoHelper {
        @SuppressWarnings("unchecked")
        private static TableSchema<MessageEntity> anyMessageTableSchema() {
            return any(TableSchema.class);
        }
    }
}



