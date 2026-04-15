package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.LocalizedContent;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.middleware.db.entities.MessageEntity;
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
    void writeMessage_shouldPutItemAndReturnMappedResponse() {
        MessageRequestDto request = buildMessageRequestDto();
        Assertions.assertNotNull(request.getSecondaryContent());
        Assertions.assertNotNull(request.getSecondaryContent().getLanguage());
        when(messageTable.putItem(any(MessageEntity.class))).thenReturn(CompletableFuture.completedFuture(null));

        MessageResponseDto response = messageDao.writeMessage(request).block();
        ArgumentCaptor<MessageEntity> entityCaptor = ArgumentCaptor.forClass(MessageEntity.class);

        verify(messageTable).putItem(entityCaptor.capture());
        MessageEntity persisted = entityCaptor.getValue();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getMessageId());
        Assertions.assertNotNull(response.getSecondaryContent());
        Assertions.assertEquals(request.getSenderId(), response.getSenderId());
        Assertions.assertNotNull(response.getCreatedAt());
        Assertions.assertEquals(request.getPrimaryContent().getSubject(), response.getPrimaryContent().getSubject());
        Assertions.assertEquals(request.getSecondaryContent().getShortBody(), response.getSecondaryContent().getShortBody());

        Assertions.assertNotNull(persisted.getMessageId());
        Assertions.assertEquals(request.getSenderId(), persisted.getSk());
        Assertions.assertEquals(request.getPrimaryContent().getLongBody(), persisted.getPrimaryMessage().getLongBody());
        Assertions.assertEquals(request.getSecondaryContent().getLanguage().getValue(), persisted.getAdditionalMessage().getLanguage());
        Assertions.assertNotNull(persisted.getCreatedAt());
        Assertions.assertNotNull(persisted.getExpiration());
    }

    @Test
    void writeMessage_shouldMapLocalizedContentAndExpiration() {
        MessageRequestDto request = buildMessageRequestDto();
        when(messageTable.putItem(any(MessageEntity.class))).thenReturn(CompletableFuture.completedFuture(null));

        messageDao.writeMessage(request).block();

        ArgumentCaptor<MessageEntity> entityCaptor = ArgumentCaptor.forClass(MessageEntity.class);
        verify(messageTable).putItem(entityCaptor.capture());
        MessageEntity persisted = entityCaptor.getValue();

        Assertions.assertEquals("Oggetto principale", persisted.getPrimaryMessage().getSubject());
        Assertions.assertEquals("Corpo principale", persisted.getPrimaryMessage().getLongBody());
        Assertions.assertEquals("Abstract principale", persisted.getPrimaryMessage().getShortBody());
        Assertions.assertEquals("IT", persisted.getPrimaryMessage().getLanguage());
        Assertions.assertEquals("Sujet secondaire", persisted.getAdditionalMessage().getSubject());
        Assertions.assertEquals("FR", persisted.getAdditionalMessage().getLanguage());
        Assertions.assertNotNull(persisted.getExpiration());
        Assertions.assertTrue(persisted.getExpiration() > Instant.now().getEpochSecond());
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

    private static final class MockitoHelper {
        @SuppressWarnings("unchecked")
        private static TableSchema<MessageEntity> anyMessageTableSchema() {
            return any(TableSchema.class);
        }
    }
}



