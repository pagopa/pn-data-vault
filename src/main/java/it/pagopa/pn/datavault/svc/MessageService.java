package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.middleware.db.MessageDao;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@CustomLog
public class MessageService {

    private final MessageDao messageDao;

    public MessageService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public Mono<MessageResponseDto> createMessage(MessageRequestDto requestDto) {
        log.debug("Creating message for senderId:{}", requestDto != null ? requestDto.getSenderId() : null);
        return messageDao.writeMessage(requestDto);
    }
}

