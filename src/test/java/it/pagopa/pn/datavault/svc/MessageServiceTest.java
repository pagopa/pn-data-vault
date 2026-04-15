package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.LocalizedContent;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.middleware.db.MessageDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
        MessageResponseDto expected = buildMessageResponseDto(buildMessageRequestDto());
        expected.setMessageId(messageId);
        expected.setSenderId(senderId.toString());

        when(messageDao.readMessage(messageId, senderId)).thenReturn(Mono.just(expected));

        MessageResponseDto result = messageService.getMessageById(messageId, senderId).block(TIMEOUT);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(messageDao).readMessage(messageId, senderId);
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

    private static MessageResponseDto buildMessageResponseDto(MessageRequestDto request) {
        MessageResponseDto response = new MessageResponseDto();
        response.setMessageId(UUID.randomUUID());
        response.setSenderId(request.getSenderId());
        response.setPrimaryContent(request.getPrimaryContent());
        response.setSecondaryContent(request.getSecondaryContent());
        response.setCreatedAt(new Date());
        return response;
    }
}

