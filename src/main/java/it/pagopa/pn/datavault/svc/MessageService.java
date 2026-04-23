package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.middleware.db.MessageDao;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.LocalizedContent;
import it.pagopa.pn.datavault.middleware.db.entities.MessageEntity;
import it.pagopa.pn.datavault.middleware.db.entities.MessageObjEntity;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@CustomLog
public class MessageService {

    private final MessageDao messageDao;

    public MessageService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public Mono<MessageResponseDto> createMessage(MessageRequestDto requestDto) {
        log.debug("Creating message for senderId:{}", requestDto != null ? requestDto.getSenderId() : null);
        MessageEntity entity = toEntity(requestDto);
        return messageDao.writeMessage(entity)
                .map(this::toResponseDto);
    }

    public Mono<MessageResponseDto> getMessageById(UUID messageId, UUID senderId) {
        log.debug("Getting message messageId:{} senderId:{}", messageId, senderId);
        return messageDao.readMessage(messageId, senderId)
                .map(this::toResponseDto);
    }
    private MessageEntity toEntity(MessageRequestDto dto) {
        Objects.requireNonNull(dto, "messageRequestDto is required");

        MessageEntity entity = new MessageEntity(dto.getSenderId());
        entity.setPrimaryMessage(toMessageObjEntity(dto.getPrimaryContent()));
        entity.setAdditionalMessage(toMessageObjEntity(dto.getSecondaryContent()));
        entity.setCreatedAt(Instant.now().toString());
        return entity;
    }

    private MessageResponseDto toResponseDto(MessageEntity entity) {
        Objects.requireNonNull(entity, "messageEntity is required");

        MessageResponseDto dto = new MessageResponseDto();
        dto.setMessageId(UUID.fromString(entity.getMessageId()));
        dto.setSenderId(entity.getSk());
        dto.setPrimaryContent(toLocalizedContent(entity.getPrimaryMessage()));
        dto.setSecondaryContent(toLocalizedContent(entity.getAdditionalMessage()));
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(Date.from(Instant.parse(entity.getCreatedAt())));
        }
        return dto;
    }

    private MessageObjEntity toMessageObjEntity(LocalizedContent dto) {
        if (dto == null) {
            return null;
        }

        MessageObjEntity entity = new MessageObjEntity();
        entity.setSubject(dto.getSubject());
        entity.setLongBody(dto.getLongBody());
        entity.setShortBody(dto.getShortBody());
        entity.setLanguage(Optional.ofNullable(dto.getLanguage())
                .map(LocalizedContent.LanguageEnum::getValue)
                .orElse(null));
        return entity;
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
