package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.LocalizedContent;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageRequestDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MessageResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.junit.jupiter.api.Assertions;

import java.util.UUID;

@WebFluxTest(controllers = {MessagesRestControllerV1.class})
class MessagesRestControllerV1Test {

    private static final String CREATE_MESSAGE_URL = "/datavault-private/v1/messages";
    private static final String GET_MESSAGE_URL    = "/datavault-private/v1/messages/{messageId}";

    @Autowired
    WebTestClient webTestClient;

    // ─────────────────────────────────────────────────────────────────────────────
    // createMessage
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void createMessage_returnsNotImplemented() {
        // Arrange
        MessageRequestDto request = buildMessageRequestDto();

        // Act & Assert
        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(501);
    }

    @Test
    void createMessage_withOnlySenderId_returnsNotImplemented() {
        // Arrange – body minimale con solo il campo obbligatorio senderId
        MessageRequestDto request = new MessageRequestDto();
        request.setSenderId(UUID.randomUUID().toString());

        // Act & Assert
        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(501);
    }

    @Test
    void createMessage_withPrimaryAndSecondaryContent_returnsNotImplemented() {
        // Arrange
        LocalizedContent primary = buildLocalizedContent(LocalizedContent.LanguageEnum.FR, "Sujet test", "Corps du message");
        LocalizedContent secondary = buildLocalizedContent(LocalizedContent.LanguageEnum.DE, "Testbetreff", "Nachrichtentext");

        MessageRequestDto request = new MessageRequestDto();
        request.setSenderId(UUID.randomUUID().toString());
        request.setPrimaryContent(primary);
        request.setSecondaryContent(secondary);

        // Act & Assert
        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(501);
    }

    @Test
    void createMessage_withEmptyBody_returnsNotImplemented() {
        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void createMessage_responseHasNoBody() {
        // Arrange
        MessageRequestDto request = buildMessageRequestDto();

        // Act & Assert – il 501 non restituisce body (build() senza body)
        webTestClient.post()
                .uri(CREATE_MESSAGE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(501)
                .expectBody(MessageResponseDto.class)
                .consumeWith(result ->
                        Assertions.assertNull(result.getResponseBody(),
                                "Il 501 non deve restituire body"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // getMessageById
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    void getMessageById_returnsNotImplemented() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        UUID senderId  = UUID.randomUUID();

        String url = GET_MESSAGE_URL.replace("{messageId}", messageId.toString())
                + "?senderId=" + senderId;

        // Act & Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(501);
    }

    @Test
    void getMessageById_withFixedIds_returnsNotImplemented() {
        // Arrange – ID fissi per riproducibilità
        String messageId = "123e4567-e89b-12d3-a456-426655440000";
        String senderId  = "987e6543-e21b-34d5-b678-123456789012";

        String url = GET_MESSAGE_URL.replace("{messageId}", messageId)
                + "?senderId=" + senderId;

        // Act & Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(501);
    }

    @Test
    void getMessageById_withInvalidUuidInPath_returnsBadRequest() {
        // Arrange – UUID malformato nel path → Spring lo rifiuta con 400
        String url = GET_MESSAGE_URL.replace("{messageId}", "not-a-valid-uuid")
                + "?senderId=" + UUID.randomUUID();

        // Act & Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getMessageById_withInvalidSenderIdParam_returnsBadRequest() {
        // Arrange – senderId non UUID
        String url = GET_MESSAGE_URL.replace("{messageId}", UUID.randomUUID().toString())
                + "?senderId=not-a-valid-uuid";

        // Act & Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getMessageById_missingSenderIdParam_returnsBadRequest() {
        // Arrange – senderId obbligatorio assente
        String url = GET_MESSAGE_URL.replace("{messageId}", UUID.randomUUID().toString());

        // Act & Assert
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
                LocalizedContent.LanguageEnum.FR,
                "Oggetto principale",
                "Corpo del messaggio principale"
        );
        MessageRequestDto dto = new MessageRequestDto();
        dto.setSenderId(UUID.randomUUID().toString());
        dto.setPrimaryContent(primary);
        return dto;
    }

    static LocalizedContent buildLocalizedContent(LocalizedContent.LanguageEnum language, String subject, String body) {
        LocalizedContent content = new LocalizedContent();
        content.setLanguage(language);
        content.setSubject(subject);
        content.setBody(body);
        return content;
    }
}



