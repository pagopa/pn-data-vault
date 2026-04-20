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
    private static final String GET_MESSAGE_URL    = "/datavault-private/v1/messages/{messageId}";

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

        Mockito.verify(messageService, Mockito.never()).createMessage(Mockito.any(MessageRequestDto.class));
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

        Mockito.verify(messageService, Mockito.never()).createMessage(Mockito.any(MessageRequestDto.class));
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
    // getMessageById
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void getMessageById_returnsOk() {
        UUID messageId = UUID.randomUUID();
        UUID senderId  = UUID.randomUUID();
        MessageResponseDto response = buildMessageResponseDto(buildMessageRequestDto());
        response.setMessageId(messageId);
        response.setSenderId(senderId.toString());

        Mockito.when(messageService.getMessageById(messageId, senderId))
                .thenReturn(Mono.just(response));

        String url = GET_MESSAGE_URL.replace("{messageId}", messageId.toString())
                + "?senderId=" + senderId;

        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.messageId").isEqualTo(messageId.toString())
                .jsonPath("$.senderId").isEqualTo(senderId.toString());
    }



    @Test
    void getMessageById_withFixedIds_returnsOk() {
        String messageId = "123e4567-e89b-12d3-a456-426655440000";
        String senderId  = "987e6543-e21b-34d5-b678-123456789012";
        MessageResponseDto response = buildMessageResponseDto(buildMessageRequestDto());
        response.setMessageId(UUID.fromString(messageId));
        response.setSenderId(senderId);

        Mockito.when(messageService.getMessageById(UUID.fromString(messageId), UUID.fromString(senderId)))
                .thenReturn(Mono.just(response));

        String url = GET_MESSAGE_URL.replace("{messageId}", messageId)
                + "?senderId=" + senderId;

        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getMessageById_notFound_returns404() {
        UUID messageId = UUID.randomUUID();
        UUID senderId  = UUID.randomUUID();

        Mockito.when(messageService.getMessageById(messageId, senderId))
                .thenReturn(Mono.empty());

        String url = GET_MESSAGE_URL.replace("{messageId}", messageId.toString())
                + "?senderId=" + senderId;

        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getMessageById_withInvalidUuidInPath_returnsBadRequest() {
        String url = GET_MESSAGE_URL.replace("{messageId}", "not-a-valid-uuid")
                + "?senderId=" + UUID.randomUUID();

        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getMessageById_withInvalidSenderIdParam_returnsBadRequest() {
        String url = GET_MESSAGE_URL.replace("{messageId}", UUID.randomUUID().toString())
                + "?senderId=not-a-valid-uuid";

        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getMessageById_missingSenderIdParam_returnsBadRequest() {
        String url = GET_MESSAGE_URL.replace("{messageId}", UUID.randomUUID().toString());

        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
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



