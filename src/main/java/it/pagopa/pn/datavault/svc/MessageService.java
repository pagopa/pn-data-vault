package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.LocalizedContent;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.middleware.db.MessageDao;
import it.pagopa.pn.datavault.middleware.db.entities.MessageEntity;
import it.pagopa.pn.datavault.middleware.db.entities.MessageObjEntity;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
@CustomLog
public class MessageService {

    private final MessageDao messageDao;

    public MessageService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public Mono<MessageResponseDto> getMessageById(UUID messageId, UUID senderId) {
        log.debug("Getting message messageId:{} senderId:{}", messageId, senderId);
        return messageDao.readMessage(messageId, senderId)
                .map(this::toResponseDto);
    }

    private MessageResponseDto toResponseDto(MessageEntity entity) {
        Objects.requireNonNull(entity, "messageEntity is required");

        MessageResponseDto dto = new MessageResponseDto();
        dto.setMessageId(UUID.fromString(entity.getMessageId()));
        dto.setSenderId(entity.getSenderId());
        dto.setPrimaryContent(toLocalizedContent(entity.getPrimaryMessage()));
        dto.setSecondaryContent(toLocalizedContent(entity.getAdditionalMessage()));
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(Date.from(Instant.parse(entity.getCreatedAt())));
        }
        return dto;
    }

    private LocalizedContent toLocalizedContent(MessageObjEntity entity) {
        if (entity == null) {
            return null;
        }

        LocalizedContent dto = new LocalizedContent();
        dto.setSubject(entity.getSubject());
        dto.setLongBody(entity.getLongBody());
        dto.setShortBody(entity.getShortBody());
        if (entity.getLanguage() != null) {
            dto.setLanguage(LocalizedContent.LanguageEnum.fromValue(entity.getLanguage()));
        }
        return dto;
    }
}

