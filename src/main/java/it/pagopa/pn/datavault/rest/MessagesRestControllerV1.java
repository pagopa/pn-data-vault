package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.MessagesApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.svc.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Slf4j
public class MessagesRestControllerV1 implements MessagesApi {

    private final MessageService messageService;

    public MessagesRestControllerV1(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Mono<ResponseEntity<MessageResponseDto>> createMessage(Mono<MessageRequestDto> messageRequestDto, ServerWebExchange exchange) {
        log.info("[enter] createMessage");
        return messageRequestDto
                .flatMap(messageService::createMessage)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @Override
    public Mono<ResponseEntity<MessageResponseDto>> getMessageById(UUID messageId, UUID senderId, ServerWebExchange exchange) {
        log.info("[enter] getMessageById messageId:{} senderId:{}", messageId, senderId);
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
    }
}

