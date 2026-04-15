package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.LocalizedContent;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import it.pagopa.pn.datavault.svc.MessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@WebFluxTest(controllers = {MessagesRestControllerV1.class})
class MessagesRestControllerV1Test {

    private static final String CREATE_MESSAGE_URL = "/datavault-private/v1/messages";

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private MessageService messageService;

    // ─────────────────────────────────────────────────────────────────────────────
    // createMessage
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void createMessage_returnsCreated() {
        MessageRequestDto request = buildMessageRequestDto();
        MessageResponseDto response = buildMessageResponseDto(request);
        response.setMessageId(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        Mockito.when(messageService.createMessage(Mockito.any(MessageRequestDto.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.senderId").isEqualTo(response.getSenderId())
                .jsonPath("$.messageId").isEqualTo("11111111-1111-1111-1111-111111111111")
                .jsonPath("$.primaryContent.subject").isEqualTo("Oggetto principale")
                .jsonPath("$.primaryContent.shortBody").isEqualTo("Abstract principale")
                .jsonPath("$.secondaryContent.language").doesNotExist();
    }

    @Test
    void createMessage_withOnlySenderId_returnsBadRequest() {
        MessageRequestDto request = new MessageRequestDto();
        request.setSenderId(UUID.randomUUID().toString());

        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createMessage_withPrimaryAndSecondaryContent_returnsCreated() {
        LocalizedContent primary = buildLocalizedContent(LocalizedContent.LanguageEnum.FR, "Sujet test", "Corps du message", "Résumé test");
        LocalizedContent secondary = buildLocalizedContent(LocalizedContent.LanguageEnum.DE, "Testbetreff", "Nachrichtentext", "Kurzfassung");

        MessageRequestDto request = new MessageRequestDto();
        request.setSenderId(UUID.randomUUID().toString());
        request.setPrimaryContent(primary);
        request.setSecondaryContent(secondary);
        MessageResponseDto response = buildMessageResponseDto(request);

        Mockito.when(messageService.createMessage(Mockito.any(MessageRequestDto.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.secondaryContent.subject").isEqualTo("Testbetreff")
                .jsonPath("$.secondaryContent.shortBody").isEqualTo("Kurzfassung");
    }

    @Test
    void createMessage_withEmptyBody_returnsBadRequest() {
        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createMessage_responseHasBody() {
        MessageRequestDto request = buildMessageRequestDto();
        MessageResponseDto response = buildMessageResponseDto(request);

        Mockito.when(messageService.createMessage(Mockito.any(MessageRequestDto.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MessageResponseDto.class)
                .consumeWith(result -> {
                    MessageResponseDto body = result.getResponseBody();
                    org.junit.jupiter.api.Assertions.assertNotNull(body);
                    org.junit.jupiter.api.Assertions.assertEquals(response.getMessageId(), body.getMessageId());
                });
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Builder helpers
    // ─────────────────────────────────────────────────────────────────────────────

    static MessageRequestDto buildMessageRequestDto() {
        LocalizedContent primary = buildLocalizedContent(
                LocalizedContent.LanguageEnum.IT,
                "Oggetto principale",
                "Corpo del messaggio principale",
                "Abstract principale"
        );
        MessageRequestDto dto = new MessageRequestDto();
        dto.setSenderId(UUID.randomUUID().toString());
        dto.setPrimaryContent(primary);
        return dto;
    }

    static MessageResponseDto buildMessageResponseDto(MessageRequestDto request) {
        MessageResponseDto response = new MessageResponseDto();
        response.setMessageId(UUID.randomUUID());
        response.setSenderId(request.getSenderId());
        response.setPrimaryContent(request.getPrimaryContent());
        response.setSecondaryContent(request.getSecondaryContent());
        response.setCreatedAt(new Date());
        return response;
    }

    static LocalizedContent buildLocalizedContent(LocalizedContent.LanguageEnum language, String subject, String longBody, String shortBody) {
        LocalizedContent content = new LocalizedContent();
        content.setLanguage(language);
        content.setSubject(subject);
        content.setLongBody(longBody);
        content.setShortBody(shortBody);
        return content;
    }
}



