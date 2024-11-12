package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.AddressEntityAddressDtoMapper;
import it.pagopa.pn.datavault.svc.AddressService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

@WebFluxTest(controllers = {AddressBookRestControllerV1.class})
@Import(AddressEntityAddressDtoMapper.class)
class AddressBookRestControllerV1Test {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    AddressEntityAddressDtoMapper mapper;

    @MockBean
    private AddressService privateService;



    @Test
    void updateRecipientAddressByInternalId() {
        //Given
        String url = "/datavault-private/v1/recipients/internal/{internalId}/addresses/{addressId}"
                .replace("{internalId}", "123e4567-e89b-12d3-a456-426655440000")
                .replace("{addressId}", "DD_c_f205_1");
        AddressDto dto = mapper.toDto(TestUtils.newAddress());


        //When
        Mockito.when( privateService.updateAddressByInternalId( Mockito.anyString(), Mockito.anyString(), Mockito.any(), isNull() ))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.put()
                .uri(url)
                .bodyValue(dto)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void updateRecipientAddressByInternalIdWithTtl() {
        //Given
        String url = "/datavault-private/v1/recipients/internal/{internalId}/addresses/{addressId}?ttl={ttl}"
                .replace("{internalId}", "123e4567-e89b-12d3-a456-426655440000")
                .replace("{addressId}", "DD_c_f205_1")
                .replace("{ttl}", "10");
        AddressDto dto = mapper.toDto(TestUtils.newAddress());


        //When
        Mockito.when( privateService.updateAddressByInternalId( Mockito.anyString(), Mockito.anyString(), Mockito.any(), eq(BigDecimal.TEN) ))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.put()
                .uri(url)
                .bodyValue(dto)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getRecipientAddressesByInternalId() {
        //Given
        String url = "/datavault-private/v1/recipients/internal/{internalId}/addresses"
                .replace("{internalId}", "123e4567-e89b-12d3-a456-426655440000");

        AddressDto dto = mapper.toDto(TestUtils.newAddress());
        RecipientAddressesDto r = new RecipientAddressesDto();

        r.putAddressesItem("DD_c_f205_1", dto);

        //When
        Mockito.when( privateService.getAddressByInternalId( Mockito.anyString()))
                .thenReturn(Mono.just(r));

        //Then
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBody(RecipientAddressesDto.class);
    }

    @Test
    void deleteRecipientAddressByInternalId() {
        //Given
        String url = "/datavault-private/v1/recipients/internal/{internalId}/addresses/{addressId}"
                .replace("{internalId}", "123e4567-e89b-12d3-a456-426655440000")
                .replace("{addressId}", "DD_c_f205_1");


        //When
        Mockito.when( privateService.deleteAddressByInternalId( Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.delete()
                .uri(url)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }
}