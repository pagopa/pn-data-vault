package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.middleware.db.MessageDao;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        return messageDao.readMessage(messageId, senderId);
    }
}

