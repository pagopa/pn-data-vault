package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.svc.RecipientService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@WebFluxTest(controllers = {RecipientsRestControllerV1.class})
class RecipientsRestControllerV1Test {


    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private RecipientService privateService;

    @Test
    void ensureRecipientByExternalId() {
        //Given
        String url = "/datavault-private/v1/recipients/external/{recipientType}"
                .replace("{recipientType}", "PF");


        //When
        Mockito.when( privateService.ensureRecipientByExternalId( Mockito.any(), Mockito.any() ))
                .thenReturn(Mono.just("1234567890"));

        //Then
        webTestClient.post()
                .uri(url)
                .bodyValue("RSSMRA85T10A562S")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getRecipientDenominationByInternalId() {

        //Given
        String url = "/datavault-private/v1/recipients/internal?internalId={internalId}"
                .replace("{internalId}", "123e4567-e89b-12d3-a456-426655440000");
        BaseRecipientDto brd = new BaseRecipientDto();
        brd.setDenomination("mario rossi");
        brd.setInternalId("123e4567-e89b-12d3-a456-426655440000");
        List<BaseRecipientDto> list = new ArrayList<>();

        //When
        Mockito.when( privateService.getRecipientDenominationByInternalId( Mockito.any() ))
                .thenReturn(Flux.fromIterable(list));

        //Then
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(BaseRecipientDto.class);
    }

    @Test
    void getRecipientDenominationByInternalIdButFailedValidationSize() {

        String url = "/datavault-private/v1/recipients/internal?internalId={internalId}"
                .replace("{internalId}", "123e4567-e89b-12d3-a456-426655440000");

        StringBuilder urlBuilder = new StringBuilder(url);

        String otherInternalIds = "&internalId=123e4567-e89b-12d3-a456-426655440000";

        urlBuilder.append(otherInternalIds.repeat(100));

        String finalUrl = urlBuilder.toString();

        assertThat(finalUrl.split("internalId")).hasSizeGreaterThan(100);


        webTestClient.get()
                .uri(finalUrl)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}