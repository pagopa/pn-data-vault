package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddress;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddresses;
import it.pagopa.pn.datavault.mapper.PaperAddressEntityPaperAddressMapper;
import it.pagopa.pn.datavault.middleware.db.entities.PaperAddressEntity;
import it.pagopa.pn.datavault.svc.PaperAddressService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(controllers = {PaperAddressRestControllerV1.class})
@Import(PaperAddressEntityPaperAddressMapper.class)
class PaperAddressRestControllerV1Test {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PaperAddressEntityPaperAddressMapper mapper;

    @MockitoBean
    private PaperAddressService paperAddressService;

    @Test
    void getPaperAddressesByPaperRequestId() {
        // Arrange
        String paperRequestId = "paperReq_123456";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}"
                .replace("{paperRequestId}", paperRequestId);

        PaperAddress paperAddressDto = buildPaperAddress();
        PaperAddresses paperAddresses = new PaperAddresses();
        List<PaperAddress> addressList = new ArrayList<>();
        addressList.add(paperAddressDto);
        paperAddresses.setAddresses(addressList);

        // Act
        Mockito.when(paperAddressService.getPaperAddressesByPaperRequestId(anyString()))
                .thenReturn(Mono.just(paperAddresses));

        // Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaperAddresses.class);
    }

    @Test
    void getPaperAddressByIds() {
        // Arrange
        String paperRequestId = "paperReq_123456";
        String paperAddressId = "addr_789";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}/{paperAddressId}"
                .replace("{paperRequestId}", paperRequestId)
                .replace("{paperAddressId}", paperAddressId);

        PaperAddress paperAddressDto = buildPaperAddress();

        // Act
        Mockito.when(paperAddressService.getPaperAddressByIds(anyString(), anyString()))
                .thenReturn(Mono.just(paperAddressDto));

        // Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaperAddress.class);
    }

    @Test
    void getPaperAddressByIds_NotFound() {
        // Arrange
        String paperRequestId = "paperReq_123456";
        String paperAddressId = "addr_nonexistent";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}/{paperAddressId}"
                .replace("{paperRequestId}", paperRequestId)
                .replace("{paperAddressId}", paperAddressId);

        // Act
        Mockito.when(paperAddressService.getPaperAddressByIds(anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updatePaperAddress() {
        // Arrange
        String paperRequestId = "paperReq_123456";
        String paperAddressId = "addr_789";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}/{paperAddressId}"
                .replace("{paperRequestId}", paperRequestId)
                .replace("{paperAddressId}", paperAddressId);

        PaperAddress paperAddressDto = buildPaperAddress();

        // Act
        Mockito.when(paperAddressService.updatePaperAddress(anyString(), anyString(), any(PaperAddress.class)))
                .thenReturn(Mono.empty());

        // Assert
        webTestClient.put()
                .uri(url)
                .bodyValue(paperAddressDto)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deletePaperAddress() {
        // Arrange
        String paperRequestId = "paperReq_123456";
        String paperAddressId = "addr_789";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}/{paperAddressId}"
                .replace("{paperRequestId}", paperRequestId)
                .replace("{paperAddressId}", paperAddressId);

        PaperAddress paperAddressDto = buildPaperAddress();

        // Act
        Mockito.when(paperAddressService.deletePaperAddress(anyString(), anyString()))
                .thenReturn(Mono.just(paperAddressDto));

        // Assert
        webTestClient.delete()
                .uri(url)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deletePaperAddress_NotFound() {
        // Arrange
        String paperRequestId = "paperReq_123456";
        String paperAddressId = "addr_nonexistent";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}/{paperAddressId}"
                .replace("{paperRequestId}", paperRequestId)
                .replace("{paperAddressId}", paperAddressId);

        // Act
        Mockito.when(paperAddressService.deletePaperAddress(anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Assert
        webTestClient.delete()
                .uri(url)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getPaperAddressesByPaperRequestId_EmptyList() {
        // Arrange
        String paperRequestId = "paperReq_empty";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}"
                .replace("{paperRequestId}", paperRequestId);

        PaperAddresses emptyPaperAddresses = new PaperAddresses();
        emptyPaperAddresses.setAddresses(new ArrayList<>());

        // Act
        Mockito.when(paperAddressService.getPaperAddressesByPaperRequestId(anyString()))
                .thenReturn(Mono.just(emptyPaperAddresses));

        // Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaperAddresses.class);
    }

    @Test
    void getPaperAddressesByPaperRequestId_MultipleAddresses() {
        // Arrange
        String paperRequestId = "paperReq_multiple";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}"
                .replace("{paperRequestId}", paperRequestId);

        PaperAddress paperAddress1 = mapper.toDto(new PaperAddressEntity());
        PaperAddress paperAddress2 = mapper.toDto(new PaperAddressEntity());

        PaperAddresses paperAddresses = new PaperAddresses();
        List<PaperAddress> addressList = new ArrayList<>();
        addressList.add(paperAddress1);
        addressList.add(paperAddress2);
        paperAddresses.setAddresses(addressList);

        // Act
        Mockito.when(paperAddressService.getPaperAddressesByPaperRequestId(anyString()))
                .thenReturn(Mono.just(paperAddresses));

        // Assert
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaperAddresses.class);
    }

    @Test
    void updatePaperAddress_ServiceError() {
        // Arrange
        String paperRequestId = "paperReq_error";
        String paperAddressId = "addr_error";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}/{paperAddressId}"
                .replace("{paperRequestId}", paperRequestId)
                .replace("{paperAddressId}", paperAddressId);

        PaperAddress paperAddressDto = buildPaperAddress();

        // Act
        Mockito.when(paperAddressService.updatePaperAddress(anyString(), anyString(), any(PaperAddress.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        // Assert
        webTestClient.put()
                .uri(url)
                .bodyValue(paperAddressDto)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void deletePaperAddress_ServiceError() {
        // Arrange
        String paperRequestId = "paperReq_error";
        String paperAddressId = "addr_error";
        String url = "/datavault-private/v1/paper-addresses/{paperRequestId}/{paperAddressId}"
                .replace("{paperRequestId}", paperRequestId)
                .replace("{paperAddressId}", paperAddressId);

        // Act
        Mockito.when(paperAddressService.deletePaperAddress(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        // Assert
        webTestClient.delete()
                .uri(url)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void createPaperAddress() {
        // Arrange
        String iun = "UYLD-ANKP-GTNE-202511-V-1";
        String url = "/datavault-private/v1/paper-addresses/{iun}".replace("{iun}", iun);

        var paperAddressRequest = new it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddressRequest();
        paperAddressRequest.setPaperAddress(new it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddress()
                .address("Via Test 1")
                .city("Roma")
                .name("Mario Rossi"));
        paperAddressRequest.setAttempt(1);
        paperAddressRequest.setRecIndex(0);
        paperAddressRequest.setPcRetry(0);
        paperAddressRequest.setNormalized(true);
        paperAddressRequest.setAddressType("RESIDENCE");

        var paperAddressEntity = new PaperAddressEntity();
        paperAddressEntity.setAddressId("addr_789");
        paperAddressEntity.setPaperRequestId("PAPER_ADDR#"+iun);

        // Act
        Mockito.when(paperAddressService.createPaperAddress(Mockito.eq(iun), Mockito.any()))
                .thenReturn(Mono.just(paperAddressEntity));

        // Assert
        webTestClient.put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paperAddressRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddressResponse.class)
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assert body != null;
                    assert "addr_789".equals(body.getPaperAddressId());
                });
    }

    static PaperAddress buildPaperAddress() {
        return new PaperAddress()
                .address("TEST")
                .city("TEST")
                .name("TEST");
    }
}