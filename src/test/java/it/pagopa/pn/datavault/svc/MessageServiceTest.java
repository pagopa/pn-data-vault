package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.LocalizedContent;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.middleware.db.MessageDao;
import it.pagopa.pn.datavault.middleware.db.entities.MessageEntity;
import it.pagopa.pn.datavault.middleware.db.entities.MessageObjEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageDao messageDao;

    @Test
    void getMessageById() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        MessageEntity expected = buildMessageEntity(messageId, senderId);

        when(messageDao.readMessage(messageId, senderId)).thenReturn(Mono.just(expected));

        MessageResponseDto result = messageService.getMessageById(messageId, senderId).block(TIMEOUT);

        assertNotNull(result);
        assertEquals(messageId, result.getMessageId());
        assertEquals(senderId.toString(), result.getSenderId());
        assertEquals("Oggetto principale", result.getPrimaryContent().getSubject());
        assertEquals("DE", result.getSecondaryContent().getLanguage().getValue());
        verify(messageDao).readMessage(messageId, senderId);
    }

    @Test
    void createMessage() {
        MessageRequestDto request = buildMessageRequestDto();
        MessageEntity persisted = buildPersistedMessageEntity(request);

        when(messageDao.writeMessage(any(MessageEntity.class))).thenReturn(Mono.just(persisted));

        MessageResponseDto result = messageService.createMessage(request).block(TIMEOUT);
        ArgumentCaptor<MessageEntity> entityCaptor = ArgumentCaptor.forClass(MessageEntity.class);

        assertNotNull(result);
        assertEquals(UUID.fromString(persisted.getMessageId()), result.getMessageId());
        assertEquals(request.getSenderId(), result.getSenderId());
        assertNotNull(result.getCreatedAt());
        assertEquals(request.getPrimaryContent().getSubject(), result.getPrimaryContent().getSubject());
        assertEquals(request.getSecondaryContent().getLanguage(), result.getSecondaryContent().getLanguage());

        verify(messageDao).writeMessage(entityCaptor.capture());
        MessageEntity toPersist = entityCaptor.getValue();
        assertEquals(request.getSenderId(), toPersist.getSk());
        assertEquals(request.getPrimaryContent().getLongBody(), toPersist.getPrimaryMessage().getLongBody());
        assertEquals(request.getSecondaryContent().getLanguage().getValue(), toPersist.getAdditionalMessage().getLanguage());
        assertNotNull(toPersist.getCreatedAt());
    }

    @Test
    void getMessageById_notFound() {
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();

        when(messageDao.readMessage(messageId, senderId)).thenReturn(Mono.empty());

        MessageResponseDto result = messageService.getMessageById(messageId, senderId).block(TIMEOUT);

        assertNull(result);
        verify(messageDao).readMessage(messageId, senderId);
    }

    private static MessageRequestDto buildMessageRequestDto() {
        LocalizedContent primary = new LocalizedContent();
        primary.setLanguage(LocalizedContent.LanguageEnum.IT);
        primary.setSubject("Oggetto principale");
        primary.setLongBody("Corpo del messaggio principale");
        primary.setShortBody("Abstract principale");

        LocalizedContent secondary = new LocalizedContent();
        secondary.setLanguage(LocalizedContent.LanguageEnum.DE);
        secondary.setSubject("Zusätzlicher Betreff");
        secondary.setLongBody("Zusätzlicher Nachrichtentext");
        secondary.setShortBody("Kurzfassung");

        MessageRequestDto dto = new MessageRequestDto();
        dto.setSenderId(UUID.randomUUID().toString());
        dto.setPrimaryContent(primary);
        dto.setSecondaryContent(secondary);
        return dto;
    }

    private static MessageEntity buildMessageEntity(UUID messageId, UUID senderId) {
        MessageEntity entity = new MessageEntity();
        entity.setMessageId(messageId.toString());
        entity.setSk(senderId.toString());
        entity.setPrimaryMessage(buildMessageObjEntity("IT", "Oggetto principale", "Corpo del messaggio principale", "Abstract principale"));
        entity.setAdditionalMessage(buildMessageObjEntity("DE", "Additional subject", "Additional message body", "Summary"));
        entity.setCreatedAt(Instant.now().toString());
        return entity;
    }

    private static MessageEntity buildPersistedMessageEntity(MessageRequestDto request) {
        MessageEntity entity = new MessageEntity();
        entity.setMessageId(UUID.randomUUID().toString());
        entity.setSk(request.getSenderId());
        entity.setPrimaryMessage(buildMessageObjEntity(
                request.getPrimaryContent().getLanguage().getValue(),
                request.getPrimaryContent().getSubject(),
                request.getPrimaryContent().getLongBody(),
                request.getPrimaryContent().getShortBody()
        ));
        entity.setAdditionalMessage(buildMessageObjEntity(
                request.getSecondaryContent().getLanguage().getValue(),
                request.getSecondaryContent().getSubject(),
                request.getSecondaryContent().getLongBody(),
                request.getSecondaryContent().getShortBody()
        ));
        entity.setCreatedAt(Instant.now().toString());
        return entity;
    }

    private static MessageObjEntity buildMessageObjEntity(String language,
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
}

