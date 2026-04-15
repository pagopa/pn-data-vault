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

    private static final String GET_MESSAGE_URL    = "/datavault-private/v1/messages/{messageId}";

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    private MessageService messageService;

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



