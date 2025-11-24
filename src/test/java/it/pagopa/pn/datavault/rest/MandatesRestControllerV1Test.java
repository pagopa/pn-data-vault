package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.mapper.MandateEntityMandateDtoMapper;
import it.pagopa.pn.datavault.svc.MandateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@WebFluxTest(controllers = {MandatesRestControllerV1.class})
@Import(MandateEntityMandateDtoMapper.class)
class MandatesRestControllerV1Test {


    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MandateEntityMandateDtoMapper mapper;

    @MockitoBean
    private MandateService privateService;



    @Test
    void updateMandateById() {
        //Given
        String url = "/datavault-private/v1/mandates/{mandateId}"
                .replace("{mandateId}", "123e4567-e89b-12d3-a456-426655440000");
        MandateDto dto = mapper.toDto(TestUtils.newMandate(true));



        //When
        Mockito.when( privateService.updateMandateByInternalId( Mockito.anyString(), Mockito.any() ))
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
    void getMandatesByIds() {
        //Given
        String url = "/datavault-private/v1/mandates?mandateId={mandateId}"
                .replace("{mandateId}", "123e4567-e89b-12d3-a456-426655440000");

        MandateDto dto = mapper.toDto(TestUtils.newMandate(true));
        MandateDto dto1 = mapper.toDto(TestUtils.newMandate(false));
        List<MandateDto> list = new ArrayList<>();
        list.add(dto);
        list.add(dto1);

        //When
        Mockito.when( privateService.getMandatesByInternalIds( Mockito.any()))
                .thenReturn(Flux.fromIterable(list));

        //Then
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(MandateDto.class);
    }

    @Test
    void deleteMandateById() {

        //Given
        String url = "/datavault-private/v1/mandates/{mandateId}"
                .replace("{mandateId}", "123e4567-e89b-12d3-a456-426655440000");

        //When
        Mockito.when( privateService.deleteMandateByInternalId( Mockito.anyString()))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.delete()
                .uri(url)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }
}